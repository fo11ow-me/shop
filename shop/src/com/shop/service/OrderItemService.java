package com.shop.service;

import java.util.ArrayList;

import com.shop.entity.OrderItem;

public interface OrderItemService {
	/**
	 * 添加一条订单明细
	 * @param orderItem
	 * @return
	 */
	public int doAdd(OrderItem orderItem);
	/**
	 * 删除一条订单明细
	 * @param id
	 * @return
	 */
	public int doDelete(int id);
	/**
	 * 更改一条订单明细
	 * @param orderItem
	 * @return
	 */
	public int doUpdate(OrderItem orderItem);
	/**
	 * 得到所有订单明细
	 * @return
	 */
	public ArrayList<OrderItem> getAll();
	/**
	 * 通过id获得订单明细
	 * @param id
	 * @return
	 */
	public OrderItem getById(int id);
	
	/**
	 * 通过购物车添加订单明细到数据库
	 * @param String[] cartIds
	 * @return
	 */
	public int addOrderItemsBycartIds(String[] cartIds);
	
	/**
	 * 通过订单id得到订单明细
	 * @param id
	 * @return
	 */
	public ArrayList<OrderItem> getOrderItems(int id);
	
	
	/**
	 * 删除订单的订单明细
	 * @param id
	 * @return
	 */
	public int doDeleteByOrder(int id);
}
