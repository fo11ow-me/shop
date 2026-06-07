<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>

<head>
<meta charset="UTF-8">
<title>${product.name }</title>
<link rel="stylesheet" type="text/css" href="css/public.css" />
<link rel="stylesheet" type="text/css" href="css/proList.css" />
<script type="text/javascript">
	function cartAdd(id){
		var amount = document.getElementById("amount").innerHTML;
		location.href='CartAdd?id='+id+'&amount='+amount;
	}
	function orderAdd(id){
		var amount = document.getElementById("amount").innerHTML;
		location.href='OrderAdd?id='+id+'&amount='+amount;
	}
</script>
</head>

<body>
	<!------------------------------head------------------------------>
	<div class="head">
		<!------------------------------head------------------------------>
		<%@ include file="head.jsp"%>
	</div>
	<!-----------------address------------------------------->
	<div class="address">
		<div class="wrapper clearfix">
			<a href="GetCategory">首页</a><span>/</span><a href="">${parentCategory.name }</a><span>/</span>
			<a href="GetProduct?id=${category.id }">${category.name }</a><span>/</span><a
				href="" class="on">${product.name }</a>
		</div>
	</div>
	<!-----------------------Detail------------------------------>
	<div class="detCon">
		<div class="proDet wrapper">
			<div class="proCon clearfix">
				<div class="proImg fl">
					<img class="det" src="http://localhost:8080/image/${list1[0].url }" />
					<div class="smallImg clearfix">
						<c:forEach var="image" items="${list1 }">
							<img src="http://localhost:8080/image/${image.url }"
								data-src="http://localhost:8080/image/${image.url }">
						</c:forEach>
					</div>
				</div>
				<div class="fr intro">
					<div class="title">
						<h4>${product.name }</h4>
						<p>【${product.detail }】</p>
						<span>￥${product.price }</span>
					</div>
					<div class="proIntro">
						<p>样式分类</p>
						<div class="smallImg clearfix categ">
							<c:forEach var="image" items="${list1 }">
								<p class="fl">
									<img src="http://localhost:8080/image/${image.url }"
										data-src="http://localhost:8080/image/${image.url }">
								</p>
							</c:forEach>
						</div>
						<p>
							商品库存<span>${product.stock }</span>件
						</p>
						<div class="num clearfix">
							<img class="fl sub" src="img/temp/sub.jpg"> <span
								id="amount" stock="${product.stock}" class="fl" contentEditable="true">1</span> <img
								class="fl add" src="img/temp/add.jpg">
						</div>
					</div>
					<div class="btns clearfix">
						<a href="javascript:orderAdd(${product.id })">
							<p class="buy fl">立即购买</p>
						</a><a href="javascript:cartAdd(${product.id })">
							<p class="cart fr">加入购物车</p>
						</a>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="like">
		<h4>猜你喜欢</h4>
		<div class="bottom">
			<div class="hd">
				<span class="prev"><img src="img/temp/prev.png"></span><span
					class="next"><img src="img/temp/next.png"></span>
			</div>
			<div class="imgCon bd">
				<div class="likeList clearfix" style="justify-content:space-around;">
					<div >
						<c:forEach end="4" var="product" items="${list2 }">
							<a href="GetProductDetail?id=${product.id}">
								<dl>
									<dt>
										<c:forEach var="image" items="${list3 }">
											<c:if test="${image.product.id == product.id}">
												<img src="http://localhost:8080/image/${image.url }" style="width:216px;height:216px">
											</c:if>
										</c:forEach>
									</dt>
									<dd>${product.name }</dd>
									<dd>￥${product.price }</dd>
								</dl>
							</a>
						</c:forEach>
					</div>
				</div>
			</div>
		</div>
	</div>
	<!--返回顶部-->
	<!----------------------------- go_top -------------------------->
	<%@ include file="go_top.jsp"%>
	<div class="msk"></div>
	<!----------------------------- footer -------------------------->
	<%@ include file="footer.jsp"%>
	<script src="js/jquery-1.12.4.min.js" type="text/javascript"
		charset="utf-8"></script>
	<script src="js/jquery.SuperSlide.2.1.1.js" type="text/javascript"
		charset="utf-8"></script>
	<script src="js/public.js" type="text/javascript" charset="utf-8"></script>
	<script src="js/nav.js" type="text/javascript" charset="utf-8"></script>
	<script src="js/pro.js" type="text/javascript" charset="utf-8"></script>
	<script src="js/cart.js" type="text/javascript" charset="utf-8"></script>
	<script type="text/javascript">
		jQuery(".bottom").slide({
			titCell : ".hd ul",
			mainCell : ".bd .likeList",
			autoPage : true,
			autoPlay : false,
			effect : "leftLoop",
			autoPlay : true,
			vis : 1
		});
	</script>
</body>

</html>