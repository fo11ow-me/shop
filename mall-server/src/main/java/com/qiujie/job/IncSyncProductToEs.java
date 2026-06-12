package com.qiujie.job;

import com.qiujie.entity.Product;
import com.qiujie.mapper.ProductMapper;
import com.qiujie.service.EsSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ES 增量同步定时任务 — 每分钟扫描 MySQL 最近变更的商品批量写入 ES
 *
 * @author qiujie
 */
@Component
@Profile("!test")
public class IncSyncProductToEs {

    private static final Logger log = LoggerFactory.getLogger(IncSyncProductToEs.class);

    private final ProductMapper productMapper;
    private final EsSyncService esSyncService;

    public IncSyncProductToEs(ProductMapper productMapper, EsSyncService esSyncService) {
        this.productMapper = productMapper;
        this.esSyncService = esSyncService;
    }

    /**
     * 每分钟扫描最近 2 分钟 update_time 变更的商品，批量同步到 ES
     */
    @Scheduled(fixedRate = 60_000)
    public void incSync() {
        try {
            LocalDateTime since = LocalDateTime.now().minusMinutes(2);
            List<Product> products = productMapper.selectByUpdateTime(since);
            if (!products.isEmpty()) {
                esSyncService.syncBatchToEs(products);
                log.debug("ES 增量同步完成: {} 个商品", products.size());
            }
        } catch (Exception e) {
            log.warn("ES 增量同步异常: {}", e.getMessage());
        }
    }
}
