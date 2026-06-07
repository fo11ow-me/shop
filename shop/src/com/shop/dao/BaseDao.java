package com.shop.dao;

import java.util.ArrayList;

/**
 * 
 * @author asus 所有对象的顶层接口
 * @param <T> 数据对象类型
 * @param <K> 数据对象主键类型
 */
public interface BaseDao<T, K> {
	/**
	 * 添加一条记录，执行insert操作
	 * 
	 * @param vo
	 * @return 数据表受影响的行数
	 */
	public int doAdd(T vo);

	/**
	 * 删除一条记录，执行delete操作
	 * 
	 * @param id
	 * @return 数据表受影响的行数
	 */
	public int doDelete(K id);

	/**
	 * 修改一条记录，执行update操作
	 * 
	 * @param vo
	 * @return 数据表受影响的行数
	 */
	public int doUpdate(T vo);

	/**
	 * 得到数据表中所有实体
	 * 
	 * @return 所有实体
	 */
	public ArrayList<T> getAll();

	/**
	 * 通过主键查找实体
	 * 
	 * @param id
	 * @return 一个实体
	 */
	public T getById(K id);
	
}
