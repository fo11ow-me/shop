package com.shop.service;

import java.util.ArrayList;

import com.shop.dao.DaoFactory;
import com.shop.entity.Cart;
import com.shop.entity.Image;
import com.shop.entity.OrderItem;
import com.shop.entity.Product;

public class ImageServiceImpl implements ImageService {

	@Override
	public int doAdd(Image image) {
		return DaoFactory.getImageDao().doAdd(image);
	}

	@Override
	public int doDelete(int id) {
		return DaoFactory.getImageDao().doDelete(id);
	}

	@Override
	public int doUpdate(Image image) {
		return DaoFactory.getImageDao().doUpdate(image);

	}

	@Override
	public ArrayList<Image> getAll() {
		return DaoFactory.getImageDao().getAll();
	}

	@Override
	public Image getById(int id) {
		return DaoFactory.getImageDao().getById(id);
	}

	@Override
	public ArrayList<Image> getImages(int id) {
		return DaoFactory.getImageDao().getImages(id);
	}

	@Override
	public ArrayList<Image> getFirstImage(ArrayList<Product> list) {
		ArrayList<Image> list1 = null;
		if (list != null) {
			for (Product product:list) {
				if (list1 == null) {
					list1 = new ArrayList<Image>();
				}
				ArrayList<Image> list2 = DaoFactory.getImageDao().getImages(product.getId());//通过商品id得到商品的所有图片
				if (list2 != null) {
					list1.add(list2.get(0));//将商品的第一张图片添加到list1中
				}
			}
		}
		return list1;
	}

	@Override
	public int doDeleteImages(int id) {
		int count = 0;
		ArrayList<Image> list = DaoFactory.getImageDao().getImages(id);
		if (list != null) {
			for (Image image : list) {
				count = DaoFactory.getImageDao().doDelete(image.getId());
			}
		}
		return count;
	}

	@Override
	public ArrayList<Image> getImagesByCart(ArrayList<Cart> list) {
		ArrayList<Image> list1 = null;
		if (list != null) {
			for (Cart cart:list) {
				if (list1 == null) {
					list1 = new ArrayList<Image>();
				}
				ArrayList<Image> list2 = DaoFactory.getImageDao().getImages(cart.getProduct().getId());//通过商品id得到商品的所有图片
				if (list2 != null) {
					list1.add(list2.get(0));//将商品的第一张图片添加到list1中
				}
			}
		}
		return list1;
	}

	@Override
	public ArrayList<Image> getImagesByOrder(int id) {
		ArrayList<Image> list = null;
		ArrayList<OrderItem> list1 = DaoFactory.getOrderItemDao().getOrderItems(id);
		if (list1!=null) {
			for (OrderItem orderItem : list1) {
				if (list == null) {
					list = new ArrayList<Image>();
				}
				ArrayList<Image> list2 = DaoFactory.getImageDao().getImages(orderItem.getProduct().getId());
				if (list2 != null) {
					list.add(list2.get(0));//将商品的第一张图片添加到list中
				}
			}
		}
		return list;
	}

	@Override
	public ArrayList<Image> getAllFirstImage() {
		ArrayList<Image> list = null;
		ArrayList<Product> list1 = DaoFactory.getProductDao().getAll();
		if (list1!=null) {
			list = ServiceFactory.getImageService().getFirstImage(list1);
		}
		return list;
	}
}
