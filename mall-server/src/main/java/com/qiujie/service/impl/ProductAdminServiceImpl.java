package com.qiujie.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiujie.entity.Product;
import com.qiujie.entity.ProductImg;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.enums.ProductStatusEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.mapper.ProductImgMapper;
import com.qiujie.mapper.ProductMapper;
import com.qiujie.service.ProductAdminService;
import com.qiujie.util.BloomFilterService;
import com.qiujie.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.qiujie.constants.RedisConstants.*;

/**
 * 商品管理服务 — 管理端 CRUD + 缓存失效。
 *
 * @author qiujie
 */
@Service
public class ProductAdminServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductAdminService {

    private final ProductMapper productMapper;
    private final ProductImgMapper productImgMapper;
    private final RedisUtil redisUtil;

    @Autowired(required = false)
    private BloomFilterService bloomFilterService;

    public ProductAdminServiceImpl(ProductMapper productMapper, ProductImgMapper productImgMapper,
                                    RedisUtil redisUtil) {
        this.productMapper = productMapper;
        this.productImgMapper = productImgMapper;
        this.redisUtil = redisUtil;
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
        }
    }
}
