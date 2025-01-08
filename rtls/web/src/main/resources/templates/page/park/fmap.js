// v:2.5.52-改h5用http获取数据前的代码


// 正式版 - 记录版本号 V: 1.2.56
var mqttClient = null;

// 体验版-记录版本号 
var JSVersion = 'V:1.3.18/10088'
var socket;
var lockReconnect = false; //避免重复连接
var websocket_time = new Date();
var mapInfo;
var userId;
var locationMarker; // 箭头
var backgroundLocationMarker; // 罗盘
var imagePath = "http://112.94.22.123:10087/rtls/wechat/";
// var formal = 'https://tuguiyao-gd.com/UWB/';
// var formal = 'http://192.168.1.124:8081/'
// var formal = 'http://192.168.1.131:8083/UWB/'
var formal = 'http://112.94.22.123:10088/UWB/'
var fMap = null;
var focusGroupID;
var naviAnalyser;
var naviPersonRequest;
var searchAnalyser;
var isFirst = true;
var navi;
var naviStart = false;
var offsetDis = 14;
var enterNaviArr = ["左转直行.png", "下坡.png", "上坡.png", "掉头.png", "直行.png", "左前方.png", "右前方.png", "右转直行.png", "上电梯.png", "下电梯.png", "左转.png", "右转.png"];
var destCoord;
var destination = {};
var newdestination = {};
var final_location_time = '';
var currentCoord = {};//绑路后结果
var currentCoord_before = {};//绑路前结果
var naviType;
var bindWalking = false;
var positionList = [];
var index_old = -1;
var pathStartEndPoints = [];
var naviZoom = 20.5;//导航开始后地图缩放级别
var turn_round_index = {
    index: -5,
    flag: false
};
var websocket_time = new Date();
var freeTimer;
var image_index = -1;
var direction = '开始导航';
var miter = '';
var miterStr = '';
var distance = 0;
var locationChangeLevel = false;
var placeNavi = {
    'destination_x': null, //目的地坐标
    'destination_y': null, //目的地坐标
    'destination_floor': null, //目的地楼层
    'destination_name': null, //目的地名称
    'place_x': null, //车位坐标
    'place_y': null, //车位坐标
    'place_floor': null, //车位楼层
    'place_name': null, //车位名称
    'place_vip': false, //车位是不是vip
    'place_fid': null, //车位fid
    'place_state': false, //是否车位导航中
    'place_lastTime': false, //是否车位导航中
    'firstRecommand': -1, //车位推荐次数
}; //当前车位导航过程中记录
var elevator_navi = false;
var finish;
var place_recommand = [];
//var startCoords;
let requestPara = {};
var scrollFloorControl;
var compass;
var clearHide = true;
var fMapResult = [];
var outdoorList = []; // 室外地图搜索
var keyword_name;
var is_show_more = true;
var place_type = [200401, 340862, 340860, 340859, 340861, 340863, 340878, 340879];
var exit_type = [200001];
var searchHistory;
var searchResult = [];
var allExit = [];
var showIconExitData = []; // 出入口
var crossLayerData = []; // 跨层通道数据
var outdoorInfo;
var ble_ned = {
    mac: '', //'64:72:D9:00:9F:75',
    state: true, //默认是开锁
};
var fixedView = true;
var fixedView_per;//地图拖动时记录状态

var tileLayer;// 瓦片底图
var coordinate;// 蜂鸟地图类型-小程序传来
var voiceValue;// 语音录入返回的文本内容
var initMapCenter;// 蜂鸟地图初始化是的中心位置
var nowinputtype = 1; // 1:搜索记录，2:收藏数据，3:搜索数据，4:热点数据，5:周边

var c_list = [];
var managetype;
var allcheckbox = false;
var showP;
var iscollect_coord;
var clickshowP = {};
var hotPList;
var showTD = true;
var isnearby = false;
var positionHistory = [];//历史位置数据
var welcomeList = ['search_1.png', 'markcar_2.png', 'findcar_3.png'];
var initF = true;
var colorList = []; // 车位状态
var noClick = false;
var varonlywall;
var oldLevel = '';
var newLverl = '';
var imageMarker;
var isShowLicenceFindCar = false;
var collectTime;

// 反向寻车 圆
var circleMarker;
var setIntervalMarkTime = false; // 导航自动标车位
var onceMarkPark = true;
var setTimeoutMarkTime;
var opensocketnum = '1';
//导航语音变量
var index_oldcount = 0;//当切换路线时，第一次切换时先将位置移动到当前路径的结束点，第二次时控制语音和位置切换到下一条路
var dis_old = 9999;//距离下个路口距离
var remain_old = 9999;
var level_old = -1;
var voice_direction_level0 = 15;
var voice_direction_level1 = 20;
var voice_direction_level2 = 15;
var voice_direction_level3 = 20;
var total_old = 9999;
var markerPoint = null; //地图关键路径标记点
var newpositionList = [];
var isoutdoor = false;

var isNaviName; // 'nearby'=就近停车  'ordinary'=找车位  'chargstation'=找充电桩   'vip'=vip进入 'accessibility'=无障碍
var ifshownavibox = 'none';

var isViPreservation = false;
var vipData;
var hasElevatorData; // 电梯数据
var ishasElevatorData = false;
var vipNaviTyep = 'none';
var getPlaceIdle = true;
var firstLocation = true;
var triggerVoice = true; // 是否播放车位占用语音
var isFirstNaviPoitNum = 1;
var isIos = false;
var showBusinessList = []; // 商家和公司列表
var onlyBusinessList = []; // 商家列表
var onlyCompanyList = []; // 公司列表
var businesscompanytime = null; // 商家-轮询
var isUpdateNaviUI = true; // 是否加载导航提示动画
var isOpenSearchPop = true; // 是否打开了搜索框
var naviLocateFlags; // 导航路径标记位数据
var isLicense = false; // 是否是车牌找车
var originalPosition;//原始定位数据bt/gps
// 路径回退
var lineR_old_remain = 0;
var lineR_now_remain = 0;
var iflineReturnTrue = false;
var fmapSetCenterFinish = true;
var fmapSetRotateFinish = true;
var heatmapInstance;// 热力图
var heatLeveData = {}; // 热力图每楼层的数据
var heatDataLists = []; // 热力图数据
var heatsetinterval; // 热力图轮询
var trackssetinterval; // 轨迹图轮询
var tracksDataList = [];// 轨迹图数据
// 保存位置信息
var send_x;
var send_y;
var send_f;
var point_x;
var point_y;
var point_f;
var colorTime = null; // 车位状态轮询
var isShowNetworkTip = true; // socket断开提示
var networkTipTime = null;
var isRecommendParkingData; // 推荐车位，用于释放已推荐的当前车位
var directionOfTravel = 0; // 计算行进方向是否错误
var isTravel = true;

var MapLoadSuccess = false;// 地图是否初始化
var MapParkSpaceState = false;// 车位占用情况是否完成传输

var queue_len = 6;

var whetherBreakSocket = false; // socket是否断开连接
var moreMapValue; // 多地图页传入搜索内容
var longTimeInResetNavi = null; // 长时间处于路径规划中
var firstUpdateNaviUI = true; // 同一个导航只触发一次修改地图控件位置
var emptyPlaceObj = {}; // 楼层空车位数据
var searchkeyTime = null; // 输入搜索节流
var circle_range = 6;//定位聚集门限
var path_change = false;//切换路径
var all_places = []; // 车位状态数据
var palceTypeTime = null; // 车位状态定时器
var clickNumber = 0; // 地图.5秒内点击次数
var nowShowZoomLevel; // 设置视图大小时的楼层
var isClickFloorControls = false; // 手动点击切换楼层
var stateNaviUserData = {}; // 开始导航时设置的用户位置
var clickDropTap = false; // 点击搜索内容
var nearbyType; // 就近停车的类型
var levelsName = []; // 每个楼层的dom-marker
var deleteDataIds = []; // 需要删除的历史记录id-list
var showBookingListByTimeData = {}; // 预约数据显示
var bookContentanimation; // 预约文字动画
var initBounds = {}; // 每楼层的bound
var initMapBound = {}; // 地图的bound
var fristaddLocationMarker = true; // 第一次设置定位marker
var noshowFixedView = false;

var elevatorData = []; // 电梯数据
var serviceFacilitiesData = []; // 厕所-安全出口-建筑
// 推广
var QRCodeCategory = '';
var QRCodeDataObj = {};
var newresultTemp = [];
var employGPSLocation = false; // 是否是使用gps定位到的位置

var phoneRealAngle; // 手机方向角度

var hadAllPlaces = false;
var hadAllExitData = false;
var hadAllBusinessData = false;
var hadAllCompanyData = false;
var hadAllElevatorData = false;
var hadAllServiceData = false;
var jumpOther = false;

var tipsText = '尚未到达定位信号覆盖区域，请稍后尝试';

var morePlaceData;
var morePlaceType;

/* type_num:
// 0 - 车位;
// 1 - 出入口;
// 2 - 室外;
// 3 - 商家/公司(旧；新：商家：3;公司：8);
// 4 - 跨层
// 5 - 地图上的数据-有楼栋-有电梯...
// 6 电梯
// 7 厕所-安全出口-室外物体
// 8 公司  */


$(function () {
    userId = getUrlStr('userId');
    // showFooterShowType1(true);
    // showSearchPop(true, []);
    // initFixedView(true);

    urlInitMap();

    connectWebsocket();
    // initMqttConnent();

    initTD(true, 'first');
    isIos = isIosorAndroid();

    $("#showJsVersion").html(JSVersion);

    // 增加帮助文字
    htmlLoadAddHelpText();

    // 给body增加上事件-解决苹果手机点击反馈问题
    addEventToBody();

    // 添加快捷分享dom
    addQuickShareTORightDom();

    getcollectlistwx();

    setInterval(() => {
        apiGetData2();
    }, 160);
});

function getcollectlistwx() {
    $.ajax({
        url: `${formal}wechat/getStorePlace?map=${mapInfo.mapId}&userId=${userId}`,
        success: function (res) {
            if (!(res.data && res.data.length)) return;
            c_list = res.data.sort((a, b) => {
                return b.id - a.id
            });
        }
    })
};

// 给body增加上事件-解决苹果手机点击反馈问题
function addEventToBody() {
    const body = document.getElementsByTagName('body')[0];
    body.addEventListener('touchstart', function (e) { });

    body.addEventListener('mouseover', function (e) { });
};

// 增加帮助文字
function htmlLoadAddHelpText() {
    let hasT = $("#helpTextId").length;
    if (!hasT) {
        let div = '<div class="helpText" id="helpTextId">帮助</div>';
        $(".operateLeft .operate-item").append(div)
    }
};

// 添加快捷分享dom
function addQuickShareTORightDom() {
    let hasQ = $("#quickshare").length;
    if (!hasQ) {
        let rightDom = $(".operateRight");
        let html = `
        <div class="quickshare operate-item" id="quickshare" onclick="onQuickShare()">
            <img class="operate-exitIcon" style="width:4.4vw;height:4.4vw" src="./image/quickshare.png" alt="">
            <span class="operate-iconText">分享</span>
        </div>
    `;
        rightDom.prepend(html)
    }
};

// 快捷分享
function onQuickShare() {
    let data = {
        x: currentCoord.x ? currentCoord.x : '',
        y: currentCoord.y ? currentCoord.y : '',
        level: currentCoord.level ? currentCoord.level : '',
        name: '分享位置'
    };

    // socket.send(JSON.stringify({
    //     todo: "wxshare",
    //     data
    // }));
    mqttPushMessage({
        todo: "wxshare",
        data
    })
};

// 小程序url带地图初始化参数
function urlInitMap() {
    let encode_data = getUrlStr('mapInfo');
    let decode_data = decodeURIComponent(encode_data);
    mapInfo = JSON.parse(decode_data)

    let div2 = `<div><div>mapInfo222</div><pre >${JSON.stringify(mapInfo, null, 2)}</pre></div>`;
    $("#error_conent").prepend(div2)
    initMap(mapInfo);
    setHTMLTitle(mapInfo);
    isShowLicenceFindCar = mapInfo.isShowLicenceFindCar;
    showFooterShowType1(true);
};

function initFixedView(initflag) {
    let imgUrl = "follow.png";
    let coord = currentCoord;

    if ((JSON.stringify(coord) == "{}" || !coord.x || !coord.y) && initflag === undefined) {
        return false;
    }

    if (initflag) {
        clickDropTap = true;
        fixedView = true;
    } else if (initflag == null) {
        clickDropTap = true;

        fixedView = !fixedView;
        if (!fixedView)
            fixedView_per = false;
    } else {
        fixedView = false;
    }
    imgUrl = fixedView ? "follow.png" : "current.png";

    if (fixedView && fMap && coord && coord.x) {
        if (!naviStart) {
            // 没有开始导航，点跟随
            if (locationMarker) {
                locationMarker.moveTo(coord);
                backgroundLocationMarker.moveTo(coord);
            };
            var currentZoom = fMap.getZoom();

            setTimeout(() => {
                coord.time = 0;
                if (currentZoom < 18) {
                    fMap.setZoom({
                        zoom: +18,
                        animate: false,
                        finish: function () {
                            moveToCoord(coord);
                        }
                    });
                } else {
                    moveToCoord(coord);
                }
            }, 160);
        } else {
            setTimeout(() => {
                coord.time = 0;
                moveToCoord(coord);
            }, 160);
        };

        if (naviStart && (naviType === "drive" || naviType === "walk")) {
            getFloorArea(); // 获取楼层面积-设置缩放大小
        }

    }
    $("#showFixedView").html(`<div class="operate-item" onclick="initFixedView()">
        <img src="http://112.94.22.123:10087/rtls/wechat/${imgUrl}" alt="">
    </div>`);

    let hadL = document.getElementById('quickPosition');

    let qphtml = `
        <div class="quickPosition" id="quickPosition" onclick="initQuickPosition()">
            查看室内地图
        </div>
    `;
    if (hadL) {
        let htmlText = $("#quickPosition").text();
        let text = fixedView ? '查看室内地图' : htmlText
        $("#quickPosition").html(`
            ${text}
        `)
    } else {
        $('body').append(qphtml)
    }
};

function initQuickPosition() {
    if (freeTimer) {
        clearTimeout(freeTimer);
    };
    let htmlText = $("#quickPosition").text();
    if (htmlText.trim() == '查看我的位置') {
        initFixedView(true)
    } else {
        if (naviStart) return; // 开始导航-使查看室内位置无效
        let nowLevel = fMap.getLevel();
        let bound = initBounds[nowLevel];
        initFixedView(false);
        fMap.setFitView(bound, false, () => { });

        if (!currentCoord.x || !currentCoord.y) return false;

        $("#quickPosition").html(`
            查看我的位置
        `)
    }
};

// 3D-2D
function initTD(type, init = 'two') {
    showTD = type ? type : !showTD;
    if (fMap) {
        fMap.setViewMode({
            mode: showTD ? fengmap.FMViewMode.MODE_3D : fengmap.FMViewMode.MODE_2D
        })
    };

    if (init == 'first') {
        $("#switchTD").html(`<div class="operate-item" onclick="initTD()">
            <!-- <img src="http://112.94.22.123:10087/rtls/wechat/${showTD ? '3d' : '2d'}.png" onclick="initTD()" alt=""> -->

            <div class="TDbox ${showTD ? 'D2' : 'D3'}">
                <div class="TDitem">2D</div>
                <div class="TDitem">3D</div>
            </div>
        
        </div>`);
    } else {
        if (showTD) {
            $(".TDbox").removeClass('D3')
            $(".TDbox").addClass('D2')
        } else {
            $(".TDbox").removeClass('D2')
            $(".TDbox").addClass('D3')
        }
    }
}

var hasmovecenter = true;
function moveToCoord(coord, angle, pathAngle) {
    if (!coord || !coord.x || !coord.y) {
        return;
    }
    let new_level = +coord.groupID || +coord.level;
    let current_level = fMap.getLevel();
    if (new_level != current_level) {
        fMap.setLevel({
            level: new_level
        });
        if (fixedView) {
            locationChangeLevel = true;
        }
    }
    let map_center = fMap.getCenter();
    if (!(map_center.x === coord.x && map_center.y === coord.y && new_level === current_level)) {
        var animateFlag = false;
        if (coord.time && coord.time > 0) {
            animateFlag = true;
            fmapSetCenterFinish = false;
        } else {
            changeAngle(angle, pathAngle);
        }

        if (new_level != current_level) {
            // 不同楼层，去除动画效果
            coord.time = 0;
            coord.duration = 0;
            animateFlag = false;
            coord.animate = false;
            hasmovecenter = true;
        };

        if (hasmovecenter) {
            // 动画没完成时，不再设置地图中心点
            // 完成动画后再设置
            hasmovecenter = false;
            fMap.setCenter({
                x: +coord.x,
                y: +coord.y,
                animate: animateFlag,
                duration: coord.time,
                finish: function () {
                    fmapSetCenterFinish = true;
                    changeAngle(angle, pathAngle);
                    hasmovecenter = true;
                }
            });
        }

        locationMarker.moveTo(coord);
        backgroundLocationMarker.moveTo(coord);

        //驾车导航过程中，偏航时，定位图标的箭头指向没有及时切换过来问题处理
        if (naviType === 'drive' && naviStart && (pathAngle || pathAngle == 0)) {
            if (fixedView) {
                locationMarker.rotateTo({
                    animate: true,
                    heading: pathAngle,
                    duration: 0.5
                });
            }
        }
    } else {
        changeAngle(angle, pathAngle);
    }
}

function modalSuccOrFail(cb) {
    $("#dialog .dialog-container").removeClass("animation_show2");
    $("#dialog .dialog-mask").removeClass("animation_show3");
    setTimeout(() => {
        cb();
    }, 300);
}

function goToMy() {
    processingFavoriteData(c_list)
    wx.miniProgram.navigateTo({
        url: "/pages/my/my"
    });
}

function goToReservation() {
    console.log('aa', mapInfo);
    wx.miniProgram.navigateTo({
        url: "/packageA/pages/reserveParking/reserveParking?mapId=" + mapInfo.mapId
    })
};

// 跳转帮助
function openHelp() {
    wx.miniProgram.navigateTo({
        url: "/packageA/pages/introduce/introduce"
    })
}

// 防抖
var ttt1;
function debounceFn(cb, data) {
    if (ttt1) {
        clearTimeout(ttt1);
    }
    var callNow = !ttt1;
    ttt1 = setTimeout(() => {
        ttt1 = null;
    }, 1000)
    if (callNow) {
        cb(data)
    }
}

// 获取当前地图地面楼层
function setGpsFloor() {
    let floorIds = fMap.getFloorInfos();
    let f1_floor = -1;
    floorIds.forEach(
        item => {
            if (item.name == "F1") {
                f1_floor = item.level;
            }
        }
    );
    // socket.send(JSON.stringify({
    //     todo: "setGpsFloor",
    //     data: {
    //         onLyOneFloor: floorIds.length > 1 ? false : true,
    //         f1_floor: f1_floor
    //     }
    // }));
    mqttPushMessage({
        todo: "setGpsFloor",
        data: {
            onLyOneFloor: floorIds.length > 1 ? false : true,
            f1_floor: f1_floor
        }
    })
}


// 车牌找车
function licenseCar() {
    // socket.send(JSON.stringify({
    //     todo: "navigateTo",
    //     data: {
    //         mapId: mapInfo.mapId
    //     }
    // }));
    mqttPushMessage({
        todo: "navigateTo",
        data: {
            mapId: mapInfo.mapId
        }
    })
}

// 商家服务
function business() {
    // socket.send(JSON.stringify({
    //     todo: "Tobusiness",
    //     data: {
    //         mapId: mapInfo.mapId
    //     }
    // }));
    mqttPushMessage({
        todo: "Tobusiness",
        data: {
            mapId: mapInfo.mapId
        }
    })
}

//重连
var reconnetTimers = null;
function reconnect() {
    if (lockReconnect) return;

    lockReconnect = true;
    //没连接上会一直重连，设置延迟避免请求过多
    reconnetTimers = setTimeout(function () {
        opensocketnum = +opensocketnum + 1;
        whetherBreakSocket = true; // socket断开连接-需要重连说明断开了
        lockReconnect = false;
        connectWebsocket();
    }, 5000);
}

//心跳检测
var heartCheck = {
    timeout: 5000, //针对不同项目设置不同时间，比如客服系统就不用那么频繁检测，股票交易所就必须很频繁
    timeoutObj: null,
    serverTimeoutObj: null,
    reset: function () {
        clearTimeout(this.timeoutObj);
        clearTimeout(this.serverTimeoutObj);
        return this;
    },
    start: function () {
        var self = this;

        this.timeoutObj = setTimeout(function () {
            //这里发送一个心跳，后端收到后，返回一个心跳消息，
            //onmessage拿到返回的心跳就说明连接正常
            var message = {
                "type": "h510010",
                "todo": "heart",
                "service": "发送维持连接消息！"
            };
            socket.send(JSON.stringify(message));//发送这个消息的时候，必须让后端收到消息后，立马发送一个消息，这样来回一个接收过程，就可以判断当前还没有断开连接。

            self.serverTimeoutObj = setTimeout(function () { //如果超过一定时间还没重置，说明后端主动断开了
                let current = new Date();
                let diff = (current.getTime() - websocket_time.getTime()) / 1000;

                if (diff > 8) {
                    if (isShowNetworkTip) {
                        networkTipTime = setTimeout(() => {
                            isShowNetworkTip = false;
                            clearTimeout(networkTipTime);
                            networkTipTime = null;
                            // showModal("网络异常，请重试", () => {
                            //     $('#dialog').html('');
                            // }, () => {
                            //     $('#dialog').html('');
                            // }, '取消', '了解')
                        }, 6000);
                    }
                    whetherBreakSocket = true; // socket断开连接

                    if (isIos) {
                        reconnect(1);
                    } else {
                        reconnect(1);
                    }

                    websocket_time = new Date();
                } else { }
            }, self.timeout)
        }, this.timeout)
    }
}


function isIosorAndroid() {
    var u = navigator.userAgent;
    var isiOS = false;
    // var isAndroid = u.indexOf('Android') > -1 || u.indexOf('Adr') > -1; //android终端
    isiOS = !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/); //ios终端
    return isiOS;
}

var ifClose = false;
function connectWebsocket() {
    heartCheck.reset().start();

    userId = getUrlStr('userId');
    // socket = new WebSocket('wss://' + location.host + '/UWB/websocket/h5WebSocket/' + userId);
    socket = new WebSocket('wss://tuguiyao-gd.com/UWB/websocket/h5WebSocket/' + userId);

    // socket = new WebSocket("ws://112.94.22.123:10088/UWB/websocket/h5WebSocket/" + userId);
    // socket = new WebSocket("ws://192.168.1.131:8083/UWB/websocket/h5WebSocket/" + userId);
    // socket = new WebSocket("ws://192.168.1.124:8081/websocket/h5WebSocket/" + userId);


    socket.onopen = function () {
        ifClose = false;
        // 5秒内重新连接成功，消除网络异常提示框
        if (networkTipTime) {
            clearTimeout(networkTipTime);
            networkTipTime = null;
        };

        if (reconnetTimers) {
            lockReconnect = false;
            clearTimeout(reconnetTimers);
            reconnetTimers = null;
        }
        websocket_time = new Date();
        if (opensocketnum == '1') {
            socket.send(JSON.stringify({
                todo: "connect",
                data: {
                    connect: true,
                    userId: userId
                }
            }));
        } else {
            threeWayHandshake()
        }
        heartCheck.reset().start();//连接成功之后启动心跳检测机制
    };
    socket.onmessage = function (res) {
        // 触发断网提示后，再次连上并收到信息后，再次进行断网提示功能
        if (!isShowNetworkTip) {
            isShowNetworkTip = true
        }
        //接收一次后台推送的消息，即进行一次心跳检测重置
        heartCheck.reset().start();
        websocket_time = new Date();
        handleMessage(res.data);
    };

    socket.onclose = function (res) {

    };

    socket.onerror = function (res) {
        opensocketnum = +opensocketnum + 1;
        reconnect();
    }
};

var threeWayHandshakeTime = null;
function threeWayHandshake() {
    if (!threeWayHandshakeTime) {
        threeWayHandshakeTime = setInterval(() => {
            socket.send(JSON.stringify({
                todo: "threeWayHandshake",
            }));
        }, 500);
    }
};

function initNed(ble) {
    if (ble.mac) {
        $("#nedIcon").html(`<div class="operate-item" onclick="operationNed()">
            <img class="operate-exitIcon" src="${imagePath}${ble.state ? 'ned_close' : 'ned_close_blue'}.png" alt="">
            <span class="operate-iconText">${ble.state ? '开锁' : '关锁'}</span>
        </div><div class="nedtip">
            <img src="${imagePath}ned_tip.png"" alt="">
        </div>`);
        $("#nedtip").html(``);
    }
}
function operationNed() {
    if (ble_ned.mac) {
        let bbb = {
            todo: "operationLock",
            data: ble_ned,
            time: new Date().getTime(),
        };

        apiPostData2(bbb);
        // mqttPushMessage({
        //     todo: "operationLock",
        //     data: {
        //         ble_ned
        //     }
        // })
    }
};

function apiPostData2(data) {
    let t = data.todo
    let d = data;
    if (t == 'navigatip') {
        let div2 = `<div><div>语音发送前</div><pre >${JSON.stringify(data, null, 2)}</pre></div>`;
        $("#error_conent").prepend(div2)
    }

    data.data = JSON.stringify(data.data);
    $.ajax({
        url: `${formal}mapHotspotData/saveTodo?userId=${userId}`,
        data: JSON.stringify(data),
        type: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        success: (res) => {
            if (t == 'navigatip') {
                let div2 = `<div><div>语音发送成功</div><pre >${JSON.stringify(d, null, 2)}</pre></div>`;
                $("#error_conent").prepend(div2)
            }
        }
    });
};

var nodataangle = true;
var angleTimea1 = null;
function apiGetData2() {
    $.ajax({
        url: `${formal}mapHotspotData/getAndDeleteTodos2?userId=${userId}`,
        success: function (res) {
            let data = res.data
            if (!(data && data.length)) return;

            data.sort((a, b) => {
                return b.time - a.time
            });

            let len = data.length;
            for (let i = 0; i < len; i++) {
                let item = data[i];
                item.data = JSON.parse(item.data);

                if (item.todo === 'initNed') {
                    if (item.data) {
                        ble_ned = item.data;
                        initNed(ble_ned)
                    }
                } else if (item.todo === 'setLocationMarkerPosition') {
                    if (nodataangle && fMap) {
                        if (item.data.angle) {
                            phoneRealAngle = item.data.angle
                        }

                        addLocationMarker(item.data.coord);
                        changePosition(item.data.coord, item.data.angle, item.data.type, item.data.move);
                    }
                } else if (item.todo === 'dis') {
                    if (item.data.dis < 1000) {
                        tipsText = '当前区域内手机信号弱，请稍后再试'
                    } else {
                        tipsText = '尚未到达定位信号覆盖区域，请稍后尝试'
                    }
                }
            }
        }
    });
};

function resEname(ename, typeID) {
    return '';
};

// item为目的地，target为车位
function recommandPlaceNavi(item, target, nearby) {
    const { x, y, floor, name, fid, vip, carBitType } = target;
    const center = { x: +x, y: +y, groupID: +floor };
    if (nearby) {
        destination = {
            x: +x,
            y: +y,
            level: +floor,
            autoMark: true,
            name: name + "(" + fMap.getFloor(+floor).name + "层)",
        };


        // 添加图片标注
        /* 构造 Marker */
        if (imageMarker) {
            imageMarker.remove();
        }
        imageMarker = new fengmap.FMImageMarker({
            x: +destination.x,
            y: +destination.y,
            url: './image/FMImageMarker.png',
            size: 30,
            height: 2,
            collision: false
        });
        const floor2 = fMap.getFloor(+destination.level)
        imageMarker.addTo(floor2);

        if (naviAnalyser) {
            cons = naviAnalyser.pathConstraint({ x: destination.x, y: destination.y, level: destination.level, buildingID: null });
            if (cons) {
                destination.x = +cons.coords.x;
                destination.y = +cons.coords.y;
            }
        }
    }
    if (!nearby && item && item.x) {
        let final_name = placeNavi.destination_name + "(" + fMap.getFloor(item.level).name + "层)";
        finish = {
            x: +x,
            y: +y,
            level: +floor,
            fid: +fid,
            name: name
        };
    };
    placeNavi.place_fid = target.fid;
    placeNavi.place_vip = target.vip || 0;
    placeNavi.place_name = name;
    placeNavi.carBitType = target.carBitType || 0;
    placeNavi.lastTime = new Date();
    newdestination = {
        x: +x,
        y: +y,
        level: +floor,
        autoMark: true,
        name: name + "(" + fMap.getFloor(+floor).name + "层)",
    }
    naviRoute(currentCoord, null, "drive", {
        x: +x,
        y: +y,
        level: +floor,
        autoMark: true,
        name: name + "(" + fMap.getFloor(+floor).name + "层)",
    }, (center) => {
        placeNavi.place_state = true;
        placeNavi.firstRecommand = 1;
    }, () => {
        placeNavi.place_state = false;
    }, null, true);
}

function initMap(mapInfo) {
    // 蜂鸟地图类型，1：百度；2：高德
    coordinate = mapInfo.coordinate || '2';

    focusGroupID = mapInfo.defaultFloor;

    var options = {
        container: document.getElementById('fengmap'),
        appName: mapInfo.appName,
        key: mapInfo.mapKey,
        mapID: mapInfo.fmapID,
        themeID: mapInfo.themeName,
        level: +focusGroupID,
        // level: 1,
        mapZoom: 18,
        zoomRange: [15, 23],
        floorSpace: 30,
        tiltAngle: 70,
        // nonFocusAlphaMode: true,
        // nonFocusAlpha: 0.8
    };
    fMap = new fengmap.FMMap(options);
    fMap.on('loaded', function () {
        MapLoadSuccess = true; // 地图初始化完成
        // 设置全览模式
        let bound = fMap.bound;

        initBounds = {}
        let ls = fMap.getLevels();
        ls.forEach((item) => {
            let b = fMap.getFloor(+item).bound;
            initBounds[item] = b
        });

        initMapBound = fMap.bound;

        let center = fMap.getCenter();
        initMapCenter = center;
        let initPosition = {
            x: center.x,
            y: center.y,
            groupID: focusGroupID
        };

        setTimeout(() => {
            naviAnalyser = new fengmap.FMNaviAnalyser({ map: fMap }, null, null);
            // todoing
            // naviAnalyser = new fengmap.FMNaviAnalyser({ map: fMap }, function () {
            //     data = {
            //         coord: {
            //             x: 12634601.052251454,
            //             y: 2653497.54485,

            //             level: 1,
            //             floor: 1,
            //             groupID: 1,
            //         }
            //     }
            //     if (true) {
            //         addLocationMarker(data.coord);
            //         changePosition(data.coord, null, 'bt', 0);
            //     }
            // }, null);
        }, 0);
        addFMapControl(10);
        SetTileLayerMode(+focusGroupID);
        initFixedView(false);
        setGpsFloor();
        addDrapMapEvent();
        SendMapSuccess();

        showEmptyPlaceHtml();

        /* 图层控制 */
        layerControl()

        /* 跨层通道 */
        crossLayerChannel();

        /* 电梯数据 -3*/
        getApiElevatorData();

        /* 厕所-安全出口-建筑-2 */
        getApiServiceFacilitiesData();

        // h5 ajax请求车位状态-1
        htmlGetWxApiForPlaceType(true);
        palceTypeTime = setInterval(() => {
            htmlGetWxApiForPlaceType(false);
        }, 30000);

        if (showIconExitData) {
            showExitIconToMap();
        };

        // 设置全览模式
        fMap.setFitView(bound, false, () => { });
    });

    fMap.on('click', function (event) {
        console.log('event', event);
        // 地图双击放大地图
        clickNumber++;
        setTimeout(() => {
            if (clickNumber == 2) {
                var currentZoom = fMap.getZoom();
                fMap.setZoom({
                    zoom: currentZoom + 1,
                    animate: true
                });
            };
            clickNumber = 0
        }, 500);

        isoutdoor = false;
        if (naviStart) return;
        if (noClick) return;
        if (morePlaceType) return;
        if (!event.targets.length) return;
        let target = event.targets[0];
        let { x, y, level, FID } = target;

        let info = {};
        switch (target.type) {
            case 8192:
            case 8:
                return;
            case 4096:
                if (target.typeID == 300000) {
                    return;
                }
                info = { FID, x, y, level, fid: FID };
                if (contains(place_type, target.typeID)) {
                    info['icon'] = 'search-place-icon';
                    info['type_num'] = '0'

                } else {
                    info['icon'] = 'search-map-icon';
                    info['type_num'] = '1'
                }
                break;
            case 64:
            case 16384:
                info = { FID, x, y, level, fid: FID };
                if (contains(place_type, target.typeID)) {
                    info['icon'] = 'search-place-icon';
                    info['type_num'] = '0'
                } else {
                    info['icon'] = 'search-map-icon';
                    info['type_num'] = '1'
                }
                break;
            case 1024:
                info = { FID, x, y, level, fid: FID };
                info['icon'] = 'search-place-icon';
                break;
        };

        if (info.fid) {
            /* 车位 */
            let isPlace = all_places.find((item) => item.fid == info.fid);
            if (isPlace && isPlace.name) {
                info.type_num = 0;
            };
            /* 出入口 */
            let isExit = showIconExitData.find((item) => item.fid == info.fid);
            if (isExit && isExit.name) {
                info.type_num = 1;
            };
            /* 跨层通道 */
            let isExit2 = crossLayerData.find((item) => item.fid == info.fid);
            if (isExit2 && isExit2.name) {
                info.type_num = 4;
            };
            /* 商家、公司 */
            let isCompany = showBusinessList.find((item) => item.fid == info.fid);
            if (isCompany && isCompany.name) {
                info.type_num = isCompany.type_num || 3;
            };
            /* 电梯 */
            let isElevator = elevatorData.find((item) => item.fid == info.fid);
            if (isElevator && isElevator.name) {
                info.type_num = 6;
            };
            /* 厕所-安全出口-室外物体 */
            let isServiceFacilities = serviceFacilitiesData.find((item) => item.fid == info.fid);
            if (isServiceFacilities && isServiceFacilities.name) {
                info.type_num = 7;
            };
        }

        // 添加图片标注
        /* 构造 Marker */
        if (imageMarker) {
            imageMarker.remove();
        }
        imageMarker = new fengmap.FMImageMarker({
            x: +event.coords.x,
            y: +event.coords.y,
            // url: 'https://developer.fengmap.com/fmAPI/images/blueImageMarker.png',
            url: './image/FMImageMarker.png',
            size: 30,
            height: 2,
            collision: false
        });
        const floor = fMap.getFloor(+event.level)
        imageMarker.addTo(floor);

        let name = target.name != undefined ? target.name : "未命名区域";
        let typeID = target.typeID;
        let is_place = contains(place_type, typeID);
        let is_exit = contains(exit_type, typeID);
        info.name = name;
        info.autoMark = is_place || false;
        info.mark = false;
        if (is_exit) {
            info.type = 'search_exit';
        }
        info.name = name + "(" + fMap.getFloor(level).name + "层)";
        showP = {
            fid: FID,
            name: info.name,
            x: event.coords.x || x,
            y: event.coords.y || y,
            level
        }

        var bind = null;

        var cons = naviAnalyser.pathConstraint({ x: event.coords.x, y: event.coords.y, level: event.level, buildingID: null });
        if (cons) {
            bind = cons.coords;
        } else {
            bind = event.coords;
        }

        info.x = bind.x;
        info.y = bind.y;

        info['ox'] = event.coords.x || x;
        info['oy'] = event.coords.y || y;

        showFooterShowType1(false);
        varonlywall = false;
        // 收藏
        iscollect_coord = info;

        showFooterShowType2(info, true);
    });

    // 楼层改变事件
    fMap.on('levelChanged', function (event) {
        // 楼层空车位数据显示更新
        showEmptyPlaceHtml();
        // 获取热力图数据
        // changeLevelGetHeatData(event.level);
        oldLevel = JSON.parse(JSON.stringify(newLverl));
        newLverl = event.level;

        if (!naviStart && !clickDropTap) {
            // let bound = fMap.getFloor(newLverl).bound;
            let bound = initBounds[newLverl];

            setTimeout(() => {
                // 设置全览模式
                fMap.setFitView(bound, false, () => { });
            }, 160);
        }

        if (oldLevel != newLverl) {
            // 当前楼层为F1时添加瓦片底图
            SetTileLayerMode(event.level);
        }

        if (!naviStart) {
            if (!locationChangeLevel) {
                initFixedView(false);
                if (freeTimer)
                    clearTimeout(freeTimer);
            }
        };

        customLevel = newLverl;
        // 自动切换楼层-改样式
        setLevelDom();
        // 设置切换楼层按钮样式
        switcherColor();
    });

    // 显示进度条
    fMap.on('progress', function (event) {
        let progressBar = event.progressBar;
        // socket.send(JSON.stringify({
        //     todo: "progressBar",
        //     data: progressBar
        // }));
        mqttPushMessage({
            todo: "progressBar",
            data: progressBar
        })
    });
}

function addDrapMapEvent() {
    const fengmap = document.getElementById('fengmap');

    fengmap.addEventListener('touchstart', function (e) {
        if (fixedView || isClickFloorControls)
            fixedView_per = true;
        initFixedView(false);
        if (freeTimer)
            clearTimeout(freeTimer);
    });

    fengmap.addEventListener('touchend', function (e) {
        freeTimer = setTimeout(() => {
            if (fixedView_per) {
                initFixedView(true);
            }
            fixedView_per = false;
            isClickFloorControls = false;
        }, 8000);
    });
};

function addLocationMarker(coord) {
    if (coord && coord.x && MapLoadSuccess) {

        if (!fristaddLocationMarker) {
            return false;
        };

        fristaddLocationMarker = false;
        currentCoord = coord;

        if (locationMarker) {
            locationMarker.remove();
            locationMarker = null;
        };
        if (backgroundLocationMarker) {
            backgroundLocationMarker.remove();
            backgroundLocationMarker = null;
        };
        locationMarker = new fengmap.FMLocationMarker({
            url: './image/newarrow.png',
            height: 1,
            // size: 32,
            size: 50,
            x: coord.x,
            y: coord.y,
            level: +coord.groupID
        });
        backgroundLocationMarker = new fengmap.FMLocationMarker({
            url: './image/newcompass.png',
            height: 1,
            size: 62,
            x: coord.x,
            y: coord.y,
            level: +coord.groupID
        });
        locationMarker.addTo(fMap);
        backgroundLocationMarker.addTo(fMap);

        setTimeout(() => {
            if (noshowFixedView) return false;
            initFixedView(true);
        }, 16);
    }
}

function changePosition(position, angle, type, move) {
    let sensorType = false;
    if (type === "sensor") {//步行且导航开始时使用传感器数据更新位置
        if (naviStart) {
            // if ((naviStart && naviType === "walk")) {
            if (navi.naviResult && index_old < 100) {
                let subs = navi.naviResult.subs;
                if (subs) {
                    let currentSub = subs[index_old];
                    if (currentSub.distance > 25) {
                        sensorType = true;
                        console.log("长路段步行使用传感器数据");
                    }
                }
            }
        }
        if (sensorType) {
            // $("#error").html("长路段步行使用传感器数据");
        } else {
            // $("#error").html("不适用传感器数据");
            return;
        }
    }



    if (position) {
        positionUpadte(position);
        if (type === "gps" || type === "bt") {
            originalPosition = position;
            originalPosition.level = position.groupID;
            var originalBind = naviAnalyser.pathConstraint({ x: position.x, y: position.y, level: position.groupID, buildingID: null });
            if (originalBind) {
                originalPosition = originalBind.coords;
                originalPosition.groupID = originalPosition.level;
            }
            wheatherInSamePath(originalPosition, angle, move);
        } else {
            positionFilter(position, angle, move);
        }
    } else {
        setLocationMarkerPosition(null, angle, 1);
        // setLocationMarkerPosition(position, angle, 1);//之前的代码
    }
}

function positionFilter(position, angle, move) {
    if (path_change) {
        if (naviType == 'drive')
            queue_len = 2;
        else {
            queue_len = 3;
        }
    } else {
        if (!(naviStart && naviType == 'drive')) {
            if (queue_len < 7) {
                queue_len++;
            }
        } else {
            if (naviStart && naviType == 'drive') {
                if (queue_len < 3) {
                    queue_len++;
                }
            }
        }
    }

    let history_res = addPositionHistory(position);//位置缓存数据中得到的定位结果
    if (history_res) {
        // this.simulateForward(history_res);
        //一阶低通滤波器
        let coord = null;
        // if (naviStart)//导航时进行滤波
        position_filter(history_res, currentCoord_before);
        // else
        //   coord = history_res;
        if (!coord || !coord.x) {
            coord = {
                x: history_res.x,
                y: history_res.y,
                groupID: history_res.groupID,
                level: history_res.groupID
            };
        }

        let delay = calculateDelay(coord, currentCoord_before);
        currentCoord_before = coord;



        var bind = null;
        if (coord) {
            var cons = naviAnalyser.pathConstraint({ x: coord.x, y: coord.y, level: coord.groupID, buildingID: null });
            if (cons) {
                bind = cons.coords;
            } else {
                bind = coord;
            }
            currentCoord = bind;
            currentCoord.groupID = bind.level;
        } else {
            currentCoord = position
        };

        let send = {
            x: currentCoord.x,
            y: currentCoord.y,
            floor: currentCoord.groupID || currentCoord.level,
            map: mapInfo.mapId,
            state: '自由行走',
            target: naviStart ? destination.name : '',
        };
        if (send_x != send.x || send_y != send.y || send_f != send.floor) {
            send_x = send.x;
            send_y = send.y;
            send_f = send.floor;
            if (!naviStart) {
                // 没有开始导航设置的位置；开始导航使用walking里的位置设置
                sendPosition(send);
            };
            // sendPosition2(send, move); // 安谱测试代码用
        }
        if (naviStart) {
            lineR_old_remain = JSON.parse(JSON.stringify(lineR_now_remain));
            navi.locate({
                x: currentCoord.x,
                y: currentCoord.y,
                level: currentCoord.groupID
            });
        } else {
            // if (bind) 
            {
                currentCoord.time = delay;
                if (move == 1) {
                    moveAlongPathTo(currentCoord, angle); // 沿路径移动
                } else {
                    let p1 = [locationMarker.x, locationMarker.y, locationMarker.level];
                    let p2 = [currentCoord.x, currentCoord.y, currentCoord.groupID || currentCoord.level];
                    if ((calculate_two_near_BeaconDis(p1, p2) > 28 && locationMarker.level == (currentCoord.groupID || currentCoord.level)) || locationMarker.level != (currentCoord.groupID || currentCoord.level)) {
                        if (locationMarker.level != (currentCoord.groupID || currentCoord.level))
                            currentCoord.time = 0;
                        else
                            currentCoord.time = 0.7;
                        setLocationMarkerPosition(currentCoord, angle, 1);
                    }
                }
            }
        }
    } else {
        var bind = null;

        if (firstLocation) {
            move = 1;
            if (position) {
                var cons = naviAnalyser.pathConstraint({ x: position.x, y: position.y, level: position.groupID, buildingID: null });
                if (cons) {
                    bind = cons.coords;
                } else {
                    bind = position;
                }
                currentCoord = bind;
                currentCoord.groupID = bind.level || position.groupID;
            } else {
                currentCoord = position
            }
            currentCoord.time = 0.3;
            firstLocation = false;
            // setLocationMarkerPosition(currentCoord, angle, 1); // 之前的

            // 修改后的
            if (!naviStart) {
                setLocationMarkerPosition(currentCoord, angle, 1);
            };
        }
    }
}

// 沿路径移到
function moveAlongPathTo(coord, angle) {
    // 上一个marker点
    let prev_m_data = locationMarker;
    // 当前定位点
    let now_c_data = coord;

    // 计算两点距离
    let pos1 = [now_c_data.x, now_c_data.y, now_c_data.level];
    let pos2 = [prev_m_data.x, prev_m_data.y, prev_m_data.level];
    let cal_dis = calculate_two_near_BeaconDis(pos1, pos2);

    if (prev_m_data.x != now_c_data.x || prev_m_data.y != now_c_data.y && prev_m_data.level == now_c_data.level && cal_dis < 50) {
        // 同层，位置点不同,且两点距离小于50


        // 两点路径分析
        var t_naviAnalyser = new fengmap.FMNaviAnalyser({ map: fMap }, (res) => {
            t_naviAnalyser.route({
                start: {
                    x: +prev_m_data.x,
                    y: +prev_m_data.y,
                    level: +prev_m_data.level
                },
                dest: {
                    x: +now_c_data.x,
                    y: +now_c_data.y,
                    level: +now_c_data.level
                },
                mode: fengmap.FMNaviMode.MODULE_BEST,
                priority: fengmap.FMNaviPriority.PRIORITY_DEFAULT
            }, (route) => {
                if (route.subs && route.subs.length) {
                    if (route.subs.length == 1) {
                        // 只有一条路径
                        setLocationMarkerPosition(currentCoord, angle, 1);
                    } else {
                        // 路径两条以上
                        // 获取路径距离
                        let sub_distance = route.distance;
                        if (sub_distance - cal_dis > 20) {
                            // 路径距离 大于 两点距离
                            setLocationMarkerPosition(currentCoord, angle, 1);
                        } else {
                            // 获取第一个拐点位置
                            let data = route.subs[0].waypoint.points
                            let points = {
                                x: data[data.length - 1].x,
                                y: data[data.length - 1].y,
                                level: data[data.length - 1].level,
                                time: 0.3,
                            }
                            setLocationMarkerPosition(points, angle, 1)
                        }
                    }
                } else {
                    // 没有路径
                    setLocationMarkerPosition(currentCoord, angle, 1);
                }
            }, () => {
                // route 失败
                setLocationMarkerPosition(currentCoord, angle, 1);
            })
        }, () => {
            // 分析失败
            setLocationMarkerPosition(currentCoord, angle, 1);
        });
    } else {
        if (prev_m_data.level != now_c_data.level)
            currentCoord.time = 0;
        setLocationMarkerPosition(currentCoord, angle, 1);
    }
};

// 判断是否新位置点是否在同一条路径上
function wheatherInSamePath(coord, angle, move) {

    let prev_m_data = locationMarker;
    // 当前定位点
    let now_c_data = coord;


    if (naviStart) {//开始导航后，切换路径判断
        // 两点路径分析
        var t_naviAnalyser = new fengmap.FMNaviAnalyser({ map: fMap }, (res) => {

            try {
                t_naviAnalyser.route({
                    start: {
                        x: +prev_m_data.x,
                        y: +prev_m_data.y,
                        level: +prev_m_data.level
                    },
                    dest: {
                        x: +now_c_data.x,
                        y: +now_c_data.y,
                        level: +now_c_data.level
                    },
                    mode: fengmap.FMNaviMode.MODULE_BEST,
                    priority: fengmap.FMNaviPriority.PRIORITY_DEFAULT
                }, (route) => {
                    if (route.subs && route.subs.length) {
                        if (route.subs.length == 1) {
                            // 只有一条路径
                            path_change = false;

                        } else {
                            path_change = true;
                        }
                    } else {
                        // 没有路径
                        path_change = false;
                    }
                    positionFilter(coord, angle, move);
                }, () => {
                    // route 失败
                    path_change = false;
                    positionFilter(coord, angle, move);
                })
            } catch (error) {
                // route 失败
                path_change = false;
                positionFilter(coord, angle, move);
            }

        }, () => {
            // 分析失败
            path_change = false;
            positionFilter(coord, angle, move);
        });
    } else {
        path_change = false;
        positionFilter(coord, angle, move);
    }

};
function positionUpadte(position) {

    newpositionList.push({
        x: position.x,
        y: position.y,
        floor: position.groupID,
    });

    if (newpositionList.length > 2) {
        newpositionList.shift();
        let old_currentCoord = newpositionList[newpositionList.length - 2];
        let new_currentCoord = newpositionList[newpositionList.length - 1];
        if (old_currentCoord.x != new_currentCoord.x || old_currentCoord.y != new_currentCoord.y || old_currentCoord.floor != new_currentCoord.floor) {
            // 位置发生变化， 且是驾车模式
            if (setIntervalMarkTime) {
                if (!setTimeoutMarkTime) {
                    setTimeoutMarkTime = setTimeout(() => {
                        // 自动标车位
                        currentCoordMark('none');
                        clearTimeout(setTimeoutMarkTime);
                        setTimeoutMarkTime = null;
                    }, 2000);
                }
            }
        }
    }
};


function calculateDelay(currentPos, formerPos) {
    if (currentPos.groupID != formerPos.groupID) {
        return 0.3;
    } else {
        let pos1 = [currentPos.x, currentPos.y, 1];
        let pos2 = [formerPos.x, formerPos.y, 1];
        let dis = calculate_two_near_BeaconDis(pos1, pos2);
        if (dis > 40) {
            return 0.3;
        } else {
            let speed = 4.0;
            if (naviType == 'drive')
                speed = 8.0;
            else if (naviType == 'walk')
                speed = 1.0;
            let delay = dis / speed;
            if (delay < 0.3)
                delay = 0.3;
            return delay;
        }
    }
}

function calculateDisDelay(dis) {
    if (dis) {
        if (dis > 40) {
            return 0.3;
        } else {
            let speed = 4.0;
            if (naviType == 'drive')
                speed = 8.0;
            else if (naviType == 'walk')
                speed = 1.0;
            let delay = dis / speed;
            if (delay < 0.01)
                delay = 0;
            return delay;
        }
    } else {
        return 0;
    }
}


// position
function position_filter(new_value, current) {
    let weight = 0.01;
    if (new_value.groupID == current.groupID) {
        let pos1 = [new_value.x, new_value.y, 1];
        let pos2 = [current.x, current.y, 1];
        let dis = calculate_two_near_BeaconDis(pos1, pos2);
        if (naviStart) {

            if (naviType == 'drive') {

                if (dis > 30 || dis < 1) {
                    weight = 0.35;
                } else {
                    weight = 0.0133 * dis + 0.3217;
                }
                //if(path_change&&dis<15){
                //	weight=0.01;
                //}
            } else if (naviType == 'walk') {
                weight = 0.0133 * dis + 0.3217;
                weight = weight < 0.8 ? 0.8 : weight;
                if (dis > 30 || dis < 1) {
                    weight = 0.02;
                }
                //if(path_change&&dis<8){
                //weight=0.01;
                //}
            }

        } else {
            weight = 1 - dis / 30.0;
            weight = weight < 0.5 ? 0.5 : weight;
            if (dis > 30 || dis < 2) {
                weight = 0.02;
            }
        }

        new_value.x = weight * current.x + (1 - weight) * new_value.x;
        new_value.y = weight * current.y + (1 - weight) * new_value.y;
    } else {

    }
}




function calculate_two_near_BeaconDis(pos0, pos1) {
    let dis = Math.sqrt(Math.pow(pos0[0] - pos1[0], 2) + Math.pow(pos0[1] - pos1[1], 2))
    return dis;
}









//历史位置更新，方便数据滤波
function addPositionHistory(point) {
    const {
        x,
        y,
        groupID
    } = point;
    let now = new Date();
    positionHistory.push({
        x: point.x,
        y: point.y,
        floor: point.groupID,
        time: now,
        //  remain:data.remain,
        //  path_index:data.index
    });



    if (positionHistory.length > queue_len) {
        positionHistory.shift();
        if (positionHistory.length == queue_len) {
            if (positionHistory.length == 1) {
                return point;
            } else {
                let res = judjeHistoryCenter(positionHistory);
                return res;
            }
        } else {
            // 步行导航开始,数据不一致，清除
            let history_len = positionHistory.length;
            let dif = history_len - queue_len;
            while (dif > 0) { dif--; positionHistory.shift(); }
            let res = judjeHistoryCenter(positionHistory);
            return res;
        }
    } else {
        // 步行导航开始,数据不一致，清除
        let res = judjeHistoryCenter(positionHistory);
        return res;
    }
}





function judjeHistoryCenter(history, r) {
    let gather = [];
    let res = {};
    res.hasRes = false;
    let len = history.length;
    let tail = history[len - 1];
    let floor = null;
    for (let k = 0; k < len; k++) {
        let former = history[k];
        gather.push(former);
        if (k == 0) {
            floor = former.floor;
        }
        if (floor != former.floor) {
            res.hasRes = false;
            if (naviStart) {
                if (naviType == 'drive') {
                    queue_len = 2;
                } else {
                    queue_len = 3;
                }
            } else {
                queue_len = 3;
            }

            history = [];
            break;

        } else {
            res.hasRes = true;
        }
    }
    if (res.hasRes) {
        if (naviStart && naviType == 'drive')
            circle_range = 15;
        else
            circle_range = 6;

        res = getMax(circle_range, Math.round(gather.length * 2 / 3), gather);
        if (Math.round(gather.length * 2 / 3) == 1) {
            res = tail;
            res.hasRes = true;
        }
        res.floor = tail.floor;
    }

    let fina_res = null;
    if (res.hasRes) {
        fina_res = { x: res.center_x, y: res.center_y, groupID: res.floor };
        if (Math.round(gather.length * 2 / 3) == 1) {
            fina_res = { x: res.x, y: res.y, groupID: res.floor };
        }
    } else {
        fina_res = null;
    }
    return fina_res;
}

function setLocationMarkerPosition(coord, angle, delay, pathAngle, setFlags = false) {
    if (coord && coord.x && coord.y) {
        if (setFlags) {
            // 设置导航路径标记位-没用到了
            naviLocateFlags = {
                x: coord.x,
                y: coord.y,
                level: +coord.groupID || coord.level,
            }
        }
        let animateFlag = false;
        if (coord.time) {
            animateFlag = true
        }
        let data = {
            x: coord.x,
            y: coord.y,
            level: +coord.groupID || coord.level,
            duration: coord.time ? coord.time : 0.3,
            animate: animateFlag,
            finish: function () { }
        };
        if (locationMarker && !fixedView) {
            // 不是跟随模式时，marker移动
            locationMarker.moveTo(data);
            backgroundLocationMarker.moveTo(data);
        }
        if (fixedView && fMap && coord) {
            // 跟随模式下，marker移动和地图中心点移动
            data.time = coord.time ? coord.time : 0.3;
            moveToCoord(data, angle, pathAngle);
        }
    } else {
        changeAngle(angle, pathAngle);
    }
}

function changeAngle(angle, pathAngle) {
    let markerAngle = null;
    if (angle || angle == 0) {
        angle = 360 - angle;//东西角度相反处理
        markerAngle = angle;
    } else {
        if (naviType === 'drive' && naviStart)//驾车导航，角度跟随路径角度
            markerAngle = pathAngle;
        if (naviType === 'walk' && naviStart)//驾车导航，角度跟随路径角度
            markerAngle = pathAngle;
    };

    if ((markerAngle || markerAngle == 0) && locationMarker) {
        if (naviType === 'walk' && naviStart) {//步行导航模式下，地图旋转
            if (angle || angle == 0) {
                locationMarker.rotateTo({
                    animate: true,
                    heading: angle,//指南针旋转
                    duration: 0.5
                });
            }
            if (pathAngle || pathAngle == 0) {
                fmapSetRotateFinish = false;
                if (fixedView) {
                    // let currentAngle = fMap.getRotation();
                    // if (Math.round(currentAngle - (360 - pathAngle)) != 0)
                    fMap.setRotation({
                        rotation: 360 - pathAngle,//旋转角度
                        animate: true,	//是否开启动画,true:开启;false:不开启. 默认 true
                        duration: 1, // 0.4
                        finish: function () {
                            fmapSetRotateFinish = true;
                        }
                    });
                }
            }
        } else if (naviType === 'drive' && naviStart && (pathAngle || pathAngle == 0)) {
            if (restoreAnimation) {
                pathAngle = 360 - pathAngle
            }
            locationMarker.rotateTo({
                animate: true,
                heading: pathAngle,
                duration: 0.5
            });
            if (fixedView) {
                let rotate = true;
                fMap.setRotation({
                    rotation: 360 - pathAngle,
                    animate: rotate,
                    duration: 1,
                });
            }
        } else if (!naviStart && (angle || angle == 0)) {
            locationMarker.rotateTo({
                animate: true,
                heading: angle,
                duration: 0.5
            });
        }
        // else {
        //     //自由模式下 根据指南针转动
        //     if (locationMarker) {
        //         if (!naviStart && (angle || angle == 0)) {
        //             locationMarker.rotateTo({
        //                 animate: true,
        //                 heading: angle,
        //                 duration: 0.5
        //             });
        //         } else if (naviType === 'drive' && naviStart && (pathAngle || pathAngle == 0)) {   //驾车导航时，第一人称需根据路径旋转地图角度
        //             if (fixedView) {
        //                 if (restoreAnimation) {
        //                     pathAngle = 360 - pathAngle
        //                 }
        //                 locationMarker.rotateTo({
        //                     animate: true,
        //                     heading: pathAngle,
        //                     duration: 0.5
        //                 });
        //                 let currentAngle = fMap.getRotation();
        //                 // if (Math.round(currentAngle - (360 - pathAngle)) != 0) {
        //                 let rotate = true;
        //                 fMap.setRotation({
        //                     rotation: 360 - pathAngle,//旋转角度
        //                     animate: rotate,//是否开启动画,true:开启;false:不开启. 默认 true
        //                     duration: 1,// 0.4
        //                 });
        //                 // }
        //             }
        //         }
        //     }
        // }
    }
}

function searchNearModel(startcoord, callback, isResetNavi, navi_type) {
    if (startcoord.x) {
        createNaviAnalyser({
            x: +startcoord.x,
            y: +startcoord.y,
            level: +startcoord.level || +startcoord.groupID
        }, callback, isResetNavi, navi_type)
    } else {
        noCurrentCoord();
    }
}

function getUrlStr(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = window.location.search.slice(1).match(reg);
    if (r != null) {
        return r[2];
    }
    return null;
}

function createNaviAnalyser(startCoord, callback, isResetNavi, navi_type) {
    if (naviAnalyser) {
        naviAnalyser = null;
    }
    naviAnalyser = new fengmap.FMNaviAnalyser({ map: fMap }, function () {
        if (naviAnalyser) {
            if (callback) {
                // createNavi(naviAnalyser);
                callback(startCoord)
            } else {
                createNavi(naviAnalyser);
                naviRoute(startCoord, null, navi_type, null, null, null, isResetNavi);
            }
        }
    });
}

function createNavi(naviAnalyser) {
    if (navi) {
        navi.dispose();
        navi = null;
    }
    bindWalking = false;
    navi = new fengmap.FMNavigation({
        map: fMap,
        analyser: naviAnalyser,
    });
}


function naviRoute(startCoords, priority, type, endCoords, successCb, failCb, isResetNavi, isElevalator) {
    if (navi) {
        navi.clearAll();
        removeLineMarker();
        pathStartEndPoints = [];
        newnaviRoute(startCoords, null, type, endCoords, successCb, failCb, isResetNavi, isElevalator)
    } else {
        if (naviAnalyser) {
            createNavi(naviAnalyser);
            newnaviRoute(startCoords, null, type, endCoords, successCb, failCb, isResetNavi, isElevalator)
        } else {
            naviAnalyser = new fengmap.FMNaviAnalyser({ map: fMap }, function () {
                createNavi(naviAnalyser);
                newnaviRoute(startCoords, null, type, endCoords, successCb, failCb, isResetNavi, isElevalator);
            });
        }
    }
}

// var resetNatTime_old = 0;
var resetNatShow = true;
var resetNaivMoveList = []
var resetttt;
var resetttt2;

function newnaviRoute(startCoords, priority, type, endCoords, successCb, failCb, isResetNavi, isElevalator) {
    if (type) {
        naviType = type;
    }
    if (isElevalator) {
        console.log("电梯导航，不将destion 重置");
    } else if (endCoords) {
        destination = endCoords;
    }

    // 终点绑路
    var cons = naviAnalyser.pathConstraint({
        x: endCoords ? +endCoords.x : +destination.x,
        y: endCoords ? +endCoords.y : +destination.y,
        level: endCoords ? (+endCoords.level || +endCoords.groupID) : (+destination.level || +destination.groupID),
        buildingID: null
    });
    // 添加图片标注
    /* 构造 Marker */
    if (imageMarker) {
        imageMarker.remove();
    }
    imageMarker = new fengmap.FMImageMarker({
        x: endCoords ? +endCoords.x : +destination.ox || +destination.x,
        y: endCoords ? +endCoords.y : +destination.oy || +destination.y,
        url: './image/FMImageMarker.png',
        size: 30,
        height: 2,
        collision: false
    });
    let imgLevel = endCoords ? (+endCoords.level || +endCoords.groupID) : (+destination.level || +destination.groupID);
    let imgFloor = fMap.getFloor(+imgLevel)
    imageMarker.addTo(imgFloor);


    if (cons) {
        naviPersonRequest = {
            start: {
                x: +startCoords.x,
                y: +startCoords.y,
                level: +startCoords.level || +startCoords.groupID,
                height: 3,
                url: 'https://developer.fengmap.com/fmAPI/images/start.png',
                size: 32,
            },
            dest: {
                x: +cons.coords.x,
                y: +cons.coords.y,
                level: +cons.coords.level,
                height: 3,
                url: 'https://developer.fengmap.com/fmAPI/images/end.png',
                size: 32
            },
            mode: fengmap.FMNaviMode.MODULE_BEST,           // 导航中路径规划模式
            priority: fengmap.FMNaviPriority.PRIORITY_DEFAULT   // 导航中的路线规划梯类优先级
        };
    } else {
        naviPersonRequest = {
            start: {
                x: +startCoords.x,
                y: +startCoords.y,
                level: +startCoords.level || +startCoords.groupID,
                height: 3,
                url: 'https://developer.fengmap.com/fmAPI/images/start.png',
                size: 32,
            },
            dest: {
                x: endCoords ? +endCoords.x : +destination.x,
                y: endCoords ? +endCoords.y : +destination.y,
                level: endCoords ? (+endCoords.level || +endCoords.groupID) : (+destination.level || +destination.groupID),
                height: 3,
                url: 'https://developer.fengmap.com/fmAPI/images/end.png',
                size: 32
            },
            mode: fengmap.FMNaviMode.MODULE_BEST,           // 导航中路径规划模式
            priority: fengmap.FMNaviPriority.PRIORITY_DEFAULT   // 导航中的路线规划梯类优先级
        };
    }

    setLocationMarkerPosition(naviPersonRequest.start);
    navi.setStartPoint(naviPersonRequest.start, true);  // 不绘画起点
    // navi.setStartPoint(naviPersonRequest.start);     // 绘画起点
    navi.setDestPoint(naviPersonRequest.dest);

    if (priority) {
        naviPersonRequest.priority = priority;
    } else {
        if (naviType == 'drive') {
            naviPersonRequest.priority = fengmap.FMNaviPriority.PRIORITY_ESCALATORONLY;
        } else if (naviType == 'walk') {
            // naviPersonRequest.priority = fengmap.FMNaviPriority.PRIORITY_DEFAULT;
            naviPersonRequest.priority = fengmap.FMNaviPriority.PRIORITY_LIFTFIRST1;
        }
    }

    try {
        navi.route(
            {
                mode: naviPersonRequest.mode,
                priority: naviPersonRequest.priority,
                toDoors: true,
            },
            function (result) {
                resetNatShow = true; // 偏航语音
                // 规划成功后，清除长时间处于路径规划中的定时器
                clearTimeout(longTimeInResetNavi);
                longTimeInResetNavi = null;
                isFirstNaviPoitNum++;
                // bindWalking = false;

                distance = Math.round(navi.naviResult.distance * 0.7);
                let len = result.subs.length;
                for (let i = 0; i < len; i++) {
                    let navData = result.subs[i];
                    let points = navData.waypoint.points;
                    if (points[0].x) {
                        pathStartEndPoints.push(points[0]);
                    }
                    if (points[points.length - 1].x) {
                        pathStartEndPoints.push(points[points.length - 1]);
                    }
                };
                navi.drawNaviLine({
                    passed: false
                });

                // 设置路径全览模式
                if (naviPersonRequest.start.level == naviPersonRequest.dest.level) {
                    isnewlinemarkertype = true;
                    if (freeTimer) {
                        clearTimeout(freeTimer);
                    };
                    if (!naviStart) {
                        initFixedView(false);

                        if (naviPersonRequest.dest.x != naviPersonRequest.start.x || naviPersonRequest.start.y != naviPersonRequest.dest.y) {
                            let bound = {
                                center: {
                                    x: naviPersonRequest.start.x,
                                    y: naviPersonRequest.start.y,
                                },
                                max: {
                                    x: naviPersonRequest.dest.x,
                                    y: naviPersonRequest.dest.y,
                                },
                                min: {
                                    x: naviPersonRequest.start.x,
                                    y: naviPersonRequest.start.y,
                                },
                            }
                            fMap.setFitView(bound, {
                                animate: false,
                                finish: function () {
                                    if (naviType == 'walk') {
                                        setLineMarker({
                                            x: naviPersonRequest.start.x,
                                            y: naviPersonRequest.start.y,
                                            z: 2.5
                                        }, {
                                            x: naviPersonRequest.dest.x,
                                            y: naviPersonRequest.dest.y,
                                            z: 2.5

                                        }, naviPersonRequest.start.level);
                                    }

                                    let s = fMap.getZoom();
                                    fMap.setZoom({
                                        zoom: s - 0.5,
                                    })
                                }
                            });
                        }

                    } else {
                        if (naviType == 'walk') {
                            setLineMarker({
                                x: naviPersonRequest.start.x,
                                y: naviPersonRequest.start.y,
                                z: 2.5
                            }, {
                                x: naviPersonRequest.dest.x,
                                y: naviPersonRequest.dest.y,
                                z: 2.5

                            }, naviPersonRequest.start.level);
                        }
                    }
                }

                showFooterShowType2(null, false);
                showFooterShowType1(false);
                $("#morePlaceView").html('');


                if (!isResetNavi && !morePlaceType) {
                    showDestiNaviInfo(true)
                };

                if (morePlaceType) {
                    switchNavi()
                };

                successCb && successCb();
            }, function (result) {
                failCb && failCb();
                isFirstNaviPoit();
            }
        );
    } catch (error) {
        isFirstNaviPoit();
    }

    if (!bindWalking) {
        bindWalking = true;
        navi.on('walking', function (data) {
            // 开始导航成功后，删除车位回收数据
            isRecommendParkingData = {};

            let nowLevel = currentCoord.level;
            if (nowShowZoomLevel != nowLevel) {
                // 重新设置视图大小
                getFloorArea();
            }

            // 开始导航后，设置位置
            startNaviSetLocal(data);

            let point = data.point;
            let resDrive;

            if (naviType == 'drive') {
                resDrive = driveNavi(point, data);
                setIntervalMarkTime = true;
                if (onceMarkPark) {
                    // 自动标车位
                    currentCoordMark('none');
                    onceMarkPark = false;
                }
                if (isNaviName == 'nearby' || isNaviName == 'ordinary' || isNaviName == 'chargstation' || isNaviName == 'vip' || isNaviName == 'accessibility') {
                    parkingSituation(isNaviName);
                }
            } else {
                resDrive = driveNavi(point, data);
            }
            console.log('resDrive', resDrive);
            let reset = judgeCrossResetNavi();
            if (resDrive == 'resetNav' && reset) {
                image_index = -1;
                miter = '';
                miterStr = '';
                direction = "路线规划中";

                // 重新规划节流
                if (!resetttt) {
                    resetttt = setTimeout(() => {
                        if (resetNatShow) {
                            resetNatShow = false;

                            let bbb = {
                                todo: "navigatip",
                                data: {
                                    str: direction,
                                    path: -1
                                },
                                time: new Date().getTime(),
                            };
                            apiPostData2(bbb);

                            // mqttPushMessage({
                            //     todo: "navigatip",
                            //     data: {
                            //         str: direction,
                            //         path: -1
                            //     }
                            // })

                            longTimeInResetNavi = setTimeout(() => {
                                // 15秒 长时处于路线规划中，再提示

                                let bbb = {
                                    todo: "navigatip",
                                    data: {
                                        str: '路线规划中，请稍等',
                                        path: -1
                                    },
                                    time: new Date().getTime(),
                                };
                                apiPostData2(bbb);

                                // mqttPushMessage({
                                //     todo: "navigatip",
                                //     data: {
                                //         str: '路线规划中，请稍后',
                                //         path: -1
                                //     }
                                // })
                            }, 15000);
                        };

                        resetPara();
                        updateNaviUI();
                        resetNaviRoute(currentCoord);
                        clearTimeout(resetttt);
                        resetttt = null;
                    }, 3000);
                    return
                }

            }
            if (resDrive == 'back') {
                clearTimeout(resetttt);
                resetttt = null;

                clearTimeout(resetttt2);
                resetttt2 = null;
                return
            }
            if (resDrive == 'end') {
                clearTimeout(resetttt);
                resetttt = null;

                clearTimeout(resetttt2);
                resetttt2 = null;
                return;
            }
            if (resDrive == 'stopNavi') {
                clearTimeout(resetttt);
                resetttt = null;

                clearTimeout(resetttt2);
                resetttt2 = null;

                noClick = false;
                clearCirclM();// 清除圆
                getPlaceIdle = false;
                setIntervalMarkTime = false; // 清除自动标车位
                onceMarkPark = true;
                isoutdoor = false;
                newdestination = {};
                isNaviName = "";
                isViPreservation = false;
                noShowDriveList = false;
                isFirstNaviPoitNum = 1;
                stopNavi();
            }
        })
    }
};

var lineMarker = null;
function setLineMarker(s, e, l) {
    var segment = new fengmap.FMSegment();
    segment.points = [s, e];
    segment.level = l;
    /* 构造线 Marker */
    lineMarker = new fengmap.FMLineMarker({
        color: '#ff0000',
        width: 1,
        smooth: true,
        radius: 1,
        animate: false,
        depth: true,
        segments: [segment],
        type: fengmap.FMLineType.ARROW,
    });
    lineMarker.addTo(fMap);
};

var isnewlinemarkertype = false;
function removeLineMarker() {
    isnewlinemarkertype = false;

    if (!lineMarker) return;

    lineMarker.remove();
    lineMarker = null;
};


var aaa = true;
function driveNavi_resetNav() {
    image_index = -1;
    miter = '';
    miterStr = '';
    direction = "路线规划中";

    // 重新规划节流
    if (!resetttt) {
        resetttt = setTimeout(() => {
            aaa = true;
            clearTimeout(resetttt);
            resetttt = null;
        }, 3000);
    };

    if (aaa) {
        aaa = false;
        if (resetNatShow) {
            resetNatShow = false;

            let bbb = {
                todo: "navigatip",
                data: {
                    str: direction,
                    path: -1
                },
                time: new Date().getTime(),
            };
            apiPostData2(bbb);

            // mqttPushMessage({
            //     todo: "navigatip",
            //     data: {
            //         str: direction,
            //         path: -1
            //     }
            // })
            longTimeInResetNavi = setTimeout(() => {
                // 15秒 长时处于路线规划中，再提示
                let bbb = {
                    todo: "navigatip",
                    data: {
                        str: '路线规划中，请稍等',
                        path: -1
                    },
                    time: new Date().getTime(),
                };
                apiPostData2(bbb);
                // mqttPushMessage({
                //     todo: "navigatip",
                //     data: {
                //         str: '路线规划中，请稍后',
                //         path: -1
                //     }
                // })
            }, 15000);
        };

        // 偏航规划重置
        restoreAnimation = false;
        returnTTS = false;

        resetPara();
        updateNaviUI();
        resetNaviRoute(currentCoord);
    }
};

// 路径回退
function drawNaviLineReturn(data) {
    if (lineR_old_remain == data.remain || lineR_old_remain <= 0 || data.remain > 99999) {
        return false
    }
    let old_now_remain = data.remain - lineR_old_remain;
    if (data.distance < 10 && old_now_remain * 0.7 > 0.01 && old_now_remain * 0.7 < 25) {
        // if (naviLocateFlags && naviLocateFlags.x) {
        //     iflineReturnTrue = true

        //     navi.locate({
        //         x: naviLocateFlags.x,
        //         y: naviLocateFlags.y,
        //         level: naviLocateFlags.level,
        //     });
        // }
        // 直接用market点的位置
        let now_locationMarker = locationMarker;

        iflineReturnTrue = true;
        navi.locate({
            x: +now_locationMarker.x,
            y: +now_locationMarker.y,
            level: +now_locationMarker.level,
        });
    }
};

// 开始导航后，有walking后的位置点设置位置
function startNaviSetLocal(data) {

    let { x, y, level } = data.point;
    if (!x || !y || !level) {
        return false;
    };
    let send = {
        x: x,
        y: y,
        floor: level,
        map: mapInfo.mapId,
        state: '自由行走',
        target: naviStart ? destination.name : '',
    };

    if (JSON.stringify(stateNaviUserData) != '{}') {
        send['state'] = stateNaviUserData.state;
        send['nav_state'] = stateNaviUserData.nav_state;
        send['target'] = stateNaviUserData.target;

        send['startX'] = stateNaviUserData.startX;
        send['startY'] = stateNaviUserData.startY;
        send['startFloor'] = stateNaviUserData.startFloor;

        send['endX'] = stateNaviUserData.endX;
        send['endY'] = stateNaviUserData.endY;
        send['endFloor'] = stateNaviUserData.endFloor;

    }

    if (point_x != send.x || point_y != send.y || point_f != send.level) {
        point_x = send.x;
        point_y = send.y;
        point_f = send.level;
        if (naviStart) {
            sendPosition(send);
        }
    }
};

function stopNavi(state) {
    clearTimeout(resetttt);
    resetttt = null;

    clearTimeout(resetttt2);
    resetttt2 = null;

    if (jumpOther && mapInfo.shortLink) {
        jumpOther = false;
        mqttPushMessage({
            todo: "jumpToOther",
        })
    }

    // 显示查看室内地图
    $("#quickPosition").css('display', 'flex')

    // 路径回退-数据重置
    lineR_old_remain = 0;
    lineR_now_remain = 0;
    iflineReturnTrue = false;

    compass.remove();
    addFMapControl(10);
    firstUpdateNaviUI = true;
    resetPara();
    naviStart = false;
    isFirstNaviPoitNum = 1;
    image_index = -1;
    direction = '开始导航';
    miter = '';
    miterStr = '';
    placeNavi.place_state = false;
    navi.clearAll();
    removeLineMarker();
    isUpdateNaviUI = true;
    $(".naviTip").removeClass("animation_show");
    $('#naviTip').html('');
    showDestiNaviInfo(false);
    showExitTips(false);
    if (morePlaceType) {
        noClick = true;
        showMorePlaceHtml();
    } else {
        if (!state) {
            noClick = true;
            showFooterShowType4(true);
        } else {
            showFooterShowType1(true);
        }
    };

    setLocationMarkerPosition(currentCoord, null, 1);

    const { type, autoMark, voiceType } = destination;
    if (type != 'exit' && type != 'outdoor' && type != 'exit2' && type != 'search_exit' && placeNavi.firstRecommand < 2 && state != "目的地路径驾车不可达") {
        if (naviType != 'walk' && !naviStart) {
            showModal("标记停车位置", () => {
                $('#dialog').html('');
                // 标车位
                currentCoordMark();
                showTipTwo();
            }, () => {
                $('#dialog').html('');
                // 标车位
                currentCoordMark('none');
                showTipTwo()
            })
        }
    } else if (type == 'outdoor') {
        // 室外地点-打开微信内置地图导航
        if (!state) {
            setTimeout(() => {
                // socket.send(JSON.stringify({
                //     todo: "openMap",
                //     data: outdoorInfo
                // }));
                mqttPushMessage({
                    todo: "openMap",
                    data: outdoorInfo
                })
            }, 5000);
        }
    };

    let send = {
        x: currentCoord.x,
        y: currentCoord.y,
        floor: currentCoord.groupID || currentCoord.level,

        speed: 10,
        target: '',
        nav_state: '',
        state: '自由行走',
        map: mapInfo.mapId,
    }
    sendPosition(send);
}

// 导航结束 是否开始目的地步行导航
function showTipTwo() {
    if (ishasElevatorData) {
        if (naviType != 'walk') {
            setTimeout(() => {
                showModal('步行导航去目的地', () => {
                    // $('#footerShowType1').html('');
                    showFooterShowType1(false);
                    // $('#footerShowType4').html('');
                    showFooterShowType4(false);
                    $('#dialog').html('');

                    // 添加图片标注
                    /* 构造 Marker */
                    if (imageMarker) {
                        imageMarker.remove();
                    }
                    imageMarker = new fengmap.FMImageMarker({
                        x: +hasElevatorData.x,
                        y: +hasElevatorData.y,
                        // url: 'https://developer.fengmap.com/fmAPI/images/blueImageMarker.png',
                        url: './image/FMImageMarker.png',
                        size: 30,
                        height: 2,
                        collision: false
                    });
                    const floor = fMap.getFloor(+hasElevatorData.level)
                    imageMarker.addTo(floor);

                    searchNearModel(currentCoord, (currentCoord) => {
                        naviRoute(currentCoord, null, 'walk', hasElevatorData, null, null, false, false);
                        hasElevatorData = null;
                        ishasElevatorData = false
                    }, null, 'walk');

                }, () => {
                    $('#dialog').html('');
                    hasElevatorData = null;
                    ishasElevatorData = false
                });
            }, 2000);
        }
        return false;
    };
    if (finish && finish.x) {
        if (naviType != 'walk') {
            setTimeout(() => {
                showModal('步行导航去目的地', () => {
                    // $('#footerShowType1').html('');
                    showFooterShowType1(false);
                    // $('#footerShowType4').html('');
                    showFooterShowType4(false);
                    $('#dialog').html('');

                    // 添加图片标注
                    /* 构造 Marker */
                    if (imageMarker) {
                        imageMarker.remove();
                    }
                    imageMarker = new fengmap.FMImageMarker({
                        x: +destination.ox || +destination.x,
                        y: +destination.oy || +destination.y,
                        // url: 'https://developer.fengmap.com/fmAPI/images/blueImageMarker.png',
                        url: './image/FMImageMarker.png',
                        size: 30,
                        height: 2,
                        collision: false
                    });
                    const floor = fMap.getFloor(+destination.level || +destination.floor || +destination.groupID)
                    imageMarker.addTo(floor);

                    // searchNearModel(currentCoord, (currentCoord) => {
                    naviRoute(currentCoord, null, 'walk', null);
                    finish = null
                    //});

                }, () => {
                    $('#dialog').html('');
                    finish = null
                });
            }, 2000);
        }
    };
}

function showModal(content, comfirmFn, cancelCb, cancelText, comfirmText) {
    $("#dialog").html(`<div class="dialog">
        <div class="dialog-mask animation_opac"></div>
        <div class="dialog-container animation_scale">
            <div class="dialogBody">
                ${content}           
            </div>
            <div class="dialogFooter">
                <div class="cancelBtn" onclick="modalSuccOrFail(${cancelCb})">${cancelText ? cancelText : '取消'}</div>
                <div class="comfirmBtn" onclick="modalSuccOrFail(${comfirmFn})">${comfirmText ? comfirmText : '确定'}</div>   
            </div>
        </div>
    </div>`);

    setTimeout(() => {
        $(".dialog-container").addClass("animation_show2");
        $(".dialog-mask").addClass("animation_show3");
    }, 350);
}


function judgeCrossResetNavi() {
    let reset = true;
    let len = pathStartEndPoints.length;
    let pos1 = [currentCoord.x, currentCoord.y, 1];
    let minFloor = 100, maxFloor = -100;
    for (let i = 0; i < len; i++) {
        let item = pathStartEndPoints[i];
        let pos2 = [item.x, item.y, 1];
        let dis = calculate_two_near_BeaconDis(pos1, pos2);
        if (item.groupID > maxFloor)
            maxFloor = item.groupID;
        if (item.groupID < minFloor)
            minFloor = item.groupID;
        if (dis < 10)
            reset = false;
    }
    if (currentCoord.groupID < minFloor || currentCoord.groupID > maxFloor)
        reset = true;
    return reset;
}

var isOnElevator = false;
function driveNavi(point, data) {
    positionList.push({
        x: point.x,
        y: point.y,
        floor: point.groupID,
        groupID: currentCoord.groupID,
        distance: data.distance,
        remain: data.remain,
        path_angle: data.angle
    });
    if (positionList.length >= 20) {
        positionList.shift();
    }
    let posInfo = {
        maxOffsetFlag: 0, //10 用户重新规划路线
        cenOffsetFlag: 0, //4 用户重新规划路线
        diifFloorFlag: 0, //4 不同楼层
        head_back: 0, //20 用户掉头
        stopNavi_near: 0, //20 导航结束
        stopNavi_far: 0, //20 导航结束
    };

    let defaultDistance1 = 12;
    let defaultDistance2 = 10;
    let level = currentCoord.level || currentCoord.groupID || currentCoord.floor;
    let levelName = fMap.getFloor(+level).name;
    let fd1 = getFloorNamePassF1(levelName);
    if (fd1) {
        defaultDistance1 = 6;
        defaultDistance2 = 5;
    }

    positionList.forEach((item) => {
        if (item.x) {
            if (isOnElevator) {
                isOnElevator = false;
            }
            if (item.distance > defaultDistance1) {
                posInfo.maxOffsetFlag += 1;
            } else if (item.distance > defaultDistance2) {
                posInfo.cenOffsetFlag += 1;
            }
        } else {
            let subs = navi.naviResult && navi.naviResult.subs || null;
            if (!subs) {
                posInfo.maxOffsetFlag += 1;
                return;
            }
            let hasSubsDistance0 = subs.some((item) => {
                return item.distance == 0
            });
            if (hasSubsDistance0) {
                subs.forEach((item) => {
                    if (item.distance == 0) {
                        let item_points = item.waypoint.points[1]
                        let currentCoord_points = currentCoord;

                        let pos1 = [item_points.x, item_points.y, item_points.level];
                        let pos2 = [currentCoord_points.x, currentCoord_points.y, currentCoord_points.level];
                        let cal_dis = calculate_two_near_BeaconDis(pos1, pos2);

                        if (cal_dis * 0.7 > 4) {
                            posInfo.maxOffsetFlag += 1;
                        } else {
                            positionList = [];
                            isOnElevator = true
                        }
                    }
                })
            } else {
                // 路径中没有 item.distance == 0 ==》偏航
                posInfo.maxOffsetFlag += 0.4;
            }
        }

        if (item.x === -1) {
            posInfo.diifFloorFlag += 1;
        }
        let dis = 100;
        if (currentCoord.groupID == destination.level) {
            let coord = (navi.pathConstraint(destination) || {}).coord;
            if (coord == undefined) coord = {};
            if (!coord || !coord.x) {
                coord.x = destination.x;
                coord.y = destination.y;
                coord.groupID = destination.groupID;
            }
            let pos11 = [item.x, item.y, 1];
            let pos12 = [coord.x, coord.y, 1];
            dis = Math.round(calculate_two_near_BeaconDis(pos11, pos12) * 0.7);
        }
        var dir_level = 12; //直线距离
        var vertical_level = 10; //直线距离
        if (naviType == 'drive') {
            dir_level = 10;
            vertical_level = 8;
        } else if (naviType == 'walk') {
            dir_level = 8;
            vertical_level = 8;
        }
        if (item.remain * 0.7 < dir_level)
            posInfo.stopNavi_far += 1;
    })
    let maxOffsetFlag_1 = 5;
    let cenOffsetFlag_2 = 6;
    if (naviType === 'drive') {
        maxOffsetFlag_1 = 5;
        cenOffsetFlag_2 = 6;
    } else {
        maxOffsetFlag_1 = 3;
        cenOffsetFlag_2 = 4;
    }

    let end_posi_data = finish || destination;
    end_posi_data['level'] = end_posi_data.level || end_posi_data.groupID;

    let start_posi = [currentCoord.x, currentCoord.y, currentCoord.level];
    let end_posi = [end_posi_data.x, end_posi_data.y, end_posi_data.level];
    let stop_dis = calculate_two_near_BeaconDis(start_posi, end_posi);

    if (stop_dis * 0.7 < 12 && currentCoord.level == end_posi_data.level) {
        if (posInfo.stopNavi_far >= 3) {
            positionList = []
            isOnElevator = false;

            errorAngleIndex = 0; // 前进角度问题
            triggerErrorAngleTip = true;

            // let subsaindex;
            // let subsa = navi.naviResult && navi.naviResult.subs || null;
            // if (subsa && subsa.length) {
            //     subsaindex = subsa.length
            // };
            // if (subsaindex) {
            //     if (+data.index + 1 == subsaindex) {
            //         return 'stopNavi'
            //     } else {
            //         drive_updatelocation(data, point);
            //     }
            // } else {
            return 'stopNavi'
            // }
        } else {
            drive_updatelocation(data, point);
        }
    } else {
        if (posInfo.maxOffsetFlag >= maxOffsetFlag_1 || posInfo.cenOffsetFlag >= cenOffsetFlag_2) {
            positionList = []
            isOnElevator = false;

            errorAngleIndex = 0; // 前进角度问题
            triggerErrorAngleTip = false; // 偏航后不再进行方向判断

            // 重新规划路径-添加角度判断

            if (naviType == 'drive') {
                planPerspectiveJudgment();
            } else {
                driveNavi_resetNav()
            }

            //     return 'resetNav';
        } else {
            drive_updatelocation(data, point);
        }
    }
}

var planPerspectiveJudgmentTimes = null;
var planPerspectiveJudgmentType = true;
var planPerspectiveJudgmentTTSTypeTimes = null;
var planPerspectiveJudgmentTTSType = true;
function planPerspectiveJudgment() {
    if (restoreAnimation) {
        setLocationMarkerPosition(null, phoneRealAngle, 1, phoneRealAngle);
    };
    if (!planPerspectiveJudgmentTimes) {
        planPerspectiveJudgmentTimes = setTimeout(() => {
            planPerspectiveJudgmentType = true;
            clearTimeout(planPerspectiveJudgmentTimes);
            planPerspectiveJudgmentTimes = null;
        }, 3000);
    }

    if (planPerspectiveJudgmentType) {
        planPerspectiveJudgmentType = false;

        abc().then((res) => {
            if (res === true) {
                driveNavi_resetNav();
            }
        })
    }
};

var restoreAnimation = false;
function abc() {
    return new Promise((resolve, reject) => {
        let now_c_data = currentCoord;
        let now_d_data = destination;
        let returnType;
        // 两点路径分析
        var t_naviAnalyser = new fengmap.FMNaviAnalyser({ map: fMap }, (res) => {
            t_naviAnalyser.route({
                start: {
                    x: +now_c_data.x,
                    y: +now_c_data.y,
                    level: +now_c_data.level
                },
                dest: {
                    x: +now_d_data.x,
                    y: +now_d_data.y,
                    level: +now_d_data.level
                },
                mode: fengmap.FMNaviMode.MODULE_BEST,
                priority: fengmap.FMNaviPriority.PRIORITY_DEFAULT
            }, (route) => {
                if (route.subs && route.subs.length) {
                    // 有路径
                    let points = route.subs[0].waypoint.points;
                    let p1 = points[0];
                    let p2 = points[1];
                    let px = p2.x - p1.x;
                    let py = p2.y - p1.y;

                    let radian = Math.atan2(py, px)
                    let PI = Math.PI;
                    let angle = radian * 180 / PI;

                    let lineAngle = ''
                    if (px >= 0 && py >= 0) {
                        lineAngle = 90 - angle
                    } else if (px <= 0 && py >= 0) {
                        lineAngle = 450 - angle
                    } else if (px <= 0 && py <= 0) {
                        lineAngle = 90 - angle
                    } else if (px >= 0 && py <= 0) {
                        lineAngle = 90 - angle
                    };

                    let playTip;

                    console.log('phoneRealAngle - lineAngle', phoneRealAngle - lineAngle);
                    console.log('phoneRealAngle ', phoneRealAngle);
                    console.log(' - lineAngle', lineAngle);

                    if (lineAngle >= 45 && lineAngle <= 315) {
                        if (phoneRealAngle - lineAngle <= 45 && phoneRealAngle - lineAngle >= -45) {
                            playTip = false;
                        } else {
                            playTip = true;
                        }
                    } else {
                        if (lineAngle > 315) {
                            if ((phoneRealAngle - lineAngle >= -45 && phoneRealAngle - lineAngle <= 360 - lineAngle) || phoneRealAngle <= 45 - (360 - lineAngle)) {
                                playTip = false
                            } else {
                                playTip = true;
                            }
                        } else if (lineAngle < 45) {
                            if ((phoneRealAngle - lineAngle <= 45 && phoneRealAngle >= 0) || (phoneRealAngle >= 360 - (45 - lineAngle))) {
                                playTip = false
                            } else {
                                playTip = true;
                            }
                        }
                    }

                    console.log('playTip', playTip);
                    if (playTip === true) {
                        restoreAnimation = true;

                        setLocationMarkerPosition(currentCoord, phoneRealAngle, 1);

                        if (!planPerspectiveJudgmentTTSTypeTimes) {
                            planPerspectiveJudgmentTTSTypeTimes = setTimeout(() => {
                                planPerspectiveJudgmentTTSType = true;
                                clearTimeout(planPerspectiveJudgmentTTSTypeTimes);
                                planPerspectiveJudgmentTTSTypeTimes = null;
                            }, 10000);
                        }
                        if (planPerspectiveJudgmentTTSType) {
                            planPerspectiveJudgmentTTSType = false;

                            let bbb = {
                                todo: "navigatip",
                                data: {
                                    str: '您已偏航，请确认行进方向',
                                    path: -1
                                },
                                time: new Date().getTime(),
                            };
                            apiPostData2(bbb);

                            // mqttPushMessage({
                            //     todo: "navigatip",
                            //     data: {
                            //         str: '您已偏航，请确认行进方向',
                            //         path: -1
                            //     }
                            // });
                        }

                        returnType = false;
                        resolve(returnType)
                    } else if (playTip === false) {
                        restoreAnimation = false;

                        returnType = true;
                        resolve(returnType)
                    }

                } else {
                    // 没有路径
                    returnType = true;
                    resolve(returnType)
                }
            }, () => {
                // route 失败
                returnType = true;
                resolve(returnType)
            })
        }, () => {
            // 分析失败
            returnType = true;
            resolve(returnType)
        });
    })
};

function setNaviDescriptions(data) {
    if (naviStart) {
        navTextChange(navi, data);
    }
    return false;
}
//重新规划路线后，判断当前位置是否与定位点偏移过大，偏移过大时进行修正
function resetNewCurrentPosition() {
    // 偏航时，清除定位历史记录-解决偏航时会结束导航问题
    positionHistory = [];

    if (originalPosition.level == currentCoord.level) {
        let pos1 = [originalPosition.x, originalPosition.y, 1];
        let pos2 = [currentCoord.x, currentCoord.y, 1];
        let dis = calculate_two_near_BeaconDis(pos1, pos2);
        if (dis > 25) {
            currentCoord = originalPosition;
            currentCoord_before = originalPosition;
            positionHistory = [];
        }
    }
};

function ifnoneDataCurrentC(oldData, Data) {
    let tx = false;
    let ty = false;
    let tl = false;
    if (Math.abs(oldData.x - Data.x) <= 0.0000002) {
        tx = true;
    };
    if (Math.abs(oldData.y - Data.y) <= 0.0000002) {
        ty = true;
    };
    if (oldData.level == Data.level) {
        tl = true
    };
    return tx && ty && tl
};

var old_pointx; // 判断行进方向时是否是同一个定位点
var errorAngleIndex = 0; // 判断手机方向和路径方向是否同向;
var triggerErrorAngleTip = true;

var returnTTS = false;

function drive_updatelocation(data, point) {
    if (isOnElevator) {
        setLocationMarkerPosition(currentCoord, null, 0.2, -data.angle, true);
        return false;
    };

    if (data.distance < 10) {
        let old_index = index_old;
        let old_remain = remain_old;
        let old_level = level_old;
        let rangeThreshold;

        // 两段距离差：
        rangeThreshold = ((data.remain * 0.7 - old_remain) > 20);
        let div = `<div><div>rrr</div>${rangeThreshold}</div>`;
        $("#error_conent").prepend(div)


        // 行进方向错误
        /* 角度问题 */
        let naviAngle = data.angle;
        if (triggerErrorAngleTip) {
            if (data.index != 0) {
                triggerErrorAngleTip = false
            } else {
                let unNaviAngle;
                if (naviAngle >= 270) {
                    unNaviAngle = naviAngle - 180;
                } else if (naviAngle >= 180) {
                    unNaviAngle = naviAngle - 180;
                } else if (naviAngle >= 90) {
                    unNaviAngle = 180 + naviAngle;
                } else {
                    unNaviAngle = 180 + naviAngle;
                }

                if (unNaviAngle >= 45 && unNaviAngle <= 315) {
                    if (phoneRealAngle - unNaviAngle <= 45 && phoneRealAngle - unNaviAngle >= -45) {
                        errorAngleIndex += 1;
                    } else {
                        errorAngleIndex = 0;
                    }
                } else {
                    if (unNaviAngle > 315) {
                        if ((phoneRealAngle - unNaviAngle >= -45 && phoneRealAngle - unNaviAngle <= 360 - unNaviAngle) || phoneRealAngle <= 45 - (360 - unNaviAngle)) {
                            errorAngleIndex += 1;
                        } else {
                            errorAngleIndex = 0;
                        }
                    } else if (unNaviAngle < 45) {
                        if ((phoneRealAngle - unNaviAngle <= 45 && phoneRealAngle >= 0) || (phoneRealAngle >= 360 - (45 - unNaviAngle))) {
                            errorAngleIndex += 1;
                        } else {
                            errorAngleIndex = 0;
                        }
                    }
                }
            }
        }

        if (errorAngleIndex >= 7) {
            errorAngleIndex = 0;
            triggerErrorAngleTip = false;

            //行进方向错误-语音提示
            let direction = '请确认行进方向';
            setTimeout(() => {
                let bbb = {
                    todo: "navigatip",
                    data: {
                        str: direction,
                        path: -1
                    },
                    time: new Date().getTime(),
                };
                apiPostData2(bbb);

                // mqttPushMessage({
                //     todo: "navigatip",
                //     data: {
                //         str: direction,
                //         path: -1
                //     }
                // })
            }, 500);
        }
        /* 角度问题 */
        let backwardMoveMarker = false;

        if (rangeThreshold && point.x && !triggerErrorAngleTip) {
            if (point.x != old_pointx) {
                old_pointx = JSON.parse(JSON.stringify(point.x))
                directionOfTravel += 1;
            }
        } else if ((data.remain * 0.7 - old_remain) < 0) {
            directionOfTravel = 0;
        };
        if (directionOfTravel >= 3 && isTravel) {
            //行进方向错误-语音提示
            directionOfTravel = 0;
            isTravel = false;
            directionOfTravelList = [];
            backwardMoveMarker = true
            let direction = '您已连续偏航，请确认行进方向';

            let bbb = {
                todo: "navigatip",
                data: {
                    str: direction,
                    path: -1
                },
                time: new Date().getTime(),
            };
            apiPostData2(bbb);

            // mqttPushMessage({
            //     todo: "navigatip",
            //     data: {
            //         str: direction,
            //         path: -1
            //     }
            // })
            returnTTS = true;
        };

        if (returnTTS) {
            if (naviAngle >= 45 && naviAngle <= 315) {
                if (phoneRealAngle - naviAngle <= 45 && phoneRealAngle - naviAngle >= -45) {
                    returnTTS = false;
                    restoreAnimation = false;
                }
            } else {
                if (naviAngle > 315) {
                    if ((phoneRealAngle - naviAngle >= -45 && phoneRealAngle - naviAngle <= 360 - naviAngle) || phoneRealAngle <= 45 - (360 - naviAngle)) {
                        returnTTS = false;
                        restoreAnimation = false;
                    }
                } else if (naviAngle < 45) {
                    if ((phoneRealAngle - naviAngle <= 45 && phoneRealAngle >= 0) || (phoneRealAngle >= 360 - (45 - naviAngle))) {
                        returnTTS = false;
                        restoreAnimation = false;
                    }
                }
            }
        };

        console.log('data.index', data.index);
        console.log('old_index', old_index);
        console.log('old_remain', old_remain);
        console.log('odata.remain * 0.7ld_remain', data.remain * 0.7);
        console.log('(old_remain - data.remain * 0.7)', (old_remain - data.remain * 0.7));

        let div2 = `<div><div>sss</div>${data.index}~${old_index}~${(old_remain - data.remain * 0.7)}</div>`;
        $("#error_conent").prepend(div2)

        if ((data.index == old_index && ((old_remain - data.remain * 0.7) >= 0)) || (data.index > old_index) || backwardMoveMarker) {
            // if ((data.index == old_index && ((old_remain - data.remain * 0.7) >= 0)) || (data.index > old_index) || rangeThreshold) {
            remain_old = data.remain * 0.7;
            isTravel = true;
            restoreAnimation = false;

            if (rangeThreshold) {
                resetPara();
            }

            if (data.index > old_index) {
                point.path_index = data.index;
                if (old_index > -1) {
                    // 跨层不走拐点
                    if (data.level != old_level || (data.index - old_index) > 1) {
                        setLocationMarkerPosition(point, null, 0, -data.angle, true);
                        index_oldcount = 1;
                    } else {
                        let posints = navi.naviResult.subs[old_index].waypoint.points;
                        let endPoint = posints[posints.length - 1];
                        if (endPoint && index_oldcount == 0) {
                            point.path_index = old_index;
                            endPoint.time = 0.2;
                            currentCoord_before = endPoint;
                            currentCoord_before.groupID = endPoint.level;
                            setLocationMarkerPosition(endPoint, null, 0.2, -data.angle, true);
                            index_oldcount = 1;
                            if (isnewlinemarkertype && naviType == 'walk') {
                                removeLineMarker();
                                setLineMarker({
                                    x: endPoint.x,
                                    y: endPoint.y,
                                    z: 2.5
                                }, {
                                    x: naviPersonRequest.dest.x,
                                    y: naviPersonRequest.dest.y,
                                    z: 2.5
                                }, naviPersonRequest.dest.level)
                            }
                        } else {
                            point.time = 0.3;
                            setLocationMarkerPosition(point, null, 0, -data.angle, true);
                        }
                    }
                } else {
                    point.time = 0.3;
                    setLocationMarkerPosition(point, null, 0, -data.angle, true);
                }
            } else {
                point.path_index = data.index;
                point.time = 0.7;

                if (data.index != old_index)
                    setLocationMarkerPosition(point, null, 0, -data.angle, true);
                else
                    setLocationMarkerPosition(point, null, 1, -data.angle, true);
            }
            level_old = data.level;


            if (turn_round_index.flag && turn_round_index.index == data.index) { } else {
                if (data.index > old_index) {
                    if (index_oldcount > 0) {
                        setNaviDescriptions(data);
                        index_oldcount = 0;
                    }
                    if (old_index == -1) {
                        setNaviDescriptions(data);
                    }
                } else {
                    setNaviDescriptions(data);
                }
                turn_round_index = {
                    index: -5,
                    flag: false
                };
            };
        }
    }
}

function resetPara() {
    index_old = -1;
    dis_old = 9999;
    remain_old = 9999;
    level_old = -1;

    turn_round_index = {
        index: -5,
        flag: false
    };
    index_oldcount = 0;
}

function setNaviPara(type) {
    if (type === 'walk') {
        voice_direction_level0 = 2;
        voice_direction_level1 = 8;
        voice_direction_level2 = 20;
        voice_direction_level3 = 30;
    } else if (type === 'drive') {
        voice_direction_level0 = 5;
        voice_direction_level1 = 22;
        voice_direction_level2 = 44;
        voice_direction_level3 = 60;
    }
}

async function resetNaviRoute(coord) {
    if (!navi) return;

    errorAngleIndex = 0; // 前进角度问题
    triggerErrorAngleTip = true; // 偏航后,再次规划路径后,再进行方向判断

    resetNewCurrentPosition(); // 偏航时，清除定位历史记录-解决偏航时会结束导航问题
    coord = currentCoord;
    navi.clearAll();
    removeLineMarker();
    positionList = [];
    pathStartEndPoints = [];
    if (!placeNavi.place_state && naviType == 'drive') {
        if (finish) {
            // destination = finish;
        }
    }
    let priority = fengmap.FMNaviPriority.PRIORITY_DEFAULT;
    if (naviType == 'drive') {
        priority = fengmap.FMNaviPriority.PRIORITY_ESCALATORONLY;
    }
    // if (destination.type == 'outdoor' || destination.type == "exit") {
    if (destination.type == 'outdoor') {
        let mindis = 10000000000;
        let near_exit = null;
        let hasSameFloor = false;
        for (let i = 0; i < allExit.length; i++) {
            allExit[i].level = +allExit[i].floor;
            let proResult = await new Promise((resolve, reject) => {
                searchNearModel(currentCoord, (startCoord) => {
                    naviAnalyser.route({
                        start: {
                            x: startCoord.x,
                            y: startCoord.y,
                            level: startCoord.level
                        },
                        dest: {
                            x: +allExit[i].x,
                            y: +allExit[i].y,
                            level: allExit[i].level
                        },
                        mode: fengmap.FMNaviMode.MODULE_BEST,
                        priority
                    }, (result) => {
                        resolve({
                            res: "success",
                            data: {
                                item: allExit[i],
                                result
                            }
                        });
                    }, (fail) => {
                        resolve({
                            res: "fail"
                        });
                    });
                });
            });
            if (proResult.res == "success") {
                let { item, result } = proResult.data;
                let dis = result.distance;
                if (currentCoord.groupID != +item.floor) {
                    dis = dis + 100000000;
                } else {
                    hasSameFloor = true;
                }
                if (mindis > dis) {
                    mindis = dis;
                    near_exit = item;
                }
            }
        }
        if (near_exit) {
            destination.x = +near_exit.x;
            destination.y = +near_exit.y;
            destination.level = +near_exit.floor;
            if (destination.type == 'outdoor' && outdoorInfo) {
                destination.name = near_exit.name + "(室外地点:" + outdoorInfo.name + ")" + "(" + fMap.getFloor(near_exit.level).name + "层)"
            } else {
                if (near_exit.name.indexOf('层)') == -1) {
                    destination.name = near_exit.name + "(" + fMap.getFloor(near_exit.level).name + "层)"
                } else {
                    destination.name = near_exit.name
                }
            }
            newdestination = destination
        }
    }
    let proResult1 = await new Promise((resolve, reject) => {
        searchNearModel(currentCoord, (startCoord) => {
            try {
                naviAnalyser.route({
                    start: {
                        x: startCoord.x,
                        y: startCoord.y,
                        level: startCoord.level
                    },
                    dest: {
                        x: destination.x,
                        y: destination.y,
                        level: destination.level
                    },
                    mode: fengmap.FMNaviMode.MODULE_BEST,
                    priority
                }, (result) => {
                    resolve({
                        res: "success"
                    });
                }, (fail) => {
                    resolve({
                        res: "fail"
                    });
                });
            } catch (error) {
                resolve({
                    res: "fail"
                });
            }

        });
    });
    if (proResult1.res == "fail") {
        if (!placeNavi.place_state && naviType == "drive") {
            let near_elevator = findNaviResult(currentCoord);
            // 楼层大于f1
            let m_level, m_floorName = '';
            if (currentCoord && currentCoord.x) {
                m_level = currentCoord.level || currentCoord.groupID;
                m_floorName = fMap.getFloor(+m_level).name;
            }
            if (getFloorNamePassF1(m_floorName)) {
                // 楼层大于f1-不做处理
            } else {
                if (near_elevator.hasRes && finish) {
                    finish.x = near_elevator.x;
                    finish.y = near_elevator.y;
                    finish.level = near_elevator.level;
                    let elevator = getTargetNearElevator(near_elevator);
                    if (elevator) {
                        // finish.name = destination.name;
                        if (elevator.name.indexOf('层)') == -1) {
                            newdestination.name = elevator.name + "(" + fMap.getFloor(+elevator.level).name + "层)";
                        } else {
                            newdestination.name = elevator.name
                        }
                    } else {
                        finish.name = "电梯" + "(" + fMap.getFloor(near_elevator.level).name + "层)" + destination.name;
                        newdestination.name = "电梯" + "(" + fMap.getFloor(near_elevator.level).name + "层)";
                    }
                } else {
                }
            }


        };
    }
    updateNaviUI();
    //searchNearModel(coord, null, true);

    if (newdestination && newdestination.x) {
        if (destination.type == 'outdoor' || destination.type == "exit") {
            // 添加图片标注
            /* 构造 Marker */
            if (imageMarker) {
                imageMarker.remove();
            }
            imageMarker = new fengmap.FMImageMarker({
                x: +destination.x,
                y: +destination.y,
                // url: 'https://developer.fengmap.com/fmAPI/images/blueImageMarker.png',
                url: './image/FMImageMarker.png',
                size: 30,
                height: 2,
                collision: false
            });
            const floor = fMap.getFloor(+destination.level)
            imageMarker.addTo(floor);
        }
        newnaviRoute(coord, null, null, newdestination, null, null, true, true);
    } else {
        newnaviRoute(coord, null, null, finish, null, null, true, true);
    };

    let start_obj = {
        x: coord.x,
        y: coord.y,
        floor: coord.level
    };
    let end_obj = {
        x: finish && finish.x ? finish.x : destination.x,
        y: finish && finish.y ? finish.y : destination.y,
        floor: finish && finish.level ? finish.level : destination.level,
    };
    let send = {
        speed: 10,
        target: finish && finish.name ? finish.name : destination.name,
        startX: start_obj.x,
        startY: start_obj.y,
        startFloor: start_obj.floor,

        endX: end_obj.x,
        endY: end_obj.y,
        endFloor: end_obj.floor,

        map: mapInfo.mapId,
    };
    sendPosition(send);
}

function navTextChange(navi, data) {
    let distancetoNext_new = Math.round(data.distancetoNext * 0.7);
    let remain_new = data.remain;
    direction = '直行';
    image_index = 4;
    let index = data.index;
    let subs = navi.naviResult.subs;
    let len = navi.naviResult.subs.length;
    let instruction = subs[index].instruction.zh;
    miter = '', miterStr = '';
    // let miter = instruction.match(/\d+/g)[0];
    // let miterStr = '米'
    let points;
    if (index + 1 < len) {
        points = subs[index + 1].waypoint.points;
    }
    let leave_sumdis = 0;
    if (len == 1) {
        distancetoNext_new = Math.round(remain_new * 0.7);
    } else {
        for (var i = index + 1; i < len; i++) {
            let navData = subs[i];
            if ((typeof navData.distance) != 'undefined') {
                leave_sumdis = navData.distance + leave_sumdis;
            }
        }
        distancetoNext_new = Math.round((remain_new - leave_sumdis) * 0.7);
    }
    if (instruction.indexOf('左转') != -1 || instruction.indexOf('左后') != -1) {
        direction = '左转';
        image_index = 10;
    } else if (instruction.indexOf('左前') != -1) {
        if (naviType == 'drive') {
            direction = "向左前方行驶";
        } else {
            direction = "向左前方行进";
        }
        image_index = 5;
    }
    if (instruction.indexOf('右转') != -1 || instruction.indexOf('右后') != -1) {
        direction = "右转";
        image_index = 11;
    } else if (instruction.indexOf('右前') != -1) {
        if (naviType == 'drive') {
            direction = "向右前方行驶";
        } else {
            direction = "向右前方行进";
        }

        image_index = 6;
    }
    if ((index + 1) < len && subs[index + 1].distance == 0) {
        let levels = subs[index + 1].levels;
        let lift_floor = fMap.getFloor(levels[levels.length - 1]).name + "层";
        let start = [points[0].x, points[0].y, 2];
        let end = [points[points.length - 1].x, points[points.length - 1].y, 2];
        let cal_dis = calculate_two_near_BeaconDis(start, end);
        let elevator = getTargetNearElevator(points[0]);
        let lift_type = "电梯";
        if (elevator) {
            let lift_name = elevator.name;
            if (lift_name.indexOf("电") != -1) {
                lift_type = lift_name;
            } else if (lift_name.indexOf("步") != -1) {
                lift_type = lift_name;
            }
        }
        miter = '';
        miterStr = '';
        if (levels[0] < levels[levels.length - 1]) {
            if (naviType == 'drive') {
                direction = '上坡';
                image_index = 2;
            } else if (cal_dis > 4) {
                direction = '上坡';
                image_index = 2;
            } else {
                direction = "乘" + lift_type + "到" + lift_floor;
                image_index = 8;
            }
        } else {
            if (naviType == 'drive') {
                direction = '下坡';
                image_index = 1;
            } else if (cal_dis > 4) {
                direction = '下坡';
                image_index = 1;
            } else {
                direction = "乘" + lift_type + "到" + lift_floor;
                image_index = 9;
            }
        }
    }
    if (direction.indexOf('梯') != -1) {
        miter = '';
        miterStr = '';
        distance = Math.round(remain_new * 0.7);
    } else {
        miter = distancetoNext_new > 0 ? distancetoNext_new : '';
        miterStr = distancetoNext_new > 0 ? '米' : '';
        distance = Math.round(remain_new * 0.7);
    }

    updateNaviUI();
    tts(navi, data, distancetoNext_new);
}


function tts(navi, data, dis_new) {
    if (returnTTS) return;

    let index = data.index;
    let remain_new = data.remain;
    let subs = navi.naviResult.subs;
    let refresh_speech = false;
    let voiceText = miter + miterStr + direction;
    let first_path = navi.naviResult.subs[0].distance;
    let current_path = Math.round(navi.naviResult.subs[index].distance * 0.7);
    let dis_tonext_cross = '前方';
    let end_cross_text = getPath_end_text(subs[index].waypoint.points);
    let speech_new = '';
    // 同一路段厂路段中间播报

    if (index_old < index) {
        dis_old = 9999;
    } else { };

    if (!refresh_speech && (voice_direction_level1 > dis_new) && (dis_old > voice_direction_level1)) { // 是否到达了路尽头
        if (!(typeof (navi.naviResult.subs[index].distance) == "undefined")) {
            refresh_speech = true;
            dis_old = dis_new;
            speech_new = dis_tonext_cross + direction;
            speech_new = dis_tonext_cross + end_cross_text[2] + direction + end_cross_text[3];
        }
    }


    if ((!refresh_speech) && (index_old == index)) {
        if (!(typeof (navi.naviResult.subs[index].distance) == "undefined")) {
            let max_dis = Math.round(navi.naviResult.subs[index].distance * 0.7);
            if ((max_dis > voice_direction_level3) && (dis_new < max_dis * 2 / 3)
                && (dis_old > max_dis * 2 / 3)) {
                refresh_speech = true;
                speech_new = dis_tonext_cross + Math.round(dis_new) + "米" + direction;
                dis_old = dis_new;
            }
        }
    }

    //切换到新路径，且路径长度较长
    if ((index_old < index) && !refresh_speech) {
        //新路径过短时不播报
        if (first_path < (voice_direction_level0) && index == 0 && naviType === 'drive') { // 开始导航时新路径过短，不播报
            speech_new = '';
        } else {
            if (Math.abs(current_path - dis_new) < 10 && index > 0)
                speech_new = dis_tonext_cross + Math.round(dis_new) + "米" + end_cross_text[0] + direction + end_cross_text[1];
            else if (dis_new < voice_direction_level1) {
                speech_new = dis_tonext_cross + Math.round(dis_new) + "米" + end_cross_text[2] + direction + end_cross_text[3];
            } else {
                speech_new = dis_tonext_cross + Math.round(dis_new) + "米" + direction;
            }
        }

        dis_old = dis_new;
        refresh_speech = true;
        index_old = index;
    };
    if (current_path <= voice_direction_level0 && naviType === 'drive') {
        refresh_speech = false;
    }
    remain_old = remain_new * 0.7;
    index_old = index;

    if (refresh_speech) {
        // 语音提示
        if (speech_new.length > 0) {

            setTimeout(() => {
                let bbb = {
                    todo: "navigatip",
                    data: {
                        str: speech_new,
                        path: index_old
                    },
                    time: new Date().getTime(),
                };
                apiPostData2(bbb);
            }, 160);

            // mqttPushMessage({
            //     todo: "navigatip",
            //     data: {
            //         str: speech_new,
            //         path: index_old
            //     }
            // })
        }
    } else {
        return null;
    }
}


function getPath_end_text(points) {
    let startPoint = points[0];
    let endPoint = points[points.length - 1];
    let near_dis = 7;
    let min_dis = 500;
    let text = ['', '', '', ''];
    if (markerPoint != null) {
        let len = markerPoint.length;
        for (let k = 0; k < len; k++) {
            let cross = markerPoint[k];
            let end = [endPoint.x, endPoint.y, endPoint.level];
            let start = [startPoint.x, startPoint.y, startPoint.level];
            let pos2 = [cross.x, cross.y, cross.floor];
            if (end[2] == start[2] && end[2] == pos2[2]) {
                let a = calculate_two_near_BeaconDis(start, pos2);
                let b = calculate_two_near_BeaconDis(end, pos2);
                if (b < near_dis) {
                    if (min_dis > (a + b)) {
                        min_dis = a + b;
                        let array = cross.interiorName.split(":");
                        text[0] = array[0];
                        text[1] = array[1];
                        if (array.length == 2) {
                            text[2] = array[0];
                            text[3] = array[1];
                        } else {
                            text[2] = array[2];
                            text[3] = array[3];
                        }
                    }
                }
            }
        }
    }

    return text;

}

function addFMapControl(y) {

    $('.emptybox').css('top', y + 'px');
    /* 两边的按钮-距离底部的距离 */
    if (y == 10) {
        $(".operate").css('bottom', '66.6667vw')
    } else {
        $(".operate").css('bottom', '58.6667vw')
    }


    // 指南针
    if ($('.fm-compass-container').length < 1) {
        var scrollCompassCtlOpt = {
            position: fengmap.FMControlPosition.Left_TOP,
            offset: {
                x: 13,
                y: y
                // y: 10
                // y: 110
            },
        };
        compass = new fengmap.FMCompass(scrollCompassCtlOpt);
        compass.addTo(fMap);
        compass.on('click', function () {
            fMap.setRotation({
                rotation: 0,
                animate: true,
                duration: 0.3,
                finish: function () { }
            })
        });
    };

    // 楼层控件
    // if ($('.fm-control-groups').length < 1) {
    //     var scrollFloorCtlOpt = {
    //         position: fengmap.FMControlPosition.RIGHT_TOP,
    //         floorButtonCount: 3,
    //         offset: {
    //             x: -7,
    //             y: y
    //             // y: 10,
    //             // y: 110
    //         },
    //         viewModeControl: false,
    //         floorModeControl: true,
    //         needAllLayerBtn: true
    //     };
    //     scrollFloorControl = new fengmap.FMToolbar(scrollFloorCtlOpt);
    //     scrollFloorControl.addTo(fMap);
    // };

    // 自定义楼层控件
    customFloorControls(y);
    // 设置初始聚焦楼层Dom样式
    setLevelDom();

    let hasVisibleLevels = ($('.fm-layer-image-one').length) || ($('.fm-layer-image-many').length);
    if (!hasVisibleLevels) {
        // 设置初始显示楼层
        setVisibleLevelsClick();
    };
};

var customLevel;
var customLevels;
var floorObj = {};  // 楼层信息
var activeFloor;    // 当前聚焦楼层DOM

function customFloorControls(y) {

    // 设置html
    let html = `
        <div class="fm-control-groups customFloorGroupsBox" id="customFloorGroupsBox">
            <div class="fm-layer" onclick="setVisibleLevelsClick()">
                <div id="fm-layer"> </div>
            </div>

            <div class="fm-floor-list-group customFloorGroupBox" id="customFloorGroupBox">
                <div class="upIcon fm-floor-list-btn" onclick="switcher('up')"><span class="upIconItem"></span></div>
                <div class="fm-floor-list"></div>
                <div class="nextIcon fm-floor-list-btn" onclick="switcher('down')"><span class="nextIconItem"></span></div>
            </div>
        </div>
    `;

    let hasFloorBox = document.getElementById('customFloorGroupsBox');

    if (hasFloorBox) {
        let inset = `${y}px 7px auto auto`;
        $(".customFloorGroupsBox").css('inset', inset)
    } else {
        $('body').append(html);


        // 获取楼层信息
        customLevel = fMap.getLevel();
        customLevels = fMap.getLevels();

        // 获取所有楼层信息
        let floorInfos = fMap.getFloorInfos().reverse()

        floorInfos.forEach(item => {
            floorObj[item.level] = item.name
        })
        let dom = document.getElementsByClassName('fm-floor-list')[0];
        $(".fm-floor-list").html('');

        customLevels.reverse().forEach((item) => {

            var labelEle = document.createElement('label')
            var textEle = document.createTextNode(floorObj[item])

            // 增加楼层Dom点击事件
            labelEle.onclick = function setLevelClick() {
                customLevel = item
                // 设置聚焦楼层
                fMap.setLevel({
                    level: item,
                })
                // 更改样式
                if (activeFloor) {
                    var activeDom = document.getElementById(activeFloor);
                    activeDom.classList.remove('fm-floor-name-active');
                    activeDom.classList.add('fm-floor-name-normal');
                }
                var dom = document.getElementById(floorObj[item]);
                dom.classList.remove('fm-floor-name-normal');
                dom.classList.add('fm-floor-name-active');
                activeFloor = floorObj[item];

                // 开始导航且是跟随模式
                if (freeTimer) {
                    clearTimeout(freeTimer);
                    freeTimer = null;
                }
                initFixedView(false);

                // if (naviStart) {
                //     clearTimeout(freeTimer);
                //     freeTimer = null;
                //     initFixedView(false);
                //     isClickFloorControls = true;
                //     freeTimer = setTimeout(() => {
                //         isClickFloorControls = false;
                //         initFixedView(true);
                //     }, 8000);
                // };

                let bound = initBounds[customLevel];
                console.log('bound', bound);

                fMap.setFitView(bound, false, () => { });

                // 手动切换楼层，设置底图
                oldLevel = JSON.parse(JSON.stringify(newLverl));
                newLverl = customLevel;
                if (oldLevel != newLverl) {
                    // 当前楼层为F1时添加瓦片底图
                    SetTileLayerMode(newLverl);
                }
            }

            // 楼层Dom显示样式
            labelEle.id = floorObj[item]
            labelEle.classList.add('fm-floor-name');
            labelEle.classList.add('fm-floor-name-normal');
            labelEle.appendChild(textEle)
            dom.appendChild(labelEle)

            var hrEle = document.createElement('hr')
            hrEle.classList.add('fm-floor-line');
            dom.appendChild(hrEle)
        });
        dom.removeChild(dom.childNodes[dom.childNodes.length - 1])
    }
    switcherColor();
};

// 点击切换楼层
function switcher(type) {
    let level = fMap.getLevel();
    let levels = fMap.getLevels();
    const targetId = level + (type === 'up' ? 1 : -1)
    const targetFloor = levels.find((item) => item === targetId)
    if (targetFloor) {
        // 设置初始聚焦楼层Dom样式
        setLevelDom();

        fMap.setLevel({ level: targetId })

        if (activeFloor) {
            var activeDom = document.getElementById(activeFloor);
            activeDom.classList.remove('fm-floor-name-active');
            activeDom.classList.add('fm-floor-name-normal');
        }
        var dom = document.getElementById(floorObj[targetId]);
        dom.classList.remove('fm-floor-name-normal');
        dom.classList.add('fm-floor-name-active');
        activeFloor = floorObj[targetId];

        $(".upIcon .upIconItem").removeClass('border-bottom-color-none');
        $(".nextIcon .nextIconItem").removeClass('border-top-color-none');
        if (targetId == Math.min(...levels)) {
            $(".nextIcon .nextIconItem").addClass('border-top-color-none');
        };
        if (targetId == Math.max(...levels)) {
            $(".upIcon .upIconItem").addClass('border-bottom-color-none');
        }

        // 全览
        if (clickDropTap) {
            // let bound = fMap.getFloor(targetId).bound;
            let bound = initBounds[targetId];
            fMap.setFitView(bound, false, () => { });
        }

        newLverl = targetId;
        if (oldLevel != newLverl) {
            // 当前楼层为F1时添加瓦片底图
            SetTileLayerMode(newLverl);
        };
    };
};

// 设置可切换楼层按钮样式
function switcherColor() {
    let level = fMap.getLevel();
    let levels = fMap.getLevels();
    const targetId = level;
    const targetFloor = levels.find((item) => item === targetId)
    if (targetFloor) {
        $(".upIcon .upIconItem").removeClass('border-bottom-color-none');
        $(".nextIcon .nextIconItem").removeClass('border-top-color-none');
        if (targetId == Math.min(...levels)) {
            $(".nextIcon .nextIconItem").addClass('border-top-color-none');
        };
        if (targetId == Math.max(...levels)) {
            $(".upIcon .upIconItem").addClass('border-bottom-color-none');
        }
    };
};

// 获取当前楼层面积--不确定正确
function getFloorArea() {
    let level;
    if (currentCoord && currentCoord.x) {
        level = +currentCoord.level
    } else {
        level = fMap.getLevel();
    };

    nowShowZoomLevel = level;


    let bound = fMap.getFloor(level).bound;
    const center = {
        x: bound.center.x,
        y: bound.center.y
    };
    const area = fengmap.FMCalculator.area(fengmap.FMCalculator.rectangleBuilder(
        bound.size.x, bound.size.y, center));

    switch (true) {
        case (area > 20000):
            setfMapZoom(20.3)
            break;
        case (area > 6000):
            setfMapZoom(20.8)
            break;
        case (area > 2000):
            setfMapZoom(21.5)
            break;
        default:
            setfMapZoom(21.8)
            break;
    }
};

// 根据楼层面积设置地图缩放
function setfMapZoom(zoom) {
    var currentZoom = fMap.getZoom();
    if (currentZoom != zoom) {
        fMap.setZoom({
            zoom: +zoom,
            animate: false
        });
    };

    let tilt = 0;
    switch (zoom) {
        case (21.8):
            tilt = 75;
            break;
        case (21.5):
            tilt = 70;
            break;
        case (20.8):
            tilt = 60;
            break;
        default:
            tilt = 55;
            break;
    };

    fMap.setTilt({
        tilt: tilt,
        animate: false,
        duration: 0,
        finish: function () { }
    })
};

function setLevelDom() {
    // 更改样式
    if (activeFloor) {
        var activeDom = document.getElementById(activeFloor);
        activeDom.classList.remove('fm-floor-name-active');
        activeDom.classList.add('fm-floor-name-normal');
    }

    // 以下代码更改dom样式
    var dom = document.getElementById(floorObj[customLevel]);

    let scrollDom = document.getElementsByClassName('fm-floor-list')[0];
    // scrollDom.scrollTo({ top: dom.offsetTop - dom.clientHeight - 20, behavior: 'smooth' });
    scrollDom.scrollTo({ top: dom.offsetTop - dom.clientHeight - 20 });

    dom.classList.remove('fm-floor-name-normal');
    dom.classList.add('fm-floor-name-active');
    activeFloor = floorObj[customLevel];
};

// 是否多楼层
var isMany = false;
function setVisibleLevelsClick() {

    // 获取最新的楼层层级信息
    let level = fMap.getLevel()
    let levels = fMap.getLevels()

    // 设置显示楼层
    let viewMode = isMany ? fMap.setVisibleLevels(levels) : fMap.setVisibleLevels([level]);

    // 设置每个楼层的标签
    if (!levelsName.length) {
        levels.forEach((item) => {
            let floor = fMap.getFloor(item);
            // 楼层的中心位置
            let center = floor.center;
            // 楼层的名字
            let name = floor.name;

            let html = `
                <div class="fm-control-popmarker2">
                <span class="fm-control-popmarker-bot2"></span>
                <span class="fm-control-popmarker-top2"></span>
                    <div id="info">
                        <div class="content">${name}</div>
                    </div>
                </div>
            `;
            var domwindow = new fengmap.FMDomMarker({
                x: +center.x,
                y: +center.y,
                height: 5,
                content: html,
                // anchor: fengmap.FMMarkerAnchor.BOTTOM
                domWidth: 30,
                domHeight: 25
            });
            domwindow.addTo(floor);
            domwindow.visible = false;
            levelsName.push(domwindow)
        })
    }

    // 以下代码更改dom样式
    let classVal = isMany ? 'fm-layer-image-many' : 'fm-layer-image-one';
    var dom = document.getElementById('fm-layer');
    let fmlayerText = isMany ? '多层' : '单层';
    if (isMany) {
        dom.classList.remove('fm-layer-image-one');

        fMap.setTilt({
            tilt: 0,
            animate: false,
            duration: 0,
            finish: function () {
                // 指北
                fMap.setRotation({
                    rotation: 0,
                    animate: false,
                    duration: 0,
                    finish: function () {

                        // 设置全览模式
                        let bound = fMap.bound;
                        fMap.setFitView(bound, {
                            animate: false,
                            finish: function () {
                                /* 设置为3D */
                                initTD(true)
                            }
                        })
                    }
                })

                // 不显示瓦片底图 
                if (tileLayer != null) {
                    tileLayer.remove(fMap);
                    tileLayer = null;
                };
            }
        });

        // 显示楼层标签
        levelsName.forEach((item) => {
            item.visible = true;
        })

    } else {
        dom.classList.remove('fm-layer-image-many');

        let nowlevel = fMap.getLevel();
        SetTileLayerMode(nowlevel, 'immediately');

        // 隐藏楼层标签
        levelsName.forEach((item) => {
            item.visible = false;
        })
    };

    let hasFText = $("#fmlayerText").length;
    if (hasFText) {
        let div = `<div class="fmlayerText" id="fmlayerText">${fmlayerText}</div>`;
        $("#fmlayerText").html(fmlayerText)
    } else {
        let div = `<div class="fmlayerText" id="fmlayerText">${fmlayerText}</div>`;
        $(".fm-layer").append(div)
    }

    dom.classList.add(classVal);
    isMany = !isMany
};


function updateNaviUI() {
    if (firstUpdateNaviUI) {
        compass.remove();
        addFMapControl(85);
        firstUpdateNaviUI = false;
    }

    $('#naviTip').html(`<div class="naviTip">
        <img src="${imagePath}${enterNaviArr[image_index]}" alt="" class="${image_index < 0 ? 'disNone' : ''}">
        <div class="navTipTxt">
            <span class="naviTipTxtL">${miter}</span>
            <span class="navTipTxtM">${miterStr}</span>
            <span class="navTipTxtR">${direction}</span>
        </div>
    </div>`);

    let html = `<div class="footer-box">`;
    let isName = (newdestination && newdestination.name) ? newdestination.name : (!destination.name || destination.name == 'undefined') ? '' : destination.name;
    console.log('isName', isName);

    let showName;
    if (isName) {
        let index = isName.indexOf('(');
        if (index != -1) {
            showName = [isName.slice(0, index), isName.slice(index)]
        }
    }

    if (showName) {
        html += `<div class="enterNaviTitle">${showName[0] + `<span style="color:#686868">${showName[1]}</span>`}</div> `
    } else if (isName == '车辆停放处') {
        html += `<div class="enterNaviTitle">您的车停在<span style="color:#686868">(${fMap.getFloor(+destination.level || +newdestination.level).name}层)</span></div> `
    }


    html += `<div class="description">距目的地剩余:${distance}米</div>
            <div class="navi-btn"> 
                <div class="navi-end ml" onclick="showExitTips(true)">退出</div>
            </div>
        </div>`

    $("#footerShowType3").html(html);
    if (isUpdateNaviUI) {
        $(".naviTip").addClass("animation_hide2");
        setTimeout(() => {
            $(".naviTip").addClass("animation_show");
        }, 300);
        $("#footerShowType3").addClass("animation_show");
        isUpdateNaviUI = false
    }
}

function warning_initPost() {
    if (final_location_time == '') {
        return true;
    } else {
        return false;
    }
}
function showNavi(type) {
    if (currentCoord && !currentCoord.x) {
        noCurrentCoord();
        // 车位释放
        parkingSpaceRelease();
        return
    }
    var keyname = destination.name.split('(')[1] || '';
    var keynamefu = destination.name.split('(')[1] || '';
    var ketnamemai = keynamefu.slice(0, 4);
    var anamelength = keyname.length
    var aname = keyname.substring(anamelength - 3, anamelength)
    naviType = type;
    if (type == 'walk' || destination.type == 'exit' || destination.type == 'outdoor' || keyname.indexOf('出口') != -1 || keyname.indexOf('出入口') != -1 ||
        aname === '出口)' || aname === '入口)' || ketnamemai === '室外地图' || destination.type === 'search_exit') {
        searchNearModel(currentCoord, null);
    } else {
        // 当前楼层是否在f1以上
        // 当前人所在楼层
        let m_level, m_floorName = '';
        if (currentCoord && currentCoord.x) {
            m_level = currentCoord.level || currentCoord.groupID;
            m_floorName = fMap.getFloor(+m_level).name;
        };
        if (getFloorNamePassF1(m_floorName)) {
            toolTipBox('当前楼层不支持驾车模式');
            return false;
        } else {
            showDedtiOrPlaceDialog(true);
        }
    }
}

function showDedtiOrPlaceDialog(flag) {
    if (flag) {
        if (isViPreservation || noShowDriveList) {
            // vip-驾车-导航
            gotoDestination();
            return false
        }

        $("#dedtiOrPlaceDialog").html(`<div class="dedtiOrPlaceDialog">
            <div class="dialog-mask animation_opac"></div>
            <div class="dialog-container animation_scale">
                <div class="dialogBody" style="overflow:hidden">
                    <span class="dialogBtn setBackgroung0" ontouchstart="setBackground(0)" ontouchend="gotoPlace(0)"><i>找</i>目的地附近普通车位</span>
                    <span class="dialogBtn setBackgroung3" ontouchstart="setBackground(3)" ontouchend="gotoPlace(3)"><i>找</i>目的地附近无障碍车位</span>
                    <span class="dialogBtn setBackgroung1" ontouchstart="setBackground(1)" ontouchend="gotoPlace(1)"><i>找</i>目的地附近充电桩车位</span>
                    <span class="dialogBtn setBackgroung" ontouchstart="setBackground('')" ontouchend="gotoDestination()"><i>去</i>目的地</span>
                </div>
                <div class="dialogClose" onclick="showDedtiOrPlaceDialog(false)"><img src="http://112.94.22.123:10087/rtls/wechat/close_white.png" alt=""></div>
            </div>
        </div>`);

        setTimeout(() => {
            $("#dedtiOrPlaceDialog .dialog-container").addClass("animation_show2")
            $("#dedtiOrPlaceDialog .dialog-mask").addClass("animation_show3")
        }, 30);
    } else {
        $("#dedtiOrPlaceDialog .dialog-container").removeClass("animation_show2");
        $("#dedtiOrPlaceDialog .dialog-mask").removeClass("animation_show3");
        setTimeout(() => {
            $("#dedtiOrPlaceDialog").html('');
        }, 200);
    }
}

function showDestiNaviInfo(flag) {
    noClick = true;
    if (flag) {
        let html = '<div class="footer-box">';
        let isName = newdestination.name ? newdestination.name : (!destination.name || destination.name == 'undefined') ? '' : destination.name;
        let showName;
        if (isName) {
            let index = isName.indexOf('(');
            if (index != -1) {
                showName = [isName.slice(0, index), isName.slice(index)]
            }
        }

        if (showName) {
            html += `<div class="outerNaviTitle">目的地：${showName[0] + `<span style="color:#686868">${showName[1]}</span>`} 距终点<span class="outerNaviMeter">${distance}</span>米</div> `
        } else if (isName == '车辆停放处') {
            html += `<div class="outerNaviTitle">您的车停在<span style="color:#686868">(${fMap.getFloor(+destination.level || +newdestination.level).name}层)</span> 离您<span class="outerNaviMeter">${distance}</span>米</div> `
        } else {
            html += `<div class="outerNaviTitle">${isName}<span style="color:#686868">(${fMap.getFloor(+destination.level || +newdestination.level).name}层)</span> 距终点<span class="outerNaviMeter">${distance}</span>米</div> `
        }

        html += `
        <div class="navi-btn">
            <div class="navi-box">
                <div class="navi-end" onclick="hideNavi()">退出</div>
                <div class="navi-start" onclick="switchNavi()">开始导航</div>
            </div>
        </div></div>
        `

        $("#footerShowType3").html(html)
        setTimeout(() => {
            showFooterShowType1(false);
            $("#footerShowType3").addClass("animation_show")
        }, 30);
    } else {
        $("#footerShowType3").removeClass("animation_show");
        $("#footerShowType3").html('');
    }
}
function calculate_two_near_BeaconDis(pos0, pos1) {
    let dis = Math.sqrt(Math.pow(pos0[0] - pos1[0], 2) + Math.pow(pos0[1] - pos1[1], 2))
    return dis;
}
function switchNavi() {
    errorAngleIndex = 0; // 前进角度问题
    triggerErrorAngleTip = true;
    positionList = [];

    // 偏航规划重置
    restoreAnimation = false;
    returnTTS = false;

    // 隐藏查看室内地图位置
    $("#quickPosition").css('display', 'none')

    // 开始导航时，清除定位历史记录
    positionHistory = [];

    var direction = "开始导航";

    let bbb = {
        todo: "navigatip",
        data: {
            str: direction,
            path: -1
        },
        time: new Date().getTime(),
    };
    apiPostData2(bbb)
    // mqttPushMessage({
    //     todo: "navigatip",
    //     data: {
    //         str: direction,
    //         path: -1
    //     }
    // })
    //setTimeout(function() {
    naviStart = true;
    if (naviType == 'drive') {
        queue_len = 3;
    } else {
        queue_len = 6;
    }

    showDestiNaviInfo(false);
    initFixedView(true);

    image_index = -1;
    miter = '';
    miterStr = '';

    updateNaviUI();
    navi.locate({
        x: currentCoord.x,
        y: currentCoord.y,
        level: currentCoord.groupID || currentCoord.level
    });
    setNaviPara(naviType);
    //},200);

    if (naviType == 'drive') {
        // 驾车导航-统计导航次数
        countNavigationTimes();
    };
    // 电梯导航
    // countNavigationTimes1();

    if (QRCodeCategory != '') {
        // 推广导航记录
        promotionNavigationRecords();
    };

    let stateText;
    if (destination.type == "exit" || destination.type == "outdoor" || destination.type == "exit2") {
        stateText = '出口导航';
    } else if (destination.autoMark) {
        stateText = '找车位';
    } else {
        stateText = '地点导航';
    }
    let send = {
        speed: 10,
        target: finish && finish.name ? finish.name : destination.name,
        state: stateText,
        nav_state: naviType === 'drive' ? '驾车导航' : '步行导航',
        startX: currentCoord.x,
        startY: currentCoord.y,
        startFloor: currentCoord.level,

        endX: finish && finish.x ? finish.x : destination.x,
        endY: finish && finish.y ? finish.y : destination.y,
        endFloor: finish && finish.level ? finish.level : destination.level,

        map: mapInfo.mapId,
    };

    // 用于walking时设置起终点
    stateNaviUserData = JSON.parse(JSON.stringify(send))

    sendPosition(send);
}

function hideNavi() {
    if (jumpOther && mapInfo.shortLink) {
        jumpOther = false;
        mqttPushMessage({
            todo: "jumpToOther",
        })
    }

    isNaviName = '';
    isViPreservation = false;
    noShowDriveList = false;
    isFirstNaviPoitNum = 1;
    newdestination = {};
    navi.clearAll();
    removeLineMarker();
    clearCirclM();// 清除圆
    showDestiNaviInfo(false);
    showFooterShowType2(null, false);
    setTimeout(() => {
        showFooterShowType1(true);
    }, 100);
    noClick = false;
    // 车位释放
    parkingSpaceRelease();
    // finish 中间位置清除
    finish = null;
}
async function searchFmap(keyword) {
    const params = {
        keyword,
        nodeType: fengmap.FMType.MODEL
    };
    let result = await searchByParams2(params);
    // 搜索地图数据排序
    let first = [];
    let end = [];
    result.forEach((item) => {
        let typeID = item.typeID;
        let is_place = contains(place_type, typeID);
        if (is_place) {
            end.push(item)
        } else {
            first.push(item)
        }
    });
    result = [...first, ...end];

    const maxLen = 200;
    if (result.length > maxLen) result.length = maxLen;
    let tworesult = result.map(item => ({
        name: item.name + "(" + fMap.getFloor(+item.level).name + "层)",
        ename: item.ename,
        x: item.center.x,
        y: item.center.y,
        level: item.level,
        id: 'f' + item.FID,
        typeID: item.typeID,
        nodeType: item.type,
        type_num: 5
    }));
    let tow_first = [];
    let tow_end = [];
    tworesult.forEach((item) => {
        let typeID = item.typeID;
        let is_place = contains(place_type, typeID);
        if (currentCoord && currentCoord.x) {
            let pos1 = {
                x: currentCoord.x,
                y: currentCoord.y,
            };
            let pos2 = {
                x: item.x,
                y: item.y,
            };
            let cal_dis = fengmap.FMCalculator.distance(pos1, pos2);
            item['cal_dis'] = cal_dis
        } else {
            item['cal_dis'] = null
        }
        if (is_place) {
            tow_end.push(item)
        } else {
            tow_first.push(item)
        }
    });
    tow_first.sort((a, b) => {
        return a.cal_dis - b.cal_dis
    });
    tow_end.sort((a, b) => {
        return a.cal_dis - b.cal_dis
    });

    searchResult = [...tow_first, ...tow_end];
    return searchResult;
}
async function searchByParams(params) {
    let sortRes = [];
    var proResult = await new Promise((resolve, reject) => {
        searchAnalyser = new fengmap.FMSearchAnalyser({ map: fMap }, function () {
            const searchRequest1 = new fengmap.FMSearchRequest();
            const searchRequest2 = new fengmap.FMSearchRequest();
            let obj = {};
            if (params.keyword) {
                // obj.keyword = params.keyword;
                obj.name = {
                    'text': params.keyword,
                    'fuzzy': true,
                    'matchCase': false
                }
            }
            //配置groupID参数
            if (params.level) {
                searchRequest1.levels = [params.level];
                searchRequest2.levels = [params.level];
            }
            //配置FID参数
            if (params.FID) {
                obj.FID = params.FID;
            }
            //配置typeID参数
            if (params.typeID != null) {
                obj.typeID = params.typeID;
            }
            //配置nodeType参数
            if (params.nodeType != null) {
                searchRequest1.type = params.nodeType;
                searchRequest2.type = params.nodeType;
            }

            searchRequest1.addCondition(obj);
            searchAnalyser.query(searchRequest1, (result) => {
                sortRes = result;
            });
            if (params.keyword == "出口") {
                obj.name = {
                    'text': '出入口',
                    'fuzzy': true,
                    'matchCase': false
                }
                searchRequest2.addCondition(obj);
                searchAnalyser.query(searchRequest2, (result) => {
                    sortRes = sortRes.concat(result);
                    resolve(sortRes);
                });
            } else {
                resolve(sortRes);
            }
        });
    });
    let res = [];
    sortRes.forEach(item => {
        if (item.FID) {
            res.push(item);
        }
    });
    return res;
};

async function searchByParams2(params) {
    let sortRes = [];
    var proResult = await new Promise((resolve, reject) => {
        searchAnalyser = new fengmap.FMSearchAnalyser({ map: fMap }, function () {
            const searchRequest1 = new fengmap.FMSearchRequest();
            let obj = {};
            if (params.keyword) {
                // obj.keyword = params.keyword;
                obj.name = {
                    'text': params.keyword,
                    'fuzzy': true,
                    'matchCase': false
                }
            }

            //配置nodeType参数
            if (params.nodeType != null) {
                searchRequest1.type = params.nodeType;
            }

            searchRequest1.addCondition(obj);
            searchAnalyser.query(searchRequest1, (result) => {
                sortRes = result.filter((item) => {
                    return item.typeID != '200401' && item.typeID != '340864' && item.typeID != '340863' && item.typeID != '340861' && item.typeID != '340862' && item.typeID != '340859' && item.typeID != '340860' && item.typeID != '340892' && item.typeID != '340890' && item.typeID != '340889' && item.typeID != '200001'
                });
                resolve(sortRes);
            });
        });
    });
    let res = [];
    sortRes.forEach(item => {
        if (item.FID) {
            res.push(item);
        }
    });
    return res;
}

function resNameList(str, keyword) {
    let keywordArr = keyword.split('');
    let strArr = str.split('');
    let result = [];
    let key = 1;
    for (let i = 0; i < strArr.length; i++) {
        let flag = false;
        keywordArr.forEach(item => {
            let regStr = new RegExp(item, "i");
            if (regStr.test(strArr[i])) {
                flag = true;
            }
        });
        result.push({
            key: key++,
            text: strArr[i],
            type: flag === true ? 'keyword' : 'text'
        });
    }
    return result;

    // let reg = new RegExp(keyword, 'i')
    // let data = str.match(reg);
    // if (data) {
    //     let index = data.index;
    //     let j = index + keywordArr.length;

    //     for (let i = 0; i < strArr.length; i++) {
    //         result.push({
    //             key: key++,
    //             text: strArr[i],
    //             type: (i >= index && i < j) ? 'keyword' : 'text'
    //         });
    //     }
    // } else {
    //     for (let i = 0; i < strArr.length; i++) {
    //         result.push({
    //             key: key++,
    //             text: strArr[i],
    //             type: false ? 'keyword' : 'text'
    //         });
    //     }
    // }
    // return result;
};

function getMateKeyLength(name, key) {
    let tName = name.split('(')[0];
    if (tName === key) {
        return 99
    }
    let keywordArr = key.split('');
    let len = 0;
    keywordArr.forEach((item) => {
        if (name.includes(item)) {
            len += 1
        }
    });
    return len
};

async function search(keyword) {
    keyword = keyword.trim().toUpperCase();
    if (!keyword.length) return;

    // fMapResult = await searchFmap(keyword); // 蜂鸟地图搜索
    var fMapResult1 = await getExitData(keyword); // 出入口
    var fMapResult2 = await getAdminElevatorData(keyword); // 电梯
    var fMapResult3 = await getAdminServiceFacilitiesData(keyword); // 厕所-安全出口
    var fMapResult33 = await getAdminServiceFacilitiesData2(keyword); // 室外物体
    var fMapResult4 = await getPlaceData(keyword); // 车位
    fMapResult.push(...fMapResult1, ...fMapResult2, ...fMapResult3, ...fMapResult33, ...fMapResult4)

    /* 设置距离 */
    fMapResult.forEach((item) => {
        if (currentCoord && currentCoord.x) {
            let pos1 = {
                x: currentCoord.x,
                y: currentCoord.y,
            };
            let pos2 = {
                x: item.x,
                y: item.y,
            };
            let cal_dis = fengmap.FMCalculator.distance(pos1, pos2);
            item['cal_dis'] = cal_dis
        } else {
            item['cal_dis'] = null
        };
        item['keyL'] = getMateKeyLength(item.name, keyword)
    });
    fMapResult.sort((a, b) => {
        return b.keyL - a.keyL
        // return a.cal_dis - b.cal_dis
    });

    const data = {
        map: mapInfo.mapId,
        keyword
    };

    if (!searchkeyTime) {
        searchkeyTime = setTimeout(() => {
            clearTimeout(searchkeyTime);
            searchkeyTime = null;

            searchTotal();
            changeApigetTargetByName(data);
        }, 500);
    }
};

function searchTotal() {
    // 统计搜索次数
    $.ajax({
        url: `${formal}emsbp/addUserSearchTotal`,
        type: 'POST',
        data: JSON.stringify({
            map: mapInfo.mapId,
            mapName: mapInfo.mapName,
            count: 1
        }),
        headers: {
            "Content-Type": "application/json"
        },
    })
};

async function changeApigetTargetByName(data) {
    var fMapResult1 = await changeApigetTargetByName1(data); // 商家
    var fMapResult2 = await changeApigetTargetByName2(data); // 公司
    let allresult = [];
    allresult.push(...fMapResult1, ...fMapResult2);

    let resultTemp = [];
    let param = data;
    keyword_name = data.keyword;
    let resData = allresult;

    resData.forEach(item => {
        if (!item.fid) return;
        let cal_dis;
        if (currentCoord && currentCoord.x) {
            let pos1 = {
                x: currentCoord.x,
                y: currentCoord.y,
            };
            let pos2 = {
                x: +item.x,
                y: +item.y,
            };
            cal_dis = fengmap.FMCalculator.distance(pos1, pos2);
        } else {
            cal_dis = null
        }
        resultTemp.push({
            type: 'fmap',
            id: item.id,
            name: item.name,
            sendName: true,
            nameList: resNameList(item.name, param.keyword),
            fid: item.fid,
            x: +item.x,
            y: +item.y,
            databaseId: item.id,
            groupID: +item.floor,
            desc: '类型：室内地点',
            icon: 'search-map-icon',
            type_num: item.types,
            dis: cal_dis,
        })
    });
    resultTemp.sort((a, b) => {
        return a.dis - b.dis
    });

    newresultTemp = JSON.parse(JSON.stringify(resultTemp));

    let mapPlaceSize = 0;
    fMapResult.forEach(item => {
        const typeID = item.typeID;
        const ename = resEname(item.ename, typeID);
        let icon = '';
        let type = '';
        let type_num = '';
        let is_place = contains(place_type, typeID);
        if (is_place) {
            icon = 'search-place-icon';
            type = 'place';
        } else {
            icon = 'search-map-icon';
            type = 'fmap';
        }
        mapPlaceSize++;
        if (mapPlaceSize < 10) {
            resultTemp.push({
                type,
                id: item.id,
                name: item.name,
                ename,
                nameList: resNameList(item.name, param.keyword),
                typeID,
                fid: item.fid,
                desc: '类型：室内地点',
                icon,
                x: item.x,
                y: item.y,
                groupID: item.groupID || item.level,
                type_num: item.type_num,
                databaseId: item.id,
                dis: item.cal_dis || item.dis
            })
        }
    });


    $.ajax({
        url: "https://restapi.amap.com/v3/assistant/inputtips",
        data: {
            key: '11b902de74442737fdd98e2af342e0ce',
            keywords: param.keyword,
            location: mapInfo.lng && mapInfo.lat ? mapInfo.lng + ',' + mapInfo.lat : "",
            datatype: 'all',
        },
        success: function (res) {
            outdoorList = [];
            const { status, tips } = res;
            if (status == '1') {
                tips.forEach(item => {
                    const {
                        id,
                        name,
                        cityname,
                        adname,
                        address,
                        type = '',
                        location,
                        district
                    } = item;
                    let addressTxt = '';
                    if (district) {
                        addressTxt += district;
                    }
                    if (typeof address == 'string') {
                        addressTxt += '-' + address;
                    };
                    if (typeof location == 'string') {
                        const locationArr = location.split(',');
                        outdoorList.push({
                            id,
                            name,
                            nameList: resNameList(name, param.keyword),
                            outdoorType: type.split(';')[2] || '',
                            addressTxt,
                            lng: +locationArr[0],
                            lat: +locationArr[1],
                            type: 'outdoor',
                            desc: addressTxt,
                            icon: 'search-outdoor-icon',
                            type_num: '2',
                            databaseId: '-1',
                        })
                    }
                });

                searchResult = resultTemp.concat(outdoorList);
                is_show_more = true;
                updateSearchPop(searchResult);
            }
        }
    });
};


// 清空输入框
function clearInput() {
    document.getElementById('searchInput').value = '';
    moreMapValue = '';
    $(".moreMapValue").html('搜索目的地')
    clearHide = true;
    showSearchPop(true, searchHistory);
};


var tt_timeout = null
async function searchInput(e) {
    /* 防抖 */
    if (tt_timeout) {
        clearTimeout(tt_timeout);
    }
    tt_timeout = setTimeout(async () => {
        searchResult = [];
        fMapResult = [];
        let value = $("#searchInput")[0].value;
        let navitem = document.getElementsByClassName('navitem');
        for (let i = 0; i < navitem.length; i++) {
            navitem[i].classList.remove('active')
        };
        if (value) {
            clearHide = false;
            await search(value);
        } else {
            clearHide = true;
        }
    }, 500);
};

function enterInput() {
    if (nowinputtype == 2 || nowinputtype == 4 || nowinputtype == 5) {
        let navitem = document.getElementsByClassName('navitem');
        for (let i = 0; i < navitem.length; i++) {
            navitem[i].classList.remove('active')
        };

        clearHide = true;
        updateSearchPop(searchHistory, false, false);
    };
};

function getSearchPop(flag) {
    $.ajax({
        url: `${formal}hot/getHotData?map=${mapInfo.mapId}&userId=${userId}`,
        success: function (res) {
            let resData = res.data;
            const list = resData.map(item => {
                let type = '';
                if (item.type == 0) {
                    type = 'place'
                } else if (item.type == 1) {
                    type = 'fmap'
                } else if (item.type == 2) {
                    type = 'outdoor'
                } else if (item.type == 3) {
                    type = 'company'
                }
                const x = +item.x;
                const y = +item.y;
                return {
                    id: item.id,
                    desc: item.desc,
                    addressTxt: item.desc,
                    fid: item.fid,
                    icon: item.icon,
                    name: item.name,
                    databaseId: item.databaseId,
                    ename: item.ename || '',
                    outdoorType: item.outdoorType,
                    sendName: item.type == 3 ? true : false,
                    type,
                    type_num: item.type,
                    x,
                    y,
                    groupID: item.floor,
                    //室外
                    lng: x,
                    lat: y,
                }
            })
            searchHistory = list;

            /* 处理出入口删除-改名后-删除历史记录 */
            searchHistory.forEach((item, index) => {
                if (item.type_num == 0) {
                    /* 车位 */
                    if (hadAllPlaces) {
                        if (!all_places.length) {
                            deleteDataIds.push(item.id)
                        } else {
                            let hasD = all_places.some((sitem) => sitem.fid == item.fid && (sitem.name.includes(item.name) || item.name.includes(sitem.name)));
                            if (!hasD) {
                                deleteDataIds.push(item.id)
                            }
                        }
                    };
                }

                if (item.type_num == 1) {
                    /* 出入口 */
                    if (hadAllExitData) {
                        if (!showIconExitData.length) {
                            deleteDataIds.push(item.id)
                        } else {
                            let hasD = showIconExitData.some((sitem) => sitem.fid == item.fid && (sitem.name.includes(item.name) || item.name.includes(sitem.name)));
                            if (!hasD) {
                                deleteDataIds.push(item.id)
                            }
                        }
                    };
                };

                if (item.type_num == 3) {
                    /* 商家 */
                    if (hadAllBusinessData) {
                        if (!onlyBusinessList.length) {
                            deleteDataIds.push(item.id)
                        } else {
                            let hasD = onlyBusinessList.some((sitem) => sitem.fid == item.fid && (sitem.name.includes(item.name) || item.name.includes(sitem.name)));
                            if (!hasD) {
                                deleteDataIds.push(item.id)
                            }
                        }
                    }
                };

                if (item.type_num == 6) {
                    /* 电梯 */
                    if (hadAllElevatorData) {
                        if (!elevatorData.length) {
                            deleteDataIds.push(item.id)
                        } else {
                            let hasD = elevatorData.some((sitem) => sitem.fid == item.fid && (sitem.name.includes(item.name) || item.name.includes(sitem.name)));
                            if (!hasD) {
                                deleteDataIds.push(item.id)
                            }
                        }
                    };
                };

                if (item.type_num == 7) {
                    /* 厕所-安全出口-室外物体 */
                    if (hadAllServiceData) {
                        if (!serviceFacilitiesData.length) {
                            deleteDataIds.push(item.id)
                        } else {
                            let hasD = serviceFacilitiesData.some((sitem) => sitem.fid == item.fid && (sitem.name.includes(item.name) || item.name.includes(sitem.name)));
                            if (!hasD) {
                                deleteDataIds.push(item.id)
                            }
                        }
                    };
                };

                if (item.type_num == 8) {
                    /* 公司 */
                    if (hadAllCompanyData) {
                        if (!onlyCompanyList.length) {
                            deleteDataIds.push(item.id)
                        } else {
                            let hasD = onlyCompanyList.some((sitem) => sitem.fid == item.fid && (sitem.name.includes(item.name) || item.name.includes(sitem.name)));
                            if (!hasD) {
                                deleteDataIds.push(item.id)
                            }
                        }
                    }
                }
            });
            /* 处理出入口删除-改名后-删除历史记录 */

            /* 删除历史记录 */
            if (deleteDataIds.length) {
                searchHistory = searchHistory.filter((fitem) => {
                    return !deleteDataIds.some((sitem) => {
                        return fitem.id == sitem
                    })
                });

                let ids = deleteDataIds.join(',')
                entDeleHistory2(ids)
                deleteDataIds = [];
            };
            /* 删除历史记录 */
            showSearchPop(true, searchHistory);

            // 多地图页输入内容
            if (moreMapValue) {
                document.getElementById('searchInput').value = moreMapValue;
                searchInput();
            };

        }
    })
    return
    // 使用http方式
    mqttPushMessage({
        todo: "api",
        data: {
            url: "hot/getHotData",
            data: {
                map: mapInfo.mapId
            }
        }
    })
}
function entDeleHistory(ids) {
    $.ajax({
        url: `${formal}hot/delHotData/${ids}?userId=${userId}`,
        success: function (res) {
            getSearchPop();
        },
    })
    return
    // 使用http方式
    mqttPushMessage({
        todo: "api",
        data: {
            url: "hot/delHotData",
            data: ids
        }
    })
};

function entDeleHistory2(ids) {
    $.ajax({
        url: `${formal}hot/delHotData/${ids}?userId=${userId}`,
        success: function (res) {
        },
    })
    return
    // 使用http方式
    mqttPushMessage({
        todo: "delHotData",
        data: {
            url: "hot/delHotData",
            data: { id: ids },
        }
    })
};

async function resSearchByFid(fid) {
    if (!fid) return;
    let params = {
        FID: fid,
        nodeType: fengmap.FMType.MODEL
    };
    let result = await searchByParams(params);
    if (result && result.length > 0) {
        let model = result[0];
        if (model != null) return model;
    } else {
        return null;
    }
}
async function showDestination(info) {
    if (typeof info.x == 'undefined' || info.x == 'undefined') {
        const model = await resSearchByFid(info.fid);
        if (model) {
            info.x = model.center.x;
            info.y = model.center.y;
            info.level = model.level;
            info.name = info.name + '(' + fMap.getFloor(model.level).name + "层)";
        }
    };
    showFooterShowType1(false);
    showFooterShowType2(info, true);
}

async function pointAndNaviFid(fid, isDetail = true, name, pos, destination_type, onlywall = false, type_num) {
    initFixedView(false);

    varonlywall = onlywall; // 控制驾车按钮是否显示
    let coord = null;
    if (fid && fid != "undefined") {
        const model = await resSearchByFid(fid);
        //设置视图
        fMap.setLevel({
            level: model.level,
        });
        fMap.setCenter({
            x: model.center.x,
            y: model.center.y,
        });
        fMap.setZoom({
            zoom: 20
        });
        if (imageMarker) {
            imageMarker.remove();
        }
        /* 构造 Marker */
        imageMarker = new fengmap.FMImageMarker({
            x: model.center.x,
            y: model.center.y,
            url: './image/FMImageMarker.png',
            size: 30,
            height: 2,
            collision: false
        });
        const floor = fMap.getFloor(model.level)
        imageMarker.addTo(floor);
        name = name || model.name || "未命名区域";
        let is_exit = false;
        if (allExit.length) {
            allExit.forEach((item) => {
                if (item.fid == model.FID || name.indexOf(item.name) != -1) {
                    is_exit = true;
                }
            })
        } else {
            if (name.indexOf('地库') != -1 || name.indexOf('出口') != -1 || name.indexOf('出入口') != -1) {
                is_exit = true;
            }
        }

        const typeID = model.typeID;
        let ename = resEname(model.ename, typeID);
        if (ename) {
            ename += "(" + ename + ')';
        }
        coord = {
            fid: model.FID,
            name: name,
            type: is_exit ? 'search_exit' : '',
            ox: model.center.x,
            oy: model.center.y,
            x: model.center.x,
            y: model.center.y,
            level: model.level,
            autoMark: (contains(place_type, typeID)) || false,
        };
        if (type_num) {
            coord['type_num'] = type_num
        };

        let mType = model.type;
        let mTypeID = model.typeID;
        switch (mType) {
            case 8192:
                break;
            case 8:
                break;
            case 4096:
                if (contains(place_type, mTypeID)) {
                    coord['icon'] = 'search-place-icon';
                } else {
                    coord['icon'] = 'search-map-icon';
                }
                break;
            case 64:
                break;
            case 16384:
                break;
            case 1024:
                coord['icon'] = 'search-place-icon';
                break;
        }
    } else {
        const model = await resSearchByFid(fid);
        //设置视图
        fMap.setLevel({
            level: +showP.level,
        });
        fMap.setCenter({
            x: +showP.x,
            y: +showP.y,
        });
        fMap.setZoom({
            zoom: 20
        });
        if (imageMarker) {
            imageMarker.remove();
        }
        /* 构造 Marker */
        imageMarker = new fengmap.FMImageMarker({
            x: +showP.x,
            y: +showP.y,
            url: './image/FMImageMarker.png',
            size: 30,
            height: 2,
            collision: false
        });
        const floor = fMap.getFloor(+showP.level)
        imageMarker.addTo(floor);

        showFooterShowType1(true);
        if (name != 'null') {
            name = showP.name;
            if (name.indexOf('层)') == -1) {
                name = name + "(" + fMap.getFloor(+showP.level).name + "层)";
            };
            showFooterShowType1(false);
            // 收藏
            iscollect_coord = {};
            iscollect_coord = {
                fid: showP.fid,
                name: showP.name,
                ox: +showP.x,
                oy: +showP.y,
                x: +showP.x,
                y: +showP.y,
                level: +showP.level,
            };
            if (type_num) {
                iscollect_coord['type_num'] = type_num
            }

            showFooterShowType2(iscollect_coord, true);
        }
        return false;
    }

    if (coord.name.indexOf('层)') == -1) {
        coord.name = coord.name + "(" + fMap.getFloor(+coord.level).name + "层)";
    }
    if (isDetail) {
        coord.destination_type = destination_type;
        showFooterShowType1(false);
        // 收藏
        iscollect_coord = coord;
        showP = {
            fid: coord.fid,
            name: coord.name,
            x: coord.x,
            y: coord.y,
            level: coord.level,
        }

        showFooterShowType2(coord, true);
    }
    setTimeout(() => {
        clickDropTap = false;
    }, 1000);
}

function dropTap(id, type_num, name, c_icon, c_type_num, c_outdoorType, c_ename) {
    clickDropTap = true;

    clickshowP = {
        c_icon,
        c_type_num,
        c_outdoorType: c_outdoorType,
        c_ename
    }

    let oname = name;
    let delete_item = null;
    let add_item = null;
    delete_item = searchHistory.find(item => item.name == name && item.type_num === type_num);
    add_item = searchResult.find(item => item.id == id && item.type_num == type_num);


    showSearchPop(false);
    let item = null;
    if (delete_item) {
        entDeleHistory2(delete_item.id)
        // mqttPushMessage({
        //     todo: "delHotData",
        //     data: {
        //         url: "hot/delHotData",
        //         data: delete_item,
        //     }
        // })

        // 删除后再添加
        if (!add_item) {
            setTimeout(() => {
                $.ajax({
                    url: `${formal}hot/addHotData?userId=${userId}`,
                    type: 'GET',
                    data: {
                        name: delete_item.name,
                        ename: delete_item.ename || '',
                        desc: delete_item.desc || '',
                        outdoorType: delete_item.outdoorType || '',
                        fid: delete_item.fid || '',
                        icon: delete_item.icon || '',
                        floor: delete_item.groupID || '',
                        x: delete_item.x || delete_item.lng,
                        y: delete_item.y || delete_item.lat,
                        map: mapInfo.mapId,
                        databaseId: delete_item.databaseId,
                        id: typeof delete_item.id != 'number' ? -1 : delete_item.id,
                        type: delete_item.type_num
                    }
                })
                // mqttPushMessage({
                //     todo: "api",
                //     data: {
                //         url: "hot/addHotData",
                //         data: delete_item,
                //         mapid: mapInfo.mapId
                //     }
                // })
            }, 0);
        }
        item = delete_item;
    }
    if (add_item) {
        // if (searchHistory.length >= 12) {
        //     let lastData = searchHistory[searchHistory.length - 1];

        //     mqttPushMessage({
        //         todo: "delHotData",
        //         data: {
        //             url: "hot/delHotData",
        //             data: lastData,
        //         }
        //     })
        // }

        //发送历史记录
        $.ajax({
            url: `${formal}hot/addHotData?userId=${userId}`,
            type: 'GET',
            data: {
                name: add_item.name,
                ename: add_item.ename || '',
                desc: add_item.desc || '',
                outdoorType: add_item.outdoorType || '',
                fid: add_item.fid || '',
                icon: add_item.icon || '',
                floor: add_item.groupID || '',
                x: add_item.x || add_item.lng,
                y: add_item.y || add_item.lat,
                map: mapInfo.mapId,
                databaseId: add_item.databaseId,
                id: typeof add_item.id != 'number' ? -1 : add_item.id,
                type: add_item.type_num
            }
        })
        // mqttPushMessage({
        //     todo: "api",
        //     data: {
        //         url: "hot/addHotData",
        //         data: add_item,
        //         mapid: mapInfo.mapId
        //     }
        // })

        item = add_item;
    }

    if (nowinputtype == 2 || nowinputtype == 4) {
        // 收藏数据 || 热点数据
        let arr = [];
        if (nowinputtype == 2) {
            arr = c_list
        } else {
            arr = hotPList
        };
        item = arr.find((it) => {
            return it.id == id
        });
        const name = item.name || '';
        showP = {
            fid: item.fid,
            name: name,
            x: item.x,
            y: item.y,
            level: item.level || item.floor
        }
        pointAndNaviFid(item.fid, true, name);
        addHotDataApi(item);
    } else {
        // 搜索数据 || 搜索记录
        if (item.type === 'outdoor') {
            isoutdoor = 'outdoor';

            getExitPlatForm(item);
        } else {
            isoutdoor = false;
            //室内地点
            const name = item.sendName ? item.name : (item.name ? item.name : '');
            showP = {
                fid: item.fid,
                name: name,
                x: item.x,
                y: item.y,
                level: item.level || item.floor || item.groupID
            }
            pointAndNaviFid(item.fid, true, name, null, null, false, type_num);
            addHotDataApi(item);
        };

        try {
            // 点击搜索内容-上传后台
            let pushObj = {
                map: mapInfo.mapId, //地图id
                mapName: mapInfo.mapName,//地图名
                // place: '',//车位id
                // placeName: '',//车位名
                // businessId: '',//商家id
                // businessName: '',//商家名
                type: ''//区分类型 1车位2商家
            };
            let originName = name.split("(")[0];
            let originId = nowinputtype == 1 ? (delete_item && delete_item.databaseId ? delete_item.databaseId : '') : id;
            if (!originId || originId == -1) return false;

            if (type_num == 0) {
                // 车位
                pushObj['place'] = originId;
                pushObj['placeName'] = originName;
                pushObj.type = '1';
                clickDataToBackstage(pushObj);
            } else if (type_num == 3) {
                let hadId = onlyBusinessList.some((items) => items.fid == item.fid);

                if (!hadId) return;
                // 商家
                pushObj['businessId'] = originId;
                pushObj['businessName'] = originName;
                pushObj.type = '2';
                clickDataToBackstage(pushObj);
            };
        } catch (error) {

        };
    };
}


// 添加热点数据
function addHotDataApi(item) {
    item.name = item.name.split("(")[0];
    // 增加一条热点数据
    let addHotItemData = {
        // "id": item.id,
        "name": item.name,
        "type": item.type_num || item.type_num == 0 ? item.type_num : (item.type || item.type == 0 ? item.type : ''),
        "x": item.x || item.lng,
        "y": item.y || item.lat,
        "z": "0",
        "map": mapInfo.mapId,
        "icon": item.icon || '',
        "ename": item.ename || '',
        "floor": item.groupID || item.floor || '',
        "fid": item.fid || '',
        "desc": item.desc || '',
        "outdoorType": item.outdoorType || '',
        "databaseId": item.databaseId,
    }

    $.ajax({
        url: `${formal}mapHotspotData/addHotSearch`,
        type: 'POST',
        data: JSON.stringify(addHotItemData),
        headers: {
            "Content-Type": "application/json"
        },
    })
    // mqttPushMessage({
    //     todo: "api",
    //     data: {
    //         url: "mapHotspotData/addHotSearch",
    //         data: {
    //             userHotData: addHotItemData
    //         }
    //     }
    // })
};

function findNearExitClick() {
    if (naviStart) return;
    if (noClick) return;

    const { x = '', y = '' } = currentCoord || {};
    if (!x || !y) {
        noCurrentCoord();
        return
    };


    clickDropTap = true;
    isoutdoor = 'exit';
    /* 改为用户选择 */
    userSelect(true);
    // getExitPlatForm(null, null);
};

/* 用户选择快速出口 */
function userSelect(type) {
    if (type) {
        // 出口离当前位置距离
        allExit.forEach(async (item) => {
            let pos1 = {
                x: +currentCoord.x,
                y: +currentCoord.y,
                level: +currentCoord.level || +currentCoord.groupID
            };
            let pos2 = {
                x: +item.x,
                y: +item.y,
                level: +item.floor || +item.level
            };
            // let cal_dis = fengmap.FMCalculator.distance(pos1, pos2);
            let cal_dis = await totalDistanceRoute(pos1, pos2);
            item['cal_dis'] = cal_dis
        });
        // 出口离当前位置排序
        allExit.sort((a, b) => {
            return a.cal_dis - b.cal_dis
        });

        let html = `
            <div class="dedtiOrPlaceDialog">
                <div class="dialog-mask animation_opac"></div>
                <div class="dialog-container animation_scale">
                    <div class="dialogBody" style="overflow:hidden">
        `;

        if (allExit.length) {
            allExit.forEach((item, index) => {
                html += `
                            <div class="dialogBtn dialogBtn2 setBackgroung${index}" ontouchstart="setBackground(${index})" ontouchend="userSelectItem(${index}, ${item.id})">
                                <div>
                                    <span class="exitName">${item.name}${index == 0 ? '(最近)' : ''}</span>
                                </div>
                                <div class="textName">${item.roadName ? "(" + item.roadName + ")" : ''}</div>
                            </div>
                `
            });
        } else {
            html += `
                            <div class="dialogBtn dialogBtn2">
                                <div>
                                    <span class="exitName">未找到出口</span>
                                </div>
                                
                            </div>
                `
        }

        html += `
                    </div>
                    <div class="dialogClose" onclick="userSelect(false)"><img src="http://112.94.22.123:10087/rtls/wechat/close_white.png" alt=""></div>
                </div>
            </div>
        `

        $("#dedtiOrPlaceDialog").html(html);

        setTimeout(() => {
            $("#dedtiOrPlaceDialog .dialog-container").addClass("animation_show2")
            $("#dedtiOrPlaceDialog .dialog-mask").addClass("animation_show3")
        }, 30);
    } else {
        $("#dedtiOrPlaceDialog .dialog-container").removeClass("animation_show2");
        $("#dedtiOrPlaceDialog .dialog-mask").removeClass("animation_show3");
        setTimeout(() => {
            $("#dedtiOrPlaceDialog").html('');
        }, 200);
    }
};

/* 两点路径长度分析-返回路线总距离 */
async function totalDistanceRoute(s, e, type) {
    let priority = fengmap.FMNaviPriority.PRIORITY_ESCALATORONLY; // 默认驾车
    if (type === 'walk') {
        // 步行状态的路径分析
        priority = fengmap.FMNaviPriority.PRIORITY_LIFTFIRST1;
    };

    let p = await new Promise((resolve, reject) => {
        var fa = new fengmap.FMNaviAnalyser({ map: fMap }, (res) => {
            fa.route({
                start: {
                    x: +s.x,
                    y: +s.y,
                    level: +s.level
                },
                dest: {
                    x: +e.x,
                    y: +e.y,
                    level: +e.level
                },
                mode: fengmap.FMNaviMode.MODULE_BEST,
                priority
            }, (route) => {
                if (route.distance || route.distance == 0) {
                    resolve(route.distance)
                } else {
                    resolve(999999999)
                }
            }, () => {
                // route 失败
                resolve(999999999)
            })
        }, () => {
            // 分析失败
            resolve(999999999)
        });
    });

    if (p) {
        return p;
    }
};

/* 用户选择出口 */
function userSelectItem(index, id) {
    let data = allExit.filter((item) => item.id == id);
    let item = data[0];

    pointAndNaviFid(item.fid, true, item.name);
    showFooterShowType1(false);
    userSelect(false);
};

async function getExitPlatForm(info, wxMap) {
    const { x = '', y = '' } = currentCoord || {};
    if (!x || !y) {
        noCurrentCoord();
        return
    };
    let list_floor = fMap.getFloor(+currentCoord.groupID).name + "层";
    let mindis = 1000000000;
    let near_exit = null;
    let hasSameFloor = false;
    for (let i = 0; i < allExit.length; i++) {
        allExit[i].level = +allExit[i].floor;
        let proResult = await new Promise((resolve, reject) => {
            searchNearModel(currentCoord, (startCoord) => {
                naviAnalyser.route({
                    start: {
                        x: startCoord.x,
                        y: startCoord.y,
                        level: startCoord.level
                    },
                    dest: {
                        x: +allExit[i].x,
                        y: +allExit[i].y,
                        level: allExit[i].level
                    },
                    mode: fengmap.FMNaviMode.MODULE_BEST,
                    priority: fengmap.FMNaviPriority.PRIORITY_DEFAULT
                }, (result) => {
                    resolve({
                        res: "success",
                        data: {
                            item: allExit[i],
                            result
                        }
                    });
                }, (fail) => {
                    resolve({
                        res: "fail"
                    });
                });
            });
        });
        if (proResult.res == "success") {
            let { item, result } = proResult.data;
            let dis = result.distance;
            if (currentCoord.groupID != +item.floor) {
                dis = dis + 100000000;
            } else {
                hasSameFloor = true;
            }
            if (mindis > dis) {
                mindis = dis;
                near_exit = item;
            }
        }

    }
    if (info) {
        outdoorInfo = {
            lat: info.lat,
            lng: info.lng,
            name: info.name,
            addressTxt: info.addressTxt
        };
    }
    // showFooterShowType2({
    //     x: +near_exit.x,
    //     y: +near_exit.y,
    //     level: +near_exit.floor,
    //     type: info ? 'outdoor' : 'exit',
    //     name: info ? near_exit.name + '(室外地点:' + info.name + ')' : near_exit.name,
    //     fid: near_exit.fid
    // }, true);
    if (near_exit && near_exit.fid) {
        pointAndNaviFid(near_exit.fid, true, (info ? near_exit.name + '(室外地点:' + info.name + ')' : near_exit.name));
        showFooterShowType1(false);
    } else {
        toolTipBox('出口规划失败，请重试')
    }
}

var Iflag = true;
function showSearchPop(flag, arr) {
    if (flag) {
        let html = `<div class="popSearchDialog">
            <div class="pop-mask ${isOpenSearchPop ? 'animation_opac' : ''}" onclick="showSearchPop(false)"></div>
            <div class="page-container ${isOpenSearchPop ? 'animation_hide' : ''}">
                <div class="pop-header">
                    <div class="pop-search">
                        <img class="search-icon" src="http://112.94.22.123:10087/rtls/wechat/searchIcon.png" alt="">
                        <input class="input_font" type="text" placeholder="搜索目的地"   onfocus="enterInput()" id="searchInput" />
                        <span class="clear" onclick="clearInput()"></span>
                    </div>

                    <div class="newNav">
                        <div class="navitem" id="collect" onclick="switchNav('collect')">
                            <div class="icon">
                                <img class="img" src="http://112.94.22.123:10087/rtls/wechat/230904_collect.png" alt="">
                            </div>
                            <div class="text">收藏夹</div>
                        </div>
                        <div class="line"></div>
                        <div class="navitem" id="hot" onclick="switchNav('hot')">
                            <div class="icon">
                                <img class="img" src="http://112.94.22.123:10087/rtls/wechat/230904_hot.png" alt="">
                            </div>
                            <div class="text">热门</div>
                        </div>
                        <div class="line"></div>
                        <div class="navitem" id="periphery" onclick="switchNav('periphery')">
                            <div class="icon">
                                <img class="img" src="http://112.94.22.123:10087/rtls/wechat/230904_periphery.png" alt="">
                            </div>
                            <div class="text">周边</div>
                        </div>
                    </div>
        `;
        if (clearHide) {
            // 搜索记录
            nowinputtype = 1;
            html += `<div id="searchPopType">
                        <div class="type-list">
                            <span>搜索记录</span>
                            <span class="delete" onclick="manage('1', false)">管理</span>
                        </div>
                    </div>
                </div>
                <div class="page-scroll" id="searchPopScroll"> 
            `;
        } else {
            // 搜索数据
            nowinputtype = 3;
            html += `<div id="searchPopType">
                        <div class="type-list">
                            <span class="cur-type">全部</span>
                            <span>车位号</span>
                            <span>室内地点</span>
                            <span>室外地点</span>
                        </div>
            </div></div><div class="page-scroll" id="searchPopScroll">`;
        }
        console.log('abcd', arr);
        for (var i = 0; i < arr.length; i++) {
            html += `<div class="search-item clickItem">`;

            if (!arr[i].icon || arr[i].icon == 'null' || arr[i].icon == 'undefined') {
                html += `<div style="width: 3.3333vw"></div>`
            } else {
                html += `<img src="${imagePath + arr[i].icon}.png" alt="">`
            };
            html += ` <div class="search-info" onclick="dropTap('${arr[i].id}', '${(arr[i].typeNum || arr[i].type_num)}', '${arr[i].name}','${arr[i].icon}','${arr[i].type_num}','${arr[i].outdoorType}','${arr[i].ename}')">
            <div class="search-title">${(arr[i].type_num == '3' || arr[i].type_num == '8') ? arr[i].name + "(" + fMap.getFloor(+arr[i].groupID).name + "层)" : arr[i].name}</div>
            <div class="search-desc">`

            if (arr[i].outdoorType) {
                html += `<span class="outdoor-type">${arr[i].outdoorType}</span>`;
            }
            html += `<span class="outdoor-type ${arr[i].type != 'outdoor' ? 'setOTColor' : ''}">${arr[i].desc}</span>
                    </div>
                </div>`
            if (clearHide) {
                html += `<div class="search-delete" onclick="deleteItem(${arr[i].id})">
                        <img src="http://112.94.22.123:10087/rtls/wechat/delete.png" alt="">
                    </div>
                `;
            }
            html += `</div>`;
        };

        if (!arr.length) {
            html += `
                <div class="isNoDataList">暂无数据</div>
            `;
        }

        html += `</div><div id="see_more_box"></div></div></div>`;
        $("#searchPop").html(html);
        setTimeout(() => {
            isOpenSearchPop = false;
            $(".page-container").addClass('animation_show');
            $(".pop-mask").addClass('animation_show3');
        }, 30);
        if (voiceValue) {
            document.getElementById('searchInput').value = voiceValue
            searchInput();
        };

        /* 输入框 */
        $('#searchInput').on('compositionstart', function () {
            Iflag = false;
        })
        $('#searchInput').on('compositionend', function () {
            Iflag = true;
        })
        $('#searchInput').on('input', function () {
            setTimeout(function () {
                if (Iflag) {
                    searchInput();
                }
            }, 16)
        })
    } else {
        isOpenSearchPop = true;
        clearHide = true;
        $(".page-container").addClass('animation_hide');
        $(".pop-mask").addClass('animation_opac');
        setTimeout(() => {
            $(".page-container").removeClass('animation_show');
            $(".pop-mask").removeClass('animation_show3');
        }, 30);

        setTimeout(() => {
            $("#searchPop").html('');
        }, 300);
        voiceValue = null;
    }
}

// 显示收藏夹内容
function showCollect(arr2, type = false) {
    let arr = [];
    if (arr2.length) {
        arr = processingFavoriteData(arr2);
    }

    let old_nowinputtype = JSON.parse(JSON.stringify(nowinputtype))
    // 收藏数据
    nowinputtype = 2;
    $("#searchPopType").html(`<div class="type-list"><span>收藏夹</span>
    <span class="delete" onclick="manage('2',${type})">${type ? '取消' : '管理'}</span></div>`);
    if (arr.length) {
        let html = '';
        let html2 = '';
        for (var i = 0; i < arr.length; i++) {
            html += `<div class="search-item clickItem">`
            if (type) {
                html += `<input class="cinput" type="checkbox"  onclick="check(${arr[i].id})" ${arr[i].check ? 'checked' : ''}/>`
            };

            if (!arr[i].icon || arr[i].icon == 'null' || arr[i].icon == 'undefined') {
                html += `<div style="width: 3.3333vw"></div>`
            } else {
                html += `<img src="${imagePath + arr[i].icon}.png" alt="">`
            };

            html += `
                    <div class="search-info" onclick="dropTap('${arr[i].id}', '${(arr[i].typeNum || arr[i].type_num)}', '${arr[i].name}','${arr[i].icon}','${arr[i].type_num}','${arr[i].outdoorType}','${arr[i].ename}')">
                        <div class="search-title">${arr[i].name}</div>
                    </div>
               `;
            if (clearHide) {
                html += `<div class="search-delete" onclick="deleteItem(${arr[i].id})">
                           <img src="http://112.94.22.123:10087/rtls/wechat/delete.png" alt="">
                       </div>
                   `;
            }
            html += `</div>`;
        };

        if (type) {
            html2 = `
                <div class="m_footer">
                    <div class="search-item clickItem">
                        <input type="checkbox" onclick="checkAll()" id="allcheckbox" ${allcheckbox ? 'checked' : ''}/>
                        <text class="m_text">全选</text>
                    </div>
                    <div class="m_dele" onclick="deleDate()">删除</div>
                </div>
            `
        };
        if (old_nowinputtype == 4 || old_nowinputtype == 5) {
            $("#searchPopScroll").addClass('animation_x1');
            $("#searchPopScroll").html(html);
            $("#see_more_box").html(html2);

            setTimeout(() => {
                $("#searchPopScroll").addClass('animation_xx');
                $("#searchPopScroll").addClass('animation_x2');
            }, 30);
        } else {
            $("#searchPopScroll").addClass('animation_x3');
            $("#searchPopScroll").html(html);
            $("#see_more_box").html(html2);
            setTimeout(() => {
                $("#searchPopScroll").addClass('animation_xx');
                $("#searchPopScroll").addClass('animation_x2');
            }, 30);
        }
    } else {
        $("#searchPopScroll").html('<div class="isNoDataList">暂无数据</div>');
    }
}

// 处理收藏内容过期
function processingFavoriteData(data) {
    let deleCollectIds = [];
    data.forEach((item, index) => {
        if (item.fid == null) {
            item.fid = ''
        }
        if (item.typeNum === '0') {
            if (hadAllPlaces) {
                if (!all_places.length) {
                    deleCollectIds.push(item.id)
                } else {
                    let hasD = all_places.some((sitem) => sitem.fid == item.fid && (sitem.name.includes(item.name) || item.name.includes(sitem.name)));
                    if (!hasD) {
                        deleCollectIds.push(item.id)
                    }
                }
            }
        } else if (item.typeNum === '1') {
            if (hadAllExitData) {
                if (!showIconExitData.length) {
                    deleCollectIds.push(item.id)
                } else {
                    let hasD = showIconExitData.some((sitem) => sitem.fid == item.fid && (sitem.name.includes(item.name) || item.name.includes(sitem.name)));
                    if (!hasD) {
                        deleCollectIds.push(item.id)
                    }
                }
            }
        } else if (item.typeNum === '3') {
            if (hadAllBusinessData) {
                if (!onlyBusinessList.length) {
                    deleCollectIds.push(item.id)
                } else {
                    let hasD = onlyBusinessList.some((sitem) => sitem.fid == item.fid && (sitem.name.includes(item.name) || item.name.includes(sitem.name)));
                    if (!hasD) {
                        deleCollectIds.push(item.id)
                    }
                }
            }
        } else if (item.typeNum === '6') {
            if (hadAllElevatorData) {
                if (!elevatorData.length) {
                    deleCollectIds.push(item.id)
                } else {
                    let hasD = elevatorData.some((sitem) => sitem.fid == item.fid && (sitem.name.includes(item.name) || item.name.includes(sitem.name)));
                    if (!hasD) {
                        deleCollectIds.push(item.id)
                    }
                }
            }
        } else if (item.typeNum === '7') {
            if (hadAllServiceData) {
                if (!serviceFacilitiesData.length) {
                    deleCollectIds.push(item.id)
                } else {
                    let hasD = serviceFacilitiesData.some((sitem) => sitem.fid == item.fid && (sitem.name.includes(item.name) || item.name.includes(sitem.name)));
                    if (!hasD) {
                        deleCollectIds.push(item.id)
                    }
                }
            }
        } else if (item.typeNum === '8') {
            if (hadAllCompanyData) {
                if (!onlyCompanyList.length) {
                    deleCollectIds.push(item.id)
                } else {
                    let hasD = onlyCompanyList.some((sitem) => sitem.fid == item.fid && (sitem.name.includes(item.name) || item.name.includes(sitem.name)));
                    if (!hasD) {
                        deleCollectIds.push(item.id)
                    }
                }
            }
        }
    });

    if (deleCollectIds.length) {
        data = data.filter((fitem) => {
            return !deleCollectIds.some((sitem) => {
                return fitem.id == sitem
            })
        });
        let ids = deleCollectIds.join(',')
        deleteCollect2(ids);
    }
    return data;
};

function deleteCollect2(ids) {
    // socket.send(JSON.stringify({
    //     todo: "deleteCollect2",
    //     data: ids
    // }));

    mqttPushMessage({
        todo: "deleteCollect2",
        data: ids
    })
};

// 热门ui
function showHotView(arr) {
    let old_nowinputtype = JSON.parse(JSON.stringify(nowinputtype))

    // 热门数据
    nowinputtype = 4;
    $("#searchPopType").html('');

    if (arr.length) {
        let html = '';
        for (var i = 0; i < arr.length; i++) {
            html += `<div class="search-item clickItem">`

            if (!arr[i].icon || arr[i].icon == 'null' || arr[i].icon == 'undefined') {
                html += `<div style="width: 3.3333vw"></div>`
            } else {
                html += `<img src="${imagePath + arr[i].icon}.png" alt="">`
            };

            html += `
                    <div class="search-info" onclick="dropTap('${arr[i].id}', '${(arr[i].type || arr[i].type_num)}', '${arr[i].name}','${arr[i].icon}','${arr[i].type_num}','${arr[i].outdoorType}','${arr[i].ename}')">
                        <div class="search-title">${arr[i].name}</div>
                    </div>
               `;

            html += `</div>`;
        };
        if (old_nowinputtype == 2) {
            $("#searchPopScroll").addClass('animation_x3');
            $("#searchPopScroll").html(html);
            setTimeout(() => {
                $("#searchPopScroll").addClass('animation_xx');
                $("#searchPopScroll").addClass('animation_x2');
            }, 30);
        } else {
            $("#searchPopScroll").addClass('animation_x1');
            $("#searchPopScroll").html(html);
            setTimeout(() => {
                $("#searchPopScroll").addClass('animation_xx');
                $("#searchPopScroll").addClass('animation_x2');
            }, 30);
        }
    } else {
        $("#searchPopScroll").html('<div class="isNoDataList">暂无数据</div>');
    }
}

// 管理ui
// 1：搜索记录-list:searchHistory，2：收藏-list:c_list
function manage(type, show) {
    managetype = type;
    if (type == 1) {
        if (!searchHistory.length) return;
        updateSearchPop(searchHistory, !show, false);
    } else {
        if (!c_list.length) return;
        showCollect(c_list, !show, false);
    }
}

// 删除某条记录
// nowinputtype
// 1：搜索记录，2：收藏夹
function deleteItem(id) {
    switch (nowinputtype) {
        case 1:
            entDeleHistory(id)
            break;
        case 2:
            deleteCollect(id)
            break;

        default:
            break;
    }
}


function switchNav(e) {
    $("#searchPopScroll").removeClass('animation_xx');
    $("#searchPopScroll").removeClass('animation_x1');
    $("#searchPopScroll").removeClass('animation_x2');
    $("#searchPopScroll").removeClass('animation_x3');
    $("#searchPopScroll").html('');
    $("#see_more_box").html('');
    $("#searchPopType").html('');

    let target = e;
    let navitem = document.getElementsByClassName('navitem');
    for (let i = 0; i < navitem.length; i++) {
        navitem[i].classList.remove('active')
    };
    switch (target) {
        case "collect":
            document.getElementById('collect').classList.add('active');
            // if (c_list && c_list.length) {
            //     showCollect(c_list);
            // } else {
            //     mqttPushMessage({
            //         todo: "api",
            //         data: {
            //             url: "wechat/getStorePlace",
            //             data: {
            //                 map: mapInfo.mapId
            //             }
            //         }
            //     })
            // }
            $.ajax({
                url: `${formal}wechat/getStorePlace?map=${mapInfo.mapId}&userId=${userId}`,
                success: function (res) {
                    c_list = res.data.sort((a, b) => {
                        return b.id - a.id
                    });
                    showCollect(res.data);
                }
            })
            break;
        case "hot":
            document.getElementById('hot').classList.add('active');

            if (hotPList && hotPList.length) {
                showHotView(hotPList)
            } else {
                // mqttPushMessage({
                //     todo: "api",
                //     data: {
                //         url: "mapHotspotData/getHotSearchByMap",
                //         data: {
                //             map: mapInfo.mapId
                //         }
                //     }
                // })
                $.ajax({
                    url: `${formal}mapHotspotData/getHotSearchByMap?map=${mapInfo.mapId}`,
                    success: function (res) {
                        let resdata = [];
                        if (res.data.length > 20) {
                            for (let i = 0; i < 20; i++) {
                                resdata.push(res.data[i])
                            }
                        } else {
                            resdata = res.data
                        };

                        // 热门
                        hotPList = resdata;
                        showHotView(resdata)
                    }
                })
            }
            break;
        case "periphery":
            document.getElementById('periphery').classList.add('active');
            $("#searchPopScroll").html('<div class="isNoDataList">暂无数据</div>');
            $("#searchPopType").html('');
            nowinputtype = 5;
            break;
    }
};

// 搜索历史切换显示
var switch_h_index = 0;
function switchHistory(e) {
    if (e.target.nodeName != "SPAN") {
        return false;
    }
    let type = e.target.dataset.atype;
    let index = e.target.dataset.ai;
    let data = JSON.parse(JSON.stringify(searchResult));
    let resdata;
    $(".cur-type").removeClass('cur-type')
    $(e.target).addClass('cur-type')
    if (type) {
        resdata = data.filter((item) => {
            return item.type == type
        })
    } else {
        resdata = data;
    };
    $("#searchPopScroll").removeClass('animation_xx');
    $("#searchPopScroll").removeClass('animation_x1');
    $("#searchPopScroll").removeClass('animation_x2');
    $("#searchPopScroll").removeClass('animation_x3');
    upSearchPopScroll(resdata, index);
};

// 更新历史数据列表
function upSearchPopScroll(arr, index) {
    let old_switch_h_index = JSON.parse(JSON.stringify(switch_h_index));
    switch_h_index = index;
    let html = '';
    for (var i = 0; i < arr.length; i++) {
        html += `<div class="search-item clickItem">`;

        if (!arr[i].icon || arr[i].icon == 'null' || arr[i].icon == 'undefined') {
            html += `<div style="width: 3.3333vw"></div>`
        } else {
            html += `<img src="${imagePath + arr[i].icon}.png" alt="">`
        };

        html += `<div class="search-info" onclick="dropTap('${arr[i].id}', '${(arr[i].typeNum || arr[i].type_num)}', '${arr[i].name}','${arr[i].icon}','${arr[i].type_num}','${arr[i].outdoorType}','${arr[i].ename}')">
        <div class="search-title">`

        if (arr[i].nameList && arr[i].nameList.length) {
            for (let j = 0; j < arr[i].nameList.length; j++) {
                html += `<text class="${arr[i].nameList[j].type === 'keyword' ? 'strong' : ''}">${arr[i].nameList[j].text}</text>`
            }
        } else {
            html += `<text class="">${arr[i].name}</text>`
        }

        html += `</div>
        <div class="search-desc">`

        if (arr[i].outdoorType) {
            html += `<span class="outdoor-type">${arr[i].outdoorType}</span>`;
        }
        html += `<span class="outdoor-type ${arr[i].type != 'outdoor' ? 'setOTColor' : ''}">${arr[i].desc}</span>
                 <span class="outdoor-type">${arr[i].dis || arr[i].dis == 0 ? '距离：' + parseInt(arr[i].dis) + '米' : ''}</span>
                </div>
            </div></div>`

    };
    if (is_show_more) {
        $("#see_more_box").html(`<div class="smore" onclick="see_more()">查看更多</div>`)
    } else {
        $("#see_more_box").html('')
    }
    if (switch_h_index > old_switch_h_index) {
        $("#searchPopScroll").addClass('animation_x3');
        $("#searchPopScroll").html(html);
        setTimeout(() => {
            $("#searchPopScroll").addClass('animation_xx');
            $("#searchPopScroll").addClass('animation_x2');
        }, 30);
    } else {
        $("#searchPopScroll").addClass('animation_x1');
        $("#searchPopScroll").html(html);
        setTimeout(() => {
            $("#searchPopScroll").addClass('animation_xx');
            $("#searchPopScroll").addClass('animation_x2');
        }, 30);
    }
};
// 周边搜索
function peripherySearch(data, callback) {
    var analyser = new fengmap.FMSearchAnalyser({ map: fMap }, function () {
        var searchRequest = new fengmap.FMSearchRequest();
        searchRequest.levels = [data.groupID]
        searchRequest.type = fengmap.FMType.MODEL;
        searchRequest.addCondition({
            'circle': {
                'center': {
                    'x': data.x,
                    'y': data.y
                },
                'radius': 15
            }
        });

        analyser.query(searchRequest, (result) => {
            if (callback) {
                result.forEach(item => {
                    let { x, y } = item.center;
                    let pointDis = Math.sqrt(Math.pow((x - data.x), 2) + Math.pow((y - data.y), 2))
                    item.pointDis = pointDis;
                });
                result.sort((a, b) => {
                    return a.pointDis - b.pointDis
                });
                callback(result)
            }
        });
    })
};

function updateSearchPop(arr, type = false, showS = true) {
    if (!clearHide) {
        // 搜索数据
        nowinputtype = 3;
        $("#searchPopType").html(`
        <div class="type-list" onclick="switchHistory(event)">
            <span class="cur-type" data-atype="" data-ai="0">全部</span>
            <span data-atype="place" data-ai="1">车位号</span>
            <span data-atype="fmap" data-ai="2">室内地点</span>
            <span data-atype="outdoor" data-ai="3">室外地点</span>
        </div>
        `);
    } else {
        // 搜索记录
        nowinputtype = 1;
        $("#searchPopType").html(`<div class="type-list"><span>搜索记录</span>
            <span class="delete" onclick="manage('1',${type})">${type ? '取消' : '管理'}</span></div>`);
    }

    let html = '';
    let html2 = '';
    console.log('aaabbb', arr);


    for (var i = 0; i < arr.length; i++) {
        html += ` <div class="search-item clickItem">`
        if (type) {
            html += `<input class="cinput" type="checkbox"  onclick="check('${arr[i].id}')" ${arr[i].check ? 'checked' : ''}/>`
        };

        if (!arr[i].icon || arr[i].icon == 'null' || arr[i].icon == 'undefined') {
            html += `<div style="width: 3.3333vw"></div>`
        } else {
            html += `<img src="${imagePath + arr[i].icon}.png" alt="">`
        };

        html += `
             <div class="search-info" onclick="dropTap('${arr[i].id}', '${(arr[i].typeNum || arr[i].type_num)}', '${arr[i].name}','${arr[i].icon}','${arr[i].type_num}','${arr[i].outdoorType}','${arr[i].ename}')">
                 <div class="search-title">`;
        if (arr[i].nameList && arr[i].nameList.length) {
            for (let j = 0; j < arr[i].nameList.length; j++) {
                html += `<text class="${arr[i].nameList[j].type === 'keyword' ? 'strong' : ''}">${arr[i].nameList[j].text}</text>`
            };
            html += `<text>${(arr[i].type_num == '3' || arr[i].type_num == '8') ? "(" + fMap.getFloor(+arr[i].groupID).name + "层)" : ''}</text>`
        } else {
            html += `<text class="">${(arr[i].type_num == '3' || arr[i].type_num == '8') ? arr[i].name + "(" + fMap.getFloor(+arr[i].groupID).name + "层)" : arr[i].name}</text>`
        }

        html += `</div><div class="search-desc">
        `;
        if (arr[i].outdoorType) {
            html += `<span class="outdoor-type">${arr[i].outdoorType}</span>`;
        }
        html += `<span class="outdoor-type ${arr[i].type != 'outdoor' ? 'setOTColor' : ''}">${arr[i].desc}</span>
                 <span class="outdoor-type">${arr[i].dis || arr[i].dis == 0 ? '距离：' + parseInt(arr[i].dis) + '米' : ''}</span>
                </div>
            </div>`
        if (clearHide) {
            html += `<div class="search-delete">
                    <img src="http://112.94.22.123:10087/rtls/wechat/delete.png" alt="">
                </div>
            `;
        }
        html += `</div>`;
    }
    if (type) {
        html2 = `
        <div class="m_footer">
            <div class="search-item">
                <input type="checkbox" onclick="checkAll()" id="allcheckbox" ${allcheckbox ? 'checked' : ''}/>
                <text class="m_text">全选</text>
            </div>
            <div class="m_dele" onclick="deleDate()">删除</div>
        </div>
    `
    }

    $("#searchPopScroll").html(html);
    $("#see_more_box").html(html2);
    if (showS && !type) {
        $("#see_more_box").html(`<div class="smore" onclick="see_more()">查看更多</div>`)
    }
}
// 多选
function check(id) {
    //获取全选框的状态
    var checkAllEle = document.getElementById("allcheckbox");
    let dataArr = [];
    if (managetype == 1) {
        dataArr = searchHistory
    } else if (managetype == 2) {
        dataArr = c_list
    }
    dataArr.forEach((item) => {
        if (item.id == id) {
            item.check = !item.check
        }
    })
    let sum = 0;
    dataArr.forEach((item) => {
        if (item.check) {
            sum++
        }
    });
    if (sum == dataArr.length) {
        checkAllEle.checked = true;
    } else {
        checkAllEle.checked = false;
    }
};
// 全选
function checkAll() {
    let dataArr = [];
    var checkAllEle = document.getElementById("allcheckbox");

    if (managetype == 1) {
        dataArr = searchHistory
    } else if (managetype == 2) {
        dataArr = c_list
    }

    if (checkAllEle.checked == true) {
        allcheckbox = true
        dataArr.forEach((item) => {
            item.check = true
        })
    } else {
        allcheckbox = false
        dataArr.forEach((item) => {
            item.check = false
        })
    }
    // 更新ui
    if (managetype == 1) {
        if (!searchHistory.length) return;
        updateSearchPop(searchHistory, true, false);
    } else {
        if (!c_list.length) return;
        showCollect(c_list, true, false);
    }
};

// 管理删除
function deleDate() {
    var dataArr = [];
    if (managetype == 1) {
        dataArr = searchHistory;
    } else if (managetype == 2) {
        dataArr = c_list
    }
    let ids = [];
    dataArr.forEach((item) => {
        if (item.check) {
            ids.push(item.id)
        }
    });
    // if (!ids.length) {
    //     return false;
    // }
    let ids_string = ids.join(',');
    if (managetype == 1) {
        allcheckbox = false;
        entDeleHistory(ids_string)
    } else if (managetype == 2) {
        deleteCollect(ids_string)
    }
};

// 删除收藏夹数据
function deleteCollect(ids) {
    if (!ids) return;
    showModal("确定删除所选地点", () => {
        $('#dialog').html('');
        delcccc(ids);
    }, () => {
        $('#dialog').html('');
    })

    // mqttPushMessage({
    //     todo: "api",
    //     data: {
    //         url: "wechat/delStorePlace",
    //         data: ids
    //     }
    // })
};

function delcccc(ids) {
    $.ajax({
        url: `${formal}wechat/delStorePlace/${ids}`,
        success: function (res) {
            allcheckbox = false;
            getccc();
        },
    })
};

function getccc() {
    $.ajax({
        url: `${formal}wechat/getStorePlace?map=${mapInfo.mapId}&userId=${userId}`,
        success: function (res) {
            c_list = res.data;
            showCollect(res.data);
        }
    })
};

// 查看更多搜索数据
function see_more() {
    var data = [];
    fMapResult.unshift(...newresultTemp);
    fMapResult.forEach(item => {
        const typeID = item.typeID;
        const ename = resEname(item.ename, typeID);
        let icon = '';
        let type = '';
        // let type_num = '';
        let is_place = contains(place_type, typeID);
        if (is_place) {
            icon = 'search-place-icon';
            type = 'place';
            // type_num = '0';
        } else {
            icon = 'search-map-icon';
            type = 'fmap';
            // type_num = '1';
        }
        data.push({
            type,
            id: item.id, //for-key
            name: item.name,
            ename,
            nameList: resNameList(item.name, keyword_name),
            typeID,
            fid: item.fid,
            desc: '类型：室内地点',
            icon,
            x: item.x,
            y: item.y,
            groupID: item.groupID || item.level,
            type_num: item.type_num,
            databaseId: item.id,
            dis: item.cal_dis || item.dis
        })
    });
    data = data.concat(outdoorList)
    searchResult = data;
    is_show_more = false;
    updateSearchPop(data, false, false);
};

var fstartY = 0;
var fmoveY = 0;
var nowTY = 0;
var fflag = false;
var fisMove = false;
function openFooterS(e) {
    fstartY = e.targetTouches[0].pageY;
    let translates = $('#footerMain').css('transform');
    let str = translates.replace(")", "");
    let split = str.split(',');
    nowTY = split[split.length - 1];
};

function openFooterM(e) {
    let windowW = window.innerWidth;
    fmoveY = e.targetTouches[0].pageY - fstartY;

    fisMove = true;

    var translateX = +nowTY + fmoveY;
    if (translateX < 0) {
        translateX = 0
    };
    if (translateX > ((windowW / 100) * 25.6)) {
        translateX = Math.round((windowW / 100) * 25.6)
    }
    let needDom = document.getElementById("footerMain");

    needDom.style.transition = 'none';
    needDom.style.transform = 'translateY(' + translateX + 'px)';
    fflag = true;
    e.preventDefault();
};
function openFooterE(e) {
    let needDom = document.getElementById("footerMain");

    if (!fisMove) return false;
    if (fflag) {
        if (Math.abs(fmoveY) > 20) {
            touchDOM();
        } else {
            needDom.style.transition = 'none';
            needDom.style.transform = 'translateY(' + nowTY + 'px)';
        }
    };
    fisMove = false
};

function touchDOM() {
    let needDom = document.getElementById("footerMain");

    if (fmoveY > 0) {
        needDom.style.transition = 'none';
        needDom.style.transform = 'translateY(25.6vw)';
        $(".footer_box_hTip_icon").removeClass('fbhi_actions');
    } else {
        needDom.style.transition = 'none';
        needDom.style.transform = 'translateY(0)';
        $(".footer_box_hTip_icon").addClass('fbhi_actions');
    };
};

function showFooterShowType1(flag) {
    if (flag) {
        let html = `
        <div class="footer-box" ontouchstart="openFooterS(event)" ontouchmove="openFooterM(event)" ontouchend="openFooterE(event)">
            <div class="footer_box_hTip" onclick="openFooter()">
                <div class="ofTip">向上滑动更多内容</div>
                <!--   // <img src="http://112.94.22.123:10087/rtls/wechat/down_up_icon.png" class="footer_box_hTip_icon" />-->
            </div>
            <div class="footer-search" onclick="getSearchPop()">
                <img src="http://112.94.22.123:10087/rtls/wechat/searchIcon.png" class="search-icon" />
                <span class="search-text moreMapValue">${moreMapValue ? moreMapValue : '搜索目的地'}</span>
                <!--    // <img src="http://112.94.22.123:10087/rtls/wechat/voiceRec.png" class="footer-voice" onclick='getVoiceRec(this,true)' /> -->
            </div>
            <div class="nav">
                <div class="nav_ul">
                    <div class="clickItem" onclick="debounceFn(nearby2, true)">
                        <img src="http://112.94.22.123:10087/rtls/wechat/parking-icon.png" alt="">
                        <span>就近停车</span>
                    </div>
                    <div class="clickItem" onclick="debounceFn(findCar, '')">
                        <img src="http://112.94.22.123:10087/rtls/wechat/find-icon.png" alt="">
                        <span>反向寻车</span>
                    </div>
        `
        if (!isShowLicenceFindCar) {
            html += `
                <div class="clickItem" onclick="licenseCar()">
                    <img src="http://112.94.22.123:10087/rtls/wechat/licenseCar.png" alt="">
                    <span>车牌找车</span>
                </div>
            `
        }
        html += `
                    <div class="clickItem" onclick="business()">
                        <img src="http://112.94.22.123:10087/rtls/wechat/business-icon.png" alt="">
                        <span>商家导航</span>
                    </div>
                    <div class="clickItem" onclick="goToMy()">
                        <img src="http://112.94.22.123:10087/rtls/wechat/my-icon.png" alt="">
                        <span>我的</span>
                    </div>
                </div>
                <div class="nav_ul" style="margin-top: 5.3333vw">
                    <div class="clickItem" onclick="goToReservation()">
                        <img src="http://112.94.22.123:10087/rtls/wechat/hreservaicon.png" alt="">
                        <span>车位预约</span>
                    </div>
                    <div class="clickItem"></div>
                    <div class="clickItem"></div>
                    <div class="clickItem"></div>`
        if (!isShowLicenceFindCar) {
            html += `
                    <div class="clickItem"></div>
                `
        }
        html += `
                </div>
            </div>
        </div>
        `
        $("#footerShowType1").html(html);
        setTimeout(() => {
            $("#footerShowType1").addClass('animation_show')
        }, 30);
    } else {
        $("#footerShowType1").removeClass('animation_show')
        $("#footerShowType1").html('');
    }
};

function openFooter() {
    let translates = $('#footerMain').css('transform');
    let str = translates.replace(")", "");
    let split = str.split(',');
    let nowTY = split[split.length - 1];
    let needDom = document.getElementById("footerMain");

    if (nowTY > 0) {
        needDom.style.transition = 'none';
        needDom.style.transform = 'translateY(0)';
    } else {
        needDom.style.transition = 'none';
        needDom.style.transform = 'translateY(25.6vw)';
    };
    setOpenFooterTipIcon();
};

function setOpenFooterTipIcon() {
    let had = $(".footer_box_hTip_icon").hasClass('fbhi_actions');
    if (had) {
        $(".footer_box_hTip_icon").removeClass('fbhi_actions');
    } else {
        $(".footer_box_hTip_icon").addClass('fbhi_actions');
    }
};

var noShowDriveList = false;
function showFooterShowType2(info, flag, cancel = 'null') {
    if (flag) {
        if (naviStart) {
            return false
        }
        // 是否显示驾车模式选择
        if (allExit.length) {
            allExit.forEach((item) => {
                if (item.fid == info.fid || info.name.indexOf(item.name) != -1) {
                    noShowDriveList = true;
                    info.type = 'exit2'
                }
            })
        } else {
            if (info.name.indexOf('地库') != -1 || info.name.indexOf('出口') != -1 || info.name.indexOf('出入口') != -1) {
                noShowDriveList = true;
                info.type = 'exit2'
            }
        }

        initFixedView(false);//弹框时退出跟随模式
        if (freeTimer) { clearTimeout(freeTimer) };
        if (info) {

            if (info.name.includes('未命名区域')) {
                if (info.fid) {
                    /* 车位 */
                    let isPlace = all_places.find((item) => item.fid == info.fid);
                    if (isPlace && isPlace.name) {
                        info.name = isPlace.name + "(" + fMap.getFloor(+info.level).name + "层)";
                        info.type_num = 0;
                    };
                    /* 出入口 */
                    let isExit = showIconExitData.find((item) => item.fid == info.fid);
                    if (isExit && isExit.name) {
                        info.name = isExit.name + "(" + fMap.getFloor(+isExit.floor).name + "层)";
                        info.type_num = 1;
                    };
                    /* 跨层通道 */
                    let isExit2 = crossLayerData.find((item) => item.fid == info.fid);
                    if (isExit2 && isExit2.name) {
                        info.name = isExit2.name + "(" + fMap.getFloor(+isExit2.floor).name + "层)";
                        info.type_num = 4;
                    };
                    /* 商家、公司 */
                    let isCompany = showBusinessList.find((item) => item.fid == info.fid);
                    if (isCompany && isCompany.name) {
                        info.name = isCompany.name + "(" + fMap.getFloor(+isCompany.level).name + "层)";
                        info.type_num = isCompany.type_num || 3;
                    };
                    /* 电梯 */
                    let isElevator = elevatorData.find((item) => item.fid == info.fid);
                    if (isElevator && isElevator.name) {
                        info.name = isElevator.name + "(" + fMap.getFloor(+isElevator.level || +isElevator.floor).name + "层)";
                        info.type_num = 6;
                    };
                    /* 厕所-安全出口-室外物体 */
                    let isServiceFacilities = serviceFacilitiesData.find((item) => item.fid == info.fid);
                    if (isServiceFacilities && isServiceFacilities.name) {
                        info.name = isServiceFacilities.name + "(" + fMap.getFloor(+isServiceFacilities.level || +isServiceFacilities.floor).name + "层)";
                        info.type_num = 7;
                    };
                }
            }

            if (info.type_num == 0 && info.fid) {
                /* 车位 */
                let isPlace = all_places.find((item) => item.fid == info.fid);
                if (isPlace) {
                    info.sstate = isPlace.state;
                    info.ttype = isPlace.type
                };
            }

            let infoname;
            if (info.name) {
                let index = info.name.indexOf('(');
                if (index != -1) {
                    infoname = [info.name.slice(0, index), info.name.slice(index)]
                }
            }

            let html = `
            <div>
                <div class="footer-box">
                    <div class="detail">`
            if (infoname) {
                html += `<div class="detail-name">地点：${infoname[0]}<span style="color:#686868">${infoname[1]}</span></div>`
            } else {
                html += `<div class="detail-name">地点：${info.name}</div>`
            }

            console.log('info', info);

            let hadFindCar = false
            if (findCarData && findCarData.fid && findCarData.fid == info.fid) {
                hadFindCar = true;
            }
            html += `<div class="detail-desc">${mapInfo ? mapInfo.mapName : ''}</div>
                    <div class="detail-operate">
                        <div onclick="debounceFn(markCurrentClick, 'locat')">
                            <img id="ParkingSpaceMarkings" class="features-icon" src="http://112.94.22.123:10087/rtls/wechat/${hadFindCar ? "mark_success2" : "mark_unsuccess2"}.png" alt="">
                            <span>车位标记</span>
                        </div>`
            if (c_list && c_list.length) {
                c_list.forEach((item) => {
                    if (item.fid == info.fid) {
                        info['collectId'] = item.id
                    }
                })
            } else {
                info['collectId'] = null
            }
            html += `<div onclick="collect(${info.collectId})">
                            <img src="http://112.94.22.123:10087/rtls/wechat/${info.collectId ? 'collect-success' : 'collect'}.png" alt="">
                            <span>收藏</span>
                        </div>
                        <div onclick="wxShare()">
                            <img src="http://112.94.22.123:10087/rtls/wechat/share.png" alt="">
                            <span>分享</span>
                        </div>`;
            if ((info.sstate || info.sstate === 0 || (info.ttype || info.ttype == 0))) {
                html += `
                  <div class="infoText">
                    <div class="infoText1 ${info.sstate || info.sstate == 0 ? "" : "infoTextHide"}">${info.sstate == 1 ? '占用' : (info.sstate == 0 ? '空闲' : '末检')}</div>
                    <div class="infoText1 ${info.ttype || info.ttype == 0 ? "" : "infoTextHide"}">${getPlaceTypeText(info.ttype)}</div>
                  </div>
                `;
            }
            html += `</div>
                </div>
                <div class="footer-features">
                    <div class="features-cancel" onclick="showFooterShowType2(null, false, 'cancel')">取消</div>
            `
            if (!varonlywall) {
                // 当前人所在楼层
                let m_level, m_floorName = '';
                if (currentCoord && currentCoord.x) {
                    m_level = currentCoord.level || currentCoord.groupID;
                    m_floorName = fMap.getFloor(+m_level).name;
                }

                if (getFloorNamePassF1(m_floorName) || isLicense) {

                } else {
                    html += `
                    <div class="features-navi" onclick="showNavi('drive')">
                        <img src="http://112.94.22.123:10087/rtls/wechat/drive.png" alt="">
                        <span>驾车</span>
                    </div>
                `
                }
            }
            html += `
                    <div class="features-navi" onclick="showNavi('walk')">
                        <img src="http://112.94.22.123:10087/rtls/wechat/step.png" alt="">
                        <span>步行</span>
                    </div>
                </div>
            </div>`

            $("#footerShowType2").html(html);
            // 动画效果
            setTimeout(() => {
                $("#footerShowType2").addClass('animation_show');
            }, 30);

            destination = info;

            if (isoutdoor) {
                destination.type = isoutdoor
            }
            // else {
            //     destination.type = ''
            // }
            var cons = null;
            if (naviAnalyser) {
                naviAnalyser = null;
            };
            naviAnalyser = new fengmap.FMNaviAnalyser({ map: fMap }, () => {
                cons = naviAnalyser.pathConstraint({ x: info.x, y: info.y, level: info.level || info.groupID, buildingID: null });
                if (cons) {
                    destination.x = cons.coords.x;
                    destination.y = cons.coords.y;
                }
            }, null);
        };
        $(".quickPosition").css('bottom', '66.6667vw')
    } else {
        $("#footerShowType2").removeClass('animation_show')
        $("#footerShowType2").html('');
        showFooterShowType1(true);
        if (cancel == 'cancel') {
            isViPreservation = false;
            isNaviName = "";
            isoutdoor = false;
            // 删除图片标注
            removeImageMarker();
            if (jumpOther && mapInfo.shortLink) {
                jumpOther = false;
                mqttPushMessage({
                    todo: "jumpToOther",
                })
            }
        }
        varonlywall = false;
        noShowDriveList = false;
        isLicense = false;
        $(".quickPosition").css('bottom', '46.9333vw');
    };
};

function getPlaceTypeText(type) {
    let placeTypeNameList = [{
        type: 0,
        name: '普通车位'
    }, {
        type: 1,
        name: '充电车位'
    }, {
        type: 2,
        name: '专属车位'
    }, {
        type: 3,
        name: '无障碍车位'
    }, {
        type: 4,
        name: '超宽车位'
    }, {
        type: 5,
        name: '子母车位'
    }, {
        type: 6,
        name: '小型车位'
    }];
    let target = placeTypeNameList.find((item) => {
        return item.type == type
    });

    return (target && target.name) ? target.name : '普通车位';
};

// 返回楼层是否高于F1层
function getFloorNamePassF1(name) {
    let hasF = name.includes("F") || name.includes("f");
    if (hasF) {
        // name中包括F/f--判断是否大于F1
        let num = name.replace(/[^0-9]/ig, "");
        if (num > 1) {
            return true;
        } else {
            return false;
        }
    } else {
        return false;
    }
};

// 收藏
var ttt;
function collect(id) {
    // 收藏防抖
    if (ttt) {
        clearTimeout(ttt);
    }
    var callNow = !ttt;
    ttt = setTimeout(() => {
        ttt = null;
    }, 1000)
    if (callNow) {
        push_collect(id)
    }
}

// 收藏与取消收藏
function push_collect(id) {
    if (id) {
        let d_index = c_list.findIndex((item) => {
            return item.id == id;
        });
        c_list.splice(d_index, 1)
        // 取消收藏
        pdelecollectwx(id)
        // mqttPushMessage({
        //     todo: "Pdelecollect",
        //     data: {
        //         url: "wechat/delStorePlace/" + id,
        //     }
        // })
    } else {
        // if (c_list.length >= 20) {
        //     let lastData = c_list[c_list.length - 1];
        //     c_list.splice(c_list.length - 1, 1)
        //     // 取消收藏

        //     mqttPushMessage({
        //         todo: "Pdelecollect",
        //         data: {
        //             url: "wechat/delStorePlace/" + lastData.id,
        //         }
        //     })
        // }

        let aaa = {
            map: mapInfo.mapId,
            x: destination.ox || destination.x,
            y: destination.oy || destination.y,
            floor: destination.level,
            name: destination.name,
            fid: destination.fid || destination.FID,
            icon: destination.icon || clickshowP.c_icon,
            typeNum: destination.type_num || destination.type_num == 0 ? destination.type_num + '' : clickshowP.c_type_num,
            outdoorType: clickshowP.c_outdoorType || '',
            ename: ''
        }
        paddcollcetwx(aaa);

        // mqttPushMessage({
        //     todo: "Paddcollcet",
        //     data: {
        //         url: "wechat/addStorePlace",
        //         data: {

        //         }
        //     }
        // })
    }
};

function pdelecollectwx(id) {
    $.ajax({
        url: `${formal}wechat/delStorePlace/${id}?userId=${userId}`,
        success: function (res) {
            pointAndNaviFid(showP.fid, true, showP.name);
        }
    })
};

function paddcollcetwx(data) {
    $.ajax({
        url: `${formal}wechat/addStorePlace?userId=${userId}`,
        data,
        success: function (res) {
            c_list.unshift(res.data)
            pointAndNaviFid(showP.fid, true, showP.name);
        }
    })
};

function showFooterShowType4(flag) {
    if (flag) {
        $("#footerShowType4").html(` <div class="footer-box">
            <div class="description" style="text-align: center;">已到达目的地附近，导航结束</div>
            <div class="closeBtn" onclick="showFooterShowType4(false)">关闭</div>
        </div>`);
        setTimeout(() => {
            $("#footerShowType4").addClass('animation_show')
        }, 30);
        // 语音提示
        if (!naviStart) {
            let bbb = {
                todo: "navigatip",
                data: {
                    str: '已到达目的地附近，导航结束',
                    path: -1
                },
                time: new Date().getTime(),
            };
            apiPostData2(bbb)

            // mqttPushMessage({
            //     todo: "navigatip",
            //     data: {
            //         str: '已到达目的地附近，导航结束',
            //         path: -1
            //     }
            // })
        }
    } else {
        noClick = false;
        isFirstNaviPoitNum = 1;
        setIntervalMarkTime = false;
        onceMarkPark = true;
        isoutdoor = false;
        newdestination = {};
        $("#footerShowType4").removeClass('animation_show')
        $("#footerShowType4").html('');
        showFooterShowType1(true);
    }
}

function setBackground(data) {
    let className = '.setBackgroung' + data;
    $(className).css('background', '#eee')

};

function gotoDestination() {
    getPlaceIdle = true;
    elevator_navi = false;
    placeNavi.firstRecommand = -1;
    getTargetElevator();
    showDedtiOrPlaceDialog(false);
}

function showExitTips(flag) {
    // if (flag) {
    //     $("#exitTips").html(`<div id="isStopNavi" class="footer footer-box exit-tips exit-show animation_hide">
    //         <span class="exit-hide" onclick="showExitTips(false)">取消</span>
    //         <span class="exit-line"></span>
    //         <span class="enter-exit" onclick="selfStopNavi()">退出导航</span>
    //     </div>`);
    //     setTimeout(() => {
    //         $("#isStopNavi").addClass("animation_show")
    //     }, 30);
    // } else {
    //     $("#isStopNavi").removeClass("animation_show");
    //     setTimeout(() => {
    //         $("#exitTips").html('');
    //     }, 300);
    // }

    if (flag) {
        showModal("确定退出导航", () => {
            $('#dialog').html('');
            selfStopNavi();//退出导航
        }, () => {
            $('#dialog').html('');
        }, '取消', '退出导航')
    }
}

function selfStopNavi() {
    clearTimeout(resetttt);
    resetttt = null;

    showExitTips(false);
    stopNavi('手动停止');
    clearCirclM(); // 清除圆
    setIntervalMarkTime = false; // 清除自动标车位
    onceMarkPark = true;
    queue_len = 6;
    newdestination = {};
    naviStart = false;
    noClick = false;
    isoutdoor = false;
    isNaviName = "";
    isViPreservation = false;
    noShowDriveList = false;
}

function pointAndNaviCoord(coord, naviType) {

}

function getTargetElevator() {
    let final_pos = {};
    final_pos.hasRes = false;
    searchNearModel(currentCoord, (currentCoord) => {
        try {
            naviAnalyser.route({
                start: {
                    x: +currentCoord.x,
                    y: +currentCoord.y,
                    level: +currentCoord.level || +currentCoord.groupID
                },
                dest: {
                    x: +destination.x,
                    y: +destination.y,
                    level: +destination.level
                },
                mode: fengmap.FMNaviMode.MODULE_SHORTEST,
                toDoors: true,
                priority: fengmap.FMNaviPriority.PRIORITY_ESCALATORONLY
            }, (result) => {
                naviRoute(currentCoord, null, "drive");
            }, (fail) => {
                let name = destination.name.split("(")[0];
                let near_exit = findNaviResult(currentCoord);
                if (near_exit.hasRes) {
                    let elevator = getTargetNearElevator(near_exit);
                    let elevatorName = "电梯";
                    let final_name = null;
                    if (elevator) {
                        elevatorName = elevator.name;
                        // final_name = name + "(" + elevatorName + "层" + ")";
                        final_name = elevatorName + "(" + fMap.getFloor(+elevator.level).name + "层)";

                    } else {
                        // final_name = name + "(" + elevatorName + fMap.getFloor(+near_exit.level).name + "层" + "电梯)";
                        final_name = name + "(" + fMap.getFloor(+near_exit.level).name + "层)";
                    }
                    finish = {
                        x: near_exit.x,
                        y: near_exit.y,
                        fid: '',
                        level: near_exit.level,
                        autoMark: false,
                        name: final_name
                    };
                    if (naviType == 'drive' && final_name) {
                        newdestination.name = final_name
                    }

                    naviRoute(currentCoord, null, "drive", finish, null, null, null, true);
                } else {
                    isFirstNaviPoit();
                }
            })
        } catch (error) {
            isFirstNaviPoit();
        }
    });
}

function getTargetNearElevator(coord) {
    let elevator = null;
    let elevatorArr = [];
    elevatorData.forEach((item) => {
        if (item.floor == coord.level) {
            let pos1 = {
                x: coord.x,
                y: coord.y,
            };
            let pos2 = {
                x: item.x,
                y: item.y,
            };
            let cal_dis = fengmap.FMCalculator.distance(pos1, pos2);
            elevatorArr.push({
                // ...item,
                name: item.name,
                level: item.floor,
                dis: cal_dis
            })
        }
    });
    elevatorArr = elevatorArr.sort((a, b) => {
        return a.dis - b.dis
    });
    elevator = elevatorArr[0]

    // const searchRequest = new fengmap.FMSearchRequest();
    // let circle = {
    //     radius: 10,
    //     center: {
    //         x: coord.x,
    //         y: coord.y
    //     }
    // };
    // searchRequest.levels = [coord.level]
    // searchRequest.addCondition({
    //     circle: circle,
    //     // typeID: [340855],
    //     keyword: "梯"
    // });
    // if (!searchAnalyser) {
    //     searchAnalyser = new fengmap.FMSearchAnalyser({ map: fMap }, function () { });
    // }
    // searchAnalyser.query(searchRequest, (result) => {
    //     let dis = 100000;
    //     console.log('result', result);
    //     if (result) {
    //         let len = result.length;
    //         for (var i = 0; i < len; i++) {
    //             let model = result[i];
    //             if (model) {
    //                 let cal_dis = model.distance;
    //                 if (dis > cal_dis) {
    //                     dis = cal_dis;
    //                     elevator = model;
    //                 }
    //             }
    //         }
    //     }
    // })
    return elevator;
}

function findNaviResult(startCoords) {
    let final_pos = {};
    final_pos.hasRes = false;
    try {
        naviAnalyser.route({
            start: {
                x: startCoords.x,
                y: startCoords.y,
                level: startCoords.level || startCoords.groupID
            },
            dest: {
                x: destination.x,
                y: destination.y,
                level: destination.level
            },
            mode: fengmap.FMNaviMode.MODULE_SHORTEST,
            toDoors: true,
            priority: fengmap.FMNaviPriority.PRIORITY_DEFAULT
        }, (result) => {
            let subs = result.subs;
            let floor_index = -10000;
            for (var i = 0; i < subs.length; i++) {
                if (subs[i].distance == 0) {
                    if (i >= 1 && i >= floor_index) {
                        let path = subs[i - 1];
                        let points = subs[i - 1].waypoint.points;
                        let path_end = points[points.length - 1];
                        final_pos.hasRes = true;
                        final_pos.x = path_end.x;
                        final_pos.y = path_end.y;
                        final_pos.level = path_end.level;
                        floor_index = i;
                    }
                }
            }
            if (!final_pos.hasRes) {
                final_pos.x = destination.x;
                final_pos.y = destination.y;
                final_pos.level = destination.level;
                final_pos.hasRes = false;
            }

        }, (fail) => { })
    } catch (error) { }

    return final_pos;
}

function gotoPlace(type, show = 'none') {
    getPlaceIdle = true;
    elevator_navi = false;
    placeNavi.firstRecommand = 1;
    if (type == 0) {
        ifshownavibox = show // 找车位
        getRecommend(destination, false, type, 'ordinary')

    } else if (type == 1) {
        ifshownavibox = show // 找充电桩
        getRecommend(destination, false, type, 'chargstation')
    } else if (type == 3) {
        ifshownavibox = show // 找无障碍
        getRecommend(destination, false, type, 'accessibility')
    }
    showDedtiOrPlaceDialog(false);
}
async function getRecommend(item, nearby, type, category) {
    let { x, y, groupID, fid, name = '' } = item;
    destination = item;
    name = name.split("(")[0];
    let near_exit = null;
    let para = null;
    isnearby = nearby;
    if (nearby) {
        // 就近停车
        placeNavi.destination_nearby = true;
        placeNavi.destination_name = name;
        placeNavi.destination_x = item.x;
        placeNavi.destination_y = item.y;
        placeNavi.destination_floor = item.groupID;

        para = {
            companyName: name,
            x: item.x,
            y: item.y,
            floor: item.groupID,
            map: mapInfo.mapId,
            carBitType: type,
            isVip: 0
        };
        placeNavi.place_vip = 0;
        placeNavi.carBitType = type;
        requestPara = para;
        isNaviName = category; // 'nearby'; // 就近停车


        $.ajax({
            url: `${formal}park/getWechatPlaceByCompanyName?userId=${userId}`,
            type: 'GET',
            data: para,
            success: function (res) {
                let resData = res.data;
                getWechatPlaceByCompanyName11(resData)
            }
        })
        // mqttPushMessage({
        //     todo: "api",
        //     data: {
        //         url: "park/getWechatPlaceByCompanyName",
        //         data: para
        //     }
        // })
    } else {
        let target = await findSelectTargetElevator(item);
        placeNavi.destination_name = name;
        placeNavi.destination_nearby = false;
        placeNavi.destination_x = item.x;
        placeNavi.destination_y = item.y;
        placeNavi.destination_floor = item.groupID;
        para = {
            companyName: name,
            // x: target.x,
            // y: target.y,
            x: target.ox || target.x,
            y: target.oy || target.y,
            floor: target.level,
            map: mapInfo.mapId,
            carBitType: type,
            isVip: 0,
        };
        placeNavi.place_vip = 0;
        placeNavi.carBitType = type;
        requestPara = para;
        isNaviName = category; // 'ordinary'; // 找车位  chargstation：找充电桩

        $.ajax({
            url: `${formal}park/getWechatPlaceByCompanyName?userId=${userId}`,
            type: 'GET',
            data: para,
            success: function (res) {
                let resData = res.data;
                getWechatPlaceByCompanyName11(resData)
            }
        })
        // mqttPushMessage({
        //     todo: "api",
        //     data: {
        //         url: "park/getWechatPlaceByCompanyName",
        //         data: para
        //     }
        // })
    }
};

function getWechatPlaceByCompanyName11(resData) {
    if (!resData) {
        if (placeNavi.carBitType == 1) {
            // 找充电桩，找不到后推荐普通车位逻辑
            getPlaceIdle = false;
            recommandCommonPlace();
        } else if (placeNavi.place_vip == 1) {
            // 无vip,推荐普通车位
            getPlaceIdle = false;
            noVipToPlainSpace();
        } else if (placeNavi.carBitType == 3) {
            // 无-无障碍车位-推荐普通车位
            getPlaceIdle = false;
            recommandCommonPlace2();
        } else {
            // 没有车位提示
            getPlaceIdle = false;

            let bbb = {
                todo: "navigatip",
                data: {
                    str: '暂无空车位，请稍后再试',
                    path: -1
                },
                time: new Date().getTime(),
            };
            apiPostData2(bbb);
            // mqttPushMessage({
            //     todo: "navigatip",
            //     data: {
            //         str: '暂无空车位，请稍后再试',
            //         path: -1
            //     }
            // })
            if (!naviStart) {
                toolTipBox('暂无空车位，请稍后再试');
            } else {
                showModal("暂无空车位，请稍后再试", () => {
                    $('#dialog').html('');
                    selfStopNavi();//退出导航
                }, () => {
                    $('#dialog').html('');
                }, '继续导航', '退出导航')
            }
        }
        return false;
    };
    isRecommendParkingData = resData[0];
    const target = resData[0];
    requestPara['place'] = target;
    requestPara['time'] = new Date();
    placeNavi.place_fid = target.fid;
    place_recommand.push(requestPara);
    target.vip = 0;
    target.carBitType = placeNavi.carBitType;
    if (isnearby) {
        // 就近停车
        if (ifshownavibox == 'none') {
            recommandPlaceNavi(destination, target, true);
        } else {
            // 切换下一个车位
            switchparkingspace(target)
        }
    } else if (isViPreservation) {
        target.vip = 1;
        placeNavi.place_vip = 1;
        placeNavi.place_name = target.name;
        placeNavi.carBitType = target.carBitType || 0;
        placeNavi.lastTime = new Date();
        if (vipNaviTyep == 'none' && !naviStart) {
            // vip车位
            pointAndNaviFid(target.fid, true, null, null, 'vip_place');
            showFooterShowType1(false);
        } else if (vipNaviTyep == 'vip' || naviStart) {
            // 切换下一个车位
            switchparkingspace(target)
        }
    } else {
        // 找车位
        if (ifshownavibox == 'none') {
            recommandPlaceNavi(destination, target, false);
        } else if (ifshownavibox == 'show2' || ifshownavibox == 'show1') {
            // 切换下一个车位
            switchparkingspace(target)
        }
    }
};

async function findSelectTargetElevator(item) {
    let { x, y, level, fid, name } = item;
    let target;
    let final_pos = {
        hasRes: false
    };
    if (!currentCoord.x) {
        noCurrentCoord();
        return false;
    }
    var proResult = await new Promise((resolve, reject) => {
        searchNearModel(currentCoord, (startCoord) => {
            naviAnalyser.route({
                start: {
                    x: startCoord.x,
                    y: startCoord.y,
                    level: startCoord.level
                },
                dest: {
                    x: destination.x,
                    y: destination.y,
                    level: destination.level
                },
                mode: fengmap.FMNaviMode.MODULE_BEST,
                priority: fengmap.FMNaviPriority.PRIORITY_ESCALATORONLY
            }, (result) => {
                target = destination;
                resolve("success");
            }, (fail) => {
                let near_exit = findNaviResult(startCoord);
                if (near_exit.hasRes) {
                    final_pos.x = near_exit.x;
                    final_pos.y = near_exit.y;
                    final_pos.level = near_exit.level;
                } else {
                    final_pos.x = item.x;
                    final_pos.y = item.y;
                    final_pos.level = currentCoord.level;
                }
                resolve("fail");
            });
        });
    })
    if (proResult == "success") {
        return target;
    } else if (proResult == "fail") {
        return final_pos;
    }
}

function isDuringTimes(startdate, enddate) {
    var newdate = new Date();
    var startdate = new Date(startdate);
    var enddate = new Date(enddate);
    var a = newdate.getTime() - startdate.getTime();
    var b = newdate.getTime() - enddate.getTime();

    if (a / 1000 < -1800 || b > 0) {
        return false;
    } else {
        return true;
    }
}

// 设置底图
// 当前楼层为F1时添加瓦片底图
function SetTileLayerMode(nowlevel, type = 'none') {
    if (!nowlevel) return;

    if (!isMany && type == 'none') {
        // 多层显示时不显示瓦片底图
        return;
    }

    let floorName = fMap.getFloor(nowlevel).name;
    if (floorName != 'f1' && floorName != 'F1') {
        if (tileLayer != null) {
            tileLayer.remove(fMap);
            tileLayer = null;
        };
        return;
    };
    if (tileLayer != null) {
        tileLayer.remove(fMap);
        tileLayer = null;
    };
    let options = {
        //高德道路图 AMAP_VECTOR    2
        //百度矢量   BAIDU_VECTOR   4
        mode: coordinate == "2" ? fengmap.FMTileProvider.AMAP_VECTOR : fengmap.FMTileProvider.BAIDU_VECTOR,
        offset: 0,
    };
    tileLayer = new fengmap.FMTileLayer(options);
    tileLayer.addTo(fMap);
};

// 收藏夹回退显示收藏位置
async function collectBack(app) {
    showP = {
        fid: app.result.fid,
        name: app.result.name,
        x: app.result.x,
        y: app.result.y,
        level: app.result.floor
    }
    if (app.result.start && app.result.end) {
        isViPreservation = true;
        isNaviName = 'vip'; // 没有地锁的-占用逻辑
        vipNaviTyep = 'vip';
        placeNavi['place_vip'] = 1;
        placeNavi.place_vip = 1;

        vipData = {
            map: mapInfo.mapId,
            carBitType: null,
            isVip: '1',
            x: app.result.x,
            y: app.result.y,
            floor: app.result.floor,
            placeName: app.result.name,
            companyName: ''
        }

        if (!vipData.x || !vipData.y) {
            const model = await resSearchByFid(app.result.fid);
            if (model) {
                vipData.x = model.center.x;
                vipData.y = model.center.y;
                vipData.floor = model.level;
            }
        }
    }
    placeNavi.place_fid = app.result.fid;
    pointAndNaviFid(app.result.fid, true, app.result.name, { x: app.result.x, y: app.result.y, floor: app.result.floor }, null, false, app.result.typeNum || null)
}

// 标车位
function markCurrentClick(type) {
    let pushdata = {}
    if (type == 'posi') {
        // 页面右边标车位
        if (!currentCoord.x) {
            noCurrentCoord();
            return false
        };
        if (!fixedView) {
            let data = {
                x: locationMarker.x,
                y: locationMarker.y,
                level: locationMarker.level
            };
            let current_level = fMap.getLevel();
            if (data.level != current_level) {
                fMap.setLevel({
                    level: +data.level
                });
            };
            fMap.setCenter({
                x: +data.x,
                y: +data.y
            })
        };

        if (naviStart) {
            // 导航过程中
            currentCoordMark('none');
        } else {
            showModal("标记停车位置", () => {
                $('#dialog').html('');
                currentCoordMark();
            }, () => {
                $('#dialog').html('');

            })
        }

    } else {
        // 地点车位标记
        $("#ParkingSpaceMarkings").attr('src', 'http://112.94.22.123:10087/rtls/wechat/mark_success2.png');
        pushdata = {
            x: destination.ox,
            y: destination.oy,
            name: '车辆停放处',
            fid: destination.FID,
            floor: destination.level,
            map: mapInfo.mapId
        };
        let current_level = fMap.getLevel();
        if (pushdata.floor != current_level) {
            fMap.setLevel({
                level: +pushdata.floor
            });
        };
        fMap.setCenter({
            x: +pushdata.x,
            y: +pushdata.y
        });
        PushSocket(pushdata, "show")
    };
}

// 用当前位置标车位
function currentCoordMark(showT = 'show') {
    // peripherySearch(currentCoord, (res) => {
    //     if (res.length) {
    //         data = res[0];
    //         pushdata = {
    //             x: data.center.x,
    //             y: data.center.y,
    //             name: data.name || '车辆停放处',
    //             fid: data.FID,
    //             floor: data.level,
    //             map: mapInfo.mapId
    //         }
    //     } else {
    //         noCurrentCoord();
    //         return false;
    //     }
    //     PushSocket(pushdata)
    // });   不用查找周边的模型
    pushdata = {
        x: currentCoord.x,
        y: currentCoord.y,
        name: '车辆停放处',
        fid: null,
        floor: currentCoord.level || currentCoord.groupID,
        map: mapInfo.mapId,

    }
    PushSocket(pushdata, showT)
};

// 标车位，发起socket
function PushSocket(data, showT) {
    $.ajax({
        url: `${formal}wechat/addLicensePos?userId=${userId}`,
        data,
        success: function (res) {
            if (showT === 'show') {
                showModal('车位标记成功，可用于反向寻车', () => {
                    $('#dialog').html('');
                    addFindCar();
                }, () => {
                    $('#dialog').html('');
                    addFindCar();
                })
            }
            addFindCar(false);
        }
    })

    // mqttPushMessage({
    //     todo: "markCurrentClick",
    //     data: {
    //         url: "wechat/addLicensePos",
    //         data,
    //         showT
    //     }
    // })
};

// 就近停车
function nearby2(flag) {
    if (flag) {

        if (currentCoord && currentCoord.x) {
            // 当前人所在楼层
            let m_level, m_floorName = '';
            if (currentCoord && currentCoord.x) {
                m_level = currentCoord.level || currentCoord.groupID;
                m_floorName = fMap.getFloor(+m_level).name;
            }
            if (getFloorNamePassF1(m_floorName) && !naviStart) {
                toolTipBox("当前楼层不支持驾车导航,<br/ >请移动到可驾车行驶楼层中");
                return false;
            }
        } else {
            noCurrentCoord();
            return false;
        };

        $("#dedtiOrPlaceDialog").html(`<div class="dedtiOrPlaceDialog">
            <div class="dialog-mask animation_opac"></div>
            <div class="dialog-container animation_scale">
                <div class="dialogBody" style="overflow:hidden">
                    <span class="dialogBtn setBackgroung1" ontouchstart="setBackground(1)" ontouchend="nearby3(1)">当前位置</span>
                    <span class="dialogBtn setBackgroung2" ontouchstart="setBackground(2)" ontouchend="nearby3(2)">就近电梯</span>
                </div>
                <div class="dialogClose" onclick="showDedtiOrPlaceDialog(false)"><img src="http://112.94.22.123:10087/rtls/wechat/close_white.png" alt=""></div>
            </div>
        </div>`);

        setTimeout(() => {
            $("#dedtiOrPlaceDialog .dialog-container").addClass("animation_show2")
            $("#dedtiOrPlaceDialog .dialog-mask").addClass("animation_show3")
        }, 30);
    } else {
        $("#dedtiOrPlaceDialog .dialog-container").removeClass("animation_show2");
        $("#dedtiOrPlaceDialog .dialog-mask").removeClass("animation_show3");
        setTimeout(() => {
            $("#dedtiOrPlaceDialog").html('');
        }, 200);
    }
};

function nearby3(type) {
    nearbyType = type;
    nearby('none');
    nearby2(false);
};

// 就近停车
function nearby(type = 'none') {
    if (currentCoord && currentCoord.x) {
        // // 判断socket是否连接成功
        // if (whetherBreakSocket) {
        //     return false
        // }
        // 就近停车类别
        if (nearbyType == 1) {
            // 当前位置最近车位
            getRecentlyCurrentCoord(type);
        } else if (nearbyType == 2) {
            // 电梯口最近车位
            getRecentlyElevator(type);
        }
    } else {
        noCurrentCoord();
    }
};
// 当前位置最近车位
function getRecentlyCurrentCoord(type) {
    let query_data = {
        x: currentCoord.x,
        y: currentCoord.y,
        groupID: currentCoord.groupID,
    };
    getPlaceIdle = true;
    ifshownavibox = type;
    getRecommend(query_data, true, 0, 'nearby')
};

// 电梯口最近车位
function getRecentlyElevator(type) {
    let near_x = 0,
        near_y = 0,
        dis = 100000,
        has_res = false;
    let query_data = {
        x: currentCoord.x,
        y: currentCoord.y,
        groupID: currentCoord.groupID,
    };
    getPlaceIdle = true;
    // 找电梯
    var analyser = new fengmap.FMSearchAnalyser({ map: fMap }, function () {
        var searchRequest = new fengmap.FMSearchRequest();
        searchRequest.levels = [currentCoord.groupID];
        searchRequest.type = fengmap.FMType.FACILITY;
        searchRequest.addCondition({
            'typeID': [170001, 170002, 170003, 170004, 170005, 170006]
        });

        analyser.query(searchRequest, (result) => {
            if (result) {
                let len = result.length;
                for (var k = 0; k < len; k++) {
                    let model = result[k];
                    let elevalator = [model.center.x, model.center.y, 2];
                    let start = [currentCoord.x, currentCoord.y, 2];
                    let cal_dis = calculate_two_near_BeaconDis(elevalator, start);
                    if (dis > cal_dis) {
                        dis = cal_dis;
                        near_x = elevalator[0];
                        near_y = elevalator[1];
                        has_res = true;
                    }
                }
                if (has_res) {
                    query_data.x = near_x;
                    query_data.y = near_y;
                };
                ifshownavibox = type;
                getRecommend(query_data, true, 0, 'nearby')
            } else {
                noCurrentCoord();
            }
        });
    })
};

// 反向寻车
function findCar() {
    if (currentCoord && currentCoord.x) {
        // 判断socket是否连接成功
        // if (whetherBreakSocket) {
        //     return false
        // }
        $.ajax({
            url: `${formal}wechat/getLicensePos?map=${mapInfo.mapId}&userId=${userId}`,
            success: function (res) {
                const data = res.data;
                if (!data) {
                    return;
                };
                if (!data.x) {
                    return
                };
                jumpOther = true;
                resultfindCar(res)
            },
        })

        // mqttPushMessage({
        //     todo: "findCar",
        //     data: {
        //         url: "wechat/getLicensePos",
        //         data: {
        //             map: mapInfo.mapId
        //         }
        //     }
        // })
    } else {
        noCurrentCoord();
    }
};

var findCarData;
function addFindCar(show = true) {
    $.ajax({
        url: `${formal}wechat/getLicensePos?map=${mapInfo.mapId}&userId=${userId}`,
        success: function (res) {
            const data = res.data;
            if (!data) {
                return;
            };
            if (!data.x) {
                return
            };
            findCarData = data
            if (show) {
                addFindCarToMap(data);
            }
        },
    })
};

var findCarMarker;
function addFindCarToMap(data) {
    if (findCarMarker) {
        findCarMarker.remove();
        findCarMarker = null;
    }
    findCarMarker = new fengmap.FMImageMarker({
        x: +data.x,
        y: +data.y,
        url: './image/findCarIcon.png',
        size: 30,
        height: 2,
        collision: false
    });
    const floor = fMap.getFloor(+data.floor)
    findCarMarker.addTo(floor);
};

// 反向寻车
function resultfindCar(data) {
    console.log('data', data);
    destination = data.data;
    destination['FID'] = data.data.fid || '';
    destination['level'] = +data.data.floor;
    // 反向寻车画圆
    var floor = fMap.getFloor(+data.data.level);
    clearCirclM();


    /* 添加圆形 */
    var circle_radius = 8;
    var circle_segments = 100;
    var circle_center = {
        x: +data.data.x,
        y: +data.data.y
    };
    /* 使用 FMCalculator 的矩形构造器可以快速构造出圆形的几何形状坐标集合 */
    var circleOption = {
        points: fengmap.FMCalculator.circleBuilder(circle_radius, circle_center, circle_segments),
        x: +data.data.x,
        y: +data.data.y,
        height: 5,
        color: '#3CF9DF',
        opacity: 0.3,
        borderWidth: 2,
        borderColor: '#3CF9DF',
    }
    circleMarker = new fengmap.FMPolygonMarker(circleOption);
    circleMarker.addTo(floor);


    showNavi('walk');
};

// 清除圆
function clearCirclM() {
    if (circleMarker) {
        circleMarker.remove();
        circleMarker = null
    };
};

// 指引页面
function showViewGuide() {
    let html = `
    <div class="guideSwitch">
        <div class="switchBox">
    `;
    for (let i = 0; i < welcomeList.length; i++) {
        html += `
            <div class="switchImg">
                <img src="http://112.94.22.123:10087/rtls/${welcomeList[i]}"></img>
            </div>
        `
    }
    html += `</div><div class="switchLi">`
    for (let i = 0; i < welcomeList.length; i++) {
        html += `
            <span class="switchspan ${i == 0 ? 'switchactive' : ''}"></span>
        `
    }
    // html += `</div><div class="startusing" onclick="startusing()">开始使用</div></div>`
    html += `</div>
            <div class="guide_footer_btn">
                <div class="guide_f_btn guide_f_btn1" onclick="startusing()">跳过</div>
                <div class="guide_f_btn guide_f_btn2 next_guide" id="next_guide">下一页</div>
            </div>
        </div>`

    $("#guideView").html(html);
    doSwitch();
};

function doSwitch() {
    // 1. 获取元素
    var focus = document.querySelector('.guideSwitch');
    var ul = document.querySelector('.switchBox');
    var ol = document.querySelector('.switchLi');
    var next = document.querySelector('.next_guide');

    var w = focus.offsetWidth;;
    var index = 0;

    ul.addEventListener('transitionend', function () {
        ol.querySelector('.switchactive').classList.remove('switchactive');
        ol.children[index].classList.add('switchactive');
    });

    var startX = 0;
    var moveX = 0;
    var flag = false;

    var changeImg = function () {
        if (moveX > 0) {
            index--;
            if (index <= 0) {
                index = 0;
            }
        } else {
            // 如果是左滑就是播放下一张，moveX是负值
            index++;
            if (index > welcomeList.length - 1) {
                index = welcomeList.length - 1
            }
        }
        // 用最新的index乘以宽度
        var translateX = -index * w;
        ul.style.transition = 'all .3s';
        ul.style.transform = 'translateX(' + translateX + 'px)';
        if (index == welcomeList.length - 1) {
            // var use = document.getElementsByClassName('startusing');
            // use[0].classList.add('showusing');
            $("#next_guide").html('开始使用')
        } else {
            // var use = document.getElementsByClassName('startusing');
            // use[0].classList.remove('showusing');
            $("#next_guide").html('下一页')
        }
    }

    ul.addEventListener('touchstart', function (e) {
        startX = e.targetTouches[0].pageX;
    });
    ul.addEventListener('touchmove', function (e) {
        moveX = e.targetTouches[0].pageX - startX;
        var translateX = -index * w + moveX;
        ul.style.transition = 'none';
        ul.style.transform = 'translateX(' + translateX + 'px)';
        flag = true;
        e.preventDefault();
    });
    ul.addEventListener('touchend', function (e) {
        if (flag) {
            //如果移动距离大于50像素，则播放上一张或者下一张
            if (Math.abs(moveX) > 50) {
                // 如果是右滑就是播放上一张，moveX是正值
                changeImg();
            } else {
                //如果移动距离小于50像素就回弹
                var translateX = -index * w;
                ul.style.transition = 'all .1s';
                ul.style.transform = 'translateX(' + translateX + 'px)';
            }
        }
    });

    next.addEventListener('click', function (e) {
        let text = e.target.innerHTML
        if (text == '下一页') {
            moveX = -100;
            changeImg()
        } else {
            startusing();
        }
    })
};

// 开始使用
function startusing() {
    $("#guideView").html('');
    // socket.send(JSON.stringify({
    //     todo: "startusing",
    // }));

    mqttPushMessage({
        todo: "startusing",
    })
};

// 显示语音弹窗
function getVoiceRec(even, show) {
    // 阻止事件冒泡
    var even = window.event || arguments.callee.caller.arguments[0];
    even.preventDefault();
    even.stopPropagation();
    if (show) {
        let html = `
        <div class="pop-mask">
            <div class="voicePop">
                <div class="weui-dialog__cancel" onclick="getVoiceRec(this,false)">
                    <img src="http://112.94.22.123:10087/rtls/wechat/close.png"></img>
                </div>
                <div class="voice_top">
                    <div class="voive_top_noanimation">
                        <text style="--d: 7"></text><text style="--d: 6"></text><text style="--d: 5"></text><text style="--d: 4"></text><text style="--d: 3"></text><text style="--d: 2"></text><text style="--d: 1"></text><text style="--d: 0"></text><text style="--d: 1"></text><text style="--d: 2"></text><text style="--d: 3"></text><text style="--d: 4"></text><text style="--d: 5"></text><text style="--d: 6"></text><text style="--d: 7"></text>
                    </div>
                    <div class="voive_top_tips">请长按录音按钮</div>
                </div>
                <div class="voice_bottom_btn" ontouchstart="down()" ontouchend="up()">长按录音</div>
            </div>
        </div>
        `;
        $("#voice").html(html);
    } else {
        $("#voice").html('');
    }
};

// 录音点击事件
function down() {
    socket.send(JSON.stringify({
        todo: "voive",
        data: {
            url: "start",
        }
    }));
};

// 成功开始录音
function voiceonstart() {
    var ani = document.getElementsByClassName('voive_top_noanimation');
    ani[0].classList.add('voive_top_animation');
    document.getElementsByClassName('voive_top_tips')[0].innerHTML = '正在聆听中...'
};

// 录音结束
function up() {
    socket.send(JSON.stringify({
        todo: "voive",
        data: {
            url: "end",
        }
    }));
};

// 成功拿到语音回调
function voicesuccess(data) {
    // 语音的内容
    if (data.result.inputValue) {
        voiceValue = data.result.inputValue
        getVoiceRec(this, false);
        getSearchPop();
    }
    var ani = document.getElementsByClassName('voive_top_noanimation');
    ani[0].classList.remove('voive_top_animation');
    document.getElementsByClassName('voive_top_tips')[0].innerHTML = data.result.voiceTips;
};


function wxShare() {
    // socket.send(JSON.stringify({
    //     todo: "wxshare",
    //     data: destination
    // }));

    mqttPushMessage({
        todo: "wxshare",
        data: destination
    })
}

// 键盘弹出，动态修改样式
function setingBottom(data) {
    let dom = document.getElementsByClassName('page-container')[0];
    $(".animation_hide").removeClass('animation_hide')
    // dom.style.cssText = `bottom:${data.height > 0 ? -data.height : data.height}px`
    if (data.height > 0) {
        dom.style.cssText = `top:0;height:100vh`
    } else {
        dom.style.cssText = `bottom:0;height:90vh`
    }
}

// 空闲车位颜色
function addPlaceColor(data, init) {
    var levels = fMap.getLevels();
    if (data.length) {
        for (var i = 0; i < levels.length; i++) {
            var floor = fMap.getFloor(levels[i]);
            var layers = floor.getLayers(fengmap.FMType.MODEL_LAYER)[0];
            data.forEach((dt) => {
                if (dt.fid) {
                    const model = layers.getFeatures().find(item => item.FID === dt.fid);
                    if (model) {
                        /* 设置模型颜色 */
                        if (dt.state == '0') {
                            model.setColor('#83F58D', 1);
                            model.setBorderColor('#45833B', 1);
                        } else if (dt.state == '1') {
                            model.setColor('#FDBBC5', 1);
                            model.setBorderColor('#BA646E', 1);
                        } else if (dt.state == '3') {
                            model.setColor('#C2C2C2', 1);
                            model.setBorderColor('#474545', 1);
                        } else {
                            model.resetColor();
                            model.resetBorderColor();
                        };

                        if (init == 'first') {
                            /* 设置模型名字 */
                            let bound = model.bound;
                            let x = +bound.center.x;
                            let y = +bound.center.y;
                            let bx = (bound.max.x + (bound.max.x + bound.center.x) / 2) / 2;
                            let by = (bound.max.y + (bound.max.y + bound.center.y) / 2) / 2;

                            let nameInfo = {
                                x: x,
                                y: y,
                                level: model.level || dt.floor,
                                name: dt.name,
                                target: dt
                            };
                            // if (bound.size.x > bound.size.y) {
                            //     nameInfo.x = bx
                            // } else {
                            //     nameInfo.y = by
                            // }
                            // setPlaceModelName(nameInfo);
                            // /* 设置模型类型图标 */
                            // setPlaceModelTypeImg(dt, model.bound);
                            setPlaceNameAndImage(nameInfo);
                        }
                    }
                } else {
                    /* 如果车位是没有fid的只添加上名字 */
                    if (init == 'first') {
                        let nameInfo = {
                            x: dt.x,
                            y: dt.y,
                            level: dt.floor,
                            name: dt.name,
                            target: dt
                        };
                        // setPlaceModelName(nameInfo);
                        setPlaceNameAndImage(nameInfo);
                    }
                }


            })
        }
    }
};

function setPlaceNameAndImage(data) {
    if (!data.level) {
        return false
    }
    let target = data.target;

    let url = '';
    if (target.type == 1) {
        /* 充电车位 */
        url = './image/placeType1.png'
    } else if (target.type == 2) {
        /* 专属车位 */
        url = './image/placeType2.png'
    } else if (target.type == 3) {
        /* 无障碍车位 */
        url = './image/placeType3.png'
    };
    if (url) {
        var compositeOptions = {
            height: 0,      // 离地高度, 默认 1
            depth: false,   // 文本渲染深度 【true】：开启深度；【false】：关闭深度
            anchor: {
                baseon: 'image',    // 锚点基于图片还是文字还是全部（all：全部，image：图片，text：文字）
                anchor: 'CENTER',   // 锚点位置
            },
            collision: true,        // true:开启避让 false:关闭避让 默认 true，注：render为flat平铺时不支持避让
            render: 'billboard',    // 渲染方式，billboard始终面向屏幕，flat平铺
            layout: {
                style: 'timage-btext',
                align: 'center'
            },
            image: {
                url, // 图片url地址
                size: [48, 48],       // 图片大小，数组第一个元素是宽，第二个元素为高，默认为[32, 32]
            },
            text: {
                padding: [0, 0, 0, 0],      // 文字内边距默认值为 [0,0,0,0]
                plateStrokeWidth: 1,       // 文字背景边框宽度，注：只有描边颜色没有描边宽度时，默认描边宽度为 1
                content: {
                    textAlign: 'Center',  // 文字对齐方式
                    lineSpacing: 2,                        // 行间距默认为2，当小于2时也等于2
                    fontSize: 13,                           // 文本字号, 默认20px
                    fillColor: '#252525',           // 文本填充色，rgb类型字符串，默认 "255,0,0"
                    strokeColor: '#fff',     // 文本边框填充色，rgb类型字符串。默认 '255,255,0'
                    strokeWidth: 1,  // 描边线宽
                    text: data.name,  // 文字内容，必填
                }
            },
            x: +data.x,
            y: +data.y
        };

        var compositeMarker = new fengmap.FMCompositeMarker(compositeOptions);
        let floor = fMap.getFloor(+data.level);
        compositeMarker.addTo(floor);
    } else {
        let textMarker = new fengmap.FMTextMarker({
            height: 0,
            fontFamily: '微软雅黑',
            fillColor: '#252525',
            strokeWidth: 1.5,
            strokeColor: '#fff',
            anchor: 'CENTER',
            fontSize: 13,
            depth: true,
            collision: true,
            text: data.name,
            x: +data.x,
            y: +data.y,
        });
        let floor = fMap.getFloor(+data.level);
        textMarker.addTo(floor)
    }
};

// 设置车位名字
function setPlaceModelName(data) {
    let textMarker = new fengmap.FMTextMarker({
        height: 0,
        fontFamily: '微软雅黑',
        fillColor: '#252525',
        strokeWidth: 1.5,
        strokeColor: '#fff',
        anchor: 'CENTER',
        fontSize: 13,
        depth: true,
        collision: true,
        text: data.name,
        x: +data.x,
        y: +data.y,
    });
    let floor = fMap.getFloor(+data.level);
    textMarker.addTo(floor)
};

// 根据车位类型设置对应的图标
function setPlaceModelTypeImg(data, bound) {
    let x = bound.center.x;
    let y = bound.center.y;
    let bx = (bound.min.x + (bound.min.x + bound.center.x) / 2) / 2;
    let by = (bound.min.y + (bound.min.y + bound.center.y) / 2) / 2;

    let url = '';
    if (data.type == 1) {
        /* 充电车位 */
        url = './image/placeType1.png'
    } else if (data.type == 2) {
        /* 专属车位 */
        url = './image/placeType2.png'
    } else if (data.type == 3) {
        /* 无障碍车位 */
        url = './image/placeType3.png'
    } else if (data.type == 4) {
        /* 超宽车位 */
        url = ''
    } else if (data.type == 5) {
        /* 子母车位 */
        url = ''
    } else if (data.type == 6) {
        /* 小型车位 */
        url = ''
    }

    if (url) {
        let imgdata = {
            x,
            y,
            url: url,
            size: 25,
            height: .2,
            collision: true
        };
        if (bound.size.x > bound.size.y) {
            imgdata.x = bx
        } else {
            imgdata.y = by
        };

        let imgMarker = new fengmap.FMImageMarker(imgdata);
        let floor = fMap.getFloor(+data.floor)
        imgMarker.addTo(floor);
    }
};

function SendMapSuccess() {
    nocoordtohttpsloca();
    getUserExclusive();
    getPlaceExith();
    getCompanyBusiness1();
    getCompanyBusiness2();
    getBookingListByTime();
    getMarkerPointwx();
    addFindCar();

    setTimeout(() => {
        // 地图初始化完成
        // socket.send(JSON.stringify({
        //     todo: "initMapSuccess",
        // }));

        mqttPushMessage({
            todo: "initMapSuccess",
        })
    }, 0);
};

function getMarkerPointwx() {
    $.ajax({
        url: `${formal}mapPathLabel/getMapPathLabel2?map=${mapInfo.mapId}`,
        success: function (res) {
            markerPoint = res.data;
        }
    })
};

function getBookingListByTime() {
    $.ajax({
        url: `${formal}wechat/getBookInf`,
        data: {
            pageSize: -1,
            mapId: mapInfo.mapId,
            place_type: 1,
            userId
        },
        success: function (res) {
            let data = res.data;
            if (!data || data.length == 0) {
                return false;
            }
            const list = data.map(item => {
                return {
                    id: item.id,
                    mapId: item.mapId,
                    fid: item.fid,
                    deviceId: item.floorLockId,
                    start: item.start,
                    end: item.end,
                    placeName: item.placeName,
                    vipType: 'place',
                }
            });

            list.sort((a, b) => {
                let toas = a.start.replace(/\.|\-/g, '/');
                let tobs = b.start.replace(/\.|\-/g, '/');
                let asT = new Date(toas).getTime();
                let bsT = new Date(tobs).getTime();
                return asT - bsT;
            });
            showBookingListByTimewx(list)
        }
    })
};

function showBookingListByTimewx(data) {
    let nowDate = new Date().getTime();
    let showData = [];
    data.forEach((item) => {
        let end = item.end.replace(/\.|\-/g, '/');
        let toend = new Date(end).getTime();
        if (nowDate < toend) {
            showData.push(item)
        }
    });
    if (showData.length) {
        let pushData = showData[0];
        $.ajax({
            url: `${formal}wechat/getPlaceDetail`,
            data: {
                fid: pushData.fid,
                map: pushData.mapId
            },
            success: function (res) {
                let resData = res.data;
                if (!resData || resData.length == 0) {
                    return false;
                };

                pushData['placeElevatorId'] = resData.elevatorId;
                // 预约数据
                showBookingListByTimeData = {
                    vipparam: pushData,
                    vipresdata: resData
                };
                showBookingListByTimeToMap();
            },
        })
    }
};

function getCompanyBusiness1() {
    $.ajax({
        url: `${formal}park/getCompany1`,
        data: {
            map: mapInfo.mapId,
        },
        success: function (res) {
            hadAllCompanyData = true;
            if (res.data && res.data.length) {
                let data = res.data.map((item) => {
                    return {
                        fid: item.fid,
                        name: item.name,
                        x: item.x,
                        y: item.y,
                        level: item.floor,
                        photolocal: '',
                        type_num: 8
                    }
                });
                onlyCompanyList = data;
                showBusinessList = showBusinessList.concat(data);
                addbusinesscompanytomap(data);
            }
        }
    })
};

var businessTypeList = [];
function getCompanyBusiness2() {
    $.ajax({
        url: `${formal}park/getShangjia1`,
        data: {
            map: mapInfo.mapId,
        },
        success: function (res) {
            hadAllBusinessData = true;
            if (res.data && res.data.length) {
                let data = res.data.map((item) => {
                    return {
                        fid: item.fid,
                        name: item.name,
                        x: item.x,
                        y: item.y,
                        level: item.floor,
                        photolocal: item.photolocal2,
                        type: item.type,
                        type_num: 3
                    }
                });
                onlyBusinessList = data;
                showBusinessList = showBusinessList.concat(data);

                try {
                    $.ajax({
                        url: `${formal}park/getShangjiaType?pageSize=-1`,
                        success: function (tres) {
                            if (tres && tres.data && tres.data.length) {
                                businessTypeList = tres.data.map((item) => {
                                    return {
                                        id: item.id,
                                        url: item.url
                                    }
                                })
                            };
                            addbusinesscompanytomap(data);
                        }
                    })
                } catch (error) {
                    addbusinesscompanytomap(data);
                }

            }
        }
    })
};

function getPlaceExith() {
    $.ajax(({
        url: `${formal}park/getPlaceExit2?map=${mapInfo.mapId}`,
        success: function (res) {
            let resData = res.data.list;
            allExit = [];
            showIconExitData = resData
            hadAllExitData = true
            resData.forEach(item => {
                if ((item.type > 0 && item.type != 3) && item.accessStatus != 0) {
                    // 快速出口的数据
                    allExit.push(item);
                }
            });
            if (MapLoadSuccess) {
                showExitIconToMap();
            }
        }
    }))
};

// 修改html - title
function setHTMLTitle(data) {
    $('title').html(data.mapName)
};


function noCurrentCoord() {
    showModal(tipsText, () => {
        $('#dialog').html('');
        openMapToWx();
    }, () => {
        $('#dialog').html('');
    }, '了解', '打开高德/百度')
};

function openMapToWx() {
    let data = {
        lat: mapInfo.lat,
        lng: mapInfo.lng,
        name: mapInfo.mapName,
        addressTxt: mapInfo.mapName
    };
    // socket.send(JSON.stringify({
    //     todo: "openMap",
    //     data: data
    // }));

    mqttPushMessage({
        todo: "openMap",
        data: data
    })
};

function toolTipBox(text) {
    showModal(text, () => {
        $('#dialog').html('');
    }, () => {
        $('#dialog').html('');
    })
}

function removeImageMarker() {
    if (!imageMarker) return;
    imageMarker.remove()
    imageMarker = null
};

var new_gpd_time = 0;
var old_gpd_time = 0;
// 获取某个车位情况-是否占用
function parkingSituation(type) {
    new_gpd_time = new Date().getTime();
    if (new_gpd_time - old_gpd_time >= 10000) {
        old_gpd_time = JSON.parse(JSON.stringify(new_gpd_time));
        if (!getPlaceIdle) return;

        $.ajax({
            url: `${formal}wechat/getPlaceDetail`,
            data: {
                map: mapInfo.mapId,
                fid: placeNavi.place_fid
            },
            success: function (res) {
                const target = res.data;
                if (!target) return;

                // 获取某个车位情况（是否占用）
                if (target.state == 1) {
                    // 占用
                    // 推荐下一个车位
                    nextParkingSpace(isNaviName);
                }
            }
        })
        // mqttPushMessage({
        //     todo: "getPlaceDetail",
        //     data: {
        //         map: mapInfo.mapId,
        //         fid: placeNavi.place_fid
        //     }
        // })
    }
};

// 车位占用，推荐一下个车位
function nextParkingSpace(type) {
    if (type == 'nearby') {
        // 就近停车
        nearby('nextP')
    } else if (type == 'ordinary') {
        // 找车位
        gotoPlace(0, 'show2');
    } else if (type == 'chargstation') {
        // 找充电桩
        gotoPlace(1, 'show1');
    } else if (type == 'vip') {
        // vip
        switchParking(vipData);
    } else if (type == 'accessibility') {
        // 无障碍
        gotoPlace(3, 'show1');
    }
};

// 切换下一个车位
async function switchparkingspace(data) {
    if (triggerVoice) {
        setTimeout(() => {
            let bbb = {
                todo: "navigatip",
                data: {
                    str: '车位已被占用，已为你导航新推荐车位',
                    path: -1
                },
                time: new Date().getTime(),
            };
            apiPostData2(bbb);

            // mqttPushMessage({
            //     todo: "navigatip",
            //     data: {
            //         str: '车位已被占用，已为你导航新推荐车位',
            //         path: -1
            //     }
            // })

        }, 100);
    } else {
        triggerVoice = true;
    }


    placeNavi.place_fid = data.fid;
    placeNavi.place_vip = data.vip || 0;
    placeNavi.place_name = data.name;
    placeNavi.carBitType = data.carBitType || 0;
    placeNavi.x = data.x || 0;
    placeNavi.y = data.y || 0;
    placeNavi.floor = data.floor || 0;
    placeNavi.lastTime = new Date();



    if (!navi) return;
    navi.clearAll();
    removeLineMarker();

    // 绑路
    var cons = naviAnalyser.pathConstraint({
        x: +data.x,
        y: +data.y,
        level: +data.floor,
        buildingID: null
    });

    let priority = fengmap.FMNaviPriority.PRIORITY_ESCALATORONLY;

    let proResult = await new Promise((resolve, reject) => {
        searchNearModel(currentCoord, (startCoord) => {
            naviAnalyser.route({
                start: {
                    x: startCoord.x,
                    y: startCoord.y,
                    level: startCoord.level
                },
                dest: {
                    x: cons ? +cons.coords.x : +data.x,
                    y: cons ? +cons.coords.y : +data.y,
                    level: cons ? +cons.coords.level : +data.floor
                },
                mode: fengmap.FMNaviMode.MODULE_BEST,
                priority
            }, (result) => {
                resolve({
                    res: "success",
                    data: {
                        result
                    }
                });
            }, (fail) => {
                resolve({
                    res: "fail"
                });
            });
        });
    });
    if (proResult.res == "success") {
        if (cons) {
            newdestination.x = +cons.coords.x;
            newdestination.y = +cons.coords.y;
            newdestination.level = +cons.coords.level;
        } else {
            newdestination.x = +data.x;
            newdestination.y = +data.y;
            newdestination.level = +data.floor;
        }

        newdestination.name = data.name;
        newdestination.fid = +data.fid;

        if (data.name.indexOf('层)') == -1) {
            newdestination.name = data.name + "(" + fMap.getFloor(+data.floor).name + "层)";
        };
        // 更新目的地
        if (finish && finish.x) {
            finish = newdestination
        } else {
            destination = newdestination
        }

        updateNaviUI();
        naviRoute(currentCoord, null, null, newdestination, null, null, true, true)
    } else {
        // triggerVoice = false;
        // switchparkingspace({
        //     fid: placeNavi.place_fid,
        //     vip: placeNavi.place_vip,
        //     name: placeNavi.place_name,
        //     carBitType: placeNavi.carBitType,
        //     x: placeNavi.x,
        //     y: placeNavi.y,
        //     floor: placeNavi.floor
        // })

        newdestination = {
            x: +data.x,
            y: +data.y,
            level: +data.floor,
            name: data.name,
            fid: +data.fid,
        };
        if (data.name.indexOf('层)') == -1) {
            newdestination.name = data.name + "(" + fMap.getFloor(+data.floor).name + "层)";
        };
        // 更新目的地
        if (finish && finish.x) {
            finish = newdestination
        } else {
            destination = newdestination
        }
    };

    // 添加图片标注
    /* 构造 Marker */
    if (imageMarker) {
        imageMarker.remove();
    }
    imageMarker = new fengmap.FMImageMarker({
        x: +data.x,
        y: +data.y,
        url: './image/FMImageMarker.png',
        size: 30,
        height: 2,
        collision: false
    });
    const floor2 = fMap.getFloor(+data.level || +data.floor)
    imageMarker.addTo(floor2);
};

// 充电桩车位已满，推荐普通车位
function recommandCommonPlace() {
    let bbb = {
        todo: "navigatip",
        data: {
            str: '充电车位已满，导航到其他车位',
            path: -1
        },
        time: new Date().getTime(),
    };
    apiPostData2(bbb);

    // mqttPushMessage({
    //     todo: "navigatip",
    //     data: {
    //         str: '充电车位已满，导航到其他车位',
    //         path: -1
    //     }
    // })
    showModal('充电车位已满，导航到其他车位', () => {
        $('#dialog').html('');
        triggerVoice = false;
        getPlaceIdle = true;
        if (naviStart) {
            gotoPlace(0, 'show2');
        } else {
            gotoPlace(0);
        }

    }, () => {
        $('#dialog').html('');
    })
};

// 无障碍车位已满，推荐普通车位
function recommandCommonPlace2() {
    let bbb = {
        todo: "navigatip",
        data: {
            str: '无障碍车位已满，导航到其他车位',
            path: -1
        },
        time: new Date().getTime(),
    };
    apiPostData2(bbb);

    // mqttPushMessage({
    //     todo: "navigatip",
    //     data: {
    //         str: '无障碍车位已满，导航到其他车位',
    //         path: -1
    //     }
    // })
    showModal('无障碍车位已满，导航到其他车位', () => {
        $('#dialog').html('');
        triggerVoice = false;
        getPlaceIdle = true;
        if (naviStart) {
            gotoPlace(0, 'show2');
        } else {
            gotoPlace(0);
        }

    }, () => {
        $('#dialog').html('');
    })
};

// 推荐其他vip车位
function switchParking(data, noVip = false) {
    let target = data;
    if (noVip) {
        target.isVip = '0'
    }

    $.ajax({
        url: `${formal}park/getWechatPlaceByCompanyName?userId=${userId}`,
        type: 'GET',
        data: target,
        success: function (res) {
            let resData = res.data;
            getWechatPlaceByCompanyName11(resData)
        }
    })
    // mqttPushMessage({
    //     todo: "api",
    //     data: {
    //         url: "park/getWechatPlaceByCompanyName",
    //         data: target
    //     }
    // })
};

// 无vip, 推荐普通车位
var noneToshow = false;
function noVipToPlainSpace() {
    placeNavi['place_vip'] = 0;
    placeNavi.place_vip = 0;
    // showModal('暂无空闲VIP车位,导航到其他车位', () => {
    // $('#dialog').html('');
    getPlaceIdle = true;
    isViPreservation = false;
    vipNaviTyep = 'none';
    if (naviStart) {
        switchParking(vipData, true);
        ifshownavibox = 'show1'
    } else {
        switchParking(vipData, true);
        ifshownavibox = 'none';
        noneToshow = true
    }

    // }, () => {
    //     $('#dialog').html('');
    // })
};

function isFirstNaviPoit() {
    if (isFirstNaviPoitNum == 1) {
        // 车位释放
        parkingSpaceRelease();

        if (employGPSLocation) {
            if (!jumpOther) {
                showModal(tipsText, () => {
                    $('#dialog').html('');
                    openMapToWx();
                }, () => {
                    $('#dialog').html('');
                }, '了解', '打开高德/百度');
            } else {
                jumpOther = false;
                showModal(tipsText, () => {
                    $('#dialog').html('');
                    openMapToWx();
                }, () => {
                    $('#dialog').html('');
                    showJumpOtherDialog();
                }, '了解', '打开高德/百度');
            }
        } else {
            $("#dialog").html('');
            $("#dialog").html(`
            <div class="dialog">
                <div class="dialog-mask animation_opac"></div>
                <div class="dialog-container animation_scale">
                    <div class="dialogBody dialogBody2" style="display: block;">
                        <div class="dialogHeader">
                            <div>路径规划失败提示建议</div>
                            <div>1、重新选择目的地</div>
                            <div>2、移动您的位置后重试</div>
                        </div>
                        <div class='leftText'></div>
                        <div class='leftText'></div>
                    </div>
                    <div class="dialogFooter">
                        <div class="comfirmBtn" onclick="hideDialog()">了解</div>   
                    </div>
                </div>
            </div>`);

            setTimeout(() => {
                $(".dialog-container").addClass("animation_show2");
                $(".dialog-mask").addClass("animation_show3");
            }, 30);
            if (navi) {
                navi.clearAll();
                removeLineMarker();
            }
            isFirstNaviPoitNum = 1;
            clearCirclM();
        }
        hideNavi();
    }
};

function showJumpOtherDialog() {
    if (mapInfo.shortLink) {
        mqttPushMessage({
            todo: "jumpToOther",
        })
    }
};

function hideDialog() {
    $(".dialog-container").removeClass("animation_show2");
    $(".dialog-mask").removeClass("animation_show3");
    setTimeout(() => {
        $("#dialog").html('');
        // showFooterShowType1(true);
        // showFooterShowType2(false);
        naviStart = false;
        queue_len = 6;
        noClick = false;
        isoutdoor = false;
        isNaviName = "";
        isViPreservation = false;
        noShowDriveList = false;
    }, 300);
};

// 显示商家名称
function addbusinesscompanytomap(data) {
    data.forEach((item) => {
        let model;
        if (item.fid) {
            model = getMapModel(item.fid);
            if (model) {
                model.setColor('#E4DDFF', 1);
                model.setBorderColor('#D7CFF1', 1);
            }
        }
        let url = '';
        if (item.photolocal) {
            url = formal + item.photolocal
        } else {
            if (item.type_num == 3) {
                // 蜂鸟api加载10087图片有跨域问题
                url = getBusinessTypeDefaultImg(item.type);
                // url = '';
            }
        }
        var floor = fMap.getFloor(+item.level);
        if (item.photolocal || (url && item.type_num == 3)) {
            // 有图片，使用复合marker
            var compositeOptions = {
                height: 0.2,      // 离地高度, 默认 1
                depth: false,   // 文本渲染深度 【true】：开启深度；【false】：关闭深度
                anchor: {
                    baseon: 'image',    // 锚点基于图片还是文字还是全部（all：全部，image：图片，text：文字）
                    anchor: 'CENTER',   // 锚点位置
                },
                collision: true,        // true:开启避让 false:关闭避让 默认 true，注：render为flat平铺时不支持避让
                render: 'billboard',    // 渲染方式，billboard始终面向屏幕，flat平铺
                layout: {
                    style: 'timage-btext',
                    align: 'center'
                },
                image: {
                    url, // 图片url地址
                    size: [88, 88],       // 图片大小，数组第一个元素是宽，第二个元素为高，默认为[32, 32]
                },
                text: {
                    padding: [0, 0, 0, 0],      // 文字内边距默认值为 [0,0,0,0]
                    plateStrokeWidth: 1,       // 文字背景边框宽度，注：只有描边颜色没有描边宽度时，默认描边宽度为 1
                    content: {
                        textAlign: 'Center',  // 文字对齐方式
                        lineSpacing: 2,                        // 行间距默认为2，当小于2时也等于2
                        fontSize: 13,                           // 文本字号, 默认20px
                        fillColor: '#252525',           // 文本填充色，rgb类型字符串，默认 "255,0,0"
                        strokeColor: '#fff',     // 文本边框填充色，rgb类型字符串。默认 '255,255,0'
                        strokeWidth: 1,  // 描边线宽
                        text: item.name,  // 文字内容，必填
                    }
                },
                x: +item.x,
                y: +item.y
            };
            // if (model) {
            //     compositeOptions.x = model.bound.center.x;
            //     compositeOptions.y = model.bound.center.y;
            // };
            var compositeMarker = new fengmap.FMCompositeMarker(compositeOptions);
            compositeMarker.addTo(floor);
        } else {
            // 没有图片，只加文字marker
            let textData = {
                height: 0,
                fontFamily: '微软雅黑',
                fillColor: '#252525',
                strokeWidth: 1,
                strokeColor: '#fff',
                anchor: 'CENTER',
                fontSize: 13,
                depth: true,
                collision: true,
                text: item.name,
                x: +item.x,
                y: +item.y,
                // y: +item.y - 0.5,
            };
            // if (model) {
            //     textData.x = model.bound.center.x;
            //     textData.y = model.bound.center.y;
            //     if (model.bound.size.x > model.bound.size.x) {
            //         textData.x -= 0.5
            //     } else {
            //         textData.y -= 0.5
            //     }
            // }
            let textMarker = new fengmap.FMTextMarker(textData);
            textMarker.addTo(floor);
        }
    });
};

function getBusinessTypeDefaultImg(type) {
    let url = '';
    let target = businessTypeList.find((item) => {
        return item.id == type
    });
    if (target) {
        url = target.url
    };
    return url
};

function contains(arr, val) {
    return arr.some(item => item === val);
};

// 保存位置信息
function sendPosition(data) {
    $.ajax({
        url: `${formal}collect/userPosition`,
        data: {
            ...data,
            userId
        },
    })

    // mqttPushMessage({
    //     todo: "sendPosition",
    //     data
    // })
};

var oldTime = 0;
// 保存位置信息
function sendPosition2(data, move) {
    if (move != 1) {
        let nowTime = new Date().getTime();
        if (oldTime && (nowTime - oldTime > 5000)) {
            return;
        } else if (oldTime === 0) {
            oldTime = nowTime;
        }
    } else {
        oldTime = 0;
    }

    mqttPushMessage({
        todo: "sendPosition2",
        data,
    })
}

// 推荐车位释放
function parkingSpaceRelease() {
    if (isRecommendParkingData && isRecommendParkingData.id) {
        let data = isRecommendParkingData.id;

        $.ajax({
            url: `${formal}park/delPlaceRecycleById?id=${data}`
        })
        // mqttPushMessage({
        //     todo: "parkingSpaceRelease",
        //     data
        // })
        isRecommendParkingData = {}
    }
};

// 楼层切换获取热力图数据
function changeLevelGetHeatData(level) {
    let nowDate = new Date().getTime();
    if (JSON.stringify(heatLeveData) == "{}") {
        getHeatData(level);
    } else {
        if (level in heatLeveData) {
            if (nowDate - heatLeveData[level].time >= 10 * 60 * 1000) {
                // 大于10分钟才更新数据
                getHeatData(level);
            } else {
                addHeatDataToMap(heatLeveData[level].data)
            }
        } else {
            getHeatData(level);
        }
    }
};

// 获取热力图数据
function getHeatData(level) {
    let start_time = new Date().getTime() - 60 * 60 * 1000; // 前一小时
    let end_time = new Date().getTime(); // 前一小时
    let map = mapInfo.mapId;
    let data = {
        map,
        level,
        start: start_time,
        end: end_time
    };
    // socket.send(JSON.stringify({
    //     todo: "getHeatData",
    //     data
    // }));
    mqttPushMessage({
        todo: "getHeatData",
        data
    })
};

// 添加热力图
function addHeatDataToMap(data) {
    var floor = fMap.getFloor(data[0].level);
    var heatMapData = data.map((item) => {
        return {
            x: item.x,
            y: item.y,
            value: item.value,
        }
    });
    var heatmap = {
        opacity: 0.5,
        radius: 30,
        valueRange: {
            max: 100,
            min: 0
        },
        quality: 1024,
        scaleRadius: true,
        isPlane: true,
        height: 0,
        gradient: {
            0.00: "rgb(0,0,0)",
            0.25: "rgb(0,0,255)",
            0.50: "rgb(0,255,0)",
            0.75: "rgb(255,255,0)",
            1.00: "rgb(255,0,0)"
        }
    };
    if (heatmapInstance) {
        heatmapInstance.remove();
        heatmapInstance = null
    }
    heatmapInstance = new fengmap.FMHeatMap(fMap, heatmap);
    heatmapInstance.addDataSource(heatMapData);
    heatmapInstance.addTo(floor);


};

// 用户的轨迹点
function addTracksToMap(data) {
    fMap.setCenter({
        x: +data[0].x,
        y: +data[0].y,
    })
    // 添加起终点marker
    var coords = data.map((item) => {
        return {
            x: item.x,
            y: item.y,
            level: item.level,
            url: 'https://developer.fengmap.com/fmAPI/images/blueImageMarker.png'
        }
    });
    for (var i = 0; i < coords.length; i++) {
        var coord = coords[i];
        var im = new fengmap.FMImageMarker({
            x: coord.x,
            y: coord.y,
            url: coord.url,
            size: 30,
            height: 0.2,
            anchor: fengmap.FMMarkerAnchor.BOTTOM,
            collision: true
        });
        var floor = fMap.getFloor(coord.level);
        im.addTo(floor);
    };
};

// 显示楼层空车位信息
function showEmptyPlaceHtml() {
    let floor = fMap.getLevel();
    let name = fMap.getFloor(floor).name;
    let passF1 = getFloorNamePassF1(name);
    if (passF1) {
        let id = document.getElementById('emptyPlace');
        if (id) {
            $(".emptybox").css('display', 'none')
        }
        return
    }
    let html = `
        <div class="emptybox" id="emptyPlace">
            <span>${name}：空车位<span class="emptynum">${emptyPlaceObj[floor] || 0}</span>个</span>
            <span class="emptycolor"></span>
            <span>：空车位</span>
        </div>
    `;
    $(".emptybox").css('display', 'flex')
    let id = document.getElementById('emptyPlace');
    if (id) {
        $("#emptyPlace").html(`
            <span>${name}：空车位<span class="emptynum">${emptyPlaceObj[floor] || 0}</span>个</span>
            <span class="emptycolor"></span>
            <span>：空车位</span>
        `)
    } else {
        $('body').append(html)
    }
};

// h5发起ajax请求车位状态数据
function htmlGetWxApiForPlaceType(first) {
    $.ajax({
        url: `${formal}wechat/getPlaceById?map=${mapInfo.mapId}`,
        success: function (res) {
            if (res.code != 200) {
                return;
            }
            const data = res.data;
            let list = data;
            let current = [];
            let dataNumObj = {};
            let list_len = list.length;
            for (let i = 0; i < list_len; i++) {
                let places = list[i].places;
                let places_len = places.length;
                for (let j = 0; j < places_len; j++) {
                    let target = places[j];
                    let currentPushData = {
                        fid: target.fid || '',
                        name: target.name,
                        type: target.type,
                        carbittype: target.carbittype || 0,
                        state: target.state,
                        x: target.x,
                        y: target.y,
                        floor: target.floor,
                        id: target.id
                    }
                    current.push(currentPushData);
                    if (target.state == 0) {
                        if (dataNumObj[target.floor]) {
                            dataNumObj[target.floor] += 1
                        } else {
                            dataNumObj[target.floor] = 1
                        }
                    }

                }
            };


            // 楼层车位数量显示
            if (JSON.stringify(dataNumObj) != '{}') {
                emptyPlaceObj = dataNumObj;
                showEmptyPlaceHtml();
            }

            // 车位状态显示
            if (first) {
                addPlaceColor(current, 'first');
            } else {
                // 有变化的数据
                const occupy = current.filter(c => !all_places.some(d => (d.id === c.id && d.state === c.state)));
                if (occupy.length) {
                    // 变化
                    addPlaceColor(occupy, '');
                }
            }
            all_places = current;
            hadAllPlaces = true;
        }
    })
};

// 地图出入口名字和图标
function showExitIconToMap() {
    let data = showIconExitData;
    if (data.length) {
        data.forEach((dt) => {

            let model = ''
            /* 设置model颜色 */
            if (dt.fid) {
                model = getMapModel(dt.fid);
                if (model) {
                    model.setColor('#CFEEDE', 1);
                    model.setBorderColor('#ACDAC2', 1);
                }
            };

            let url = '';
            if (dt.accessStatus == '0') {
                // 禁止
                if (dt.type != 3) {
                    url = './image/Exit1.png'
                } else {
                    url = './image/Exit2.png'
                }
            } else {
                // 通行
                if (dt.type != 3) {
                    url = './image/Exit1.png'
                } else {
                    url = './image/Exit2.png'
                }
            };

            if (url) {
                var compositeOptions = {
                    height: 0,      // 离地高度, 默认 1
                    depth: false,   // 文本渲染深度 【true】：开启深度；【false】：关闭深度
                    anchor: {
                        baseon: 'image',    // 锚点基于图片还是文字还是全部（all：全部，image：图片，text：文字）
                        anchor: 'CENTER',   // 锚点位置
                    },
                    collision: true,        // true:开启避让 false:关闭避让 默认 true，注：render为flat平铺时不支持避让
                    render: 'billboard',    // 渲染方式，billboard始终面向屏幕，flat平铺
                    layout: {
                        style: 'timage-btext',
                        align: 'center'
                    },
                    image: {
                        url, // 图片url地址
                        size: [74, 74],       // 图片大小，数组第一个元素是宽，第二个元素为高，默认为[32, 32]
                    },
                    text: {
                        padding: [0, 0, 0, 0],      // 文字内边距默认值为 [0,0,0,0]
                        plateStrokeWidth: 1,       // 文字背景边框宽度，注：只有描边颜色没有描边宽度时，默认描边宽度为 1
                        content: {
                            textAlign: 'Center',  // 文字对齐方式
                            lineSpacing: 2,                        // 行间距默认为2，当小于2时也等于2
                            fontSize: 13,                           // 文本字号, 默认20px
                            fillColor: '#252525',           // 文本填充色，rgb类型字符串，默认 "255,0,0"
                            strokeColor: '#fff',     // 文本边框填充色，rgb类型字符串。默认 '255,255,0'
                            strokeWidth: 1,  // 描边线宽
                            text: dt.name,  // 文字内容，必填
                        }
                    },
                    x: +dt.x,
                    y: +dt.y
                };

                // if (model) {
                //     compositeOptions.x = model.bound.center.x;
                //     compositeOptions.y = model.bound.center.y;
                // };

                var compositeMarker = new fengmap.FMCompositeMarker(compositeOptions);
                let f = (model && model.level) ? model.level : dt.floor;
                let floor = fMap.getFloor(+f);
                compositeMarker.addTo(floor);
            } else {
                let textData = {
                    height: 0,
                    fontFamily: '微软雅黑',
                    fillColor: '#252525',
                    strokeWidth: 1.5,
                    strokeColor: '#fff',
                    anchor: 'CENTER',
                    fontSize: 13,
                    depth: true,
                    collision: true,
                    text: dt.name,
                    x: +dt.x,
                    y: +dt.y,
                };

                // if (model) {
                //     textData.x = model.bound.center.x;
                //     textData.y = model.bound.center.y;
                // };

                let textMarker = new fengmap.FMTextMarker(textData);
                let f = (model && model.level) ? model.level : dt.floor;
                let floor = fMap.getFloor(+f);
                textMarker.addTo(floor)
            }
        })
    }
};

/* 从后台搜索车位数据 */
async function getPlaceData(keyword) {
    let sortRes = [];
    var proResult = await new Promise((resolve, reject) => {
        $.ajax({
            url: `${formal}wechat/getPlaceById`,
            data: {
                name: keyword,
                map: mapInfo.mapId,
            },
            type: 'GET',
            success: function (res) {
                var data = [];
                if (res && res.data && res.data.list) {
                    data = res.data.list
                } else if (res && res.data) {
                    data = res.data
                } else {
                    data = res;
                };
                if (!data || data.length == 0) {
                    resolve(sortRes);
                };

                var data2 = [];
                data.forEach((item) => {
                    data2.push(...(item.places))
                });

                sortRes = data2.map((item) => {
                    return {
                        id: item.id,
                        name: item.floor ? (item.name + "(" + fMap.getFloor(+item.floor).name + "层)") : item.name,
                        x: item.x,
                        y: item.y,
                        level: item.floor,
                        fid: item.fid,
                        typeID: 200401,
                        nodeType: item.type,
                        type_num: 0
                    }
                });
                resolve(sortRes)
            }
        });
    });
    let res = sortRes;
    return res;
};

// /* 出入口数据 */
async function getExitData(keyword) {
    let sortRes = [];
    var proResult = await new Promise((resolve, reject) => {
        $.ajax({
            url: `${formal}es/search`,
            // url: `${formal}park/getPlaceExit2`,
            data: {
                keyword,
                map: mapInfo.mapId,
                indexName: 'parking_exit'
            },
            type: 'GET',
            // type: 'POST',
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            success: function (res) {
                var data = [];
                if (res && res.data && res.data.list) {
                    data = res.data.list
                } else if (res && res.data) {
                    data = res.data
                } else {
                    data = res;
                };
                if (!data || data.length == 0) {
                    resolve(sortRes);
                }
                sortRes = data.map((item) => {
                    let name = item.floor ? (item.name + "(" + fMap.getFloor(+item.floor).name + "层)") : item.name;
                    return {
                        id: item.id,
                        name,
                        x: item.x,
                        y: item.y,
                        level: item.floor,
                        fid: item.fid,
                        typeID: '',
                        nodeType: item.type,
                        type_num: 1
                    }
                });

                resolve(sortRes)
            }
        })
    });
    let res = sortRes;
    return res;
};

/* 关键字查询电梯 */
async function getAdminElevatorData(keyword) {
    let sortRes = [];
    var proResult = await new Promise((resolve, reject) => {
        $.ajax({
            url: `${formal}es/search`,
            data: {
                keyword,
                map: mapInfo.mapId,
                indexName: 'parking_elevator_binding'
            },
            type: 'GET',
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            success: function (res) {
                var data = [];
                if (res && res.data && res.data.list) {
                    data = res.data.list
                } else if (res && res.data) {
                    data = res.data
                } else {
                    data = res;
                };
                if (!data || data.length == 0) {
                    resolve(sortRes);
                };
                sortRes = data.map((item) => {
                    let hasF = fMap.getFloor(+item.floor);
                    let name;
                    if (hasF) {
                        name = item.floor ? (item.name + "(" + fMap.getFloor(+item.floor).name + "层)") : item.name;
                    } else {
                        name = item.name
                    }
                    return {
                        id: item.id,
                        name,
                        x: item.x,
                        y: item.y,
                        level: item.floor,
                        fid: item.fid,
                        typeID: '',
                        nodeType: item.type,
                        type_num: 6 // 电梯
                    }
                });

                resolve(sortRes)
            }
        })
    });
    let res = sortRes;
    return res;
};


/* 关键字查询厕所 */
async function getAdminServiceFacilitiesData(keyword) {
    let sortRes = [];
    var proResult = await new Promise((resolve, reject) => {
        $.ajax({
            url: `${formal}es/search`,
            data: {
                keyword,
                map: mapInfo.mapId,
                indexName: 'map_wc'
            },
            type: 'GET',
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            success: function (res) {
                var data = [];
                if (res && res.data && res.data.list) {
                    data = res.data.list
                } else if (res && res.data) {
                    data = res.data
                } else {
                    data = res;
                };
                if (!data || data.length == 0) {
                    resolve(sortRes);
                }
                sortRes = data.map((item) => {
                    let name = item.floor ? (item.name + "(" + fMap.getFloor(+item.floor).name + "层)") : item.name;
                    return {
                        id: item.id,
                        name,
                        x: item.x,
                        y: item.y,
                        level: item.floor,
                        fid: item.fid,
                        typeID: '',
                        nodeType: item.type,
                        type_num: 7 // 厕所-安全出口-室外物体
                    }
                });

                resolve(sortRes)
            }
        })
    });
    let res = sortRes;
    return res;
};

/* 关键字查询安全出口-室外物体 */
async function getAdminServiceFacilitiesData2(keyword) {
    let sortRes = [];
    var proResult = await new Promise((resolve, reject) => {
        $.ajax({
            url: `${formal}es/search`,
            data: {
                keyword,
                map: mapInfo.mapId,
                indexName: 'map_build'
            },
            type: 'GET',
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            success: function (res) {
                var data = [];
                if (res && res.data && res.data.list) {
                    data = res.data.list
                } else if (res && res.data) {
                    data = res.data
                } else {
                    data = res;
                };
                if (!data || data.length == 0) {
                    resolve(sortRes);
                }
                sortRes = data.map((item) => {
                    let name = item.floor ? (item.name + "(" + fMap.getFloor(+item.floor).name + "层)") : item.name;
                    return {
                        id: item.id,
                        name,
                        x: item.x,
                        y: item.y,
                        level: item.floor,
                        fid: item.fid,
                        typeID: '',
                        nodeType: item.type,
                        type_num: 7 // 厕所-安全出口-室外物体
                    }
                });

                resolve(sortRes)
            }
        })
    });
    let res = sortRes;
    return res;
};

/* 商家 */
async function changeApigetTargetByName1(data) {
    let sortRes = [];
    var proResult = await new Promise((resolve, reject) => {
        $.ajax({
            url: `${formal}es/search`,
            data: {
                keyword: data.keyword,
                map: mapInfo.mapId,
                indexName: 'shangjia'
            },
            type: 'GET',
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            success: function (res) {
                var data = [];
                if (res && res.data && res.data.list) {
                    data = res.data.list
                } else if (res && res.data) {
                    data = res.data
                } else {
                    data = res;
                };
                if (!data || data.length == 0) {
                    resolve(sortRes);
                };

                data.forEach((item) => {
                    sortRes.push({
                        ...item,
                        types: '3'
                    })
                })

                resolve(sortRes)
            }
        })
    });
    let res = sortRes;
    return res;
};

/* 公司 */
async function changeApigetTargetByName2(data) {
    let sortRes = [];
    var proResult = await new Promise((resolve, reject) => {
        $.ajax({
            url: `${formal}es/search`,
            data: {
                keyword: data.keyword,
                map: mapInfo.mapId,
                indexName: 'parking_company'
            },
            type: 'GET',
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            success: function (res) {
                var data = [];
                if (res && res.data && res.data.list) {
                    data = res.data.list
                } else if (res && res.data) {
                    data = res.data
                } else {
                    data = res;
                };
                if (!data || data.length == 0) {
                    resolve(sortRes);
                };

                data.forEach((item) => {
                    sortRes.push({
                        ...item,
                        types: '8'
                    })
                })

                resolve(sortRes)
            }
        })
    });
    let res = sortRes;
    return res;
};

/* 查询model */
function getMapModel(fid) {
    var levels = fMap.getLevels();
    for (let i = 0; i < levels.length; i++) {
        var floor = fMap.getFloor(levels[i]);
        var layers = floor.getLayers(fengmap.FMType.MODEL_LAYER)[0];
        var model = layers.getFeatures().find(item => item.FID === fid);
        if (model) {
            return model
        }
    };
    return null;
};

/* 图层控制 */
function layerControl() {
    fMap.getLevels().forEach(level => {
        let floor = fMap.getFloor(level);
        /* 去除原地图上的文字 */
        let layer = getLayerByType(floor, fengmap.FMType.LABEL_LAYER);

        /* 去除地图上的车位图标 */
        let layer2 = getLayerByType2(floor, fengmap.FMType.FACILITY_LAYER);

        layer.visible = !layer.visible;
        layer2.visible = !layer2.visible;
    })
};
function getLayerByType(floor, layerType) {
    var layers = floor.getLayers();
    for (var index = 0; index < layers.length; index++) {
        const layer = layers[index];
        if (layer.type === layerType) {
            return layer;
        }
    }
}
function getLayerByType2(floor, layerType) {
    var layers = floor.getLayers();
    for (var index = 0; index < layers.length; index++) {
        const layer = layers[index];
        if (layer.type === layerType) {
            // if (layer.children.length) {
            //     layer.children.forEach((item) => {
            //         if (item.typeID == '800004' || item.typeID == '800005' || item.typeID == '800006' || item.typeID == '800013' || item.typeID == '110001') {
            //             item.visible = false
            //         }
            //     })
            // }
            return layer
        }
    }
}

/* 跨层通道 */
function crossLayerChannel() {
    $.ajax({
        url: `${formal}crossLevelCorridor/getConditionalQuery2?map=${mapInfo.mapId}`,
        success: function (res) {
            const data = res.data;
            if (!data || data.length == 0) {
                return;
            }
            crossLayerData = data;
            setCrossLayerChannel(data);
        }
    })
};
function setCrossLayerChannel(data) {
    data.forEach((dt) => {
        let model = ''
        /* 设置model颜色 */
        if (dt.fid) {
            model = getMapModel(dt.fid);
            if (model) {
                model.setColor('#E3F1FF', 1);
                model.setBorderColor('#597CBE', 1);
            }
        };

        let floor = fMap.getFloor(+dt.floor);

        // 有图片，使用复合marker
        var compositeOptions = {
            height: 0.2,      // 离地高度, 默认 1
            depth: false,   // 文本渲染深度 【true】：开启深度；【false】：关闭深度
            anchor: {
                baseon: 'image',    // 锚点基于图片还是文字还是全部（all：全部，image：图片，text：文字）
                anchor: 'CENTER',   // 锚点位置
            },
            collision: true,        // true:开启避让 false:关闭避让 默认 true，注：render为flat平铺时不支持避让
            render: 'billboard',    // 渲染方式，billboard始终面向屏幕，flat平铺
            layout: {
                style: 'timage-btext',
                align: 'center'
            },
            image: {
                url: './image/Exit3.png', // 图片url地址
                size: [74, 74],       // 图片大小，数组第一个元素是宽，第二个元素为高，默认为[32, 32]
            },
            text: {
                padding: [0, 0, 0, 0],      // 文字内边距默认值为 [0,0,0,0]
                plateStrokeWidth: 1,       // 文字背景边框宽度，注：只有描边颜色没有描边宽度时，默认描边宽度为 1
                content: {
                    textAlign: 'Center',  // 文字对齐方式
                    lineSpacing: 2,                        // 行间距默认为2，当小于2时也等于2
                    fontSize: 13,                           // 文本字号, 默认20px
                    fillColor: '#252525',           // 文本填充色，rgb类型字符串，默认 "255,0,0"
                    strokeColor: '#fff',     // 文本边框填充色，rgb类型字符串。默认 '255,255,0'
                    strokeWidth: 1,  // 描边线宽
                    text: dt.name,  // 文字内容，必填
                }
            },
            x: +dt.x,
            y: +dt.y
        };
        if (model) {
            compositeOptions.x = model.bound.center.x;
            compositeOptions.y = model.bound.center.y;
        };
        var compositeMarker = new fengmap.FMCompositeMarker(compositeOptions);
        compositeMarker.addTo(floor);
    })
};

// 显示预约数据到地图上
function showBookingListByTimeToMap() {
    let topW = $("#emptyPlace").width() + 20;

    let param = showBookingListByTimeData.vipparam;
    let html = `
        <div class="booking" id="Booking" onclick="clickBooking()" style="${topW ? 'width:' + topW + 'px' : ''}">
            <div class="bookContent" id="bookContent">
                <span>您的预约车位：${param.placeName}</span>
                <span>开始时间：${param.start}</span>
                <span>结束时间：${param.end}</span>
                <span>您的预约车位：${param.placeName}</span>
                <span>开始时间：${param.start}</span>
                <span>结束时间：${param.end}</span>
            </div>
        </div>
    `;

    let id = document.getElementById('Booking');
    if (id) {
        $("#Booking").html(`
            <div class="bookContent" id="bookContent">
                <span>您的预约车位：${param.placeName}</span>
                <span>开始时间：${param.start}</span>
                <span>结束时间：${param.end}</span>
                <span>您的预约车位：${param.placeName}</span>
                <span>开始时间：${param.start}</span>
                <span>结束时间：${param.end}</span>
            </div>
        `)
    } else {
        $('body').append(html)
    };

    // 滚动动画
    let w = $(".bookContent").width();
    let sw = w / 2;
    let l = 0;
    if (bookContentanimation) {
        clearInterval(bookContentanimation);
        bookContentanimation = null;
    };
    bookContentanimation = setInterval(() => {
        l--;
        $(".bookContent").css('left', l + 'px');
        if (l <= -sw) {
            l = 0;
        }
    }, 32);

    // 设置css动画
    // document.styleSheets[0].insertRule(
    //     `@keyframes example {
    //         0% {
    //             left: 0;
    //         }

    //         100% {
    //             left: ${-sw}px;
    //         }
    //      }`,
    //     0
    // );
    // document.getElementById("bookContent").style.animation = "example 16s linear infinite"
};

function clickBooking() {
    let resData = showBookingListByTimeData.vipresdata;
    let param = showBookingListByTimeData.vipparam;
    showVipTextMessage(resData, param)
};

// 短信进入
function showVipTextMessage(resData, param) {
    noshowFixedView = true;
    isViPreservation = true;

    const {
        mapId,
        fid,
        deviceId, //地锁mac地址
        vipType,
        start,
        end,
        placeName,
        placeElevatorId
    } = param;
    placeNavi.place_state = true;
    placeNavi.lastTime = new Date();
    const target = resData;
    const {
        state,
    } = target;

    vipData = {
        map: mapInfo.mapId,
        carBitType: null,
        isVip: '1',
        x: +resData.x,
        y: +resData.y,
        floor: +resData.floor,
        placeName: placeName,
        companyName: resData.companyName
    }

    if (placeElevatorId || resData.elevatorId) {
        // 有绑定电梯-查询电梯数据
        $.ajax({
            url: `${formal}WxMiniApp/getParkingElevatorBindingById/${placeElevatorId || resData.elevatorId}`,
            success: function (res) {
                if (!(res.data && res.data.length)) return;
                ishasElevatorData = true;
                let target = res.data[0]
                hasElevatorData = {
                    x: target.x,
                    y: target.y,
                    level: target.floor,
                    fid: target.fmapID,
                    name: target.name
                };
            }
        })
        // mqttPushMessage({
        //     todo: "getplaceElevatorData",
        //     data: placeElevatorId || resData.elevatorId
        // })
    }

    if (vipType == 'place') {
        if (state != 1) {
            clickDropTap = true;

            showP = {
                // fid: data.vipresdata.data.fid,
                // name: data.vipresdata.data.name,
                // x: +data.vipresdata.data.x,
                // y: +data.vipresdata.data.y,
                // level: +data.vipresdata.data.floor
                fid: resData.fid,
                name: resData.name,
                x: +resData.x,
                y: +resData.y,
                level: +resData.floor
            };
            if (deviceId && deviceId != 'null') {
                ble_ned.mac = deviceId;
                // socket.send(JSON.stringify({
                //     todo: "initBle",
                //     data: {
                //         ble_ned
                //     }
                // }));
                mqttPushMessage({
                    todo: "initBle",
                    data: {
                        ble_ned
                    }
                })
                initNed(ble_ned);
                placeNavi.place_name = placeName;
                placeNavi.place_fid = fid;
                placeNavi.place_vip = 1;
                placeNavi.destination_nearby = true;
                isNaviName = 'vip';
                vipNaviTyep = 'vip';
                pointAndNaviFid(target.fid, true, null, null, 'vip_place');
                showFooterShowType1(false);
            } else {
                // 没有地锁
                placeNavi.place_name = placeName;
                placeNavi.place_fid = fid;
                placeNavi.place_vip = 1;
                placeNavi.destination_nearby = true;
                isNaviName = 'vip'; // 没有地锁的-占用逻辑
                vipNaviTyep = 'vip';
                pointAndNaviFid(target.fid, true, null, null, 'vip_place');
                showFooterShowType1(false);
            }
        } else {
            showFooterShowType1(true);
            placeNavi.place_name = placeName;
            placeNavi.place_fid = fid;
            placeNavi.place_vip = 1;
            placeNavi.destination_nearby = true;
            // 预约的车位是不空闲
            showModal('您预约的车位已被占用,导航到其他车位', () => {
                $('#dialog').html('');
                triggerVoice = false;
                isNaviName = 'vip'; // 没有地锁的-占用逻辑
                vipNaviTyep = 'none';
                switchParking(vipData);
            }, () => {
                $('#dialog').html('');
                isViPreservation = false;
            })
        }
    }
};



/* 电梯数据 */
function getApiElevatorData() {
    $.ajax({
        url: `${formal}peb/getParkingElevatorBinding?map=${mapInfo.mapId}&pageSize=-1`,
        success: function (res) {
            if (res.code != 200) {
                return;
            }
            const data = res.data;
            elevatorData = data;
            hadAllElevatorData = true;
            setElevatorDataToMap(elevatorData);
        }
    })
};
function setElevatorDataToMap(data) {
    data.forEach((item) => {
        let model = '';
        let mData = {
            x: item.x,
            y: item.y,
            level: item.floor,
            name: item.name,
            objectType: item.objectType,
            iconType: item.iconType || ''
        }
        if (item.fid) {
            model = getMapModel(item.fid);
            if (model) {
                /* 设置模型颜色 */
                if (item.objectType == '200005') {
                    /* 步行梯 */
                    model.setColor('#daeed9', 1);
                    model.setBorderColor('#BCD4BD', 1);
                } else if (item.objectType == '340818') {
                    /* 手扶电梯 */
                    model.setColor('#D9F2FC', 1);
                    model.setBorderColor('#AAD2EB', 1);
                } else if (item.objectType == '340855') {
                    /* 电梯前室 */
                    model.setColor('#D3DEFE', 1);
                    model.setBorderColor('#ACBCFE', 1);
                } else if (item.objectType == '200004') {
                    /* 直升电梯 */
                    model.setColor('#8EEBFF', 1);
                    model.setBorderColor('#38A9C7', 1);
                };

                mData.x = model.bound.center.x;
                mData.y = model.bound.center.y;
                mData.level = model.level;
            }
        };
        elevatorDataDraw(mData)
    })
};
function elevatorDataDraw(data) {
    if (!data.level) {
        return false;
    };
    /* 设置图标 */
    let url = '';
    if (data.iconType == '170001') {
        /* 步行梯 */
        url = './image/Exit5.png'
    } else if (data.iconType == '170003') {
        /* 手扶电梯 */
        url = './image/Exit7.png'
    } else if (data.iconType == '170006') {
        /* 电梯前室 */
        url = './image/Exit6.png'
    };
    if (url) {
        var compositeOptions = {
            height: 0,      // 离地高度, 默认 1
            depth: false,   // 文本渲染深度 【true】：开启深度；【false】：关闭深度
            anchor: {
                baseon: 'image',    // 锚点基于图片还是文字还是全部（all：全部，image：图片，text：文字）
                anchor: 'CENTER',   // 锚点位置
            },
            collision: true,        // true:开启避让 false:关闭避让 默认 true，注：render为flat平铺时不支持避让
            render: 'billboard',    // 渲染方式，billboard始终面向屏幕，flat平铺
            layout: {
                style: 'timage-btext',
                align: 'center'
            },
            image: {
                url, // 图片url地址
                size: [74, 74],       // 图片大小，数组第一个元素是宽，第二个元素为高，默认为[32, 32]
            },
            text: {
                padding: [0, 0, 0, 0],      // 文字内边距默认值为 [0,0,0,0]
                plateStrokeWidth: 1,       // 文字背景边框宽度，注：只有描边颜色没有描边宽度时，默认描边宽度为 1
                content: {
                    textAlign: 'Center',  // 文字对齐方式
                    lineSpacing: 2,                        // 行间距默认为2，当小于2时也等于2
                    fontSize: 13,                           // 文本字号, 默认20px
                    fillColor: '#252525',           // 文本填充色，rgb类型字符串，默认 "255,0,0"
                    strokeColor: '#fff',     // 文本边框填充色，rgb类型字符串。默认 '255,255,0'
                    strokeWidth: 1,  // 描边线宽
                    text: data.name,  // 文字内容，必填
                }
            },
            x: +data.x,
            y: +data.y
        };

        var compositeMarker = new fengmap.FMCompositeMarker(compositeOptions);
        let floor = fMap.getFloor(+data.level);
        compositeMarker.addTo(floor);
    } else {
        let textMarker = new fengmap.FMTextMarker({
            height: 0,
            fontFamily: '微软雅黑',
            fillColor: '#252525',
            strokeWidth: 1.5,
            strokeColor: '#fff',
            anchor: 'CENTER',
            fontSize: 13,
            depth: true,
            collision: true,
            text: data.name,
            x: +data.x,
            y: +data.y,
        });
        let floor = fMap.getFloor(+data.level);
        textMarker.addTo(floor)
    }
};

/* 厕所-安全出口-建筑 */
function getApiServiceFacilitiesData() {
    $.ajax({
        url: `${formal}mapBuild/getMapBuild2?map=${mapInfo.mapId}&pageSize=-1`,
        success: function (res) {
            if (res.code != 200) {
                return;
            };
            const data = res.data;
            serviceFacilitiesData = data;
            hadAllServiceData = true;
            setServiceFacilitiesData(serviceFacilitiesData);
        }
    })
};

function setServiceFacilitiesData(data) {
    data.forEach((item) => {
        let model = '';
        let mData = {
            x: item.x,
            y: item.y,
            level: item.floor,
            name: item.name,
            objectType: item.objectType,
            iconType: item.iconType || ''
        }
        if (item.fid) {
            model = getMapModel(item.fid);
            if (model) {
                /* 设置模型颜色 */
                if (item.objectType == '340873') {
                    /* 一类建筑 */
                    // model.setColor('#8EABF0', 1);
                    // model.setBorderColor('#486FCF', 1);
                } else if (item.objectType == '340874') {
                    /* 二类建筑 */
                    // model.setColor('#BDDBFE', 1);
                    // model.setBorderColor('#597CBE', 1);
                } else if (item.objectType == '340875') {
                    /* 三类建筑 */
                    // model.setColor('#D4F6FD', 1);
                    // model.setBorderColor('#597CBE', 1);
                } else if (item.objectType == '100004') {
                    /* 男洗手间 */
                    model.setColor('#dcf6ff', 1);
                    model.setBorderColor('#9eb5bf', 1);
                } else if (item.objectType == '100005') {
                    /* 女洗手间 */
                    model.setColor('#FADADE', 1);
                    model.setBorderColor('#FADADE', 1);
                } else if (item.objectType == '200010') {
                    /* 安全出口 */
                    model.setColor('#daeed9', 1);
                    model.setBorderColor('#BCD4BD', 1);
                } else if (item.objectType == '200110') {
                    /* 外部道路 */
                    model.setColor('#FFFFFF', 1);
                    model.setBorderColor('#FFFFFF', 1);
                }

                // mData.x = model.bound.center.x;
                // mData.y = model.bound.center.y;
                mData.level = model.level;
            }
        };
        serviceFacilitiesDataDraw(mData)
    });
};
function serviceFacilitiesDataDraw(data) {
    if (!data.level) {
        return false;
    };
    /* 设置图标 */
    let url = '';

    if (data.iconType == '100004') {
        /* 男洗手间 */
        url = './image/icon_wc_1.png'
    } else if (data.iconType == '100005') {
        /* 女洗手间 */
        url = './image/icon_wc_2.png'
    } else if (data.iconType == '100003') {
        /* 母婴室 */
        url = './image/icon_wc_4.png'
    } else if (data.iconType == '800008') {
        /* 护士站 */
        url = './image/icon_wc_5.png'
    } else if (data.iconType == '110002') {
        /* 安全出口 */
        url = './image/Exit4.png'
    };

    if (url) {
        var compositeOptions = {
            height: 0,      // 离地高度, 默认 1
            depth: false,   // 文本渲染深度 【true】：开启深度；【false】：关闭深度
            anchor: {
                baseon: 'image',    // 锚点基于图片还是文字还是全部（all：全部，image：图片，text：文字）
                anchor: 'CENTER',   // 锚点位置
            },
            collision: true,        // true:开启避让 false:关闭避让 默认 true，注：render为flat平铺时不支持避让
            render: 'billboard',    // 渲染方式，billboard始终面向屏幕，flat平铺
            layout: {
                style: 'timage-btext',
                align: 'center'
            },
            image: {
                url, // 图片url地址
                size: [74, 74],       // 图片大小，数组第一个元素是宽，第二个元素为高，默认为[32, 32]
            },
            text: {
                padding: [0, 0, 0, 0],      // 文字内边距默认值为 [0,0,0,0]
                plateStrokeWidth: 1,       // 文字背景边框宽度，注：只有描边颜色没有描边宽度时，默认描边宽度为 1
                content: {
                    textAlign: 'Center',  // 文字对齐方式
                    lineSpacing: 2,                        // 行间距默认为2，当小于2时也等于2
                    fontSize: 13,                           // 文本字号, 默认20px
                    fillColor: '#252525',           // 文本填充色，rgb类型字符串，默认 "255,0,0"
                    strokeColor: '#fff',     // 文本边框填充色，rgb类型字符串。默认 '255,255,0'
                    strokeWidth: 1,  // 描边线宽
                    text: data.name,  // 文字内容，必填
                }
            },
            x: +data.x,
            y: +data.y
        };

        var compositeMarker = new fengmap.FMCompositeMarker(compositeOptions);
        let floor = fMap.getFloor(+data.level);
        compositeMarker.addTo(floor);
    } else {
        let fontSize = 13;
        let fillColor = '#252525';
        let strokeColor = '#fff';
        let collision = true;

        if (data.objectType == '340873') {
            fontSize = 15;
            fillColor = '#a200ff';
            strokeColor = '#fff';
            collision = false;
        };

        let textMarker = new fengmap.FMTextMarker({
            height: 0,
            fontFamily: '微软雅黑',
            fillColor: fillColor,
            strokeWidth: 1.5,
            strokeColor: strokeColor,
            anchor: 'CENTER',
            fontSize: fontSize,
            depth: true,
            collision: collision,
            text: data.name,
            x: +data.x,
            y: +data.y,
        });
        let floor = fMap.getFloor(+data.level);
        textMarker.addTo(floor)
    }
};

/* 上传点击搜索数据到后台 */
function clickDataToBackstage(data) {
    $.ajax({
        url: `${formal}emsbp/addEachMapSearchBusinessPlace`,
        data: JSON.stringify(data),
        type: 'POST',
        headers: {
            "Content-Type": "application/json"
        },
        success: function (res) { }
    })
};

// 驾车导航-统计导航次数
function countNavigationTimes() {
    if (destination && destination.type_num == '0') {
        // 车位导航
        try {
            let name = destination.name.split("(")[0] || destination.name;
            var data = {
                map: mapInfo.mapId, //地图id
                mapName: mapInfo.mapName,//地图名
                placeName: name,
                type: "1", // 驾车导航-统计导航次数
            };
            $.ajax({
                url: `${formal}emsbp/addUserActiveSelectPlace`,
                data: JSON.stringify(data),
                type: 'POST',
                headers: {
                    "Content-Type": "application/json"
                },
                success: function (res) { }
            })
        } catch (error) {

        }
    }
};

// 电梯导航
function countNavigationTimes1() {
    if (destination && destination.type_num == '6') {
        // 电梯导航
        try {
            let name = destination.name.split("(")[0] || destination.name;
            var data = {
                map: mapInfo.mapId,
                mapName: mapInfo.mapName,
                name: name,
                type: "3",
            };
            $.ajax({
                url: `${formal}emsbp/addUserActiveSelectPlace`,
                data: JSON.stringify(data),
                type: 'POST',
                headers: {
                    "Content-Type": "application/json"
                },
                success: function (res) { }
            })
        } catch (error) {

        }
    }
};

// 推广导航记录
function promotionNavigationRecords() {
    QRCodeCategory = '';
    try {
        var data = {
            map: mapInfo.mapId,
            mapName: mapInfo.mapName,
            userId: userId,
            destId: QRCodeDataObj.destId,
            shangJiaName: QRCodeDataObj.name,
            type: "2", // 推广商家导航-记录导航信息
        };

        $.ajax({
            url: `${formal}emsbp/addUserActiveSelectPlace`,
            data: JSON.stringify(data),
            type: 'POST',
            headers: {
                "Content-Type": "application/json"
            },
            success: function (res) { }
        })
    } catch (error) {

    }
};
var reconnectT = null;
var onesend = true;
var aaT = null;
// mqtt连接
function initMqttConnent() {
    userId = getUrlStr('userId');
    let topicT = 'wx' + userId;

    // var host = 'wss://tuguiyao-gd.com:10086/mqtt';
    // var host = 'ws://tuguiyao-gd.com:18080/mqtt';
    // var host = 'ws://192.168.1.16:8083/mqtt';
    // var host = 'ws://192.168.1.95:8083/mqtt';
    // var host = 'wss://9nhv6n.natappfree.cc/mqtt';
    var host = 'wss://tuguiyao-gd.com/mqtt';


    const clientId = 'h5_' + userId
    var options = {
        clientId: clientId,
        connectTimeout: 2000,
        reconnectPeriod: 4000, // 设置为 0 禁用自动重连，两次重新连接之间的间隔时间
        keepalive: 5,
        // protocolVersion: 4, //MQTT连接协议版本
    };
    mqttClient = mqtt.connect(host, options);

    mqttClient.on('error', (err) => {

    })

    mqttClient.on('reconnect', () => {

    })

    mqttClient.on("offline", () => {
        let ed = mqttClient.connected

        if (ed) {
            mqttClient.end(true, {}, function () {
                if (!aaT) {
                    aaT = setTimeout(() => {
                        initMqttConnent();
                        clearTimeout(aaT)
                        aaT = null;
                    }, 200);
                }
            });
        }
    });

    mqttClient.on('connect', () => {
        if (onesend) {
            onesend = false;
            mqttPushMessage({
                todo: "connect",
                data: {
                    connect: true,
                    userId: userId
                }
            })
        }

        mqttClient.subscribe(topicT, { qos: 0 }, function (err) {
            if (!err) {
                console.log('mqtt-订阅成功');
            }
        });
    })

    // Received
    mqttClient.on('message', (topic, message) => {
        if (!message.toString()) return;
        receiptMQTT(topic, message.toString())
    })
};
/* 接收 */
function receiptMQTT(topic, message) {
    handleMessage(message)
};

/* 发送 */
function mqttPushMessage(data) {
    socket.send(JSON.stringify(data));

    return
    userId = getUrlStr('userId');
    let topicT = 'web' + userId;

    // Publish
    mqttClient.publish(topicT, JSON.stringify(data), { qos: 0 }, function (err) {

    })
};

/* 接收到的数据处理 */
function handleMessage(msg) {
    var data = JSON.parse(msg);

    if (data.todo == "initMap") {

        // mqttPushMessage({
        //     todo: "api",
        //     data: {
        //         url: "park/getPlaceExit",
        //         data: {
        //             map: mapInfo.mapId
        //         }
        //     }
        // })
    } else if (data.todo == "addlocationMarker") {
        employGPSLocation = true;
        let res = determineWhetherItIsMapData(data.coord);
        if (!res) return;

        let div2 = `<div><div>mapInfo222</div><pre >${JSON.stringify(data, null, 2)}</pre></div>`;
        $("#error_conent").prepend(div2)

        addLocationMarker(data.coord);
        changePosition(data.coord, null, 'gps', 0);
    } else if (data.todo == "setLocationMarkerPosition") {
        let res = determineWhetherItIsMapData(data.coord);
        if (!res) return;

        if (data.coord) {
            nocoordtohttpsloca();

            let maxX = maxY = minX = minY = 0;
            if (initMapBound && initMapBound.max) {
                maxX = initMapBound.max.x;
                maxY = initMapBound.max.y;
                minX = initMapBound.min.x;
                minY = initMapBound.min.y;
            };
            if (data.type == 'gps') {

                // 使用gps定位
                if ((data.coord.x < minX || data.coord.x > maxX) || (data.coord.y < minY || data.coord.y > maxY)) {
                    // gps定位的点在地图边界外
                    employGPSLocation = true;
                    tipsText = '尚未到达定位信号覆盖区域，请稍后尝试'
                } else {
                    employGPSLocation = false;
                    tipsText = '当前区域内手机信号弱，请稍后再试'
                }
            } else {
                employGPSLocation = false;
                tipsText = '当前区域内手机信号弱，请稍后再试'
            }
        };

        if (data.angle) {
            nodataangle = false;

            if (angleTimea1) {
                clearTimeout(angleTimea1);
                angleTimea1 = null;
            }
            if (!angleTimea1) {
                angleTimea1 = setTimeout(() => {
                    nodataangle = true;
                    clearTimeout(angleTimea1);
                    angleTimea1 = null;
                }, 1000);
            }
            phoneRealAngle = data.angle
        }

        addLocationMarker(data.coord);
        changePosition(data.coord, data.angle, data.type, data.move);
    } else if (data.todo == "closeConnect") {
        // socket.close({
        //     code: 1000,
        //     reason: '1000'
        // });
        // socket = null;
    } else if (data.todo == "getApiResult") {
        let result = data.result;
        if (result.url === "park/getWechatPlaceByCompanyName") {
            let resData = result.res.data;
            if (!resData) {
                if (placeNavi.carBitType == 1) {
                    // 找充电桩，找不到后推荐普通车位逻辑
                    getPlaceIdle = false;
                    recommandCommonPlace();
                } else if (placeNavi.place_vip == 1) {
                    // 无vip,推荐普通车位
                    getPlaceIdle = false;
                    noVipToPlainSpace();
                } else if (placeNavi.carBitType == 3) {
                    // 无-无障碍车位-推荐普通车位
                    getPlaceIdle = false;
                    recommandCommonPlace2();
                } else {
                    // 没有车位提示
                    getPlaceIdle = false;

                    let bbb = {
                        todo: "navigatip",
                        data: {
                            str: '暂无空车位，请稍后再试',
                            path: -1
                        },
                        time: new Date().getTime(),
                    };
                    apiPostData2(bbb);

                    // mqttPushMessage({
                    //     todo: "navigatip",
                    //     data: {
                    //         str: '暂无空车位，请稍后再试',
                    //         path: -1
                    //     }
                    // })

                    if (!naviStart) {
                        toolTipBox('暂无空车位，请稍后再试');
                    } else {
                        showModal("暂无空车位，请稍后再试", () => {
                            $('#dialog').html('');
                            selfStopNavi();//退出导航
                        }, () => {
                            $('#dialog').html('');
                        }, '继续导航', '退出导航')
                    }
                }
                return false;
            };
            isRecommendParkingData = resData[0];
            const target = resData[0];
            requestPara['place'] = target;
            requestPara['time'] = new Date();
            placeNavi.place_fid = target.fid;
            place_recommand.push(requestPara);
            target.vip = 0;
            target.carBitType = placeNavi.carBitType;
            if (isnearby) {
                // 就近停车
                if (ifshownavibox == 'none') {
                    recommandPlaceNavi(destination, target, true);
                } else {
                    // 切换下一个车位
                    // triggerVoice = true;
                    switchparkingspace(target)
                }
            } else if (isViPreservation) {
                target.vip = 1;
                placeNavi.place_vip = 1;
                placeNavi.place_name = target.name;
                placeNavi.carBitType = target.carBitType || 0;
                placeNavi.lastTime = new Date();
                // initFixedView(false);
                if (vipNaviTyep == 'none' && !naviStart) {
                    // vip车位
                    pointAndNaviFid(target.fid, true, null, null, 'vip_place');
                    showFooterShowType1(false);
                } else if (vipNaviTyep == 'vip' || naviStart) {
                    // 切换下一个车位
                    // triggerVoice = true;
                    switchparkingspace(target)
                }
            } else {
                // 找车位
                if (ifshownavibox == 'none') {
                    recommandPlaceNavi(destination, target, false);
                } else if (ifshownavibox == 'show2' || ifshownavibox == 'show1') {
                    // 切换下一个车位
                    // triggerVoice = true;
                    switchparkingspace(target)
                }
            }
        } else if (result.url == "hot/getHotData") {
            let resData = result.res.data;
            const list = resData.map(item => {
                let type = '';
                if (item.type == 0) {
                    type = 'place'
                } else if (item.type == 1) {
                    type = 'fmap'
                } else if (item.type == 2) {
                    type = 'outdoor'
                } else if (item.type == 3) {
                    type = 'company'
                }
                const x = +item.x;
                const y = +item.y;
                return {
                    id: item.id,
                    desc: item.desc,
                    addressTxt: item.desc,
                    fid: item.fid,
                    icon: item.icon,
                    name: item.name,
                    databaseId: item.databaseId,
                    ename: item.ename || '',
                    outdoorType: item.outdoorType,
                    sendName: item.type == 3 ? true : false,
                    type,
                    type_num: item.type,
                    x,
                    y,
                    groupID: item.floor,
                    //室外
                    lng: x,
                    lat: y,
                }
            })
            searchHistory = list;

            /* 处理出入口删除-改名后-删除历史记录 */
            searchHistory.forEach((item, index) => {
                if (item.type_num == 0) {
                    /* 车位 */
                    if (hadAllPlaces) {
                        if (!all_places.length) {
                            deleteDataIds.push(item.id)
                        } else {
                            let hasD = all_places.some((sitem) => sitem.fid == item.fid && (sitem.name.includes(item.name) || item.name.includes(sitem.name)));
                            if (!hasD) {
                                deleteDataIds.push(item.id)
                            }
                        }
                    };
                }

                if (item.type_num == 1) {
                    /* 出入口 */
                    if (hadAllExitData) {
                        if (!showIconExitData.length) {
                            deleteDataIds.push(item.id)
                        } else {
                            let hasD = showIconExitData.some((sitem) => sitem.fid == item.fid && (sitem.name.includes(item.name) || item.name.includes(sitem.name)));
                            if (!hasD) {
                                deleteDataIds.push(item.id)
                            }
                        }
                    };
                };

                if (item.type_num == 3) {
                    /* 商家 */
                    if (hadAllBusinessData) {
                        if (!onlyBusinessList.length) {
                            deleteDataIds.push(item.id)
                        } else {
                            let hasD = onlyBusinessList.some((sitem) => sitem.fid == item.fid && (sitem.name.includes(item.name) || item.name.includes(sitem.name)));
                            if (!hasD) {
                                deleteDataIds.push(item.id)
                            }
                        }
                    }
                };

                if (item.type_num == 6) {
                    /* 电梯 */
                    if (hadAllElevatorData) {
                        if (!elevatorData.length) {
                            deleteDataIds.push(item.id)
                        } else {
                            let hasD = elevatorData.some((sitem) => sitem.fid == item.fid && (sitem.name.includes(item.name) || item.name.includes(sitem.name)));
                            if (!hasD) {
                                deleteDataIds.push(item.id)
                            }
                        }
                    };
                };

                if (item.type_num == 7) {
                    /* 厕所-安全出口-室外物体 */
                    if (hadAllServiceData) {
                        if (!serviceFacilitiesData.length) {
                            deleteDataIds.push(item.id)
                        } else {
                            let hasD = serviceFacilitiesData.some((sitem) => sitem.fid == item.fid && (sitem.name.includes(item.name) || item.name.includes(sitem.name)));
                            if (!hasD) {
                                deleteDataIds.push(item.id)
                            }
                        }
                    };
                };

                if (item.type_num == 8) {
                    /* 公司 */
                    if (hadAllCompanyData) {
                        if (!onlyCompanyList.length) {
                            deleteDataIds.push(item.id)
                        } else {
                            let hasD = onlyCompanyList.some((sitem) => sitem.fid == item.fid && (sitem.name.includes(item.name) || item.name.includes(sitem.name)));
                            if (!hasD) {
                                deleteDataIds.push(item.id)
                            }
                        }
                    }
                }
            });
            /* 处理出入口删除-改名后-删除历史记录 */

            /* 删除历史记录 */
            if (deleteDataIds.length) {
                searchHistory = searchHistory.filter((fitem) => {
                    return !deleteDataIds.some((sitem) => {
                        return fitem.id == sitem
                    })
                });

                let ids = deleteDataIds.join(',')
                entDeleHistory2(ids)
                deleteDataIds = [];
            };
            /* 删除历史记录 */
            showSearchPop(true, searchHistory);

            // 多地图页输入内容
            if (moreMapValue) {
                document.getElementById('searchInput').value = moreMapValue;
                searchInput();
            };

        } else if (result.url == "wechat/getTargetByName") {
            return // 改到h5
            let resultTemp = [];
            let param = result.param;
            keyword_name = result.param.keyword;
            let resData = result.res.data;

            resData.forEach(item => {
                if (!item.fid) return;
                let cal_dis;
                if (currentCoord && currentCoord.x) {
                    let pos1 = {
                        x: currentCoord.x,
                        y: currentCoord.y,
                    };
                    let pos2 = {
                        x: +item.x,
                        y: +item.y,
                    };
                    cal_dis = fengmap.FMCalculator.distance(pos1, pos2);
                } else {
                    cal_dis = null
                }
                resultTemp.push({
                    type: 'fmap',
                    id: item.id,
                    name: item.name,
                    sendName: true,
                    nameList: resNameList(item.name, param.keyword),
                    fid: item.fid,
                    x: +item.x,
                    y: +item.y,
                    databaseId: item.id,
                    groupID: +item.floor,
                    desc: '类型：室内地点',
                    icon: 'search-map-icon',
                    type_num: item.types,
                    dis: cal_dis,
                })
            });
            resultTemp.sort((a, b) => {
                return a.dis - b.dis
            });

            newresultTemp = JSON.parse(JSON.stringify(resultTemp));

            let mapPlaceSize = 0;
            fMapResult.forEach(item => {
                const typeID = item.typeID;
                const ename = resEname(item.ename, typeID);
                let icon = '';
                let type = '';
                let type_num = '';
                let is_place = contains(place_type, typeID);
                if (is_place) {
                    icon = 'search-place-icon';
                    type = 'place';
                    // type_num = '0';
                } else {
                    icon = 'search-map-icon';
                    type = 'fmap';
                    // type_num = '1';
                }
                mapPlaceSize++;
                if (mapPlaceSize < 10) {
                    resultTemp.push({
                        type,
                        id: item.id,
                        name: item.name,
                        ename,
                        nameList: resNameList(item.name, param.keyword),
                        typeID,
                        fid: item.fid,
                        desc: '类型：室内地点',
                        icon,
                        x: item.x,
                        y: item.y,
                        groupID: item.groupID || item.level,
                        type_num: item.type_num,
                        databaseId: item.id,
                        dis: item.cal_dis || item.dis
                    })
                }
            });

            // fMapResult.unshift(...newresultTemp)
            // fMapResult = newresultTemp.concat(fMapResult);


            $.ajax({
                url: "https://restapi.amap.com/v3/assistant/inputtips",
                data: {
                    key: '11b902de74442737fdd98e2af342e0ce',
                    keywords: param.keyword,
                    location: mapInfo.lng && mapInfo.lat ? mapInfo.lng + ',' + mapInfo.lat : "",
                    datatype: 'all',
                },
                success: function (res) {
                    outdoorList = [];
                    const { status, tips } = res;
                    if (status == '1') {
                        tips.forEach(item => {
                            const {
                                id,
                                name,
                                cityname,
                                adname,
                                address,
                                type = '',
                                location,
                                district
                            } = item;
                            let addressTxt = '';
                            if (district) {
                                addressTxt += district;
                            }
                            if (typeof address == 'string') {
                                addressTxt += '-' + address;
                            };
                            if (typeof location == 'string') {
                                const locationArr = location.split(',');
                                outdoorList.push({
                                    id,
                                    name,
                                    nameList: resNameList(name, param.keyword),
                                    outdoorType: type.split(';')[2] || '',
                                    addressTxt,
                                    lng: +locationArr[0],
                                    lat: +locationArr[1],
                                    type: 'outdoor',
                                    desc: addressTxt,
                                    icon: 'search-outdoor-icon',
                                    type_num: '2',
                                    databaseId: '-1',
                                })
                            }
                        });

                        searchResult = resultTemp.concat(outdoorList);
                        is_show_more = true;
                        updateSearchPop(searchResult);
                    }
                }
            });
        } else if (result.url == "hot/delHotData") {
            getSearchPop();
        } else if (result.url == "park/getPlaceExit") {
            let resData = result.res.data;
            allExit = [];
            showIconExitData = resData
            hadAllExitData = true
            resData.forEach(item => {
                if ((item.type > 0 && item.type != 3) && item.accessStatus != 0) {
                    // 快速出口的数据
                    allExit.push(item);
                }
            });
            if (MapLoadSuccess) {
                showExitIconToMap();
            }
        } else if (result.url == "wechat/getPlaceDetail") {
            let resData = result.res.data;
            let param = result.param;
            const {
                mapId,
                fid,
                deviceId, //地锁mac地址
                vipType,
                start,
                end,
                placeName,
            } = param;
            placeNavi.place_state = true;
            placeNavi.lastTime = new Date();
            const target = resData;
            const {
                state,
            } = target;
            if (vipType == 'place' && isDuringTimes(start, end)) {
                if (state != 1) {
                    if (deviceId && deviceId != 'null') {
                        ble_ned.mac = deviceId;
                        // socket.send(JSON.stringify({
                        //     todo: "initBle",
                        //     data: {
                        //         ble_ned
                        //     }
                        // }));
                        mqttPushMessage({
                            todo: "initBle",
                            data: {
                                ble_ned
                            }
                        })
                        initNed(ble_ned);
                        placeNavi.place_name = placeName;
                        placeNavi.place_fid = fid;
                        placeNavi.place_vip = 1;
                        placeNavi.destination_nearby = true;
                        pointAndNaviFid(target.fid, true, null, null, 'vip_place');
                        showFooterShowType1(false);
                    }
                }
            }
        } else if (result.url == "wechat/getStorePlace") {
            c_list = data.result.res.data;
            showCollect(data.result.res.data);
        } else if (result.url == "wechat/delStorePlace") {
            pointAndNaviFid(showP.fid, true, showP.name);
        } else if (result.url == "wechat/addStorePlace") {
            pointAndNaviFid(showP.fid, true, showP.name);
        } else if (result.url == "mapHotspotData/getHotSearchByMap") {
            // 热门
            hotPList = result.res.data;
            showHotView(result.res.data)
        }
    } else if (data.todo == "initNed") {
        if (data.ble_ned) {
            ble_ned = data.ble_ned;
            initNed(ble_ned)
        }
    } else if (data.todo == "initNedDeviceId") {
        let deviceId = data.deviceId;
        ble_ned.deviceId = deviceId;
    } else if (data.todo == 'license') {
        console.log('license', data.result);

        // 车牌找车
        isLicense = true;
        jumpOther = true;
        noshowFixedView = true;
        clickDropTap = true;
        /* 构造 Marker */
        if (imageMarker) {
            imageMarker.remove();
        }

        if (data.result.length == 1) {
            let resData = data.result[0];
            if (resData.areaData) {
                if (resData.placeName) {
                    /* 视频数据-单车位 */
                    showP = {
                        x: resData.x,
                        y: resData.y,
                        level: resData.floor,
                        name: resData.placeName,
                    }
                    pointAndNaviFid(resData.fid, true, resData.placeName, null, 'license', true)
                } else {
                    /* 摄像头-区域 */
                    morePlaceData = data.result;
                    morePlaceType = 'area'
                    showMorePlaceHtml()
                }
            } else {
                /* 红绿灯-单车位 */
                showP = {
                    x: resData.x,
                    y: resData.y,
                    level: resData.floor,
                    name: resData.placeName,
                }
                pointAndNaviFid(resData.fid, true, resData.placeName, null, 'license', true)
            }
        } else {
            /* 视频数据-多车位 */
            morePlaceData = data.result;
            morePlaceType = 'more'
            showMorePlaceHtml()
        }
    } else if (data.todo == "Tobusiness") {
        // 商家服务
        clickDropTap = true;
        pointAndNaviFid(data.result.fid, true, data.result.name, null, null, false, '3')
    } else if (data.todo == "collectBack") {
        clickDropTap = true;

        collectBack(data);
    } else if (data.todo == "deleteCollect") {
        allcheckbox = false

        mqttPushMessage({
            todo: "api",
            data: {
                url: "wechat/getStorePlace",
                data: {
                    map: mapInfo.mapId
                }
            }
        })
        // nowinputtype = 2;
    } else if (data.todo == "showcollect") {
        c_list = data.res.data;
    } else if (data.todo == "Pdelecollect") {
        pointAndNaviFid(showP.fid, true, showP.name);
    } else if (data.todo == "Paddcollcet") {
        c_list.unshift(data.result.data)
        pointAndNaviFid(showP.fid, true, showP.name);
    } else if (data.todo == "findCar") {
        // 反向寻车
        jumpOther = true;
        resultfindCar(data)
    } else if (data.todo == "showSwiperBox") {
        // 指引页面
        showViewGuide();
    } else if (data.todo == "voiceend") {
        // 语音功能回调
        voicesuccess(data)
    } else if (data.todo == 'voiceonstart') {
        voiceonstart();
    } else if (data.todo == "onKeyboardHeightChange") {
        // 键盘弹出
        setingBottom(data);
    } else if (data.todo == "addPlaceColortoh") {
        // 空闲车位颜色
        // addPlaceColor(data);
        colorList = data.data;
        if (colorList.length) {
            MapParkSpaceState = true;
            if (fMap) {
                addPlaceColor(colorList, data.color)
            } else {
                colorTime = setInterval(() => {
                    addPlaceColor(colorList, data.color);
                    clearInterval(colorTime);
                    colorTime = null;
                }, 200);
            }
        }
    } else if (data.todo == "addPlaceColortohTwo") {
        addPlaceColor(data.data, data.color);
    } else if (data.todo == "addPlaceColortohs") {
        // 车位状态-分组传
        if (data.data.length) {
            colorList.push(...data.data);
            if (data.success) {
                MapParkSpaceState = true;
                if (fMap) {
                    addPlaceColor(colorList, data.color)
                } else {
                    colorTime = setInterval(() => {
                        addPlaceColor(colorList, data.color);
                        clearInterval(colorTime);
                        colorTime = null;
                    }, 2000);
                }
            }
        }
    } else if (data.todo == "showLicenceFindCar") {
        // 是否显示车牌找车按钮
        if (data.mapValue) {
            // 多地图页输入内容
            moreMapValue = data.mapValue;
        };
        if (data.shareInfo && (data.shareInfo.groupID || data.shareInfo.name)) {
            return false;
        } else if (data.vipresdata && data.vipresdata.data) {
            // vipvip
            let resData = data.vipresdata.data;
            let param = data.vipparam;
            showVipTextMessage(resData, param);
        }
    } else if (data.todo == "showShareInfo") {
        if (data.shareInfo.type == 'promotion') {
            // 推广
            noshowFixedView = true;
            clickDropTap = true;
            if (data.shareInfo && (data.shareInfo.floor || data.shareInfo.x)) {
                showP = {
                    fid: data.shareInfo.fid,
                    name: data.shareInfo.name,
                    x: +data.shareInfo.x,
                    y: +data.shareInfo.y,
                    level: +data.shareInfo.floor
                };
                // 推广
                QRCodeCategory = data.shareInfo.type;
                QRCodeDataObj = data.shareInfo;
                // 商家
                pointAndNaviFid(data.shareInfo.fid, true, data.shareInfo.name, null, null, false, '3');
                showFooterShowType1(false);
            }
        } else {
            // 分享
            noshowFixedView = true;
            clickDropTap = true;

            if (data.shareInfo && (data.shareInfo.groupID || data.shareInfo.name)) {
                showP = {
                    fid: data.shareInfo.fid,
                    name: data.shareInfo.name,
                    x: +data.shareInfo.x,
                    y: +data.shareInfo.y,
                    level: +data.shareInfo.groupID
                };

                pointAndNaviFid(data.shareInfo.fid, true, data.shareInfo.name, null, null, false, data.shareInfo.type);
                showFooterShowType1(false);
            }
        }


    } else if (data.todo == "showDestination") {
        showDestination(data.shareInfo);
    } else if (data.todo == "getMarkerPoint") {
        markerPoint = data.data;
    } else if (data.todo == "heart") { } else if (data.todo == "returnGetPlaceDetail") {
        // 获取某个车位情况（是否占用）
        if (data.data.state == 1) {
            // 占用
            // 推荐下一个车位
            nextParkingSpace(isNaviName);
        }
    } else if (data.todo == "returnElevatorData") {
        // 车位绑电梯，电梯的数据
        ishasElevatorData = true;
        let target = data.data.data[0]
        hasElevatorData = {
            x: target.x,
            y: target.y,
            level: target.floor,
            fid: target.fmapID,
            name: target.name
        };
    } else if (data.todo == "showCompanyBusiness2") {
        hadAllCompanyData = true;
        if (data.companyList && data.companyList.length) {
            onlyCompanyList.push(...data.companyList)
        };
        if (data.success) {
            showBusinessList = showBusinessList.concat(onlyCompanyList);
            if (fMap) {
                addbusinesscompanytomap(onlyCompanyList);
            }
        }
    } else if (data.todo == 'showCompanyBusiness1') {
        hadAllBusinessData = true;
        if (data.businessList && data.businessList.length) {
            onlyBusinessList.push(...data.businessList)
        };
        if (data.success) {
            showBusinessList = showBusinessList.concat(onlyBusinessList);
            if (fMap) {
                addbusinesscompanytomap(onlyBusinessList);
            }
        }

    } else if (data.todo == "returnHeatData") {
        if (data.data.length) {
            heatLeveData[data.data[0].level] = {
                data: data.data,
                time: new Date().getTime()
            }
            if (fMap) {
                addHeatDataToMap(data.data); // 热力图
            } else {
                heatsetinterval = setInterval(() => {
                    if (fMap) {
                        addHeatDataToMap(data.data); // 热力图
                        clearInterval(heatsetinterval);
                        heatsetinterval = null;
                    }
                }, 2000);
            }
        } else {
            if (heatmapInstance) {
                heatmapInstance.remove();
                heatmapInstance = null
            }
        }
    } else if (data.todo == "returnHeatDatas") {
        // 热力图-数据过大，分组传
        if (data.data.length) {
            heatDataLists.push(...data.data);
            if (data.success) {
                // 数据传完了
                heatLeveData[data.data[0].level] = {
                    data: heatDataLists,
                    time: new Date().getTime()
                }
                if (fMap) {
                    addHeatDataToMap(heatDataLists); // 热力图
                } else {
                    heatsetinterval = setInterval(() => {
                        if (fMap) {
                            addHeatDataToMap(heatDataLists); // 热力图
                            clearInterval(heatsetinterval);
                            heatsetinterval = null;
                        }
                    }, 2000);
                }
            }
        }
    } else if (data.todo == "returnTracksData") {
        // 轨迹
        if (data.data.length) {
            if (fMap) {
                addTracksToMap(data.data); // 轨迹图
            } else {
                trackssetinterval = setInterval(() => {
                    if (fMap) {
                        addTracksToMap(data.data); // 轨迹图
                        clearInterval(trackssetinterval);
                        trackssetinterval = null;
                    }
                }, 2000);
            }
        }
    } else if (data.todo == "returnTracksDatas") {
        if (data.data.length) {
            tracksDataList.push(...data.data);
            if (data.success) {
                if (fMap) {
                    addTracksToMap(tracksDataList); // 轨迹图
                } else {
                    trackssetinterval = setInterval(() => {
                        if (fMap) {
                            addTracksToMap(tracksDataList); // 轨迹图
                            clearInterval(trackssetinterval);
                            trackssetinterval = null;
                        }
                    }, 2000);
                }
            }
        }
    } else if (data.todo == "threeWayHandshake") {
        clearInterval(threeWayHandshakeTime);
        threeWayHandshakeTime = null;
        // 重连-连接上后，让小程序发送没发完的数据
        whetherBreakSocket = false; // sokcet重连成功
    } else if (data.todo == "showEmptyPlaceNum") {
        // 显示楼层的空车位数据
        if (JSON.stringify(data.data) != '{}') {
            emptyPlaceObj = data.data;
            showEmptyPlaceHtml();
        }
    } else if (data.todo == "showBookingListByTime") {
        // 预约数据
        showBookingListByTimeData = {
            vipparam: data.vipparam,
            vipresdata: data.vipresdata
        };
        showBookingListByTimeToMap();
    }
};

function getUserExclusive() {
    let userId = getUrlStr('userId');
    $.ajax({
        url: `${formal}lockDevice/isExclusiveUser?mapId=${mapInfo.mapId}&userId=${userId}`,
        success: function (res) {
            if (!res.data) return;
            let mapId = res.data.mapId;
            let companyId = res.data.companyId;

            $("#locklist").html(`<div class="operate-item" onclick="openLockList(${mapId}, ${companyId})">
            <img class="operate-exitIcon" src="${imagePath}lockh101.png" alt="">
            <span class="operate-iconText">开锁</span>
        </div>`);
        }
    })
};

/* 跳转小程序地锁列表 */
function openLockList(mapId, companyId) {
    wx.miniProgram.navigateTo({
        url: `/pages/lockList/lockList?mapId=${mapId}&companyId=${companyId}`
    })
};

let nocoordTimes = null;
let httpgetlocaTime = null;
function nocoordtohttpsloca() {
    if (nocoordTimes) {
        clearTimeout(nocoordTimes);
    };

    if (httpgetlocaTime) {
        clearInterval(httpgetlocaTime);
    };

    nocoordTimes = setTimeout(() => {
        openhttpgetloca();
    }, 1500);
};

function openhttpgetloca() {
    getLocationData();

    httpgetlocaTime = setInterval(() => {
        getLocationData();
    }, 400);
};

/* http方式获取定位数据 */
function getLocationData() {
    let userId = getUrlStr('userId');

    $.ajax({
        url: `${formal}mapHotspotData/getData?userId=` + userId,
        success: function (res) {
            if (!res.data) return;
            let data = JSON.parse(res.data);

            if (data.map != mapInfo.mapId) {
                return
            };

            let resType = determineWhetherItIsMapData(data.coord);
            if (!resType) return;

            if (data.coord) {
                let maxX = maxY = minX = minY = 0;
                if (initMapBound && initMapBound.max) {
                    maxX = initMapBound.max.x;
                    maxY = initMapBound.max.y;
                    minX = initMapBound.min.x;
                    minY = initMapBound.min.y;
                };
                if (data.type == 'gps') {
                    // 使用gps定位
                    if ((data.coord.x < minX || data.coord.x > maxX) || (data.coord.y < minY || data.coord.y > maxY)) {
                        // gps定位的点在地图边界外
                        employGPSLocation = true;
                        tipsText = '尚未到达定位信号覆盖区域，请稍后尝试'
                    } else {
                        employGPSLocation = false;
                        tipsText = '当前区域内手机信号弱，请稍后再试'
                    }
                } else {
                    employGPSLocation = false;
                    tipsText = '当前区域内手机信号弱，请稍后再试'
                }
            };

            if (data.angle) {
                phoneRealAngle = data.angle
            }

            addLocationMarker(data.coord);
            changePosition(data.coord, data.angle, data.type, data.move);
        }
    })
};

function determineWhetherItIsMapData(data) {
    if (data && data.x) {
        if (!fMap) {
            return false;
        };
        if (!MapLoadSuccess) {
            return false;
        };

        let levels = fMap.getLevels() || [1];
        let nowLevel = data.groupID;
        let had = levels.some((item) => {
            return item == nowLevel
        });
        if (!had) {
            return false;
        };
        return true;
    } else {
        return true;
    }
};

var polygon;
function addAreaDataToMap(data) {
    initFixedView(false);

    if (polygon) {
        polygon.remove();
    };

    /* 添加任意多边形 */
    var polygonOption = {
        color: "#009944",
        borderWidth: 2,
        borderColor: "#009944",
        opacity: 0.3
    };

    polygonOption.points = data;

    var floor = fMap.getFloor(+data[0].floor);
    polygon = new fengmap.FMPolygonMarker(polygonOption);
    polygon.addTo(floor);

    /* 显示这该区域 */
    //设置视图
    fMap.setLevel({
        level: +data[0].floor
    });
    fMap.setCenter({
        x: +data[0].x,
        y: +data[0].y,
    });
    fMap.setZoom({
        zoom: 20
    });
};

function showMorePlaceHtml() {
    noClick = true;

    let data = morePlaceData[0];
    let adata = JSON.parse(data.areaData);
    addAreaDataToMap(adata);
    let showAData = adata[0];
    let showADataName = data.areaName;
    let showLevelName = fMap.getFloor(+showAData.floor).name;

    let html = `
        <div class="morePlaceViewMain">
            <div class=""morePlaceViewTopMain>
                <div class="morePlaceViewTopTitle" id="morePlaceViewTopTitle">您的车停在${showADataName}(${showLevelName}层)</div>
            </div>`;
    if (morePlaceType === 'more') {
        html += `
            <div class="morePlaceViewCenterMain" id="morePlaceViewCenterMain">
                <div class="morePlaceViewTip">以下为可能停靠的车位</div>
                <div class="morePlaveViewList">
            `;
        morePlaceData.forEach((item) => {
            html += `
                    <div class="morePlaceItem" onclick="clickMorePlaceItem(this, '${item.placeName}')">${item.placeName}</div>
                `;
        })
        html += `</div></div>`;
    };
    html += `<div class="morePlaceViewBottomMain" id="morePlaceViewBottomMain">
                <div class="morePlaceViewBMBtn1" onclick="removeMorePlace()">退出</div>
                <div class="morePlaceViewBMBtn2" onclick="goToMorePlace('1')">导航到区域</div>
            </div>
        </div>
    `;

    showFooterShowType1(false);

    $("#morePlaceView").html(html);
};

function clickMorePlaceItem(that, pName) {
    $(".morePlaceItem.morePlaceItemActive").removeClass("morePlaceItemActive");
    $(that).addClass('morePlaceItemActive')
    console.log(pName);
    let target = morePlaceData.find((item) => {
        return item.placeName == pName
    });
    console.log('target', target);
    destination['FID'] = target.fid || '';
    destination['level'] = +target.floor;
    destination['x'] = +target.x;
    destination['y'] = +target.y;
    destination['name'] = target.placeName + "(" + fMap.getFloor(+target.floor).name + ")";


    /* 构造 Marker */
    if (imageMarker) {
        imageMarker.remove();
    }
    imageMarker = new fengmap.FMImageMarker({
        x: +target.x,
        y: +target.y,
        url: './image/FMImageMarker.png',
        size: 30,
        height: 2,
        collision: false
    });
    const floor = fMap.getFloor(+target.floor)
    imageMarker.addTo(floor);

    /* 显示这该区域 */
    //设置视图
    fMap.setLevel({
        level: +target.floor
    });
    fMap.setCenter({
        x: +target.x,
        y: +target.y,
    });
    fMap.setZoom({
        zoom: 20
    });

    $("#morePlaceViewTopTitle").html(`位置：${target.placeName}`);

    let html = `<div class="morePlaceViewBMBtn1" onclick="backMorePlace()">返回</div>
                <div class="morePlaceViewBMBtn2" onclick="goToMorePlace('2')">立即导航</div>`

    $("#morePlaceViewBottomMain").html(html)
};

function backMorePlace() {
    showMorePlaceHtml();
};

function removeMorePlace() {
    $("#morePlaceView").html('');
    noClick = false;
    morePlaceType = '';

    if (polygon) {
        polygon.remove();
    };
    showFooterShowType1(true);
};

function goToMorePlace(type) {
    naviType = 'walk'
    if (type == '1') {
        let data = morePlaceData[0];
        let adata = JSON.parse(data.areaData);

        destination['FID'] = '';
        destination['level'] = +adata[0].floor;
        destination['x'] = +adata[0].x;
        destination['y'] = +adata[0].y;
        destination['name'] = data.areaName + '(' + fMap.getFloor(+adata[0].floor).name + ')';
    }
    searchNearModel(currentCoord, (currentCoord) => {
        naviRoute(currentCoord, null);
    }, null, 'walk');
};

// 错误框
function hide_error() {
    $("#error").addClass('error_hide')
};
function show_error() {
    $("#error").removeClass('error_hide')
};
function clear_error() {
    $("#error_conent").html('')
};