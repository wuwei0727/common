var barType = 'count';
var beaconList = [];//信标列表
var detectorList = [];
var gatewayList = [];

var mapId = '';
var uId = '';
var operShow = '';
var tablen = 1;
var detailList = null;

var playEnd = null;
var playIndex = 0;

var historyList = [];
var historyTimer = null;
var payList = null;
var playItem = null;

var minOffsetDis = 3;
var maxOffsetDis = 15;
var maxEndDistance = 10;
var locationMarker = null;

var devicePie = null;
var deviceType = 'beacon';
var deviceChars = [];

var userList = [];//用户列表

var fMapLayer = {};
//定义搜索分析类
var analyser = null;

// 导航对象（init文件的用于别的用途）
var naviTarget = null;
//开始导航开关
var navigation = false;

var countAndFeeDay = 1;
var countAndFeeChatrs = null;

var oldUserId = '';
var useChargeECharts; // 充电车位使用-echarts
var mapUserList = []; // 地图实时用户数据
var allEnter = []; // 地图车位实时数据

var settime = null;

var timeData = {
    time: "1",
    mapId: '',
}; // 选择的时间段


$(function () {

    getTime();
    settime = setInterval(getTime, 1000);

    var params = getAllUrl();
    mapId = params.mapId;
    var uidDom = document.getElementById('websocket');
    if (uidDom) {
        uId = $.trim(uidDom.innerText);
    }
    if (!mapId) {
        tips('获取地图失败');
        return;
    };

    timeData.mapId = mapId;

    // initDate(-7); 

    $('#realtime').bind("mouseleave", function () {
        $(this).hide();
        $('#realtime-btn').removeClass('current-oper');
        operShow = '';
    });
    $('#beacon').bind("mouseleave", function () {
        $(this).hide();
        $('#beacon-btn').removeClass('current-oper');
        operShow = '';
    });
    $('#legend').bind("mouseleave", function () {
        $(this).hide();
        $('#legend-btn').removeClass('current-oper');
        operShow = '';
    });
    $('.realtime-list').height($(window).height() - 44 - 40 - 15 * 2 - 18 * 2 - 220);
    initMap();

});

function getTime() {
    clearInterval(settime);
    let curTime = new Date();
    let dayArr = ['星期天', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
    $(".headerTimeD")[0].innerText = curTime.getFullYear() + '年' + zero(curTime.getMonth() + 1) + '月' + zero(curTime.getDate()) + '日';
    $(".headerTimeT")[0].innerText = zero(curTime.getHours()) + ':' + zero(curTime.getMinutes()) + ':' + zero(curTime.getSeconds());
    $(".headerTimeW")[0].innerText = dayArr[curTime.getDay()];
    settime = setInterval(getTime, 1000);
}
function zero(str) {
    return str < 10 ? '0' + str : str;
}

//初始化车位使用时间
function initDate(daysAgo) {
    //daysAgo天(-)前(+)后
    var now = new Date();

    var ago = new Date(now.getTime() + (daysAgo + 1) * 24 * 60 * 60 * 1000);
    var time = ago.getFullYear() + '-' + padZero(ago.getMonth() + 1) + '-' + padZero(ago.getDate()) + ' 至 ';
    time += now.getFullYear() + '-' + padZero(now.getMonth() + 1) + '-' + padZero(now.getDate());
    $('#time').val(time);
    jeDate('#time', {
        theme: {
            bgcolor: "#4A60CF",
            pnColor: "#4A60CF"
        },
        multiPane: false,
        range: " 至 ",
        format: "YYYY-MM-DD",
        donefun: function () {
            placeAnalyze();
        }
    });
}

//补零
function padZero(n) {
    return n > 9 ? '' + n : '0' + n;
}

//切换标题
function switchTitle(that, type) {
    var _that = $(that);
    if (_that.hasClass('cur-title')) {
        return;
    }
    _that.addClass('cur-title');
    _that.siblings().removeClass('cur-title');
    $('#' + type).show().siblings().hide();

    if (type === 'countAndFeeBox') {
        //显示后再加载，不然宽度会变形
        if (!countAndFeeChatrs) {
            initCountAndFee(1);
        } else {
            countAndFeeChatrs.resize();
        }
    } else if (type === 'useCharge-box') {
        useChargeECharts.resize()
    } else if (type === 'deviceCount') {
        var target = contrastReturn(deviceChars, 'type', deviceType);
        if (!target) {
            tips('切换失败');
            return;
        }

        if (devicePie) {
            devicePie.setOption({
                series: [{
                    name: target.title,
                    data: [
                        { value: target.onLine, name: '在线' },
                        { value: target.offLine, name: '离线 ' },
                        { value: target.subLowPower, name: '低电量' },
                    ],
                }],
            })
        } else {
            devicePie = initPie('deviceStatistics', {
                name: target.title,
                color: ['#0990E2', '#8A8A8A', '#D5C417'],
                data: [
                    { value: target.onLine, name: '在线' },
                    { value: target.offLine, name: '离线 ' },
                    { value: target.subLowPower, name: '低电量' },
                ],
            })
        }

        devicePie.resize();
    } else if (type == 'parkUsageStatisticsBox') {
        // 热门地点
        initPopularLocations();
    }
}

function switchCountAndFee(that, day) {
    if (countAndFeeDay === day) {
        return;
    }
    countAndFeeDay = day;
    switchSeleBox(that);
    initCountAndFee(day);
}

function initCountAndFee(day) {
    $.ajax({
        url: url + 'park/getPlaceMapFeeAndFlow',
        type: 'post',
        data: {
            map: mapId,
            day: day,
        },
        success: function (res) {
            if (res.code !== 200) {
                console.log('初始化折线图失败');
                return;
            }
            var data = res.data;
            var target = null;
            var xData = [];
            var count = [];
            var fee = [];
            for (var i = 0; i < data.length; i++) {
                target = data[i];
                xData.push(target.txt);
                count.push(target.val.count);
                fee.push(target.val.fee);
            }
            initCountAndFeeChatrs(xData, count, fee);
        },
        error: function (err) {
            console.log(err);
        }
    })
}

function initCountAndFeeChatrs(xData, count, fee) {
    var option = null;
    if (countAndFeeChatrs) {
        option = {
            xAxis: {
                data: xData,
            },
            series: [{
                data: count,
            }, {
                data: fee,
            }]
        }
    } else {
        option = {
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    type: 'shadow'
                }
            },
            legend: {
                data: ['车流量', '收费情况'],
                textStyle: {
                    color: '#fff',
                }
            },
            grid: {
                left: '5%',
                right: '5%',
                bottom: '3%',
                containLabel: true
            },
            xAxis: {
                type: 'category',
                data: xData,
                axisTick: {
                    alignWithLabel: true
                },
                axisPointer: {
                    type: 'shadow'
                },
                axisLine: {
                    lineStyle: {
                        color: '#fff',
                    }
                },
            },
            yAxis: [
                {
                    type: 'value',
                    name: '车流量',
                    nameTextStyle: {
                        color: "#fff"
                    },
                    splitLine: {
                        show: false,
                    },
                    axisLabel: {
                        show: true,
                        textStyle: {
                            color: '#fff'
                        }
                    }
                }, {
                    type: 'value',
                    name: '收费情况',
                    nameTextStyle: {
                        color: "#fff"
                    },
                    splitLine: {
                        show: false,
                    },
                    axisLabel: {
                        show: true,
                        textStyle: {
                            color: '#fff'
                        }
                    }
                }
            ],
            series: [
                {
                    name: '车流量',
                    type: 'line',
                    data: count,
                    smooth: true,
                }, {
                    name: '收费情况',
                    type: 'line',
                    yAxisIndex: 1,
                    data: fee,
                    smooth: true,
                }
            ]
        };
        countAndFeeChatrs = echarts.init(document.getElementById('countAndFee'));
    }
    countAndFeeChatrs.setOption(option);
}

//初始化数据
function initMap() {
    $.ajax({
        url: url + 'park/getSimulateTrail',
        data: {
            map: mapId,
        },
        success: function (res) {
            if (res.code == 200) {
                var data = res.data;
                if (!data) {
                    tips('获取地图详情失败，请重试');
                    return;
                }
                $('#header').html(data.fName);
                var list = data.list;
                var result = [];
                var target = null;
                for (var i = 0; i < list.length; i++) {
                    target = list[i];
                    result.push({
                        id: target.id,
                        niceName: target.niceName,
                        start: {
                            x: +target.startX,
                            y: +target.startY,
                            groupID: +target.floor,
                        },
                        end: {
                            x: +target.endX,
                            y: +target.endY,
                            groupID: +target.floor,
                        },
                    })
                }
                detailList = result;
                loading();
                console.log('data', data);
                openMap({
                    fKey: data.fKey,
                    fName: data.appName,
                    fId: data.fID,
                    themeName: data.themeName,
                    //path:data.themeImg,
                    // defaultThemeName: '3008',
                    defaultControlsPose: 0,
                    //初始指北针的偏移量
                    compassOffset: [20, 50],
                    //指北针大小默认配置
                    compassSize: 64,
                    //defaultViewMode:fengmap.FMViewMode.MODE_2D,
                    defaultBackgroundColor: '#153160',
                    focusFloor: 1,
                    //楼层控件的top值
                    floorTop: 142,
                }, function () {
                    //获取楼层的layer
                    // var floorList = fMap.groupIDs;
                    var floorList = fMap.getLevels();
                    var group = null;
                    var gid;
                    // for (var j = 0; j < floorList.length; j++) {
                    //     //获取所有楼层的layer
                    //     gid = floorList[j];
                    //     // group = fMap.getFMGroup(gid);
                    //     group = fMap.getLevel(gid);
                    //     fMapLayer['f' + gid] = {
                    //         imgLayer: group.getOrCreateLayer('imageMarker'),
                    //         textLayer: group.getOrCreateLayer('textMarker'),
                    //         domLayer: group.getOrCreateLayer('domMarker'),
                    //     }
                    // }
                    removeLoad();
                    /*fMap.on('focusGroupIDChanged', function (event) {
                        var gid = event.gid;
                    });*/
                    // fMap.setAutoRotateBymodelSpeed(2);


                    //初始化搜索
                    analyser = new fengmap.FMSearchAnalyser({ map: fMap });//fid搜索
                    naviAnalyser = new fengmap.FMNaviAnalyser({ map: fMap });//路径计算

                    // init(); // 车位数相关
                    // placeAnalyze(); // 停车时长及费用
                    //initViolation(); // 违停信息

                    //分站、检测器、网关
                    // getAllMarker();

                    //获取车位
                    getAllPlace();
                    setTimeout(function () {
                        startRequest();
                    }, 1000)
                    //初始化模拟数据
                    // initDetail();
                    //历史记录
                    // getHistory();

                    // api请求
                    // apiGetDataState();

                }, function () {
                    //error
                    removeLoad();
                })
            } else {
                tips(res.message);
            }
        },
        error: function (jqXHR) {
            tips('系统繁忙');
        }
    })
}

function initPie(dom, params) {
    var pie = echarts.init(document.getElementById(dom));
    var data = params.data;
    for (var i = 0; i < data.length; i++) {
        data[i].label = {
            fontSize: 20
        }
    }
    var option = {
        tooltip: {
            trigger: 'item'
        },
        legend: {
            orient: 'vertical',
            left: '50%',
            y: 'center',

            selectedMode: false,
            textStyle: {
                color: '#758CB2',
                fontSize: 16,
            }
        },
        series: [
            {
                name: params.name,
                type: 'pie',
                radius: params.radius || 55,
                center: ['30%', '50%'],
                label: {
                    normal: {
                        show: true,
                        position: 'inner',
                        color: '#fff',
                        formatter: '{d}%'
                    }
                },
                color: params.color || ['#0999E2', '#5AD1C5'],
                data: data,
                emphasis: {
                    itemStyle: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0.5)'
                    }
                }
            }
        ]
    }
    pie.setOption(option);
    return pie;
};

