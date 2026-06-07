package com.shop.service;

import java.util.ArrayList;

import com.shop.entity.Product;

public interface ProductService {
	/**
	 * 添加一个商品
	 * @param product 商品
	 * @return
	 */
	public int doAdd(Product product);
	/**
	 * 删除一个商品
	 * @param id
	 * @return
	 */
	public int doDelete(int id);
	/**
	 * 更新一个商品
	 * @param product
	 * @return
	 */
	public int doUpdate(Product product);
	/**
	 * 得到所有商品
	 * @return
	 */
	public ArrayList<Product> getAll();
	/**
	 * 通过商品的id得到商品
	 * @param id
	 * @return
	 */
	public Product getById(int id);
	
	/**
	 * 得到部分商品
	 * @param pageSize 每页显示的商品数
	 * @param pageIndex 表示当前第几页
	 * @return 
	 */
	public ArrayList<Product> getSome(int pageSize, int pageIndex, String keyword);
	
	/**
	 * 得到商品总数
	 * @param keyword 关键词
	 * @return 
	 */
	public int getTotalNumber(String keyword);
	
	/**
	 * 获取刚插入的自增id的值
	 * @return
	 */
	public int getLastInsertId();
	
	/**
	 * 获取刚添加的商品
	 * @return
	 */
	public Product getLastInsertProduct();
	
	/**
	 * 通过名称获取商品
	 * @param name
	 * @return
	 */
	public Product getByName(String name);
	
	/**
	 * 通过分类id得到某一分类下的所有商品
	 * @param id
	 * @return
	 */
	public ArrayList<Product> getProducts(int id);
	
	/**
	 * 得到某一父级分类下所有商品
	 * @param id
	 * @return
	 */
	public ArrayList<Product> getProductsByParentId(int id);
	
	/**
	 * 通过购物车id得到商品
	 * @param String[] cartIds
	 * @return 
	 */
	public ArrayList<Product> getProductsByCartId(String[] cartIds);
	
	/**
	 * 模糊查询时的关键字，当keyword不为空时，进行模糊查询，得到商品
	 * @param keyword
	 * @return
	 */
	public ArrayList<Product> getSome(String keyword);
}
