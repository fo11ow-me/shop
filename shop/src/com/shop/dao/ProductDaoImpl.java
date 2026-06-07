package com.shop.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.shop.entity.Product;
import com.shop.util.ConnectDB;

public class ProductDaoImpl implements ProductDao {
	private Connection connection = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	@Override
	public int doAdd(Product product) {
		int count = 0;
		connection = ConnectDB.getConnection();
		String sql = "insert into shop_product values(null,?,?,?,?,?)";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, product.getName());
			preparedStatement.setDouble(2, product.getPrice());
			preparedStatement.setInt(3, product.getStock());
			preparedStatement.setInt(4, product.getCategory().getId());
			preparedStatement.setString(5, product.getDetail());
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
		String sql = "delete from shop_product where product_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, id);
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
	public int doUpdate(Product product) {
		int count = 0;
		connection = ConnectDB.getConnection();
		String sql = "update shop_product set product_name = ?,product_price = ?,product_stock = ?,"
				+ "category_id = ?,product_detail = ? where product_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, product.getName());
			preparedStatement.setDouble(2, product.getPrice());
			preparedStatement.setInt(3, product.getStock());
			preparedStatement.setInt(4, product.getCategory().getId());
			preparedStatement.setString(5, product.getDetail());
			preparedStatement.setInt(6, product.getId());
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
	public ArrayList<Product> getAll() {
		ArrayList<Product> list = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_product order by product_id desc";
		try {
			preparedStatement = connection.prepareStatement(sql);
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				if (list == null) {
					list = new ArrayList<Product>();
				}
				Product product = new Product();
				product.setId(resultSet.getInt(1));
				product.setName(resultSet.getString(2));
				product.setPrice(resultSet.getDouble(3));
				product.setStock(resultSet.getInt(4));
				product.setCategory(DaoFactory.getCategoryDao().getById(resultSet.getInt(5)));
				product.setDetail(resultSet.getString(6));
				list.add(product);
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
	public Product getById(Integer id) {
		Product product = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_product where product_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, id);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				product = new Product();
				product.setId(resultSet.getInt(1));
				product.setName(resultSet.getString(2));
				product.setPrice(resultSet.getDouble(3));
				product.setStock(resultSet.getInt(4));
				product.setCategory(DaoFactory.getCategoryDao().getById(resultSet.getInt(5)));
				product.setDetail(resultSet.getString(6));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return product;
	}

	@Override
	public Product getByName(String name) {
		Product product = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_product where product_name = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, name);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				product = new Product();
				product.setId(resultSet.getInt(1));
				product.setName(resultSet.getString(2));
				product.setPrice(resultSet.getDouble(3));
				product.setStock(resultSet.getInt(4));
				product.setCategory(DaoFactory.getCategoryDao().getById(resultSet.getInt(5)));
				product.setDetail(resultSet.getString(6));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			ConnectDB.closeAll(resultSet, preparedStatement, connection);
		}
		return product;
	}

	@Override
	public ArrayList<Product> getSome(int limit, int offset, String keyword) {
		ArrayList<Product> list = null;
		connection = ConnectDB.getConnection();
		try {
			if (keyword != null) {
				String sql = "select * from shop_product where product_name like ? order by product_id asc limit ? offset ?";
				preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setString(1, "%" + keyword +"%");
				preparedStatement.setInt(2, limit);
				preparedStatement.setInt(3, offset);
			}else {
				String sql = "select * from shop_product order by product_id asc limit ? offset ?";
				preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setInt(1, limit);
				preparedStatement.setInt(2, offset);
			}
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				if (list == null) {
					list = new ArrayList<Product>();
				}
				Product product = new Product();
				product.setId(resultSet.getInt(1));
				product.setName(resultSet.getString(2));
				product.setPrice(resultSet.getDouble(3));
				product.setStock(resultSet.getInt(4));
				product.setCategory(DaoFactory.getCategoryDao().getById(resultSet.getInt(5)));
				product.setDetail(resultSet.getString(6));
				list.add(product);
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
	public int getTotalNumber(String keyword) {
		int count = 0;
		connection = ConnectDB.getConnection();
		try {
			if (keyword != null) {
				String sql = "select count(*) from shop_product where product_name like ?";
				preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setString(1, "%"+keyword+"%");
			}else {
				String sql = "select count(*) from shop_product";
				preparedStatement = connection.prepareStatement(sql);
			}
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
	public int getLastInsertId() {
		int count = 0;
		connection = ConnectDB.getConnection();
		String sql = "select max(product_id) from shop_product";
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
	public ArrayList<Product> getProducts(int id) {
		ArrayList<Product> list = null;
		connection = ConnectDB.getConnection();
		String sql = "select * from shop_product where category_id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, id);
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				if (list == null) {
					list = new ArrayList<Product>();
				}
				Product product = new Product();
				product.setId(resultSet.getInt(1));
				product.setName(resultSet.getString(2));
				product.setPrice(resultSet.getDouble(3));
				product.setStock(resultSet.getInt(4));
				product.setCategory(DaoFactory.getCategoryDao().getById(resultSet.getInt(5)));
				product.setDetail(resultSet.getString(6));
				list.add(product);
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
	public ArrayList<Product> getSome(String keyword) {
		ArrayList<Product> list = null;
		connection = ConnectDB.getConnection();
		try {
			if (keyword != null) {
				String sql = "select * from shop_product where product_name like ? order by product_id asc";
				preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setString(1, "%" + keyword +"%");
			}else {
				String sql = "select * from shop_product order by product_id asc";
				preparedStatement = connection.prepareStatement(sql); 
			}
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				if (list == null) {
					list = new ArrayList<Product>();
				}
				Product product = new Product();
				product.setId(resultSet.getInt(1));
				product.setName(resultSet.getString(2));
				product.setPrice(resultSet.getDouble(3));
				product.setStock(resultSet.getInt(4));
				product.setCategory(DaoFactory.getCategoryDao().getById(resultSet.getInt(5)));
				product.setDetail(resultSet.getString(6));
				list.add(product);
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
