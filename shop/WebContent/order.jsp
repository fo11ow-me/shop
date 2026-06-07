<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head lang="en">
<meta charset="utf-8" />
<title>我的订单</title>
<link rel="stylesheet" type="text/css" href="css/public.css" />
<link rel="stylesheet" type="text/css" href="css/myorder.css" />
</head>
<body>
	<!------------------------------head------------------------------>
	<div class="head ding">
		<!------------------------------head------------------------------>
		<%@ include file="head.jsp"%>
	</div>
	</div>
	<!------------------------------idea------------------------------>
	<div class="address mt">
		<div class="wrapper clearfix">
			<a href="#" class="fl">首页</a><span>/</span><a href="mygxin.html">个人中心</a><span>/</span><a
				href="myorderq.html" class="on">我的订单</a>
		</div>
	</div>
	<!------------------------------Bott------------------------------>
	<div class="Bott">
		<div class="wrapper clearfix">
			<!------------------------------user_side_bar------------------------------>
			<%@ include file="user_side_bar.jsp"%>
			<div class="you fl">
				<div class="my clearfix">
					<h2 class="fl">我的订单</h2>
					<a href="#" class="fl">请谨防钓鱼链接或诈骗电话，了解更多&gt;</a>
				</div>
				<div class="dlist clearfix">
					<ul class="fl clearfix" id="wa">
						<li class="on"><a href="">全部订单</a></li>
					</ul>
				</div>
				<c:forEach var="order" items="${list1 }">
					<div class="dkuang deng">
						<c:if test="${order.status==0 }">
						<p class="one">待支付</p>
						</c:if>
						<c:if test="${order.status==1 }">
						<p class="one">已支付</p>
						</c:if>
						<div class="word clearfix">
							<ul class="fl clearfix">
								<li><fmt:formatDate value="${order.createTime }" pattern="yyyy-MM-dd HH:mm:ss"/></li>
								<li>${sessionScope.user.name }</li>
								<li>订单号:${order.num }</li>
								<li>在线支付</li>
								<li></li>
							</ul>
							<p class="fr">
								订单金额：<span>${order.cost }</span>元
							</p>
						</div>
						<c:forEach var="orderItem" items="${list2 }">
							<c:if test="${orderItem.order.id == order.id }">
								<div class="shohou clearfix">
									<c:forEach var="image" items="${list3 }">
										<c:if test="${image.product.id == orderItem.product.id }">
											<a href="GetProductDetail?id=${orderItem.product.id }" class="fl"><img src="http://localhost:8080/image/${image.url }" /></a>
										</c:if>
									</c:forEach>
									<p class="fl">
										<a href="#">${orderItem.product.name }</a> <a href="#">¥${orderItem.amount }×${orderItem.product.price }</a>
										<a href="#">${orderItem.product.detail }</a>
									</p>
								</div>
							</c:if>
						</c:forEach>
						<c:if test="${order.status == 0 }">
						<div class="shohou clearfix">
							<p class="fr">
								<a href="OrderUpdate?id=${order.id }">立即支付</a>
							</p>
						</div>
						</c:if>
						<c:if test="${order.status == 1 }">
						<div class="shohou clearfix">
							<p class="fr">
								<a href="OrderDelete?id=${order.id }">订单删除</a>
							</p>
						</div>
						</c:if>
					</div>
				</c:forEach>
			</div>
		</div>
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