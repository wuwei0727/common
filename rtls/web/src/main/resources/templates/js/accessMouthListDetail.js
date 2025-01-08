var Id;
var mapList = [];
var currentMapId;//初始化的id，不同则重新初始化地图
//定义搜索分析类
var analyser = null;
//选中的模型
var selectedModel = null;
var mapData = {};
var site = null;
var flag = false;
$(function () {
    calcHeig();
    loadSeleData();
    var id = getUrlStr('id');
    if (id) {
        Id = id;
        init();
    }
})
//初始化数据
function init() {
    $.ajax({
        url: url + 'park/getPlaceExitById/' + Id,
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res) {
            if (!res.code) {
                let newWindow = window.open('about:blank');
                newWindow.document.write(res);
                newWindow.focus();
                window.history.go(-1);
            } else if (res.code === 200) {
                var data = res.data;
                mapData = data;
                setData(data, '.main');
                currentMapId = $('[name="map"]').data('val');
                if (data.map) {
                    $('[name="map"]').eq(0).addClass('batchTxtChange');
                }
                // if (data.type === 0) {
                //     $('[name="type"]').val('地库入口');
                // } else if (data.type === 1) {
                //     $('[name="type"]').val('地库出口');
                // } else {
                //     $('[name="type"]').val('地库出入口');
                // }
                $('[name="type"]')[0].innerText = getCheType(data.type);
                if (data.type || data.type === 0) {
                    $('[name="type"]').eq(0).addClass('batchTxtChange');
                };

                $('[name="accessStatus"]')[0].innerText = getCheAccessStatus(data.accessStatus);
                if (data.accessStatus || data.accessStatus === 0) {
                    $('[name="accessStatus"]').eq(0).addClass('batchTxtChange');
                };

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
}

function getCheType(num) {
    if (num == '0') {
        return '入口'
    } else if (num == '1') {
        return '出口'
    } else if (num == '2') {
        return '出入口'
    } else if (num == '3') {
        return '地库出入口'
    } else {
        return ''
    }
}

function getCheAccessStatus(num) {
    if (num == '0') {
        return '禁行'
    } else if (num == '1') {
        return "通行"
    } else {
        return ''
    }
}

//加载下拉数据-关联地图
function loadSeleData() {
    $.ajax({
        url: url + 'map/getMap2dSel',
        data: {
            pageSize: -1,
            enable: 1,
            // companyId: companyId
        },
        success: function (res) {
            if (res.code !== 200) {
                tips(res.message);
                return;
            }
            mapList = [];
            var list = res.data;
            var html = '';
            // mapName = res.data[0].mapName;
            var first = list.find(item => item.id == currentMapId);
            // var first = list[0]
            var target = null;
            for (var i = 0; i < list.length; i++) {
                target = list[i];
                mapList.push({
                    id: target.id,
                    type: target.type,
                    fmapID: target.fmapID || '',
                    appName: target.appName || '',
                    mapKey: target.mapKey || '',
                    path: target.themeImg || '',
                    mapName: target.name || ''
                });
                html += '<div onclick="seleMap(this,\'' + list[i].id + '\')">' + list[i].name + '</div>';
            }
            var mapSelect = $('#mapSelect');
            mapSelect.html(html);
            if (first) {
                mapSelect.prev().html(first.name);
                seleMap(null, first.id);
            }
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}

//选点显示地图
function selectPoint() {
    var mapId = $('[name="map"]').data('val');
    if (!mapId) {
        tips('请选择关联地图');
        return;
    }
    $('#mapPop').show();
    $('#mask').show();
    var target = null;
    var floor = +$('[name="floor"]').val();
    if (!Id) {
        currentMapId = undefined;
    }
    if (currentMapId == mapId) {
        target = contrastReturn(mapList, 'id', mapId);
        openMap({
            fKey: target.mapKey,
            fName: target.appName,
            fId: target.fmapID,
            focusFloor: floor,
            path: target.path,
            //初始指北针的偏移量
            compassOffset: [20, 12],
            //指北针大小默认配置
            compassSize: 48,
            mapId
        }, function () {
            analyser = new fengmap.FMSearchAnalyser(fMap);
            site = addLayer({
                x: +mapData.x,
                y: +mapData.y,
                num: mapData.name,
                focusFloor: Number(floor)
            });
            if (mapData.x && mapData.y) {
                fMap.moveTo({
                    x: +mapData.x,
                    y: +mapData.y,
                    groupID: +floor,
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
    } else if (currentMapId != mapId) {
        target = contrastReturn(mapList, 'id', mapId);
        console.log('target', target);
        openMap({
            fKey: target.mapKey,
            fName: target.appName,
            fId: target.fmapID,
            path: target.path,
            //初始指北针的偏移量
            compassOffset: [20, 12],
            //指北针大小默认配置
            compassSize: 48,
            mapId
        }, function () {
            analyser = new fengmap.FMSearchAnalyser(fMap);
        });
        flag = true;
    }
}

//地图的回调
function mapClickFn(info, target) {
    tips('已选择地点');
    var mapId = $('[name="map"]').data('val');
    if (currentMapId != mapId) {
        if (flag) {
            site = addLayer({
                x: +info.x,
                y: +info.y,
                num: Id ? mapData.name : '',
                focusFloor: Number(info.floor)
            });
        } else {
            addLayer({
                x: +info.x,
                y: +info.y,
                num: Id ? mapData.name : '',
                focusFloor: Number(info.floor)
            }, site);
        }

        if (!Id) {
            currentMapId = mapId;
        }

    } else {
        addLayer({
            x: +info.x,
            y: +info.y,
            num: Id ? mapData.name : '',
            focusFloor: Number(info.floor)
        }, site);
    }
    flag = false;
    $('[name="x"]').val(info.x);
    $('[name="y"]').val(info.y);
    $('[name="floor"]').val(info.floor);
    $('[name="fid"]').val(info.fid || '');
    if (selectedModel) {
        selectedModel.selected = false;
    }
    //染色
    target.selected = true;
    selectedModel = target;
}

/* 确定 */
function mapConfirm() {
    hidePop('#mapPop')
};

/* 取消 */
function mapCancel(){
    hidePop('#mapPop');
    tips('已取消选择的地点');
    $('[name="x"]').val('');
    $('[name="y"]').val('');
    $('[name="floor"]').val('');
    $('[name="fid"]').val('');
};

//处理高度信息
function calcHeig() {
    var win = $(window);
    var fengmap = $('#fengmap');
    fengmap.css({
        height: win.height() - 40 - 52,
        width: win.width() - 40,
    })
}

//地图切换  
function seleMap(that, id) {
    seleBatch(that, id, function () {
        if (currentMapId != id) {
            $('[name="map"]').val(id);
        } else {
            $('[name="map"]').val(id);
        }
    });
}

function save() {
    var sendData = getData('.main');

    if (!sendData) {
        return;
    };
    if (!sendData.name) {
        tips('请输入名称');
        return;
    }
    if (!sendData.map) {
        tips('请选择关联地图');
        return;
    };
    if (!sendData.x || !sendData.y) {
        tips('请在地图中选择模型，进行选点');
        return;
    }
    if (sendData.type === '') {
        tips('请选择出入口类型');
        return;
    }
    if (sendData.accessStatus === '') {
        tips('请选择交通状态');
        return;
    };
    sendData.accessStatus = +sendData.accessStatus;
    sendData['z'] = 0;
    var path = url;
    if (Id) {
        path += '/park/editPlaceExitById';
        sendData.id = Id;
    } else {
        path += '/park/addPlaceExit';
    }

    console.log('sendData', sendData);

    $.ajax({
        url: path,
        data: JSON.stringify(sendData),
        type: 'post',
        headers: {
            "Content-Type": "application/json;charset=UTF-8"
        },
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res) {
            if (res.code == 200) {
                tips(res.message, function () {
                    if (!Id) {
                        location.reload();
                    }
                }, 1000);
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
}
