var ids;//删除数据的ID
var where = {};
var list = {};
var polygonColor = ['#d71618', '#ff9600', '#009944'];
var mapList = [];
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
    loadFun('map/getMap2dSel',{pageSize:-1,enable:1},'#mapSelect', undefined,undefined,(list) => {
        mapList = list || [];
    });
}


function reset() {
    resetSearch();
}
//初始化列表
function init() {
    $.ajax({
        url: url + 'guideScreenDevice/getAllGuideScreenDeviceOrConditionQuery',
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
                for (var j = 0; j < allName.length; j++) {
                    nameTar = allName[j];
                    if (nameTar === '') {
                        continue;
                    }
                    value = listTar[nameTar];
                    if (nameTar === 'line') {
                        html += resTabCheck(listTar, lineNum + i, "id", "deviceId");
                    } else if (nameTar === 'operating') {
                        var editBtn = document.getElementById("editBtn");
                        var deleteBtn = document.getElementById("deleteBtn");
                        var editTxt = editBtn ? '<span class="tabOper" onclick="detail(\'' + listTar.id + '\')">编辑</span>' : '';
                        var delteTxt = deleteBtn ? '<span class="tabOper deleOpa" onclick="showDele(\'' + listTar.id + '\',\'' + listTar.deviceId + '\')">删除</span>' : '';
                        var posTxt = '<span class="tabOper deleOpa" onclick="showlocation(\'' + listTar.id + '\',\'' + listTar.deviceId + '\',)">位置</span>';
                        html = html + '<td>' + editTxt + delteTxt + posTxt + '</td>';
                    } else if (nameTar === 'networkStatus') {
                        html += '<td>';
                        if(value === 0){
                            html += '<span class="stateNot">离线</span>';
                        }
                        if(value === 1){
                            html += '<span class="stateHas">在线</span>';
                        }
                        html += '</td>';
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
        url: url + 'guideScreenDevice/delGuideScreenDevice/' + ids,
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
            if(status == 401){
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
    $('#editFrame').attr('src', './guideScreenDetail.html?id=' + id)
    $('#editFrame').show()
}

function addRecommend() {
    $('#addFrame').attr('style', 'z-index: 99')
    $('#addFrame').attr('src', './guideScreenDetail.html')
    $('#addFrame').show()
}

function closeFrame() {
    $('#addFrame').hide()
    $('#editFrame').hide()
    $('html').attr('style', 'overflow:auto')
    search()
}

// 位置显示推荐 （需要传优先级别、顶点数据）
function showlocation(id) {
    $('#mapPop').show();
    $('#mask').show();
    if (id) {
        $.ajax({
            url: url + 'guideScreenDevice/getGuideScreenDeviceById/' + id,
            beforeSend: function () {
                loading();
            },
            complete: function () {
                removeLoad();
            },
            success: function (res) {
                console.log(res)
                if (res.code == 200) {
                    var data = res.data[0];
                    var findItem = mapList.find(item => data.map === item.id);
                    console.log(findItem);
                    if (data.x) {
                        openMap({
                            fKey: findItem.mapKey || '',
                            fName: findItem.appName || '',
                            fId: findItem.fmapID || '',
                            focusFloor: Number(data.floor) || '',
                            path: findItem.path || '',
                            FMViewMode: fengmap.FMViewMode.MODE_2D,
                            themeName: findItem.themeName || '',
                            //初始指北针的偏移量
                            compassOffset: [20, 12],
                            //指北针大小默认配置
                            compassSize: 48,
                            wantToDo: "selectPoint",
                            mapId: findItem.id
                        }, function () {
                            addLayer({
                                x: +data.x,
                                y: +data.y,
                                num: data.deviceId+'',
                                floor: Number(data.floor)
                            });
                            fMap.setLevel({
                                level: +data.floor
                            });
                            fMap.setCenter({
                                x: +data.x,
                                y: +data.y,
                                animate: true
                            });
                        });
                    }
                }
            }
        })
    }
}