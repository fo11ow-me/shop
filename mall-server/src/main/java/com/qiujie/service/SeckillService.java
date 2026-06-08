package com.qiujie.service;

import com.qiujie.entity.SeckillSession;

import java.util.List;
import java.util.Map;

/**
 * 秒杀活动业务接口
 *
 * @author qiujie
 */
public interface SeckillService {

    /**
     * 获取当前进行中的秒杀场次列表（含商品信息、剩余库存）
     *
     * @return 进行中的秒杀场次列表
     */
    List<Map<String, Object>> getActiveSessions();

    /**
     * 获取即将开始的秒杀场次列表（含商品信息）
     *
     * @return 即将开始的秒杀场次列表
     */
    List<Map<String, Object>> getUpcomingSessions();

    /**
     * 执行秒杀：校验场次有效性、防重复、扣减 Redis 库存、异步创建订单
     *
     * @param sessionId 秒杀场次 ID
     * @param userId    用户 ID
     */
    void execute(Integer sessionId, Integer userId);

    /**
     * 查询秒杀执行结果（供前端轮询）
     *
     * @param sessionId 秒杀场次 ID
     * @param userId    用户 ID
     * @return { status: 0=排队中, 1=成功, -1=失败, msg: 描述 }
     */
    Map<String, Object> getResult(Integer sessionId, Integer userId);

    // ========== 后台管理 CRUD ==========

    /** 秒杀场次分页列表 */
    Map<String, Object> listPage(Integer current, Integer size, Integer status);

    /** 创建秒杀场次，同时初始化 Redis 库存 */
    void create(SeckillSession session);

    /** 更新秒杀场次，同步失效相关缓存 */
    void updateSession(SeckillSession session);

    /** 删除秒杀场次（逻辑删除），已结束的不允许 */
    void deleteSession(Integer id);
}
