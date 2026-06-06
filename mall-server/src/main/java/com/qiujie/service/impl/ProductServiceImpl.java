package com.qiujie.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiujie.document.ProductDocument;
import com.qiujie.entity.Category;
import com.qiujie.entity.Product;
import com.qiujie.entity.ProductImg;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.enums.ProductStatusEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.mapper.CategoryMapper;
import com.qiujie.mapper.ProductImgMapper;
import com.qiujie.mapper.ProductMapper;
import com.qiujie.repository.ProductSearchRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    @Autowired(required = false)
    private ProductSearchRepository productSearchRepository;

    public ProductServiceImpl(ProductMapper productMapper, CategoryMapper categoryMapper,
                              ProductImgMapper productImgMapper, CacheClient cacheClient,
                              StringRedisTemplate stringRedisTemplate, RedisUtil redisUtil) {
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
        this.productImgMapper = productImgMapper;
        this.cacheClient = cacheClient;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisUtil = redisUtil;
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
            Map<Integer, String> productImages = new HashMap<>();
            for (Product p : products) {
                ProductImg img = productImgMapper.selectFirstByProductId(p.getId());
                if (img != null) {
                    productImages.put(p.getId(), img.getUrl());
                }
            }
            item.put("productImages", productImages);
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
        Map<Integer, String> productImages = new HashMap<>();
        for (Product p : result.getRecords()) {
            ProductImg img = productImgMapper.selectFirstByProductId(p.getId());
            if (img != null) {
                productImages.put(p.getId(), img.getUrl());
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        data.put("productImages", productImages);
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
        Map<Integer, String> productImages = new HashMap<>();
        for (Product p : result.getRecords()) {
            ProductImg img = productImgMapper.selectFirstByProductId(p.getId());
            if (img != null) {
                productImages.put(p.getId(), img.getUrl());
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        data.put("productImages", productImages);
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
        for (Product p : result.getRecords()) {
            ProductImg img = productImgMapper.selectFirstByProductId(p.getId());
            if (img != null) p.setImages(List.of(img));
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
        if (product.getImages() != null) {
            for (ProductImg img : product.getImages()) {
                img.setProductId(product.getId());
                productImgMapper.insert(img);
            }
        }
        redisUtil.del(CACHE_HOME_KEY);
        syncToEs(product);
    }

    @Override
    public boolean updateById(Product product) {
        boolean result = super.updateById(product);
        redisUtil.del(CACHE_HOME_KEY);
        redisUtil.del(CACHE_PRODUCT_KEY + product.getId());
        syncToEs(product);
        return result;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        boolean result = super.removeById(id);
        redisUtil.del(CACHE_HOME_KEY);
        redisUtil.del(CACHE_PRODUCT_KEY + id);
        deleteFromEs((Integer) id);
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
                syncToEs(product);
            } else {
                deleteFromEs(id);
            }
        }
    }

    private void syncToEs(Product product) {
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

    private void deleteFromEs(Integer id) {
        if (productSearchRepository == null) return;
        try {
            productSearchRepository.deleteById(id);
        } catch (Exception ignored) {
        }
    }
}
