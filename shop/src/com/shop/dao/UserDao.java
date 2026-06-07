package com.shop.dao;

import java.util.ArrayList;

import com.shop.entity.User;

public interface UserDao extends BaseDao<User, Integer> {

	/**
	 * 得到部分用户
	 * @param limit   每次最多可以查询的记录数
	 * @param offset  从offset处开始查询记录
	 * @param keyword 模糊查询时的关键字，当keyword不为空时，进行模糊查询
	 * @return
	 */
	public ArrayList<User> getSome(int limit, int offset, String keyword);

	/**
	 * 通过用户名查询用户，返回一个user对象
	 * 
	 * @param name 用户名
	 * @return
	 */
	public User getByName(String name);

	/**
	 * 得到用户总数
	 * @param keyword 模糊查询时的关键字，当keyword不为空时，进行模糊查询
	 * @return
	 */
	public int getTotalNumber(String keyword);
}
