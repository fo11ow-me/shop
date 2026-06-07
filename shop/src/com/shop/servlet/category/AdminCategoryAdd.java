package com.shop.servlet.category;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.shop.entity.Category;
import com.shop.service.ServiceFactory;

/**
 * Servlet implementation class AdminCategoryUpdate
 */
@WebServlet("/admin/AdminCategoryAdd")
public class AdminCategoryAdd extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AdminCategoryAdd() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ArrayList<Category> list = ServiceFactory.getCategoryService().getAll();
		request.setAttribute("list", list);
		request.getRequestDispatcher("admin_category_add.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//设置字符集编码
		request.setCharacterEncoding("UTF-8"); 
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		int parentId = Integer.parseInt(request.getParameter("parentId"));
		String name = request.getParameter("name");
		Category category = new Category();
		category.setParentId(parentId);
		category.setName(name);
		int count = ServiceFactory.getCategoryService().doAdd(category);
		if (count > 0) {
			response.sendRedirect(request.getContextPath()+"/admin/AdminGetCategory");
		}else {
			out.write("<script>");
			out.write("alert('分类添加失败!');");
			out.write("location.href='"+request.getContextPath()+"/admin/AdminCategoryAdd'");
			out.write("</script>");
		}
	}

}
