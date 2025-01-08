var recommId;
var mapList = [];
var currentMapId;//初始化的id，不同则重新初始化地图
//定义搜索分析类
var analyser = null;
//选中的模型
var selectedModel = null;
var placeList = null;
var hasPlace = [];//现有车位
var addPlace = [];//新增车位
var delPlace = [];//删除的车位
var fids = [];
var comData = {};
var map;//地图id
let mapName;
let desc;
var initData;
var placeNameList = [];
var allCheck = false;
var vertexInfo = [];
var polygonColor = ['#d71618', '#ff9600', '#009944'];
var initVertex = [];
var initVertexInfo = [];
var initPlaceNameList = [];
var selScreenData;

$(function () {
    $(document).on('click', function (e) {
        hideSele();
    })
    var id = getUrlStr('id');
    if (id) {
        recommId = id;
        initMap(() => {
            init();
        });
        $('#titleFlag').html('编辑');
    } else {
        initMap();
    }

});

//初始化地图
function initMap(callback) {
    $.ajax({
        url: url + 'map/getMap2dSel',
        data: {
            pageIndex: 1,
            pageSize: 20,
            showScreen: "showScreen"
        },
        success: function (res) {
            if (res.code !== 200) {
                tips(res.message);
                return;
            }
            callback && callback();
            console.log(res)
            mapList = [];
            selScreenData = res.data.showScreen;
            var list = res.data.list;
            var showScreen = res.data.showScreen;
            var selData = JSON.stringify(res.data.showScreen).replace(/"/g, "'");
            var html = '';
            var target = null;
            var curName = 'mapName';
            var curId = 'mapId';
            var curTxt = '--请先选择关联地图--';
            for (var i = 0; i < list.length; i++) {
                target = list[i];
                mapList.push({
                    id: target.id,
                    type: target.type,
                    fmapID: target.fmapID || '',
                    appName: target.appName || '',
                    mapKey: target.mapKey || '',
                    path: target.themeImg || '',
                    mapName: target.name || '',
                    themeName: target.themeName || ''
                });
            }
            var firstTxt = { tarName: "deviceId", id: "guideScreenDeviceId", txt: curTxt };
            for (var i = 0; i < showScreen.length; i++) {
                html += '<div onclick="seleMap(this,\'' + showScreen[i][curId] + '\',\'\', ' + selData + ',\'showScreenList\',\'#deviceSelect\',' + JSON.stringify(firstTxt).replace(/"/g, "'") + ')" data-id="' + showScreen[i][curId] + '" title="' + showScreen[i][curName] + '">' + showScreen[i][curName] + '</div>';
            }
            var mapSelect = $('#mapSelect');
            mapSelect.html(html);
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}
//地图切换
function seleMap(that, id, initPoints, selData, selItemList, addom, firstTxt) {
    seleBatch(that, id, function () {
        var resInfo = contrastReturn(mapList, 'id', +id);
        if (!resInfo) {
            tips('获取失败，请重试');
            return;
        }
        if (recommId) {
            if (resInfo.mapName !== mapName) {
                initPoints = [];
                placeNameList = [];
                vertexInfo = [];
                $('#recomPlaceList').html('');
            } else {
                initPoints = initVertex;
                placeNameList = initPlaceNameList;
                vertexInfo = initVertexInfo;
                var html = '';
                placeNameList.forEach(item => {
                    html += '<span class="recomPlaceItem" onclick="checkRecomPlace(\'' + item.name + '\')"><span class="recomPlaceItemCheck" value="' + item.name + '">√</span><span>' + item.name + '</span></span>';
                });
                $('#recomPlaceList').html(html);
            }
        } else {
            initPoints = [];
            placeNameList = [];
            vertexInfo = [];
            $('#recomPlaceList').html('');
        }
        $('[name="map"]').val(id);
        // 添加公司位置和关联车位颜色显示
        openMap({
            fKey: resInfo.mapKey || '',
            fName: resInfo.appName || '',
            fId: resInfo.fmapID || '',
            focusFloor: Number(resInfo.floor) || '',
            path: resInfo.path || '',
            FMViewMode: fengmap.FMViewMode.MODE_2D,
            themeName: resInfo.themeName || '',
            //初始指北针的偏移量
            compassOffset: [20, 12],
            //指北针大小默认配置
            compassSize: 48,
            polygonColor: polygonColor[0],
            initPoints: initPoints,
            wantToDo: "drawArea",
            mapId: id
        });
    }, selData, selItemList, addom, firstTxt);
}

//初始化数据
function init() {
    $.ajax({
        url: url + 'showScreenConfig/getShowScreenConfigById/' + recommId,
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
                var data = res.data[0];
                setData(data, '.main');
                mapName = data.mapName;
                if (data.map) {
                    $('[name="map"]').eq(0).addClass('batchTxtChange');
                    let findItem = selScreenData.find(item => item.mapId == data.map);
                    findItem ? addSelData('#deviceSelect', { tarName: "deviceId", id: "guideScreenDeviceId", txt: "--请选择设备ID--" }, undefined, findItem.showScreenList) : '';
                }
                if (data.deviceNum && data.guideScreenId) {
                    $('[name="guideScreenId"]').eq(0).addClass('batchTxtChange');
                    $('[name="guideScreenId"]').data('val', data["guideScreenId"]).html(data.deviceNum);
                }
                if (data.placeList) {
                    var arr = data.placeList.split(',');
                    arr.forEach(item => {
                        placeNameList.push({ name: item, check: false });
                    });
                    var html = '';
                    placeNameList.forEach(item => {
                        html += '<span class="recomPlaceItem" onclick="checkRecomPlace(\'' + item.name + '\')"><span class="recomPlaceItemCheck" value="' + item.name + '">√</span><span>' + item.name + '</span></span>';
                    });
                    $('#recomPlaceList').html(html);
                    initPlaceNameList = placeNameList;
                }
                var initPoints = [];
                if (data.vertexInfo) {
                    data.vertexInfo.forEach(item => {
                        let obj = {}, obj1 = {}, tempPointObj = {};
                        let tempPoinArr = [], tempPoinArr1 = [];
                        item.points.forEach(subItem => {
                            obj = { x: subItem.x, y: subItem.y, floor: +subItem.floor };
                            obj1 = { x: subItem.x, y: subItem.y };
                            tempPoinArr.push(obj);
                            tempPoinArr1.push(obj1);
                            tempPointObj = { floor: +subItem.floor, points: tempPoinArr1 };
                        });
                        initPoints.push(tempPoinArr);
                        vertexInfo.push(tempPointObj);
                    });
                    initVertex = initPoints;
                    initVertexInfo = vertexInfo;
                    let first = mapList.find(item => item.mapName === mapName);
                    if (first) {
                        $('#mapSelect').prev().html(first.name);
                        seleMap(null, first.id, initPoints);
                    }
                }
            }
            else {
                tips(res.message);
            }
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}

//保存
function save() {
    var sendData = getData('.main');
    if (!sendData) {
        return;
    }
    var placeList = '';
    placeNameList.forEach(item => {
        placeList === '' ? placeList += item.name : placeList += "," + item.name
    });
    sendData.placeList = placeList;
    vertexInfo.forEach(item => {
        delete item.pm;
    });
    sendData.vertexInfo = vertexInfo;
    if (!sendData.map) {
        tips('请选择关联地图');
        return;
    }
    if(sendData.vertexInfo.length === 0) {
        tips('请在地图上选择区域');
        return;
    }
    var path = url;
    if (recommId) {
        path += 'showScreenConfig/updateShowScreenConfig';
        sendData.id = recommId;
    } else {
        path += 'showScreenConfig/addShowScreenConfig';
    }
    $.ajax({
        url: path,
        data: JSON.stringify(sendData),
        contentType: "application/json",
        type: 'post',
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res,jqXHR) {
            if (res.code == 200) {
                tips(res.message, function () {
                    if (!recommId) {
                        location.reload();
                    }
                }, 1000);
            } else {
                tips(res.message);
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
function addRecomPlace(place, mapPointsArr) {
    mapPointsArr ? vertexInfo = mapPointsArr : '';
    var newPlaceName = [...place, ...placeNameList];
    var obj = {};
    placeNameList = newPlaceName.reduce(function (item, next) {
        obj[next.name] ? "" : obj[next.name] = true && item.push(next);
        return item;
    }, []);
    var html = '';
    placeNameList.forEach(item => {
        html += '<span class="recomPlaceItem" onclick="checkRecomPlace(\'' + item.name + '\')"><span class="recomPlaceItemCheck" value="' + item.name + '">√</span><span>' + item.name + '</span></span>';
    });
    $('#recomPlaceList').html(html);
}
function delRecomPlace(place, mapPointsArr) {
    vertexInfo = mapPointsArr;
    var html = '';
    placeNameList = placeNameList.filter(item => !place.some(subItem => subItem.name === item.name));
    placeNameList.forEach(item => html += '<span class="recomPlaceItem"  onclick="checkRecomPlace(\'' + item.name + '\')"><span class="recomPlaceItemCheck"  value="' + item.name + '">√</span><span>' + item.name + '</span></span>');
    $('#recomPlaceList').html(html);
}
function checkRecomPlace(itemName) {
    var findItem = placeNameList.find(item => item.name === itemName);
    if (findItem) {
        findItem.check = !findItem.check;
        findItem.check ? $('[value="' + itemName + '"]').addClass('recomPlaceItemChecked') : $('[value="' + itemName + '"]').removeClass('recomPlaceItemChecked');
    }
}
function allcheckPlace() {
    allCheck = !allCheck;
    allCheck ? $(".seleAllPlaceLabel").eq(0).addClass('seleAllPlaceCheck') : $(".seleAllPlaceLabel").eq(0).removeClass('seleAllPlaceCheck');
    placeNameList.forEach(item => {
        item.check = allCheck;
        allCheck ? $('[value="' + item.name + '"]').addClass('recomPlaceItemChecked') : $('[value="' + item.name + '"]').removeClass('recomPlaceItemChecked');
    });
}
function delCheckPlace() {
    var list = placeNameList.filter(item => item.check);

    let allLen = placeNameList.length;
    let seleLen = list.length;

    if (allLen == seleLen) {
        mapArr.forEach((item) => {
            item.pm.remove();
        })
        mapArr = [];
        vertexInfo = [];
        placeNameList = [];
    };
    $(".seleAllPlaceLabel").eq(0).removeClass('seleAllPlaceCheck');

    delRecomPlace(list, vertexInfo);
}
function reload() {
    location.reload()
}