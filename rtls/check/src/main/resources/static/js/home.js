var url = '';
$(function(){
	//设置高度
	setIframeHei();
})
//设置iframe的高度
function setIframeHei(){
	var hei = Math.floor($(window).height() - 60);
	var iframeBox = document.getElementById('iframeBox');
	iframeBox.style.height = hei + 'px';
	//设置宽度
	iframeBox.style.width = iframeBox.offsetWidth + 'px';
	setUrl('./testIndex.html?type=0',1);
}
//设置iframe地址
//flag 不需补全
function setUrl(url,flag){
	var Url = '';
	if(flag){
		Url = url;
	}else{
		Url = './' + url + '.html';
	}
	var iframeBox = document.getElementById('iframeBox');
	iframeBox.src = Url;
}
//子级调用改变菜单
function childMenu(that){
	var curA = document.getElementsByClassName('curA')[0];
	if(curA){
		if(curA != that){
			curA.className = '';
			that.className = 'curA';
		}
	}else{
		that.className = 'curA';
	}
	var path = that.getAttribute('data-path');
	setUrl(path,true);

}
//提示
function tips(str,time){
	$('body').append('<div class="showTips">' + (str || '系统繁忙！') + '</div>');
    setTimeout(function(){
        $('.showTips').remove();
    },(time || 3) * 1000)
}
//展开、收起子菜单
function unfold(that){
	var curMenu = document.getElementById('curMenu');
	var parent = that.parentNode;
	var _that = $(that);
	if(curMenu){
		if(curMenu === parent){
			//相同的dom
			_that.next().slideUp(300);
			parent.id = '';
			return;
		}else{
			//不同的dom
			$(curMenu).find('.memuList').slideUp(300);
			curMenu.id = '';
		}
	}
	_that.next().slideDown(300);
	parent.id = 'curMenu';
}
