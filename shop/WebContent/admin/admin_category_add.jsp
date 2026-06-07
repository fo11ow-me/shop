<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8" />
<title>新增分类</title>
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
						href="AdminGetCategory">分类管理</a><span class="crumb-step">&gt;</span><span>新增分类</span>
				</div>
			</div>
			<div class="result-wrap">
				<div class="result-content">
					<form action="AdminCategoryAdd" method="post" id="myform"
						name="myform">
						<table class="insert-tab" style="width: 100%">
							<tbody>
								<tr>
									<th><i class="require-red">*</i>父级id：</th>
									<td><select class="common-text required" name="parentId">
											<option value="0" selected="selected">根分类</option>
											<c:forEach var="category" items="${list}">
												<c:if test="${category.parentId == 0}">
													<option value="${category.id}">${category.name }</option>
												</c:if>
											</c:forEach>
									</select></td>
								</tr>
								<tr>
									<th><i class="require-red">*</i>分类名：</th>
									<td><input class="common-text required" name="name"
										size="50" value="" type="text"></td>
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