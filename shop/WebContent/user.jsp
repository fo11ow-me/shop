<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head lang="en">
<meta charset="utf-8" />
<title>个人中心</title>
<link rel="stylesheet" type="text/css" href="css/public.css" />
<link rel="stylesheet" type="text/css" href="css/mygxin.css" />
</head>
<body>
	<div class="head ding">
		<!------------------------------head------------------------------>
		<%@ include file="head.jsp"%>
	</div>
	<!------------------------------idea------------------------------>
	<div class="address mt" id="add">
		<div class="wrapper clearfix">
			<a href="GetCategory" class="fl">首页</a><span>/</span><a href="GetUser"
				class="on">个人中心</a>
		</div>
	</div>
	<!------------------------------Bott------------------------------>
	<div class="Bott">
		<div class="wrapper clearfix">
			<!------------------------------user_side_bar------------------------------>
			<%@ include file="user_side_bar.jsp"%>
			<div class="you fl">
				<div class="tx clearfix">
					<div class="fl clearfix">
						<a href="#" class="fl"><img src="img/tx.png" /></a>
						<p class="fl">
							<span>${sessionScope.user.name}</span><a href="UserUpdate">修改个人信息></a>
						</p>
					</div>
					<div class="fr">绑定邮箱：${sessionScope.user.email}</div>
				</div>
				<div class="bott">
					<div class="clearfix">
						<a href="#" class="fl"><img src="img/gxin1.jpg" /></a>
						<p class="fl">
							<span>订单<strong></strong></span><a href="GetOrder">查看订单></a>
						</p>
					</div>
					<div class="clearfix">
						<a href="#" class="fl"><img src="img/gxin2.jpg" /></a>
						<p class="fl">
							<span>购物车<strong></strong></span><a href="GetCart">查看购物车></a>
						</p>
					</div>
				</div>
			</div>
		</div>
	</div>
	<!----------------------------- go_top -------------------------->
	<%@ include file="go_top.jsp"%>
	<!----------------------------- footer -------------------------->
	<%@ include file="footer.jsp"%>
	<script src="js/jquery-1.12.4.min.js" type="text/javascript"
		charset="utf-8"></script>
	<script src="js/public.js" type="text/javascript" charset="utf-8"></script>
	<script src="js/user.js" type="text/javascript" charset="utf-8"></script>
</body>
</html>