package com.qiujie.config;

import com.qiujie.document.ProductDocument;
import com.qiujie.entity.Product;
import com.qiujie.entity.ProductImg;
import com.qiujie.enums.ProductStatusEnum;
import com.qiujie.mapper.ProductImgMapper;
import com.qiujie.mapper.ProductMapper;
import com.qiujie.repository.ProductSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

import java.util.List;

/**
 * ES 配置 — 启动时检查索引并全量导入已有商品
 *
 * @author qiujie
 */
@Configuration
@Profile("prod")
public class ElasticsearchConfig implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchConfig.class);

    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductSearchRepository productSearchRepository;
    private final ProductMapper productMapper;
    private final ProductImgMapper productImgMapper;

    public ElasticsearchConfig(ElasticsearchOperations elasticsearchOperations,
                                ProductSearchRepository productSearchRepository,
                                ProductMapper productMapper,
                                ProductImgMapper productImgMapper) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.productSearchRepository = productSearchRepository;
        this.productMapper = productMapper;
        this.productImgMapper = productImgMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);
        if (!indexOps.exists()) {
            log.info("ES 索引不存在，开始创建并全量导入...");
            indexOps.createWithMapping();
        } else {
            log.info("ES 索引已存在，执行增量同步...");
            // 清空后重新导入，保证数据一致
            indexOps.delete();
            indexOps.createWithMapping();
        }
        importAllProducts();
        log.info("ES 索引创建完成");
    }

    private void importAllProducts() {
        List<Product> products = productMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Product>()
                        .eq("is_deleted", 0));
        log.info("ES 全量导入：查询到 {} 个商品", products.size());
        int count = 0;
        int skipped = 0;
        for (Product p : products) {
            if (p.getStatus() != ProductStatusEnum.ON_SHELF) {
                skipped++;
                log.debug("跳过商品 id={}, status={}", p.getId(), p.getStatus());
                continue;
            }
            try {
                ProductDocument doc = toDocument(p);
                productSearchRepository.save(doc);
                count++;
            } catch (Exception e) {
                log.error("ES 导入商品 id={} 失败: {}", p.getId(), e.getMessage());
            }
        }
        log.info("ES 导入完成：成功 {} 个, 跳过 {} 个 (非上架)", count, skipped);
    }

    private ProductDocument toDocument(Product p) {
        ProductDocument doc = new ProductDocument();
        doc.setId(p.getId());
        doc.setName(p.getName());
        doc.setDetail(p.getDetail());
        doc.setPrice(p.getPrice());
        doc.setStock(p.getStock());
        doc.setCategoryId(p.getCategoryId());
        doc.setStatus(p.getStatus() != null ? p.getStatus().getCode() : 0);
        doc.setCreateTime(p.getCreateTime());
        ProductImg img = productImgMapper.selectFirstByProductId(p.getId());
        if (img != null) {
            doc.setImage(img.getUrl());
        }
        return doc;
    }
}
