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
var polygonColor = ['#009944', '#ff9600', '#d71618'];
var initVertex = [];
var initVertexInfo = [];
var initPlaceNameList = [];

$(function () {
    $(document).on('click', function (e) {
        hideSele();
    })
    var id = getUrlStr('id');
    if (id) {
        recommId = id;
        init((initPoints) => {
            initMap(initPoints);
        });
        $('#titleFlag').html('编辑');
    } else {
        initMap();
    }

});

//初始化地图
function initMap(initPoints) {
    $.ajax({
        url: url + 'map/getMap2dSel',
        data: {
            pageSize: -1,
            enable: 1,
            recommId: recommId
        },
        success: function (res) {
            if (res.code !== 200) {
                tips(res.message);
                return;
            }
            mapList = [];
            var list = res.data;
            var html = '';
            var first = list.find(item => item.name == mapName);
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
                    mapName: target.name || '',
                    themeName: target.themeName || ''
                });
                html += '<div onclick="seleMap(this,\'' + list[i].id + '\')">' + list[i].name + '</div>';
            }
            var mapSelect = $('#mapSelect');
            mapSelect.html(html);
            if (first) {
                mapSelect.prev().html(first.name);
                seleMap(null, first.id, initPoints);
            }
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}
function seleRecommelevel(that, id) {
    seleBatch(that, id);
    if (fMap) {
        initMapEditor(polygonColor[+id - 1], true);
    }
}
//地图切换
function seleMap(that, id, initPoints) {
    var sendData = getData('.main');
    if (!sendData.recommelevel) {
        tips('请先选择优先级别');
        return;
    }
    seleBatch(that, id, function () {
        var resInfo = contrastReturn(mapList, 'id', id);
        if (!resInfo) {
            tips('获取失败，请重试');
            return;
        }
        if (resInfo.type !== 2) {
            tips('地图类型不正确，请重试');
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
            polygonColor: polygonColor[sendData.recommelevel - 1],
            initPoints: initPoints,
            wantToDo: "drawArea",
            mapId: id
        });
    });
}

//初始化数据
function init(callback) {
    $.ajax({
        url: url + 'recommConfig/getRecommConfigInfoById/' + recommId,
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
                    callback ? callback(initPoints) : '';
                }
                if (data.recommelevel) {
                    var recommelevel = data.recommelevel === 3 ? '高' : data.recommelevel === 2 ? '中' : data.recommelevel === 1 ? '低' : '';
                    $('[name="recommelevel"]').eq(0).addClass('batchTxtChange');
                    $('[name="recommelevel"]').data('val', data["recommelevel"]).html(recommelevel);
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
    console.log(sendData);
    var path = url;
    if (recommId) {
        path += 'recommConfig/updateRecommConfig';
        sendData.id = recommId;
        sendData.delplaceids = delPlace.join();
    } else {
        path += 'recommConfig/addRecommConfig';
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
        success: function (res) {
            console.log(res)
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
    };
    delRecomPlace(list, vertexInfo);
}
function reload() {
    location.reload()
}