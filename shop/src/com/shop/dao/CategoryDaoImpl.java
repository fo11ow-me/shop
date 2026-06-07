package com.shop.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.shop.entity.Category;
import com.shop.util.ConnectDB;

public class CategoryDaoImpl implements CategoryDao {
	private Connection connection = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	@Override
	public int doAdd(Category category) {
		int count = 0;
		connection = ConnectDB.getConnection();
		String sql = "insert into shop_category values(null,?,?)";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, category.getName());
			preparedStatement.setInt(2, category.getParentId());
			count = preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return count;
	}

	@Override
	public int doDelete(Integer id) {
		int count = 0;
		connection = ConnectDB.getConnection();
		String sql = "delete from shop_category where category_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, id);
			count = preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return count;
	}

	@Override
	public int doUpdate(Category category) {
		int count = 0;
		connection = ConnectDB.getConnection();
		String sql = "update shop_category set category_name = ?,category_parentId = ? where category_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, category.getName());
			preparedStatement.setInt(2, category.getParentId());
			preparedStatement.setInt(3, category.getId());
			count = preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return count;
	}

	@Override
	public ArrayList<Category> getAll() {
		ArrayList<Category> list = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_category";
		try {
			preparedStatement = connection.prepareStatement(sql);
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				if (list==null) {
					list = new ArrayList<Category>();
				}
				Category category = new Category();
				category.setId(resultSet.getInt(1));
				category.setName(resultSet.getString(2));
				category.setParentId(resultSet.getInt(3));
				list.add(category);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return list;
	}

	@Override
	public Category getById(Integer id) {
		Category category = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_category where category_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, id);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				category = new Category();
				category.setId(resultSet.getInt(1));
				category.setName(resultSet.getString(2));
				category.setParentId(resultSet.getInt(3));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return category;
	}

	@Override
	public Category getByName(String name) {
		Category category = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_category where category_name = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, name);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				category = new Category();
				category.setId(resultSet.getInt(1));
				category.setName(resultSet.getString(2));
				category.setParentId(resultSet.getInt(3));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return category;
	}

}
