package com.shop.service;

import java.util.ArrayList;

import com.shop.entity.Cart;

public interface CartService {
	/**
	 * 购物车添加
	 * @param cart
	 * @return
	 */
	public int doAdd(Cart cart);
	/**
	 * 购物车删除
	 * @param id
	 * @return
	 */
	public int doDelete(int id);
	/**
	 * 更新购物车
	 * @param cart
	 * @return
	 */
	public int doUpdate(Cart cart);
	/**
	 * 得到所有购物车
	 * @return
	 */
	public ArrayList<Cart> getAll();
	/**
	 * 通过id得到购物车
	 * @param id
	 * @return
	 */
	public Cart getById(int id);
	/**
	 * 通过用户id,得到购物车
	 * @param id
	 * @return
	 */
	public ArrayList<Cart> getCarts(int id);
	
	/**
	 * 通过用户id和商品id,查询购物车
	 * @param id 用户id
	 * @param pid 商品id
	 * @return
	 */
	public Cart getCart(int id,int pid);
	
	/**
	 * 通过购物车id,得到购物车
	 * @param cartIds
	 * @return
	 */
	public ArrayList<Cart> getCartsByCartIds(String[] cartIds);
}