function initPie2(dom, param, total) {
    var series;
    if (param.data1) {
        series = [
            {
                type: 'pie',
                radius: ['35%', '50%'],
                center: ['50%', '24%'],
                data: param.data1,
                itemStyle: getPieCAndI().itemStyle,
                feature: {
                    restore: { show: true },
                },
                color: [
                    getPieCAndI(param.color[0], param.color[1]).color,
                    getPieCAndI(param.color[2], param.color[3]).color
                ],
            },
            {
                type: 'pie',
                radius: ['25%', '35%'],
                center: ['50%', '24%'],
                data: param.data2,
                itemStyle: getPieCAndI().itemStyle,
                feature: {
                    restore: { show: true },
                },
                color: [(param.color[4]), (param.color[5])],
            }
        ];
    } else {
        series = [
            {
                type: 'pie',
                radius: ['35%', '50%'],
                center: ['50%', '24%'],
                data: param.data,
                itemStyle: getPieCAndI().itemStyle,
                feature: {
                    restore: { show: true },
                },
                color: [
                    // getPieCAndI(param.color[0], param.color[1]).color,
                    // getPieCAndI(param.color[2], param.color[3]).color
                    // param.color[0], param.color[1], param.color[2]
                    {
                        type: 'linear',
                        x: 0,
                        y: 0,
                        x2: 0,
                        y2: 1,
                        colorStops: [{
                            offset: 0, color: param.color[0]
                        }, {
                            offset: 1, color: param.color[1]
                        }],
                        global: false
                    },
                    {
                        type: 'linear',
                        x: 0,
                        y: 0,
                        x2: 0,
                        y2: 1,
                        colorStops: [{
                            offset: 0, color: param.color[2]
                        }, {
                            offset: 1, color: param.color[3]
                        }],
                        global: false
                    },
                    {
                        type: 'linear',
                        x: 0,
                        y: 0,
                        x2: 0,
                        y2: 1,
                        colorStops: [{
                            offset: 0, color: param.color[4]
                        }, {
                            offset: 1, color: param.color[4]
                        }],
                        global: false
                    },
                ],
            }
        ];
    }
    var pie = echarts.init(document.getElementById(dom));
    var option = {
        legend: {
            orient: 'vertical',
            left: param.legendLeft || '0%',
            itemWidth: 4,
            itemHeight: 4,
            itemGap: 7,
            top: '50%',
            selectedMode: false,
            textStyle: {
                color: '#B8D3F1',
                fontSize: 10,
            },
            formatter: function (name) {
                var arr;
                // if (name === '在线' || name === '离线') {
                //     arr = param.data1 || param.data;
                // } else if (name === '低电量' || name === '电量正常') {
                //     arr = param.data2;
                // }
                arr = param.data;
                var sum = 0, sum1 = 0, index = 0;
                // arr.forEach(item => {
                //     if (item.name === '在线' || item.name === '离线') {
                //         sum += item.value;
                //     } else if (item.name === '低电量' || item.name === '电量正常') {
                //         sum1 += item.value;
                //     }
                // });
                arr.forEach((item, i) => {
                    sum += item.value
                    if (item.name == name) {
                        index = i;
                    }
                });
                // if (name === '在线' || name === '离线') {
                //     let percent = (arr[index].value / sum * 100).toFixed(2);
                //     return name + ' ' + arr[index].value + " (" + percent + '%)';
                // } else if (name === '低电量' || name === '电量正常') {
                //     let percent1 = (arr[index].value / sum1 * 100).toFixed(2);
                //     return name + ' ' + arr[index].value + " (" + percent1 + '%)';
                // }
                let percent = (arr[index].value / sum * 100).toFixed(2);
                return name + ' ' + arr[index].value + " (" + percent + '%)';
            },
        },
        tooltip: {
            trigger: 'item',
            position: ['20%', '50%']
        },
        series: series

    };
    pie.setOption(option);
    pie.resize();
    window.addEventListener("resize", function () {
        pie.resize();
    });
    let domNum = "#" + dom + "Total"
    $(domNum).html(total);
};

function getPieCAndI(color1, color2) {
    var color = {
        type: 'linear',
        x: 0,
        y: 0,
        x2: 0,
        y2: 1,
        colorStops: [{
            offset: 0, color: color1
        }, {
            offset: 1, color: color2
        }],
        global: false
    };
    var itemStyle = {
        normal: {
            label: {
                show: false,
                formatter: '{d}%',
            },
            labelLine: {
                show: false
            }
        }
    };
    return { color, itemStyle };
}

//初始化数据
function init() {
    $.ajax({
        url: url + 'park/getRealTimeData',
        data: {
            map: mapId,
        },
        success: function (res) {
            if (res.code != 200) {
                tips(res.message);
                return;
            }
            var data = res.data;
            $('#totalPlace').html(data.totalPlace);
            $('#emptyPlace').html(data.emptyPlace);
            $('#usedPlace').html(data.usedPlace);
            $('#totalCharge').html(data.totalCharge);
            $('#usedCharge').html(data.usedCharge);
            $('#emptyCharge').html(data.emptyCharge);

            initPie('usePlace', {
                name: '车位使用',
                color: ['#de7065', '#009417'],
                data: [
                    { value: data.usedPlace, name: '已停车位' },
                    { value: data.emptyPlace, name: '空余车位 ' },
                ],
                //radius: [35, 55],
            })
            useChargeECharts = initPie('useCharge', {
                name: '充电桩使用',
                color: ['#4361d8', '#052af7'],
                data: [
                    { value: data.usedCharge, name: '使用中' },
                    { value: data.emptyCharge, name: '未使用 ' },
                ],
                radius: [35, 55],
            })
            initPie('appointment', {
                name: '预约车位',
                color: ['#2CDAFF', '#4A60CF'],
                data: [
                    { value: data.bookPlace, name: '预约车位' },
                    { value: data.totalPlace, name: '总车位 ' },
                ],
                radius: [35, 55],
            })
        },
        error: function (err) {
            tips('系统繁忙');
        }
    })
}

//违停信息
function initViolation() {
    $.ajax({
        url: url + 'park/getWeiTing',
        data: {
            pageIndex: 1,
            pageSize: 20
        },
        type: 'post',
        success: function (res) {
            var tab = $('#tab');
            if (res.code != 200) {
                tips(res.message);
                return;
            }
            var data = res.data;
            var list = data.list;
            var len = list.length;
            var target = null;
            var html = '';
            for (var i = 0; i < len; i++) {
                target = list[i];
                html += '<tr>';
                html += '<td>' + target.license + '</td>';
                html += '<td>' + (target.floor || '') + '</td>';
                html += '<td>' + target.time + '</td>';
                html += '<td>' + (target.state ? '否' : '是') + '</td>';
                html += '</tr>';
            }
            $('#violation').html(html);
        }
    })
}

//初始化数据
function placeAnalyze() {
    var time = $('#time').val();
    time = time.split(' 至 ');
    $.ajax({
        url: url + 'park/queryPlaceUseData',
        data: {
            map: mapId,
            start: time[0],
            end: time[1],
            content: barType,
        },
        success: function (res) {
            if (res.code != 200) {
                tips(res.message);
                return;
            }
            var list = res.data;
            var len = list.length;
            if (!len) {
                initBar([], [], '');
                return;
            }
            var xData = [];
            var data = [];
            var target = null;
            for (var i = 0; i < len; i++) {
                target = list[i];
                xData.push(target.placeName);
                data.push(target[barType]);
            }
            var name = '';
            if (barType === 'count') {
                name = '频次';
            } else {
                name = '时长';
            }
            initBar(xData, data, name);
        },
        error: function (err) {
            tips('系统繁忙');
        }
    })
}

//初始化柱状图
function initBar(xData, data, name) {
    var bar = echarts.init(document.getElementById('placeBar'));
    var option = {
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'shadow'
            }
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: [
            {
                type: 'category',
                data: xData,
                axisTick: {
                    alignWithLabel: true
                },
                axisLabel: {
                    show: true,
                    textStyle: {
                        color: '#fff'
                    }
                }
            }
        ],
        yAxis: [
            {
                type: 'value',
                axisLabel: {
                    show: true,
                    textStyle: {
                        color: '#fff'
                    }
                }
            }
        ],
        series: [
            {
                name: name || '',
                type: 'bar',
                barWidth: '60%',
                data: data,
            }
        ]
    };
    bar.setOption(option);
};
function initBar2(dom, params) {
    var findCarBar = document.getElementById(dom);
    var bar = echarts.init(findCarBar);
    var option = {
        series: [{
            type: 'bar',
            data: params.yValue,
            itemStyle: {
                color: {
                    type: 'linear',
                    x: 0,
                    y: 0,
                    x2: 0,
                    y2: 1,
                    colorStops: [{
                        offset: 0, color: params.color[0]
                    }, {
                        offset: 1, color: params.color[1]
                    }],
                    global: false
                }
            },
            barWidth: '40%'
        }],
        tooltip: {
            trigger: 'item',
            extraCssText: 'padding: 8px 10px 4px;line-height: 1.5;font-size: 16px;text-align: center;',
            // extraCssText: 'padding: 8px 10px 4px; line-height: 1.5; font-size: 16px;background-color:rgba(77,81,255,0.17); color:#05C7FF; text-align: center;border: 1px solid #fff;',
            position: ["40%", "30%"],
            formatter: function (params) {
                let str = params.name + '</br>' + params.value + "万(小时)";
                if (params) {
                    str += "<span style='display:inline-block;width:10px;height:10px;border-radius:10px;></span>"
                }
                return str
            },
        },
        xAxis: {
            data: params.xValue,
            splitLine: {
                show: false
            },
            axisTick: {
                show: false
            },
            axisLabel: {
                color: '#B8D3F1',
                fontSize: 10,
            }
        },
        yAxis: {
            type: 'value',
            name: "万(小时)",
            nameTextStyle: {
                color: "#B8D3F1",

            },
            splitLine: {
                show: false
            },
            axisLine: {
                show: true
            },
            axisTick: {
                show: true
            },
            axisLabel: {
                hideOverlap: false,
                color: '#B8D3F1',
                fontSize: 10
            }
        },
        grid: {
            top: '18%',
            bottom: '0%',
            left: '0%',
            right: '0%',
            containLabel: true,
            borderWidth: 0
        }
    };
    bar.setOption(option);
    window.addEventListener("resize", function () {
        bar.resize();
    });
};

