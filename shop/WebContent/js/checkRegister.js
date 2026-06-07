function changeCode(img){
	img.src = "GetVerifyCode?"+Math.random();
}
function focusItem(obj){
	var msg = $(obj).next();
	if($(obj).attr('name') == 'verifyCode'){
		msg.next().html('').removeClass('error');
	}else{
		msg.html('').removeClass('error');
	}
}

var flag1 = false;//用户名标志位
var flag2 = false;//密码标志位
var flag3 = false;//确认密码标志位
var flag4 = false;//验证码标志位

function checkItem(obj){
	var msg = $(obj).next();
	switch($(obj).attr('name')){
		case 'name':
			if(obj.value == ""){
				msg.html('用户名不能为空');
				msg.addClass('error');			
			}else{
				var url = "CheckName?name="+encodeURI($(obj).val())+"&"+Math.random();
				$.get(url,function(data){
					if(data=="true"){
						msg.html('用户已存在');
						msg.addClass('error');
					}else{
						flag1 = true;
						msg.html('').removeClass('error');
					}
				});
			}
			break;
		case 'password':
			if(obj.value == ""){
				msg.html('密码不能为空');
				msg.addClass('error');			
			}else{
				flag2 = true;
				msg.html('').removeClass('error');
			}
			break;
		case 'repassword':
			if(obj.value == ""){
				msg.html('确认密码不能为空');
				msg.addClass('error');			
			}else if(obj.value != $("input[name='password']").val()){
				msg.html('两次输入的密码不一致');
				msg.addClass('error');			
			}else{
				flag3 = true;
				msg.html('').removeClass('error');
			}
			break;
		case 'verifyCode':
			if(obj.value == ""){
				msg.next().html('验证码不能为空');
				msg.next().addClass('error');			
			}else{
				var url = "CheckVerifyCode?code="+encodeURI($(obj).val())+"&"+Math.random();
				$.get(url,function(data){
					if(data == 'false'){
						msg.next().html('验证码错误');
						msg.next().addClass('error');	
					}else{
						flag4 = true;
						msg.next().html('').removeClass('error');
					}
				});
			}
	}
}
function checkInfo(obj){
	var e = obj.getElementsByTagName('input');
	//有onblur属性的input输入框才是需要验证的
	for(var i = 0; i < e.length; i++){
		if(e[i].getAttribute('onblur')){
			checkItem(e[i]);//检查表单各元素
		}
	}
	if(flag1&&flag2&&flag3&&flag4){
		return true;
	}
	return false;
}