package com.qiujie.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiujie.document.ProductDocument;
import com.qiujie.entity.Product;
import com.qiujie.entity.ProductImg;
import com.qiujie.enums.ProductStatusEnum;
import com.qiujie.mapper.ProductImgMapper;
import com.qiujie.mapper.ProductMapper;
import com.qiujie.service.ProductSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 商品搜索服务 — ES 优先，失败降级 MySQL。
 *
 * @author qiujie
 */
@Service
public class ProductSearchServiceImpl implements ProductSearchService {

    private static final Logger log = LoggerFactory.getLogger(ProductSearchServiceImpl.class);

    private final ProductMapper productMapper;
    private final ProductImgMapper productImgMapper;

    @Autowired(required = false)
    private ElasticsearchOperations elasticsearchOperations;

    public ProductSearchServiceImpl(ProductMapper productMapper, ProductImgMapper productImgMapper) {
        this.productMapper = productMapper;
        this.productImgMapper = productImgMapper;
    }

    @Override
    public Map<String, Object> search(String keyword, Integer current, Integer size) {
        if (keyword == null || keyword.trim().isEmpty()) return null;
        try {
            return searchFromEs(keyword, current, size);
        } catch (Exception e) {
            log.warn("ES 搜索失败，降级为 MySQL LIKE 查询: {}", e.getMessage());
            return searchFromMysql(keyword, current, size);
        }
    }

    private Map<String, Object> searchFromEs(String keyword, Integer current, Integer size) {
        if (elasticsearchOperations == null) throw new RuntimeException("ES not available");
        int pageNum = current != null ? current - 1 : 0;
        int pageSize = size != null ? size : 10;

        Query esQuery = Query.of(q -> q
                .bool(b -> b
                        .filter(f -> f.term(t -> t.field("status")
                                .value(v -> v.longValue(ProductStatusEnum.ON_SHELF.getCode()))))
                        .should(s -> s.match(m -> m.field("name").query(keyword.trim())))
                        .should(s -> s.match(m -> m.field("detail").query(keyword.trim())))
                        .minimumShouldMatch("1")));

        var nativeQuery = NativeQuery.builder()
                .withQuery(esQuery)
                .withPageable(PageRequest.of(pageNum, pageSize, Sort.by(Sort.Order.desc("_score"))))
                .build();

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);

        Map<String, Object> data = new HashMap<>();
        data.put("records", hits.getSearchHits().stream().map(h -> h.getContent()).toList());
        data.put("total", hits.getTotalHits());
        data.put("current", current != null ? current : 1);
        data.put("size", pageSize);
        return data;
    }

    private Map<String, Object> searchFromMysql(String keyword, Integer current, Integer size) {
        Page<Product> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        var result = productMapper.selectPageByKeyword(page, keyword.trim());
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        data.put("productImages", batchProductImages(result.getRecords()));
        return data;
    }

    @Override
    public List<ProductDocument> recommend(Integer productId, Integer size) {
        int limit = size != null ? size : 8;
        try {
            if (elasticsearchOperations != null) {
                ProductDocument source = elasticsearchOperations.get(
                        String.valueOf(productId), ProductDocument.class);
                if (source != null) {
                    var query = Query.of(q -> q.bool(b -> b
                            .mustNot(mn -> mn.term(t -> t.field("id")
                                    .value(v -> v.longValue(productId))))
                            .filter(f -> f.term(t -> t.field("status")
                                    .value(v -> v.longValue(ProductStatusEnum.ON_SHELF.getCode()))))
                            .should(s -> s.match(m -> m.field("name")
                                    .query(source.getName()).boost(3.0f)))
                            .should(s -> s.term(t -> t.field("categoryId")
                                    .value(v -> v.longValue(source.getCategoryId())).boost(5.0f)))
                            .minimumShouldMatch("1")));
                    return elasticsearchOperations.search(
                            NativeQuery.builder().withQuery(query).withMaxResults(limit)
                                    .withSort(Sort.by(Sort.Order.desc("_score"))).build(),
                            ProductDocument.class)
                            .getSearchHits().stream().map(h -> h.getContent()).toList();
                }
            }
        } catch (Exception e) {
            log.warn("ES 推荐失败，降级为 MySQL 同类目查询: {}", e.getMessage());
        }
        // 降级：MySQL 同类目商品（排除当前商品）
        Product product = productMapper.selectById(productId);
        if (product == null || product.getCategoryId() == null) return List.of();
        List<Product> products = productMapper.selectByCategoryIdLimit(product.getCategoryId(), limit + 1);
        return products.stream()
                .filter(p -> !p.getId().equals(productId))
                .limit(limit)
                .map(this::toDocument)
                .toList();
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
        doc.setCreateTime(p.getCreateTime() != null ? p.getCreateTime().toString() : null);
        ProductImg img = productImgMapper.selectFirstByProductId(p.getId());
        doc.setImage(img != null ? img.getUrl() : null);
        return doc;
    }

    private Map<Integer, String> batchProductImages(List<Product> products) {
        if (products == null || products.isEmpty()) return Collections.emptyMap();
        List<Integer> productIds = products.stream().map(Product::getId).distinct().toList();
        return productImgMapper.selectByProductIds(productIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        com.qiujie.entity.ProductImg::getProductId,
                        com.qiujie.entity.ProductImg::getUrl, (a, b) -> a));
    }
}
