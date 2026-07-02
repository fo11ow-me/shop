package com.qiujie.service.impl;

// 静态导入 Redis 常量（如 CACHE_SECKILL_ACTIVE_KEY、SECKILL_STOCK_KEY 等），简化代码中常量引用
import static com.qiujie.constants.RedisConstants.*;

// Hutool JSON 工具类，用于对象与 JSON 字符串之间的序列化/反序列化
import cn.hutool.json.JSONUtil;
// MyBatis-Plus 分页结果接口
import com.baomidou.mybatisplus.core.metadata.IPage;
// MyBatis-Plus 分页对象，封装 current/size 参数
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// MyBatis-Plus ServiceImpl 基类，提供通用 CRUD 方法（save、update、remove、list 等）
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
// RabbitMQ 交换机/队列/路由键常量定义
import com.qiujie.config.RabbitMQConfig;
// 商品实体
import com.qiujie.entity.Product;
// 商品图片实体（封面图）
import com.qiujie.entity.ProductImg;
// 秒杀场次实体，对应数据库 seckill_session 表
import com.qiujie.entity.SeckillSession;
// 业务状态枚举，定义统一的错误码和提示信息
import com.qiujie.enums.BusinessStatusEnum;
// 自定义业务异常，携带 BusinessStatusEnum 错误码
import com.qiujie.exception.ServiceException;
// 商品图片 Mapper，提供按 productIds 批量查询的方法
import com.qiujie.mapper.ProductImgMapper;
// 商品 Mapper，继承 MyBatis-Plus BaseMapper
import com.qiujie.mapper.ProductMapper;
// 秒杀场次 Mapper，包含自定义 SQL（selectActiveSessions / selectUpcomingSessions）
import com.qiujie.mapper.SeckillSessionMapper;
// 秒杀消息体，发送到 RabbitMQ 的订单创建消息
import com.qiujie.mq.SeckillMessage;
// 秒杀服务接口
import com.qiujie.service.SeckillService;
// Redis 工具类，封装了 RedisTemplate 的常用操作和 Lua 脚本执行
import com.qiujie.util.RedisUtil;
// Spring RabbitMQ 模板，用于发送消息到交换机
import org.springframework.amqp.rabbit.core.RabbitTemplate;
// Spring 服务层注解，标记为 Spring Bean
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 秒杀活动业务实现
 * <p>
 * 核心设计：
 * <ul>
 *   <li>库存扣减：Redis Lua 脚本原子执行（初始化库存 + 防重检查 + 扣减 + 标记用户）</li>
 *   <li>订单创建：RabbitMQ 异步削峰，避免高并发直接冲击数据库</li>
 *   <li>缓存策略：进行中/即将开始的场次列表使用 Cache-Aside 模式缓存</li>
 *   <li>可靠性：MQ 发送失败时 Lua 脚本回滚 Redis 库存，保证数据一致</li>
 *   <li>秒杀结果：通过 Redis 轮询获取订单创建结果（异步解耦）</li>
 * </ul>
 *
 * @author qiujie
 */
@Service
public class SeckillServiceImpl extends ServiceImpl<SeckillSessionMapper, SeckillSession> implements SeckillService {

    // Redis Key 前缀：用户秒杀订单标记，格式 seckill:order:{场次ID}:{用户ID}
    // 用于防重判断——同一用户在同一场次只能秒杀一次
    private static final String SECKILL_ORDER_KEY = "seckill:order:";
    // Redis Key 前缀：秒杀结果缓存，格式 seckill:result:{场次ID}:{用户ID}
    // 前端轮询此 Key 获取订单创建结果（排队中 / 成功 / 失败）
    private static final String SECKILL_RESULT_KEY = "seckill:result:";

    // 秒杀场次 Mapper，包含自定义 SQL：selectActiveSessions / selectUpcomingSessions
    private final SeckillSessionMapper seckillSessionMapper;
    // 商品 Mapper，用于批量查询秒杀场次关联的商品信息
    private final ProductMapper productMapper;
    // 商品图片 Mapper，用于批量查询商品封面图
    private final ProductImgMapper productImgMapper;
    // Redis 工具类，封装 get/set/del/executeLua 操作
    private final RedisUtil redisUtil;
    // RabbitMQ 模板，用于发送秒杀订单创建消息
    private final RabbitTemplate rabbitTemplate;

