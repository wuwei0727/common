var url = '../';
//计算page
function calcPage(){
    if(!window.pageSize){
        return;
    }
    return (pageSize = 20);//固定
    var hei = $(window).height();
    var temp = hei - 46 - 51 - 16 * 2 - 50 - 46 - $('.search').outerHeight();
    temp = temp / 60;
    pageSize = temp < 5 ? '5' : Math.floor(temp) - 1;//给批量操作留空间
}
//显示弹窗
function showPop(target){
	if(!target){
		return;
	}
	$('#mask').show();
	$(target).show();
}
//隐藏弹窗
function hidePop(target,mask){
	if(!target){
		return;
	}
	$('#' + (mask || 'mask')).hide();
	$(target).hide();
}
//获取url的参数值
function getUrlStr(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = window.location.search.slice(1).match(reg);
    if (r != null){
    	return window.unescape(r[2]);
	};
	return null;
}
//removeLoad
function removeLoad(){
    $('#mask').remove();
    $('#load').remove();
}
var tipsTimer = null;
//创建提示
function tips(str,fn,time){
	var tips = $('#tip');
	if(tipsTimer){
		clearTimeout(tipsTimer);
	}else{
		tips.css('top','50px');
	}
	tips.html(str || '');
	tipsTimer = setTimeout(function(){
        tips.css('top','-50px');
        clearTimeout(tipsTimer);
        tipsTimer = null;
        fn && fn();
    },(time || 3000))
}
//提示（直接显示）
function showTips(str,fn,time){
    $('body').append('<div class="showTips">' + str + '</div>');
    setTimeout(function(){
        $('.showTips').remove();
        fn && fn();
    },(time || 3000))
}
//获取thead的name
function getTheadName(ele){
	var res = [];
	var allName = ele.find('td[name]');
	for(var i = 0;i < allName.length;i++){
		res.push(allName[i].getAttribute('name'));
	}
	return res;
}
// load
function loading(){
    $('body').append('<div id="loadMask"></div><div id="load"><img src="../image/common/load.gif" /></div>');
}
//removeLoad
function removeLoad(){
    $('#loadMask').remove();
    $('#load').remove();
}
//无数据时table
//noP  不返回p的txt
function noData(len,noP,parDom){
    if(len == 0){
        len = getTheadName((parDom || $('#tab')).find('thead')).length || 1;
    }
    var res = '<tr class="noData"><td colspan="' + len + '"><img src="../image/common/noData.png">';
    if(!noP){
        res += '<p>空空如也~ </p>';
    }
    res += '</td></tr>';
    return res;
}
//查询失败
function resErrTab(len,parDom,txt){
    if(len == 0){
        len = getTheadName((parDom || $('#tab')).find('thead')).length || 1;
    }
    var res = '<tr class="noData"><td colspan="' + len + '"><img src="../image/common/error.png">';
    res += '<p>' + (txt || '查询失败~ ') + '</p>';
    res += '</td></tr>';
    return res;
}
//转换空值
function convertNull(eName,val){
    if(eName == 'heat'){
        return val || 0;
    }
    return val || '';
}
//请求错误时
function resError(jqXHR){
    tips('系统繁忙');
}