function initBar3(dom, params) {
    var findCarBar = document.getElementById(dom);
    var bar = echarts.init(findCarBar);
    var option = {
        series: [{
            type: 'bar',
            data: params.yValue,
            itemStyle: {
                color: {
                    type: 'linear',
                    x: 0,
                    y: 0,
                    x2: 0,
                    y2: 1,
                    colorStops: [{
                        offset: 0, color: params.color[0]
                    }, {
                        offset: 1, color: params.color[1]
                    }],
                    global: false
                }
            },
            barWidth: '40%'
        }],
        tooltip: {
            trigger: 'item',
            extraCssText: 'padding:8px 10px 4px;line-height:1.5;font-size:16px;text-align:center;',
            // extraCssText: 'padding: 8px 10px 4px; line-height: 1.5; font-size: 16px;background-color:rgba(77,81,255,0.17);color:#05C7FF;text-align:center;border: 1px solid #fff;',
            position: ["40%", "30%"],
            formatter: function (params) {
                let str = params.name + '</br>' + params.value + "万次";
                if (params) {
                    str += "<span style='display:inline-block;width:10px;height:10px;border-radius:10px;></span>"
                }
                return str
            },
        },
        xAxis: {
            data: params.xValue,
            splitLine: {
                show: false
            },
            axisTick: {
                show: false
            },
            axisLabel: {
                color: '#B8D3F1',
                fontSize: 10,
            }
        },
        yAxis: {
            type: 'value',
            name: "万次",
            nameTextStyle: {
                color: "#B8D3F1",

            },
            splitLine: {
                show: false
            },
            axisLine: {
                show: true
            },
            axisTick: {
                show: true
            },
            axisLabel: {
                hideOverlap: false,
                color: '#B8D3F1',
                fontSize: 10
            }
        },
        grid: {
            top: '18%',
            bottom: '0%',
            left: '0%',
            right: '0%',
            containLabel: true,
            borderWidth: 0
        }
    };
    bar.setOption(option);
    window.addEventListener("resize", function () {
        bar.resize();
    });
};

//切换车位使用分析的条件
function switchParking(that, type) {
    if (barType === type) {
        return;
    }
    barType = type;

    switchSeleBox(that);
    placeAnalyze();
}

//切换seleBox样式
function switchSeleBox(that) {
    var _that = $(that);
    var parent = _that.parent();
    parent.find('.curSele').removeClass('curSele');
    _that.addClass('curSele');
}

//切换设备统计
function switchDevice(that, type) {
    if (deviceType === type) {
        return;
    }
    var target = contrastReturn(deviceChars, 'type', type);
    if (!target) {
        tips('切换失败');
        return;
    }
    deviceType = type;
    switchSeleBox(that);

    if (devicePie) {
        devicePie.setOption({
            series: [{
                name: target.title,
                data: [
                    { value: target.onLine, name: '在线' },
                    { value: target.offLine, name: '离线 ' },
                    { value: target.subLowPower, name: '低电量' },
                ],
            }],
        })
    } else {
        devicePie = initPie('deviceStatistics', {
            name: target.title,
            color: ['#0990E2', '#8A8A8A', '#D5C417'],
            data: [
                { value: target.onLine, name: '在线' },
                { value: target.offLine, name: '离线 ' },
                { value: target.subLowPower, name: '低电量' },
            ],
        })
    }
}

//设备统计饼图
function deviceData(data, type) {
    if (type == 12) {
        // 信标统计
    };
    if (type == 13) {
        // 网关统计
    };
    if (type == 14) {
        // 车位检测器统计
    }
};

//显示隐藏
function toggle(that, dir) {
    var $that = $(that);
    if ($that.hasClass('arrorRotate') || $that.hasClass('farrorRotate')) {
        if (dir === 'left') {
            $('.l').removeClass('lHide');
            $that.removeClass('arrorRotate');
        } else if (dir === 'right') {
            $('.r').removeClass('rHide');
            $that.removeClass('arrorRotate');
            $(".viewMode3").css('z-index', 2)
        } else if (dir === 'top') {
            $('.f').removeClass('fHide');
            $that.removeClass('farrorRotate');
            $('.flooterBox').removeClass('flooterBoxHide');
        }
    } else {
        if (dir === 'left') {
            $('.l').addClass('lHide');
            $that.addClass('arrorRotate');
        } else if (dir === 'right') {
            $('.r').addClass('rHide');
            $that.addClass('arrorRotate');
            setTimeout(() => {
                $(".viewMode3").css('z-index', 4)
            }, 310);
        } else if (dir === 'top') {
            $('.f').addClass('fHide');
            $that.addClass('farrorRotate');
            $('.flooterBox').addClass('flooterBoxHide');
        }
    }
}

var tipsTimer = null;

//创建提示
function tips(str, fn, time) {
    var tips = $('#tip');
    if (tipsTimer) {
        clearTimeout(tipsTimer);
    } else {
        tips.css('top', '50px');
    }
    tips.html(str || '');
    tipsTimer = setTimeout(function () {
        tips.css('top', '-100px');
        clearTimeout(tipsTimer);
        tipsTimer = null;
        fn && fn();
    }, (time || 3000))
}

// load
function loading() {
    $('body').append('<div id="loadMask"></div><div id="load"><img src="../image/common/load.gif" /></div>');
}

//removeLoad
function removeLoad() {
    $('#loadMask').remove();
    $('#load').remove();
}


//获取url的所有参
function getAllUrl() {
    var url = location.search; //获取url中"?"符后的字串  
    var theRequest = new Object();
    if (url.indexOf("?") != -1) {
        var str = url.substr(1);
        strs = str.split("&");
        for (var i = 0; i < strs.length; i++) {
            theRequest[strs[i].split("=")[0]] = window.decodeURIComponent(strs[i].split("=")[1]);
        }
    }
    return theRequest;
}

//旋转
function rotate(that) {
    if (!mapLoad) {
        tips('地图加载失败，请重试');
        return;
    }
    if (fMap.getAutoRotateBymodel()) {
        $(that).removeClass('rotates');
        fMap.setAutoRotateBymodel(false);
    } else {
        $(that).addClass('rotates');
        fMap.setAutoRotateBymodel(true);
        fMap.moveTo(fMap.center);
    }
}

//功能切换
function toggleOper(that, type) {
    if (operShow === type) {
        //关闭
        $('#' + operShow + '-btn').removeClass('current-oper');
        $('#' + operShow).hide();
        operShow = '';
        return;
    }
    if (operShow) {
        $('#' + operShow + '-btn').removeClass('current-oper');
        $('#' + operShow).hide();
    }
    operShow = type;
    $('#' + type + '-btn').addClass('current-oper');
    $('#' + type).show();
}

//信标操作
function markerOper(that, type) {
    var _that = $(that);
    var flag = false;
    if (_that.hasClass('current-marker')) {
        _that.removeClass('current-marker');
    } else {
        _that.addClass('current-marker');
        flag = true;
    }

    var targetList = null;
    if (type === 'base') {
        targetList = beaconList;
    } else if (type === 'detector') {
        targetList = detectorList;
    } else if (type === 'gateway') {
        targetList = gatewayList;
    } else {
        tips('出现错误');
        return;
    }

    var target = null;
    for (var i = 0; i < targetList.length; i++) {
        target = targetList[i];
        target.im.visible = flag;
        target.tm.visible = flag;
    }
}

//添加图片
function addFMImageMarker(imgLayer, para) {
    var im = resImgMarker(para);
    imgLayer.addMarker(im);

    return im;
}

//添加文本
function addTextMarker(textLayer, para) {
    var tm = resTextMarker(para);
    textLayer.addMarker(tm);

    return tm;
}

//改变图片
function changeFMImageMarker(list, flag) {
    var target = null;
    var base = null;
    var len = beaconList.length;
    var j;
    for (var i = 0; i < list.length; i++) {
        target = list[i];
        for (j = 0; j < len; j++) {
            base = beaconList[j];
            if (base.name == target.num) {
                if (base.state === flag) {
                    continue;
                }
                base.state = flag;
                base.im.url = flag ? '../image/common/beacon-light.png' : '../image/common/beacon-gray.png';
                base.tm.fillcolor = flag ? '#FFE400' : '#B3B3B3';
            }
        }
    }
}

var socket;

