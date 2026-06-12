package com.qiujie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiujie.entity.Category;
import com.qiujie.document.ProductDocument;
import com.qiujie.entity.Product;
import com.qiujie.vo.ProductVO;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;
import java.util.Map;

public interface ProductService extends IService<Product> {

    List<Map<String, Object>> home();

    List<Category> categories();

    Map<String, Object> getByCategory(Integer categoryId, Integer current, Integer size);

    Map<String, Object> search(String keyword, Integer current, Integer size);

    /**
     * 基于当前商品的内容推荐（"看了又看"），用 ES function_score 实现
     */
    List<ProductDocument> recommend(Integer productId, Integer size);

    ProductVO detail(Integer id);

    IPage<Product> listPage(Integer current, Integer size, String name, Integer status, Integer categoryId);

    void add(Product product);

    void toggleStatus(Integer id);
}
