package com.shop.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.shop.entity.OrderItem;
import com.shop.util.ConnectDB;

public class OrderItemDaoImpl implements OrderItemDao{
	private Connection connection = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	
	@Override
	public int doAdd(OrderItem orderItem) {
		int count = 0;
		connection = ConnectDB.getConnection();
		String sql = "insert into shop_orderitem values(null,?,?,?)";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, orderItem.getOrder().getId());
			preparedStatement.setInt(2, orderItem.getProduct().getId());
			preparedStatement.setInt(3, orderItem.getAmount());
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
		String sql = "delete from shop_orderitem where orderItem_id = ?";
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
	public int doUpdate(OrderItem orderItem) {
		int count = 0;
		connection = ConnectDB.getConnection();
		String sql = "update shop_orderitem set order_id = ?, product_id = ?, product_amount = ? where orderItem_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, orderItem.getOrder().getId());
			preparedStatement.setInt(2, orderItem.getProduct().getId());
			preparedStatement.setInt(3, orderItem.getAmount());
			preparedStatement.setInt(4, orderItem.getId());
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
	public ArrayList<OrderItem> getAll() {
		ArrayList<OrderItem> list = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_orderitem";
		try {
			preparedStatement = connection.prepareStatement(sql);
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				if (list==null) {
					list = new ArrayList<OrderItem>();
				}
				OrderItem orderItem = new OrderItem();
				orderItem.setId(resultSet.getInt(1));
				orderItem.setOrder(DaoFactory.getOrderDao().getById(resultSet.getInt(2)));
				orderItem.setProduct(DaoFactory.getProductDao().getById(resultSet.getInt(3)));
				orderItem.setAmount(resultSet.getInt(4));
				list.add(orderItem);
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
	public OrderItem getById(Integer id) {
		OrderItem orderItem = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_orderitem where orderItem_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, id);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				orderItem = new OrderItem();
				orderItem.setId(resultSet.getInt(1));
				orderItem.setOrder(DaoFactory.getOrderDao().getById(resultSet.getInt(2)));
				orderItem.setProduct(DaoFactory.getProductDao().getById(resultSet.getInt(3)));
				orderItem.setAmount(resultSet.getInt(4));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return orderItem;
	}

	@Override
	public ArrayList<OrderItem> getOrderItems(int id) {
		ArrayList<OrderItem> list = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_orderitem where order_id = ?"; 
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, id);
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				if (list==null) {
					list = new ArrayList<OrderItem>();
				}
				OrderItem orderItem = new OrderItem();
				orderItem.setId(resultSet.getInt(1));
				orderItem.setOrder(DaoFactory.getOrderDao().getById(resultSet.getInt(2)));
				orderItem.setProduct(DaoFactory.getProductDao().getById(resultSet.getInt(3)));
				orderItem.setAmount(resultSet.getInt(4));
				list.add(orderItem);
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
