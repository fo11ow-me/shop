package com.shop.servlet.cart;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.shop.entity.Cart;
import com.shop.entity.Category;
import com.shop.entity.Image;
import com.shop.entity.User;
import com.shop.service.ServiceFactory;

/**
 * Servlet implementation class GetCart
 */
@WebServlet("/GetCart")
public class GetCart extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetCart() {
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
			User user = (User) session.getAttribute("user");
			ArrayList<Category> list = ServiceFactory.getCategoryService().getAll();//得到所有分类
			ArrayList<Cart> list1 = ServiceFactory.getCartService().getCarts(user.getId());
			ArrayList<Image> list2 = ServiceFactory.getImageService().getImagesByCart(list1);
			request.setAttribute("list", list);
			request.setAttribute("list1", list1);
			request.setAttribute("list2", list2);
			request.getRequestDispatcher("cart.jsp").forward(request, response);
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
