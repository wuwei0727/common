var recommId;
var ID;
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
var mapId;
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

var initCameraVertexInfo;
var cameraVertexInfo;

$(function () {
    $(document).on('click', function (e) {
        hideSele();
    })
    var id = getUrlStr('id');
    if (id) {
        recommId = id;
        ID = id;
        initMap(() => {
            init();
        });
        $('#titleFlag').html('编辑');
    } else {
        initMap();
    }

    $("#r").on("change", function () {
        var val = $(this).val();

        if (val && pointCricle) {
            let info = {};
            info.x = $("[name='x']").val();
            info.y = $("[name='y']").val();
            info.floor = $("[name='floor']").val();
            info.r = val;
            addCircleLayer(info);
        }
    })
});

//初始化地图
function initMap(callback) {
    $.ajax({
        url: url + 'map/getMap2dSel',
        data: {
            pageSize: -1,
            enable: 1,
        },
        success: function (res) {
            if (res.code !== 200) {
                tips(res.message);
                return;
            }
            callback && callback();
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

//地图切换
function seleMap(that, id) {
    console.log(123);

    seleBatch(that, id, function () {
        var resInfo = contrastReturn(mapList, 'id', +id);
        if (!resInfo) {
            tips('获取失败，请重试');
            return;
        }
        if (recommId) {
            if (resInfo.mapName !== mapName) {
                cameraVertexInfo = [];
                cameraAreaData = [];
                cameraAreaId = '';
            } else {
                cameraVertexInfo = initCameraVertexInfo;
                cameraAreaData = JSON.parse(initCameraVertexInfo);
            }
        } else {
            cameraVertexInfo = [];
            cameraAreaData = [];
            cameraAreaId = '';
        }
        $('[name="map"]').val(id);

        if (recommId) {
            if (mapId != id) {
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
                    initPoints: [],
                    wantToDo: "all",
                    mapId: id,
                    noView: false,
                });
            } else {
                let info = JSON.parse(initCameraVertexInfo);
                let ainfo = calculatePolygonCentroid(info);
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
                    initPoints: [],

                    initCameraArea: cameraVertexInfo,
                    cArea: 'add',

                    wantToDo: "all",
                    mapId: id,
                    noView: true,
                }, function () {
                    fMap.setLevel({
                        level: +info[0].floor

                    })
                    fMap.setCenter({
                        x: +ainfo.x,
                        y: +ainfo.y,
                        animate: true
                    })
                });
            }
        } else {
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
                initPoints: [],
                wantToDo: "all",
                mapId: id,
                noView: false,
            });
        }
    });
}

//初始化数据
function init() {
    $.ajax({
        url: url + 'qrCodeLocation/getQrCodeLocationById/' + recommId,
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
                initData = data;
                setData(data, '.main');
                mapId = data.map;
                mapName = data.mapName;
                if (data.map) {
                    $('[name="map"]').eq(0).addClass('batchTxtChange');
                }

                if (data.areaInfo) {
                    initCameraVertexInfo = data.areaInfo
                    cameraAreaData = JSON.parse(data.areaInfo);
                }
                let first = mapList.find(item => item.id == mapId);
                if (first) {
                    $('#mapSelect').prev().html(first.name);
                    seleMap(null, first.id, []);
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
    console.log(sendData);

    if (!sendData) {
        return;
    }

    sendData.areaInfo = JSON.stringify(cameraAreaData);

    if (!sendData.areaName) {
        tips('请输入区域名称')
        return;
    }

    if (!sendData.map) {
        tips('请选择关联地图');
        return;
    }

    if (!sendData.areaInfo || sendData.areaInfo == '[]') {
        tips('请在地图上选择区域');
        return;
    }

    let floor = cameraAreaData[0].floor;
    sendData.floor = floor;

    console.log('sendData', sendData);

    var path = url;
    var method = 'post';
    if (recommId) {
        path += 'qrCodeLocation/editQrCodeLocation';
        sendData.id = recommId;
        method = 'put'
    } else {
        path += 'qrCodeLocation/addQrCodeLocation';
        method = 'post'
    }
    $.ajax({
        url: path,
        data: JSON.stringify(sendData),
        contentType: "application/json",
        type: method,
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res, jqXHR) {
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
            if (status == 401) {
                document.write(jqXHR.responseText)
            }
            resError(jqXHR);
        }
    })
}

function reload() {
    location.reload()
};

/* 框选 */
function drawOpetion1(type) {
    if (type == 'vpolygon' && cameraAreaId) {
        tips('已经存在区域，请编辑或删除后再添加');
        return;
    }
    CLICKTYPE = 'drawArea';
    drawOpetion(type);
};

var cameraAreaData = [];
var cameraAreaId = '';
/* 摄像头的区域 */
function initCameraArea(points, level, id) {
    cameraAreaId = id;
    points.forEach((item) => {
        item.floor = level;
    })
    cameraAreaData = points;
};

function addCameraAreaId(id) {
    cameraAreaId = id;
};

/* 计算多边形的中心点 */
function calculatePolygonCentroid(points) {
    let sumX = 0;
    let sumY = 0;
    let numPoints = points.length;

    for (let i = 0; i < numPoints; i++) {
        sumX += points[i].x;
        sumY += points[i].y;
    }

    // 计算平均值
    let centerX = sumX / numPoints;
    let centerY = sumY / numPoints;

    return { x: centerX, y: centerY };
};