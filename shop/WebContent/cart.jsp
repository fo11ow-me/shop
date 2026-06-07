<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head lang="en">
<meta charset="utf-8" />
<title>cart</title>
<link rel="stylesheet" type="text/css" href="css/public.css" />
<link rel="stylesheet" type="text/css" href="css/proList.css" />
<script src="js/jquery-1.12.4.min.js" type="text/javascript"
	charset="utf-8"></script>

</head>
<body>
	<!--------------------------------------cart--------------------->
	<div class="head ding">
		<!------------------------------head------------------------------>
		<%@ include file="head.jsp"%>
	</div>
	<div class="cart mt">
		<!-----------------logo------------------->
		<!-----------------site------------------->
		<div class="site">
			<p class=" wrapper clearfix">
				<span class="fl">购物车</span><img class="top"
					src="img/temp/cartTop01.png"><a href="GetCategory" class="fr">继续购物&gt;</a>
			</p>
		</div>
		<!-----------------table------------------->
		<div class="table wrapper">
			<div class="tr">
				<div>商品</div>
				<div>单价</div>
				<div>数量</div>
				<div>小计</div>
				<div>操作</div>
			</div>
			<c:forEach var="cart" items="${list1 }">
				<div class="th">
					<div class="pro clearfix">
						<label class="fl"> <input name="cartIds" type="checkbox"
							value="${cart.id}" /> <span></span></label><a class="fl"
							href="GetProductDetail?id=${cart.product.id }">
							<dl class="clearfix">
								<dt class="fl">
									<c:forEach var="image" items="${list2 }">
										<c:if test="${image.product.id == cart.product.id }">
											<img src="http://localhost:8080/image/${image.url }"
												style="width: 120px; height: 120px">
										</c:if>
									</c:forEach>
								</dt>
								<dd class="fl">
									<p>${cart.product.name }</p>
									<p>商品信息:</p>
									<p>${cart.product.category.name }</p>
								</dd>
							</dl>
						</a>
					</div>
					<div class="price">￥${cart.product.price }</div>
					<div class="number">
						<p class="num clearfix">
							<img class="fl sub" src="img/temp/sub.jpg"> <span
								cartId="${cart.id }" stock="${cart.product.stock}" class="fl">${cart.amount }</span><img
								class="fl add" src="img/temp/add.jpg">
						</p>
					</div>
					<div class="price sAll">￥${cart.product.price*cart.amount}</div>
					<div class="price">
						<a href="CartDelete?id=${cart.id }">删除</a>
					</div>
				</div>
			</c:forEach>
			<div class="tr clearfix">
				<label class="fl"><input class="checkAll" type="checkbox" /><span></span></label>
				<p class="fl">
					<a href="javascript:cartDelete()">批量删除</a>
				</p>
				<p class="fr">
					<span>共<small id="sl">0</small>件商品
					</span><span>合计:&nbsp;<small id="all">￥0.00</small></span><a
						href="javascript:orderPay()" class="count">结算</a>
				</p>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		function orderPay() {
			var str = "";
			$("input[name='cartIds']:checked").each(function(index, item) {
				if ($("input[name='cartIds']:checked").length - 1 == index) {
					str += $(this).val();
				} else {
					str += $(this).val() + ",";
				}
			});
			if(str!=""){
				location.href="OrderPay?cartIds="+str;
			}
		}
		
		function cartDelete() {
			var str = "";
			$("input[name='cartIds']:checked").each(function(index, item) {
				if ($("input[name='cartIds']:checked").length - 1 == index) {
					str += $(this).val();
				} else {
					str += $(this).val() + ",";
				}
			});
			if(str!=""){
				var data = {'cartIds':str};
				$.post('CartDelete', data,function(result){});
			}
		}
	</script>
	<div class="mask"></div>
	<!----------------------------- go_top -------------------------->
	<%@ include file="go_top.jsp"%>
	<!----------------------------- footer -------------------------->
	<%@ include file="footer.jsp"%>
	<!-------------------------------mask---------------------------->
	<div class="mask"></div>
	<script src="js/public.js" type="text/javascript" charset="utf-8"></script>
	<script src="js/pro.js" type="text/javascript" charset="utf-8"></script>
	<script src="js/cart.js" type="text/javascript" charset="utf-8"></script>
</body>
</html>