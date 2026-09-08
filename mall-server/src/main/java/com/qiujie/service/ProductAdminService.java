package com.qiujie.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qiujie.entity.Product;

/**
 * 商品管理服务 — 管理端 CRUD + 缓存失效。
 *
 * @author qiujie
 */
public interface ProductAdminService extends IService<Product> {

    IPage<Product> listPage(Integer current, Integer size, String name, Integer status, Integer categoryId);

    void add(Product product);

    boolean updateById(Product product);

    boolean removeById(java.io.Serializable id);

    void toggleStatus(Integer id);
}
