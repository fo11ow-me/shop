package com.shop.servlet.user;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.shop.entity.User;
import com.shop.service.ServiceFactory;

/**
 * Servlet implementation class AdminUserUpdate
 */
@WebServlet("/admin/AdminUserUpdate")
public class AdminUserUpdate extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AdminUserUpdate() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 获取service层的对象
		int id = Integer.parseInt(request.getParameter("id"));// 获取用户id
		int pageIndex = Integer.parseInt(request.getParameter("page"));// 获取page参数
		String keyword = request.getParameter("keyword");// 获取关键字
		// 通过id获取用户
		User user = ServiceFactory.getUserService().getById(id);
		// 向用户信息修改页面发送参数
		request.setAttribute("pageIndex", pageIndex);
		request.setAttribute("user", user);
		request.setAttribute("keyword", keyword);
		request.getRequestDispatcher("admin_user_update.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//设置字符集编码
		request.setCharacterEncoding("UTF-8"); 
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		//获取请求参数
		Integer id = Integer.parseInt(request.getParameter("id"));
		String name = request.getParameter("name");
		String password = request.getParameter("password");
		String phone = request.getParameter("phone");
		String email = request.getParameter("email");
		String address = request.getParameter("address");
		Integer status =Integer.parseInt(request.getParameter("status"));
		int pageIndex = Integer.parseInt(request.getParameter("page"));
		String keyword = request.getParameter("keyword");
		//得到用户实体
		User user = ServiceFactory.getUserService().getById(id);
		User user1 = ServiceFactory.getUserService().getByName(name);//判断新的用户名是否已被使用
		if (user1 == null || user.getName().equals(name)) { //如果新的用户名未被使用，或将要更新的用户名与现在的用户名一致，则执行更新
			user.setName(name);
			user.setPassword(password);
			user.setPhone(phone);
			user.setEmail(email);
			user.setAddress(address);
			user.setStatus(status);
			//判断用户数据是否更新成功
			if (ServiceFactory.getUserService().doUpdate(user) > 0) {//修改用户成功，跳转到用户列表页面，否则提示用户信息修改失败!
				response.sendRedirect(request.getContextPath()+"/admin/AdminGetUser?keyword="+keyword+"&page="+pageIndex);
			}else {
				out.write("<script>");
				out.write("alert('用户信息修改失败!');");
				out.write("location.href='"+request.getContextPath()+"/admin/AdminUserUpdate?keyword="+keyword+"&page="+pageIndex+"&id="+id+"'");
				out.write("</script>");
			}
		}else {
			out.write("<script>");
			out.write("alert('用户名已被使用!');");
			out.write("location.href='"+request.getContextPath()+"/admin/AdminUserUpdate?keyword="+keyword+"&page="+pageIndex+"&id="+id+"'");
			out.write("</script>");
		}
	}

}
