<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<div class="wrapper clearfix">
	<div class="clearfix" id="top">
		<h1 class="fl">
			<a href="GetCategory"><img src="img/logo.png"/></a>
		</h1>
		<div class="fr clearfix" id="top1">
			<p class="fl">
				<c:if test="${empty sessionScope.user}">
					<a href="login.jsp" id="login">登录</a>
					<a href="register.jsp" id="reg">注册</a>
				</c:if>
				<c:if test="${not empty sessionScope.user}">
					<a>你好 ${sessionScope.user.name}</a>
				</c:if>
			</p>
			<form action="GetProduct" method="post" class="fl">
				<input type="text" placeholder="热门搜索：干花花瓶" value="${keyword }" name="keyword" /> 
				<input type="submit" value="" />
			</form>
			<div class="btn fl clearfix">
				<a href="GetUser"><img src="img/grzx.png" /></a> <a href="#"
					class="er1"><img src="img/ewm.png" /></a> <a href="GetCart"><img
					src="img/gwc.png" /></a>
				<p>
					<a href="#"><img src="img/smewm.png" /></a>
				</p>
			</div>
		</div>
	</div>
	<ul class="clearfix" id="bott">
		<li><a href="GetCategory">首页</a></li>
		<c:forEach var="category" items="${list}">
			<li><c:if test="${category.parentId == 0}">
					<a href="GetProduct?id=${category.id }">${category.name}</a>
				</c:if>
				<div class="sList2">
					<div class="clearfix">
						<c:forEach var="category1" items="${list}">
							<c:if test="${category1.parentId == category.id }">
								<a href="GetProduct?id=${category1.id }">${category1.name}</a>
							</c:if>
						</c:forEach>
					</div>
				</div></li>
		</c:forEach>
	</ul>
</div>
