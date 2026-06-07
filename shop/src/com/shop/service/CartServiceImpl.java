package com.shop.service;

import java.util.ArrayList;

import com.shop.dao.DaoFactory;
import com.shop.entity.Cart;

public class CartServiceImpl implements CartService{

	@Override
	public int doAdd(Cart cart) {
		if (cart.getAmount() > cart.getProduct().getStock()) {
			cart.setAmount(cart.getProduct().getStock());
		}
		return DaoFactory.getCartDao().doAdd(cart);
	}

	@Override
	public int doDelete(int id) {
		return DaoFactory.getCartDao().doDelete(id);
	}

	@Override
	public int doUpdate(Cart cart) {
		if (cart.getAmount() > cart.getProduct().getStock()) {
			cart.setAmount(cart.getProduct().getStock());
		}
		return DaoFactory.getCartDao().doUpdate(cart);
	}

	@Override
	public ArrayList<Cart> getAll() {
		return DaoFactory.getCartDao().getAll();
	}

	@Override
	public Cart getById(int id) {
		return DaoFactory.getCartDao().getById(id);
	}

	@Override
	public ArrayList<Cart> getCarts(int id) {
		return DaoFactory.getCartDao().getCarts(id);
	}

	@Override
	public Cart getCart(int id, int pid) {
		Cart cart = null;
		ArrayList<Cart> list = DaoFactory.getCartDao().getCarts(id);
		if (list!=null) {
			for (Cart cart1 : list) {
				if (cart1.getProduct().getId() == pid) {
					return cart1;
				}
			}
		}
		return cart;
	}

	@Override
	public ArrayList<Cart> getCartsByCartIds(String[] cartIds){
		ArrayList<Cart> list = null;
		if (cartIds.length>0) {
			for (int i = 0; i < cartIds.length; i++) {
				if (list == null) {
					list = new ArrayList<Cart>();
				}
				Cart cart = DaoFactory.getCartDao().getById(Integer.parseInt(cartIds[i]));
				if (cart!=null) {
					list.add(cart);
				}
			}
		}
		return list;
	}
}
