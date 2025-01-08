
var ID;
var elevatorId;
var mapId;
var currentMapId;//初始化的id，不同则重新初始化地图
var mapList = [];
//定义搜索分析类
var analyser = null;
//选中的模型
var selectedModel = null;
var mapData = {};
var site = null;
var flag = false;
var hadCompanyId;
var hadCompanyName;

$(function () {
    calcHeig();

    $(document).on('click', function (e) {
        hideSele();
    })
    loadSeleData();
    var id = getUrlStr('id');
    if (id) {
        ID = id;
        init();
        $('#titleFlag').html('编辑');
    } else {
        // getelevatorlist(); // 自定义电梯下拉框
    }
})
//加载下拉数据
function loadSeleData() {
    loadMapSeleData();
    initTypeSelce();
}

// 关联地图
function loadMapSeleData() {
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
            var first = list.find(item => item.id == currentMapId);
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

// 车位类型下拉选择
function initTypeSelce() {
    var typeArr = [0, 1, 2, 3, 4, 5, 6, 7];
    var html = '';
    for (var i = 0; i < typeArr.length; i++) {
        var typeName = getCheType(typeArr[i]);
        html += '<div onclick="seleBatch(this,\'' + (+typeArr[i]) + '\')">' + typeName + '</div>';
    }
    var type = $('#type');
    type.html(html);
};

// 自定义电梯下拉框
function getelevatorlist() {
    $.ajax({
        url: url + 'peb/getParkingElevatorBinding',
        data: {
            pageSize: -1,
            pageIndex: 0,
            map: mapId
        },
        success: function (res) {
            if (res.code !== 200) {
                tips(res.message);
                return;
            }
            var list = res.data;
            var html = '<div onclick="seleelEvator(this,\'' + null + '\', \'' + null + '\', \'' + null + '\', \'' + null + '\')">' + '--请选择--' + '</div>';
            var first = list.find(item => item.id == elevatorId);
            var target = null;
            for (var i = 0; i < list.length; i++) {
                target = list[i];

                html += '<div onclick="seleelEvator(this,\'' + list[i].id + '\', \'' + list[i].name + '\', \'' + list[i].floor + '\', \'' + list[i].building + '\')">' + list[i].name + '</div>';
            }
            var mapSelect = $('#elevatorId');
            mapSelect.html(html);
            if (first) {
                mapSelect.prev().html(first.name);
                seleelEvator(null, first.id, first.name, first.floor, first.building);
            }
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}

//初始化数据
function init() {
    $.ajax({
        url: url + 'park/getPlaceById/' + ID,
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
            } else if (res.code == 200) {
                var data = res.data;

                console.log('dataaaa', data);

                mapData = data;
                mapId = data.map;
                getelevatorlist(); // 自定义电梯下拉框

                var typeArr = data.parkingType.split(",");
                var html = '';
                for (var i = 0; i < typeArr.length; i++) {
                    var typeName = getCheType(typeArr[i]);
                    html += '<div onclick="seleBatch(this,\'' + (+typeArr[i]) + '\')">' + typeName + '</div>';
                }
                var type = $('#type');
                type.html(html);
                setData(data, '.main');
                currentMapId = $('[name="map"]').data('val');
                if (data.map) {
                    $('[name="map"]').eq(0).addClass('batchTxtChange');
                }

                if (data.company) {
                    hadCompanyId = data.company;
                    hadCompanyName = data.companyName;
                    $('[name="company"]').eq(0).addClass('batchTxtChange');
                    $('[name="company"]')[0].innerText = data.companyName;
                }

                elevatorId = $('[name="elevatorId"]').data('val');

                var state = data.state;
                var stateTxt = '';
                if (state == 1) {
                    stateTxt = '已停';
                } else if (state == 2) {
                    stateTxt = '已预约';
                } else {
                    stateTxt = '空闲';
                }
                var carbittypeTxt = '';
                if (data.carbittype || data.carbittype == 0) {
                    $('[name="carbittype"]').eq(0).addClass('batchTxtChange');
                    if (data.carbittype == 0) {
                        carbittypeTxt = '否';
                    } else if (data.carbittype == 1) {
                        carbittypeTxt = '是';
                    }
                    $('[name="carbittype"]')[0].innerText = carbittypeTxt;
                }
                if (data.isReservable || data.isReservable == 0) {
                    let text = ''
                    $('[name="isReservable"]').eq(0).addClass('batchTxtChange');
                    if (data.isReservable == 0) {
                        text = '否';
                    } else if (data.isReservable == 1) {
                        text = '是';
                    }
                    $('[name="isReservable"]')[0].innerText = text;
                }
                $('[name="stateName"]').val(stateTxt);
                $('[name="type"]')[0].innerText = getCheType(data.type);
                if (data.type || data.type === 0) {
                    $('[name="type"]').eq(0).addClass('batchTxtChange');
                };
                $('[name="state"]')[0].innerText = getCheState(data.state);
                if (data.state || data.state === 0) {
                    $('[name="state"]').eq(0).addClass('batchTxtChange');
                };
                $('[name="configWay"]')[0].innerText = getCheConfigWay(data.configWay);
                if (data.configWay || data.configWay === 0) {
                    $('[name="configWay"]').eq(0).addClass('batchTxtChange');
                };
                if (data.elevatorName) {
                    $('[name="elevatorId"]')[0].innerText = data.elevatorName;
                    $('[name="elevatorId"]').eq(0).addClass('batchTxtChange');
                }
            } else {
                tips(res.message);
            }
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}
function getCheType(type) {
    var typeName = type == '0' ? '普通车位' :
        type == '1' ? '充电车位' :
            type == '2' ? '专属车位' :
                type == '3' ? '无障碍车位' :
                    type == '4' ? '超宽车位' :
                        type == '5' ? '子母车位' :
                            type == '6' ? '小型车位' : '';
    return typeName;
}
function getCheState(state) {
    var stateName = state == '0' ? '空闲' :
        state == '1' ? '占用' :
            state == '3' ? '未检' : '';
    return stateName
}

function getCheConfigWay(num) {
    var text = num == '1' ? '超声检测' :
        num == '2' ? '视频检测' :
            num == '3' ? '超声检测加视频检测' : '无';
    return text
}
//保存
function save() {
    var sendData = getData('.main');

    console.log('sendData', sendData);

    if (!sendData) {
        return;
    };
    if (sendData.elevatorId == "null") {
        sendData['elevatorId'] = 0;
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
    if(!(sendData.isReservable || sendData.isReservable == 0)) {
        tips('请选择是否可预约');
        return;
    }
    var path = url;
    if (ID) {
        path += 'park/updatePlace';
        sendData.id = ID;
    } else {
        path += 'park/addPlace';
    }
    delete sendData.elevatorFloor
    delete sendData.building

    console.log('sendData', sendData);

    $.ajax({
        url: path,
        data: sendData,
        type: 'get',
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res) {
            if (res.code == 200) {
                tips(res.message, function () {
                    if (!ID) {
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

// 绑定电梯
function seleelEvator(that, id, name, floor, building) {
    console.log('绑定电梯', id, name, floor, building);
    $('[name="building"]').val(building != 'null' ? building : '');
    $('[name="elevatorFloor"]').val(floor != 'null' ? floor : '');
    $('[name="elevatorId"]').val(id != 'null' ? id : '');


    seleBatch(that, id, function () {
        $('[name="elevatorName"]').val(id != 'null' ? id : '');
    });
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
    if (!ID) {
        currentMapId = undefined;
    }
    if (currentMapId == mapId) {
        target = contrastReturn(mapList, 'id', mapId);
        console.log('target', target);
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

//地图切换  
function seleMap(that, id) {
    mapId = id;
    getelevatorlist();
    $('[name="company"]').eq(0).removeClass('batchTxtChange');
    $('[name="company"]').data('val', '');
    $('[name="company"]')[0].innerText = '--请选择先选择地图--';

    loadFun('park/getCompany', { pageIndex: 0, pageSize: -1, map: id }, '#companySelect', '--请选择关联公司--', 'name', function () {
        if (hadCompanyId) {
            $('[name="company"]').data('val', hadCompanyId);
            $('[name="company"]').eq(0).addClass('batchTxtChange');
            $('[name="company"]')[0].innerText = hadCompanyName;
        };
    });

    seleBatch(that, id, function () {
        if (currentMapId != id) {
            $('[name="map"]').val(id);
        } else {
            $('[name="map"]').val(id);
        }
    });
}

//处理高度信息
function calcHeig() {
    var win = $(window);
    var fengmap = $('#fengmap');
    fengmap.css({
        height: win.height() - 40 - 52,
        width: win.width() - 40,
    })
}

//地图的回调
function mapClickFn(info, target) {
    console.log(info);
    tips('已选择地点');
    var mapId = $('[name="map"]').data('val');
    if (currentMapId != mapId) {
        if (flag) {
            site = addLayer({
                x: +info.x,
                y: +info.y,
                num: ID ? mapData.name : '',
                focusFloor: Number(info.floor)
            });
        } else {
            addLayer({
                x: +info.x,
                y: +info.y,
                num: ID ? mapData.name : '',
                focusFloor: Number(info.floor)
            }, site);
        }

        if (!ID) {
            currentMapId = mapId;
        }

    } else {
        addLayer({
            x: +info.x,
            y: +info.y,
            num: ID ? mapData.name : '',
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
};

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