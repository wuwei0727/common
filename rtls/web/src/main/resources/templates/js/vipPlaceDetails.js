var ID;
var mapList = [];
var currentMapId;//初始化的id，不同则重新初始化地图
//定义搜索分析类
var analyser = null;
//选中的模型
var selectedModel = null;
var mapData = {};
var site = null;
var flag = false;
var selPlaceData;

$(function () {
    calcHeig();
    $(document).on('click', function (e) {
        hideSele();
    })
    initDate();
    var id = getUrlStr('id');
    if (id) {
        ID = id;
        loadSeleData(() => {
            init();
        });
        $('#titleFlag').html('编辑');
    } else {
        loadSeleData();
    }
})
//初始化数据
function init() {
    $.ajax({
        url: url + 'vip/getVipParingSpaceInfoById/' + ID,
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
                mapData = data;
                setData(data, '.main');
                currentMapId = $('[name="map"]').data('val');
                if (data.map) {
                    $('[name="map"]').eq(0).addClass('batchTxtChange');
                    let findItem = selPlaceData.find(item => item.mapId === data.map);
                    findItem ? addSelData('#nameSelect', {tarName: "placeName", id: "placeName", txt: "--请选择车位名--"}, undefined, findItem.placeNameList) : '';
                }
                $('[name="name"]')[0].innerText = (data.name);
                $('[name="name"]').data('val', data.name);
                if (data.name) {
                    $('[name="name"]').eq(0).addClass('batchTxtChange');
                }
                if(data.appointmentSlot) {
                    initDate(data.appointmentSlot);
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
function loadSeleData(fn) {
    loadSele('map/getMap2dSel', { pageIndex: 1, pageSize: 20, placeName: 'placeName', type: "vipPlace" }, '#mapSelect', undefined, undefined, fn, 1);
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
            selPlaceData = res.data.placeName;
            var selData = JSON.stringify(res.data.placeName).replace(/"/g, "'");
            var html = '';
            var curName = temp.tarName || 'mapName';
            var curId = temp.id || 'mapId';
            var curTxt = temp.txt || '--请选择关联地图--';
            if (typeof firstTxt !== "boolean") {
                html = '<div onclick="seleBatch(this,\'\')" data-id="">' + curTxt + '</div>';
            }
            var list = res.data.placeName || [];
            var firstTxt = {tarName: "placeName", id: "placeName", txt: "--请选择车位名--"};
            for (var i = 0; i < list.length; i++) {
                html += '<div onclick="seleBatch(this,\'' + list[i][curId] +  '\',\'\', ' + selData+  ',\'placeNameList\',\'#nameSelect\','+ JSON.stringify(firstTxt).replace(/"/g, "'") +')" data-id="' + list[i][curId] + '" title="' + list[i][curName] + '">' + list[i][curName] + '</div>';
            }
            $(dom).html(html);
            fn ? fn() : '';
        },
        error: function (err) {
            resError(err);
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

function zero(str) {
    return str < 10 ? '0' + str : str;
}

function initDate(appointmentSlot) {

    let minDate = '';
    let curTime = new Date();
    minDate = curTime.getFullYear() + '-' + zero(curTime.getMonth() + 1) + '-' + zero(curTime.getDate());

    if (appointmentSlot) {
        $('#time').val(appointmentSlot);
    }
    jeDate('#time', {
        theme: {
            bgcolor: "#4A60CF",
            pnColor: "#4A60CF"
        },
        minDate,
        multiPane: false,
        range: "至",
        format: "YYYY-MM-DD hh:mm",
        donefun: function () {
        }
    });
}
function padZero(n) {
    return n > 9 ? '' + n : '0' + n;
}
function save() {
    var sendData = getData('.main');
    if (!sendData) {
        return;
    }
    if(!sendData.map) {
        tips('请选择关联地图');
        return;
    } else if(!sendData.name) {
        tips('请选择车位名称');
        return;
    } else if(!sendData.license) {
        tips('请输入车牌号');
        return;
    } else if(!sendData.reservationPerson) {
        tips('请输入预约人');
        return;
    } else if(!sendData.time){
        tips('请选择开始时间和结束时间');
        return;
    } else if(!sendData.phone) {
        tips('请输入手机号码');
        return;
    }  else {
        var reg_phone = /^1[3-9]\d{9}$/;
        if(!reg_phone.test(sendData.phone)){
            tips('请输入正确的手机号码！！！');
            return;
        }
    }
    var arr = sendData.time.split("至");
    if (new Date(arr[0]).getTime() > new Date(arr[1]).getTime()) {
        tips('结束时间必须大于开始时间');
        return
    }
    sendData.startTime = arr[0];
    sendData.endTime = arr[1];
    var path = url;
    if (ID) {
        path += 'vip/editVipParingSpaceInfo';
        sendData.id = ID;
    } else {
        path += 'vip/addVipParingSpaceInfo';

    }
    delete sendData.time;
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