    /**
     * 构造器注入（Spring 推荐方式，无需 @Autowired）
     * <p>
     * 所有依赖通过 final 字段 + 构造器注入，保证不可变性和线程安全。
     *
     * @param seckillSessionMapper 秒杀场次 Mapper
     * @param productMapper        商品 Mapper
     * @param productImgMapper     商品图片 Mapper
     * @param redisUtil            Redis 工具类
     * @param rabbitTemplate       RabbitMQ 模板
     */
    public SeckillServiceImpl(SeckillSessionMapper seckillSessionMapper,
                              ProductMapper productMapper,
                              ProductImgMapper productImgMapper,
                              RedisUtil redisUtil,
                              RabbitTemplate rabbitTemplate) {
        this.seckillSessionMapper = seckillSessionMapper;
        this.productMapper = productMapper;
        this.productImgMapper = productImgMapper;
        this.redisUtil = redisUtil;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 获取当前正在进行中的秒杀场次列表
     * <p>
     * 使用 Cache-Aside 模式：
     * <ol>
     *   <li>先查 Redis 缓存</li>
     *   <li>命中则直接返回</li>
     *   <li>未命中则查数据库，组装数据后写入缓存并返回</li>
     * </ol>
     *
     * @return 正在进行中的秒杀场次列表，每个 Map 包含场次信息 + 商品信息 + 商品图片
     */
    @Override
    @SuppressWarnings("unchecked") // JSONUtil.toList 返回原始 List，转为泛型时产生警告
    public List<Map<String, Object>> getActiveSessions() {
        // 从 Redis 读取进行中场次缓存，Key 为 cache:seckill:active
        String cached = (String) redisUtil.get(CACHE_SECKILL_ACTIVE_KEY);
        if (cached != null) {
            // 缓存命中：将 JSON 字符串反序列化为 List<Map> 后直接返回，跳过数据库查询
            // JSONUtil.toList 返回 List<Map>（原始类型），通过 (List<?>) 中间转换避免编译警告
            return (List<Map<String, Object>>) (List<?>) JSONUtil.toList(cached, Map.class);
        }

        // 缓存未命中：查询当前时间处于 start_time ~ end_time 之间的秒杀场次
        List<SeckillSession> sessions = seckillSessionMapper.selectActiveSessions(LocalDateTime.now());
        // 批量查询所有场次关联的商品，避免 N+1 问题
        Map<Integer, Product> productMap = batchProducts(sessions);
        // 批量查询所有场次关联的商品封面图，避免 N+1 问题
        Map<Integer, ProductImg> imgMap = batchProductImages(sessions);

        // 将场次实体组装为前端需要的 Map 结构（含商品名称、价格、封面图等）
        List<Map<String, Object>> result = new ArrayList<>();
        for (SeckillSession session : sessions) {
            result.add(sessionToMap(session, productMap.get(session.getProductId()),
                    imgMap.get(session.getProductId())));
        }
        // 写入 Redis 缓存，TTL 由 CACHE_SECKILL_SESSION_TTL 常量控制
        redisUtil.set(CACHE_SECKILL_ACTIVE_KEY, JSONUtil.toJsonStr(result), CACHE_SECKILL_SESSION_TTL);
        return result;
    }

    /**
     * 获取即将开始的秒杀场次列表（start_time > 当前时间）
     * <p>
     * 同样使用 Cache-Aside 模式，逻辑与 getActiveSessions 完全对称。
     *
     * @return 即将开始的秒杀场次列表
     */
    @Override
    @SuppressWarnings("unchecked") // 同上：JSONUtil.toList 原始类型转换
    public List<Map<String, Object>> getUpcomingSessions() {
        // 从 Redis 读取即将开始场次缓存，Key 为 cache:seckill:upcoming
        String cached = (String) redisUtil.get(CACHE_SECKILL_UPCOMING_KEY);
        if (cached != null) {
            // 缓存命中：直接反序列化返回
            return (List<Map<String, Object>>) (List<?>) JSONUtil.toList(cached, Map.class);
        }

        // 缓存未命中：查询 start_time > 当前时间 的秒杀场次
        List<SeckillSession> sessions = seckillSessionMapper.selectUpcomingSessions(LocalDateTime.now());
        Map<Integer, Product> productMap = batchProducts(sessions);
        Map<Integer, ProductImg> imgMap = batchProductImages(sessions);

        List<Map<String, Object>> result = new ArrayList<>();
        for (SeckillSession session : sessions) {
            result.add(sessionToMap(session, productMap.get(session.getProductId()),
                    imgMap.get(session.getProductId())));
        }
        // 写入 Redis 缓存
        redisUtil.set(CACHE_SECKILL_UPCOMING_KEY, JSONUtil.toJsonStr(result), CACHE_SECKILL_SESSION_TTL);
        return result;
    }

    /**
     * 执行秒杀——核心方法
     * <p>
     * 流程：
     * <ol>
     *   <li>校验场次是否存在且处于活动时间范围内</li>
     *   <li>Redis Lua 脚本原子执行：库存初始化 → 防重检查 → 扣库存 → 标记用户已参与</li>
     *   <li>清除秒杀列表缓存，使下次查询获取最新库存</li>
     *   <li>发送 RabbitMQ 消息，异步创建订单（削峰）</li>
     *   <li>MQ 发送失败时，Lua 脚本回滚库存和用户标记</li>
     * </ol>
     * <p>
     * 为什么要用 Lua 脚本：
     * <ul>
     *   <li>原子性：Redis 单线程执行 Lua，避免「查库存 → 判断 → 扣库存」之间的竞态条件</li>
     *   <li>减少网络往返：4 次 Redis 操作合并为 1 次网络调用</li>
     * </ul>
     *
     * @param sessionId 秒杀场次 ID
     * @param userId    用户 ID
     * @throws ServiceException 场次不存在 / 已过期 / 重复秒杀 / 库存不足 / 系统异常
     */
    @Override
    public void execute(Integer sessionId, Integer userId) {
        // ========== 1. 校验秒杀场次存在且进行中 ==========
        SeckillSession session = seckillSessionMapper.selectById(sessionId);
        if (session == null) {
            // 场次不存在，抛出业务异常（全局异常处理器会转为统一响应格式）
            throw new ServiceException(BusinessStatusEnum.SECKILL_SESSION_NOT_EXIST);
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(session.getStartTime()) || now.isAfter(session.getEndTime())) {
            // 当前时间不在场次的 start_time ~ end_time 范围内
            throw new ServiceException(BusinessStatusEnum.SECKILL_SESSION_EXPIRED);
        }

        // ========== 2. Lua 脚本原子执行 ==========
        // Redis Key：秒杀库存，格式 seckill:stock:{场次ID}
        String stockKey = SECKILL_STOCK_KEY + sessionId;
        // Redis Key：用户秒杀标记，格式 seckill:order:{场次ID}:{用户ID}
        // 用于防重——同一用户在同一场次只能秒杀一次
        String orderKey = SECKILL_ORDER_KEY + sessionId + ":" + userId;
        // Lua 脚本：Redis 单线程原子执行，避免并发竞态
        // KEYS[1] = stockKey   KEYS[2] = orderKey
        // ARGV[1] = 初始库存   ARGV[2] = 用户标记过期时间(秒)
        String lua = """
            local stockKey = KEYS[1]
            local orderKey = KEYS[2]
            local initStock = tonumber(ARGV[1])
            local orderTtl = tonumber(ARGV[2])

            -- 库存 Key 不存在 → 首次访问，用数据库库存初始化 Redis 库存
            if redis.call('EXISTS', stockKey) == 0 then
                redis.call('SET', stockKey, initStock)
            end

            -- 防重检查：用户秒杀标记 Key 是否存在
            if redis.call('EXISTS', orderKey) == 1 then
                return -1    -- 重复秒杀
            end

            -- 读取库存并去引号后转为数字
            -- 注：RedisTemplate 序列化会给值加双引号，需 gsub('"','') 去除
            local raw = redis.call('GET', stockKey) or '0'
            raw = raw:gsub('"', '')
            local stock = tonumber(raw)
            if stock <= 0 then
                return -2    -- 库存不足
            end

            -- 扣库存 + 标记用户已参与（带 TTL）
            redis.call('SET', stockKey, stock - 1)
            redis.call('SET', orderKey, '1', 'EX', orderTtl)
            return 1          -- 秒杀成功
            """;

        // 执行 Lua 脚本：传入 KEYS 列表和 ARGV 参数列表
        // 返回值类型为 Long：-1=重复，-2=库存不足，1=成功
        Long result = redisUtil.executeLua(lua, Long.class,
                List.of(stockKey, orderKey),
                session.getSeckillStock(), 86400); // orderTtl=86400秒=24小时
        if (result == null) {
            // Lua 脚本执行异常（如 Redis 连接断开）
            throw new ServiceException(BusinessStatusEnum.ERROR);
        }
        if (result == -1) {
            // 用户已参与过该场次秒杀
            throw new ServiceException(BusinessStatusEnum.SECKILL_DUPLICATE);
        }
        if (result == -2) {
            // 库存已耗尽
            throw new ServiceException(BusinessStatusEnum.SECKILL_STOCK_EMPTY);
        }

        // ========== 3. 失效秒杀缓存 ==========
        // 库存已变化，删除进行中/即将开始场次的列表缓存
        // 下次查询时 Cache-Aside 模式会重新从数据库加载并写入缓存
        redisUtil.del(CACHE_SECKILL_ACTIVE_KEY);
        redisUtil.del(CACHE_SECKILL_UPCOMING_KEY);

        // ========== 4. 发送 RabbitMQ 消息，异步创建订单 ==========
        // 构建秒杀消息体，包含创建订单所需的全部字段
        SeckillMessage message = new SeckillMessage();
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setProductId(session.getProductId());
        message.setSeckillPrice(session.getSeckillPrice());
        if (rabbitTemplate == null) {
            // 防御性检查：RabbitMQ 未配置时直接抛异常
            throw new ServiceException(BusinessStatusEnum.ERROR);
        }
        try {
            // 发送消息到秒杀交换机，路由键为 seckill
            // 消息由 SeckillMessageListener 消费，执行数据库订单创建
            rabbitTemplate.convertAndSend(RabbitMQConfig.SECKILL_EXCHANGE,
                    RabbitMQConfig.SECKILL_ROUTING_KEY, message);
        } catch (Exception e) {
            // ========== MQ 发送失败：Lua 脚本回滚 ==========
            // 为什么需要回滚？库存已在步骤 2 中扣减，但订单创建消息未发出
            // 如果不回滚，用户扣了库存但永远收不到订单，造成"超卖但不成交"
            //
            // 回滚操作：
            //   KEYS[1]=stockKey  → 库存 +1
            //   KEYS[2]=orderKey  → 删除用户秒杀标记，允许重新秒杀
            String rollbackLua = """
                local raw = redis.call('GET', KEYS[1])
                if raw then
                    raw = raw:gsub('"', '')
                    redis.call('SET', KEYS[1], tonumber(raw) + 1)
                end
                redis.call('DEL', KEYS[2])
                """;
            // Void.class 表示不需要返回值，仅执行副作用
            redisUtil.executeLua(rollbackLua, Void.class, List.of(stockKey, orderKey));
            throw new ServiceException(BusinessStatusEnum.ERROR);
        }
    }

    /**
     * 获取秒杀结果——供前端轮询
     * <p>
     * 秒杀请求是异步处理的（Lua 扣库存后发 MQ），前端不知道订单是否创建成功。
     * 此方法读取 Redis 中的秒杀结果缓存，由 MQ 消费者在订单创建成功后写入。
     * <p>
     * 返回值：
     * <ul>
     *   <li>status=1, msg="秒杀成功" → 订单创建成功</li>
     *   <li>status=-1, msg="秒杀失败" → 订单创建失败（如库存回滚、MQ 重试耗尽）</li>
     *   <li>status=0, msg="排队中" → 订单尚未创建完成，前端继续轮询</li>
     * </ul>
     *
     * @param sessionId 秒杀场次 ID
     * @param userId    用户 ID
     * @return 秒杀结果 Map，包含 status 和 msg 字段
     */
    @Override
    public Map<String, Object> getResult(Integer sessionId, Integer userId) {
        // 拼装 Redis Key：seckill:result:{场次ID}:{用户ID}
        String resultKey = SECKILL_RESULT_KEY + sessionId + ":" + userId;
        Object cached = redisUtil.get(resultKey);
        if (cached != null) {
            // 已有结果：MQ 消费者已写入，直接解析返回
            return JSONUtil.parseObj(cached.toString());
        }
        // 尚无结果：订单还在 MQ 队列中或正在创建，返回"排队中"让前端继续轮询
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", 0);
        result.put("msg", "排队中");
        return result;
    }

    /**
     * 后台管理——秒杀场次分页列表
     * <p>
     * 支持按状态筛选：
     * <ul>
     *   <li>status=0 → 进行中（start_time <= now <= end_time）</li>
     *   <li>status=1 → 未开始（start_time > now）</li>
     *   <li>status=2 → 已结束（end_time < now）</li>
     *   <li>status=null → 全部</li>
     * </ul>
     *
     * @param current 当前页码
     * @param size    每页条数
     * @param status  筛选状态（可选）
     * @return 分页数据 Map：records / total / current / size
     */
    @Override
    public Map<String, Object> listPage(Integer current, Integer size, Integer status) {
        // 创建 MyBatis-Plus 分页对象
        Page<SeckillSession> page = new Page<>(current, size);
        // 查询条件构造器，根据 status 动态添加时间范围条件
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SeckillSession> wrapper = null;
        if (status != null) {
            wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            LocalDateTime now = LocalDateTime.now();
            if (status == 0) {
                // 进行中：start_time <= now <= end_time
                wrapper.le("start_time", now).ge("end_time", now);
            } else if (status == 1) {
                // 未开始：start_time > now
                wrapper.gt("start_time", now);
            } else if (status == 2) {
                // 已结束：end_time < now
                wrapper.lt("end_time", now);
            }
        }
        if (wrapper == null) {
            // 无筛选条件：按创建时间倒序
            wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.orderByDesc("create_time");
        } else {
            // 有筛选条件：仍按创建时间倒序，保持列表顺序一致
            wrapper.orderByDesc("create_time");
        }
        // 执行分页查询
        IPage<SeckillSession> result = seckillSessionMapper.selectPage(page, wrapper);

        // 批量查询关联商品和图片，避免循环中逐条查询（N+1 问题）
        List<SeckillSession> sessions = result.getRecords();
        Map<Integer, Product> productMap = batchProducts(sessions);
        Map<Integer, ProductImg> imgMap = batchProductImages(sessions);

        // 将商品名称、原价、封面图 URL 回填到 SeckillSession 实体中
        // 注意：这些字段在 SeckillSession 中使用 @TableField(exist=false) 标记，不存入数据库
        for (SeckillSession session : sessions) {
            Product product = productMap.get(session.getProductId());
            if (product != null) {
                session.setProductName(product.getName());
                session.setProductPrice(product.getPrice());
            }
            ProductImg img = imgMap.get(session.getProductId());
            if (img != null) {
                session.setProductImg(img.getUrl());
            }
        }

        // 组装分页响应 Map
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());   // 当前页记录列表
        data.put("total", result.getTotal());       // 总记录数
        data.put("current", result.getCurrent());   // 当前页码
        data.put("size", result.getSize());         // 每页条数
        return data;
    }

