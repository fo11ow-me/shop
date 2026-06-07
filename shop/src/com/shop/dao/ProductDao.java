package com.shop.dao;

import java.util.ArrayList;

import com.shop.entity.Product;

public interface ProductDao extends BaseDao<Product, Integer>{
	/**
	 * 得到部分商品
	 * @param limit   每次最多可以查询的记录数
	 * @param offset  从offset处开始查询记录
	 * @param keyword 模糊查询时的关键字，当keyword不为空时，进行模糊查询
	 * @return
	 */
	public ArrayList<Product> getSome(int limit, int offset, String keyword);
	
	/**
	 * 通过商品名称查询分类，返回一个商品对象
	 * @param name 分类名称
	 * @return
	 */
	public Product getByName(String name);
	
	/**
	 * 得到商品总数
	 * @param keyword 模糊查询时的关键字，当keyword不为空时，进行模糊查询
	 * @return
	 */
	public int getTotalNumber(String keyword);
	
	/**
	 * 获取刚插入的自增id的值
	 * @return
	 */
	public int getLastInsertId();
	
	/**
	 * 通过分类id得到某一分类下的所有商品
	 * @param id
	 * @return
	 */
	public ArrayList<Product> getProducts(int id);
	
	/**
	 * 模糊查询时的关键字，当keyword不为空时，进行模糊查询，得到商品
	 * @param keyword
	 * @return
	 */
	public ArrayList<Product> getSome(String keyword);
}
