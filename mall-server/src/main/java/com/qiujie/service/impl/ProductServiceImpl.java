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
import com.qiujie.repository.ProductSearchRepository;
import com.qiujie.service.EsSyncService;
import com.qiujie.service.ProductService;
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
    private final EsSyncService esSyncService;
    private final SeckillSessionMapper seckillSessionMapper;

    @Autowired(required = false)
    private ProductSearchRepository productSearchRepository;

    public ProductServiceImpl(ProductMapper productMapper, CategoryMapper categoryMapper,
                              ProductImgMapper productImgMapper, CacheClient cacheClient,
                              StringRedisTemplate stringRedisTemplate, RedisUtil redisUtil,
                              EsSyncService esSyncService,
                              SeckillSessionMapper seckillSessionMapper) {
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
        this.productImgMapper = productImgMapper;
        this.cacheClient = cacheClient;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisUtil = redisUtil;
        this.esSyncService = esSyncService;
        this.seckillSessionMapper = seckillSessionMapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> home() {
        String jsonStr = stringRedisTemplate.opsForValue().get(CACHE_HOME_KEY);
        if (StrUtil.isNotBlank(jsonStr)) {
            return (List<Map<String, Object>>) (List<?>) JSONUtil.toList(jsonStr, Map.class);
        }
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
        if (!result.isEmpty()) {
            cacheClient.set(CACHE_HOME_KEY, result, CACHE_HOME_TTL, TimeUnit.SECONDS);
        }
        return result;
    }

    @Override
    public List<Category> categories() {
        String jsonStr = stringRedisTemplate.opsForValue().get(CACHE_CATEGORY_TREE_KEY);
        if (StrUtil.isNotBlank(jsonStr)) {
            return JSONUtil.toList(jsonStr, Category.class);
        }
        List<Category> parentCategories = categoryMapper.selectByParentId(0);
        for (Category parent : parentCategories) {
            List<Category> children = categoryMapper.selectByParentId(parent.getId());
            parent.setChildren(children);
        }
        if (!parentCategories.isEmpty()) {
            cacheClient.set(CACHE_CATEGORY_TREE_KEY, parentCategories, CACHE_CATEGORY_TTL, TimeUnit.SECONDS);
        }
        return parentCategories;
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
        if (productSearchRepository == null) {
            throw new RuntimeException("ES not available");
        }
        int pageNum = current != null ? current - 1 : 0;
        int pageSize = size != null ? size : 10;
        var pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Order.desc("_score")));

        var esPage = productSearchRepository.findByStatusAndNameOrDetail(
                ProductStatusEnum.ON_SHELF.getCode(), keyword.trim(), keyword.trim(), pageable);

        Map<String, Object> data = new HashMap<>();
        data.put("records", esPage.getContent());
        data.put("total", esPage.getTotalElements());
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
    public ProductVO detail(Integer id) {
        return cacheClient.handleCachePenetration(
                CACHE_PRODUCT_KEY, id, ProductVO.class,
                productId -> {
                    ProductVO product = productMapper.selectDetailById(productId);
                    if (product == null) {
                        throw new ServiceException(BusinessStatusEnum.PRODUCT_NOT_EXIST);
                    }
                    return product;
                },
                CACHE_PRODUCT_TTL, TimeUnit.SECONDS
        );
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
        esSyncService.syncToEs(product);
    }

    @Override
    public boolean updateById(Product product) {
        boolean result = super.updateById(product);
        redisUtil.del(CACHE_HOME_KEY);
        redisUtil.del(CACHE_PRODUCT_KEY + product.getId());
        esSyncService.syncToEs(product);
        return result;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        boolean result = super.removeById(id);
        redisUtil.del(CACHE_HOME_KEY);
        redisUtil.del(CACHE_PRODUCT_KEY + id);
        esSyncService.deleteFromEs((Integer) id);
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
            if (product.getStatus() == ProductStatusEnum.ON_SHELF) {
                esSyncService.syncToEs(product);
            } else {
                esSyncService.deleteFromEs(id);
            }
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
