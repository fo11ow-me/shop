package com.qiujie.util;

import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import static com.qiujie.constants.RedisConstants.*;

/**
 * 商品销量排行榜服务 — Caffeine 本地聚合 + Redis ZSet 持久化
 *
 * @author qiujie
 */
@Component
public class SalesRankService {

    private static final Logger log = LoggerFactory.getLogger(SalesRankService.class);

    private final RedisUtil redisUtil;
    private final Cache<Integer, LongAdder> pendingSales;

    public SalesRankService(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
        this.pendingSales = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .removalListener(this::flushToRedis)
                .build();
    }

    /** 记录商品销量增加 */
    public void recordSale(Integer productId, int amount) {
        if (amount <= 0) return;
        pendingSales.asMap().computeIfAbsent(productId, k -> new LongAdder()).add(amount);
    }

    /** 记录销量回退（取消/超时） */
    public void recordCancel(Integer productId, int amount) {
        if (amount <= 0) return;
        pendingSales.asMap().computeIfAbsent(productId, k -> new LongAdder()).add(-amount);
    }

    /** 获取 Top 5，带读缓存 */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getTop5() {
        String cached = (String) redisUtil.get(CACHE_SALES_RANK_TOP5);
        if (cached != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = JSONUtil.toBean(cached, List.class);
            return list;
        }
        Set<ZSetOperations.TypedTuple<Object>> rank =
                redisUtil.zRevRangeWithScores(PRODUCT_SALES_RANK_KEY, 0, 4);
        if (rank == null || rank.isEmpty()) return List.of();
        List<Map<String, Object>> result = rank.stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("productId", Integer.valueOf(Objects.toString(t.getValue())));
            m.put("salesCount", t.getScore() != null ? t.getScore().intValue() : 0);
            return m;
        }).collect(Collectors.toList());
        redisUtil.set(CACHE_SALES_RANK_TOP5, JSONUtil.toJsonStr(result), 3);
        return result;
    }

    /** 从 MySQL 全量重建 ZSet + 刷新读缓存 */
    public void rebuildFromDb(java.util.function.Supplier<Map<Integer, Integer>> dbSupplier) {
        Map<Integer, Integer> salesFromDb = dbSupplier.get();
        for (Map.Entry<Integer, Integer> e : salesFromDb.entrySet()) {
            redisUtil.zIncrBy(PRODUCT_SALES_RANK_KEY, e.getKey().toString(), e.getValue());
        }
        redisUtil.expire(PRODUCT_SALES_RANK_KEY, 86400);
        redisUtil.del(CACHE_SALES_RANK_TOP5);
        log.info("Sales rank rebuilt: {} products", salesFromDb.size());
    }

    /** Caffeine 过期回调 — 批量写入 Redis */
    private void flushToRedis(Integer productId, LongAdder adder, RemovalCause cause) {
        long delta = adder.sum();
        if (delta == 0) return;
        redisUtil.zIncrBy(PRODUCT_SALES_RANK_KEY, productId.toString(), delta);
        redisUtil.del(CACHE_SALES_RANK_TOP5);
        if (redisUtil.getTime(PRODUCT_SALES_RANK_KEY) < 0) {
            redisUtil.expire(PRODUCT_SALES_RANK_KEY, 86400);
        }
        log.debug("Flushed sales rank: product={} delta={}", productId, delta);
    }

    @PreDestroy
    void shutdown() {
        pendingSales.asMap().forEach((productId, adder) -> {
            long delta = adder.sum();
            if (delta != 0) {
                redisUtil.zIncrBy(PRODUCT_SALES_RANK_KEY, productId.toString(), delta);
            }
        });
        pendingSales.invalidateAll();
        redisUtil.del(CACHE_SALES_RANK_TOP5);
    }
}
