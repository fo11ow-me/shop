<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8" />
<title>后台管理首页</title>
<link rel="stylesheet" type="text/css" href="css/common.css" />
<link rel="stylesheet" type="text/css" href="css/main.css" />
</head>
<body>
	<%@ include file="admin_top_bar.jsp"%>
	<div class="container clearfix">
		<%@ include file="admin_side_bar.jsp"%>
		<!--/sidebar-->
		<div class="main-wrap">
			<div class="result-wrap">
				<div class="result-title">
					<h1>系统基本信息</h1>
				</div>
				<div class="result-content">
					<ul class="sys-info-list">
						<li><label class="res-lab">操作系统</label> <span
							class="res-info">Window 10</span></li>
						<li><label class="res-lab">运行环境</label> <span
							class="res-info">Tomcat-9.0.39&nbsp;&nbsp;MySQL-8.0.17&nbsp;&nbsp;MySQL-connector-java-8.0.17&nbsp;&nbsp;JDK-14
						</span></li>
						<li><label class="res-lab">北京时间</label><span class="res-info">2020年12月18日
						</span></li>
					</ul>
				</div>
			</div>
		</div>
		<!--/main-->
	</div>
</body>
</html>