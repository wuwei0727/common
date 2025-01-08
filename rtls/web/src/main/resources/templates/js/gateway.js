var ids;//删除数据的ID
var where = {};
var list= {}
$(function(){
	$(document).on('click',function(e){
		hideSele();
	})
	where.pageIndex = 1;
	where.pageSize = pageSize;
    loadSeleData();
    init();
})
//加载下拉数据
function loadSeleData() {
    loadFun('map/getMap2dSel', { pageSize: -1, enable: 1 }, '#mapSelect1');
    loadFun('map/getMap2dSel', { pageSize: -1, enable: 1 }, '#mapSelect');
}
//初始化列表
function init(){
	$.ajax({
        url: url + 'gateway_lora/getGatewaySel',
        data:where,
        type:'post',
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success: function(res) {
        	var tab = $('#tab');
            if(res.code != 200){
                tableException(tab,res.message);
                return;
            }
            var data = res.data;
             list = data.list;
            var len = list.length;
            if (!len) {
                tableException(tab, '当前搜索结果为空');
            }
            //全选按钮（取消）
            var allChe = $('#allCheck');
            if(allChe.hasClass('curSele')){
                allChe.removeClass('curSele');
            }
            if(pageIndex != data.pageIndex){
                pageIndex = data.pageIndex;
            }
        	var allName = getTheadName(tab.find('thead'));

        	var html = '';
            var target = null;
            var name = null;
            var value = null;
            var lineNum = (pageIndex - 1) * pageSize + 1;
        	for(var i = 0;i < len;i++){
        		target = list[i];
        		html += '<tr>';
        		for(var j = 0;j < allName.length;j++){
        			name = allName[j];
                    if(name === ''){
                        continue;
                    }
                    value = target[name];
        			if(name === 'line'){
                        html += resTabCheck(target,lineNum + i,'id','num');
        			}else if(name === 'operating'){
                        var editBtn = document.getElementById("editBtn");
                        var deleteBtn = document.getElementById("deleteBtn");
                        var editTxt = editBtn ? '<span class="tabOper" onclick="detail(\'' + target.id + '\')">详情</span>' : '';
                        var delteTxt = deleteBtn ? '<span class="tabOper deleOpa" onclick="showDele(\'' + target.id + '\',\'' + target.num + '\')">删除</span>' : '';
                        var posTxt = '<span class="tabOper deleOpa" onclick="showlocation(\'' + target.id + '\',\'' + target.num + '\',)">位置</span>';
                        html = html + '<td>' + editTxt + delteTxt + posTxt + '</td>';
        			}else if(name === 'networkstate'){
                        html += '<td>';
                        if(value === 1){
                            html += '<span class="stateHas">';
                        }
                        // else if(value === 2){
                        //     html += '<span class="stateWarn" style="left:-2px"><img class="lowBattery" src="../image/common/lowBattery.png">';
                        // }
                        else{
                            html += '<span class="stateNot">';
                        }
                        html += target['networkName'] + '</span></td>';
                    }else{
    					html += '<td>' + convertNull(value) + '</td>';
        			}
        		}
        		html += '</tr>';
        	}
        	tab.find('tbody').html(html);
            var htmlStr ="共 <span class='c4A60CF'>"+ data.pages +" </span> 页 / <span class='c4A60CF'>" + data.total + " </span>条数据"
            $('[id="total"]').html(htmlStr);
        	//生成页码
        	initDataPage(pageIndex,data.pages,data.total);
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
//单行的删除提示
function showDele(id,txt){
    ids = id;
    $('#deleTxt').text(txt);
    showPop('#delePop');
}
//多行的删除
function showAllDele(){
	var cheInp = $('#tab tbody').find('input:checked');
    if(!cheInp.length){
        tips('请选择至少一条数据');
        return;
    }
    var showTxt = '';
    var cheId = '';
    for(var i = 0;i < cheInp.length;i++){
    	showTxt += cheInp[i].getAttribute('data-txt') + '、';
    	cheId += cheInp[i].value + ',';
    }
    showTxt = showTxt.slice(0,-1);
    ids = cheId.slice(0,-1);
    $('#deleTxt').html(showTxt);
	showPop('#delePop');
}
//确认删除
function entDele(){
	$.ajax({
        url:url + 'gateway_lora/delGateway/' + ids,
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success:function(res){
            tips(res.message);
        	if(res.code == 200){
                hidePop('#delePop');
                search();
        	}
        },
        error:function(jqXHR){
            var status = jqXHR.status;
            if(status == 401){
                document.write(jqXHR.responseText)
            }
            resError(jqXHR);
        }
    })
}
//翻页
function turnPage(){
	where.pageIndex = pageIndex;
	init();
}
//编辑
function detail(id){
    $('#editFrame').attr('style', 'z-index: 99') 
    $('#editFrame').attr('src', './gatewayDetails.html?id=' + id) 
    $('#editFrame').show()
}

function closeFrame(){
    $('#editFrame').hide()
    search()
}

function switchGateway(that,id){
    if(that.innerHTML === '断开连接'){
        disconnectGateway(that,id);
    }else{
        connectGateway(that,id);
    }
}

//表格排序
function tabSort(that,sortName){
    console.log(sortName)
    switchSort(that,function(sortVal){
        if(sortVal == 'desc'){
            where.desc = sortName +'+0' +' '+'desc';
        }else{
            where.desc = sortName +'+0';
        }
        where.pageIndex = 1;
        console.log(where);
        init();
    })
}
function tabSort1(that,sortName){
    switchSort(that,function(sortVal){
        if(sortVal === 'desc'){
            where.desc = sortName + ' desc';
        }else{
            where.desc = sortName;
        }
        where.pageIndex = 1;
        console.log(where);
        init();
    })
}
// 位置显示
function showlocation(id){
     
    var idmai=id
    // const result = list.find(item => item.id == idmai);
    
    $('#mapPop').show();
    $('#mask').show();
  
    result = contrastReturn(list,'id',idmai);
    console.log(result.mapKey,'晚上来吗',result.appName,result.fmapID,result);

    openMap({
        fKey: result.mapKey || '',
        fName: result.appName || '',
        fId: result.fmapID || '',
        focusFloor: result.floor || '',
        path: result.themeImg || '',
        x: result.x,
        y: result.y,
        num: result.num,
        //初始指北针的偏移量
        compassOffset: [20, 12],
        //指北针大小默认配置
        compassSize: 48,
        mapId: result.map
    }, function () {
        analyser = new fengmap.FMSearchAnalyser(fMap);
        var x = +$('[name="x"]').val();
        var y = +$('[name="y"]').val();
        if (x && y) {
            fMap.moveTo({
                x: x,
                y: y,
                groupID: floor,
            })
        }
        var fid = $('[name="fid"]').val();
        var target = null;
        if (fid) {
            target = findModel({ FID: fid });
            if (target) {
                target.selected = true;
                selectedModel = target;
            }
        }
    });
};

function exportShow() {
    $('#saveTxt').html('导出网关数据');
    showPop('#permissPop');
};

function exportBtn() {
    var mapId = $('[name="map"]').data('val');
    if (!mapId) {
        tips('请先选择地图');
        return;
    }
    var xhr = new XMLHttpRequest();
    xhr.open('POST', url + '/importExportDeviceInfo/exportGatewayExcel/' + mapId);
    xhr.onreadystatechange = function () {
        if (xhr.readyState === XMLHttpRequest.OPENED || xhr.readyState === XMLHttpRequest.HEADERS_RECEIVED) {
            if (xhr.status === 500) {
                xhr.responseType = 'text';
            } else if (xhr.status === 200) {
                xhr.responseType = 'blob';
            }
        }
    };
    xhr.onload = function () {
        if (this.status === 200) {
            // 将二进制数据转换为 Blob 对象
            var blob = new Blob([this.response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8' });
            // 获取 Content-Disposition 头中的文件名
            var fileName = '';
            var disposition = xhr.getResponseHeader('Content-Disposition');
            if (disposition && disposition.indexOf('attachment') !== -1) {
                var filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
                var matches = filenameRegex.exec(disposition);
                if (matches != null && matches[1]) {
                    fileName = decodeURIComponent(matches[1].replace(/['"]/g, ''));
                }
            }
            // 创建 Object URL
            var url = URL.createObjectURL(blob);
            // 创建链接元素
            var link = document.createElement('a');
            link.href = url;
            link.download = fileName;
            // 添加链接到页面并执行下载
            document.body.appendChild(link);
            link.click();
            // 删除链接元素和 Object URL
            document.body.removeChild(link);
            URL.revokeObjectURL(url);
        } else {
            alert("导出Excel文件失败:" + xhr.responseText);
        }
    };
    console.log(this.statusText)
    xhr.onerror = function () {
        alert("导出Excel文件失败:" + this.responseText);
    }
    xhr.send();
};

//关闭弹窗
function closePop(target) {
    //恢复状态
    $('[name="cnamePop"]').val('');
    hidePop(target);
};