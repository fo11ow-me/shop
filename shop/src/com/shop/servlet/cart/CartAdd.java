package com.shop.servlet.cart;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.shop.entity.Cart;
import com.shop.entity.User;
import com.shop.service.ServiceFactory;

/**
 * Servlet implementation class CartAdd
 */
@WebServlet("/CartAdd")
public class CartAdd extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CartAdd() {
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
		int count = 0;
		int amount =Integer.parseInt(request.getParameter("amount"));//添加到购物车中的商品的个数
		int id = Integer.parseInt(request.getParameter("id"));//得到商品id
		if (session.getAttribute("user")!=null) {
			User user = (User)session.getAttribute("user");
			Cart cart = ServiceFactory.getCartService().getCart(user.getId(), id);
			if (cart!=null) {//判断用户之前是否添加过此商品
				int amount1 = cart.getAmount() + amount;
				cart.setAmount(amount1);
				count = ServiceFactory.getCartService().doUpdate(cart);
			}else {
				Cart cart1 = new Cart();
				cart1.setUser(user);
				cart1.setProduct(ServiceFactory.getProductService().getById(id));
				cart1.setAmount(amount);
				count = ServiceFactory.getCartService().doAdd(cart1);
			}
			if (count > 0) {//判断商品是否添加到了购物车
				response.sendRedirect(request.getContextPath()+"/GetCart");
			}else {
				out.write("<script>");
				out.write("alert('商品添加失败!');");
				out.write("location.href='" + request.getContextPath() + "/GetProductDetail?id="+id+"'");
				out.write("</script>");
			}
		}else {
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
