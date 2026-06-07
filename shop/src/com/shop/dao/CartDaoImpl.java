package com.shop.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.shop.entity.Cart;
import com.shop.util.ConnectDB;

public class CartDaoImpl implements CartDao{
	private Connection connection = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	@Override
	public int doAdd(Cart cart) {
		int count = 0;
		connection = ConnectDB.getConnection();
		String sql = "insert into shop_cart values(null,?,?,?)";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, cart.getUser().getId());
			preparedStatement.setInt(2, cart.getProduct().getId());
			preparedStatement.setInt(3, cart.getAmount());
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
		String sql = "delete from shop_cart where cart_id = ?";
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
	public int doUpdate(Cart cart) {
		int count = 0;
		connection = ConnectDB.getConnection();
		String sql = "update shop_cart set user_id = ?, product_id = ?, product_amount = ? where cart_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, cart.getUser().getId());
			preparedStatement.setInt(2, cart.getProduct().getId());
			preparedStatement.setInt(3, cart.getAmount());
			preparedStatement.setInt(4, cart.getId());
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
	public ArrayList<Cart> getAll() {
		ArrayList<Cart> list = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_cart order by cart_id desc";
		try {
			preparedStatement = connection.prepareStatement(sql);
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				if (list==null) {
					list = new ArrayList<Cart>();
				}
				Cart cart = new Cart();
				cart.setId(resultSet.getInt(1));
				cart.setUser(DaoFactory.getUserDao().getById(resultSet.getInt(2)));
				cart.setProduct(DaoFactory.getProductDao().getById(resultSet.getInt(3)));
				cart.setAmount(resultSet.getInt(4));
				list.add(cart);
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
	public Cart getById(Integer id) {
		Cart cart = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_cart where cart_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, id);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				cart = new Cart();
				cart.setId(resultSet.getInt(1));
				cart.setUser(DaoFactory.getUserDao().getById(resultSet.getInt(2)));
				cart.setProduct(DaoFactory.getProductDao().getById(resultSet.getInt(3)));
				cart.setAmount(resultSet.getInt(4));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return cart;
	}

	@Override
	public ArrayList<Cart> getCarts(int id) {
		ArrayList<Cart> list = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_cart where user_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, id);
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				if (list==null) {
					list = new ArrayList<Cart>();
				}
				Cart cart = new Cart();
				cart.setId(resultSet.getInt(1));
				cart.setUser(DaoFactory.getUserDao().getById(resultSet.getInt(2)));
				cart.setProduct(DaoFactory.getProductDao().getById(resultSet.getInt(3)));
				cart.setAmount(resultSet.getInt(4));
				list.add(cart);
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
