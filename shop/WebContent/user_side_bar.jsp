<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<div class="zuo fl">
	<h3>
		<a href="#"><img src="img/tx.png" /></a>
		<p class="clearfix">
			<span>${sessionScope.user.name}</span>
		</p>
		<p class="clearfix">
			<span><a href="Logout">退出登录</a></span>
		</p>
	</h3>
	<div>
		<h4>个人中心</h4>
		<ul>
			<li><a href="GetUser">我的中心</a></li>
			<li><a href="GetCart">我的购物车</a></li>
			<li><a href="GetOrder">我的订单</a></li>
		</ul>
	</div>
</div>