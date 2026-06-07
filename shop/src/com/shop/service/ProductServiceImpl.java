package com.shop.service;

import java.util.ArrayList;

import com.shop.dao.DaoFactory;
import com.shop.entity.Cart;
import com.shop.entity.Category;
import com.shop.entity.Product;

public class ProductServiceImpl implements ProductService{

	@Override
	public int doAdd(Product product) {
		return DaoFactory.getProductDao().doAdd(product);
	}

	@Override
	public int doDelete(int id) {
		return DaoFactory.getProductDao().doDelete(id);
	}

	@Override
	public int doUpdate(Product product) {
		return DaoFactory.getProductDao().doUpdate(product);
	}

	@Override
	public ArrayList<Product> getAll() {
		return DaoFactory.getProductDao().getAll();
	}

	@Override
	public Product getById(int id) {
		return DaoFactory.getProductDao().getById(id);
	}

	@Override
	public ArrayList<Product> getSome(int pageSize, int pageIndex, String keyword) {
		int offset = pageSize * (pageIndex - 1);
		return DaoFactory.getProductDao().getSome(pageSize, offset, keyword);
	}

	@Override
	public int getTotalNumber(String keyword) {
		return DaoFactory.getProductDao().getTotalNumber(keyword);
	}

	@Override
	public int getLastInsertId() {
		return DaoFactory.getProductDao().getLastInsertId();
	}

	@Override
	public Product getLastInsertProduct() {
		int id = DaoFactory.getProductDao().getLastInsertId();
		return DaoFactory.getProductDao().getById(id);
	}

	@Override
	public Product getByName(String name) {
		return DaoFactory.getProductDao().getByName(name);
	}

	@Override
	public ArrayList<Product> getProducts(int id) {
		return DaoFactory.getProductDao().getProducts(id);
	}

	@Override
	public ArrayList<Product> getProductsByParentId(int id) {
		ArrayList<Product> list1 = null;
		ArrayList<Category> list = ServiceFactory.getCategoryService().getCategories(id);
		if (list != null) {
			for (Category category : list) {
				ArrayList<Product> list2 = DaoFactory.getProductDao().getProducts(category.getId());
				if (list2 != null) {
					for (Product product : list2) {
						if (list1 == null) {
							list1 = new ArrayList<Product>();
						}
						list1.add(product);
					}
				}
			}
		}
		return list1;
	}

	@Override
	public ArrayList<Product> getProductsByCartId(String[] cartIds) {
		ArrayList<Product> list = null;
		if (cartIds.length>0) {
			for (int i = 0; i < cartIds.length; i++) {
				if (list == null) {
					list = new ArrayList<Product>();
				}
				Cart cart = DaoFactory.getCartDao().getById(Integer.parseInt(cartIds[i]));
				if (cart!=null) {
					Product product = DaoFactory.getProductDao().getById(cart.getProduct().getId());
					if (product!=null) {
						list.add(product);
					}
				}
			}
		}
		return list;
	}

	@Override
	public ArrayList<Product> getSome(String keyword) {
		return DaoFactory.getProductDao().getSome(keyword);
	}
}
