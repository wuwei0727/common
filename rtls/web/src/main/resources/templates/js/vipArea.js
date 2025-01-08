var ids;//删除数据的ID
var where = {};
var list = {};
// var barrierGateAreaStr = "";
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
    loadSele('map/getMap2dSel', { pageIndex: 1, pageSize: 20, areaName: 'areaName' }, '#mapSelect', undefined, undefined, undefined, 1);
}
function loadSele(path, where, dom, firstTxt, tarName, fn, a) {
    var temp = null;
    if (typeof firstTxt === 'object') {
        temp = firstTxt;
    } else {
        temp = {
            txt: firstTxt,
            tarName: tarName,
        }
    }
    $.ajax({
        url: url + path,
        data: where,
        type: 'post',
        success: function (res) {
            if (res.code != 200) {
                tips('获取选项失败');
                return;
            }
            selAreaData = res.data.areaName;
            var selData = JSON.stringify(res.data.areaName).replace(/"/g, "'");
            var html = '';
            var curName = temp.tarName || 'mapName';
            var curId = temp.id || 'mapId';
            var curTxt = temp.txt || '--请选择关联地图--';

            var firstTxt = { tarName: "areaName", id: "barrierGateId", txt: "--请选择绑定区域--" };

            if (typeof firstTxt !== "boolean") {
                // html = '<div onclick="seleBatch(this,\'\')" data-id="">' + curTxt + '</div>';
                html = '<div onclick="seleBatch(this,\'' + '' + '\',\'\', ' + '[]' + ',\'areaNameList\', \'#barrierGateId\',' + JSON.stringify(firstTxt).replace(/"/g, "'") + ')" data-id="' + '' + '" title="' + curTxt + '">' + curTxt + '</div>';
            }
            var list = res.data.areaName || [];
            for (var i = 0; i < list.length; i++) {
                html += '<div onclick="seleBatch(this,\'' + list[i][curId] + '\',\'\',' + selData + ',\'areaNameList\',\'#barrierGateId\',' + JSON.stringify(firstTxt).replace(/"/g, "'") + ')" data-id="' + list[i][curId] + '" title="' + list[i][curName] + '">' + list[i][curName] + '</div>';
            }
            $(dom).html(html);
            fn ? fn() : '';
        },
        error: function (err) {
            resError(err);
        }
    })
}
//初始化列表
function init() {
    $.ajax({
        url: url + 'vip/getVipAreaInfo',
        data: where,
        type: 'post',
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res) {
            var tab = $('#tab');
            if (res.code !== 200) {
                tableException(tab, res.message);
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
            if (allChe.prop('checked')) {
                allChe.prop('checked', false);
            }
            if (pageIndex !== data.pageIndex) {
                pageIndex = data.pageIndex;
            }
            var allName = getTheadName(tab.find('thead'));

            var html = '';
            var nameTar = null;
            var listTar = null;
            var value = null;
            var lineNum = (pageIndex - 1) * pageSize + 1;
            for (var i = 0; i < len; i++) {
                listTar = list[i];
                html += '<tr>';
                let timePeriod = listTar['appointmentSlot'].split('至');
                let appointedTime = period(timePeriod[0], timePeriod[1]);
                for (var j = 0; j < allName.length; j++) {
                    nameTar = allName[j];
                    if (nameTar === '') {
                        continue;
                    }
                    value = listTar[nameTar];
                    if (nameTar === 'line') {
                        html += resTabCheck(listTar, lineNum + i, 'id', 'license');
                    } else if (nameTar === 'state') {
                        html += '<td>';
                        if (value === 0) {
                            html += '<span class="stateHas">空闲</span>';
                        }
                        if (value === 1) {
                            html += '<span class="stateNot">已停</span>';
                        } else if (value === 2) {
                            html += '<span class="reserve">已预约</span>';
                        }
                        html += '</td>';
                    } else if (nameTar === 'operating') {
                        var editBtn = document.getElementById("editBtn");
                        var deleteBtn = document.getElementById("deleteBtn");
                        var editTxt = editBtn ? '<span class="tabOper" onclick="detail(\'' + listTar.id + '\')">编辑</span>' : '';
                        var delteTxt = deleteBtn ? '<span class="tabOper deleOpa" onclick="showDele(\'' + listTar.id + '\',\'' + listTar.license + '\')">删除</span>' : '';
                        var posTxt = '<span class="tabOper deleOpa" onclick="showlocation(\'' + listTar.id + '\',\'' + listTar.num + '\',)">位置</span>';
                        var sendTxt = '<span class="deleOpa tabOper tabMessage messageBtn" onclick="sendMessage(\'' + listTar.id + '\',\'' + listTar.phone + '\',\'' + i + '\',\'' + listTar.map + '\')">发送短信</span>';
                        html = html + '<td>' + editTxt + delteTxt + posTxt + sendTxt + '</td>';
                    } else if (nameTar === 'appointedTime') {
                        html += '<td>' + appointedTime + '小时</td>';
                    } else {
                        html += '<td>' + convertNull(listTar[nameTar]) + '</td>';
                    }
                }
                html += '</tr>';
            }
            tab.find('tbody').html(html);
            var htmlStr = "共 <span class='c4A60CF'>" + data.pages + " </span> 页 / <span class='c4A60CF'>" + data.total + " </span>条数据"
            $('[id="total"]').html(htmlStr);
            //生成页码
            initDataPage(pageIndex, data.pages, data.total);
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}
// 重置
function reset() {
    $('#barrierGateId').html('<div onclick="seleBatch(this,\'\')" data-id="">--请先选择关联地图--</div>');
    resetSearch();
}
//单行的删除提示
function showDele(id, txt) {
    ids = id;
    $('#deleTxt').text(txt);
    showPop('#delePop');
}
//多行的删除
function showAllDele() {
    var cheInp = $('#tab tbody').find('input:checked');
    if (!cheInp.length) {
        tips('请选择至少一条数据');
        return;
    }
    var showTxt = '';
    var cheId = '';
    for (var i = 0; i < cheInp.length; i++) {
        showTxt += cheInp[i].getAttribute('data-txt') + '、';
        cheId += cheInp[i].value + ',';
    }
    showTxt = showTxt.slice(0, -1);
    ids = cheId.slice(0, -1);
    $('#deleTxt').html(showTxt);
    showPop('#delePop');
}
//确认删除
function entDele() {
    $.ajax({
        url: url + 'vip/delVipAreaInfo/' + ids,
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res) {
            tips(res.message);
            if (res.code == 200) {
                hidePop('#delePop');
                search();
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
}
//翻页
function turnPage() {
    where.pageIndex = pageIndex;
    init();
}
//编辑
function detail(id) {
    $('#editFrame').attr('style', 'z-index: 99')
    $('#editFrame').attr('src', './vipAreaDetails.html?id=' + id);
    $('#editFrame').show()
}

function addVipArea() {
    $('#addFrame').attr('style', 'z-index: 99')
    $('#addFrame').attr('src', './vipAreaDetails.html');
    $('#addFrame').show()
}

function closeFrame() {
    $('#addFrame').hide()
    $('#editFrame').hide()
    search()
}

// 位置显示商家信息的
function showlocation(id) {

    var idmai = id
    // const result = list.find(item => item.id == idmai)1;

    $('#mapPop').show();
    $('#mask').show();

    result = contrastReturn(list, 'id', idmai);

    openMap({
        fKey: result.mapKey || '',
        fName: result.appName || '',
        fId: result.fmapID || '',
        focusFloor: Number(result.floor) || '',
        path: result.themeImg || '',
        x: result.x,
        y: result.y,
        num: result.vipArea,
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
}
function period(startTime, endTime) {
    startTime = startTime.replace(/\.|\-/g, '/');
    endTime = endTime.replace(/\.|\-/g, '/');
    
    var start = new Date(startTime).getTime();
    var end = new Date(endTime).getTime();
    var result = (end - start) / 1000;
    return (result / 3600).toFixed(1);
}
// 发送短信
function sendMessage(vipAreaId, phone, i, mapId) {
    var templateCode = "SMS_468970016";
    // var templateCode = "137878";
    $.ajax({
        url: url + 'sms/sendSMS',
        data: {
            vipAreaId,
            phone,
            templateCode,
            mapId
        },
        type: 'post',
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res) {
            if (res.code === 200) {
                tips(res.message, null, 3000);
                $('.messageBtn').eq(i).addClass('messageOpa')
                    .html("短信通知已发送，可能会有延后，请耐心等待")
                    .removeClass('tabMessage')
                    .removeAttr('onclick');
            } else {
                tips(res.message, null, 3000);
            }
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}