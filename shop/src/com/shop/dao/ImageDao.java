package com.shop.dao;

import java.util.ArrayList;

import com.shop.entity.Image;

public interface ImageDao extends BaseDao<Image, Integer>{
	/**
	 * 通过商品id得到图片
	 * @param id
	 * @return
	 */
	public ArrayList<Image> getImages(int id);
	
}
