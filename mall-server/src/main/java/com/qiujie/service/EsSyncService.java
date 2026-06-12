package com.qiujie.service;

import com.qiujie.document.ProductDocument;
import com.qiujie.entity.Product;
import com.qiujie.entity.ProductImg;
import com.qiujie.enums.ProductStatusEnum;
import com.qiujie.mapper.ProductImgMapper;
import com.qiujie.repository.ProductSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * ES 异步同步服务，避免阻塞 HTTP 响应线程
 *
 * @author qiujie
 */
@Service
public class EsSyncService {

    private static final Logger log = LoggerFactory.getLogger(EsSyncService.class);

    @Autowired(required = false)
    private ProductSearchRepository productSearchRepository;

    private final ProductImgMapper productImgMapper;

    public EsSyncService(ProductImgMapper productImgMapper) {
        this.productImgMapper = productImgMapper;
    }

    @Async
    public void syncToEs(Product product) {
        if (productSearchRepository == null) return;
        try {
            if (product.getStatus() == ProductStatusEnum.ON_SHELF) {
                productSearchRepository.save(toDocument(product));
            }
        } catch (Exception e) {
            log.warn("ES 同步商品 {} 失败: {}", product.getId(), e.getMessage());
        }
    }

    private ProductDocument toDocument(Product product) {
        ProductImg img = productImgMapper.selectFirstByProductId(product.getId());
        String imageUrl = img != null ? img.getUrl() : null;
        return buildDocument(product, imageUrl);
    }

    private ProductDocument toDocument(Product product, String imageUrl) {
        return buildDocument(product, imageUrl);
    }

    private ProductDocument buildDocument(Product product, String imageUrl) {
        ProductDocument doc = new ProductDocument();
        doc.setId(product.getId());
        doc.setName(product.getName());
        doc.setDetail(product.getDetail());
        doc.setPrice(product.getPrice());
        doc.setStock(product.getStock());
        doc.setCategoryId(product.getCategoryId());
        doc.setStatus(product.getStatus().getCode());
        doc.setCreateTime(product.getCreateTime());
        if (imageUrl != null) {
            doc.setImage(imageUrl);
        }
        return doc;
    }

    /**
     * 批量同步商品到 ES，定时增量补偿使用
     */
    @Async
    public void syncBatchToEs(List<Product> products) {
        if (productSearchRepository == null || products.isEmpty()) return;
        try {
            List<Integer> productIds = products.stream().map(Product::getId).toList();
            Map<Integer, String> imgMap = productImgMapper.selectByProductIds(productIds).stream()
                    .collect(Collectors.toMap(ProductImg::getProductId, ProductImg::getUrl, (a, b) -> a));
            List<ProductDocument> docs = products.stream()
                    .filter(p -> p.getStatus() == ProductStatusEnum.ON_SHELF)
                    .map(p -> toDocument(p, imgMap.get(p.getId())))
                    .collect(Collectors.toList());
            if (!docs.isEmpty()) {
                productSearchRepository.saveAll(docs);
            }
        } catch (Exception e) {
            log.warn("ES 批量同步失败: {}", e.getMessage());
        }
    }

    public void deleteFromEs(Integer id) {
        if (productSearchRepository == null) return;
        try {
            productSearchRepository.deleteById(id);
        } catch (Exception ignored) {
        }
    }
}
