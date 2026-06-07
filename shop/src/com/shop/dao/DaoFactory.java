package com.shop.dao;

/**
 * 工厂类，得到Dao层实例
 * @author asus
 */
public class DaoFactory {
	public static UserDao getUserDao() {
		return new UserDaoImpl();
	}
	public static ProductDao getProductDao() {
		return new ProductDaoImpl();
	}
	public static CategoryDao getCategoryDao() {
		return new CategoryDaoImpl();
	}
	public static ImageDao getImageDao() {
		return new ImageDaoImpl();
	}
	public static CartDao getCartDao() {
		return new CartDaoImpl();
	}
	public static OrderDao getOrderDao() {
		return new OrderDaoImpl();
	}
	public static OrderItemDao getOrderItemDao() {
		return new OrderItemDaoImpl();
	}
}
