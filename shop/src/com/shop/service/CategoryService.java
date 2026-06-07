package com.shop.service;

import java.util.ArrayList;

import com.shop.entity.Category;

public interface CategoryService {
	/**
	 * 添加一个商品分类
	 * @param category 商品分类
	 * @return
	 */
	public int doAdd(Category category);
	/**
	 * 删除一个商品分类
	 * @param id
	 * @param parentId
	 * @return
	 */
	public int doDelete(int id,int parentId);
	/**
	 * 更新一个商品分类
	 * @param category
	 * @return
	 */
	public int doUpdate(Category category);
	/**
	 * 得到所有分类
	 * @return
	 */
	public ArrayList<Category> getAll();
	/**
	 * 通过分类的id得到分类
	 * @param id
	 * @return
	 */
	public Category getById(int id);
	
	/**
	 * 通过父分类得到子分类
	 * @param id
	 * @return
	 */
	public ArrayList<Category> getCategories(int id);
}
