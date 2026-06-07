<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8" />
<title>商品管理</title>
<link rel="stylesheet" type="text/css" href="css/common.css" />
<link rel="stylesheet" type="text/css" href="css/main.css" />
<script type="text/javascript" charset=UTF-8>
	//删除一个
	function deleteOne(mess, url) {
		if (confirm(mess)) {
			location.href = url;
		}
	}
	//删除多个
	function deleteMore(mess, id) {
		if (isSelected()) {
			if (confirm(mess)) {
				var form = document.getElementById(id);
				form.submit();
			}
		} else {
			alert('还未选择要删除的商品！');
		}
	}
	//同时选择多个
	function selectMore(e) {
		var a = document.getElementsByName('items');
		for (var i = 0; i < a.length; i++) {
			a[i].checked = e.checked;
		}
	}

	//判断是否有商品被选中
	function isSelected() {
		var a = document.getElementsByName('items');
		for (var i = 0; i < a.length; i++) {
			if (a[i].checked) {
				return true;
			}
		}
		return false;
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
						class="crumb-step">&gt;</span><span class="crumb-name">商品管理</span>
				</div>
			</div>
			<div class="search-wrap">
				<div class="search-content">
					<form action="AdminGetProduct" method="get">
						<table class="search-tab">
							<tr>
								<th width="10"></th>
								<td><input class="common-text" name="keyword"
									value="${param.keyword}" id="" type="text"
									onkeydown="if(event.keyCode==32){return false;}"></td>
								<td><input class="btn btn-primary btn2" name="sub"
									value="搜索商品" type="submit"></td>
							</tr>
						</table>
					</form>
				</div>
			</div>
			<div class="result-wrap">
				<form name="myform" id="myform" method="post"
					action="AdminProductDelete">
					<div class="result-title">
						<div class="result-list">
							<a href="AdminProductAdd"><i class="icon-font"> </i>新增商品</a> 
							<a id="batchDel" href="javascript:deleteMore('你确定要删除这些商品吗？','myform')"> 
								<i class="icon-font"></i>批量删除
							</a>
						</div>
					</div>
					<div class="result-content">
						<table class="result-tab" style="width: 100%">
							<tr>
								<th class="tc" width="5%"><input class="allChoose" name=""
									onclick="selectMore(this)" type="checkbox"></th>
								<th>id</th>
								<th>商品名称</th>
								<th>商品价格</th>
								<th>商品库存</th>
								<th>商品图片</th>
								<th>商品分类</th>
								<th>商品信息</th>
								<th>操作</th>
							</tr>
							<c:forEach var="product" items="${list}">
								<tr>
									<td class="tc"><input name="items" value="${product.id}"
										type="checkbox"></td>
									<td>${product.id}</td>
									<td>${product.name}</td>
									<td>${product.price}</td>
									<td>${product.stock}</td>
									<td><c:forEach var="image" items="${list1 }">
											<c:if test="${image.product.id == product.id }">
												<img src="http://localhost:8080/image/${image.url }"
													style="width: 80px; height: 50px">
											</c:if>
										</c:forEach></td>
									<td>${product.category.name }</td>
									<td>${product.detail}</td>
									<td><a class="link-update"
										href="AdminProductUpdate?keyword=${param.keyword }&page=${pageIndex}&id=${product.id}">修改</a>
										<a class="link-del"
										href="javascript:deleteOne('你确定要删除商品[${product.name }]吗？','AdminProductDelete?keyword=${param.keyword }&page=${pageIndex}&id=${product.id}')">删除</a>
									</td>
								</tr>
							</c:forEach>
						</table>
						<div class="list-page">
							共${totalNumber}条记录，当前第${pageIndex}/${pageTotalNumber }页 <a
								href="AdminGetProduct?keyword=${param.keyword }&page=1">首页</a> <a
								href="AdminGetProduct?keyword=${param.keyword }&page=${pageIndex-1<1?1:pageIndex-1}">上一页</a>
							<a
								href="AdminGetProduct?keyword=${param.keyword }&page=${pageIndex+1>pageTotalNumber?pageTotalNumber:pageIndex+1}">下一页</a>
							<a
								href="AdminGetProduct?keyword=${param.keyword }&page=${pageTotalNumber}">尾页</a>
						</div>
					</div>
				</form>
			</div>
		</div>
		<!--/main-->
	</div>
</body>
</html>