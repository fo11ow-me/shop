package com.qiujie.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiujie.entity.Category;
import com.qiujie.entity.Product;
import com.qiujie.entity.ProductImg;
import com.qiujie.entity.SeckillSession;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.enums.ProductStatusEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.mapper.CategoryMapper;
import com.qiujie.mapper.ProductImgMapper;
import com.qiujie.mapper.ProductMapper;
import com.qiujie.mapper.SeckillSessionMapper;
import com.qiujie.document.ProductDocument;
import com.qiujie.repository.ProductSearchRepository;
import com.qiujie.service.ProductService;
import com.qiujie.util.BloomFilterService;
import com.qiujie.util.CacheClient;
import com.qiujie.util.RedisUtil;
import com.qiujie.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.qiujie.constants.RedisConstants.*;

/**
 * 商品服务实现
 *
 * @author qiujie
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final ProductImgMapper productImgMapper;
    private final CacheClient cacheClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisUtil redisUtil;
    private final SeckillSessionMapper seckillSessionMapper;

    @Autowired(required = false)
    private BloomFilterService bloomFilterService;

    @Autowired(required = false)
    private ProductSearchRepository productSearchRepository;

    @Autowired(required = false)
    private ElasticsearchOperations elasticsearchOperations;

    public ProductServiceImpl(ProductMapper productMapper, CategoryMapper categoryMapper,
                              ProductImgMapper productImgMapper, CacheClient cacheClient,
                              StringRedisTemplate stringRedisTemplate, RedisUtil redisUtil,
                              SeckillSessionMapper seckillSessionMapper) {
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
        this.productImgMapper = productImgMapper;
        this.cacheClient = cacheClient;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisUtil = redisUtil;
        this.seckillSessionMapper = seckillSessionMapper;
    }

    @Override
    public List<Map<String, Object>> home() {
        List<Map<String, Object>> cached = cacheClient.queryWithLogicalExpire(
                CACHE_HOME_KEY, List.class,
                () -> {
                    List<Category> parentCategories = categoryMapper.selectByParentId(0);
                    List<Map<String, Object>> result = parentCategories.stream().map(cat -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("category", cat);
                        List<Product> products = productMapper.selectByCategoryIdLimit(cat.getId(), 8);
                        item.put("products", products);
                        item.put("productImages", batchProductImages(products));
                        item.put("seckillMap", batchSeckillInfo(products));
                        return item;
                    }).collect(java.util.stream.Collectors.toList());
                    return result.isEmpty() ? null : result;
                },
                CACHE_HOME_TTL, TimeUnit.SECONDS);
        return cached;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Category> categories() {
        List<Category> cached = cacheClient.queryWithLogicalExpire(
                CACHE_CATEGORY_TREE_KEY, List.class,
                () -> {
                    List<Category> all = categoryMapper.selectList(
                            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Category>()
                                    .eq("is_deleted", 0));
                    java.util.Map<Integer, List<Category>> childrenMap = new java.util.HashMap<>();
                    List<Category> parents = new java.util.ArrayList<>();
                    for (Category c : all) {
                        if (c.getParentId() == 0) {
                            parents.add(c);
                        } else {
                            childrenMap.computeIfAbsent(c.getParentId(),
                                    k -> new java.util.ArrayList<>()).add(c);
                        }
                    }
                    for (Category parent : parents) {
                        parent.setChildren(childrenMap.getOrDefault(parent.getId(), java.util.List.of()));
                    }
                    return parents.isEmpty() ? null : parents;
                },
                CACHE_CATEGORY_TTL, TimeUnit.SECONDS);
        return cached != null ? cached : java.util.List.of();
    }

    @Override
    public Map<String, Object> getByCategory(Integer categoryId, Integer current, Integer size) {
        Page<Product> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        var result = productMapper.selectPageByCategoryId(page, categoryId);
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        data.put("productImages", batchProductImages(result.getRecords()));
        return data;
    }

    @Override
    public Map<String, Object> search(String keyword, Integer current, Integer size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        try {
            return searchFromEs(keyword, current, size);
        } catch (Exception e) {
            log.warn("ES 搜索失败，降级为 MySQL LIKE 查询: {}", e.getMessage());
            return searchFromMysql(keyword, current, size);
        }
    }

    private Map<String, Object> searchFromEs(String keyword, Integer current, Integer size) {
        if (elasticsearchOperations == null) {
            throw new RuntimeException("ES not available");
        }
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
        if (elasticsearchOperations == null) return List.of();
        try {
            ProductDocument source = elasticsearchOperations.get(
                    String.valueOf(productId), ProductDocument.class);
            if (source == null) return List.of();
            int limit = size != null ? size : 8;

            // 同分类推荐 + 排除自己
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

            var nativeQuery = NativeQuery.builder()
                    .withQuery(query)
                    .withMaxResults(limit)
                    .withSort(org.springframework.data.domain.Sort.by(
                            org.springframework.data.domain.Sort.Order.desc("_score")))
                    .build();
            return elasticsearchOperations.search(nativeQuery, ProductDocument.class)
                    .getSearchHits().stream().map(h -> h.getContent()).toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public ProductVO detail(Integer id) {
        if (bloomFilterService != null && !bloomFilterService.mightContain(id)) {
            throw new ServiceException(BusinessStatusEnum.PRODUCT_NOT_EXIST);
        }
        ProductVO product = cacheClient.queryWithLogicalExpire(
                CACHE_PRODUCT_KEY, id, ProductVO.class,
                productId -> {
                    ProductVO p = productMapper.selectDetailById(productId);
                    if (p == null) {
                        throw new ServiceException(BusinessStatusEnum.PRODUCT_NOT_EXIST);
                    }
                    return p;
                },
                CACHE_PRODUCT_TTL, TimeUnit.SECONDS);
        return product;
    }

    @Override
    public IPage<Product> listPage(Integer current, Integer size, String name, Integer status, Integer categoryId) {
        Page<Product> page = new Page<>(current, size);
        IPage<Product> result = productMapper.selectPageByName(page, name, status, categoryId);
        if (!result.getRecords().isEmpty()) {
            List<Integer> productIds = result.getRecords().stream().map(Product::getId).toList();
            List<ProductImg> allImgs = productImgMapper.selectByProductIds(productIds);
            Map<Integer, List<ProductImg>> imgMap = allImgs.stream()
                    .collect(Collectors.groupingBy(ProductImg::getProductId));
            for (Product p : result.getRecords()) {
                p.setImages(imgMap.getOrDefault(p.getId(), List.of()));
            }
        }
        return result;
    }

    @Override
    public void add(Product product) {
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException(BusinessStatusEnum.PARAM_ERROR);
        }
        if (product.getStock() == null || product.getStock() < 0) {
            throw new ServiceException(BusinessStatusEnum.PARAM_ERROR);
        }
        save(product);
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            for (ProductImg img : product.getImages()) {
                img.setProductId(product.getId());
            }
            productImgMapper.insertBatch(product.getImages());
        }
        redisUtil.del(CACHE_HOME_KEY);
        if (bloomFilterService != null) bloomFilterService.add(product.getId());
    }

    @Override
    public boolean updateById(Product product) {
        boolean result = super.updateById(product);
        redisUtil.del(CACHE_HOME_KEY);
        redisUtil.del(CACHE_PRODUCT_KEY + product.getId());
        return result;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        boolean result = super.removeById(id);
        redisUtil.del(CACHE_HOME_KEY);
        redisUtil.del(CACHE_PRODUCT_KEY + id);
        return result;
    }

    @Override
    public void toggleStatus(Integer id) {
        Product product = getById(id);
        if (product != null) {
            product.setStatus(product.getStatus() == ProductStatusEnum.ON_SHELF
                    ? ProductStatusEnum.OFF_SHELF : ProductStatusEnum.ON_SHELF);
            super.updateById(product);
            redisUtil.del(CACHE_HOME_KEY);
            redisUtil.del(CACHE_PRODUCT_KEY + id);
            // ES 索引由 IncSyncProductToEs 定时任务自动同步
        }
    }

    /**
     * 批量查询商品秒杀信息（进行中场次）
     */
    private Map<Integer, Map<String, Object>> batchSeckillInfo(List<Product> products) {
        if (products == null || products.isEmpty()) return Collections.emptyMap();
        List<Integer> productIds = products.stream().map(Product::getId).distinct().toList();
        List<SeckillSession> sessions = seckillSessionMapper.selectActiveByProductIds(productIds, LocalDateTime.now());
        Map<Integer, Map<String, Object>> result = new HashMap<>();
        for (SeckillSession s : sessions) {
            Map<String, Object> info = new HashMap<>();
            info.put("sessionId", s.getId());
            info.put("seckillPrice", s.getSeckillPrice());
            info.put("seckillStock", s.getSeckillStock());
            info.put("endTime", s.getEndTime().toString());
            result.put(s.getProductId(), info);
        }
        return result;
    }

    /**
     * 批量查询商品首图，避免 N+1 查询
     */
    private Map<Integer, String> batchProductImages(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Integer> productIds = products.stream().map(Product::getId).distinct().toList();
        List<ProductImg> imgs = productImgMapper.selectByProductIds(productIds);
        return imgs.stream().collect(Collectors.toMap(
                ProductImg::getProductId, ProductImg::getUrl, (a, b) -> a));
    }


}
