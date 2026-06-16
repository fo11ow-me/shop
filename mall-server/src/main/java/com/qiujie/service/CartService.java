package com.qiujie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiujie.entity.Cart;

import java.util.List;
import java.util.Map;

public interface CartService extends IService<Cart> {

    List<Cart> list(Integer userId);

    void add(Integer userId, Cart cart);

    void update(Integer userId, Cart cart);

    void delete(Integer userId, Integer id);

    void batchDelete(Integer userId, Map<String, List<Integer>> params);
}
