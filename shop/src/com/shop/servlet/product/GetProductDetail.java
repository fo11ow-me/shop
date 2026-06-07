package com.shop.servlet.product;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.shop.entity.Category;
import com.shop.entity.Image;
import com.shop.entity.Product;
import com.shop.service.ServiceFactory;

/**
 * Servlet implementation class GetProductDetail
 */
@WebServlet("/GetProductDetail")
public class GetProductDetail extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetProductDetail() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ArrayList<Category> list = ServiceFactory.getCategoryService().getAll();//得到所有分类
		int id = Integer.parseInt(request.getParameter("id"));
		Product product = ServiceFactory.getProductService().getById(id); //得到商品
		Category category = ServiceFactory.getCategoryService().getById(product.getCategory().getId()); //得到商品的分类
		Category parentCategory = ServiceFactory.getCategoryService().getById(category.getParentId()); //得到商品的父分类
		ArrayList<Image> list1 = ServiceFactory.getImageService().getImages(id); //得到商品的所有图片
		ArrayList<Product> list2 = ServiceFactory.getProductService().getProducts(product.getCategory().getId()); //得到当前分类下的所有商品
		ArrayList<Image> list3 = ServiceFactory.getImageService().getFirstImage(list2); //得到当前分类下的所有商品的第一个图片
		request.setAttribute("list", list);
		request.setAttribute("list1", list1);
		request.setAttribute("list2", list2);
		request.setAttribute("list3", list3);
		request.setAttribute("product", product);
		request.setAttribute("category", category);
		request.setAttribute("parentCategory", parentCategory);
		request.getRequestDispatcher("product_detail.jsp").forward(request, response);
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
