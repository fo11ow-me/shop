package com.shop.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.shop.entity.Image;
import com.shop.util.ConnectDB;

public class ImageDaoImpl implements ImageDao{
	private Connection connection = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	
	@Override
	public int doAdd(Image image) {
		int count = 0;
		connection = ConnectDB.getConnection();
		String sql = "insert into shop_image values(null,?,?)";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, image.getUrl());
			preparedStatement.setInt(2, image.getProduct().getId());
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
		String sql = "delete from shop_image where image_id = ?";
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
	public int doUpdate(Image image) {
		int count = 0;
		connection = ConnectDB.getConnection();
		String sql = "update shop_image set image_url = ?,product_id = ? where image_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, image.getUrl());
			preparedStatement.setInt(2, image.getProduct().getId());
			preparedStatement.setInt(3, image.getId());
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
	public ArrayList<Image> getAll() {
		ArrayList<Image> list = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_image";
		try {
			preparedStatement = connection.prepareStatement(sql);
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				if (list == null) {
					list = new ArrayList<Image>();
				}
				Image image = new Image();
				image.setId(resultSet.getInt(1));
				image.setUrl(resultSet.getString(2));
				image.setProduct(DaoFactory.getProductDao().getById(resultSet.getInt(3)));
				list.add(image);
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
	public Image getById(Integer id) {
		Image image = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_image where image_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, id);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				image = new Image();
				image.setId(resultSet.getInt(1));
				image.setUrl(resultSet.getString(2));
				image.setProduct(DaoFactory.getProductDao().getById(resultSet.getInt(3)));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return image;
	}
	@Override
	public ArrayList<Image> getImages(int id) {
		ArrayList<Image> list = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_image where product_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, id);
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				if (list == null) {
					list = new ArrayList<Image>();
				}
				Image image = new Image();
				image.setId(resultSet.getInt(1));
				image.setUrl(resultSet.getString(2));
				image.setProduct(DaoFactory.getProductDao().getById(resultSet.getInt(3)));
				list.add(image);
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
