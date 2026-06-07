package com.shop.service;

import java.util.ArrayList;

import com.shop.entity.Order;

public interface OrderService {
	/**
	 * 添加订单
	 * @param order
	 * @return
	 */
	public int doAdd(Order order);
	/**
	 * 删除订单
	 * @param id
	 * @return
	 */
	public int doDelete(int id);
	/**
	 * 订单更新
	 * @param order
	 * @return
	 */
	public int doUpdate(Order order);
	/**
	 * 得到所有订单
	 * @return
	 */
	public ArrayList<Order> getAll();
	/**
	 * 通过id得到订单
	 * @param id
	 * @return
	 */
	public Order getById(int id);
	
	/**
	 * 得到订单总价
	 * @param cartIds
	 * @return
	 */
	public double getTotalPrice(String[] cartIds);
	
	/**
	 * 获取刚插入的自增id的值
	 * @return
	 */
	public int getLastInsertId();
	
	/**
	 * 获取刚插入的订单
	 * @return
	 */
	public Order getLastInsertOrder();
	
	/**
	 * 得到用户的所有订单
	 * @param id 用户id
	 * @return
	 */
	public ArrayList<Order> getOrders(int id);
	
	/**
	 * 得到用户已支付的订单
	 * @param id
	 * @return
	 */
	public ArrayList<Order> getOrdersPay(int id);
	
	
	/**
	 * 得到用户未支付的订单
	 * @param id
	 * @return
	 */
	public ArrayList<Order> getOrdersUnpay(int id);
	
}

