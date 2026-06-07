package com.shop.dao;

import com.shop.entity.Category;

public interface CategoryDao extends BaseDao<Category, Integer>{
	/**
	 * 通过分类名称查询分类，返回一个category对象
	 * @param name 分类名称
	 * @return
	 */
	public Category getByName(String name);
}
