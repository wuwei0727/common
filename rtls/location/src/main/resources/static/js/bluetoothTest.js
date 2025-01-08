var searchData = null;
var tabLen = 0;//表格的td列数，用于无数据时
var eleArr = [];
var styleFill = null;
$(function(){
	//计算pageSize
    calcPage();
    searchData = {
        bname:'',
        btype:1,
        start_time:'',
        end_time:'',
        pageIndex:pageIndex,
        pageSize:pageSize,
    }
	init();
	$('#styleList').click(function(ev){
		var target = ev.target || ev.srcElement;
		var targetName = target.nodeName.toLowerCase();
		if(targetName == 'b'){
			$('.curStyleItem').removeClass('curStyleItem');
			//新增
			addItem();
			return false;
		}
		if(targetName == 'i'){
			//删除
			if(confirm('是否确定删除该样式')){
				deleStyle(target);
			}
			//tips('删除');
			return false;
		}
		if(targetName == 'span'){
			initStyleDetail(target.getAttribute('data-id'));
			if(styleFill.is(':visible')){
				return;
			}
			//切换类
			if(target.className == 'styleItem curStyleItem'){
				return;
			}
			$('.curStyleItem').removeClass('curStyleItem');
			$(target).addClass('curStyleItem');
			return false;
		}
		return false;
	})
	eleArr = [{
		txt:'挂钩',
		type:2,
		allInp:$('#hookUp').find('input')
	},{
		txt:'卡扣',
		type:1,
		allInp:$('#buckle').find('input')
	},{
		txt:'信标',
		type:3,
		allInp:$('#beacon').find('input')
	},{
		txt:'PA信标',
		type:4,
		allInp:$('#paBeacon').find('input')
	}];
	styleFill = $('#styleFill');
})
//加载数据
function init(){
	$.ajax({
        url: url + 'batch/getBatchSel',
        type:'post',
        data:searchData,
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success: function(data) {
            tips(data.msg);
        	var tab = $('#tab');
            if(!data.status){
                tab.find('tbody').html(resErrTab(tabLen,tab));
                initDataPage(0,0,0);
                return;
            }
            var list = data.list || [];
            var len = list.length;

        	if(!len){
        		tab.find('tbody').html(noData(tabLen));
                initDataPage(0,0,0);
        		return;
        	}
            if(pageIndex != data.pageIndex){
                searchData.pageIndex = pageIndex = data.pageIndex;
            }
        	
        	var allName = getTheadName(tab.find('thead'));
            tabLen || (tabLen = allName.length);//为0时则赋值
        	var html = '';
        	var nameTar = null;
        	var listTar = null;
            var lineNum = (pageIndex - 1) * pageSize + 1;
        	for(var i = 0;i < len;i++){
        		listTar = list[i];
        		html += '<tr>';
        		for(var j = 0;j < allName.length;j++){
        			nameTar = allName[j];
        			if(nameTar == 'line'){
                        html += '<td class="padLeft40">' + (lineNum + i) + '</td>';
        			}else if(nameTar == 'operating'){
        				html += '<td><span class="tabOper" onclick="edit(\'' + listTar.id + '\')">下载</span><span class="tabOper delete" onclick="edit(\'' + listTar.id + '\')">分析</span><span class="tabOper delete" onclick="dele(\'' + listTar.id + '\',\'' + listTar.bname + '\')">删除</span></td>';
        			}else{
    					html += '<td>' + (listTar[nameTar] || '') + '</td>';
        			}
        		}
        		html += '</tr>';
        	}
        	tab.find('tbody').html(html);
        	//生成页码
        	initDataPage(pageIndex,data.pages,data.total);
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
var deleId;
//删除
function dele(id,txt){
	deleId = id;
    $('#deleTxt').text(txt);
    showPop('#deleInq');
}
//确认删除
function entDele(){
    $.ajax({
        url:url + 'batch/delBatch',
        data:{
            ids:deleId
        },
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success:function(data){
            if(data.status){
                hidePop('#deleInq');
                deleId = '';
                tips(data.msg,function(){
                    init();
                },500);
            }else{
                tips(data.msg);
            }
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
//新增样式
function addItem(){
	if(styleFill.is(':visible')){
		tips('请先保存当前的新增指标');
		return;
	}else{
		styleFill.show();
	}
}
//加载样式
function initStyle(){
	$.ajax({
        url: url + 'model/getModelSel',
        type:'post',
        data:{
        	pageSize:-1,
        },
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success: function(data) {
        	if(!data.status){
				tips('获取样式失败，请重试');
				return;
        	}
        	var list = data.list;
        	var len = list.length;
        	var html = '';
        	for(var i = 0;i < len;i++){
        		html += '<span class="styleItem" data-id="' + list[i].id + '">' + list[i].mname + '<i class="styleItemClose"></i></span>';
        	}
        	styleFill.before(html);
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
//保存样式
function saveStyle(){
	if(styleFill.is(':hidden')){
		//tips('请先填写样式名称');
		editStyle();
		return;
	}
	var val = $.trim(styleFill.find('input').val());
	if(!val){
		tips('新增样式名称不能为空');
		return;
	}
	var tempArr = resData();
	if(tempArr){
		sendStyle(tempArr,val,'add');
	}
}
//编辑样式
function editStyle(){
	var curStyleItem = $('.curStyleItem');
	var tempArr = resData();
	if(tempArr){
		sendStyle(tempArr,curStyleItem.text(),'edit',curStyleItem.data('id'));
	}
}
//返回值
function resData(){
	var tempArr = [];
	var itemData = null;
	for(var i = 0;i < eleArr.length;i++){
		itemData = resVal((eleArr[i].allInp),eleArr[i].txt);
		if(itemData){
			itemData.type = eleArr[i].type;
			tempArr.push(itemData);
		}else{
			return;
		}
	}
	return tempArr;
}
//判断值并返回
function resVal(hookUpData,txt){
	var eVal;
	var eName;
	var tempData = {};
	var flag = false;
	$.each(hookUpData,function(i,row){
		eVal = $.trim(row.value);
		eName = row.name;
		/*
		if(eVal === ''){
			tips('请填写全 ' + txt + '列 的所有输入框');
			flag = true;
			return;
		}
		if(isNaN(eVal)){
			tips( txt + '列请填写正确的数字');
			flag = true;
			return;
		}*/
		tempData[eName] = eVal || i;
	})
	if(flag){
		return '';
	}
	var data = {
		rssi_avg:tempData.rssi_avgS + ',' + tempData.rssi_avgE,
		rssi_variance:tempData.rssi_variance,
		voltage_avg:tempData.voltage_avgS + ',' + tempData.voltage_avgE,
		voltage_max:tempData.voltage_maxS + ',' + tempData.voltage_maxE,
		voltage_min:tempData.voltage_minS + ',' + tempData.voltage_minE,
		air_pressure_avg:tempData.air_pressure_avgS + ',' + tempData.air_pressure_avgE,
	}
	return data;
}
//添加样式数据
function sendStyle(arr,mname,flag,id){
	var sUrl;
	if(flag == 'add'){
		sUrl = url + 'model/addModel?mname=' + mname;
	}else if(flag == 'edit'){
		// 样式自增id
		sUrl = url + 'model/updateModel?mname=' + mname + '&id=' + id;
	}else{
		return;
	}
	$.ajax({
        url: sUrl,
        type:'post',
        headers : {
            'Content-Type' : 'application/json'
        },
        data:JSON.stringify(arr),
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success: function(data) {
        	tips(data.msg);
        	if(data.status){
        		if(flag == 'add'){
					styleFill.hide().before('<span class="styleItem curStyleItem" data-id="' + data.obj + '">' + mname + '<i class="styleItemClose"></i></span>');
        		}
        	}
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
//加载样式详情
function initStyleDetail(id){
	if(!id){
		tips('获取样式详情失败');
		return;
	}
	if(id == -1){
		clearInpVal();
		return;
	}
	$.ajax({
        url: url + 'model/getModelId',
        type:'post',
        data:{
        	id:id,
        },
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success: function(data) {
        	if(!data.status){
				tips('获取样式失败，请重试');
				return;
        	}
        	//遍历赋值
        	var conditions = data.model.conditions;
        	var len = conditions.length;
        	if(!len){
        		clearInpVal();
        		return;
        	}
        	for(var i = 0;i < len;i++){
        		for(var j = 0;j < eleArr.length;j++){
        			if(conditions[i].type == eleArr[j].type){
        				setInpVal(eleArr[j].allInp,conditions[i]);
        				break;
        			}
        		}
        	}
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
//清空值
function clearInpVal(){
	let allInp = null;
	for(var i = 0;i < eleArr.length;i++){
		allInp = eleArr[i].allInp;
		for(var j = 0;j < allInp.length;j++){
			if(allInp[j].value){
				allInp[j].value = '';
			}
		}
	}
}
//写值
function setInpVal(eArr,vObj){
	var target = null;
	var eName;
	var tempVal;
	for(var i = 0;i < eArr.length;i++){
		target = eArr[i];
		eName = target.name;
		if(eName == 'rssi_variance'){
			if(target.value != vObj[eName]){
				target.value = vObj[eName];
			}
			continue;
		}
		if(eName.indexOf('S') != -1){
			tempVal = vObj[eName.slice(0,-1)].split(',')[0];
		}else if(eName.indexOf('E') != -1){
			tempVal = vObj[eName.slice(0,-1)].split(',')[1];
		}
		if(target.value != tempVal){
			target.value = tempVal;
		}
	}
}
//删除样式
function deleStyle(target){
	var parent = $(target).parent();
	var clear = parent.hasClass('curStyleItem');
	var id = parent.data('id');
	$.ajax({
        url: url + 'model/delModel',
        type:'post',
        data:{
        	id:id,
        },
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success: function(data) {
        	tips(data.msg);
        	if(data.status){
				parent.remove();
				if(clear){
					clearInpVal();
					$('#blankStyle').addClass('curStyleItem');
				}
        	}
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
//分析并保存
function analysis(){
    $.ajax({
        url:url + '/bluetooth/analyzeResult',
        data:{
            batch_id:'35',
            model_id:'17',
            desc:'',
        },
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success:function(data){
            tips(data.msg);
            if(data.status){
            	$('#scanTime').html(data.batch.scan_time || '');
            	$('#scanDuration').html(data.batch.scan_duration || '');
            	//赋值条件
            	setAnalysisInfo(data.conditions);
            	//赋值表格
            	setAnalysisTab(data.result);
            }
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
//赋值条件
function setAnalysisInfo(data){
	var obj = {
		id2:{
			txt:'挂钩',
			type:2,
			ele:'#aHookUp'
		},
		id1:{
			txt:'卡扣',
			type:1,
			ele:'#aBuckle'
		},
		id3:{
			txt:'信标',
			type:3,
			ele:'#aBeacon'
		},
		id4:{
			txt:'PA信标',
			type:4,
			ele:'#aPaBeacon'
		}
	};
	var allInp = null;
	var target = null;
	var name;
	for(var i = 0;i < data.length;i++){
		target =  data[i];
		allInp = $(obj['id' + target.type].ele).find('[name]');
		for(var j = 0;j < allInp.length;j++){
			name = allInp[j].name;
			if(name == 'voltage_avg'){
				allInp[j].value = '< ' + target[name];
			}else{
				allInp[j].value = target[name].replace(',',' ~ ');
			}
		}
	}
}
//赋值表格
function setAnalysisTab(data){
	var html = '';
	var index = 0;
	var tab = $('#analysisTab');
	var allName = getTheadName(tab.find('thead'));
	var listTar = null;
	var nameTar;
	for(var i = 0;i < data.length;i++){
		listTar = data[i];
		html += '<tr>';
		for(var j = 0;j < allName.length;j++){
			nameTar = allName[j];
			if(nameTar == 'line'){
                html += '<td class="padLeft40">' + (1 + i) + '</td>';
			}else if(nameTar == 'rssi_avg_result' || nameTar == 'rssi_variance_result' || nameTar == 'voltage_avg_result' || nameTar == 'voltage_max_result' || nameTar == 'voltage_min_result' || nameTar == 'air_pressure_avg_result'){
				if(listTar[nameTar]){
					html += '<td><span class="analysisIcon qualified"></span></td>';
				}else{
					html += '<td><span class="analysisIcon failed"></span></td>';
				}
			}else{
				html += '<td>' + listTar[nameTar] + '</td>';
			}
		}
		html += '</tr>';
	}
	tab.find('tbody').html(html);
}