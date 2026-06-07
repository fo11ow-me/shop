package com.shop.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import com.shop.entity.Order;
import com.shop.util.ConnectDB;


public class OrderDaoImpl implements OrderDao{
	private Connection connection = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	
	@Override
	public int doAdd(Order order) {
		int count = 0;
		connection = ConnectDB.getConnection();
		String sql = "insert into shop_order values(null,?,?,?,now(),now(),?,?,?)";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, order.getUser().getId());
			preparedStatement.setString(2, order.getNum());
			preparedStatement.setDouble(3, order.getCost());
			preparedStatement.setInt(4, order.getPayMethod());
			preparedStatement.setInt(5, order.getExpress());
			preparedStatement.setInt(6, order.getStatus());
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
		String sql = "delete from shop_order where order_id = ?";
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
	public int doUpdate(Order order) {
		int count = 0;
		connection = ConnectDB.getConnection();
		String sql = "update shop_order set user_id = ?,order_num = ?,"
				+ "total_cost = ?,update_time = now(),pay_method = ?,"
				+ "express_delivery = ?,order_status = ? where order_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, order.getUser().getId());
			preparedStatement.setString(2, order.getNum());
			preparedStatement.setDouble(3, order.getCost());
			preparedStatement.setInt(4, order.getPayMethod());
			preparedStatement.setInt(5, order.getExpress());
			preparedStatement.setInt(6, order.getStatus());
			preparedStatement.setInt(7, order.getId());
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
	public ArrayList<Order> getAll() {
		ArrayList<Order> list = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_order order by create_time desc";
		try {
			preparedStatement = connection.prepareStatement(sql);
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				if (list == null) {
					list = new ArrayList<Order>();
				}
				Order order = new Order();
				order.setId(resultSet.getInt(1));
				order.setUser(DaoFactory.getUserDao().getById(resultSet.getInt(2)));
				order.setNum(resultSet.getString(3));
				order.setCost(resultSet.getDouble(4));
				order.setCreateTime(resultSet.getTimestamp(5));
				order.setUpdateTime(resultSet.getTimestamp(6));
				order.setPayMethod(resultSet.getInt(7));
				order.setExpress(resultSet.getInt(8));
				order.setStatus(resultSet.getInt(9));
				list.add(order);
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
	public Order getById(Integer id) {
		Order order = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_order where order_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, id);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				order = new Order();
				order.setId(resultSet.getInt(1));
				order.setUser(DaoFactory.getUserDao().getById(resultSet.getInt(2)));
				order.setNum(resultSet.getString(3));
				order.setCost(resultSet.getDouble(4));
				order.setCreateTime(resultSet.getTimestamp(5));
				order.setUpdateTime(resultSet.getTimestamp(6));
				order.setPayMethod(resultSet.getInt(7));
				order.setExpress(resultSet.getInt(8));
				order.setStatus(resultSet.getInt(9));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return order;
	}

	@Override
	public int getLastInsertId() {
		int count = 0;
		connection = ConnectDB.getConnection();
		String sql = "select max(order_id) from shop_order";
		try {
			preparedStatement = connection.prepareStatement(sql);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				count = resultSet.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return count;
	}

	@Override
	public ArrayList<Order> getOrders(int id) {
		ArrayList<Order> list = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_order where user_id = ? order by create_time desc";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, id);
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				if (list == null) {
					list = new ArrayList<Order>();
				}
				Order order = new Order();
				order.setId(resultSet.getInt(1));
				order.setUser(DaoFactory.getUserDao().getById(resultSet.getInt(2)));
				order.setNum(resultSet.getString(3));
				order.setCost(resultSet.getDouble(4));
				order.setCreateTime(resultSet.getTimestamp(5));
				order.setUpdateTime(resultSet.getTimestamp(6));
				order.setPayMethod(resultSet.getInt(7));
				order.setExpress(resultSet.getInt(8));
				order.setStatus(resultSet.getInt(9));
				list.add(order);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return list;
	}
}
