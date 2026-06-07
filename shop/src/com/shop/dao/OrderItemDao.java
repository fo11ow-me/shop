package com.shop.dao;

import java.util.ArrayList;

import com.shop.entity.OrderItem;

public interface OrderItemDao extends BaseDao<OrderItem, Integer>{
	/**
	 * 通过订单id得到订单明细
	 * @param id
	 * @return
	 */
	public ArrayList<OrderItem> getOrderItems(int id);
}
