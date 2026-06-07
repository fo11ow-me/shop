package com.shop.servlet.user;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.shop.entity.User;
import com.shop.service.ServiceFactory;

/**
 * Servlet implementation class GetAllUsers
 */
@WebServlet("/admin/AdminGetUser")
public class AdminGetUser extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AdminGetUser() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int pageSize = 6;//页面所显示的用户数
		int pageIndex = 1;//当前页面索引,默认为1，首页
		int pageTotalNumber = 1;//页面总数
		String page = request.getParameter("page");//接受客户端传来的page参数
		String keyword = request.getParameter("keyword");//关键字
		int userTotalNumber = ServiceFactory.getUserService().getTotalNumber(keyword);//用户总数
		if (userTotalNumber > 0) {//判断是否有用户
			if (userTotalNumber%pageSize!=0) {//计算总页数
				pageTotalNumber = userTotalNumber/pageSize + 1;
			}else {
				pageTotalNumber = userTotalNumber/pageSize;
			}
			if (page != null) {
				pageIndex = Integer.parseInt(page);
			}
		}
		//查询得到对应的用户
		ArrayList<User> userList = ServiceFactory.getUserService().getSome(pageSize, pageIndex,keyword);
		request.setAttribute("pageIndex", pageIndex);
		request.setAttribute("pageTotalNumber", pageTotalNumber);
		request.setAttribute("userTotalNumber", userTotalNumber);
		request.setAttribute("userList", userList);
		request.getRequestDispatcher("admin_user.jsp").forward(request, response);//跳转到用户列表页面
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

	}

}
