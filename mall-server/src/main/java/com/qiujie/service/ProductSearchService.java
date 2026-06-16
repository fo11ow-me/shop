package com.qiujie.service;

import com.qiujie.document.ProductDocument;

import java.util.List;
import java.util.Map;

/**
 * 商品搜索服务 — ES 搜索引擎 + MySQL 降级。
 *
 * @author qiujie
 */
public interface ProductSearchService {

    Map<String, Object> search(String keyword, Integer current, Integer size);

    List<ProductDocument> recommend(Integer productId, Integer size);
}
