package com.shop.service;

import java.util.ArrayList;

import com.shop.dao.DaoFactory;
import com.shop.entity.Cart;
import com.shop.entity.Order;

public class OrderServiceImpl implements OrderService{

	@Override
	public int doAdd(Order order) {
		return DaoFactory.getOrderDao().doAdd(order);
	}

	@Override
	public int doDelete(int id) {
		return DaoFactory.getOrderDao().doDelete(id);
	}

	@Override
	public int doUpdate(Order order) {
		return DaoFactory.getOrderDao().doUpdate(order);
	}

	@Override
	public ArrayList<Order> getAll() {
		return DaoFactory.getOrderDao().getAll();
	}

	@Override
	public Order getById(int id) {
		return DaoFactory.getOrderDao().getById(id);
	}

	@Override
	public double getTotalPrice(String[] cartIds) {
		double count = 0;
		ArrayList<Cart> list = ServiceFactory.getCartService().getCartsByCartIds(cartIds);
		if (list != null) {
			for (Cart cart : list) {
				count += cart.getProduct().getPrice()*cart.getAmount();
			}
		}
		return count;
	}

	@Override
	public int getLastInsertId() {
		return DaoFactory.getOrderDao().getLastInsertId();
	}

	@Override
	public Order getLastInsertOrder() {
		return DaoFactory.getOrderDao().getById(DaoFactory.getOrderDao().getLastInsertId());
	}

	@Override
	public ArrayList<Order> getOrders(int id) {
		return DaoFactory.getOrderDao().getOrders(id);
	}

	@Override
	public ArrayList<Order> getOrdersPay(int id) {
		ArrayList<Order> list = null;
		ArrayList<Order> list1 = DaoFactory.getOrderDao().getOrders(id);
		if (list1!=null) {
			for (Order order : list1) {
				if (list==null) {
					list = new ArrayList<Order>();
				}
				if (order.getStatus()==1) {
					list.add(order);
				}
			}
		}
		return list;
	}

	@Override
	public ArrayList<Order> getOrdersUnpay(int id) {
		ArrayList<Order> list = null;
		ArrayList<Order> list1 = DaoFactory.getOrderDao().getOrders(id);
		if (list1!=null) {
			for (Order order : list1) {
				if (list==null) {
					list = new ArrayList<Order>();
				}
				if (order.getStatus()==0) {
					list.add(order);
				}
			}
		}
		return list;
	}
	
}
