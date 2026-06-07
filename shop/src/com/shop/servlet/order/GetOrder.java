package com.shop.servlet.order;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.shop.entity.Category;
import com.shop.entity.Image;
import com.shop.entity.Order;
import com.shop.entity.OrderItem;
import com.shop.entity.User;
import com.shop.service.ServiceFactory;

/**
 * Servlet implementation class GetOrder
 */
@WebServlet("/GetOrder")
public class GetOrder extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetOrder() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 设置字符集编码
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		// 获取打印输出流
		PrintWriter out = response.getWriter();
		HttpSession session = request.getSession();
		if (session.getAttribute("user")!=null) {
			ArrayList<Category> list = ServiceFactory.getCategoryService().getAll();
			User user = (User) session.getAttribute("user");
			ArrayList<Order> list1  = ServiceFactory.getOrderService().getOrders(user.getId());
			ArrayList<OrderItem> list2 = ServiceFactory.getOrderItemService().getAll();
			ArrayList<Image> list3 = ServiceFactory.getImageService().getAllFirstImage();
			request.setAttribute("list", list);
			request.setAttribute("list1", list1);
			request.setAttribute("list2", list2);
			request.setAttribute("list3", list3);
			request.getRequestDispatcher("order.jsp").forward(request, response);
		}else{
			out.write("<script>");
			out.write("alert('你还未登录!');");
			out.write("location.href='" + request.getContextPath() + "/login.jsp'");
			out.write("</script>");
		}
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
