package com.shop.dao;

import java.util.ArrayList;

import com.shop.entity.Order;

public interface OrderDao extends BaseDao<Order, Integer>{
	/**
	 * 获取刚插入的自增id的值
	 * @return
	 */
	public int getLastInsertId();
	
	/**
	 * 得到用户的所有订单
	 * @param id 用户id
	 * @return
	 */
	public ArrayList<Order> getOrders(int id);
}