    /**
     * 后台管理——创建秒杀场次
     * <p>
     * 前置校验：
     * <ol>
     *   <li>必填字段非空（秒杀价、库存、开始/结束时间）</li>
     *   <li>结束时间必须晚于开始时间</li>
     *   <li>关联商品必须存在</li>
     * </ol>
     * 创建后初始化 Redis 库存，并删除列表缓存使下次查询可见新场次。
     *
     * @param session 秒杀场次实体（前端传入）
     */
    @Override
    public void create(SeckillSession session) {
        // 必填字段校验：秒杀价、秒杀库存、开始时间、结束时间
        if (session.getSeckillPrice() == null || session.getSeckillStock() == null
                || session.getStartTime() == null || session.getEndTime() == null) {
            throw new ServiceException(BusinessStatusEnum.PARAM_ERROR);
        }
        // 时间合法性校验：结束时间必须晚于开始时间
        if (!session.getEndTime().isAfter(session.getStartTime())) {
            throw new ServiceException(BusinessStatusEnum.PARAM_ERROR.getCode(), "结束时间必须晚于开始时间");
        }
        // 关联商品存在性校验
        Product product = productMapper.selectById(session.getProductId());
        if (product == null) {
            throw new ServiceException(BusinessStatusEnum.PRODUCT_NOT_EXIST);
        }
        // 插入秒杀场次记录到数据库
        seckillSessionMapper.insert(session);
        // 初始化 Redis 库存：Key = seckill:stock:{场次ID}，Value = 秒杀库存数量
        // 使用 String.valueOf 避免 RedisTemplate 序列化时添加引号
        redisUtil.set(SECKILL_STOCK_KEY + session.getId(), String.valueOf(session.getSeckillStock()));
        // 删除列表缓存：新场次可能影响进行中/即将开始列表，清除后下次查询重新加载
        redisUtil.del(CACHE_SECKILL_ACTIVE_KEY);
        redisUtil.del(CACHE_SECKILL_UPCOMING_KEY);
    }

