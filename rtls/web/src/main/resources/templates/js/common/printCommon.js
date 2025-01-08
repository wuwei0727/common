//请求错误时
function resError(jqXHR){
    tips('系统繁忙');
}
//调用打印
function startPrint(that){
	that.style.display = 'none';
	window.print();
	that.style.display = 'block';
}
var tipsTimer = null;
//创建提示
function tips(str,fn,time){
	var tips = $('.tips');
	if(tipsTimer){
		clearTimeout(tipsTimer);
	}
	if(!tips.length){
		tips = $('<div class="tips"></div>');
		$('body').append(tips);
	}
	tips.html(str || '');
	tipsTimer = setTimeout(function(){
		tips.remove();
        clearTimeout(tipsTimer);
        tipsTimer = null;
        fn && fn();
    },(time || 3000))
}
//获取url的所有参
function getAllUrl() {
    var url = location.search; //获取url中"?"符后的字串  
    var theRequest = new Object();
    if (url.indexOf("?") != -1) {
        var str = url.substr(1);
        strs = str.split("&");
        for(var i = 0; i < strs.length; i ++) {
            theRequest[strs[i].split("=")[0]] = (strs[i].split("=")[1]);  
        }  
    }
    return theRequest;
}
//转换空值
function convertNull(val){
    if(val == undefined){
        return val || '';
    }
    return val || 0;
}
//显示搜索条件
function searchInfo(){
    if(!where){
        where = {};
    }
    var searchItem = JSON.parse(sessionStorage.getItem('prints') || '[]');
    var html = '';
    var target = null;
    var month = null;
    for(var i = 0;i < searchItem.length;i++){
        target = searchItem[i];
        if(target.eName == 'month'){
            month = target.eVal;
        }
        where[target.eName] = target.eVal;
        html += target.eTitle + '：' + target.eTxt + '，';
    }
    html = html.slice(0,-1);
    $('#searchTxt').html(html);
    month = month || '';
    return {
        y:month.slice(0,4),
        m:month.slice(4),
    }
}

//生成表格
function createTd(day){
    var temp = ['日','一','二','三','四','五','六'][day];
    if(day == 6 || day == 0){
        return '<td class="tdF3">' + temp + '</td>';
    }else{
        return '<td>' + temp + '</td>';
    }
}