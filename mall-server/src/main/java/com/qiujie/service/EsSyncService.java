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
                ProductDocument doc = new ProductDocument();
                doc.setId(product.getId());
                doc.setName(product.getName());
                doc.setDetail(product.getDetail());
                doc.setPrice(product.getPrice());
                doc.setStock(product.getStock());
                doc.setCategoryId(product.getCategoryId());
                doc.setStatus(product.getStatus().getCode());
                doc.setCreateTime(product.getCreateTime());
                ProductImg img = productImgMapper.selectFirstByProductId(product.getId());
                if (img != null) {
                    doc.setImage(img.getUrl());
                }
                productSearchRepository.save(doc);
            }
        } catch (Exception e) {
            log.warn("ES 同步商品 {} 失败: {}", product.getId(), e.getMessage());
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
