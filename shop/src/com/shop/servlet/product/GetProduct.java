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
 * Servlet implementation class GetProduct
 */
@WebServlet("/GetProduct")
public class GetProduct extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetProduct() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 设置字符集编码
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		// 获取请求参数
		int id = Integer.parseInt(request.getParameter("id"));
		ArrayList<Category> list = ServiceFactory.getCategoryService().getAll();// 得到所有分类
		Category category = ServiceFactory.getCategoryService().getById(id);// 当前分类
		ArrayList<Product> list1 = null;//存放商品
		ArrayList<Image> list2 = null;//存放商品图片
		if (category.getParentId() == 0) {//判断是否是子分类还是父分类
			list1 = ServiceFactory.getProductService().getProductsByParentId(id);
			list2 = ServiceFactory.getImageService().getFirstImage(list1);
		} else {
			Category parentCategory = ServiceFactory.getCategoryService().getById(category.getParentId());// 当前子分类的父分类
			list1 = ServiceFactory.getProductService().getProducts(id);// 得到当前分类下的所有商品
			list2 = ServiceFactory.getImageService().getFirstImage(list1);// 得到当前分类下所有商品的第一个图片
			request.setAttribute("parentCategory", parentCategory);
		}
		request.setAttribute("list", list);
		request.setAttribute("list1", list1);
		request.setAttribute("list2", list2);
		request.setAttribute("category", category);
		request.getRequestDispatcher("product.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 设置字符集编码
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		ArrayList<Category> list = ServiceFactory.getCategoryService().getAll();// 得到所有分类
		String keyword = request.getParameter("keyword");
		ArrayList<Product> list1 = ServiceFactory.getProductService().getSome(keyword);
		//System.out.println(list1.size());
		ArrayList<Image> list2 = ServiceFactory.getImageService().getFirstImage(list1);
		request.setAttribute("list", list);
		request.setAttribute("list1", list1);
		request.setAttribute("list2", list2);
		request.setAttribute("keyword", keyword);
		request.getRequestDispatcher("index.jsp").forward(request, response);
	}

}
