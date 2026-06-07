<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8" />
<title>分类管理</title>
<link rel="stylesheet" type="text/css" href="css/common.css" />
<link rel="stylesheet" type="text/css" href="css/main.css" />
<script type="text/javascript" charset=UTF-8>
	//删除分类
	function deleteOne(mess, url) {
		if (confirm(mess)) {
			location.href = url;
		}
	}
</script>
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
						class="crumb-step">&gt;</span><span class="crumb-name">分类管理</span>
				</div>
			</div>
			<div class="result-wrap">
				<form name="myform" id="myform">
					<div class="result-title">
						<div class="result-list">
							<a href="AdminCategoryAdd"><i class="icon-font"> </i>新增分类</a>
						</div>
					</div>
					<div class="result-content">
						<table class="result-tab" style="width: 80%">
							<tr>
								<th>id</th>
								<th>分类名</th>
								<th>操作</th>
							</tr>
							<c:forEach var="category" items="${list}">
								<c:if test="${category.parentId == 0}">
									<tr style="background: DarkGray">
										<td>${category.id}</td>
										<td>${category.name}</td>
										<td><a href="AdminCategoryUpdate?id=${category.id}">修改</a>
											<a
											href="javascript:deleteOne('你确定要删除父分类[${category.name }]吗？','AdminCategoryDelete?id=${category.id}&parentId=${category.parentId }')">删除</a></td>
									</tr>
									<c:forEach var="category1" items="${list}">
										<c:if test="${category1.parentId == category.id}">
											<tr>
												<td>${category1.id}</td>
												<td style="width: 300px"><i style="padding-left: 40px">${category1.name}</i></td>
												<td><a href="AdminCategoryUpdate?id=${category1.id}">修改</a>
													<a
													href="javascript:deleteOne('你确定要删除分类[${category1.name }]吗？','AdminCategoryDelete?id=${category1.id}&parentId=${category1.parentId }')">删除</a></td>
											</tr>
										</c:if>
									</c:forEach>
								</c:if>
							</c:forEach>
						</table>
					</div>
				</form>
			</div>
		</div>
		<!--/main-->
	</div>
</body>
</html>