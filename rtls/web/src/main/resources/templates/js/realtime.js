var mapWh = {};//全屏时需要的信息（最大从父页面传入）
var mapId;//当前地图Id
var types = 1
var socket;
var domBag = {};//dom袋子
var mapOffL = 41;//左侧距离
var mapOffT = 231;//上侧距离


var baseArr = null;//基站信息


var mapChildDisplay = null;//内部元素的显示
var mapInfo = null;//地图信息
var mapType = '2';//地图类型

var mapBox = null;//地图外层的盒子
var fMapDom = null;//蜂鸟dom
var fMapLayer = {};//蜂鸟覆盖物的父类
//定义搜索分析类
var analyser = null;
var marker = [];//商家，充电桩等图片数组

$(function () {
    initLayer();
    $(document).on('click', function (e) {
        hideSele();
    })
    if (typeof (WebSocket) == "undefined") {
        tips("您的浏览器不支持WebSocket");
        return;
    }
    mapBox = document.getElementById('mapBox');
    fMapDom = document.getElementById('fengmap');
    //计算高度
    calcHeig();
    initMap();

})
//初始化图层事件
function initLayer() {
    var layer = document.getElementById('layer');
    var layerPop = document.getElementById('layerPop');
    var timer = null;
    layerPop.onmouseenter = layer.onmouseenter = function () {
        if (timer) {
            clearTimeout(timer);
            return;
        }
        layerPop.style.display = 'block';
    }
    layerPop.onmouseleave = layer.onmouseleave = function () {
        timer = setTimeout(function () {
            timer = null;
            layerPop.style.display = 'none';
        }, 300)
    }
}
//初始化地图
function initMap() {
    $.ajax({
        url: url + 'map/getMap2dSel',
        data: {
            pageSize: -1,
            enable: 1
        },
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res) {
            if (res.code != 200) {
                tips(res.message);
                return;
            }
            mapInfo = [];
            var list = res.data;
            var html = '';
            var first = list[0];
            var target = null;
            for (var i = 0; i < list.length; i++) {
                target = list[i];
                if (target.type != '2') {
                    continue;
                }
                mapInfo.push({
                    id: target.id,
                    mapName: target.name,
                    type: target.type,
                    wMeter: target.width,
                    hMeter: target.height,
                    basePath: target.subUrl,
                    userPath: target.tagUrl,
                    baseC: target.subColor,
                    fmapID: target.fmapID || '',
                    appName: target.appName || '',
                    mapKey: target.mapKey || '',
                    path: target.themeImg || '',
                    themeName: target.themeName || ''
                });
                html += '<div onclick="seleMap(this,\'' + list[i].id + '\',\'' + list[i].name + '\')">' + list[i].name + '</div>';
            }
            var mapSelect = $('#mapSelect');
            mapSelect.html(html);
            var mapClick = JSON.parse(sessionStorage.getItem("mapClick")) || undefined;
            var findItem;
            mapClick ? findItem = mapInfo.find(item => item.mapName === mapClick.name) : '';
            if (mapClick && findItem) {
                mapSelect.prev().html(mapClick.name);
                seleMap(null, mapClick.id, mapClick.name);
            } else {
                if (first) {
                    mapSelect.prev().html(first.name);
                    seleMap(null, first.id, first.name);
                }
            }
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}
//地图切换
function seleMap(that, id, name) {
    if (mapId == id) {
        return;
    }
    var resInfo = contrastReturn(mapInfo, 'id', id);
    if (!resInfo) {
        tips('获取失败，请重试');
        return;
    }
    var obj = { id: id, name: name };
    sessionStorage.setItem("mapClick", JSON.stringify(obj));
    seleBatch(that, id, function () {
        mapId = id;
        baseArr = [];
        //图形显示配置
        $('#marker').html('');

        var warnListLen = warnList.length;
        if (warnListLen) {
            //清空警告弹窗
            for (var i = 0; i < warnListLen; i++) {
                temp = warnList[i];
                //移除
                $(item.dom).remove();
                item.dom = null;
                item = null;
            }
            warnList = [];
        }
        $.each(fMapLayer, function (i, item) {
            item.imgLayer.removeAll();
            item.textLayer.removeAll();
            //item.extrudeLayer.removeAll();
            //item.domLayer.removeAll();
            delete fMapLayer[i];
        })
        fMapLayer = {};
        domBag = {};
        initFmap({
            fKey: resInfo.mapKey,
            fName: resInfo.appName,
            fId: resInfo.fmapID,
            path: resInfo.path,
            type: resInfo.type,
            themeName: resInfo.themeName,
            defaultControlsPose: 0,
            //初始指北针的偏移量
            compassOffset: [20, 60],
            //指北针大小默认配置
            compassSize: 48,
            focusFloor: 1,
            //defaultViewMode:fengmap.FMViewMode.MODE_2D,
            mapId
        });
        initPlaceInfo();
    });
}

var socket;
//请求定位数据
function startRequest() {
    if (socket) {
        getMapBefore();//请求之前的数据
        return;
    }
    if (typeof (WebSocket) == "undefined") {
        tips("您的浏览器不支持WebSocket");
    } else {
        if (!parent.uid) {
            console.log('定位数据连接失败，请检查是否登录');
            tips('定位数据连接失败，请检查是否登录');
            return;
        }
        console.log("您的浏览器支持WebSocket");
        //实现化WebSocket对象，指定要连接的服务器地址与端口  建立连接
        socket = new WebSocket(websocketUrl + parent.uid);
        //打开事件
        socket.onopen = function () {
            console.log("WebSocket 连接成功时触发");
            //tips("WebSocket 连接成功时触发");
            //socket.send("这是来自客户端的消息" + location.href + new Date());
        };
        //获得消息事件
        socket.onmessage = function (res) {
            processData(res.data);
        };
        //关闭事件
        socket.onclose = function () {
            console.log("Socket已关闭");
        };
        //发生了错误事件
        socket.onerror = function () {
            tips("Socket发生了错误，无法收到位置信息，请联系管理员", null, 5000);
        }
    }
}
//处理数据
function processData(res) {
    var item = JSON.parse(res);
    console.log(item);
    if (item.type != 2) {
        if (item.map != mapId) {
            return;
        }
    }
    var data = null;
    if (item.data instanceof Array) {
        //首次为数组
        data = item.data;
    } else {
        data = [item.data];
    }
    if (item.type == 1) {
        //定位数据
        fMapPos(data);
    } else if (item.type == 2) {
        //报警数据
        processAlarm(data);
    } else {
        console.log('未知类型');
    }
}
//高度
function calcHeig() {
    var winHeig = $(window).height();//可视区的高
    var screenHei = winHeig - 20 * 2 - 2;
    var heig = screenHei - ($('.top').outerHeight() || 0) - 20 * 2 - 116 - 20;
    mapBox.style.height = heig + 'px';
    var w = mapBox.offsetWidth - 2;
    fMapDom.style.width = w + 'px';
    fMapDom.style.height = heig + 'px';
    mapWh = {
        w: w,
        h: heig,
    }
}
//初始化微基站信息
function getAllBs() {
    $.ajax({
        url: url + 'bsconfig/getBsConfigSel',
        data: {
            map: mapId,
            pageSize: -1
        },
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res) {
            var data = res.data || [];
            var len = data.length;
            if (res.code != 200 || !len) {
                return;
            }
            var target = null;

            var em = null;
            var tm = null;
            var curFloor = null;
            var x;
            var y;
            var floor;
            for (var i = 0; i < len; i++) {
                target = data[i];
                if (target.x == undefined || target.y == undefined) {
                    continue;
                }
                if (isNaN(target.x) || isNaN(target.y)) {
                    continue;
                }
                floor = +target.floor;
                curFloor = fMapLayer['f' + floor];
                if (!curFloor) {
                    //找不到对应的楼层
                    return;
                }
                x = target.x;
                y = target.y;
                em = addFMImageMarker(curFloor.imgLayer, {
                    x: x,
                    y: y,
                    url: '../image/common/bluetooth.png',
                    size: 16,
                    height: 2,
                    avoid: false
                });
                tm = addTextMarker(curFloor.textLayer, {
                    x: x - 0.5,
                    y: y,
                    height: 3,
                    name: target.num,
                    fillcolor: '#4a60cf',
                    fontsize: 16,
                    avoid: false
                });
                tm.show = false;
                em.show = false;
                baseArr.push({
                    x: x,
                    y: y,
                    floor: floor,
                    type: 'base',
                    id: target.id,
                    area: em,
                    txt: tm
                });
            }
            var html = '<div class="layerItem">\
                            <div class="layerItemTitle">蓝牙信标</div>\
                            <div class="layerItemOpa">\
                                <span class="layerOpaMr" onclick="switchFmapDis(this,\'base\',\'area\')">图标</span><span onclick="switchFmapDis(this,\'base\',\'txt\')">名称</span>\
                            </div>\
                        </div>';
            $('#marker').append(html);
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}
//初始化地图
function initFmap(para) {
    openMap(para, function () {
        fMap.tiltAngle = 90;
        //获取当前聚焦楼层（会销毁）
        var floorList = fMap.groupIDs;
        var group = null;
        var gid;
        for (var j = 0; j < floorList.length; j++) {
            //获取所有楼层的layer
            gid = floorList[j];
            group = fMap.getFMGroup(gid);
            fMapLayer['f' + gid] = {
                imgLayer: group.getOrCreateLayer('imageMarker'),
                textLayer: group.getOrCreateLayer('textMarker'),
                extrudeLayer: group.getOrCreateLayer('extrudeMarker'),
                domLayer: group.getOrCreateLayer('domMarker')
            }
        }
        // 初始化搜索
        analyser = new fengmap.FMSearchAnalyser(fMap);
        var sortRes = searchByParams({ typeID: 200401 });
        const removeDuplicateObj = (arr) => {
            let obj = {};
            arr = arr.reduce((newArr, next) => {
                obj[next.ID] ? "" : (obj[next.ID] = true && newArr.push(next));
                return newArr;
            }, []);
            return arr;
        };
        var newResult = removeDuplicateObj(sortRes);
        for (var i = 0; i < newResult.length; i++) {
            var target = newResult[i];
            var model = findModel({ FID: target.FID });
            if (model) {
                setModelRender(model, resStateColor(target.state), target.state);
            }
        }
        //获取分站
        getAllBs();
        //获取车位
        getAllPlace();
        //获取商家
        getAllBusiness();
    });
}
//添加图片
function addFMImageMarker(imgLayer, para) {
    var em = resImgMarker(para);
    imgLayer.addMarker(em);
    return em;
}
//添加几何体
function addExtrudeMarker(extrudeLayer, point, para) {
    var em = resExtrudeMarker(point, para);
    extrudeLayer.addMarker(em);
    return em;
}
//添加文本
function addTextMarker(textLayer, para) {
    var tm = resTextMarker(para);
    textLayer.addMarker(tm);
    return tm;
}
//绘制位置
function fMapPos(data) {
    //同一设置为false,用于以前没返回的坐标
    var x;
    var y;
    var z;
    var id;
    var level;
    var target = null;
    var im = null;
    var dom = null;
    var personName;
    var item = null;
    var currentLayer = null;//当前楼层layer
    var currentFocus = fMap.currentFocusGroup;//当前楼层
    var floor;//target楼层
    for (var i = 0; i < data.length; i++) {
        target = data[i];
        id = target.tagid;
        floor = +target.floor;//要转成数字类型
        level = target.level;
        item = domBag[id];
        if (!target.status) {
            console.log('离线');
            if (item) {
                item.status = false;
                $('#user' + id).find('.userIcon').get(0).src = '../image/common/userOffline.png';
                continue;
            }
        }
        x = +target.x;
        y = +target.y;
        z = +target.z;

        if (item) {
            dom = $('#user' + id);//html的dom元素
            if (!item.status) {
                //离线变在线
                item.status = true;
                dom.find('.userIcon').get(0).src = '../image/common/labelBlue.png';
            }
            //蜂鸟的dom
            item.dom.setPosition(x, y, floor, z);
            //更新层高
            if (item.level != level) {
                item.level = level;
                dom.find('.drawerSpan').html(level);
            }
            //更新z
            if (item.z != z) {
                item.z = z;
                dom.find('.zSpan').html(z);
            }
        } else {
            personName = target.personName;
            currentLayer = fMapLayer['f' + floor];
            if (!currentLayer) {
                //找不到对应的楼层
                return;
            }
            //<img class="userIcon" onclick="showDetail(\'' + 'dp' + id + '\',\'' + target.id + '\')" src="' + (target.status ? '../image/common/labelBlue.png' : '../image/common/userOffline.png') + '" />\
            var domHtml = '<div class="user" id="user' + id + '">\
                    <div class="userName" style="color:#fff">' + personName + '</div>\
                    <img class="userIcon" src="' + (target.status ? '../image/common/labelBlue.png' : '../image/common/userOffline.png') + '" />\
                    <div class="alertBox drawer"><div>高度：<span class="zSpan">' + z + '</span></div><div class="alertTxt">抽屉层数：<span class="drawerSpan">' + level + '</span></div></div>\
                </div>';
            dom = new fengmap.FMDomMarker({
                x: x,
                y: y,
                height: z,
                domWidth: '20',
                domHeight: '24',
                domContent: domHtml,
                anchor: fengmap.FMMarkerAnchor.BOTTOM
            });
            domBag[id] = {
                dom: dom,//为蜂鸟的dom
                domType: 'fmap',
                floor: floor,
                level: level,
                z: z,
                searInfo: {
                    tagid: id,
                    personName: personName
                },
                status: (target.status ? true : false),
                show: true,
                offset: {
                    x: x,
                    y: y,
                }
            };
            currentLayer.domLayer.addMarker(dom);
            if (floor != currentFocus) {
                dom.show = false;
            }
        }
    }
}

//切换显示fmap地图子元素
function switchFmapDis(that, type, attr) {
    var flag = toggleClass(that, 'curLayerOpa');
    var resInfoArr = null;
    if (type === 'base' || type === 'micro') {
        resInfoArr = contrastReturnMore(baseArr, 'type', type);
    } else if (type === 'business' || type === 'charge') {
        resInfoArr = contrastReturnMore(marker, 'type', type);
    } else {
        resInfoArr = contrastReturnMore(areaArr, 'type', type);
    }
    var target = null;
    for (var i = 0; i < resInfoArr.length; i++) {
        target = resInfoArr[i];
        target[attr].show = flag;
    }
}
//切换显示文本
function mapFeatures(that, type) {
    var flag = toggleClass(that, 'curLayerOpa');
    if (type === 'label') {
        changeLabelLayerVis1(flag);
    } else {
        tips('切换失败');
    }
}
//切换class
function toggleClass(that, className) {
    var _that = $(that);
    var flag;
    if (_that.hasClass(className)) {
        _that.removeClass(className);
        flag = false;
    } else {
        _that.addClass(className);
        flag = true;
    }
    return flag;
}
//全屏
function mapFullScreen(that) {
    var _that = $(that);
    if (_that.hasClass('mapMin')) {
        _that.removeClass('mapMin');
        parent.setParentInfo(false, function (w, h) {
            showDom(w, h);
        });
    } else {
        _that.addClass('mapMin');
        parent.setParentInfo(true, function (w, h) {
            hideDom(w, h);
        });
    }
}
function hideDom(w, h) {
    $('.top').hide();
    $('.topList').hide();
    $('.wrapR').hide();
    $('.wrapL').css('margin-right', 0);
    $('.wrap').css('margin', 0);

    $('.addWarn').hide();

    mapBox.style.height = h + 'px';
    fMapDom.style.width = w + 'px';
    fMapDom.style.height = h + 'px';
    mapOffL = mapOffT = 21;
}
function showDom() {
    $('.wrap').css('margin', '20px');
    $('.top').show();
    $('.topList').show();
    $('.wrapR').show();
    $('.wrapL').css('margin-right', '660px');
    $('.wrap').css('margin', '20px');

    $('.addWarn').show();

    var w = mapWh.w;
    var h = mapWh.h;
    mapBox.style.height = h + 'px';
    fMapDom.style.width = w + 'px';
    fMapDom.style.height = h + 'px';
    mapOffL = 41;
    mapOffT = 231;
}

/***************报警定位弹窗****************/
var warnList = [];//弹窗（报警，人员详情）数组
//处理报警数据
function processAlarm(data) {
    var target = null;
    var warnId;
    var warntype;
    var warnstate;
    var matchInfo = null;
    var warnItem = null;
    var userDom = null;//用户报警时需要
    var hasWarntype = null;//当前存在的报警类型
    var warntypeInd;//下标
    var len;
    for (var i = 0; i < data.length; i++) {
        target = data[i];
        warntype = target.warntype;
        if (warntype != 7) {
            console.log('其他报警暂不处理');
            continue;
        }
        warnstate = target.warnstate;
        warnId = target.id;
        warnItem = contrastReturn(warnList, 'warnId', warnId);
        if (warnstate == 1) {
            //结束
            if (warnItem) {
                //如果有
                hasWarntype = warnItem.warntype;
                len = hasWarntype.length;
                warntypeInd = hasWarntype.indexOf(warntype);
                if (warntypeInd != -1) {
                    //找到有未结束的
                    hasWarntype.splice(warntypeInd, 1);//移除
                    len--;
                    if (!len) {
                        $(warnItem.dom).remove();
                        warnItem.dom = null;
                        contrastReturnOpa(warnList, 'warnId', warnId, function (item, i) {
                            warnList.splice(i, 1)
                        })
                    } else {
                        if (mapType == '1') {
                            warnItem.dom.getElementsByClassName('alertTxt')[0].innerHTML = resUserTxt(hasWarntype, len);
                        } else if (mapType == '2') {
                            //直接修改dom,蜂鸟暂没方法调用修改
                            $('#fmapTxt' + warnId).html(resUserTxt(hasWarntype, len));
                        }
                    }
                }
            }
            //防止手动关闭弹窗
            if (warntype == 10) {
                //人员离线
                personOffline(target.tagid, target.warnstate);
            } else if (warntype == 13) {
                //分站离线处理图标
                baseOffline(target.personid, target.warnstate);
            }
            return;
        }

        if (warntype == 2 || warntype == 4 || warntype == 5 || warntype == 6 || warntype == 7 || warntype == 10) {
            if (warntype == 10) {
                //人员离线
                personOffline(target.tagid, target.warnstate);
            }
            if (!warnItem) {//没有则创建
                matchInfo = domBag[target.tagid];
                if (!matchInfo) {
                    return;
                }
                warnItem = resWarnItem({
                    x: matchInfo.offset.x,
                    y: matchInfo.offset.y,
                    floor: matchInfo.floor,
                    warnId: warnId
                }, 'user');
                userDom = matchInfo.dom;
            }
        } else if (warntype == 3) {
            if (!warnItem) {
                matchInfo = contrastReturn(areaArr, 'id', target.area);
                if (!matchInfo) {
                    return;
                }
                warnItem = resWarnItem({
                    x: matchInfo.centerPoint.x,
                    y: matchInfo.centerPoint.y,
                    floor: matchInfo.floor,
                    warnId: warnId
                }, 'area');
            }
        } else if (warntype == 7 || warntype == 8 || warntype == 9 || warntype == 13 || warntype == 14) {
            if (warntype == 13) {
                //分站离线处理图标
                baseOffline(target.personid, target.warnstate);
            }
            if (!warnItem) {//创建
                matchInfo = contrastReturn(baseArr, 'id', target.personid);
                if (!matchInfo) {
                    return;
                }
                warnItem = resWarnItem({
                    x: matchInfo.x,
                    y: matchInfo.y,
                    floor: matchInfo.floor,
                    warnId: warnId
                }, 'base');
            }
        }
        if (warnItem) {
            //userDom为添加的父盒子
            processWarn(warnItem, target, userDom);
        }
    }
}

//处理报警显示的逻辑
function processWarn(res, target, userDom) {
    var hasWarntype = res.warntype;
    var len = hasWarntype.length;
    if (hasWarntype.indexOf(target.warntype) != -1) {
        //重复的判断
        return;
    }
    hasWarntype.push(target.warntype);
    len++;
    if (len > 1) {
        //只更新文字
        if (mapType == '1') {
            res.dom.getElementsByClassName('alertTxt')[0].innerHTML = resUserTxt(hasWarntype, len);
        } else if (mapType == '2') {
            //直接修改dom,蜂鸟暂没方法调用修改
            //user可以用1来修改（*）
            $('#fmapTxt' + res.warnId).html(resUserTxt(hasWarntype, len));
        }
        return;
    }
    var resType = null;
    //新增
    resType = contrastReturn(warnTypeList, 'id', target.warntype);
    var alertDom = null;
    var pos = null;
    if (res.type == 'user') {
        alertDom = resAlertBox(res.warnId, resType, 'alertBox');
        if (mapType == '1') {
            userDom.appendChild(alertDom);
        } else if (mapType == '2') {
            //$('#user' + res.warnId).append(alertDom);
            $('#user' + target.tagid).append(alertDom);
        }
    } else {
        if (mapType == '1') {
            alertDom = resAlertBox(res.warnId, resType, 'deviceAlert')
            pos = changePos(res.x, res.y);
            alertDom.style.left = pos.x - 20 + 'px';
            alertDom.style.top = pos.y - 100 + 'px';
            $('#map').append(alertDom);
        } else if (mapType == '2') {
            alertDom = resFmapAlertBox(fMapLayer['f' + res.floor].domLayer, res, resType);
            if (res.floor != fMap.currentFocusGroup) {
                alertDom.show = false;
            }
        }
    }
    res.dom = alertDom;
}
//创建返回蜂鸟的warnItem
function resFmapAlertBox(domLayer, attr, resType) {
    var domMarker = new fengmap.FMDomMarker({
        x: attr.x,
        y: attr.y,
        height: 3,
        domWidth: '80',
        domHeight: '100',
        domContent: '<div class="deviceAlert" style="left:16px;">' +
            '<div>' +
            '<img class="alertClose" src="../image/common/close.png" onclick="closeWarn(\'' + attr.warnId + '\',\'\',true)">' +
            '<img class="alertIcon" src="../image/common/' + resType.icon + '.png">' +
            '</div>' +
            '<div class="alertTxt" id="fmapTxt' + attr.warnId + '">' + resType.txt + '</div>' +
            '</div>',
        anchor: fengmap.FMMarkerAnchor.BOTTOM
    });
    domLayer.addMarker(domMarker);
    return domMarker;
}
//创建并返回warnItem
function resWarnItem(attr, type) {
    /*if(!matchInfo){
        return;
    }*/
    //区分fmap和平面 'fmap'为蜂鸟，'img'或''为平面
    var domType;
    if (mapType == '2') {
        domType = 'fmap';
    } else {
        domType = 'img';
    }
    var item = {
        warnId: attr.warnId,
        warntype: [],
        x: attr.x,
        y: attr.y,
        type: type,
        dom: null,
        domType: domType,
        floor: attr.floor,
    };
    warnList.push(item);
    return item;
}
//更新用户文字
function resUserTxt(hasWarntype, len) {
    //更新文字
    var warnTxt = '';
    for (var j = 0; j < len; j++) {
        resType = contrastReturn(warnTypeList, 'id', hasWarntype[j]);
        if (resType) {
            warnTxt += resType.txt + '/';
        }
    }
    return warnTxt.slice(0, -1);
}
//分站离线处理
function baseOffline(id, warnstate) {
    var baseItem = contrastReturn(baseArr, 'id', id);
    if (!baseItem) {
        return;
    }
    warnstate = !warnstate;//取反转布尔
    if (baseItem.offline == warnstate) {
        return;
    }
    baseItem.offline = warnstate;
    if (mapType == '1') {
        redraw();
    } else {
        if (warnstate) {
            baseItem.dom.url = '../image/common/baseOffline.png';
            baseItem.txt.fillcolor = '#969696';
        } else {
            baseItem.dom.url = '../image/common/bluetooth.png';
            baseItem.txt.fillcolor = '#4a60cf';
        }
    }
}
//人员离线处理
function personOffline(id, warnstate) {
    var personItem = domBag[id];
    if (!personItem) {
        return;
    }
    warnstate = !warnstate;//取反转布尔
    if (personItem.offline == warnstate) {
        return;
    }
    var img = personItem.dom.getElementsByClassName('userIcon')[0];
    if (warnstate) {
        img.src = '../image/common/userOffline.png';
    } else {
        img.src = '../image/common/labelBlue.png';
    }
    personItem.offline = warnstate;
}
//关闭警告
function closeWarn(id, hide, isFmap) {
    contrastReturnOpa(warnList, 'warnId', id, function (item, i) {
        if (hide) {
            //隐藏
            item.show = false;
            if (isFmap) {
                item.dom.show = false;
            } else {
                item.dom.style.display = 'none';
            }
        } else {
            //移除
            if (isFmap) {
                fMapLayer['f' + item.floor].domLayer.removeMarker(item.dom);
            } else {
                $(item.dom).remove();
            }

            item.dom = null;
            item = null;
            warnList.splice(i, 1);
        }
    })
}
//创建返回警告dom
function resAlertBox(warnId, resType, domClass) {
    var alertBox = document.createElement('div');
    var warntype = resType.id;//报警类型
    alertBox.className = domClass;
    if (warntype == 6) {
        alertBox.style.zIndex = 99;
    }
    var html = '<div>\
                    <img class="alertClose" src="../image/common/close.png" onclick="closeWarn(\'' + warnId + '\')" />\
                    <img class="alertIcon" src="../image/common/' + resType.icon + '.png" />\
                </div>';
    if (resType.txt) {
        html += '<div class="alertTxt">' + resType.txt + '</div>';
    }
    alertBox.innerHTML = html;
    return alertBox;
}
//报警类型列表
var warnTypeList = [{
    id: 1,
    txt: '井下超员',
    icon: 'overcrowded',
}, {
    id: 2,
    txt: '井下超时',
    icon: 'overcrowded',
}, {
    id: 3,
    txt: '区域超员',
    icon: 'abnormal',
}, {
    id: 4,
    txt: '非授权进入',
    icon: 'overcrowded',
}, {
    id: 5,
    txt: '非授权离开',
    icon: 'overcrowded',
}, {
    id: 6,
    icon: 'sos',
}, {
    id: 7,
    txt: '低电量显示',
    icon: 'abnormal',
}, {
    id: 8,
    txt: '分站故障',
    icon: 'abnormal',
}, {
    id: 9,
    txt: '分站超员',
    icon: 'abnormal',
}, {
    id: 10,
    txt: '离线报警',
    icon: 'overcrowded',
}, {
    id: 13,
    txt: '分站离线',
    icon: 'abnormal',
}, {
    id: 14,
    txt: '分站主供电异常',
    icon: 'abnormal',
}];

//车位信息的跳转
function placeDetail(types) {
    var item = contrastReturn(mapInfo, 'id', mapId);
    var name;
    var value;
    var type;
    var state;
    switch (types) {
        case 'stoppedPlace':
            name = 'state';
            value = "1";
            type = ''
            break;
        case 'freePlace':
            name = 'state';
            value = "0";
            type = ''
            break;
        case 'stoppedCharge':
            name = 'charge';
            value = "1";
            type = 1;
            state = 1;
            break;
        case 'freeCharge':
            name = 'charge';
            value = "0";
            type = 1;
            state = 0;
            break;
    }
    var path = './place.html?mapId=' + mapId + '&type=' + type + '&mapName=' + encodeURIComponent(item.mapName) + '&' + name + '=' + value + (state != null ? '&state=' + state : '');
    console.log(path)
    parent.setUrl(path, true);
}
//跳转至人数详情
function personDetails(areaId) {
    if (areaId == undefined) {
        tips('获取样品数详情失败，请重试');
        return;
    }
    var path = './dailyDetails.html?mid=' + mapId + '&aid=' + areaId;
    parent.setUrl(path, true);
}

//请求地图之前未显示的信息
function getMapBefore() {
    $.ajax({
        url: url + '/location/getMapMessageSel/' + mapId,
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res) {
            if (res.code != 200) {
                console.log('获取地图之前的信息失败，请重试');
                return;
            }
            var data = null;
            $.each(res.data, function (index, item) {
                if (item.map != mapId) {
                    return true;
                }
                data = item.data;
                if (item.type == 1) {
                    //定位数据
                    fMapPos(data);
                } else if (item.type == 2) {
                    //报警数据
                    processAlarm(data);
                } else {
                    console.log('未知类型');
                }
            })
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}
//获取车位
function getAllPlace() {
    $.ajax({
        url: url + 'park/getPlace',
        data: {
            pageSize: -1,
        },
        success: function (res) {
            if (res.code != 200) {
                return;
            };

            console.log('mapId', mapId);
            var list = res.data;
            var target = null;
            var model = null;
            var curFloor = null;
            var im = null;
            var flag = false;
            for (var i = 0; i < list.length; i++) {
                target = list[i];
                if (!target.fid) {
                    continue;
                }
                if (target.type == 1) {
                    model = findModel({ FID: target.fid });
                    if (model) {
                        curFloor = fMapLayer['f' + model.groupID];
                        flag || (flag = true);
                        im = addFMImageMarker(curFloor.imgLayer, {
                            x: +target.x,
                            y: +target.y + 2,
                            url: '../image/common/charge.png',
                            size: 16,
                            height: 2
                        });
                        im.show = false;
                        marker.push({
                            type: 'charge',
                            id: target.id,
                            area: im,
                        })
                    }
                }
                // if(!target.state){
                //     continue;
                // }
                model = findModel({ FID: target.fid });
                if (model) {
                    setModelRender(model, resStateColor(target.state), target.state);
                }
            }
            var html = '<div class="layerItem">\
                            <div class="layerItemTitle">充电桩</div>\
                            <div class="layerItemOpa">\
                                <span class="layerOpaMr" onclick="switchFmapDis(this,\'charge\',\'area\')">图标</span>\
                            </div>\
                        </div>';
            if (flag) {
                $('#marker').append(html);
            }
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}
//返回状态相对应的颜色
function resStateColor(state) {
    switch (state) {
        // case 0:
        //     return "#1BC736";
        // case 1:
        //     //已停
        //     return '#FFB145';
        // case 2:
        //     //预约
        //     return '#cdf408';
        case 0:
            return '#83F58D';
        case 1:
            return '#FDBBC5';
        case 3:
            return '#C2C2C2'
        default:
            return '';
    }
}
//搜索车位号
function inquire() {
    var keyword = $('[name="keyword"]').val();
    if (!keyword) {
        console.log('请输入要搜索的车位号');
        if (selectedModel) {
            selectedModel.selected = false;
        }
        return;
    }
    if (!mapLoad) {
        tips('地图正在加载成功，请稍后');
        return;
    }
    var model = null;
    if (keyword) {
        // model = findModel({ keyword: keyword });
        // if (model) {
        //     //移动到对应的位置
        //     fMap.moveTo({
        //         x: model.mapCoord.x,
        //         y: model.mapCoord.y,
        //         groupID: model.groupID,
        //         callback: function () {
        //             if (selectedModel) {
        //                 selectedModel.selected = false;
        //             }
        //             model.selected = true;
        //             selectedModel = model;
        //         }
        //     })
        //     fMap.mapScale = 300;
        //     return;
        // }

        searchKeyWordByPlaceName(keyword);
    }
    // tips('当前车位暂无相对应的位置');
};

/* 搜索车位 */
function searchKeyWordByPlaceName(key) {
    $.ajax({
        url: url + 'park/getPlace',
        data: {
            pageSize: -1,
            map: mapId,
            name: key
        },
        success: function (res) {
            console.log(res);
            if (res.code != 200) {
                tips('当前车位暂无相对应的位置');
                return;
            };
            var list = res.data;
            console.log('list', list);
            if (!list.length) {
                tips('当前车位暂无相对应的位置');
                return
            };
            let target = list[0];
            console.log('target', target);
            if (target.fid) {
                let model = findModel({ FID: target.fid });
                console.log('model', model);
                if (model) {
                    //移动到对应的位置
                    fMap.moveTo({
                        x: model.mapCoord.x,
                        y: model.mapCoord.y,
                        groupID: model.groupID,
                        callback: function () {
                            if (selectedModel) {
                                selectedModel.selected = false;
                            }
                            model.selected = true;
                            selectedModel = model;
                        }
                    })
                    fMap.mapScale = 300;
                    return;
                } else {
                    fMap.moveTo({
                        x: +target.x,
                        y: +target.y,
                        groupID: +target.floor,
                        callback: function () {

                        }
                    })
                }
            } else {
                fMap.moveTo({
                    x: +target.x,
                    y: +target.y,
                    groupID: +target.floor,
                    callback: function () {

                    }
                })
            }


        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
};

//初始化车位的信息
function initPlaceInfo() {
    var stoppedPlace = $('#stoppedPlace');
    var freePlace = $('#freePlace');
    var stoppedCharge = $('#stoppedCharge');
    var freeCharge = $('#freeCharge');
    $.ajax({
        url: url + 'park/getRealTimeData',
        data: {
            map: mapId,
        },
        success: function (res) {
            console.log(res);
            if (res.code != 200) {
                tips('获取停车位数据失败');
                stoppedPlace.html(0);
                freePlace.html(0);
                stoppedCharge.html(0);
                freeCharge.html(0);
                return;
            }
            var data = res.data;
            stoppedPlace.html(data.usedPlace);
            freePlace.html(data.emptyPlace);
            stoppedCharge.html(data.usedCharge);
            freeCharge.html(data.emptyCharge);
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}

//初始化微基站信息
function getAllBusiness() {
    $.ajax({
        url: url + 'park/getShangjia',
        data: {
            map: mapId,
            pageSize: -1
        },
        success: function (res) {
            var list = res.data.list || [];
            var len = list.length;
            if (res.code != 200 || !len) {
                return;
            }
            var target = null;

            var em = null;
            var tm = null;
            var curFloor = null;
            var x;
            var y;
            var floor;
            for (var i = 0; i < len; i++) {
                target = list[i];
                x = +target.x;
                y = +target.y + 2;
                if (x == undefined || y == undefined) {
                    continue;
                }
                if (isNaN(x) || isNaN(y)) {
                    continue;
                }
                floor = +target.floor;
                curFloor = fMapLayer['f' + floor];
                if (!curFloor) {
                    //找不到对应的楼层
                    return;
                }
                em = addFMImageMarker(curFloor.imgLayer, {
                    x: x,
                    y: y,
                    url: '../image/common/business.png',
                    size: 16,
                    height: 2
                });
                tm = addTextMarker(curFloor.textLayer, {
                    x: x - 0.5,
                    y: y,
                    height: 3,
                    name: target.name,
                    fillcolor: '#ffa800',
                    strokecolor: '#ffffff',
                    fontsize: 16,
                });
                em.show = false;
                tm.show = false;
                marker.push({
                    type: 'business',
                    id: target.id,
                    area: em,
                    txt: tm
                })
            }
            var html = '<div class="layerItem">\
                            <div class="layerItemTitle">商家</div>\
                            <div class="layerItemOpa">\
                                <span class="layerOpaMr" onclick="switchFmapDis(this,\'business\',\'area\')">图标</span><span onclick="switchFmapDis(this,\'business\',\'txt\')">名称</span>\
                            </div>\
                        </div>';
            $('#marker').append(html);
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}
//跳转至展示页
function toView() {
    window.open('./view.html?mapId=' + mapId);
}