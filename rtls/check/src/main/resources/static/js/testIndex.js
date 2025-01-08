var baseHtml = '';
var url = "";
var testUrl = ""
var curType;//当前类型
var batch;//批次
var requestTime = 500;
var startFlag = false;
var tab1Name = null;
var tab1Tbody = null;
var tab2Name = null;
var tab2Tbody = null;
$(function(){
	curType = getAllUrl().type;
	var txt;
	switch(curType){
		case '0':
			txt = '并发量测试-单分站测试';
			break;
		case '1':
			txt = '并发量测试-多分站测试';
			break;
		case '2':
			txt = '精度测试';
			break;
		case '3':
			txt = '巡检周期测试';
			break;
	}
	$('#headingTxt').html(txt);
	if(curType != 0){
		$('#addIcon').removeClass('hide');
	}
	var tab = $('#tab');
	tab1Name = getTheadName(tab.find('thead'));
	tab1Tbody = tab.find('tbody');
	var tab2 = $('#tab2');
	tab2Name = getTheadName(tab2.find('thead'));
	tab2Tbody = tab2.find('tbody');
	tab = tab2 = null;
	loadMap();
})
//添加select选框
function addSele(noRemove){
	var html = '<span class="seleBox"><select class="sele" name="bsItem">';
	html += baseHtml;
	if(noRemove){
		html += '</select></span>';
	}else{
		html += '</select><span class="remove" onclick="removeSele(this)"></span></span>';
	}
	$('#base').append(html);
}
//移除select选框
function removeSele(that){
	var _that = $(that);
	_that.parent().remove();
}
//加载地图
function loadMap(){
	$.ajax({
		url: url + '/map/getMap2dSel',
		data:{
			pageSize:-1
		},
        success: function(res) {
            if(res.code != 200){
            	tips(res.message);
            	return;
            }
            var list = res.data;
            var html = '';
            var first = list[0];
            for(var i = 0;i < list.length;i++){
            	html += '<option value="' + list[i].id + '">' + list[i].name + '</option>';
            }
            $('#map').html(html);
            if(first){
            	loadBase(first.id,null);
            }
        },
        error:function(jqXHR){
            tips('获取地图数据出错');
        }
	})
}
//加载分站
function loadBase(mapId,clear){
	$.ajax({
		url: url + '/map/getSubId/' + mapId,
		data:{
			pageSize:-1
		},
        success: function(res) {
            if(res.code != 200){
            	tips(res.message);
            	return;
            }
            var list = res.data;
            var typeName = null;
            var child = null;
            var html = '';
            for(var i = 0;i < list.length;i++){
            	typeName = list[i].typeName;
            	child = list[i].substations;
            	for(var j = 0;j < child.length;j++){
	            	html += '<option value="' + child[j].num + '">' + typeName + '-->' + child[j].num + '</option>';
	            }
            }
            baseHtml = html;
            if(clear){
            	$('#base').html('');
            }
			if(curType == 0){
				addSele('noRemove');
			}
        },
        error:function(jqXHR){
            tips('获取分站数据出错');
        }
	})
}
//切换地图
function switchMap(that){
	loadBase(that.value,true);
}
//显示上传的信息
function upload(that){
	var info = '';
	var file = that.files[0];
	if(file){
		info += '文件名：' + file.name + '&nbsp;&nbsp;&nbsp;&nbsp;大小：' + (file.size / 1024) + 'kb';
	}
	$('#fileInfo').html(info);
}
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
	sendData.append('type',curType);
	$.ajax({
		url:testUrl + '/bstagcheck/startcheck',
		data:sendData,
		type:'post',
		beforeSend:function(){
			$('#sMask').show();
			$('#startPop').show();
		},
        processData: false,// jQuery不要去处理发送的数据
        contentType: false,// jQuery不要去设置Content-Type请求头
		success:function(res){
			$('#sMask').hide();
			$('#startPop').hide();
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
	getrecentlocation();
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
			if(res.code !== 200){
				tips(res.message || '获取实时检卡结果失败，请重试');
				return;
			}
			var html = '';
        	var nameTar = null;
        	var listTar = null;
        	var list = (res.data[0] || {}).list || [];
        	for(var i = 0;i < list.length;i++){
        		listTar = list[i];
        		html += '<tr>';
        		for(var j = 0;j < tab1Name.length;j++){
        			nameTar = tab1Name[j];
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
        			}else if(nameTar == 'opa'){
        				if(listTar.state){
    						html += '<td></td>';
        				}else{
    						html += '<td><span class="tabOper" onclick="missedRead(\'' + (listTar.lackedetail || '') + '\')">显示漏读标签</span></td>';
        				}
        			}else{
    					html += '<td>' + listTar[nameTar] + '</td>';
        			}
        		}
        		html += '</tr>';
        	}
        	tab1Tbody.html(html);
        	if((list[0] || {}).state === 0){
        		startFlag = false;
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
//获取最新的定位结果
function getrecentlocation(){
	$.ajax({
		url:testUrl + '/bstagcheck/getrecentlocation',
		data:{
			tagcheckid:batch
		},
		success:function(res){
			if(res.code !== 200){
				tips(res.message || '获取最新的定位结果失败，请重试');
				return;
			}
			var html = '';
        	var nameTar = null;
        	var listTar = null;
        	var list = res.data;
        	for(var i = 0;i < list.length;i++){
        		listTar = list[i];
        		html += '<tr>';
        		for(var j = 0;j < tab2Name.length;j++){
        			nameTar = tab2Name[j];
        			if(nameTar == 'lineNum'){
                        html += '<td class="padLeft40">' + (1 + i) + '</td>';
        			}else if(nameTar == 'position'){
        				html += '<td>' + (listTar.x + ',' + listTar.y + ',' + listTar.z) + '</td>';
        			}else{
    					html += '<td>' + listTar[nameTar] + '</td>';
        			}
        		}
        		html += '</tr>';
        	}
        	tab2Tbody.html(html);
		},
		error:function(err){
            tips('获取最新的定位结果失败，请重试');
		}
	})
}
//获取并校验数据
function getData(){
	var formData = new FormData();
	var file = document.getElementById('file').files[0];
	if(!file){
		tips('请上传标签文件');
		return;
	}
    formData.append('file',file);
	var bsId = [];
	var allBs = $('[name="bsItem"]');
	for(var i = 0;i < allBs.length;i++){
		if(bsId.indexOf(allBs[i].value) != -1){
			continue;
		}
		bsId.push(allBs[i].value);
	}
	if(!bsId.length){
		tips('请选择至少一个分站');
		return;
	}
    formData.append('bslist',bsId.join());
	var finishType = $('[name="finishType"]');
	formData.append('finishType',finishType.is(':checked') ? 1 : -1);

	var checkTime = $('[name="checkTime"]');
	if(checkTime.is(':checked')){
		var setTime = $.trim($('[name="setTime"]').val());
		if(!/^[1-9]\d*$/g.test(setTime)){
			tips('勾选了设定时间，请输入正整数');
			return;
		}
		formData.append('checkTime',setTime);
	}else{
		formData.append('checkTime',-1);
	}
	return formData;
}
var tipsTimer = null;
//提示
function tips(str,time){
	clearTimeout(tipsTimer);
	$('body').append('<div class="showTips">' + (str || '系统繁忙！') + '</div>');
    tipsTimer = setTimeout(function(){
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
//显示漏读标签
function missedRead(txt){
	if(!txt){
		tips('漏读标签为空');
		return;
	}
	$('#popInfoCon').html(txt);
	$('#mask').show();
	$('#pop').show();
}
//隐藏漏读弹窗
function hideMissedRead(){
	$('#mask').hide();
	$('#pop').hide();
}
//转换率
function chagnecRate(num,len){
	num = num || 0;
	return (num * 100).toFixed(len || 2);
}
//选择运动模式
function check(that,cancelTar){
	if(that.checked){
		$('[name="' + cancelTar + '"]').attr('checked',false);
	}
}
