package com.shop.service;

public class ServiceFactory {
	public static UserService getUserService() {
		return new UserServiceImpl();
	}
	public static CategoryService getCategoryService() {
		return new CategoryServiceImpl();
	}
	public static ProductService getProductService() {
		return new ProductServiceImpl();
	}
	public static ImageService getImageService() {
		return new ImageServiceImpl();
	}
	public static CartService getCartService() {
		return new CartServiceImpl();
	}
	public static OrderService getOrderService() {
		return new OrderServiceImpl();
	}
	public static OrderItemService getOrderItemService() {
		return new OrderItemServiceImpl();
	}
}
