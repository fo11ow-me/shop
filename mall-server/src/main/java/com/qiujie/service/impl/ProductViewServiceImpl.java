package com.qiujie.service.impl;

import com.qiujie.entity.Category;
import com.qiujie.entity.Product;
import com.qiujie.entity.ProductImg;
import com.qiujie.entity.SeckillSession;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.mapper.CategoryMapper;
import com.qiujie.mapper.ProductImgMapper;
import com.qiujie.mapper.ProductMapper;
import com.qiujie.mapper.SeckillSessionMapper;
import com.qiujie.service.ProductViewService;
import com.qiujie.util.BloomFilterService;
import com.qiujie.util.CacheClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.qiujie.constants.RedisConstants.*;

/**
 * 商品浏览服务实现 — 首页、分类、详情，仅依赖缓存 + DB。
 *
 * @author qiujie
 */
@Service
public class ProductViewServiceImpl implements ProductViewService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final ProductImgMapper productImgMapper;
    private final CacheClient cacheClient;
    private final SeckillSessionMapper seckillSessionMapper;

    @Autowired(required = false)
    private BloomFilterService bloomFilterService;

    public ProductViewServiceImpl(ProductMapper productMapper, CategoryMapper categoryMapper,
                                   ProductImgMapper productImgMapper, CacheClient cacheClient,
                                   SeckillSessionMapper seckillSessionMapper) {
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
        this.productImgMapper = productImgMapper;
        this.cacheClient = cacheClient;
        this.seckillSessionMapper = seckillSessionMapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> home() {
        return cacheClient.queryWithLogicalExpire(
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
                    }).collect(Collectors.toList());
                    return result.isEmpty() ? null : result;
                },
                CACHE_HOME_TTL, TimeUnit.SECONDS);
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
                    Map<Integer, List<Category>> childrenMap = new HashMap<>();
                    List<Category> parents = new ArrayList<>();
                    for (Category c : all) {
                        if (c.getParentId() == 0) {
                            parents.add(c);
                        } else {
                            childrenMap.computeIfAbsent(c.getParentId(),
                                    k -> new ArrayList<>()).add(c);
                        }
                    }
                    for (Category parent : parents) {
                        parent.setChildren(childrenMap.getOrDefault(parent.getId(), List.of()));
                    }
                    return parents.isEmpty() ? null : parents;
                },
                CACHE_CATEGORY_TTL, TimeUnit.SECONDS);
        return cached != null ? cached : List.of();
    }

    @Override
    public Map<String, Object> getByCategory(Integer categoryId, Integer current, Integer size) {
        var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Product>(
                current != null ? current : 1, size != null ? size : 10);
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
    public Product detail(Integer id) {
        if (bloomFilterService != null && !bloomFilterService.mightContain(id)) {
            throw new ServiceException(BusinessStatusEnum.PRODUCT_NOT_EXIST);
        }
        return cacheClient.queryWithLogicalExpire(
                CACHE_PRODUCT_KEY, id, Product.class,
                productId -> {
                    Product p = productMapper.selectDetailById(productId);
                    if (p == null) {
                        throw new ServiceException(BusinessStatusEnum.PRODUCT_NOT_EXIST);
                    }
                    return p;
                },
                CACHE_PRODUCT_TTL, TimeUnit.SECONDS);
    }

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

    private Map<Integer, String> batchProductImages(List<Product> products) {
        if (products == null || products.isEmpty()) return Collections.emptyMap();
        List<Integer> productIds = products.stream().map(Product::getId).distinct().toList();
        List<ProductImg> imgs = productImgMapper.selectByProductIds(productIds);
        return imgs.stream().collect(Collectors.toMap(
                ProductImg::getProductId, ProductImg::getUrl, (a, b) -> a));
    }
}