    /**
     * 后台管理——更新秒杀场次
     * <p>
     * 额外校验：
     * <ol>
     *   <li>已结束的场次不允许修改</li>
     *   <li>秒杀价必须低于商品原价（在商品存在时）</li>
     * </ol>
     * 更新后同步 Redis 库存缓存。
     *
     * @param session 秒杀场次实体（含更新后的字段值）
     */
    @Override
    public void updateSession(SeckillSession session) {
        // 查询现有场次，确保场次存在
        SeckillSession existing = seckillSessionMapper.selectById(session.getId());
        if (existing == null) {
            throw new ServiceException(BusinessStatusEnum.SECKILL_SESSION_NOT_EXIST);
        }
        // 已结束的场次不允许修改（end_time < now）
        if (existing.getEndTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(BusinessStatusEnum.SECKILL_SESSION_EXPIRED);
        }
        // 时间合法性校验
        if (!session.getEndTime().isAfter(session.getStartTime())) {
            throw new ServiceException(BusinessStatusEnum.PARAM_ERROR.getCode(), "结束时间必须晚于开始时间");
        }
        // 秒杀价必须低于商品原价：防止配置错误导致秒杀价高于或等于原价
        Product product = productMapper.selectById(session.getProductId());
        if (product != null && session.getSeckillPrice() != null
                && session.getSeckillPrice().compareTo(product.getPrice()) >= 0) {
            throw new ServiceException(BusinessStatusEnum.PARAM_ERROR.getCode(), "秒杀价必须低于商品原价");
        }
        // 更新数据库记录
        seckillSessionMapper.updateById(session);
        // 同步 Redis 库存缓存：如果管理员修改了库存数量，Redis 中的库存数据需要同步
        redisUtil.set(SECKILL_STOCK_KEY + session.getId(), String.valueOf(session.getSeckillStock()));
        // 删除列表缓存
        redisUtil.del(CACHE_SECKILL_ACTIVE_KEY);
        redisUtil.del(CACHE_SECKILL_UPCOMING_KEY);
    }

