	var baseHtml = '';
var testUrl = ""
var curType = 5;//当前类型
var batch;//批次
var requestTime = 500;
var startFlag = false;
var loadBox = null;
$(function(){
	loadBox = $('#loadBox');
})
//开始
function start(){
	if(startFlag){
		tips('请等待上次结果结束再进行开启');
		return;
	}
	var sendData = getData();
	if(!sendData){
		return;
	}
	sendData.type = curType;
	$.ajax({
		url:testUrl + '/bstagcheck/startcheck',
		data:sendData,
		type:'post',
		success:function(res){
			if(res.code !== 200){
				tips(res.message || '开始检卡失败，请重试');
				return;
			}
			batch = res.data;
			if(!batch){
				tips(res.message);
				return;
			}
			tips('开始检卡成功');
			loadBox.show();
			startFlag = true;
			sendRequest();
		},
		error:function(err){
            tips('开始检卡失败，请重试');
		}
	})
}
//请求
function sendRequest(){
	getcheckres();
}
//结束
function end(){
	if(!startFlag){
		tips('请先开始测试');
		return;
	}
	$.ajax({
		url:testUrl + '/bstagcheck/endcheck',
		data:{
			type:curType,
			tagcheckid:batch
		},
		success:function(res){
            tips(res.message);
            if(res.code === 200){
            	startFlag = false;
				loadBox.hide();
            }
		},
		error:function(err){
            tips('结束检卡失败，请重试');
		}
	})
}
//获取实时检卡结果
function getcheckres(){
	$.ajax({
		url:testUrl + '/bstagcheck/getcheckres',
		data:{
			tagcheckid:batch,
			type:curType
		},
		success:function(res){
			console.log(res);
			var tab = $('#tab');
			if(res.code !== 200){
				tips(res.message || '获取实时检卡结果失败，请重试');
				return;
			}
			var allName = getTheadName(tab.find('thead'));
			var html = '';
        	var nameTar = null;
        	var listTar = null;
        	var list = (res.data[0] || {}).list || [];
        	for(var i = 0;i < list.length;i++){
        		listTar = list[i];
        		html += '<tr>';
        		for(var j = 0;j < allName.length;j++){
        			nameTar = allName[j];
        			if(nameTar == 'lineNum'){
                        html += '<td class="padLeft40">' + (1 + i) + '</td>';
        			}else if(nameTar == 'state'){
        				html += '<td>' + (listTar[nameTar] ? '测试中' : '结束') + '</td>';
        			}else if(nameTar == 'errorrate'){
    					html += '<td>' + chagnecRate(child[nameTar],7) + '%</td>';
        			}else if(nameTar == 'lackpercent' || nameTar == 'lostrate'){
    					html += '<td>' + chagnecRate(child[nameTar]) + '%</td>';
        			}else if(nameTar == 'period'){
    					html += '<td>' + listTar[nameTar] + '毫秒</td>';
        			}else if(nameTar == 'start' || nameTar == 'end'){
    					html += '<td>' + changeTime(listTar[nameTar]) + '</td>';
        			}else{
    					html += '<td>' + listTar[nameTar] + '</td>';
        			}
        		}
        		html += '</tr>';
        	}
        	tab.find('tbody').html(html);
        	if((list[0] || {}).state === 0){
        		//结束请求（手动结束也进行请求，直到状态为0）
            	startFlag = false;
				loadBox.hide();
        		return;
        	}
        	setTimeout(function(){
        		sendRequest();
        	},requestTime)
		},
		error:function(err){
            tips('获取实时检卡结果失败，请重试');
		}
	})
}
//获取并校验数据
function getData(){
	var where = {};
	var bslist = $('[name="bslist"]').val();
	if(!bslist){
		tips('请输入基站标号');
		return;
	}
	var count = $('[name="count"]').val();
	if(!/^\d+$/.test(count)){
		tips('发送包数填写错误，请输入0和正整数');
		return;
	}
	var interval = $('[name="interval"]').val();
	if(!/^[1-9]\d*$/.test(interval)){
		tips('测试间隔填写错误，请输入正整数');
		return;
	}
	where.bslist = bslist;
	where.count = count;
	where.interval = interval;
	return where;
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
//转换率
function chagnecRate(num,len){
	num = num || 0;
	return (num * 100).toFixed(len || 2);
}