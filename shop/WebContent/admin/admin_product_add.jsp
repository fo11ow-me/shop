<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8" />
<title>新增商品</title>
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
						href="AdminGetProduct">商品管理</a><span class="crumb-step">&gt;</span><span>新增商品</span>
				</div>
			</div>
			<div class="result-wrap">
				<div class="result-content">
					<form action="AdminProductAdd" method="post" id="myform"
						enctype="multipart/form-data" name="myform">
						<table class="insert-tab" style="width: 100%">
							<tbody>
								<tr>
									<th><i class="require-red">*</i>商品名称：</th>
									<td><input class="common-text required" name="name"
										size="50" value="" type="text"></td>
								</tr>
								<tr>
									<th><i class="require-red">*</i>商品价格：</th>
									<td><input class="common-text required" name="price"
										size="50" value="" type="number"></td>
								</tr>
								<tr>
									<th><i class="require-red">*</i>商品库存：</th>
									<td><input class="common-text required" name="stock"
										size="50" value="" type="number"></td>
								</tr>
								<tr>
									<th><i class="require-red">*</i>商品图片：</th>
									<td><input id="" name="image" type="file"
										multiple="multiple"></td>
								</tr>
								<tr>
									<th><i class="require-red">*</i>商品分类：</th>
									<td><select class="common-text required" name="category">
											<c:forEach var="category" items="${list}">
												<c:if test="${category.parentId == 0}">
													<option value="${category.id}" disabled="disabled"
														style="background: DarkGray">${category.name }</option>
													<c:forEach var="category1" items="${list }">
														<c:if test="${category1.parentId == category.id }">
															<option value="${category1.id}">${category1.name }</option>
														</c:if>
													</c:forEach>
												</c:if>
											</c:forEach>
									</select></td>
								</tr>
								<tr>
									<th><i class="require-red">*</i>商品信息：</th>
									<td><textarea name="detail" class="common-textarea"
											id="content" cols="30" style="width: 78%;" rows="10"></textarea></td>
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