    /**
     * 后台管理——删除秒杀场次
     * <p>
     * 约束：已结束的场次不允许删除（保护历史数据完整性）。
     * 删除后清理对应的 Redis 库存 Key 和列表缓存。
     *
     * @param id 秒杀场次 ID
     */
    @Override
    public void deleteSession(Integer id) {
        // 查询场次，确保存在
        SeckillSession session = seckillSessionMapper.selectById(id);
        if (session == null) {
            throw new ServiceException(BusinessStatusEnum.SECKILL_SESSION_NOT_EXIST);
        }
        // 已结束的场次不允许删除
        if (session.getEndTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(BusinessStatusEnum.SECKILL_SESSION_EXPIRED);
        }
        // 从数据库删除
        seckillSessionMapper.deleteById(id);
        // 清理 Redis：删除库存 Key 和列表缓存
        redisUtil.del(SECKILL_STOCK_KEY + id);
        redisUtil.del(CACHE_SECKILL_ACTIVE_KEY);
        redisUtil.del(CACHE_SECKILL_UPCOMING_KEY);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将秒杀场次实体转换为前端展示用的 Map 结构
     * <p>
     * 合并场次信息 + 商品信息 + 商品封面图 + 剩余库存（从 Redis 实时读取）。
     *
     * @param session 秒杀场次实体
     * @param product 关联的商品（可为 null）
     * @param img     关联的商品封面图（可为 null）
     * @return 前端展示用的 Map
     */
    private Map<String, Object> sessionToMap(SeckillSession session, Product product, ProductImg img) {
        // 使用 LinkedHashMap 保持字段插入顺序
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("sessionId", session.getId());
        map.put("productId", session.getProductId());

        // 商品存在时才填充商品相关字段
        if (product != null) {
            map.put("productName", product.getName());
            map.put("productPrice", product.getPrice());
            if (img != null) {
                map.put("productImg", img.getUrl());
            }
        }

        map.put("seckillPrice", session.getSeckillPrice());
        map.put("totalStock", session.getSeckillStock());
        // 剩余库存优先从 Redis 读取（实时），Redis 无数据时回退到数据库值
        map.put("remainingStock", getRemainingStock(session.getId(), session.getSeckillStock()));
        map.put("startTime", session.getStartTime());
        map.put("endTime", session.getEndTime());
        return map;
    }

    /**
     * 批量查询秒杀场次关联的商品
     * <p>
     * 避免在循环中逐条查询（N+1 问题），一次 SQL 查出所有关联商品。
     *
     * @param sessions 秒杀场次列表
     * @return productId → Product 的映射 Map
     */
    private Map<Integer, Product> batchProducts(List<SeckillSession> sessions) {
        // 空列表直接返回空 Map，避免无效数据库查询
        if (sessions.isEmpty()) return Map.of();
        // 收集所有场次的 productId，去重后转为 List
        List<Integer> productIds = sessions.stream()
                .map(SeckillSession::getProductId).distinct().toList();
        // MyBatis-Plus selectBatchIds：WHERE id IN (...)
        // 转为 Map，key=productId，遇重复取第一个（实际不会重复）
        return productMapper.selectBatchIds(productIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        Product::getId, p -> p, (a, b) -> a));
    }

