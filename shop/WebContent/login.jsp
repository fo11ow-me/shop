<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>用户登录</title>
<link rel="stylesheet" type="text/css" href="css/public.css" />
<link rel="stylesheet" type="text/css" href="css/login.css" />
<script src="js/jquery-1.12.4.min.js"></script>
<script type="text/javascript" src="js/checkLogin.js"></script>
<style>
.reg p .error {
	display: inline-block;
	background-color: #ffe8e0;
	vertical-align: middle;
	height: 25px;
	line-height: 25px;
	margin-left: 10px;
}
</style>
</head>
<body>
	<!-------------------login-------------------------->
	<div class="reg">
		<form action="Login" method="post" onsubmit="return checkInfo(this)">
			<h1>
				<a href="GetCategory"><img src="img/temp/logo.png"></a>
			</h1>
			<h2
				style="padding: 0px; margin: 0px; font-size: 30px; background: #3344AA; text-align: center; line-height: 50px; color: #FFFFFF">用户登录</h2>
			<p>
				<input type="text" style="vertical-align: middle;" name="name"
					value="" placeholder="请输入用户名" onfocus="focusItem(this)"
					onblur="checkItem(this)"><span></span>
			</p>
			<p>
				<input type="password"
					style="vertical-align: middle; border: 1px solid #dbdbdb;"
					name="password" value="" placeholder="请输入密码"
					onfocus="focusItem(this)" onblur="checkItem(this)"><span></span>
			</p>
			<p>
				<input class="code" type="text"
					style="vertical-align: middle; margin-right: 10px;"
					name="verifyCode" value="" placeholder="验证码"
					onfocus="focusItem(this)" onblur="checkItem(this)"> <img
					style="vertical-align: middle;" onclick="changeCode(this)"
					src="GetVerifyCode"><span></span>
			</p>
			<p>
				<input type="submit" name="" value="登  录">
			</p>
			<p class="txt">
				<a class="" href="register.jsp">免费注册</a>&nbsp;&nbsp;<a
					href="#">忘记密码？</a>
			</p>
		</form>
	</div>
</body>
</html>