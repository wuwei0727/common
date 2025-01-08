var ids;//删除数据的ID
var where = {};
var list = [];
$(function () {
    $(document).on('click', function (e) {
        hideSele();
    })
    where.pageIndex = 1;
    where.pageSize = pageSize;
    loadSeleData();
    init();
})
//加载下拉数据
function loadSeleData() {
    loadFun('map/getMap2dSel', { pageSize: -1, enable: 1 }, '#mapSelect');
    loadFun('map/getMap2dSel', { pageSize: -1, enable: 1 }, '#mapSelect1');
}
//初始化列表
function init(){
    $.ajax({
        url: url + 'vip/getFloorLockInfo',
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
            if(res.code !== 200){
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
            if(allChe.prop('checked')){
                allChe.prop('checked',false);
            }
            if(pageIndex !== data.pageIndex){
                pageIndex = data.pageIndex;
            }
            var allName = getTheadName(tab.find('thead'));

            var html = '';
            var nameTar = null;
            var listTar = null;
            var value = null;
            var lineNum = (pageIndex - 1) * pageSize + 1;
            for(var i = 0;i < len;i++){
                listTar = list[i];
                html += '<tr>';           
                for(var j = 0;j < allName.length;j++){
                    nameTar = allName[j];
                    if(nameTar === ''){
                        continue;
                    }
                    value = listTar[nameTar];
                    if(nameTar === 'line'){
                        html += resTabCheck(listTar,lineNum + i, "id", "deviceNum");
                    }else if(nameTar === 'operating'){
                        var editBtn = document.getElementById("editBtn");
                        var deleteBtn = document.getElementById("deleteBtn");
                        var todoBtn = document.getElementById("todoBtn");
                        var codeBtn = document.getElementById("codeBtn");

                        var editTxt = editBtn ? '<span class="tabOper" onclick="detail(\'' + listTar.id + '\')">编辑</span>' : '';
                        var delteTxt = deleteBtn ? '<span class="tabOper deleOpa" onclick="showDele(\'' + listTar.id + '\',\'' + listTar.deviceNum + '\')">删除</span>' : '';
                        var codeTxt = codeBtn ? '<span class="tabOper deleOpa" onclick="seeCode(\'' + listTar.id + '\')">二维码</span>' : '';

                        var posTxt = '<span class="tabOper deleOpa" onclick="showlocation(\'' + listTar.id + '\',\'' + listTar.num + '\',)">位置</span>';
                        var moreBtn = todoBtn ? `
                            <span class="moreBtnBox">
                                <span class="moreBtnText" onclick="openMoreBtn('${listTar.id}')">更多操作</span>
                                <ul class="moreBtnUl" id="${'moreBtnUl' + listTar.id}">
                                    <li onclick="clickMoreBtnTrue(${listTar.id},'0')">降锁</li>
                                    <li onclick="clickMoreBtnTrue(${listTar.id},'1')">升锁</li>
                                    <li onclick="clickMoreBtnTrue(${listTar.id},'2')">正常模式</li>
                                    <li onclick="clickMoreBtnTrue(${listTar.id},'3')">升锁模式</li>
                                    <li onclick="clickMoreBtnTrue(${listTar.id},'4')">降锁模式</li>
                                    <li onclick="clickMoreBtnTrue(${listTar.id},'5')">测试模式</li>
                                </ul>
                            </span>
                        ` : ''
                        html = html + '<td>' + editTxt + delteTxt + codeTxt + posTxt + moreBtn + '</td>';
                    } else if (nameTar === 'placeState') {
                        html += '<td>';
                        if(value === 0){
                            html += '<span class="stateHas">空闲</span>';
                        }
                        if(value === 1){
                            html += '<span class="stateNot">已停</span>';
                        }else if(value === 2){
                            html += '<span class="reserve">已预约</span>';
                        }
                        html += '</td>';
                    }else if (nameTar === 'state') {
                        html += '<td>';
                        if (value === 0) {
                            html += '<span class="stateHas">空闲</span>';
                        } else if (value === 1) {
                            html += '<span class="stateNot">占用</span>';
                        } else {
                            html += '<span class="stateNot">位置异常</span>';
                        }
                        html += '</td>';
                    } else if (nameTar === 'networkstate') {
                        if (listTar[nameTar] == '0') {
                            html += '<td class="stateNot">' + '离线' + '</td>';
                        } else if (listTar[nameTar] == '1') {
                            html += '<td class="stateHas">' + '在线' + '</td>';
                        }
                    } else if (nameTar === 'floorLockState') {
                        if (listTar[nameTar] == '0') {
                            html += '<td>' + '降锁' + '</td>';
                        } else if (listTar[nameTar] == '1') {
                            html += '<td>' + '升锁' + '</td>';
                        } else {
                            html += '<td>' + '位置异常' + '</td>';
                        }
                    } else if (nameTar === 'model') {
                        if (listTar[nameTar] == '2') {
                            html += '<td>' + '正常模式' + '</td>';
                        } else if (listTar[nameTar] == '3') {
                            html += '<td>' + '升锁模式' + '</td>';
                        } else if (listTar[nameTar] == '4') {
                            html += '<td>' + '降锁模式' + '</td>';
                        } else {
                            html += '<td>' + '测试模式' + '</td>';
                        }
                    } else if (nameTar === 'power') {
                        html += '<td>' + convertNull(listTar[nameTar] || 0) + '%</td>';
                    } else {
                        html += '<td>' + convertNull(listTar[nameTar]) + '</td>';
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
        url:url + 'vip/delFloorLockInfo/' + ids,
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
    $('#editFrame').attr('src', './floorLockDetails.html?id=' + id) 
    $('#editFrame').show()
}

function addFloorLock() {
    $('#addFrame').attr('style', 'z-index: 99') 
    $('#addFrame').attr('src', './floorLockDetails.html') 
    $('#addFrame').show()
}

function closeFrame(){
    $('#addFrame').hide()
    $('#editFrame').hide()
    $('#cordFrame').hide()
    search()
}

function seeCode(id) {
    $('#cordFrame').attr('style', 'z-index: 99')
    $('#cordFrame').attr('src', './floorLockCode.html?id=' + id)
    $('#cordFrame').show()
};

// 位置显示信息
function showlocation(id) {

    var idmai=id
    // const result = list.find(item => item.id == idmai)1;

    $('#mapPop').show();
    $('#mask').show();

    result = contrastReturn(list,'id',idmai);

    openMap({
        fKey: result.mapKey||'',
        fName: result.appName||'',
        fId: result.fmapID||'',
        focusFloor:Number(result.floor)||'',
        path:result.themeImg || '',
        x:result.x,
        y:result.y,
        num:result.deviceNum,
        //初始指北针的偏移量
        compassOffset: [20, 12],
        //指北针大小默认配置
        compassSize: 48,
        mapId: result.map
    }, function () {
        analyser = new fengmap.FMSearchAnalyser(fMap);
        var x = +$('[name="x"]').val();
        var y = +$('[name="y"]').val();
        if(x && y){
            fMap.moveTo({
                x:x,
                y:y,
                groupID:floor,
            })
        }
        var fid = $('[name="fid"]').val();
        var target = null;
        if(fid){
            target = findModel({FID:fid});
            if(target){
                target.selected = true;
                selectedModel = target;
            }
        }
    });

}

function tabSort(that,sortName){
    switchSort(that,function(sortVal){
        if(sortVal === 'desc'){
            where.desc = sortName+'+0' + ' desc';
        }else{
            where.desc = sortName+'+0';
        }
        where.pageIndex = 1;
        console.log(where);
        init();
    })
}
function tabSort1(that,sortName){
    switchSort(that,function(sortVal){
        if(sortVal === 'desc'){

            where.desc = sortName +'+0' + ' desc';
        }else{
            where.desc = sortName+'+0';
        }
        where.pageIndex = 1;
        console.log(where);
        init();
    })
};

/* 更多操作 */
function openMoreBtn(id) {
    let openMore = $(`#moreBtnUl${id}`).is('.openMore');
    $(".moreBtnUl").removeClass('openMore');
    if (openMore) {
        $(`#moreBtnUl${id}`).removeClass("openMore");
    } else {
        $(`#moreBtnUl${id}`).addClass("openMore");
        document.getElementById(('moreBtnUl' + id)).addEventListener('mouseleave', function (e) {
            $(`#moreBtnUl${id}`).removeClass("openMore");
        });
    }
};

/* 点击按钮 */
function clickMoreBtnTrue(id, type) {
    openMoreBtn(id);
    console.log(id, type);
    console.log('list', list);
    let target = list.find((item) => item.id == id);
    console.log('target', target);
    let data = {
        endTime: target.validEndTime ? target.validEndTime : undefined,
        nedid: target.deviceNum,
        mode: type
    };
    if (!data.endTime) {
        tips('无地锁相关配置,请先配置')
        return;
    };
    $.ajax({
        url: url + 'vip/checkAndCall',
        data,
        type: 'POST',
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res) {
            console.log(res);
            if (res.code == 200) {
                tips(res.message, function () {
                    search();
                }, 5000);
            } else {
                tips(res.message);
            }
        },
        error: function (jqXHR) {
            var status = jqXHR.status;
            if (status == 401) {
                document.write(jqXHR.responseText)
            }
            resError(jqXHR);
        }
    })
};


function exportShow() {
    $('#saveTxt').html('导出地锁数据');
    showPop('#permissPop');
};

function exportBtn() {
    var mapId = $('[name="map1"]').data('val');
    if (!mapId) {
        tips('请先选择地图');
        return;
    }
    var xhr = new XMLHttpRequest();
    xhr.open('POST', url + '/importExportDeviceInfo/exportFloorLockExcel/' + mapId);
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