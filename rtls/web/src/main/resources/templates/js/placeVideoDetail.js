var ID;
var mapList = [];
var currentMapId;//初始化的id，不同则重新初始化地图
//定义搜索分析类
var analyser = null;
//选中的模型
var selectedModel = null;
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
        url: url + 'placeVideoDetection/getPlaceVideoDetectionById/' + ID,
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
                console.log(res.data)
                var data = res.data[0];
                setData(data, '.main');
                currentMapId = $('[name="map"]').data('val');
                if(data.map) {
                    $('[name="map"]').eq(0).addClass('batchTxtChange');
                }
            } else {
                tips(res.message);
                if (!res.code) {
                    let newWindow = window.open('about:blank');
                    newWindow.document.write(res);
                    newWindow.focus();
                    window.history.go(-1);
                }
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
        height: win.height() - 40 - 40,
        width: win.width() - 40,
    })
}

function save() {
    var sendData = getData('.main');
    if (!sendData) {
        return;
    }
    if(!sendData.map) {
        tips('请选择关联地图');
        return;
    } else if(!sendData.ip) {
        tips('请输入IP');
        return;
    } else if(!sendData.placeInquireAddress) {
        tips('请输入车位查询地址');
        return;
    } else if(!sendData.licenseInquireAddress) {
        tips('请输入车牌查询地址');
        return;
    }else if (sendData.serviceStatusTime < 2){
        tips('服务状态时间不能低于2分钟');
        return;
    }
    var path = url;
    if (ID) {
        path += 'placeVideoDetection/updatePlaceVideoDetection';
        sendData.id = +ID;
    } else {
        path += 'placeVideoDetection/addPlaceVideoDetection';

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
            resError(jqXHR);
        }
    })
}
function seleMap(that, id) {
    seleBatch(that, id);
}