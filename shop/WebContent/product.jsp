<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%> 
<!DOCTYPE html>
<html>
<head lang="en">
<meta charset="utf-8" />
<title>${category.name }</title>
<link rel="stylesheet" type="text/css" href="css/public.css" />
<link rel="stylesheet" type="text/css" href="css/proList.css" />
<link rel="stylesheet" type="text/css" href="css/index.css" />
</head>
<body>
	<div class="head">
		<!------------------------------head------------------------------>
		<%@ include file="head.jsp"%>
	</div>
	<!------------------------------banner------------------------------>
	<div class="banner">
		<a href="#"><img src="img/temp/banner1.jpg" /></a>
	</div>
	<!-----------------address------------------------------->
	<div class="address">
		<div class="wrapper clearfix">
			<a href="GetCategory">首页</a><span>/</span>
			<c:if test="${not empty parentCategory}">
				<a href="">${parentCategory.name }</a><span>/</span>
			</c:if>
			<a href="" class="on">${category.name }</a>
		</div>
	</div>
	<!----------------proList------------------------->
	<div class="people">
		<div class="wrapper">
			<h2>
				<img src="img/ih1.jpg" />
			</h2>
			<c:forEach begin="0" end="${fn:length(list1)}" step="4" varStatus="status">
				<div class="pList clearfix tran" style="justify-content:center;">
					<c:forEach begin="${status.index }" end="${status.index+3}" var="product"  items="${list1 }">
						<a href="GetProductDetail?id=${product.id}" style="width:268px;height:322px;margin:25px">
							<dl>
								<dt>
									<span class="abl"></span>
									<c:forEach var="image" items="${list2 }">
										<c:if test="${image.product.id == product.id }">
											<img src="http://localhost:8080/image/${image.url }" style="width:268px;height:268px">
										</c:if>
									</c:forEach>
									<span class="abr"></span>
								</dt>
								<dd>${product.name }</dd>
								<dd>
									<span>￥${product.price }</span>
								</dd>
							</dl>
						</a>
					</c:forEach>
				</div>
			</c:forEach>
		</div>
	</div>
	<!----------------mask------------------->
	<!-- <div class="mask"></div>  -->
	<!--返回顶部-->
	<!----------------------------- go_top -------------------------->
	<%@ include file="go_top.jsp"%>
	<div class="msk"></div>
	<!--footer-->
	<!----------------------------- footer -------------------------->
	<%@ include file="footer.jsp"%>
	<script src="js/jquery-1.12.4.min.js" type="text/javascript"
		charset="utf-8"></script>
	<script src="js/public.js" type="text/javascript" charset="utf-8"></script>
	<script src="js/nav.js" type="text/javascript" charset="utf-8"></script>
	<script src="js/pro.js" type="text/javascript" charset="utf-8"></script>
	<script src="js/cart.js" type="text/javascript" charset="utf-8"></script>
</body>
</html>