package com.qiujie.constants;

public class RedisConstants {
    private RedisConstants(){}

    // 登录相关

    /**
     * 登录验证码前缀
     */
    public static final String LOGIN_CODE_KEY = "login:code:";
    /**
     * 登录验证码有效期 min
     */
    public static final Long LOGIN_CODE_TTL = 200L;
    /**
     * 登录用户前缀
     */
    public static final String LOGIN_USER_KEY = "login:user:";
    /**
     * 登录用户有效期 min
     */
    public static final Long LOGIN_USER_TTL = 3600L;


    // 店铺相关

    /**
     * 空对象的有效期 s
     */
    public static final Long CACHE_NULL_TTL = 10L;
    /**
     * 店铺缓存数据有效期
     */
    public static final Long CACHE_SHOP_TTL = 30L;
    /**
     * 缓存店铺数据前缀
     */
    public static final String CACHE_SHOP_KEY = "cache:shop:";
    /**
     * 缓存店铺类型前缀
     */
    public static final String CACHE_SHOP_TYPE_KEY = "cache:type:";
    /**
     * 店铺类型缓存数据有效期
     */
    public static final Long CACHE_SHOP_TYPE_TTL = 30L;
    /**
     * 店铺数据缓存逻辑过期时间 s
     */
    public static final Long CACHE_SHOP_LOGICAL_TTL = 10L;


    // 订单相关

    /**
     * 分布式ID前缀
     */
    public static final String ID_PREFIX = "icr:";
    /**
     * 秒杀券订单ID前缀
     */
    public static final String SECKILL_VOUCHER_ORDER = "order";


    // 秒杀缓存

    /** 秒杀活动场次缓存键 */
    public static final String CACHE_SECKILL_ACTIVE_KEY = "cache:seckill:active";
    /** 即将开始场次缓存键 */
    public static final String CACHE_SECKILL_UPCOMING_KEY = "cache:seckill:upcoming";
    /** 秒杀场次缓存 TTL（秒） */
    public static final Long CACHE_SECKILL_SESSION_TTL = 30L;

    // 优惠券相关

    /**
     * 秒杀券库存
     */
    public static final String SECKILL_STOCK_KEY = "seckill:stock:";


    // 锁相关

    /**
     * 订单分布式锁前缀
     */
    public static final String LOCK_ORDER_KEY = "lock:order:";
    /**
     * 店铺分布式锁
     */
    public static final String LOCK_SHOP_KEY = "lock:shop:";

    public static final String LOCK_PRODUCT_KEY = "lock:product:";

    public static final Long LOCK_TTL = 10L;

    // 队列相关

    /**
     * 队列名
     */
    public static final String QUEUE_NAME = "stream.orders";
    /**
     * 消费者组
     */
    public static final String GROUP_NAME = "g1";
    /**
     * 起始消费者偏移量
     * 指定的偏移量为0，那么Redis会从最早的可用消息开始提供给该消费者组的消费者
     */
    public static final String OFF_SET = "0";


    // 博客相关

    /**
     * 博客点赞key前缀
     */
    public static final String BLOG_LIKED_KEY = "blog:liked:";
    /**
     * 粉丝key前缀
     */
    public static final String FOLLOW_KEY = "follows:";
    /**
     * Feed流前缀
     */
    public static final String FEED_KEY = "feed:";
    /**
     * 店铺坐标数据前缀
     */
    public static final String SHOP_GEO_KEY = "shop:geo:";
    /**
     * 用户签到前缀
     */
    public static final String USER_SIGN_KEY = "sign:";


    // 商品/分类缓存

    /**
     * 商品详情缓存前缀
     */
    public static final String CACHE_PRODUCT_KEY = "cache:product:";
    /**
     * 分类树缓存键
     */
    public static final String CACHE_CATEGORY_TREE_KEY = "cache:category:tree";
    /**
     * 首页数据缓存键
     */
    public static final String CACHE_HOME_KEY = "cache:product:home";
    /**
     * 商品缓存 TTL（秒）
     */
    public static final Long CACHE_PRODUCT_TTL = 1800L;
    /**
     * 分类缓存 TTL（秒）
     */
    public static final Long CACHE_CATEGORY_TTL = 1800L;
    /**
     * 首页缓存 TTL（秒）
     */
    public static final Long CACHE_HOME_TTL = 600L;


}
