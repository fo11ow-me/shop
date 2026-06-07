package com.shop.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet Filter implementation class AdminCheck
 */
@WebFilter(urlPatterns = "/admin/*")
public class AdminCheck implements Filter {

	/**
	 * Default constructor.
	 */
	public AdminCheck() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request1 = (HttpServletRequest) request;
		HttpServletResponse response1 = (HttpServletResponse) response;
		// 设置字符集编码
		request.setCharacterEncoding("UTF-8");
		String path = request1.getRequestURI();//获取请求的路径
		if (!path.endsWith(".css")&&!path.endsWith(".js")&&!path.endsWith(".png")&&!path.endsWith(".gif")) {//放行静态资源
			response.setContentType("text/html;charset=UTF-8");
			HttpSession session = request1.getSession(false);// session不自动创建
			if (session == null || session.getAttribute("admin") == null) {// 判断session和adminName是否为空
				PrintWriter out = response1.getWriter();
				out.write("<script>");
				out.write("alert('你还未登录请先登录!');");
				out.write("location.href='" + request1.getContextPath() + "/admin_login.jsp'");
				out.write("</script>");
				out.close();//关闭打印流
				return;//如果还未登录，直接退出过滤器去登录
			}
		}
		chain.doFilter(request, response);
	}
	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
