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
var flag3 = false;//验证码标志位
var name = '';

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
						name = encodeURI($(obj).val());
						flag1 = true;
						msg.html('').removeClass('error');
					}else{
						msg.html('用户不存在');
						msg.addClass('error');
					}
				});
			}
			break;
		case 'password':
			if(obj.value == ""){
				msg.html('密码不能为空');
				msg.addClass('error');			
			}else{
				var url = "CheckPassword?name="+name+"&password="+encodeURI($(obj).val())+"&"+Math.random();
				$.get(url,function(data){
					if(data=="true"){
						flag2 = true;
						msg.html('').removeClass('error');
					}else{
						msg.html('用户密码错误');
						msg.addClass('error');
					}
				});
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
						flag3 = true;
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
	if(flag1&&flag2&&flag3){
		return true;
	}
	return false;
}