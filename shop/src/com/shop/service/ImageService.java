package com.shop.service;

import java.util.ArrayList;
import com.shop.entity.Cart;
import com.shop.entity.Image;
import com.shop.entity.Product;

public interface ImageService {
	/**
	 * 添加图片
	 * @param image
	 * @return
	 */
	public int doAdd(Image image);
	/**
	 * 删除图片
	 * @param id
	 * @return
	 */
	public int doDelete(int id);
	/**
	 * 修改图片信息
	 * @param image
	 * @return
	 */
	public int doUpdate(Image image);
	/**
	 * 查询所有图片
	 * @return
	 */
	public ArrayList<Image> getAll();
	/**
	 * 通过id查找图片
	 * @return
	 */
	public Image getById(int id);
	
	/**
	 * 通过商品id得到图片
	 * @param id
	 * @return
	 */
	public ArrayList<Image> getImages(int id);
	
	/**
	 * 得到一组商品的第一个图片
	 * @param list
	 * @return
	 */
	public ArrayList<Image> getFirstImage(ArrayList<Product> list);
	
	/**
	 * 通过商品id删除商品所有图片
	 * @param id
	 * @return
	 */
	public int doDeleteImages(int id);
	
	/**
	 * 得到购物车中每个商品的第一个图片
	 * @param list
	 * @return
	 */
	public ArrayList<Image> getImagesByCart(ArrayList<Cart> list);
	
	/**
	 * 通过订单id得到订单明细的图片
	 * @param id 
	 * @return
	 */
	public ArrayList<Image> getImagesByOrder(int id);

	/**
	 *得到所有商品的第一张图片
	 * @return
	 */
	public ArrayList<Image> getAllFirstImage();
	
}
