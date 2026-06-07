package com.shop.dao;

import java.sql.*;
import java.util.ArrayList;

import com.shop.entity.User;
import com.shop.util.ConnectDB;

public class UserDaoImpl implements UserDao {
	private Connection connection = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	@Override
	public ArrayList<User> getSome(int limit, int offset, String keyword) {
		ArrayList<User> list = null;
		connection = ConnectDB.getConnection();
		try {
			if (keyword != null) {
				String sql = "select * from shop_user where user_name like ? order by user_id asc limit ? offset ?";
				preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setString(1, "%" + keyword + "%");
				preparedStatement.setInt(2, limit);
				preparedStatement.setInt(3, offset);
			} else {
				String sql = "select * from shop_user order by user_id asc limit ? offset ?";
				preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setInt(1, limit);
				preparedStatement.setInt(2, offset);
			}
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				if (list == null) {
					list = new ArrayList<User>();
				}
				User user = new User();
				user.setId(resultSet.getInt(1));
				user.setName(resultSet.getString(2));
				user.setPassword(resultSet.getString(3));
				user.setPhone(resultSet.getString(4));
				user.setEmail(resultSet.getString(5));
				user.setAddress(resultSet.getString(6));
				user.setStatus(resultSet.getInt(7));
				list.add(user);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return list;
	}

	@Override
	public User getByName(String name) {
		User user = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_user where user_name = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, name);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				user = new User();
				user.setId(resultSet.getInt(1));
				user.setName(resultSet.getString(2));
				user.setPassword(resultSet.getString(3));
				user.setPhone(resultSet.getString(4));
				user.setEmail(resultSet.getString(5));
				user.setAddress(resultSet.getString(6));
				user.setStatus(resultSet.getInt(7));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		// TODO Auto-generated method stub
		return user;
	}

	@Override
	public int getTotalNumber(String keyword) {
		int count = 0;
		connection = ConnectDB.getConnection();
		try {
			if (keyword != null) {
				String sql = "select count(*) from shop_user where user_name like ?";
				preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setString(1, "%" + keyword + "%");
			} else {
				String sql = "select count(*) from shop_user";
				preparedStatement = connection.prepareStatement(sql);
			}
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				count = resultSet.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return count;
	}

	@Override
	public int doAdd(User user) {
		int count = 0;
		connection = ConnectDB.getConnection();
		String sql = "insert into shop_user values(null,?,?,?,?,?,?)";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, user.getName());
			preparedStatement.setString(2, user.getPassword());
			preparedStatement.setString(3, user.getPhone());
			preparedStatement.setString(4, user.getEmail());
			preparedStatement.setString(5, user.getAddress());
			preparedStatement.setInt(6, user.getStatus());
			count = preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return count;
	}

	@Override
	public int doDelete(Integer id) {
		int count = 0;
		connection = ConnectDB.getConnection();
		String sql = "delete from shop_user where user_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, id);// 通过user_id删除用户
			count = preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return count;
	}

	@Override
	public int doUpdate(User user) {
		int count = 0;
		connection = ConnectDB.getConnection();
		String sql = "update shop_user set user_name = ?,user_password = ?,user_phone = ?,"
				+ "user_email = ?,user_address = ?,user_status = ? where user_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, user.getName());
			preparedStatement.setString(2, user.getPassword());
			preparedStatement.setString(3, user.getPhone());
			preparedStatement.setString(4, user.getEmail());
			preparedStatement.setString(5, user.getAddress());
			preparedStatement.setInt(6, user.getStatus());
			preparedStatement.setInt(7, user.getId());
			count = preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return count;
	}

	@Override
	public ArrayList<User> getAll() {
		ArrayList<User> list = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_user";
		try {
			preparedStatement = connection.prepareStatement(sql);
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				if (list == null) {
					list = new ArrayList<User>();
				}
				User user = new User();
				user.setId(resultSet.getInt(1));
				user.setName(resultSet.getString(2));
				user.setPassword(resultSet.getString(3));
				user.setPhone(resultSet.getString(4));
				user.setEmail(resultSet.getString(5));
				user.setAddress(resultSet.getString(6));
				user.setStatus(resultSet.getInt(7));
				list.add(user);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return list;
	}

	@Override
	public User getById(Integer id) {
		User user = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_user where user_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, id);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				user = new User();
				user.setId(resultSet.getInt(1));
				user.setName(resultSet.getString(2));
				user.setPassword(resultSet.getString(3));
				user.setPhone(resultSet.getString(4));
				user.setEmail(resultSet.getString(5));
				user.setAddress(resultSet.getString(6));
				user.setStatus(resultSet.getInt(7));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		// TODO Auto-generated method stub
		return user;
	}
}
