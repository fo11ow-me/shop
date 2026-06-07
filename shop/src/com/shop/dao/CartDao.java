package com.shop.dao;

import java.util.ArrayList;

import com.shop.entity.Cart;

public interface CartDao extends BaseDao<Cart, Integer>{
	/**
	 * 通过用户id,得到购物车
	 * @param id
	 * @return
	 */
	public ArrayList<Cart> getCarts(int id);
}
