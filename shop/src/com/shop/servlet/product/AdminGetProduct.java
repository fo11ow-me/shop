package com.shop.servlet.product;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.shop.entity.Image;
import com.shop.entity.Product;
import com.shop.service.ServiceFactory;

/**
 * Servlet implementation class AdminGetProduct
 */
@WebServlet("/admin/AdminGetProduct")
public class AdminGetProduct extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AdminGetProduct() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int pageSize = 6;//页面所显示的商品数
		int pageIndex = 1;//当前页面索引,默认为1，首页
		int pageTotalNumber = 1;//页面总数
		String page = request.getParameter("page");//接受客户端传来的page参数
		String keyword = request.getParameter("keyword");//关键字
		int totalNumber = ServiceFactory.getProductService().getTotalNumber(keyword);//商品总数
		if (totalNumber > 0) {//判断是否有商品
			if (totalNumber%pageSize!=0) {//计算总页数
				pageTotalNumber = totalNumber/pageSize + 1;
			}else {
				pageTotalNumber = totalNumber/pageSize;
			}
			if (page != null) {
				pageIndex = Integer.parseInt(page);
			}
		}
		//查询得到对应的商品
		ArrayList<Product> list = ServiceFactory.getProductService().getSome(pageSize, pageIndex,keyword);
		//查询对应商品的第一个图片
		ArrayList<Image> list1 = ServiceFactory.getImageService().getFirstImage(list);
		request.setAttribute("pageIndex", pageIndex);
		request.setAttribute("pageTotalNumber", pageTotalNumber);
		request.setAttribute("totalNumber", totalNumber);
		request.setAttribute("list", list);
		request.setAttribute("list1", list1);
		request.getRequestDispatcher("admin_product.jsp").forward(request, response);//跳转到商品列表页面
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
