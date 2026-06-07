<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!doctype html>
<html>
<head>
<meta charset="UTF-8" />
<title>后台登录</title>
<link rel="stylesheet" type="text/css" href="css/admin_login.css" />
</head>
<body>
	<div class="admin_login_wrap">
		<h1>后台登录</h1>
		<div class="adming_login_border">
			<div class="admin_input">
				<form action="AdminLogin" method="post">
					<ul class="admin_items">
						<li><label for="user">用户名：</label> <input type="text"
							name="name" value="" id="user" size="40"
							class="admin_input_style" /></li>
						<li><label for="pwd">密码：</label> <input type="password"
							name="password" value="" id="pwd" size="40"
							class="admin_input_style" /></li>
						<li><input type="submit" tabindex="3" value="提交"
							class="btn btn-primary" /></li>
					</ul>
				</form>
			</div>
		</div>
		<p class="admin_copyright">
			<a tabindex="5" href="#"></a> &copy; 2020 Powered
			by Judy
		</p>
	</div>
</body>
</html>
