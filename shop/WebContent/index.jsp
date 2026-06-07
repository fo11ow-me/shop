<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html>
<head lang="en">
<meta charset="utf-8" />
<title>首页</title>
<link rel="stylesheet" type="text/css" href="css/public.css" />
<link rel="stylesheet" type="text/css" href="css/index.css" />
</head>
<body>
	<div class="head">
		<!------------------------------head------------------------------>
		<%@ include file="head.jsp"%>
	</div>
	<!-------------------------banner--------------------------->
	<div class="block_home_slider">
		<div id="home_slider" class="flexslider">
			<ul class="slides">
				<li>
					<div class="slide">
						<img src="img/banner2.jpg" />
					</div>
				</li>
				<li>
					<div class="slide">
						<img src="img/banner1.jpg" />
					</div>
				</li>
			</ul>
		</div>
	</div>
	<!------------------------------people------------------------------>
	<div class="people">
		<div class="wrapper">
			<h2>
				<img src="img/ih1.jpg" />
			</h2>
			<c:forEach begin="0" end="${fn:length(list1)}" step="4"
				varStatus="status">
				<div class="pList clearfix tran" style="justify-content:center;" >
					<c:forEach begin="${status.index }" end="${status.index + 3}"
						var="product" items="${list1 }" varStatus="status1">
							<a href="GetProductDetail?id=${product.id}"
								style="width: 268px; height:322px;margin:25px;">
								<dl>
									<dt>
										<span class="abl"></span>
										<c:forEach var="image" items="${list2 }">
											<c:if test="${image.product.id == product.id }">
												<img src="http://localhost:8080/image/${image.url }"
													style="width: 268px; height: 268px">
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
	<!----------------------------- go_top -------------------------->
	<%@ include file="go_top.jsp"%>
	<!----------------------------- footer -------------------------->
	<%@ include file="footer.jsp"%>
	<script src="js/jquery-1.12.4.min.js" type="text/javascript"
		charset="utf-8"></script>
	<script src="js/public.js" type="text/javascript" charset="utf-8"></script>
	<script src="js/nav.js" type="text/javascript" charset="utf-8"></script>
	<script src="js/jquery.flexslider-min.js" type="text/javascript"
		charset="utf-8"></script>
	<script type="text/javascript">
		$(function() {
			$('#home_slider').flexslider({
				animation : 'slide',
				controlNav : true,
				directionNav : true,
				animationLoop : true,
				slideshow : true,
				slideshowSpeed : 2000,
				useCSS : false
			});
		});
	</script>
</body>
</html>