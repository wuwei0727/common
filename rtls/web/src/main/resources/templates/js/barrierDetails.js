var ID;
var mapList = [];
var currentMapId;//初始化的id，不同则重新初始化地图
//定义搜索分析类
var analyser = null;
//选中的模型
var selectedModel = null;
var barrierData = {};
var site = null;
var flag = false;

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
    }
})
//初始化数据
function init() {
    $.ajax({
        url: url + 'vip/getBarrierGateInfoInfoById/' + ID,
        beforeSend: function () {
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success:function(res){
            if (!res.code) {
                let newWindow = window.open('about:blank');
                newWindow.document.write(res);
                newWindow.focus();
                window.history.go(-1);
            }else if(res.code === 200){
                var data = res.data;
                barrierData = data;
                setData(data, '.main');
                currentMapId = $('[name="map"]').data('val');
                if(data.map) {
                    $('[name="map"]').eq(0).addClass('batchTxtChange');
                }
            } else {
                tips(res.message);
            }
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}

//加载下拉数据
function loadSeleData() {
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



//处理高度信息
function calcHeig() {
    var win = $(window);
    var fengmap = $('#fengmap');
    fengmap.css({
        height: win.height() - 40 - 52,
        width: win.width() - 40,
    })
}

function save() {
    var sendData = getData('.main');
    var mapId = $('[name="map"]').data('val');
	var x = $('[name="x"]').val();
	var y = $('[name="y"]').val();
    if (!sendData) {
        return;
    }
    if(!sendData.map) {
        tips('请选择关联地图');
        return;
    } else if(!sendData.deviceNum) {
        tips('请输入设备编号');
        return;
    } else if(!sendData.bindingArea) {
        tips('请输入区域名称');
        return;
    } else if(!mapId) {
        tips("请选择关联地图");
        return;
    } else if(!x || !y) {
        tips("请在地图中选点");
        return;
    }
    var path = url;
    if (ID) {
        path += 'vip/editBarrierGateInfo';
        sendData.id = ID;
    } else {
        path += 'vip/addBarrierGateInfo';

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
            if (res.code == 200) {
                tips(res.message, function () {
                    if(ID){
                        init();
                    }else {
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
//选点显示地图
function selectPoint(){
    var mapId = $('[name="map"]').data('val');
    if(!mapId){
        tips('请选择关联地图');
        return;
    }
    $('#mapPop').show();
    $('#mask').show();
    var target = null;
    var floor = +$('[name="floor"]').val();
    if(currentMapId == mapId){
        target = contrastReturn(mapList,'id',mapId);
        openMap({
            fKey: target.mapKey,
            fName: target.appName,
            fId: target.fmapID,
            focusFloor:floor,
            path:target.path,
            //初始指北针的偏移量
            compassOffset: [20, 12],
            //指北针大小默认配置
            compassSize: 48,
            mapId
        }, function () {
            analyser = new fengmap.FMSearchAnalyser(fMap);
            addLayer({
                x: +barrierData.x,
                y: +barrierData.y,
                num: barrierData.deviceNum,
                focusFloor: Number(floor)
            });
            if(barrierData.x && barrierData.y){
                fMap.moveTo({
                    x: +barrierData.x,
                    y: +barrierData.y,
                    groupID: +floor,
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
    }else if(currentMapId != mapId){
        target = contrastReturn(mapList,'id',mapId);
        openMap({
            fKey: target.mapKey,
            fName: target.appName,
            fId: target.fmapID,
            path:target.path,
            //初始指北针的偏移量
            compassOffset: [20, 12],
            //指北针大小默认配置
            compassSize: 48,
            mapId
        }, function () {
            analyser = new fengmap.FMSearchAnalyser(fMap);
        });
    }
}
//地图的回调
function mapClickFn(info,target){
    if(info.nodeType !== fengmap.FMNodeType.MODEL){
        tips('请选择模型');
        return;
    }else {
        tips('已选择地点');
    }
    $('[name="fid"]').val(info.fid);
    $('[name="x"]').val(info.cx);
    $('[name="y"]').val(info.cy);
    $('[name="z"]').val(info.z);
    $('[name="floor"]').val(info.floor);
    if(selectedModel){
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
    $('[name="z"]').val('');
    $('[name="floor"]').val('');
    $('[name="fid"]').val('');
};

function cancelButton(that) {
    document.getElementById("file").value = "";
    var img = document.getElementById("cancelUploadImg");
    photo = '';
    img.src = ''
}

//地图切换
function seleMap(that, id) {
    seleBatch(that, id, function () {
        if (currentMapId != id) {
            $('[name="fid"]').val('');
            $('[name="x"]').val('');
            $('[name="y"]').val('');
            $('[name="z"]').val('');
            $('[name="floor"]').val('1');
            $('[name="map"]').val(id);
        } else {
            $('[name="fid"]').val(barrierData.fid);
            $('[name="x"]').val(barrierData.x);
            $('[name="y"]').val(barrierData.y);
            $('[name="z"]').val(barrierData.z);
            $('[name="floor"]').val(barrierData.floor);
            $('[name="map"]').val(id);
        }
    });
}