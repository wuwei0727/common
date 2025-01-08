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
var companyList = [];
var selDataList = [];

$(function () {
    calcHeig();
    $(document).on('click', function (e) {
        hideSele();
    })
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
        url: url + 'vip/getFloorLockInfoInfoById/' + ID,
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
                if(data.map) {
                    $('[name="map"]').eq(0).addClass('batchTxtChange');
                };

                if (data.company) {
                    let findItem = companyList.filter(item => item.map === data.map);
                    console.log('findItem', findItem);
                    let target = companyList.find((item) => {
                        return item.comId === data.company;
                    });

                    console.log('target', target);


                    findItem ? addSelData2(findItem) : '';
                    findItem ? addSelData('#parkingNameSelect', { tarName: "placeName", id: "placeName", txt: "--请选择车位名--" }, undefined, target.list) : '';

                    $('#companyName')[0].innerText = (target.comName);
                    $('#companyName').eq(0).addClass('batchTxtChange');
                } else {
                    let findItem = selPlaceData.find(item => item.mapId === data.map);
                    findItem ? addSelData('#parkingNameSelect', {tarName: "placeName", id: "placeName", txt: "--请选择车位名--"}, undefined, findItem.placeNameList) : '';
                }
                if(data.parkingName) {
                    $('[name="parkingName"]')[0].innerText = (data.parkingName);
                    $('[name="parkingName"]').data('val', data.parkingName);
                    $('[name="parkingName"]').eq(0).addClass('batchTxtChange');
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
            var status = jqXHR.status;
            if(status == 401){
                document.write(jqXHR.responseText)
            }
            resError(jqXHR);
        }
    })
}

//加载下拉数据
function loadSeleData(fn) {
    loadSele('map/getMap2dSel', { pageIndex: 1, pageSize: 20, placeName: 'floorLock' }, '#mapSelect', undefined, undefined, fn, 1);
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
            selDataList = [];
            companyList = [];
            res.data.placeName.forEach((item) => {
                let term = []
                item.placeNameList.forEach((item1) => {
                    if (item1.comId) {
                        let hi = companyList.findIndex((cfItem) => {
                            return cfItem.comId === item1.comId;
                        })
                        if (hi != -1) {
                            companyList[hi].list.push(item1);
                        } else {
                            let d = {
                                comId: item1.comId,
                                comName: item1.comName,
                                map: item.mapId,
                                mapId: item1.comId,
                                list: [item1]
                            };
                            companyList.push(d);
                        }
                    } else {
                        term.push(item1);
                    }
                });
                item.placeNameList = term;
                selDataList.push(item);
            });

            console.log('companyList', companyList);
            console.log('selDataList', selDataList);



            var selData = JSON.stringify(selDataList).replace(/"/g, "'");
            var html = '';
            var curName = temp.tarName || 'mapName';
            var curId = temp.id || 'mapId';
            var curTxt = temp.txt || '--请选择关联地图--';
            if (typeof firstTxt !== "boolean") {
                html = '<div onclick="seleBatch1(this,\'\')" data-id="">' + curTxt + '</div>';
            };
            var list = res.data.placeName || [];
            var firstTxt = {tarName: "placeName", id: "placeName", txt: "--请选择车位名--"};
            for (var i = 0; i < list.length; i++) {
                html += '<div onclick="seleBatch1(this,\'' + list[i][curId] + '\',\'\', ' + selData + ',\'placeNameList\',\'#parkingNameSelect\',' + JSON.stringify(firstTxt).replace(/"/g, "'") + ')" data-id="' + list[i][curId] + '" title="' + list[i][curName] + '">' + list[i][curName] + '</div>';
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

function save() {
    var sendData = getData('.main');
    if (!sendData) {
        return;
    }
    if(!sendData.map) {
        tips('请选择关联地图');
        return;
    } else if(!sendData.deviceNum) {
        tips('请输入设备编号');
        return;
    }
    var path = url;
    if (ID) {
        path += 'vip/editFloorLockInfo';
        sendData.id = ID;
    } else {
        path += 'vip/addFloorLockInfo';

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

// function seleMap(that, id) {
//     seleBatch(that, id);
// }


function seleBatch1(ele, val, fn, selData, selItemList, addom, firstTxt) {
    seleBatch(ele, val, fn, selData, selItemList, addom, firstTxt);
    $('#companyName')[0].innerText = ('--请选择关联公司--');
    $('#companyName').eq(0).removeClass('batchTxtChange');

    /* 公司下拉框 */
    let mapId = val;
    console.log(mapId);

    if (!mapId) return;
    let data = companyList.filter(item => item.map == mapId);
    var selData1 = JSON.stringify(data).replace(/"/g, "'");

    var html = '';
    var curName = 'comName';
    var curId = 'comId';
    var curTxt = '--请选择关联公司--';
    if (true) {
        html = '<div onclick="seleBatch2(this,\'\')" data-id="">' + curTxt + '</div>';
    };
    let firstTxt1 = { tarName: "placeName", id: "placeName", txt: "--请选择关联公司--" };
    for (var i = 0; i < data.length; i++) {
        html += '<div onclick="seleBatch2(this,\'' + data[i][curId] + '\',\'\', ' + selData1 + ',\'list\',\'#parkingNameSelect\',' + JSON.stringify(firstTxt1).replace(/"/g, "'") + ')" data-id="' + data[i][curId] + '" title="' + data[i][curName] + '">' + data[i][curName] + '</div>';
    }
    $("#companyList").html(html);
};

function seleBatch2(ele, val, fn, selData, selItemList, addom, firstTxt) {
    seleBatch(ele, val, fn, selData, selItemList, addom, firstTxt);

    if (!val) {
        let mapId = $("[name='map']").data("val");
        let data = selDataList.find((item) => {
            return item.mapId == mapId
        })
        addSelData('#parkingNameSelect', { tarName: "placeName", id: "placeName", txt: "--请选择车位名--" }, undefined, data.placeNameList)
    }
};

function addSelData2(data){
    var selData1 = JSON.stringify(data).replace(/"/g, "'");

    var html = '';
    var curName = 'comName';
    var curId = 'comId';
    var curTxt = '--请选择关联公司--';
    if (true) {
        html = '<div onclick="seleBatch2(this,\'\')" data-id="">' + curTxt + '</div>';
    };
    let firstTxt1 = { tarName: "placeName", id: "placeName", txt: "--请选择关联公司--" };
    for (var i = 0; i < data.length; i++) {
        html += '<div onclick="seleBatch2(this,\'' + data[i][curId] + '\',\'\', ' + selData1 + ',\'list\',\'#parkingNameSelect\',' + JSON.stringify(firstTxt1).replace(/"/g, "'") + ')" data-id="' + data[i][curId] + '" title="' + data[i][curName] + '">' + data[i][curName] + '</div>';
    }
    $("#companyList").html(html);
};