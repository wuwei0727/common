var baseHtml = '';
var testUrl = ""
var curType = 5;//当前类型
var batch;//批次
$(function(){
	getcheckres();
})
//获取实时检卡结果
function getcheckres(){
	$.ajax({
		url:testUrl + '/bstagcheck/getcheckres',
		type:'post',
		data:{
			tagcheckid:'',
			type:curType
		},
		success:function(res){
			var tab = $('#tab');
			if(res.code !== 200){
				tips(res.message || '获取实时检卡结果失败，请重试');
				return;
			}
			var allName = getTheadName(tab.find('thead'));
			var len = allName.length - 1;
			var html = '';
        	var nameTar = null;
        	var listTar = null;
        	var list = res.data || [];
        	var child = null;
        	for(var i = 0;i < list.length;i++){
        		listTar = list[i].list;
				html += '<tr><td class="padLeft40" colspan="' + len + '">批次号：' + list[i].tagcheckid + '</td>';
				html += '<td><span class="tabOper" onclick="download(\'' + list[i].tagcheckid + '\')">下载测试结果</span><span class="tabOper deleOpa" onclick="deleteItem(\'' + list[i].tagcheckid + '\')"">删除</span></td></tr>';
        		for(var j = 0;j < listTar.length;j++){
					html += '<tr>';
					child = listTar[j];
	        		for(var z = 0;z < allName.length;z++){
	        			nameTar = allName[z];
	        			if(nameTar == 'lineNum'){
	                        html += '<td class="padLeft40">' + (1 + i) + '</td>';
	        			}else if(nameTar == 'period'){
	    					html += '<td>' + child[nameTar] + '毫秒</td>';
	        			}else if(nameTar == 'errorrate'){
	    					html += '<td>' + chagnecRate(child[nameTar],7) + '%</td>';
	        			}else if(nameTar == 'lackpercent' || nameTar == 'lostrate'){
	    					html += '<td>' + chagnecRate(child[nameTar]) + '%</td>';
	        			}else if(nameTar == 'opa'){
	    					html += '<td></td>';
	        			}else if(nameTar == 'start' || nameTar == 'end'){
	    					html += '<td>' + changeTime(child[nameTar]) + '</td>';
	        			}else{
	    					html += '<td>' + child[nameTar] + '</td>';
	        			}
	        		}
	        		html += '</tr>';
        		}
        		
        	}
        	tab.find('tbody').html(html);
		},
		error:function(err){
            tips('获取实时检卡结果失败，请重试');
		}
	})
}
//下载
function download(id){
	if(!id){
		tips('下载失败，请重试');
		return;
	}
	var url = testUrl + '/bstagcheck/outexcel';
    var form = $("<form></form>").attr("action", url).attr("method", "post");
    form.append($("<input></input>").attr("type", "hidden").attr("name", "tagcheckid").attr("value", id));
    form.append($("<input></input>").attr("type", "hidden").attr("name", "type").attr("value", curType));
    form.appendTo('body').submit().remove();
}
//提示
function tips(str,time){
	$('body').append('<div class="showTips">' + (str || '系统繁忙！') + '</div>');
    setTimeout(function(){
        $('.showTips').remove();
    },(time || 3) * 1000)
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
//转换时间
function changeTime(time){
	if(!time){
		return '';
	}
	var setTime = new Date(time);
	var year = setTime.getFullYear();
	var mon = zeroPadding(setTime.getMonth() + 1);
	var day = zeroPadding(setTime.getDate());
	var hours = zeroPadding(setTime.getHours());
	var min = zeroPadding(setTime.getMinutes());
	var sec = zeroPadding(setTime.getSeconds());
	return year + '/' + mon + '/' + day + ' ' +  hours + ':' + min + ':' + sec;
}
//补零
function zeroPadding(str){
	return str > 9 ? str : '0' + str;
}
//获取url的所有参
function getAllUrl() {
    var url = location.search; //获取url中"?"符后的字串  
    var theRequest = new Object();
    if (url.indexOf("?") != -1) {
        var str = url.substr(1);
        strs = str.split("&");
        for(var i = 0; i < strs.length; i ++) {
            theRequest[strs[i].split("=")[0]] = window.unescape(strs[i].split("=")[1]);  
        }  
    }
    return theRequest;
}
//删除
function deleteItem(id){
	if(confirm('是否确定删除批次号为：' + id + ' 的批次?')){
		$.ajax({
			url:testUrl + '/bstagcheck/deletetagcheckid',
			data:{
				tagcheckid:id,
			},
			success:function(res){
				tips(res.message);
				if(res.code === 200){
					getcheckres();
				}
			},
			error:function(err){
	            tips('数据导出失败，请重试');
			}
		})
	}
}
//转换率
function chagnecRate(num,len){
	num = num || 0;
	return (num * 100).toFixed(len || 2);
}