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
@WebServlet("/admin/AdminCategoryUpdate")
public class AdminCategoryUpdate extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AdminCategoryUpdate() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//设置字符集编码
		request.setCharacterEncoding("UTF-8"); 
		response.setContentType("text/html;charset=UTF-8");
		int id = Integer.parseInt(request.getParameter("id"));
		Category category = ServiceFactory.getCategoryService().getById(id);
		ArrayList<Category> list = ServiceFactory.getCategoryService().getAll();
		request.setAttribute("category", category);
		request.setAttribute("list", list);
		request.getRequestDispatcher("admin_category_update.jsp").forward(request, response);
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//设置字符集编码
		request.setCharacterEncoding("UTF-8"); 
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		int id = Integer.parseInt(request.getParameter("id"));
		//获取请求参数
		String name = request.getParameter("name");
		int parentId = Integer.parseInt(request.getParameter("parentId"));
		Category category = ServiceFactory.getCategoryService().getById(id);
		category.setName(name);
		category.setParentId(parentId);
		int count = ServiceFactory.getCategoryService().doUpdate(category);
		if (count > 0) {
			response.sendRedirect(request.getContextPath()+"/admin/AdminGetCategory");
		}else {
			out.write("<script>");
			out.write("alert('分类更新失败!');");
			out.write("location.href='"+request.getContextPath()+"/admin/AdminCategoryUpdate?id="+id+"'");
			out.write("</script>");
		}
	}

}
