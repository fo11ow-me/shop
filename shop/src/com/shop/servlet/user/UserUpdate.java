package com.shop.servlet.user;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.shop.dao.DaoFactory;
import com.shop.entity.Category;
import com.shop.entity.User;
import com.shop.service.ServiceFactory;

/**
 * Servlet implementation class UserUpdate
 */
@WebServlet("/UserUpdate")
public class UserUpdate extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UserUpdate() {
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
		// 获取打印输出流
		PrintWriter out = response.getWriter();
		HttpSession session = request.getSession();
		if (session.getAttribute("user") != null) {
			ArrayList<Category> list = DaoFactory.getCategoryDao().getAll();
			request.setAttribute("list", list);
			request.getRequestDispatcher("user_update.jsp").forward(request, response);
		} else {
			out.write("<script>");
			out.write("alert('你还未登录!');");
			out.write("location.href='" + request.getContextPath() + "/login.jsp'");
			out.write("</script>");

		}
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
		HttpSession session = request.getSession();
		// 获取请求参数
		User user = (User) session.getAttribute("user");
		String name = request.getParameter("name");
		String phone = request.getParameter("phone");
		String email = request.getParameter("email");
		String address = request.getParameter("address");
		User user1 = ServiceFactory.getUserService().getByName(name);
		if (user1 == null || user.getName().equals(name)) { // 如果新的用户名未被使用，或将要更新的用户名与现在的用户名一致，则执行更新
			user.setName(name);
			user.setPhone(phone);
			user.setEmail(email);
			user.setAddress(address);
			if (ServiceFactory.getUserService().doUpdate(user) > 0) {
				response.sendRedirect(request.getContextPath() + "/GetUser");
			} else {
				out.write("<script>");
				out.write("alert('用户信息修改失败!');");
				out.write("location.href='" + request.getContextPath() + "/UserUpdate'");
				out.write("</script>");
			}
		} else {
			out.write("<script>");
			out.write("alert('用户名已被使用!');");
			out.write("location.href='" + request.getContextPath() + "/UserUpdate'");
			out.write("</script>");
		}
	}

}
