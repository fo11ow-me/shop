package com.shop.servlet.product;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.shop.service.ServiceFactory;

/**
 * Servlet implementation class AdminProductDelete
 */
@WebServlet("/admin/AdminProductDelete")
public class AdminProductDelete extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AdminProductDelete() {
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
				PrintWriter out = response.getWriter();
				int id = Integer.parseInt(request.getParameter("id"));// 获取商品id
				int pageIndex = Integer.parseInt(request.getParameter("page"));// 获取page参数
				String keyword = request.getParameter("keyword");// 获取关键字
				if (ServiceFactory.getProductService().doDelete(id) <= 0) {
					out.write("<script>");
					out.write("alert('商品删除失败!');");
					out.write("</script>");
				}
				ServiceFactory.getImageService().doDeleteImages(id);
				response.sendRedirect(
						request.getContextPath() + "/admin/AdminGetProduct?keyword=" + keyword + "&page=" + pageIndex);
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 设置字符集编码
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		String items[] = request.getParameterValues("items");
		for (int i = 0; i < items.length; i++) {
			int id = Integer.parseInt(items[i]);
			if (ServiceFactory.getProductService().doDelete(id) <= 0) {
				out.write("<script>");
				out.write("alert('商品删除失败!');");
				out.write("</script>");
				break;// 如果某个商品删除失败，则跳出循环
			}
			ServiceFactory.getImageService().doDeleteImages(id);
		}
		response.sendRedirect(request.getContextPath() + "/admin/AdminGetProduct");
	}

}
