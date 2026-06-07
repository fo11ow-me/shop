package com.shop.servlet.category;

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
 * Servlet implementation class GetCategory
 */
@WebServlet("/GetCategory")
public class GetCategory extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetCategory() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ArrayList<Category> list = ServiceFactory.getCategoryService().getAll();//得到所有分类
		ArrayList<Product> list1 = ServiceFactory.getProductService().getAll();
		ArrayList<Image> list2 = ServiceFactory.getImageService().getFirstImage(list1);
		request.setAttribute("list", list);
		request.setAttribute("list1", list1);
		request.setAttribute("list2", list2);
		request.getRequestDispatcher("index.jsp").forward(request, response);
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
