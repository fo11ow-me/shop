<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head lang="en">
<meta charset="utf-8" />
<title>个人信息修改</title>
<link rel="stylesheet" type="text/css" href="css/public.css" />
<link rel="stylesheet" type="text/css" href="css/mygrxx.css" />
</head>
<body>
	<div class="head ding">
		<!------------------------------head------------------------------>
		<%@ include file="head.jsp"%>
	</div>
	<!------------------------------idea------------------------------>
	<div class="address mt">
		<div class="wrapper clearfix">
			<a href="GetCategory" class="fl">首页</a><span>/</span><a href="GetUser"
				class="on">个人信息</a>
		</div>
	</div>
	<!------------------------------Bott------------------------------>
	<div class="Bott">
		<div class="wrapper clearfix">
			<!------------------------------user_side_bar------------------------------>
			<%@ include file="user_side_bar.jsp"%>
			<div class="you fl">
				<h2>个人信息</h2>
				<div class="gxin">
					<div class="tx">
						<a href="#"><img src="img/tx.png" />
							<p id="avatar"></p></a>
					</div>
					<div class="xx">
						<h3 class="clearfix">
							<strong class="fl">基础资料</strong><a href="#" class="fr" id="edit1">编辑</a>
						</h3>
						<div>姓名：${sessionScope.user.name}</div>
						<div>电话：${sessionScope.user.phone}</div>
						<div>邮箱：${sessionScope.user.email}</div>
						<div>地址：${sessionScope.user.address}</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<!--遮罩-->
	<div class="mask"></div>
	<!--编辑弹框-->
	<div class="bj">
		<div class="clearfix">
			<a href="#" class="fr gb"><img src="img/icon4.png" /></a>
		</div>
		<h3>编辑基础资料</h3>
		<form action="UserUpdate" method="post">
			<p>
				<label>姓名：</label><input type="text" name="name"
					value="${sessionScope.user.name}" />
			</p>
			<p>
				<label>电话：</label><input type="text" name="phone"
					value="${sessionScope.user.phone}" />
			</p>
			<p>
				<label>邮箱：</label><input type="email" name="email"
					style="width: 252px; height: 42px"
					value="${sessionScope.user.email}" />
			</p>
			<p>
				<label>地址：</label><input type="text" name="address"
					value="${sessionScope.user.address}" />
			</p>
			<div class="bc">
				<input type="submit" value="保存" /><input type="reset" value="取消" />
			</div>
		</form>
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