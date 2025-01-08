var tabLen = 0;//表格的td列数，用于无数据时
var eleArr = [];
var styleFill = null;
var where = {};//搜索条件
var thead = null;
var styleId;
$(function(){
	//计算pageSize
    calcPage();
    where = {
        bname:'',
        btype:1,
        start:'',
        end:'',
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
	//初始化时间范围
	laydate.render({
	  	elem: '#seleTime',
	  	theme: '#0876D7',
	  	type: 'datetime',
	  	range: '至',
	  	done:function(value){
	  		//确定清除的回调
	  		$('#timeRange').html(value);
	  	}
	});
    thead = document.getElementById('thead');
})

function getNowFormatDate() {
    var date = new Date();
    var seperator1 = "-";
    var seperator2 = ":";
    var month = date.getMonth() + 1;
    var strDate = date.getDate();
    if (month >= 1 && month <= 9) {
        month = "0" + month;
    }
    if (strDate >= 0 && strDate <= 9) {
        strDate = "0" + strDate;
    }
    var currentdate = date.getFullYear() + seperator1 + month + seperator1 + strDate
            + " " + date.getHours() + seperator2 + date.getMinutes()
            + seperator2 + date.getSeconds();
    return currentdate;
}
//加载数据
function init(){
	$.ajax({
        url: where.url,
        type:'post',
        data:where,
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success: function(data) {
            tips(data.msg);
        	var tab = $('#tab');
          
            var list = data.allList || [];
            var len = list.length;

        	
           // if(pageIndex != data.pageIndex){
            //    where.pageIndex = pageIndex = data.pageIndex;
            //}
        	
        	var allName = getTheadName(tab.find('thead'));
            tabLen || (tabLen = allName.length);//为0时则赋值
        	var html = '';
        	var nameTar = null;
        	var listTar = null;
            var lineNum = (pageIndex - 1) * pageSize + 1;
        	for(var i = 0;i < len;i++){
        		listTar = list[i];
				listTar.id=i;
        		html += '<tr>';
        		for(var j = 0;j < allName.length;j++){
        			nameTar = allName[j];
        			if(nameTar == 'line'){
                        html += '<td class="padLeft40">' + (lineNum + i) + '</td>';
        			}else if(nameTar == 'operating'){
        				html += '<td><span class="tabOper" onclick="bluetoothDownload(\'' + listTar.id + '\')">下载</span><span class="tabOper delete" onclick="analysisPop(\'' + listTar.id + '\')">分析</span><span class="tabOper delete" onclick="dele(\'' + listTar.id + '\',\'' + listTar.gatewaynum + '\')">删除</span></td>';
        			}else{
    					html += '<td>' + (listTar[nameTar] || '0') + '</td>';
        			}
        		}
        		html += '</tr>';
        	}
        	tab.find('tbody').html(html);
        	//生成页码
        	//initDataPage(pageIndex,data.pages,data.total);
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
var curId;
var styleFlag = true;
//显示样式弹窗
function analysisPop(id){
	curId = id;
	if(styleFlag){
		initStyle();
	}
	showPop('#stylePop');
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
        	styleFlag = false;
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
	var id = curStyleItem.data('id');
	if(id == -1){
		tips('空白样式不能保存');
		return;
	}
	var tempArr = resData();
	if(tempArr){
		sendStyle(tempArr,curStyleItem.text(),'edit',id);
	}
}
//返回值
function resData(){
	var tempArr = [];
	var itemData = null;
	for(var i = 0;i < eleArr.length;i++){
		itemData = resVal((eleArr[i].allInp),eleArr[i].txt);
		if(itemData == -1){
			continue;
		}
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
	var isNull = false;
	$.each(hookUpData,function(i,row){
		eVal = $.trim(row.value);
		eName = row.name;

		if(isNaN(eVal)){
			tips( txt + '列请填写正确的数字');
			flag = true;
			return;
		}/*
		if(eVal === ''){
			tips('请填写全 ' + txt + '列 的所有输入框');
			flag = true;
			return;
		}*/
		if(!isNull && eVal){
			isNull = true;
		}
		tempData[eName] = eVal || '';
	})
	if(flag){
		return '';
	}
	//判断是否为全空
	if(!isNull){
		return -1;
	}
	if(tempData.rssi_avgS){
		if(!tempData.rssi_avgE){
			tips('请填写 <span style="color:#ffa500">' + txt + '列</span> 的 <span style="color:#ffa500">RSSI平均</span> 的结束值',null,5000);
			return;
		}
	}
	if(tempData.rssi_avgE){
		if(!tempData.rssi_avgS){
			tips('请填写 <span style="color:#ffa500">' + txt + '列</span> 的 <span style="color:#ffa500">RSSI平均</span> 的开始值',null,5000);
			return;
		}
	}

	if(tempData.voltage_avgS){
		if(!tempData.voltage_avgE){
			tips('请填写 <span style="color:#ffa500">' + txt + '列</span> 的 <span style="color:#ffa500">电压平均</span> 的结束值',null,5000);
			return;
		}
	}
	if(tempData.voltage_avgE){
		if(!tempData.voltage_avgS){
			tips('请填写 <span style="color:#ffa500">' + txt + '列</span> 的 <span style="color:#ffa500">电压平均</span> 的开始值',null,5000);
			return;
		}
	}

	if(tempData.voltage_maxS){
		if(!tempData.voltage_maxE){
			tips('请填写 <span style="color:#ffa500">' + txt + '列</span> 的 <span style="color:#ffa500">电压MAX</span> 的结束值',null,5000);
			return;
		}
	}
	if(tempData.voltage_maxE){
		if(!tempData.voltage_maxS){
			tips('请填写 <span style="color:#ffa500">' + txt + '列</span> 的 <span style="color:#ffa500">电压MAX</span> 的开始值',null,5000);
			return;
		}
	}

	if(tempData.air_pressure_avgS){
		if(!tempData.air_pressure_avgE){
			tips('请填写 <span style="color:#ffa500">' + txt + '列</span> 的 <span style="color:#ffa500">气压平均</span> 的结束值',null,5000);
			return;
		}
	}
	if(tempData.air_pressure_avgE){
		if(!tempData.air_pressure_avgS){
			tips('请填写 <span style="color:#ffa500">' + txt + '列</span> 的 <span style="color:#ffa500">气压平均</span> 的开始值',null,5000);
			return;
		}
	}
	var data = {
		rssi_avg:(tempData.rssi_avgS) + ',' + (tempData.rssi_avgE),
		rssi_variance:(tempData.rssi_variance),
		voltage_avg:(tempData.voltage_avgS) + ',' + (tempData.voltage_avgE),
		voltage_max:(tempData.voltage_maxS) + ',' + (tempData.voltage_maxE),
		voltage_min:(tempData.voltage_minS) + ',' + (tempData.voltage_minE),
		air_pressure_avg:(tempData.air_pressure_avgS || '') + ',' + (tempData.air_pressure_avgE || ''),
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
        	/*
        	for(var i = 0;i < len;i++){
        		for(var j = 0;j < eleArr.length;j++){
        			if(conditions[i].type == eleArr[j].type){
        				setInpVal(eleArr[j].allInp,conditions[i]);
        				break;
        			}
        		}
        	}*/
        	var falg;
        	for(var j = 0;j < eleArr.length;j++){
        		falg = true;
        		for(var i = 0;i < len;i++){
        			if(conditions[i].type == eleArr[j].type){
        				falg = false;
	    				setInpVal(eleArr[j].allInp,conditions[i]);
	    				break;
	    			}
        		}
        		if(falg){
        			clearInpVal([eleArr[j]]);
        		}
    		}
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
//清空值
function clearInpVal(itemArr){
	var tempArr = itemArr || eleArr;
	let allInp = null;
	for(var i = 0;i < tempArr.length;i++){
		allInp = tempArr[i].allInp;
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
	var id = $('.curStyleItem').data('id');
	if(id == -1){
		tips('空白样式不能进行分析，请选择自定义样式');
		return;
	}
	if(!id){
		tips('请先保存当前的新增样式');
		return;
	}
	styleId && (styleId = '');
    $.ajax({
        url:url + '/bluetooth/analyzeResult',
        data:{
            batch_id:curId,
            model_id:id
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
            	styleId = data.id;
            	//赋值表格
            	setAnalysisTab(data.result);
        		$('#scanTime').html(data.batch.scan_time || '');
            	$('#scanDuration').html(data.batch.scan_duration || '');
            	//赋值条件
            	setAnalysisInfo(data.conditions);
            	closePop('#stylePop');
            	$('#subtypeTxt').html('类型');
        		$('#analysisPop').show();
            }
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
//赋值条件
function setAnalysisInfo(data){
    var itemArr = [{
            txt:'挂钩',
            type:2,
            ele:'#aHookUp'
        },{
            txt:'卡扣',
            type:1,
            ele:'#aBuckle'
        },{
            txt:'信标',
            type:3,
            ele:'#aBeacon'
        },{
            txt:'PA信标',
            type:4,
            ele:'#aPaBeacon'
        }]
	var target = null;
	
    var flag;
    for(var z = 0; z < itemArr.length;z++){
        flag = true;
        for(var i = 0;i < data.length;i++){
            target = data[i];
            if(itemArr[z].type == target.type){
                flag = false;
                setAnalysisVal($(itemArr[z].ele).find('[name]'),target,true);
            }
        }
        if(flag){
            setAnalysisVal($(itemArr[z].ele).find('[name]'),null,false);
        }
    }
}
//设置值
function setAnalysisVal(item,data,falg){
    var name;
    for(var j = 0;j < item.length;j++){
        name = item[j].name;
        if(name == 'rssi_variance'){
            item[j].value = falg ? (resChangeAna(data[name],1)) : '------';
        }else{
            item[j].value = falg ? (resChangeAna(data[name])) : '------';
        }
    }
}
//返回转换的值
function resChangeAna(val,variance){
    if(val == ',' || val === ''){
        return '------';
    }
    if(variance){
        return '< ' + val;
    }else{
        return val.replace(',',' ~ ');
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
				if(listTar[nameTar] == 2){
                    html += '<td>------</td>';
                }else if(listTar[nameTar] == 1){
					html += '<td><span class="analysisIcon qualified">';
				}else{
					html += '<td><span class="analysisIcon failed">';
				}
                html += '</span><span style="margin-left:10px">' + ((+(listTar['analyze'] || {})[nameTar.slice(0,-7)]) || 0).toFixed(2) + '</span></td>';
			}else{
				html += '<td>' + listTar[nameTar] + '</td>';
			}
		}
		html += '</tr>';
	}
	tab.find('tbody').html(html);
}
//搜索事件
function search(num){
	where.gatewaynum = $('[name="gatewaynum"]').val();
	where.infrarednum=$('[name="infrarednum"]').val();
	where.state=$('[name="state"]').val();


	// var seleTime = $('#seleTime').val().split(' 至 ');
	where.start = $('[name="start"]').val();
	where.end =$('[name="end"]').val();
    if(num==1){
		where.url=url+"kk/state";
        where.pageIndex = 1;
        init();
    }else if(num==2){
		where.url=url+"kk/state";
        where.pageIndex = pageIndex;
		var formUrl = where.url;
		var form = $("<form></form>").attr("action", formUrl).attr("method", "post");
		var html = '';
		var target = null;
		var txt = '';
		for (var key in where) {

			console.log(key); //获取key值 console.log(json[key]);
			html += '<input type="hide" name="' + key + '" value="' + where[key]+ '" />';
			txt += key + '：' + where[key] + '  ';
		}

		form.append(html);
		form.append('<input type="hide" name="title" value="' + txt + '" />');
		form.appendTo('body').submit().remove();
    }else if(num==3){
		where.url=url+"kk/exportFraredCount";
        where.pageIndex = pageIndex;
		var formUrl = where.url;
		var form = $("<form></form>").attr("action", formUrl).attr("method", "post");
		var html = '';
		var target = null;
		var txt = '';
		for (var key in where) {

			console.log(key); //获取key值 console.log(json[key]);
			html += '<input type="hide" name="' + key + '" value="' + where[key]+ '" />';
			txt += key + '：' + where[key] + '  ';
		}

		form.append(html);
		form.append('<input type="hide" name="title" value="' + txt + '" />');
		form.appendTo('body').submit().remove();
    }


}

//选择类型 --筛选
function seleType(that,subtype){
	$.ajax({
        url:url + 'bluetooth/analyzeRecord',
        data:{
            id:styleId,
            subtype:subtype || ''
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
                $('#subtypeTxt').html(that.innerHTML);
            	//赋值表格
            	setAnalysisTab(data.result);
            }
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
//关闭弹窗
function closePop(pop,mask){
	clearInpVal();
	if(styleFill.is(':visible')){
		styleFill.val('').hide();
		$('#blankStyle').addClass('curStyleItem');
	}else{
		var curStyleItem = $('.curStyleItem');
		if(curStyleItem.data('id') != -1){
			curStyleItem.removeClass('curStyleItem');
			$('#blankStyle').addClass('curStyleItem');
		}
	}
	$(pop).hide();
	if(mask){
		$('#mask').hide();
	}
}
//表格滚动固定表头
function tabScroll(that){
    thead.style.transform = 'translateY(' + (that.scrollTop) + 'px)';
}
//下载
function bluetoothDownload(id,name){
    if(!id){
        tips('下载失败，资源不存在');
        return;
    }
    var a = document.createElement("a");
    a.href = url + '/bluetooth/exportBluetooth?id=' + id;
    $("body").append(a);// 修复firefox中无法触发click
    a.click();
    $(a).remove();
    setTimeout(function(){
        init(true);
    },300)
}