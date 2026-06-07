package com.shop.service;

import java.util.ArrayList;

import com.shop.dao.DaoFactory;
import com.shop.entity.User;

public class UserServiceImpl implements UserService {

	@Override
	public ArrayList<User> getAll() {
		return DaoFactory.getUserDao().getAll();
	}

	@Override
	public ArrayList<User> getSome(int pageSize, int pageIndex, String keyword) {
		int offset = pageSize * (pageIndex - 1);
		return DaoFactory.getUserDao().getSome(pageSize, offset, keyword);
	}

	@Override
	public User getById(int id) {
		return DaoFactory.getUserDao().getById(id);
	}

	@Override
	public User getByName(String name) {
		return DaoFactory.getUserDao().getByName(name);
	}

	@Override
	public int getTotalNumber(String keyword) {
		return DaoFactory.getUserDao().getTotalNumber(keyword);
	}

	@Override
	public int doAdd(User user) {
		String name = user.getName();
		if (DaoFactory.getUserDao().getByName(name) == null) {// 判断用户名是否已被他人使用
			return DaoFactory.getUserDao().doAdd(user);
		}
		return 0;
	}

	@Override
	public int doUpdate(User user) {
		return DaoFactory.getUserDao().doUpdate(user);
	}

	@Override
	public int doDelete(int id) {
		return DaoFactory.getUserDao().doDelete(id);
	}
}
