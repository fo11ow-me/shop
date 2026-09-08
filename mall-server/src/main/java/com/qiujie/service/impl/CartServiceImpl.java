package com.qiujie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiujie.entity.Cart;
import com.qiujie.entity.Product;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.mapper.CartMapper;
import com.qiujie.mapper.ProductMapper;
import com.qiujie.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    public CartServiceImpl(CartMapper cartMapper, ProductMapper productMapper) {
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
    }

    public List<Cart> list(Integer userId) {
        return cartMapper.selectByUserId(userId);
    }

    @Transactional
    public void add(Integer userId, Cart cart) {
        Product product = productMapper.selectById(cart.getProductId());
        if (product == null || product.getDeleted() == 1) {
            throw new ServiceException(BusinessStatusEnum.PRODUCT_NOT_EXIST);
        }
        Cart existCart = cartMapper.selectByUserIdAndProductId(userId, cart.getProductId());
        if (existCart != null) {
            existCart.setAmount(existCart.getAmount() + (cart.getAmount() != null ? cart.getAmount() : 1));
            updateById(existCart);
            return;
        }
        cart.setUserId(userId);
        cart.setIsSelected(1);
        if (cart.getAmount() == null || cart.getAmount() < 1) {
            cart.setAmount(1);
        }
        try {
            save(cart);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            Cart dup = cartMapper.selectByUserIdAndProductId(userId, cart.getProductId());
            if (dup != null) {
                dup.setAmount(dup.getAmount() + (cart.getAmount() != null ? cart.getAmount() : 1));
                updateById(dup);
                return;
            }
            throw e;
        }
    }

    public void update(Integer userId, Cart cart) {
        Cart dbCart = cartMapper.selectById(cart.getId());
        if (dbCart == null || !dbCart.getUserId().equals(userId)) {
            throw new ServiceException(BusinessStatusEnum.CART_NOT_EXIST);
        }
        if (cart.getAmount() != null && cart.getAmount() > 0) {
            dbCart.setAmount(cart.getAmount());
        }
        if (cart.getIsSelected() != null) {
            dbCart.setIsSelected(cart.getIsSelected());
        }
        updateById(dbCart);
    }

    public void delete(Integer userId, Integer id) {
        Cart dbCart = cartMapper.selectById(id);
        if (dbCart == null || !dbCart.getUserId().equals(userId)) {
            throw new ServiceException(BusinessStatusEnum.CART_NOT_EXIST);
        }
        removeById(id);
    }

    @Transactional
    public void batchDelete(Integer userId, Map<String, List<Integer>> params) {
        List<Integer> ids = params.get("ids");
        if (ids == null || ids.isEmpty()) {
            throw new ServiceException(BusinessStatusEnum.CART_SELECT_EMPTY);
        }
        for (Integer id : ids) {
            Cart dbCart = cartMapper.selectById(id);
            if (dbCart != null && dbCart.getUserId().equals(userId)) {
                removeById(id);
            }
        }
    }
}
