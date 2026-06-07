package com.shop.service;

import java.util.ArrayList;

import com.shop.dao.DaoFactory;
import com.shop.entity.Category;

public class CategoryServiceImpl implements CategoryService{

	@Override
	public int doAdd(Category category) {
		return DaoFactory.getCategoryDao().doAdd(category);
	}

	@Override
	public int doDelete(int id, int parentId) {
		if (parentId == 0) {//判断是否是父级分类
			ArrayList<Category> list = DaoFactory.getCategoryDao().getAll();
			for (Category category:list) {
				if (category.getParentId() == id) {//删除父级分类的子分类
					DaoFactory.getCategoryDao().doDelete(category.getId());
				}
			}
		}
		return DaoFactory.getCategoryDao().doDelete(id);
	}

	@Override
	public int doUpdate(Category category) {
		return DaoFactory.getCategoryDao().doUpdate(category);
	}

	@Override
	public ArrayList<Category> getAll() {
		return DaoFactory.getCategoryDao().getAll();
	}

	@Override
	public Category getById(int id) {
		return DaoFactory.getCategoryDao().getById(id);
	}
	
	@Override
	public ArrayList<Category> getCategories(int id){
		ArrayList<Category> list = null;
		ArrayList<Category> list1 = DaoFactory.getCategoryDao().getAll();
		if (list1 != null) {
			for (Category category : list1) {
				if (list == null) {
					list = new ArrayList<Category>();
				}
				if (category.getParentId()==id) {
					list.add(category);
				}
			}
		}
		return list;
	}

}
