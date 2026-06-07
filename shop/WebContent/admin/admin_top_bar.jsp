<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<div class="topbar-wrap white">
	<div class="topbar-inner clearfix">
		<div class="topbar-logo-wrap clearfix">
			<h1 class="topbar-logo none">
				<a href="admin_index.jsp" class="navbar-brand">后台管理</a>
			</h1>
			<ul class="navbar-list clearfix">
				<li><a class="on" href="admin_index.jsp">首页</a></li>
			</ul>
		</div>
		<div class="top-info-wrap">
			<ul class="top-info-list clearfix">
				<li><a href="${pageContext.request.contextPath }/AdminLogout">退出</a></li>
			</ul>
		</div>
	</div>
</div>
