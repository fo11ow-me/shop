package com.shop.servlet.user;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.shop.service.ServiceFactory;

/**
 * Servlet implementation class AdminUserDelete
 */
@WebServlet("/admin/AdminUserDelete")
public class AdminUserDelete extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AdminUserDelete() {
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
		PrintWriter out = response.getWriter();
		int id = Integer.parseInt(request.getParameter("id"));// 获取用户id
		int pageIndex = Integer.parseInt(request.getParameter("page"));// 获取page参数
		String keyword = request.getParameter("keyword");// 获取关键字
		if (ServiceFactory.getUserService().doDelete(id) <= 0) {
			out.write("<script>");
			out.write("alert('用户删除失败!');");
			out.write("</script>");
		}
		response.sendRedirect(
				request.getContextPath() + "/admin/AdminGetUser?keyword=" + keyword + "&page=" + pageIndex);
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
		PrintWriter out = response.getWriter();
		String items[] = request.getParameterValues("items");
		for (int i = 0; i < items.length; i++) {
			if (ServiceFactory.getUserService().doDelete(Integer.parseInt(items[i])) <= 0) {
				out.write("<script>");
				out.write("alert('用户删除失败!');");
				out.write("</script>");
				break;// 如果某个用户删除失败，则跳出循环
			}
		}
		response.sendRedirect(request.getContextPath() + "/admin/AdminGetUser");
	}

}
