package com.shop.service;

import java.util.ArrayList;

import com.shop.entity.User;

public interface UserService {
	/**
	 * @return 返回一个包含数据库中所有用户的顺序表
	 */
	public ArrayList<User> getAll();
	
	/**
	 * 得到部分用户
	 * @param pageSize 每页显示的用户数
	 * @param pageIndex 表示当前第几页
	 * @return 
	 */
	public ArrayList<User> getSome(int pageSize, int pageIndex, String keyword);

	/**
	 * @param id 用户id
	 * @return 通过用户id查询用户，返回一个user对象
	 */
	public User getById(int id);

	/**
	 * 通过用户名查询用户
	 * @param name 用户名
	 * @return 返回一个user对象
	 */
	public User getByName(String name);
	
	/**
	 * 得到用户总数
	 * @param keyword 关键词
	 * @return 
	 */
	public int getTotalNumber(String keyword);

	/**
	 * 添加用户
	 * @param user 用户对象
	 * @return 
	 */
	public int doAdd(User user);
	
	/**
	 * 更新用户信息
	 * @param user 用户对象
	 * @return 
	 */
	public int doUpdate(User user);
	
	/**
	 * 删除用户
	 * @param user 用户对象
	 * @return 
	 */
	public int doDelete(int id);
	
}
