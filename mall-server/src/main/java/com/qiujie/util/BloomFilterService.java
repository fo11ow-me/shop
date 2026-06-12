package com.qiujie.util;

import com.qiujie.mapper.ProductMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 商品 ID 集合 — 防缓存穿透
 *
 * @author qiujie
 */
@Component
@Profile("!test")
public class BloomFilterService {

    private static final Logger log = LoggerFactory.getLogger(BloomFilterService.class);

    private final ProductMapper productMapper;
    private Set<Integer> productIds = Collections.emptySet();

    public BloomFilterService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @PostConstruct
    void init() {
        try {
            List<Integer> ids = productMapper.selectAllIds();
            productIds = new HashSet<>(ids);
            log.info("商品 ID 集合初始化完成: {} 个", ids.size());
        } catch (Throwable e) {
            log.warn("商品 ID 集合初始化失败，缓存穿透保护暂时不可用: {}", e.getMessage());
        }
    }

    public boolean mightContain(Integer id) {
        return productIds.contains(id);
    }

    public void add(Integer id) {
        productIds.add(id);
    }
}
