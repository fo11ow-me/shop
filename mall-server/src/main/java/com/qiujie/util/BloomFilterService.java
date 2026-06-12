package com.qiujie.util;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import com.qiujie.mapper.ProductMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 商品 ID Bloom 过滤器 — 防缓存穿透
 *
 * @author qiujie
 */
@Component
@Profile("!test")
public class BloomFilterService {

    private static final Logger log = LoggerFactory.getLogger(BloomFilterService.class);
    private static final int EXPECTED_INSERTIONS = 10000;

    private final ProductMapper productMapper;
    private BitMapBloomFilter bloomFilter;

    public BloomFilterService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @PostConstruct
    void init() {
        try {
            List<Integer> ids = productMapper.selectAllIds();
            bloomFilter = new BitMapBloomFilter(Math.max(ids.size(), EXPECTED_INSERTIONS));
            for (Integer id : ids) {
                bloomFilter.add(id.toString());
            }
            log.info("Bloom 过滤器初始化完成: {} 个商品 ID", ids.size());
        } catch (Throwable e) {
            log.warn("Bloom 过滤器初始化失败，缓存穿透保护暂时不可用: {}", e.getMessage());
            bloomFilter = null;
        }
    }

    public boolean mightContain(Integer id) {
        if (bloomFilter == null) return true;
        return bloomFilter.contains(id.toString());
    }

    public void add(Integer id) {
        if (bloomFilter != null) bloomFilter.add(id.toString());
    }
}
