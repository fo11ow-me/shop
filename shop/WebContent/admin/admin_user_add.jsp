<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8" />
<title>新增用户</title>
<link rel="stylesheet" type="text/css" href="css/common.css" />
<link rel="stylesheet" type="text/css" href="css/main.css" />
</head>
<body>
	<%@ include file="admin_top_bar.jsp"%>
	<div class="container clearfix">
		<%@ include file="admin_side_bar.jsp"%>
		<!--/sidebar-->
		<div class="main-wrap">
			<div class="crumb-wrap">
				<div class="crumb-list">
					<i class="icon-font"></i><a href="admin_index.jsp">首页</a><span
						class="crumb-step">&gt;</span><a class="crumb-name"
						href="AdminGetUser">用户管理</a><span class="crumb-step">&gt;</span><span>新增用户</span>
				</div>
			</div>
			<div class="result-wrap">
				<div class="result-content">
					<form action="AdminUserAdd" method="post" id="myform" name="myform">
						<table class="insert-tab" style="width: 100%">
							<tbody>
								<tr>
									<th><i class="require-red">*</i>用户名：</th>
									<td><input class="common-text required" name="name"
										size="50" value="" type="text"></td>
								</tr>
								<tr>
									<th><i class="require-red">*</i>用户密码：</th>
									<td><input class="common-text required" name="password"
										size="50" value="" type="password"></td>
								</tr>
								<tr>
									<th><i class="require-red">*</i>确认密码：</th>
									<td><input class="common-text required" name="repassword"
										size="50" value="" type="password"></td>
								</tr>
								<tr>
									<th><i class="require-red">*</i>电话号码：</th>
									<td><input class="common-text required" name="phone"
										size="50" value="" type="text"></td>
								</tr>
								<tr>
									<th><i class="require-red">*</i>用户邮箱：</th>
									<td><input class="common-text required" name="email"
										size="50" value="" type="email"></td>
								</tr>
								<tr>
									<th><i class="require-red">*</i>送货地址：</th>
									<td><input class="common-text required" name="address"
										size="50" value="" type="text"></td>
								</tr>
								<tr>
									<th><i class="require-red">*</i>用户身份：</th>
									<td><input type="radio" name="status" value="0"
										checked="checked">普通用户&nbsp;&nbsp;&nbsp;<input
										type="radio" name="status" value="1">管理员</td>
								</tr>
								<tr>
									<th></th>
									<td><input class="btn btn-primary btn6 mr10" value="提交"
										type="submit"> <input class="btn btn6"
										onClick="history.go(-1)" value="返回" type="button"></td>
								</tr>
							</tbody>
						</table>
					</form>
				</div>
			</div>

		</div>
		<!--/main-->
	</div>
</body>
</html>