var where = null;
var tabLen = 0;//表格的td列数，用于无数据时
var thead = null;
$(function(){
	//计算pageSize
    calcPage();
    where = {
        start:'',
        end:'',
        pageIndex:pageIndex,
        pageSize:pageSize,
    }
	init();
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
//加载数据
function init(){
	$.ajax({
        url: url + 'bluetooth/getRecordSel',
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
                where.pageIndex = pageIndex = data.pageIndex;
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
        				html += '<td><span class="tabOper" onclick="startDownload(\'' + listTar.id + '\')">下载</span><span class="tabOper delete" onclick="preview(\'' + listTar.id + '\')">预览</span><span class="tabOper delete" onclick="dele(\'' + listTar.id + '\',\'' + (lineNum + i) + '\')">删除</span></td>';
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
var curId;
//获取分析详情
function preview(id,subtype){
    if(id){
        curId = id;
    }
    $.ajax({
        url:url + 'bluetooth/analyzeRecord',
        data:{
            id:curId,
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
                if(!subtype){
                    showPop('#analysisPop');
                    $('#scanTime').html(data.batch.scan_time || '');
                    $('#scanDuration').html(data.batch.scan_duration || '');
                    //赋值条件
                    setAnalysisInfo(data.conditions);
                    $('#subtypeTxt').html('类型');
                }
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
                    continue;
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
        url:url + 'bluetooth/delRecord',
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
//搜索事件
function search(num){
    if(num){
        var seleTime = $('#seleTime').val().split(' 至 ');
        where.start = seleTime[0] || '';
        where.end = seleTime[1] || '';
        where.pageIndex = 1;
    }else{
        where.pageIndex = pageIndex;
    }
    init();
}
//选择类型
function seleType(that,id){
    preview('',id);
    $('#subtypeTxt').html(that.innerHTML);
}
//表格滚动固定表头
function tabScroll(that){
    thead.style.transform = 'translateY(' + (that.scrollTop) + 'px)';
}
//下载
function startDownload(id,name){
    if(!id){
        tips('下载失败，资源不存在');
        return;
    }
    var a = document.createElement("a");
    // a.download = name || '分析记录';
    a.href = url + '/bluetooth/exportResult?id=' + id;
    $("body").append(a);// 修复firefox中无法触发click
    a.click();
    $(a).remove();
    setTimeout(function(){
        init(true);
    },300)
}