<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8" />
<title>用户管理</title>
<link rel="stylesheet" type="text/css" href="css/common.css" />
<link rel="stylesheet" type="text/css" href="css/main.css" />
<script type="text/javascript" charset=UTF-8>
//删除一个用户
function deleteOneUser(mess, url) {
	if (confirm(mess)) {
		location.href = url;
	}
}
//删除多个用户
function deleteMoreUsers(mess,id) {
	if(isUserSelected()){
		if (confirm(mess)) {
			var form = document.getElementById(id);
			form.submit();
		}
	}else{
		alert('还未选择要删除的用户！');
	}
}
//同时选择多个用户
function selectMoreUsers(e){
	var a = document.getElementsByName('items');
	for (var i = 0; i < a.length; i++) {
		a[i].checked = e.checked;
	}
}

//判断是否有用户被选中
function isUserSelected(){
	var a = document.getElementsByName('items');
	for (var i = 0; i < a.length; i++) {
		if(a[i].checked){
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
						class="crumb-step">&gt;</span><span class="crumb-name">用户管理</span>
				</div>
			</div>
			<div class="search-wrap">
				<div class="search-content">
					<form action="AdminGetUser" method="get">
						<table class="search-tab">
							<tr>
								<th width="10"></th>
								<td><input class="common-text" name="keyword"
									value="${param.keyword}" id="" type="text"
									onkeydown="if(event.keyCode==32){return false;}"></td>
								<td><input class="btn btn-primary btn2" name="sub"
									value="搜索用户" type="submit"></td>
							</tr>
						</table>
					</form>
				</div>
			</div>
			<div class="result-wrap">
				<form name="myform" id="myform" method="post"
					action="AdminUserDelete">
					<div class="result-title">
						<div class="result-list">
							<a href="admin_user_add.jsp"><i class="icon-font"> </i>新增用户</a> <a
								id="batchDel"
								href="javascript:deleteMoreUsers('你确定要删除这些用户吗？','myform')">
								<i class="icon-font"></i>批量删除
							</a>
						</div>
					</div>
					<div class="result-content">
						<table class="result-tab" style="width: 100%">
							<tr>
								<th class="tc" width="5%"><input class="allChoose" name=""
									onclick="selectMoreUsers(this)" type="checkbox"></th>
								<th>id</th>
								<th>用户名</th>
								<th>电话号码</th>
								<th>电子邮箱</th>
								<th>地址</th>
								<th>用户类别</th>
								<th>操作</th>
							</tr>
							<c:forEach var="user" items="${userList}">
								<tr>
									<td class="tc"><input name="items" value="${user.id}"
										type="checkbox"></td>
									<td>${user.id}</td>
									<td>${user.name}</td>
									<td>${user.phone}</td>
									<td>${user.email}</td>
									<td>${user.address}</td>
									<td>${user.status==1?"管理员":"普通用户"}</td>
									<td><a class="link-update"
										href="AdminUserUpdate?keyword=${param.keyword }&page=${pageIndex}&id=${user.id}">修改</a>
										<a class="link-del"
										href="javascript:deleteOneUser('你确定要删除用户[${user.name }]吗？','AdminUserDelete?keyword=${param.keyword }&page=${pageIndex}&id=${user.id}')">删除</a>
									</td>
								</tr>
							</c:forEach>
						</table>
						<div class="list-page">
							共${userTotalNumber}条记录，当前第${pageIndex}/${pageTotalNumber }页 <a
								href="AdminGetUser?keyword=${param.keyword }&page=1">首页</a> <a
								href="AdminGetUser?keyword=${param.keyword }&page=${pageIndex-1<1?1:pageIndex-1}">上一页</a>
							<a
								href="AdminGetUser?keyword=${param.keyword }&page=${pageIndex+1>pageTotalNumber?pageTotalNumber:pageIndex+1}">下一页</a>
							<a
								href="AdminGetUser?keyword=${param.keyword }&page=${pageTotalNumber}">尾页</a>
						</div>
					</div>
				</form>
			</div>
		</div>
		<!--/main-->
	</div>
</body>
</html>