package com.shop.service;

import java.util.ArrayList;

import com.shop.dao.DaoFactory;
import com.shop.entity.Cart;
import com.shop.entity.Order;
import com.shop.entity.OrderItem;

public class OrderItemServiceImpl implements OrderItemService{

	@Override
	public int doAdd(OrderItem orderItem) {
		return DaoFactory.getOrderItemDao().doAdd(orderItem);
	}

	@Override
	public int doDelete(int id) {
		return DaoFactory.getOrderItemDao().doDelete(id);
	}

	@Override
	public int doUpdate(OrderItem orderItem) {
		return DaoFactory.getOrderItemDao().doUpdate(orderItem);
	}

	@Override
	public ArrayList<OrderItem> getAll() {
		return DaoFactory.getOrderItemDao().getAll();
	}

	@Override
	public OrderItem getById(int id) {
		return DaoFactory.getOrderItemDao().getById(id);
	}

	@Override
	public int addOrderItemsBycartIds(String[] cartIds) {
		int count = 0;
		ArrayList<Cart> list1 = ServiceFactory.getCartService().getCartsByCartIds(cartIds);
		Order order = ServiceFactory.getOrderService().getLastInsertOrder();
		if (list1!=null ) {
			for (Cart cart : list1) {
				OrderItem orderItem = new OrderItem();
				orderItem.setOrder(order);
				orderItem.setProduct(cart.getProduct());
				orderItem.setAmount(cart.getAmount());
				DaoFactory.getOrderItemDao().doAdd(orderItem);
				cart.getProduct().setStock(cart.getProduct().getStock()-cart.getAmount());//减少商品库存
				DaoFactory.getProductDao().doUpdate(cart.getProduct());//更新商品信息
				count = DaoFactory.getCartDao().doDelete(cart.getId());
			}
		}
		return count;
	}
	@Override
	public ArrayList<OrderItem> getOrderItems(int id) {
		return DaoFactory.getOrderItemDao().getOrderItems(id);
	}

	@Override
	public int doDeleteByOrder(int id) {
		int count = 0;
		ArrayList<OrderItem> list = DaoFactory.getOrderItemDao().getAll();
		if (list!=null) {
			for (OrderItem orderItem : list) {
				if (orderItem.getOrder().getId()==id) {
					count = DaoFactory.getOrderItemDao().doDelete(orderItem.getId());
				}
			}
		}
		return count;
	}
	
	
}