//请求定位数据
function startRequest() {
    if (socket) {
        return;
    }
    if (typeof (WebSocket) == "undefined") {
        tips("您的浏览器不支持WebSocket");
    } else {
        /*if(!uId){
            console.log('定位数据连接失败，请检查是否登录');
            tips('定位数据连接失败，请检查是否登录');
            return;
        }*/
        console.log("您的浏览器支持WebSocket");
        //实现化WebSocket对象，指定要连接的服务器地址与端口  建立连接
        //socket = new WebSocket('ws://' + location.host + '/websocket/location/' + uId);
        socket = new WebSocket(websocketUrl + mapId);

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
    //推送的都为单个的对象
    var item = JSON.parse(res);
    // console.log(item);
    /*if(item.type != 2){
        if(item.map != mapId){
            return;
        }
    }*/
    /*var data = null;
    if(item.data instanceof Array){
        //首次为数组
        data = item.data;
    }else{
        data = [item.data];
    }*/
    if (item.map != mapId) {
        return;
    }
    var type = item.type;
    var data = item.data;
    if (type == 1) {
        //定位数据
        fMapPos([data]);//暂不修改里面的方法
        fMapUsersNum([data]);// 计算用户数
    } else if (type == 5 || type == 6 || type == 7) {
        //信标状态
        changeState(item);
    } else if (type == 8) {
        //车位状态
        changeModelRender(data);
    } else if (type == 9) {
        //车位使用
    } else if (type == 10) {
        //充电桩使用
    } else if (type == 11) {
        //预约车位(修改次数少就不保存变量)
    } else if (type == 12 || type == 13 || type == 14) {
        //设备统计
        // deviceData(data, type);
    } else if (type == 15) {
        //收费统计
        // chargeStatistics(data);
    } else if (type == 16) {
        //出入数据
        // inAndOutData(data);
    } else if (type == 17) {
        // 地图车位实时数据-40
        allEnter = data;
        // initEnter(data);
    } else if (type == 18) {
        // 地图车位实时数据-1
        // updateEnter(allEnter, data);
    } else {
        console.log('未知类型');
    }
}

//信标状态数据
function fMapPos(data) {
    var target = null;
    var result = null;
    var html = '';
    var im = null;
    var tm = null;
    var curLayer = null;
    var userid;
    for (var i = 0; i < data.length; i++) {
        target = data[i];
        userid = target.userid;
        target.x = +target.x;
        target.y = +target.y;
        result = contrastReturn(userList, 'id', userid);
        if (!result) {
            // curLayer = fMapLayer['f' + target.floor];
            // if (!curLayer) {
            //     console.log('楼层错误');
            //     continue;
            // }
            html += '<tr><td class="order">' + (tablen) + '</td>';
            html += '<td class="user"><div class="checkbox detail-item" onclick="checkUser(this,\'' + userid + '\' ,\'' + tablen + '\')">';
            html += '<div class="checked-radiu"></div></div>';
            html += '<div class="user-text">' + '微信' + (tablen) + '</div></tr>';
            tablen++;

            im = newAddImgMarkerToMap(target.floor, {
                x: target.x,
                y: target.y,
                url: '../image/fMap/mapUserRed.png',
                size: 24,
                height: 2,
            })
            tm = newAddTextMarkerToMap(target.floor, {
                x: target.x,
                y: target.y + 1,
                height: 3,
                name: ' ',
                fillcolor: '#fff',
                strokecolor: '#3C5CFF',
                fontsize: 16,
                avoid: false,
            })
            tm.visible = im.visible = false;
            tm.selfId = im.selfId = userid;
            tm.selfType = im.selfType = 'user';

            // 初始化地图实时用户位置
            let mapuseritemdata = {
                userid: userid,
                imgMarker: null
            };
            mapuseritemdata.imgMarker = newAddImgMarkerToMap(target.floor, {
                x: target.x,
                y: target.y,
                url: '../image/fMap/mapUserBlue.png',
                size: 24,
                height: 2,
                avoid: false,
            });
            mapuseritemdata.imgMarker.visible = true;
            mapUserList.push(mapuseritemdata);

            userList.push({
                id: userid,
                name: target.niceName,
                checked: false,
                avoidState: true,//避让状态
                im: im,
                tm: tm,
                floor: target.startFloor,
                nav_state: target.nav_state,
                state: target.state,
                target: target.target,
                speed: target.speed,
                type: target.type || 'user',
                startX: target.startX,
                startY: target.startY,
                startFloor: target.startFloor,
                endX: target.endX,
                endY: target.endY,
                endFloor: target.endFloor,
            });
        } else {
            if (target.target) {
                if (result.checked) {
                    var startInfo = {
                        x: +target.startX,
                        y: +target.startY,
                        groupID: +target.startFloor,
                    }
                    if (target.target != result.target || isResetNavi(result, startInfo)) {
                        if (result.hasNaviLine) {
                            //如果以前有则移除当前线
                            removeNaviRoute(userid);
                        };
                        //导航
                        createNavi(userid, startInfo, {
                            x: +target.endX,
                            y: +target.endY,
                            groupID: +target.endFloor,
                        });
                        result.hasNaviLine = true;
                    };
                }
            } else {
                if (result.hasNaviLine) {
                    //如果以前有则移除当前线
                    removeNaviRoute(userid);
                    result.hasNaviLine = false;
                }
            }
            setLocationMakerPosition(result, target);
            updateUserInfo(result, target);

            // 更新地图上的用户位置
            updateMapUsersPoint(target);

            if (result.checked) {
                // 更新用户实时情况
                showRealtimeDetail(result, result.checked, userNameIndex);
            }

        }
    }
    if (html) {
        $('#realtimeTbody').append(html);
    }
};

var usersNumInfoData = [];
function fMapUsersNum(data) {
    let usersNumObj = {};

    if (!data.length) {
        $("#usersNumMainBox").addClass('noneShow');
        return false
    };

    for (var i = 0; i < data.length; i++) {
        let target = data[i];
        let userid = target.userid;
        let resultIndex = usersNumInfoData.findIndex((fitem) => {
            return fitem.userid == userid;
        });
        if (resultIndex != -1) {
            usersNumInfoData.splice(resultIndex, 1, target)
        } else {
            usersNumInfoData.push(target)
        }
    };

    $("#usersNumMainBox").removeClass('noneShow');

    usersNumInfoData.forEach((item) => {
        if (item.floor < 1) {
            item.floor = item.startFloor
        };

        if (usersNumObj[item.floor]) {
            usersNumObj[item.floor] += 1
        } else {
            usersNumObj[item.floor] = 1
        };
    });

    let html = '';
    for (key in usersNumObj) {
        if (key < 1) return false;
        let name = fMap.getFloor(+key).name || key;
        let num = usersNumObj[key];

        html += `
            <div class="userInfoItem">
                <div class="userInfoNum">${num}</div>
                <div class="usreInfoName">${name}</div>
            </div>
        `
    };

    $("#usersNumBox").html(html)
};

//判断是否偏航
function isResetNavi(result, start) {
    var sx = start.x;
    var sy = start.y;
    var sgroupID = start.groupID;
    if (!sx || !sy || !sgroupID) {
        //偏航时会携带起点的信息（导航时的位置变化没有--用于区分）
        return false;
    }
    if (result.startX != sx || result.startY != sy || result.startFloor != sgroupID) {
        return true;
    }
    return false;
}

//更新人员信息
function updateUserInfo(result, target) {
    // if (JSON.stringify(result) == JSON.stringify(target)) {
    //     console.log('不更新信息，为相同的情况');
    //     return;
    // }
    //text用于说明，清楚可不写
    var list = [
        {
            key: 'target',
            text: '目的地',
            write: true,
        }, {
            key: 'speed',
            text: '速度',
            write: true,
        }, {
            key: 'nav_state',
            text: '导航模式',
            write: true,
        }, {
            key: 'state',
            text: '状态',
            write: true,
        }, {
            key: 'startX',
        }, {
            key: 'startY',
        }, {
            key: 'startFloor',
        }, {
            key: 'endX',
        }, {
            key: 'endY',
        }, {
            key: 'endFloor',
        }
    ]
    var key;
    var value;
    var write;
    var dom = null;
    if (result.dm) {
        dom = $('#user' + result.id);
    }
    for (var i = 0; i < list.length; i++) {
        key = list[i].key;
        write = list[i].write;
        value = target[key];
        if (value != result[key]) {
            if (write && dom) {
                dom.find('.' + key).html(value);
            }
            result[key] = value;
        }
    };
}

//更新位置
function setLocationMakerPosition(result, target, na_vi = false) {


    var locationGid = result.im.groupID || result.im.level;
    var x = target.x;
    var y = target.y;
    var groupID = target.floor;
    if (!x || !y) {
        return;
    }
    var data = {
        x: x,
        y: y,
        groupID: groupID,
        time: 0.5,
        //空函数，不加蜂鸟会报错
        callback: function () {
        },
        update: function () {
        },
    }

    if (locationGid !== groupID) {
        //跨层
        result.im.remove();
        let checked = result.checked || true;
        result.im = newAddImgMarkerToMap(groupID, {
            x: x,
            y: y,
            url: `../image/fMap/${checked ? 'mapUserRed' : 'mapUserBlue'}.png`,
            size: 24,
            height: 2,
        });
        result.im.visible = true;
        // result.im.setPosition(x, y, groupID, 2);
        // result.tm.setPosition(x, y + 1, groupID, 3);
        // if (result.dm && result.dm.visible) {
        //     result.dm.setPosition(x, y, groupID, 2);
        // }
    } else {
        result.im.moveTo(data);
        // if (result.dm && result.dm.visible) {
        //     result.dm.moveTo(data);
        // }
        // data.y = y + 1;
        // result.tm.moveTo(data);
    };

    if (naviTarget && (result.checked || na_vi)) {
        naviTarget.locate({
            x: x,
            y: y,
            level: groupID
        });
    }
}

//选择显示的用户
function checkUser(that, userid, index) {
    if (detailImgMarket) {
        detailImgMarket.remove();
    };
    var result = contrastReturn(userList, 'id', userid);
    if (!result) {
        console.log('找不到相对应的用户');
        return;
    }
    var im = result.im;
    var tm = result.tm;
    var dm = result.dm;
    // 只保留一个
    if (naviTarget) {
        if (navigation) {
            naviTarget.stop();
        }
        naviTarget.clearAll();
        naviTarget.dispose();
        naviTarget = null;
    };
    $('.current-check').removeClass('current-check');
    if (!oldUserId) {
        oldUserId = userid;
    } else {
        if (oldUserId != userid) {
            var resultOld = contrastReturn(userList, 'id', oldUserId);
            var imOld = resultOld.im;
            var tmOld = resultOld.tm;
            var dmOld = resultOld.dm;
            imOld.visible = false;
            tmOld.visible = false;
            if (dmOld && dmOld.visible) {
                dmOld.visible = false;
            }
            removeNaviRoute(oldUserId);
            resultOld.hasNaviLine = false;
            if (resultOld.type === 'history') {
                if (historyTimer) {
                    clearTimeout(historyTimer);
                    historyTimer = null;
                }
            };
            resultOld.checked = false;
            oldUserId = userid;
        }
    }


    if (result.checked) {
        $(that).removeClass('current-check');
        im.visible = false;
        tm.visible = false;
        if (dm && dm.visible) {
            dm.visible = false;
        }
        removeNaviRoute(userid);
        result.hasNaviLine = false;
        if (result.type === 'history') {
            if (historyTimer) {
                clearTimeout(historyTimer);
                historyTimer = null;
            }
        }
    } else {

        $(that).addClass('current-check');
        im.visible = true;
        tm.visible = false;

        if (result.type === 'history') {
            // fMap.moveTo({
            //     x: result.startX,
            //     y: result.startY,
            //     groupID: result.startFloor,
            //     time: 0.5,
            //     callback: function () {
            //     },
            //     update: function () {
            //     },
            // });

            let nowleve = fMap.getLevel();
            if (result.startFloor != nowleve) {

                fMap.setLevel({
                    level: +result.startFloor,
                    animate: false,
                })
            };
            fMap.setCenter({
                x: +result.startX,
                y: +result.startY,
                animate: false,
            });
        } else {
            // fMap.moveTo({
            //     x: im.x,
            //     y: im.y,
            //     groupID: im.groupID,
            //     time: 0.5,
            //     callback: function () {
            //     },
            //     update: function () {
            //     },
            // })

            let nowleve = fMap.getLevel();
            if (im.level != nowleve) {
                fMap.setLevel({
                    level: +im.level,
                    animate: false,
                })
            };
            fMap.setCenter({
                x: +im.x,
                y: +im.y,
                animate: false,
            });
        }
        if (result.target) {
            //显示路线（关闭时会移除所属路线）
            createNavi(userid, {
                x: +result.startX,
                y: +result.startY,
                groupID: +result.startFloor,
            }, {
                x: +result.endX,
                y: +result.endY,
                groupID: +result.endFloor,
            });
            result.hasNaviLine = true;
            if (result.type === 'history') {
                playHistory(result);
            }
        }
    };

    result.checked = !result.checked;

    // 设置选中的用户详情
    showRealtimeDetail(result, result.checked, index);
    // 设置选中图标颜色
    setMapUsersListColor(result);



}

//信标状态数据
function changeState(item) {
    var type = item.type;
    var data = item.data;
    var networkstate = data.networkstate;
    var power = data.power;
    var targetList = null;
    if (type == 5) {
        targetList = beaconList;
        changeFMImageMarker(data, true);
    } else if (type == 6) {
        targetList = gatewayList;
    } else if (type == 7) {
        targetList = detectorList;
    } else {
        return;
    }
    var result = contrastReturn(targetList, 'id', data.id);
    if (!result) {
        //新增的暂不管
        return;
    }
    var dm = result.dm;
    var dom = null;
    if (dm) {
        dom = $('#' + result.type + result.id);
    }
    if (result.networkstate != networkstate) {
        if (dom) {
            if (networkstate === 0) {
                dom.find('.networkstate').html('离线').removeClass('stateHas').addClass('stateNot');
            } else if (networkstate === 2) {
                dom.find('.networkstate').html('低电量').removeClass('stateHas').addClass('stateWarn');
            } else {
                dom.find('.networkstate').html('在线').removeClass('stateNot').addClass('stateHas');
            }
        }
        result.networkstate = networkstate;
    }
    if (result.power != power) {
        if (dom) {
            dom.find('.power').html(power);
        }
        result.power = power;
    }
}

//寻找对比返回
function contrastReturn(arr, attr, mVal) {
    var target = null;
    for (var i = 0; i < arr.length; i++) {
        target = arr[i];
        if (target[attr] == mVal) {
            return target;
        }
    }
}

//点击事件
function mapClickFn(result) {
    var selfType = result.selfType;
    if (!selfType) {
        return;
    }
    var target = null;
    var targetList = null;
    var text = '';
    if (selfType === 'base') {
        targetList = beaconList;
        text = '信标';
    } else if (selfType === 'detector') {
        targetList = detectorList;
        text = '车位检测器';
    } else if (selfType === 'gateway') {
        targetList = gatewayList;
        text = '网关';
    } else if (selfType === 'user') {
        //人员
        targetList = userList;
    } else {
        return;
    }
    target = contrastReturn(targetList, 'id', result.selfId);
    if (!target) {
        tips('没有找到相关的数据');
        return;
    }
    if (target.type === 'history') {
        //暂不显示
        return;
    }
    createDomMarker(target, selfType, text || target.name);
    return false;
}

//创建相关弹窗
function createDomMarker(target, type, title) {
    var im = null;
    // if (target.dm) {
    //     if (target.dm.show) {
    //         return;
    //     }
    //     if (type === 'user') {
    //         im = target.im;
    //         target.dm.setPosition(im.x, im.y, im.groupID, 2);
    //     }
    //     target.dm.show = true;
    //     return;
    // }
    var html = '<div class="pop" id="' + (type + target.id) + '">\
                    <div class="pop-title">\
                        <span class="pop-line"></span>\
                        <span>' + title + '</span>\
                        <span class="close" onclick="hideDm(\'' + target.id + '\',\'' + type + '\')"></span>\
                    </div>\
                    <div class="pop-content">';
    var writeList = [];
    if (type === 'base') {
        writeList = [{
            key: 'name',
            text: '设备ID',
        }, {
            key: 'power',
            text: '电量',
        }, {
            key: 'networkstate',
            text: '状态',
        }]
    } else if (type === 'detector') {
        writeList = [{
            key: 'name',
            text: '设备ID',
        }, {
            key: 'power',
            text: '电量',
        }, {
            key: 'networkstate',
            text: '状态',
        }]
    } else if (type === 'gateway') {
        writeList = [{
            key: 'name',
            text: '设备ID',
        }, {
            key: 'networkstate',
            text: '状态',
        }]
    } else if (type === 'user') {
        writeList = [{
            key: 'target',
            text: '目的地',
        }, {
            key: 'speed',
            text: '速度',
        }, {
            key: 'nav_state',
            text: '导航模式',
        }, {
            key: 'state',
            text: '状态',
        }]
    } else {
        return;
    }
    var writeItem = null;
    for (var i = 0; i < writeList.length; i++) {
        writeItem = writeList[i];
        html += '<div class="pop-item">\
                    <div class="pop-text">' + writeItem.text + '</div>\
                    <div class="pop-info">\
                        <span>:</span>\
                        <span class="pop-value ' + writeItem.key;
        if (writeItem.key === 'networkstate') {
            if (target.networkstate === 1) {
                html += ' stateHas">在线</span>';
            } else if (target.networkstate === 2) {
                html += ' stateWarn">低电量</span>';
            } else {
                html += ' stateHas">离线</span>';
            }
        } else {
            html += '">' + target[writeItem.key] + '</span>';
        }
        html += '</div></div>';
    }
    html += '</div>';
    var dm = null;
    if (type === 'user') {
        im = target.im;
        // dm = addFMDomMarker(fMapLayer['f' + im.groupID].domLayer, {
        //     x: im.x,
        //     y: im.y,
        //     domHtml: html,
        // });

    } else {
        dm = newAddDomMarkerToMap(target.floor, {
            x: target.x,
            y: target.y,
            domHtml: html,
        });
    }
    target.dm = dm
}

//添加dom模型
function addFMDomMarker(domLayer, para) {
    var dm = new fengmap.FMDomMarker({
        x: para.x + 0.5,
        y: para.y,
        height: para.z || 2,
        //没必要传
        //domWidth: para.width || 20,
        //domHeight: para.high || 24,
        domContent: para.domHtml,
        anchor: fengmap.FMMarkerAnchor.LEFT_TOP,
    });
    domLayer.addMarker(dm);
    return dm;
}

//隐藏Dm
function hideDm(id, type) {
    var target = null;
    if (type === 'base') {
        target = contrastReturn(beaconList, 'id', id);
    } else if (type === 'detector') {
        target = contrastReturn(detectorList, 'id', id);
    } else if (type === 'gateway') {
        target = contrastReturn(gatewayList, 'id', id);
    } else if (type === 'user') {
        target = contrastReturn(userList, 'id', id);
    }
    if (target) {
        //target.dm.show = false;
        //移除，跨层时会显示

        target.dm.remove();
    }
}

//获取marker
function getAllMarker() {
    var list = ['base', 'detector', 'gateway'];
    for (var i = 0; i < list.length; i++) {
        getAll(list[i]);
    }
}

//请求数据
function getAll(type) {
    var path = '';
    if (type === 'base') {
        path = 'bsconfig/getBsConfigSel';
    } else if (type === 'detector') {
        path = 'infrared/getInfraredSel';
    } else if (type === 'gateway') {
        path = 'gateway_lora/getGatewaySel';
    } else {
        tips('未知错误');
        return;
    }
    $.ajax({
        url: url + path,
        data: {
            map: mapId,
            pageSize: -1
        },
        success: function (res) {
            var data = res.data || [];
            var len = data.length;
            if (res.code != 200 || !len) {
                return;
            }
            drawMarker(data, type);
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}

//绘制marker并记录信息
function drawMarker(list, type) {
    var target = null;
    var len = list.length;
    var em = null;
    var tm = null;
    var curLayer = null;
    var x;
    var y;
    var floor;
    var targetList = null;

    var markerUrl = '';
    var markerColor = '';
    if (type === 'base') {
        targetList = beaconList;
        markerUrl = 'beacon-gray';
        markerColor = 'B3B3B3';
    } else if (type === 'detector') {
        targetList = detectorList;
        markerUrl = 'detector';
        markerColor = '00eb0b';
    } else if (type === 'gateway') {
        targetList = gatewayList;
        markerUrl = 'gateway';
        markerColor = '03f6ff';
    }
    for (var i = 0; i < len; i++) {
        target = list[i];
        if (target.x == undefined || target.y == undefined) {
            continue;
        }
        if (isNaN(target.x) || isNaN(target.y)) {
            continue;
        }
        floor = +target.floor;
        // curLayer = fMapLayer['f' + floor];
        // if (!curLayer) {
        //     //找不到对应的楼层
        //     return;
        // }
        x = target.x;
        y = target.y
        // im = addFMImageMarker(curLayer.imgLayer, {
        //     x: x,
        //     y: y,
        //     url: '../image/common/' + markerUrl + '.png',
        //     size: 22,
        //     height: 2,
        // });
        // tm = addTextMarker(curLayer.textLayer, {
        //     x: x,
        //     y: y + 0.5,
        //     height: 3,
        //     name: target.num,
        //     fillcolor: '#' + markerColor,
        //     fontsize: 16,
        // });
        im = newAddImgMarkerToMap(floor, {
            x: x,
            y: y,
            url: '../image/common/' + markerUrl + '.png',
            size: 22,
            height: 2,
        });
        tm = newAddTextMarkerToMap(floor, {
            x: x,
            y: y + 0.5,
            height: 3,
            name: target.num,
            fillcolor: '#' + markerColor,
            fontsize: 16,
        });



        tm.show = false;
        im.show = false;
        //自定义属性
        im.selfId = target.id;
        im.selfType = type;
        tm.selfId = target.id;
        tm.selfType = type;

        targetList.push({
            x: x,
            y: y,
            name: target.num,
            state: false,
            floor: floor,
            type: type,
            id: target.id,
            im: im,
            tm: tm,
            //显示的数据
            power: target.power || '',
            networkstate: target.networkstate,
        });
    }
}

//获取车位
function getAllPlace() {
    $.ajax({
        url: url + 'park/getPlace',
        data: {
            map: mapId,
            // state: 1,
            pageSize: -1,
        },
        success: function (res) {
            if (res.code != 200) {
                return;
            }
            var list = res.data;
            var target = null;
            var model = null;

            // list.length
            for (var i = 0; i < list.length; i++) {
                target = list[i];
                if (!target.fid) {
                    continue;
                }

                setPlaceColor([target.fid], resStateColor(target.state));
            }
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}

//根据fid改变状态
function changeModelRender(data, fid, state) {
    var fid = data.fid;
    var state = data.state;
    setPlaceColor([fid], resStateColor(state));

}

//返回状态相对应的颜色
function resStateColor(state) {
    switch (state) {
        case 0:
            return '#1BC736';
        case 1:
            //已停
            return '';
        // return '#FFDFAA';
        // case 2:
        //     //预约
        //     return '#cdf408';
        default:
            return '';
    }
}

//添加初始化的点
function initDetail() {
    var target = null;
    var html = '';
    for (var i = 0; i < detailList.length; i++) {
        target = detailList[i];
        html += '<tr><td class="order">' + tablen + '</td>';
        html += '<td class="user"><div class="checkbox detail-item" onclick="checkDetail(this,\'' + target.id + '\',\'' + tablen + '\')">';
        html += '<div class="checked-radiu"></div></div>';
        html += '<div class="user-text moreTextHide">' + '微信' + tablen + '</div></tr>';
        // html += '<div class="user-text moreTextHide">' + target.niceName + '</div></tr>';
        tablen++
    }
    $('#realtimeTbody').append(html);
}

function checkDetail(that, userid, index) {
    if (naviTarget) {
        if (navigation) {
            naviTarget.stop();
        }
        naviTarget.clearAll();
        naviTarget.dispose();
        naviTarget = null;
        if (detailImgMarket) {
            detailImgMarket.remove();
        }
    };
    var className = 'current-check';
    var _that = $(that);
    if (_that.hasClass(className)) {
        _that.removeClass(className);
    } else {
        $('.detail-item.' + className).removeClass(className);
        _that.addClass(className)
        setTimeout(function () {
            showDetail(userid);
        }, 100)
    };

    if (!oldUserId) {
        oldUserId = '';
    } else {
        if (oldUserId != userid) {
            var resultOld = contrastReturn(userList, 'id', oldUserId);

            var imOld = resultOld.im;
            var tmOld = resultOld.tm;
            var dmOld = resultOld.dm;
            imOld.show = false;
            tmOld.show = false;
            if (dmOld && dmOld.show) {
                dmOld.show = false;
            }
            removeNaviRoute(oldUserId);
            resultOld.hasNaviLine = false;
            if (resultOld.type === 'history') {
                if (historyTimer) {
                    clearTimeout(historyTimer);
                    historyTimer = null;
                }
            };
            resultOld.checked = false;
            oldUserId = '';
        }
    }
    detailList.forEach((ditem) => {
        if (ditem.id == userid) {
            ditem.checked = !ditem.checked;
        } else {
            ditem.checked = false;
        }
    });
    var result = contrastReturn(detailList, 'id', userid);

    // 设置选中的用户详情
    showRealtimeDetail(result, result.checked, index);
}

//显示默认导航的信息
function showDetail(userid) {
    var result = contrastReturn(detailList, 'id', userid);
    if (!result) {
        console.log('找不到相对应的默认用户');
        return;
    }
    createDetailNavi(result.start, result.end);
}

//创建导航
function createDetailNavi(start, end) {
    if (!naviTarget) {
        //初始化导航对象

        let nnaviAnalyser = new fengmap.FMNaviAnalyser({ map: fMap }, function () {
            if (naviAnalyser) {
                naviTarget = new fengmap.FMNavigation({
                    map: fMap,
                    analyser: nnaviAnalyser,
                    //导航图标
                    locationMarkerUrl: '../image/park/location.png',
                    // locationMarkerUrl: '../image/fMap/location.png',
                    //设置Marker尺寸
                    locationMarkerSize: 0,
                });

                newcreateDetailNavi(start, end);
            }
        });


    } else {
        newcreateDetailNavi(start, end);
    }

}

var detailImgMarket;
function newcreateDetailNavi(start, end) {
    //添加起点
    naviTarget.setStartPoint({
        x: start.x,
        y: start.y,
        level: start.groupID,
        url: '../image/fMap/start.png',
        size: 32,
        collision: true
    });

    //添加终点
    naviTarget.setDestPoint({
        x: end.x,
        y: end.y,
        level: end.groupID,
        url: '../image/fMap/end.png',
        size: 32
    });





    let nowleve = fMap.getLevel();
    if (start.groupID != nowleve) {
        fMap.setLevel({
            level: start.groupID,
            animate: false,
        })
    };
    fMap.setCenter({
        x: start.x,
        y: start.y,
        animate: false,
    });



    // 导航分析
    naviTarget.route(
        {
            mode: fengmap.FMNaviMode.MODULE_BEST,
            priority: fengmap.FMNaviPriority.PRIORITY_DEFAULT
        },
        function (result) {
            // 导航分析成功回调
            var line = naviTarget.drawNaviLine();

            // 使用红色用户图标
            if (detailImgMarket) {
                detailImgMarket.remove();
            }
            detailImgMarket = newAddImgMarkerToMap(start.groupID, {
                x: start.x,
                y: start.y,
                url: '../image/fMap/mapUserRed.png',
                size: 24,
                height: 2,
            });
            detailImgMarket.visible = true;

            // 自适应路线全览
            naviTarget.overview({
                ratio: 1.5
            }, function () {
                console.log('自适应全览动画结束回调');
                // 开始模拟导航
                startImitateNavi();
                navigation = true
            });
        }, function (result) {
            // 导航分析失败回调
            console.log("failed", result);
        }
    );
    // 导航过程中位置发生变化时触发的事件, 模拟导航与真实导航调用locate后都都会触发。
    naviTarget.on('walking', function (data) {
        // 更新路线信息
        let point = data.point;
        if (detailImgMarket) {
            // 跨层
            if (detailImgMarket.level != point.level) {
                detailImgMarket.remove();
                detailImgMarket = newAddImgMarkerToMap(point.level, {
                    x: point.x,
                    y: point.y,
                    url: '../image/fMap/mapUserRed.png',
                    size: 24,
                    height: 2,
                });
                detailImgMarket.visible = true;
            } else {
                detailImgMarket.moveTo({
                    x: point.x,
                    y: point.y,
                })
            }
        }
    })
    // 模拟导航结束事件, 真实导航不会触发该事件。需要开发者根据driving事件的返回内容, 进行业务处理。
    naviTarget.on('complete', function (data) {
        console.log('导航结束！');
        // navigation = false;
        // 重新开始模拟导航
        startImitateNavi();
    })
};

// 开始模拟导航
function startImitateNavi() {
    naviTarget.simulate({
        speed: 7,               // 模拟导航定位图标行进的速度, 单位m/s。默认7m/s。
        followPosition: true,   // 模拟导航中在位置发生变化时, 地图是否跟随位置居中显示。默认true。
        followAngle: false,     // 模拟导航中在方向发生变化时, 地图是否跟随方向变化, 保持路线朝向屏幕上方。 默认false。
        changeTiltAngle: true,  // 模拟导航中楼层发生变化时是否改变地图的倾斜角度, 并执行动画。默认为true。
    });
};

//定义路径规划对象
var naviAnalyser = null;
var naviList = [];//导航信息列表
//创建导航信息
function createNavi(id, start, end) {

    if (!start.x && !start.y && !start.groupID) {
        return false
    }
    //创建导航信息
    if (!naviTarget) {
        //初始化导航对象

        let nnaviAnalyser = new fengmap.FMNaviAnalyser({ map: fMap }, function () {
            if (naviAnalyser) {
                naviTarget = new fengmap.FMNavigation({
                    map: fMap,
                    analyser: nnaviAnalyser,
                    //导航图标
                    locationMarkerUrl: '../image/park/location.png',
                    // locationMarkerUrl: '../image/fMap/location.png',
                    //设置Marker尺寸
                    locationMarkerSize: 0,
                });

                createUserNaviObj(id, start, end);
            }
        });


    } else {
        createUserNaviObj(id, start, end);
    }
}

// 用户导航
function createUserNaviObj(id, start, end) {
    //添加起点
    naviTarget.setStartPoint({
        x: start.x,
        y: start.y,
        level: start.groupID,
        url: '../image/fMap/start.png',
        size: 32,
        collision: true
    });

    //添加终点
    naviTarget.setDestPoint({
        x: end.x,
        y: end.y,
        level: end.groupID,
        url: '../image/fMap/end.png',
        size: 32
    });

    let nowleve = fMap.getLevel();

    if (start.groupID != nowleve) {
        fMap.setLevel({
            level: start.groupID,
            animate: false,
        })
    };
    fMap.setCenter({
        x: start.x,
        y: start.y,
        animate: false,
    });



    // 导航分析
    naviTarget.route(
        {
            mode: fengmap.FMNaviMode.MODULE_BEST,
            priority: fengmap.FMNaviPriority.PRIORITY_DEFAULT
        },
        function (result) {
            // 导航分析成功回调
            naviTarget.drawNaviLine();
            // 自适应路线全览
            naviTarget.overview({
                ratio: 1.5
            }, function () {

            });
        }, function (result) {
            // 导航分析失败回调
            console.log("failed", result);
        }
    );
    // 导航过程中位置发生变化时触发的事件, 模拟导航与真实导航调用locate后都都会触发。
    naviTarget.on('walking', function (data) {
        // 更新路线信息
        // updateUI(data);
    })
}

function removeNaviRoute(id) {
    // var target = null;
    // var flag = false;
    // for (var i = 0; i < naviList.length; i++) {
    //     target = naviList[i];
    //     if (target.id == id) {
    //         flag = true;
    //         break;
    //     }
    // }
    // if (!flag) {
    //     console.log('找不到相对应的导航路线');
    //     return;
    // }
    // //清空导航线
    // fMap.removeLineMarker(target.line);
    // //清空起点、终点marker
    // fMapLayer['f' + target.sIm.groupID].imgLayer.removeMarker(target.sIm);
    // fMapLayer['f' + target.eIm.groupID].imgLayer.removeMarker(target.eIm);
    // naviList.splice(i, 1);

    if (naviTarget) {
        naviTarget.clearAll();
        naviTarget.dispose();
        naviTarget = null;
    }
}

//收费统计
function chargeStatistics(target) {
    $('#fee').html(target.fee);
    $('#totalCount').html(target.totalCount);
    $('#averageStaytime').html(target.averageStaytime);
    $('#monthlyRentCount').html(target.monthlyRentCount);
}

//停车场出入数据
function inAndOutData(target) {
    var html = '<tr>';
    html += '<td>' + target.license + '</td>';
    html += '<td>' + (target.fee || '') + '</td>';
    html += '<td>' + target.inTime + '</td>';
    html += '<td>' + target.outTime + '</td>';
    html += '</tr>';
    $('#violation').prepend(html);
}

function getHistory() {
    $.ajax({
        url: url + 'park/getTrailRecordByMap',
        data: {
            map: mapId,
        },
        success: function (res) {
            if (res.code !== 200) {
                return;
            }

            var data = res.data;
            var len = data.length;
            if (!len) {
                return;
            }
            var target = null;
            var list = null;
            var listTarget = null;
            var result = [];
            var resultList = [];
            var start = null;
            var end = null;
            for (var i = 0; i < len; i++) {
                target = data[i];
                list = target.list;
                resultList = [];
                for (var j = 0; j < list.length; j++) {
                    listTarget = list[j];
                    resultList.push({
                        x: +listTarget.x,
                        y: +listTarget.y,
                        floor: +listTarget.floor,
                    })
                }
                start = resultList[0];
                end = resultList[j - 1];
                result.push({
                    x: start.x,
                    y: start.y,

                    startX: start.x,
                    startY: start.y,
                    startFloor: start.floor,
                    endX: end.x,
                    endY: end.y,
                    endFloor: end.floor,

                    floor: start.floor,

                    userid: target.id,
                    type: 'history',
                    target: true,
                    niceName: target.name,
                })
                historyList.push({
                    id: target.id,
                    list: resultList,
                })
            }
            fMapPos(result);
        }
    })
}

//播放历史
function playHistory(item) {
    var result = contrastReturn(historyList, 'id', item.id);
    if (!result) {
        console.log('找不到相对应的点');
        return;
    }
    playItem = {
        im: item.im,
        tm: item.tm,
        //dm:item.dm,
    }
    playList = result.list;
    playIndex = 0;
    changeCoord();
}

//播放
function changeCoord() {
    clearTimeout(historyTimer);
    //定时器
    historyTimer = setTimeout(function () {
        if (playIndex >= playList.length) {
            clearTimeout(historyTimer);
            historyTimer = null;
            return;
        }
        setLocationMakerPosition(playItem, playList[playIndex], true);
        playIndex++;
        changeCoord();
    }, 300);
}

var userNameIndex;
// 显示用户详情
function showRealtimeDetail(userData, checked, index) {


    userNameIndex = index

    if (checked) {
        let name = '微信' + index;
        $("#rDetailHeaderName").html(name);

        let type = userData.nav_state || '自由行走';
        $("#rDetailInfoType").html(type);

        let end = userData.target || '--';
        $("#rDetailInfoEnd").html(end);


        $("#realtimeDetail").css('display', 'block')

    } else {
        $("#realtimeDetail").css('display', 'none')

    }
}

// 更新地图上的用户位置
function updateMapUsersPoint(data) {
    mapUserList.forEach((item) => {
        if (item.userid == data.userid) {
            // item.imgMarker.setPosition(data.x, data.y, data.floor, 2)
            if (item.imgMarker.level != (data.floor != 0 ? data.floor : data.startFloor)) {
                // 跨层
                let isvisible = item.imgMarker.visible;
                let pFloor = data.floor != 0 ? data.floor : data.startFloor
                item.imgMarker.remove();
                item.imgMarker = newAddImgMarkerToMap(pFloor, {
                    x: data.x,
                    y: data.y,
                    url: '../image/fMap/mapUserBlue.png',
                    size: 24,
                    height: 2,
                });
                item.imgMarker.visible = isvisible;
            } else {
                item.imgMarker.moveTo({
                    x: +data.x,
                    y: +data.y
                })
            }
        }
    })
};

// 选中用户
function setMapUsersListColor(data) {
    let uid = data.id;
    let checked = data.checked;
    mapUserList.forEach((item) => {
        if (item.userid == uid && checked) {
            item.imgMarker.visible = false
        } else {
            item.imgMarker.visible = true
        }
    })
};

// 车位实时数据
function updateEnter(allEnterData, data) {
    var result = JSON.parse(JSON.stringify(allEnterData));

    let hasData = result.some((item) => item.parkingLot == data.parkingLot && item.carbit == data.carbit);

    if (hasData) {
        result.forEach((item, index) => {
            if (item.parkingLot == data.parkingLot && item.carbit == data.carbit) {
                result.splice(index, 1)
            }
        });
        result.unshift(data);
    } else {
        result.pop();
        result.unshift(data);
    }
    allEnter = result;

    initEnter(allEnter);
}

function initEnter(data) {
    var carContent = document.getElementById("carContent");
    carContent.innerHTML = "";
    var htmlStr = "<ul>";
    data.forEach(item => {
        let str1 = item.carbit ? item.carbit.substring(0, item.carbit.length - 2) : '';
        let str2 = item.carbit ? item.carbit.substring(item.carbit.length - 2, item.carbit.length) : '';
        let classStr = str2 === '空闲' ? 'green' : str2 === '占用' ? 'orange' : '';
        htmlStr += "<li><span>" + item.time + "</span><span>" + item.parkingLot + "</span><span>" + str1 + ' <i class=\'' + classStr + '\'>' + str2 + "</i></span>";
    });
    htmlStr += "</ul>";
    carContent.innerHTML = htmlStr;
    $('.scrollDiv').liMarquee({
        direction: 'up',//身上滚动 
        runshort: false,//内容不足时不滚动
        scrollamount: 16//速度
    });
}

var hotSettimeoutData = null;
// 热门地点
function initPopularLocations() {
    $.ajax({
        url: url + 'mapHotspotData/getHotSearchByMap',
        type: 'get',
        data: {
            map: mapId,
            time: timeData.time
        },
        success: function (res) {
            if (res.data.length) {
                res.data.sort((a, b) => {
                    return b.score - a.score
                });

                res.data.length = 10;
                let html = '';
                res.data.forEach((item, index) => {
                    if (index == 0) {
                        html += `
                        <div class="tableUl">
                            <div class="tableLi1">
                                <img src="../image/common/ranking1.png" alt="" srcset="">
                            </div>
                            <div class="tableLi2">
                                <div class="tableLi2d">
                                    <span>${item.name}</span>
                                </div>
                            </div>
                            <div class="tableLi3">${item.score}</div>
                        </div>
                        `
                    } else if (index == 1) {
                        html += `
                        <div class="tableUl">
                            <div class="tableLi1">
                                <img src="../image/common/ranking2.png" alt="" srcset="">
                            </div>
                            <div class="tableLi2">
                                <div class="tableLi2d">
                                    <span>${item.name}</span>
                                </div>
                            </div>
                            <div class="tableLi3">${item.score}</div>
                        </div>
                        `
                    } else if (index == 2) {
                        html += `
                        <div class="tableUl">
                            <div class="tableLi1">
                                <img src="../image/common/ranking3.png" alt="" srcset="">
                            </div>
                            <div class="tableLi2">
                                <div class="tableLi2d">
                                    <span>${item.name}</span>
                                </div>
                            </div>
                            <div class="tableLi3">${item.score}</div>
                        </div>
                        `
                    } else {
                        html += `
                        <div class="tableUl">
                            <div class="tableLi1">${index + 1}</div>
                            <div class="tableLi2">
                                <div class="tableLi2d">
                                    <span>${item.name}</span>
                                </div>
                            </div>
                            <div class="tableLi3">${item.score}</div>
                        </div>
                        `
                    }
                });
                $("#parkingSpaceUtilizationRate").html(html)

            } else {

            }
        },
        error: function (err) {
            console.log(err);
        }
    });

    if (hotSettimeoutData) {
        clearTimeout(hotSettimeoutData);
        hotSettimeoutData = null;
    };

    // 显示一次后定时
    hotSettimeoutData = setTimeout(() => {
        initPopularLocations();
    }, 12 * 3600 * 1000);
};



// 添加图片
function newAddImgMarkerToMap(level, data) {
    var im = new fengmap.FMImageMarker({
        x: data.x,
        y: data.y,
        url: data.url,
        size: data.size,
        height: data.height,
        anchor: fengmap.FMMarkerAnchor.BOTTOM,
        collision: false
    });
    var floor = fMap.getFloor(level);
    im.addTo(floor);
    im.visible = false;
    return im;
};

// 添加文字
function newAddTextMarkerToMap(level, data) {
    let textMarker = new fengmap.FMTextMarker({
        height: data.height,
        fontFamily: '微软雅黑',
        fillColor: data.fillcolor,
        strokeWidth: 1,
        strokeColor: '#fff',
        anchor: 'CENTER',
        fontSize: data.fontsize,
        depth: true,
        collision: false,
        text: data.name,
        x: +data.x,
        y: +data.y,
    });
    var floor = fMap.getFloor(level);
    textMarker.addTo(floor);
    textMarker.visible = false;
    return textMarker
}

// dom覆盖物
function newAddDomMarkerToMap(level, data) {
    let domMarker = new fengmap.FMDomMarker({
        x: data.x,
        y: data.y,
        domWidth: '184px',
        anchor: fengmap.FMMarkerAnchor.BOTTOM,
        content: data.domHtml
    });

    var floor = fMap.getFloor(level);
    domMarker.addTo(floor);
    return domMarker
};

/* 下拉选择 */
function openSelect() {
    let hae = $(".selectUl").hasClass('showSelect');
    if (hae) {
        $(".selectUl").removeClass('showSelect')
    } else {
        $(".selectUl").addClass('showSelect')
    }
};


// api调用
function apiGetDataState() {
    return false;
    // 车位导航总数
    getPlaceNavigationTotal();
    // 车位导航使用率
    getPlaceNavigationUseRate();
    // 平台车位利用率
    getPlatformPlaceUtilizationRate();
    // 车位预约总数
    getReservationTotal();
    // 反向寻车总数
    getReverseCarSearchTotal();
    // 停车场车位空闲率
    getPlaceAvailabilityRate();

    // 每小时空车位数
    getPerHourNullPlaceNumber();

    // 车位使用次数
    getPlaceUseTotal();
    // 车位空闲总时长
    getPlaceldleTotalDuration();

    // 用户总数
    getMapUsersTotal();
    // 访问总次数
    getCumulativeUseFrequency();
    // 活跃用户数
    getActiveUserNumber();
    // 新增用户数
    getWithinThreeMonthsNewUsers();
    // 用户搜索总数
    getUserSearchTotal();
    // 位置分享总数
    getLocationShareTotal();

    // 用户检索前10商家
    getTop10Business();

    // 热门地点
    initPopularLocations();

    // 车位数
    getDeviceCount();
};

// 车位导航总数
function getPlaceNavigationTotal() {
    $.ajax({
        url: url + 'view/getPlaceNavigationTotal',
        data: timeData,
        success: function (res) {
            console.log('车位导航总数', res);
            if (res.code != 200) {
                return;
            };

            let placeNavigationTotal = 0;
            res.data.forEach((item) => {
                placeNavigationTotal += +item.placeNavigationTotal
            });
            $("#carParkTotal").html(placeNavigationTotal);


        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

// 车位导航使用率
function getPlaceNavigationUseRate() {
    $.ajax({
        url: url + 'view/getPlaceNavigationUseRate',
        data: timeData,
        success: function (res) {
            console.log('车位导航使用率', res);
            if (res.code != 200) {
                return;
            };
            if (!res.data.length) return;
            let placeNavigationTotal = 0;
            res.data.forEach((item) => {
                placeNavigationTotal += +item.placeNavigationUseRate || 0
            });

            placeNavigationTotal = (placeNavigationTotal * 100).toFixed(2);

            $("#carParkNaviga").html(placeNavigationTotal + '%')
            $("#carParkNaviga").attr('title', placeNavigationTotal + '%')
            $("#carParkNavigaH").css('height', placeNavigationTotal >= 100 ? 100 : placeNavigationTotal + '%')


        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

/* 平台车位利用率 */
function getPlatformPlaceUtilizationRate() {
    $.ajax({
        url: url + 'view/getPlatformPlaceUtilizationRate',
        data: timeData,
        success: function (res) {
            console.log('平台车位利用率', res);
            if (res.code != 200) {
                return;
            };

            let platformUtilizationRate = 0;
            res.data.forEach((item) => {
                platformUtilizationRate += +item.platformUtilizationRate
            });

            platformUtilizationRate = platformUtilizationRate.toFixed(2);

            $("#carParkUsage").html(platformUtilizationRate + '%');
            $("#carParkUsage").attr('title', platformUtilizationRate + '%');
            $("#carParkUsageH").css('height', (platformUtilizationRate >= 100 ? 100 : platformUtilizationRate) + '%');


        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

/* 车位预约总数 */
function getReservationTotal() {
    $.ajax({
        url: url + 'view/getReservationTotal',
        data: timeData,
        success: function (res) {
            console.log('车位预约总数', res);
            if (res.code != 200) {
                return;
            };
            let allTotal = 0;
            res.data.forEach((item) => {
                allTotal += +item.reservationTotal
            });
            $("#carParkReserva").html(allTotal)

        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

// 反向寻车总数
function getReverseCarSearchTotal() {
    $.ajax({
        url: url + 'view/getReverseCarSearchTotal',
        data: timeData,
        success: function (res) {
            console.log('反向寻车总数', res);
            if (res.code != 200) {
                return;
            };

            let reverseCarSearchTotal = 0;
            res.data.forEach((item) => {
                reverseCarSearchTotal += +item.reverseCarSearchTotal
            });
            $("#carParkFindCar").html(reverseCarSearchTotal)


        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

/* 停车场车位空闲率 */
function getPlaceAvailabilityRate() {
    $.ajax({
        url: url + 'view/getPlaceAvailabilityRate',
        data: timeData,
        success: function (res) {
            console.log('停车场车位空闲率', res);
            if (res.code != 200) {
                return;
            };

            let placeAvailabilityRate = 0;
            res.data.forEach((item) => {
                placeAvailabilityRate += +item.placeAvailabilityRate
            });

            placeAvailabilityRate = placeAvailabilityRate.toFixed(2)

            $("#carParkIdleRate").html(placeAvailabilityRate + '%');
            $("#carParkIdleRate").attr('title', placeAvailabilityRate + '%');
            $("#carParkIdleRateH").css('height', (placeAvailabilityRate >= 100 ? 100 : placeAvailabilityRate) + '%');

        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

// 每小时空车位数
function getPerHourNullPlaceNumber() {
    $.ajax({
        url: url + 'view/getPerHourNullPlaceNumber',
        data: timeData,
        success: function (res) {
            console.log('每小时空车位数', res);
            if (res.code != 200) {
                return;
            };
            if (res.data.length == 0) {
                return;
            };

            let arrH = [];
            let arrH2 = [];
            let arrNum = [];
            let revData = res.data.reverse();
            revData.forEach((item) => {
                // arrH.push(item.hour);
                arrH.push(item.hour.split(" ")[1]);
                arrH2.push(item.hour);
                arrNum.push(item.nullPlaceNumber);
            });

            var emptyParkSpaces = document.getElementById('emptyParkSpaces');
            var myZhe = echarts.init(emptyParkSpaces);
            var option = {
                tooltip: {
                    trigger: 'item',
                    // extraCssText: 'padding: 8px 10px 4px; line-height: 1.5; font-size: 14px;background-color:rgba(77,81,255,0.17); color:#05C7FF;text-align: center;',
                    extraCssText: 'padding: 8px 10px 4px; line-height: 1.5; font-size: 14px;text-align: center;',
                    position: "top",
                    formatter: function (params) {
                        let str = arrH2[params.dataIndex] + '</br>' + "空车位数:" + params.value;
                        // let str = params.name + '</br>' + "空车位数:" + params.value;
                        if (params) {
                            str += "<span style='display:inline-block;width:10px;height:10px;border-radius:10px;></span>"
                        }
                        return str
                    },
                },
                xAxis: {
                    type: 'category',
                    name: '(小时)',
                    data: arrH,
                    axisLine: {
                        show: false,
                        lineStyle: {
                            color: '#B8D3F1'
                        }
                    },
                    axisTick: {
                        show: false
                    },
                    splitLine: {
                        show: true,
                        lineStyle: {
                            color: '#385982',
                            cap: 'square',
                            opacity: 0.3
                        },
                    }
                },
                yAxis: {
                    type: 'value',
                    name: '(空车位数)',
                    axisLine: {
                        lineStyle: {
                            color: '#B8D3F1'
                        }
                    },
                    splitLine: {
                        lineStyle: {
                            color: '#385982',
                            cap: 'square',
                            opacity: 0.3
                        }
                    }
                },
                series: [
                    {
                        data: arrNum,
                        type: 'line',
                        smooth: true,
                        lineStyle: {
                            color: '#10C4FF'
                        },
                        itemStyle: {
                            color: '#10C4FF'
                        }
                    }
                ],
                grid: {
                    top: '18%',
                    bottom: '0%',
                    left: '3%',
                    right: '2%',
                    width: '85%',
                    containLabel: true,
                    borderColor: '#385982'
                }
            };
            myZhe.setOption(option);
            myZhe.resize();
            window.addEventListener("resize", function () {
                myZhe.resize();
            });


        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

// 车位使用次数
function getPlaceUseTotal() {
    $.ajax({
        url: url + 'view/getPlaceUseTotal',
        data: timeData,
        success: function (res) {
            console.log('车位使用次数总数', res);
            if (res.code != 200) {
                return;
            };
            if (!res.data.length) return;

            res.data.sort((a, b) => {
                return b.placeUseTotal - a.placeUseTotal
            });


            res.data.length = 10;

            let dataTotal = 0;
            let xArr = [];
            let yArr = [];
            res.data.forEach((item) => {
                dataTotal += +item.placeUseTotal;
                xArr.push(item.placeName || item.name);
                yArr.push(((item.placeUseTotal) / 10000).toFixed(2));
            });
            dataTotal = dataTotal / 10000;
            $("#carUseBarNum").html((dataTotal.toFixed(2)) + '万');
            initBar3("carUseBar", {
                xValue: xArr,
                yValue: yArr,
                color: ['#B6A4FF', '#5530E9']
            });

        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

// 车位空闲总时长
function getPlaceldleTotalDuration() {
    $.ajax({
        url: url + 'view/getPlaceIdleTotalDuration',
        data: timeData,
        success: function (res) {
            console.log('车位空闲总时长', res);
            if (res.code != 200) {
                return;
            };
            if (!res.data.length) return;

            // res.data.forEach((item) => {
            //     let findItem = parkadeInfo.find((pitem) => pitem.map == item.map);
            //     if (findItem) {
            //         item.mapName = findItem.mapName
            //     }
            // });

            let xArr = [];
            let yArr = [];

            res.data.sort((a, b) => {
                return b.totalVacantDuration - a.totalVacantDuration
            });

            res.data.forEach((item) => {
                xArr.push(item.placeName || item.name);
                yArr.push(((item.totalVacantDuration / 60 / 60) / 10000).toFixed(2))
            });


            initBar2("spacesTotalTime", {
                xValue: xArr,
                yValue: yArr,
                color: ['#00F6FF', '#002DD3']
            });

        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

// 地图用户总数
function getMapUsersTotal() {
    $.ajax({
        url: url + 'view/getMapUsersTotal',
        data: timeData,
        success: function (res) {
            console.log('地图用户总数', res);
            if (res.code != 200) {
                return;
            };

            let activeUsers = 0;
            res.data.forEach((item) => {
                activeUsers += +item.userTotal || 0
            });
            $("#userTotal").html(activeUsers);

        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

// 访问总次数
function getCumulativeUseFrequency() {
    $.ajax({
        url: url + 'view/getCumulativeUseFrequency',
        data: timeData,
        success: function (res) {
            console.log('访问总次数', res);
            if (res.code != 200) {
                return;
            };

            let activeUsers = 0;
            res.data.forEach((item) => {
                activeUsers += +item.useFrequency || 0
            });
            $("#visits").html(activeUsers);

        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

// 月活跃用户数
function getActiveUserNumber() {
    $.ajax({
        url: url + 'view/getActiveUserNumber',
        data: timeData,
        success: function (res) {
            console.log('月活跃用户数', res);
            if (res.code != 200) {
                return;
            };
            if (!res.data.length) return;

            let activeUsers = 0;
            var arrNum = [];
            var arrMonth = [];
            res.data.forEach((item) => {
                activeUsers += +item.activeUsers || 0;

                arrNum.push(item.activeUsers);
                var str = item.perMonth.split('-')[1];
                var s = str.split('');
                if (s[0] === '0') {
                    str = s[1];
                }
                arrMonth.push(str + '月')
            });

            $("#usersActive").html(activeUsers);



            var monthZhe = document.getElementById('monthZhe');
            var myZhe = echarts.init(monthZhe);
            var option = {
                tooltip: {
                    trigger: 'item',
                    extraCssText: 'padding: 8px 10px 4px; line-height: 1.5; font-size: 14px;text-align: center;',
                    // extraCssText: 'padding: 8px 10px 4px; line-height: 1.5; font-size: 14px;background-color:rgba(77,81,255,0.17); color:#05C7FF; text-align: center;',
                    position: ['40%', '30%'],
                    formatter: function (params) {
                        let str = params.name + "月活用户数<br/>" + params.value + "人";
                        if (params) {
                            str += "<span style='display:inline-block;width:10px;height:10px;border-radius:10px;></span>"
                        }
                        return str
                    },
                },
                xAxis: {
                    type: 'category',
                    data: arrMonth,
                    axisLine: {
                        show: false,
                        lineStyle: {
                            color: '#B8D3F1'
                        }
                    },
                    axisTick: {
                        show: false
                    },
                    splitLine: {
                        show: true,
                        lineStyle: {
                            color: '#385982',
                            cap: 'square',
                            opacity: 0.3
                        },
                    }
                },
                yAxis: {
                    type: 'value',
                    name: '(人)',
                    axisLine: {
                        lineStyle: {
                            color: '#B8D3F1'
                        }
                    },
                    splitLine: {
                        lineStyle: {
                            color: '#385982',
                            cap: 'square',
                            opacity: 0.3
                        }
                    }
                },
                series: [
                    {
                        data: arrNum,
                        type: 'line',
                        lineStyle: {
                            color: '#10C4FF'
                        },
                        itemStyle: {
                            color: '#10C4FF'
                        },
                        areaStyle: {
                            color: {
                                type: 'linear',
                                x: 0,
                                y: 0,
                                x2: 0,
                                y2: 1,
                                colorStops: [{
                                    offset: 0, color: '#1E7EEA'
                                }, {
                                    offset: 1, color: '#040F28'
                                }],
                                global: false
                            }
                        },

                    }
                ],
                grid: {
                    top: '18%',
                    bottom: '0%',
                    left: '3%',
                    width: '100%',
                    containLabel: true,
                    borderColor: '#385982'
                }
            };
            myZhe.setOption(option);
            window.addEventListener("resize", function () {
                myZhe.resize();
            });

        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

/* 新增用户数 */
function getWithinThreeMonthsNewUsers() {
    $.ajax({
        url: url + 'view/getWithinThreeMonthsNewUsers',
        data: timeData,
        success: function (res) {
            console.log('新增用户数', res);
            if (res.code != 200) {
                return;
            }

            if (res.data.length) {
                let data = res.data[0];
                let newUserTotal = data.newUserTotal
                $("#usersNew").html(newUserTotal)
            }

        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

// 用户搜索总数
function getUserSearchTotal() {
    $.ajax({
        url: url + 'view/getUserSearchTotal',
        data: timeData,
        success: function (res) {
            console.log('用户搜索总数', res);
            if (res.code != 200) {
                return;
            };

            let userSearchTotal = 0;
            res.data.forEach((item) => {
                userSearchTotal += +item.userSearchTotal
            });
            $("#usersSearchesTotal").html(userSearchTotal);

        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

// 位置分享总数
function getLocationShareTotal() {
    $.ajax({
        url: url + 'view/getLocationShareTotal',
        data: timeData,
        success: function (res) {
            console.log('位置分享总数', res);
            if (res.code != 200) {
                return;
            };

            let locationShareTotal = 0;
            res.data.forEach((item) => {
                locationShareTotal += +item.locationShareTotal
            });
            $("#sharesTotal").html(locationShareTotal)


        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

/* 用户检索前10商家 */
function getTop10Business() {
    $.ajax({
        url: url + 'view/getTop10Business',
        data: timeData,
        success: function (res) {
            console.log('用户检索前10停车场', res);
            if (res.code != 200) {
                return;
            };
            if (!res.data.length) return;

            let html = '';
            res.data.forEach((item, index) => {
                if (index == 0) {
                    html += `
                    <div class="tableUl">
                        <div class="tableLi1">
                            <img src="../image/common/ranking1.png" alt="" srcset="">
                        </div>
                        <div class="tableLi2">
                            <div class="tableLi2d">
                                <span>${item.businessName}</span>
                            </div>
                        </div>
                        <div class="tableLi3">${item.businessSearchCount}</div>
                    </div>
                    `
                } else if (index == 1) {
                    html += `
                    <div class="tableUl">
                        <div class="tableLi1">
                            <img src="../image/common/ranking2.png" alt="" srcset="">
                        </div>
                        <div class="tableLi2">
                            <div class="tableLi2d">
                                <span>${item.businessName}</span>
                            </div>
                        </div>
                        <div class="tableLi3">${item.businessSearchCount}</div>
                    </div>
                    `
                } else if (index == 2) {
                    html += `
                    <div class="tableUl">
                        <div class="tableLi1">
                            <img src="../image/common/ranking3.png" alt="" srcset="">
                        </div>
                        <div class="tableLi2">
                            <div class="tableLi2d">
                                <span>${item.businessName}</span>
                            </div>
                        </div>
                        <div class="tableLi3">${item.businessSearchCount}</div>
                    </div>
                    `
                } else {
                    html += `
                    <div class="tableUl">
                        <div class="tableLi1">${index + 1}</div>
                        <div class="tableLi2">
                            <div class="tableLi2d">
                                <span>${item.businessName}</span>
                            </div>
                        </div>
                        <div class="tableLi3">${item.businessSearchCount}</div>
                    </div>
                    `
                }
            });
            $("#merchantsTopRetrieve").html(html)

        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};


// 车位数
function getDeviceCount() {
    $.ajax({
        url: url + 'view/getDeviceCount',
        data: timeData,
        success: function (res) {
            console.log('车位数', res);
            if (res.code != 200) {
                return;
            };

            if (!res.data.length) return false;

            let data = res.data[0];
            // 车位总数
            let total = data.carBitCount;
            $("#carTotal").html(total);
            // 占用车位
            let occupySpace = data.carBitOccupy;
            let occupySpaceD = ((data.carBitOccupy / total) * 100).toFixed(2);
            $("#occupySpace").html(occupySpace);
            $("#carYiOccupy2").html(occupySpaceD + '%');
            // 空闲
            let freeSpace = data.carBitIdle;
            let freeSpaceD = (100 - occupySpaceD).toFixed(2);
            $("#freeSpace").html(freeSpace);
            $("#carYiFree2").html(freeSpaceD + '%');
            // 充电
            let chargeSpace = data.carBitChargePark;
            let chargeSpaceD = ((data.carBitChargePark / total) * 100).toFixed(2);
            $("#chargeSpace").html(chargeSpace);
            $("#carBitChargePark2").html(chargeSpaceD + '%');
            // 专用
            let specialSpace = data.carBitExclusive;
            let specialSpaceD = ((data.carBitExclusive / total) * 100).toFixed(2);
            $("#specialSpace").html(specialSpace);
            $("#carYiSpecial2").html(specialSpaceD + '%');
            // vip
            let vipSpace = data.carBitVIP;
            let vipSpaceD = ((data.carBitVIP / total) * 100).toFixed(2);
            $("#vipSpace").html(vipSpace);
            $("#carYiVip2").html(vipSpaceD + '%');

        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};






//请求错误时
function resError(jqXHR) {
    tips('系统繁忙');
}