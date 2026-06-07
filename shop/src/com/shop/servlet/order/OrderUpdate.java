package com.shop.servlet.order;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.shop.entity.Order;
import com.shop.service.ServiceFactory;

/**
 * Servlet implementation class OrderUpdate
 */
@WebServlet("/OrderUpdate")
public class OrderUpdate extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OrderUpdate() {
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
		int id = Integer.parseInt(request.getParameter("id"));
		Order order = ServiceFactory.getOrderService().getById(id);
		order.setStatus(1);
		int count = ServiceFactory.getOrderService().doUpdate(order);
		if (count > 0) {
			out.write("<script>");
			out.write("confirm('支付成功!');");
			out.write("</script>");
		}else {
			out.write("<script>");
			out.write("alert('支付失败!');");
			out.write("</script>");
		}
		response.sendRedirect(request.getContextPath()+"/GetOrder");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