    /**
     * 批量查询秒杀场次关联的商品首图
     * <p>
     * 每个商品可能有多张图片，取第一张作为封面图（SQL 层保证）。
     *
     * @param sessions 秒杀场次列表
     * @return productId → ProductImg 的映射 Map
     */
    private Map<Integer, ProductImg> batchProductImages(List<SeckillSession> sessions) {
        if (sessions.isEmpty()) return Map.of();
        List<Integer> productIds = sessions.stream()
                .map(SeckillSession::getProductId).distinct().toList();
        // 自定义 Mapper 方法：SELECT ... FROM product_img WHERE product_id IN (...) LIMIT 1 per product
        List<ProductImg> imgs = productImgMapper.selectByProductIds(productIds);
        // 转为 Map，key=productId，遇重复取第一个
        return imgs.stream().collect(java.util.stream.Collectors.toMap(
                ProductImg::getProductId, img -> img, (a, b) -> a));
    }

    /**
     * 获取秒杀场次剩余库存
     * <p>
     * 优先从 Redis 读取实时库存（秒杀过程中动态变化），
     * Redis 中无数据时（如场次刚创建、Redis 数据过期）回退到数据库值。
     *
     * @param sessionId 秒杀场次 ID
     * @param dbStock   数据库中的初始库存（兜底值）
     * @return 当前剩余库存数量
     */
    private Integer getRemainingStock(Integer sessionId, Integer dbStock) {
        // 从 Redis 读取实时库存：Key = seckill:stock:{场次ID}
        Object cached = redisUtil.get(SECKILL_STOCK_KEY + sessionId);
        if (cached != null) {
            // Redis 命中：返回实时库存（已扣减后的值）
            return Integer.parseInt(cached.toString());
        }
        // Redis 未命中：返回数据库初始库存（场次刚创建或 Redis 数据已过期）
        return dbStock;
    }
}
