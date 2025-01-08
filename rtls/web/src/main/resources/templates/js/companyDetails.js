var companyId;
var mapList = [];
var currentMapId;//初始化的id，不同则重新初始化地图
//定义搜索分析类
var analyser = null;
//选中的模型
var selectedModel = null;
var placeList = null;
var hasPlace = [];//现有车位
var hadPlaceFidList = [];//已有车位的fid
var addPlace = [];//新增车位
var delPlace = [];//删除的车位
var fids = [];
var comData = {};
var map;//地图id
let mapName;
let desc;
var initData;
// var list={};
$(function(){
    calcHeig();
    $(document).on('click',function(e){
        hideSele();
    })
    var id = getUrlStr('id');
    if(id){
        companyId = id;
        init();
        $('#titleFlag').html('编辑');
    }
    //1
    initMap();
    placeList = $('#placeList');
    placeList.on('click','.placeItem',function(e){
        var target = $(e.target);
        var data = target.data();
        var id = data.id;
        var fid = data.fid;
        if (target.hasClass('curPlace')) {
            //取消的情况
            findIdAndRemove(addPlace,id,'remove');
            if(findIdAndRemove(hasPlace,id)){
                //在保存前的原有车位才要添加
                delPlace.push(id);
            }
            target.removeClass('curPlace');
            findIdAndRemove(hadPlaceFidList, fid, 'remove');
        } else {
            findIdAndRemove(delPlace, id, 'remove');
            addPlace.push(id);
            target.addClass('curPlace');
            hadPlaceFidList.push(fid);
        };
        addPlaceListColor(hadPlaceFidList);
        moveModel(data.fid);
    })
})
//处理高度信息
function calcHeig(){
    var win = $(window);
    var fengmap = $('#fengmap');
    var high = win.height() - 48 - 40 - 40 * 2 - $('.mainBox').height() - 138 - 10 - 2;
    if(high){
        high = Math.max(300,high);
    }
    fengmap.css({
        height:high,
        width:win.width() - 40 - 30 * 2 - 2,
    })
}
//初始化地图
function initMap(){
    $.ajax({
        url: url + 'map/getMap2dSel',
        data:{
            pageSize:-1,
            enable:1,
            companyId:companyId
        },
        success: function(res) {
            if(res.code !== 200){
                tips(res.message);
                return;
            }
            mapList = [];
            var list = res.data;
            var html = '';
            mapName = res.data[0].mapName;
            var first = list.find(item => item.name == mapName);
            // var first = list[0]
            var target = null;
            for(var i = 0;i < list.length;i++){
                target = list[i];
                mapList.push({
                    id:target.id,
                    type:target.type,
                    fmapID:target.fmapID || '',
                    appName:target.appName || '',
                    mapKey:target.mapKey || '',
                    path:target.themeImg || '',
                    mapName:target.name || ''
                });
                html += '<div onclick="seleMap(this,\'' + list[i].id + '\')">' + list[i].name + '</div>';
            }
            var mapSelect = $('#mapSelect');
            mapSelect.html(html);
            if(first){
                mapSelect.prev().html(first.name);
                seleMap(null,first.id);
            }
        },
        error:function(jqXHR){
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
        $('[name="map"]').val(id);
        if (mapName !== resInfo.mapName) {
            $('[name="fid"]').val('');
            $('[name="x"]').val('');
            $('[name="y"]').val('');
            $('[name="z"]').val('');
            $('[name="floor"]').val('1');
        }
        searchPlace(id);
        selectedModel = '';
        // 添加公司位置和关联车位颜色显示
        openMap({
            fKey: resInfo.mapKey || '',
            fName: resInfo.appName || '',
            fId: resInfo.fmapID || '',
            focusFloor: Number(resInfo.floor) || '',
            path: resInfo.path || '',
            //初始指北针的偏移量
            compassOffset: [20, 12],
            //指北针大小默认配置
            compassSize: 48,
            mapId: id
        }, function () {
            analyser = new fengmap.FMSearchAnalyser(fMap);
            if(mapName === resInfo.mapName) {
                    addLayer({
                        x: +comData.x,
                        y: +comData.y,
                        num: comData.name,
                        focusFloor: Number(comData.floor)
                    });
                    var x = +comData.x;
                    var y = +comData.y;
                    var floor = +comData.floor;
                    if (x && y) {
                        fMap.moveTo({
                            x: x,
                            y: y,
                            groupID: floor,
                        })
                    }
                    var fid = comData.fid;
                    var target = null;
                    if (fid) {
                        target = findModel({ FID: fid });
                        if (target) {
                            target.selected = true;
                            selectedModel = target;
                        }
                    }
            }


            for (var i = 0; i < fids.length; i++) {
                var model = findModel({ FID: fids[i].fid });
                if (model) {
                    setModelRender(model, resStateColor(fids[i].state));
                }
            }

        });
    });
}

//返回状态相对应的颜色
function resStateColor(state) {
    switch (state) {
        case 1:
            //已停
            return '#FFB145';
        case 2:
            //预约
            return '#cdf408';
        default:
            return '';
    }
}

//查询过滤车位
function searchPlace(mapId){
    map=mapId;//地图id
    $.ajax({
        url:url + 'park/getPlace',
        type:'post',
        data:{
            map:mapId,
            desc:'name',
        },
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success:function(res){
            if(res.code === 200){
                var data = res.data;
                var len = data.length;
                if(!len){
                    tips('当前地图的车位为空');

                }
                var html = '';
                var target = null;

                for(var i = 0;i < len;i++){
                    target = data[i];
                    html += '<div class="placeItem';
                    if(target.company){
                        if(target.company == companyId){
                            //当前车位
                            html += ' curPlace';
                            hasPlace.push(target.id);

                            hadPlaceFidList.push(target.fid)
                        } else {
                            //其他公司车位
                            html += ' otherPlace';
                        }
                    }
                    html += '" data-id="' + target.id + '" title="上次所分配的公司：' + (target.companyName || '') + '" data-fid="' + (target.fid || '') + '">' + target.name + '</div>';
                }
                placeList.html(html);
            }else{
                tips(res.message);
            }
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
//初始化数据
function init(){
    console.log("init1")
    $.ajax({
        url:url + 'park/getCompanyById/' + companyId,
        beforeSend:function(){
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
            }else if(res.code == 200){
                var data = res.data;
                //改
                mapName = res.data.mapName;
                comData = res.data.data[0];
                fids = res.data.list;
                initData = comData;
                setData(data, '.main');
                $('[name="fid"]').val(comData.fid);
                $('[name="x"]').val(comData.x);
                $('[name="y"]').val(comData.y);
                $('[name="z"]').val(comData.z);
                $('[name="floor"]').val(comData.floor);
                $('[name="map"]').val(comData.map);
                if(comData.map) {
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
//保存
function save() {
    // var comNameReg = /^[\u4E00-\u9FA5]+$/;
    var managerReg = /^[a-zA-z\u4E00-\u9FA5]+$/;
    var phoneReg = /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/;
    var sendData = getData('.main');
    if(!sendData){
        return;
    }

    if(sendData.pwd.length<6){
        tips('输入密码小于6位');
        return;
    }
    // if (!comNameReg.test(sendData.name)) {
    //     tips('公司名称只能包含汉字！！！');
    //     return;
    // }
    if (!sendData.name) {
        tips('请输入公司名称！！！');
        return;
    }
    if(!managerReg.test(sendData.user)){
        tips('管理者只能包含汉字或英文！！！')
        return;
    }
    if(!phoneReg.test(sendData.phone)){
        tips('请输入正确的手机号')
        return;
    }
    if(!sendData.map) {
        tips('请选择关联地图');
        return;
    }
    if(companyId && sendData.map === initData.map && sendData.x === '') {  
        sendData.x = initData.x;
        sendData.y = initData.y;
        sendData.z = initData.z;
        sendData.floor = initData.floor;
        sendData.fid = initData.fid;
    }
    if(!sendData.x || !sendData.y) {
        tips('请在地图中选择模型，进行选点');
        return;
    }
    var path = url;
    sendData.addplaceids = addPlace.join();
    if(companyId){
        path += 'park/updateCompany';
        sendData.id = companyId;
        sendData.delplaceids = delPlace.join();
    }else{
        path += 'park/addCompany';
    }
    $.ajax({
        url:path,
        data:sendData,
        type:'post',
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success:function(res){
            if(res.code == 200){
                tips(res.message,function(){
                    if(!companyId){
                        location.reload();
                    }
                },1000);
            }else{
                tips(res.message);
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
//点击车位时显示
function moveModel(fid){
    if(!mapLoad){
        tips('地图正在加载成功，请稍后');
        return;
    }
    var model = null;
    if(fid){
        model = findModel({FID:fid});
        if(model){
            //移动到对应的位置
            fMap.moveTo({
                x: model.mapCoord.x,
                y: model.mapCoord.y,
                groupID:model.groupID,
                callback:function(){
                    if(selectedModel){
                        selectedModel.selected = false;
                    }
                    model.selected = true;
                    selectedModel = model;
                    fMap.scaleLevelTo({
                        duration: 0,
                        level: 20,
                        callback: function () { }
                    })
                }
            })
            return;
        }
    }
    tips('当前车位暂无相对应的位置');
};

//添加车位列表颜色
var addMapPlaceListColorList = [];
function addPlaceListColor(fidList) {
    if (!mapLoad) {
        tips('地图正在加载成功，请稍后');
        return;
    };

    addMapPlaceListColorList.forEach((target) => {
        target.model.selected = false;
        target.model.setColorToDefault();
    })

    fidList.forEach(fid => {
        let model = findModel({ FID: fid });
        if (model) {
            model.selected = false;
            model.setColor('#4A60CF', 1);
            let had = addMapPlaceListColorList.find(item => item.fid === fid);
            if (!had) {
                addMapPlaceListColorList.push({ fid, model })
            }
        }
    });
};

//查找相对应或删除
function findIdAndRemove(list,id,remove){
    var len = list.length;
    if(!len){
        return false;
    }
    for(var i = 0;i < len;i++){
        if(list[i] == id){
            if(remove){
                return list.splice(i,1);
            }else{
                return true;
            }
        }
    }
    return false;
}
//地图的回调
function mapClickFn(info,target){
    if(info.nodeType !== fengmap.FMNodeType.MODEL){
        tips('请选择模型');
        return;
    }
    if(info){
        tips('已选择地点');
    }
    $('[name="fid"]').val(info.fid);
    $('[name="x"]').val(info.cx);
    $('[name="y"]').val(info.cy);
    $('[name="z"]').val(info.z);
    $('[name="floor"]').val(info.floor);
    $('[name="map"]').val(map);
    if(selectedModel){
        selectedModel.selected = false;
    }
    //染色
    target.selected = true;
    selectedModel = target;
}