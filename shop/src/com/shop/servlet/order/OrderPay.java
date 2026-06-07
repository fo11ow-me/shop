package com.shop.servlet.order;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.UUID;

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
 * Servlet implementation class OrderPay
 */
@WebServlet("/OrderPay")
public class OrderPay extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public OrderPay() {
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
			User user = (User) session.getAttribute("user");
			String[] cartIds = request.getParameter("cartIds").split(",");
			double totalCost = ServiceFactory.getOrderService().getTotalPrice(cartIds);
			String num = UUID.randomUUID().toString().trim().replaceAll("-", "").substring(2, 12);// 截取UUID的子串作为订单编号
			Order order = new Order();// 新建订单
			order.setUser(user);
			order.setNum(num);
			order.setPayMethod(0);
			order.setExpress(0);
			order.setStatus(0);
			order.setCost(totalCost);
			int count = ServiceFactory.getOrderService().doAdd(order);
			if (count > 0) {// 判断订单是否生成成功
				ServiceFactory.getOrderItemService().addOrderItemsBycartIds(cartIds);
				ArrayList<Category> list = ServiceFactory.getCategoryService().getAll();
				Order order1 = ServiceFactory.getOrderService().getLastInsertOrder();// 获取刚添加的订单
				ArrayList<OrderItem> list1 = ServiceFactory.getOrderItemService().getOrderItems(order1.getId());
				ArrayList<Image> list2 = ServiceFactory.getImageService().getImagesByOrder(order1.getId());
				request.setAttribute("list", list);
				request.setAttribute("list1", list1);
				request.setAttribute("list2", list2);
				request.setAttribute("order", order1);
				request.getRequestDispatcher("order_detail.jsp").forward(request, response);
			} else {
				out.write("<script>");
				out.write("alert('订单生成失败!');");
				out.write("location.href='" + request.getContextPath() + "/GetCart'");
				out.write("</script>");
			}
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
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
