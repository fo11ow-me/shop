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
@Profile("!test")
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
        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);
            if (!indexOps.exists()) {
                log.info("ES 索引不存在，开始创建并全量导入...");
                indexOps.createWithMapping();
            } else {
                log.info("ES 索引已存在，执行增量同步...");
                indexOps.delete();
                indexOps.createWithMapping();
            }
            importAllProducts();
            log.info("ES 索引创建完成");
        } catch (Exception e) {
            log.error("ES 索引初始化失败（应用仍可正常启动）: {}", e.getMessage());
        }
    }

    private static final int BATCH_SIZE = 500;

    private void importAllProducts() {
        List<Product> products = productMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Product>()
                        .eq("is_deleted", 0));
        log.info("ES 全量导入：查询到 {} 个商品", products.size());

        List<ProductDocument> docs = products.stream()
                .filter(p -> p.getStatus() == ProductStatusEnum.ON_SHELF)
                .map(this::toDocument)
                .collect(java.util.stream.Collectors.toList());

        for (int i = 0; i < docs.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, docs.size());
            try {
                productSearchRepository.saveAll(docs.subList(i, end));
            } catch (Exception e) {
                log.error("ES 批量导入失败 [{}, {}): {}", i, end, e.getMessage());
            }
        }
        log.info("ES 导入完成：成功 {} 个, 跳过 {} 个 (非上架)", docs.size(), products.size() - docs.size());
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
