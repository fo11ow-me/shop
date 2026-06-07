<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head lang="en">
<meta charset="utf-8" />
<title>order</title>
<link rel="stylesheet" type="text/css" href="css/public.css" />
<link rel="stylesheet" type="text/css" href="css/proList.css" />
<link rel="stylesheet" type="text/css" href="css/mygxin.css" />
</head>
<body>
	<!----------------------------------------order------------------>
	<div class="head ding">
		<!------------------------------head------------------------------>
		<%@ include file="head.jsp"%>
	</div>
	<div class="order cart mt">
		<!-----------------site------------------->
		<div class="site">
			<p class="wrapper clearfix">
				<span class="fl">订单确认</span><img class="top"
					src="img/temp/cartTop02.png">
			</p>
		</div>
		<!-----------------orderCon------------------->
		<div class="orderCon wrapper clearfix">
			<div class="orderL fl">
				<!--------h3---------------->
				<h3>收件信息</h3>
				<!--------addres---------------->
				<div class="addres clearfix">
					<div class="addre fl on">
						<div class="tit clearfix">
							<p class="fl">
								${sessionScope.user.name}<span class="default">[默认地址]</span>
							</p>
						</div>
						<div class="addCon">
							<p>${sessionScope.user.address}</p>
							<p>${sessionScope.user.phone}</p>
						</div>
					</div>
				</div>
				<h3>支付方式</h3>
				<!--------way---------------->
				<div class="way clearfix">
					<img class="on" src="img/temp/way01.jpg"><img
						src="img/temp/way02.jpg"><img src="img/temp/way03.jpg"><img
						src="img/temp/way04.jpg">
				</div>
				<h3>选择快递</h3>
				<!--------dis---------------->
				<div class="dis clearfix">
					<span class="on">顺风快递</span><span>百世汇通</span><span>圆通快递</span><span>中通快递</span>
				</div>
			</div>
			<div class="orderR fr">
				<div class="msg">
					<h3>
						订单内容<a href="GetCart" class="fr">返回购物车</a>
					</h3>
					<!--------ul---------------->
					<c:forEach var="orderItem" items="${list1}">
						<ul class="clearfix">
							<li class="fl">
								<c:forEach var="image" items="${list2 }">
									<c:if test="${image.product.id == orderItem.product.id }">
										<img src="http://localhost:8080/image/${image.url }"
											style="width: 87px; height: 87px">
									</c:if>
								</c:forEach></li>
							<li class="fl"><p>${orderItem.product.name}</p>
								<p>样式分类：
								<c:forEach var="category" items="${list}" >
								<c:if test="${orderItem.product.category.id == category.id }">
									${category.name}
								</c:if>
								</c:forEach>
								</p>
								<p>数量：${orderItem.amount }</p></li>
							<li class="fr">￥${orderItem.amount*orderItem.product.price }</li>
						</ul>
					</c:forEach>
				</div>
				<!--------tips---------------->
				<div class="tips">
					<p>
						<span class="fl">商品金额：</span><span class="fr">￥${order.cost }</span>
					</p>
					<p>
						<span class="fl">优惠金额：</span><span class="fr">￥0.00</span>
					</p>
					<p>
						<span class="fl">运费：</span><span class="fr">免运费</span>
					</p>
				</div>
				<!--------tips count---------------->
				<div class="count tips">
					<p>
						<span class="fl">合计：</span><span class="fr">￥${order.cost }</span>
					</p>
				</div>
				<!--<input type="button" name="" value="去支付">-->
				<a href="OrderUpdate?id=${order.id }" class="pay">去支付</a>
			</div>
		</div>
	</div>
	<!--编辑弹框-->
	<!--遮罩-->
	<div class="mask"></div>
	<!----------------------------- go_top -------------------------->
	<%@ include file="go_top.jsp"%>
	<!----------------------------- footer -------------------------->
	<%@ include file="footer.jsp"%>
	<script src="js/jquery-1.12.4.min.js" type="text/javascript"
		charset="utf-8"></script>
	<script src="js/public.js" type="text/javascript" charset="utf-8"></script>
	<script src="js/pro.js" type="text/javascript" charset="utf-8"></script>
	<script src="js/user.js" type="text/javascript" charset="utf-8"></script>
</body>
</html>