package com.shop.servlet.cart;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.shop.service.ServiceFactory;

/**
 * Servlet implementation class CartDelete
 */
@WebServlet("/CartDelete")
public class CartDelete extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CartDelete() {
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
		int count = ServiceFactory.getCartService().doDelete(id);
		if (count <= 0) {
			out.write("<script>");
			out.write("alert('购物车商品删除失败!');");
			out.write("</script>");
		}
		response.sendRedirect(request.getContextPath()+"/GetCart");
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 设置字符集编码
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		// 获取打印输出流
		PrintWriter out = response.getWriter();
		String cartIds[] = request.getParameter("cartIds").split(",");
		for (int i = 0; i < cartIds.length; i++) {
			int  id= Integer.parseInt(cartIds[i]);
			if(ServiceFactory.getCartService().doDelete(id)<=0) {
				out.write("<script>");
				out.write("alert('购物车商品删除失败!');");
				out.write("</script>"); 
				break;// 如果某个商品删除失败，则跳出循环
			}
		}
		response.sendRedirect(request.getContextPath()+"/GetCart");
	}

}
