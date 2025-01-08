var guideID;
var mapList = [];
var currentMapId;//初始化的id，不同则重新初始化地图
var map;//地图id
let mapName;
var isSelect = false; 
var guideData;

$(function () {
    $(document).on('click', function (e) {
        hideSele();
    })
    var id = getUrlStr('id');
    if (id) {
        guideID = id;
        init(() => {
            initMap();
        });
        $('#titleFlag').html('编辑');
    } else {
        initMap();
    }

});

//初始化地图
function initMap() {
    $.ajax({
        url: url + 'map/getMap2dSel',
        data: {
            pageSize: -1,
            enable: 1,
            guideID: guideID
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
                    mapName: target.name || '',
                    themeName: target.themeName || ''
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
        $('.noActiveBtn').removeClass('avtiveSpan');
        activeBtn = '';
        isSelect = false;
        $('[name="map"]').val(id);
        if (currentMapId != id) {
            $('[name="x"]').val('');
            $('[name="y"]').val('');
            $('[name="z"]').val('');
            $('[name="floor"]').val('1');
            $('[name="map"]').val(id);
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
                wantToDo: "selectPoint",
                mapId: id
            });
        } else {
            $('[name="x"]').val(guideData.x);
            $('[name="y"]').val(guideData.y);
            $('[name="z"]').val(guideData.z);
            $('[name="floor"]').val(guideData.floor);
            $('[name="map"]').val(id);
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
                wantToDo: "selectPoint",
                mapId: id
            }, function () {
                addLayer({
                    x: +guideData.x,
                    y: +guideData.y,
                    // num: guideData.devicenum,
                    floor: Number(+guideData.floor)
                });
                fMap.setLevel({
                    level: +guideData.floor
                   
                })
                fMap.setCenter({
                    x: +guideData.x,
                    y: +guideData.y,
                    animate: true
                })
            });
        }
        
    });
}

//初始化数据
function init(callback) {
    $.ajax({
        url: url + 'guideScreenDevice/getGuideScreenDeviceById/' + guideID,
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
                currentMapId = $('[name="map"]').data('val');
                callback();
                guideData = data;
                $('[name="x"]').val(guideData.x)
                $('[name="y"]').val(guideData.y)
                $('[name="floor"]').val(guideData.floor)
                if (data.map) {
                    $('[name="map"]').eq(0).addClass('batchTxtChange');
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
    if (!sendData.map) {
        tips('请选择关联地图');
        return;
    }
    if(!sendData.x) {
        tips('请在地图上选点');
        return;
    }
    if(!sendData.deviceId) {
        tips('请输入设备ID');
        return
    } else {
        if(isNaN(sendData.deviceId)) {
            tips('设备ID只能为数字');
            return
        }
    }

    var path = url;
    if (guideID) {
        path += 'guideScreenDevice/updateGuideScreenDevice';
        sendData.id = guideID;
    } else {
        path += 'guideScreenDevice/addGuideScreenDevice';
    }
    console.log(sendData);
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
            if (res.code == 200) {
                tips(res.message, function () {
                    if (!guideID) {
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

function reload() {
    location.reload()
}
function selectPoint() {
    isSelect = !isSelect;
    if(isSelect) {
        $('.noActiveBtn').addClass('avtiveSpan');
        activeBtn = 'select';
    } else {
        $('.noActiveBtn').removeClass('avtiveSpan');
        activeBtn = '';
    }
}
function mapClickFn(info) {
    $('[name="x"]').val(info.x);
    $('[name="y"]').val(info.y);
    $('[name="floor"]').val(info.floor);
}