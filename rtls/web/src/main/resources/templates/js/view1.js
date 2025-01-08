var barType = 'count';
var beaconList = [];//�ű��б�
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

var userList = [];//�û��б�

var fMapLayer = {};
//��������������
var analyser = null;

// ��������init�ļ������ڱ����;��
var naviTarget = null;
//��ʼ��������
var navigation = false;

var countAndFeeDay = 1;
var countAndFeeChatrs = null;

var oldUserId = '';
var useChargeECharts; // ��糵λʹ��-echarts
var mapUserList = []; // ��ͼʵʱ�û�����
var allEnter = []; // ��ͼ��λʵʱ����

var settime = null;

var timeData = {
    time: "1",
    mapId: '',
}; // ѡ���ʱ���


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
        tips('��ȡ��ͼʧ��');
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
    let dayArr = ['������', '����һ', '���ڶ�', '������', '������', '������', '������'];
    $(".headerTimeD")[0].innerText = curTime.getFullYear() + '��' + zero(curTime.getMonth() + 1) + '��' + zero(curTime.getDate()) + '��';
    $(".headerTimeT")[0].innerText = zero(curTime.getHours()) + ':' + zero(curTime.getMinutes()) + ':' + zero(curTime.getSeconds());
    $(".headerTimeW")[0].innerText = dayArr[curTime.getDay()];
    settime = setInterval(getTime, 1000);
}
function zero(str) {
    return str < 10 ? '0' + str : str;
}

//��ʼ����λʹ��ʱ��
function initDate(daysAgo) {
    //daysAgo��(-)ǰ(+)��
    var now = new Date();

    var ago = new Date(now.getTime() + (daysAgo + 1) * 24 * 60 * 60 * 1000);
    var time = ago.getFullYear() + '-' + padZero(ago.getMonth() + 1) + '-' + padZero(ago.getDate()) + ' �� ';
    time += now.getFullYear() + '-' + padZero(now.getMonth() + 1) + '-' + padZero(now.getDate());
    $('#time').val(time);
    jeDate('#time', {
        theme: {
            bgcolor: "#4A60CF",
            pnColor: "#4A60CF"
        },
        multiPane: false,
        range: " �� ",
        format: "YYYY-MM-DD",
        donefun: function () {
            placeAnalyze();
        }
    });
}

//����
function padZero(n) {
    return n > 9 ? '' + n : '0' + n;
}

//�л�����
function switchTitle(that, type) {
    var _that = $(that);
    if (_that.hasClass('cur-title')) {
        return;
    }
    _that.addClass('cur-title');
    _that.siblings().removeClass('cur-title');
    $('#' + type).show().siblings().hide();

    if (type === 'countAndFeeBox') {
        //��ʾ���ټ��أ���Ȼ��Ȼ����
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
            tips('�л�ʧ��');
            return;
        }

        if (devicePie) {
            devicePie.setOption({
                series: [{
                    name: target.title,
                    data: [
                        { value: target.onLine, name: '����' },
                        { value: target.offLine, name: '���� ' },
                        { value: target.subLowPower, name: '�͵���' },
                    ],
                }],
            })
        } else {
            devicePie = initPie('deviceStatistics', {
                name: target.title,
                color: ['#0990E2', '#8A8A8A', '#D5C417'],
                data: [
                    { value: target.onLine, name: '����' },
                    { value: target.offLine, name: '���� ' },
                    { value: target.subLowPower, name: '�͵���' },
                ],
            })
        }

        devicePie.resize();
    } else if (type == 'parkUsageStatisticsBox') {
        // ���ŵص�
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
                console.log('��ʼ������ͼʧ��');
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
                data: ['������', '�շ����'],
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
                    name: '������',
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
                    name: '�շ����',
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
                    name: '������',
                    type: 'line',
                    data: count,
                    smooth: true,
                }, {
                    name: '�շ����',
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

//��ʼ������
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
                    tips('��ȡ��ͼ����ʧ�ܣ�������');
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
                    //��ʼָ�����ƫ����
                    compassOffset: [20, 50],
                    //ָ�����СĬ������
                    compassSize: 64,
                    //defaultViewMode:fengmap.FMViewMode.MODE_2D,
                    defaultBackgroundColor: '#153160',
                    focusFloor: 1,
                    //¥��ؼ���topֵ
                    floorTop: 142,
                }, function () {
                    //��ȡ¥���layer
                    // var floorList = fMap.groupIDs;
                    var floorList = fMap.getLevels();
                    var group = null;
                    var gid;
                    // for (var j = 0; j < floorList.length; j++) {
                    //     //��ȡ����¥���layer
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


                    //��ʼ������
                    analyser = new fengmap.FMSearchAnalyser({ map: fMap });//fid����
                    naviAnalyser = new fengmap.FMNaviAnalyser({ map: fMap });//·������

                    // init(); // ��λ�����
                    // placeAnalyze(); // ͣ��ʱ��������
                    //initViolation(); // Υͣ��Ϣ

                    //��վ�������������
                    // getAllMarker();

                    //��ȡ��λ
                    getAllPlace();
                    setTimeout(function () {
                        startRequest();
                    }, 1000)
                    //��ʼ��ģ������
                    // initDetail();
                    //��ʷ��¼
                    // getHistory();

                    // api����
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
            tips('ϵͳ��æ');
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
                // if (name === '����' || name === '����') {
                //     arr = param.data1 || param.data;
                // } else if (name === '�͵���' || name === '��������') {
                //     arr = param.data2;
                // }
                arr = param.data;
                var sum = 0, sum1 = 0, index = 0;
                // arr.forEach(item => {
                //     if (item.name === '����' || item.name === '����') {
                //         sum += item.value;
                //     } else if (item.name === '�͵���' || item.name === '��������') {
                //         sum1 += item.value;
                //     }
                // });
                arr.forEach((item, i) => {
                    sum += item.value
                    if (item.name == name) {
                        index = i;
                    }
                });
                // if (name === '����' || name === '����') {
                //     let percent = (arr[index].value / sum * 100).toFixed(2);
                //     return name + ' ' + arr[index].value + " (" + percent + '%)';
                // } else if (name === '�͵���' || name === '��������') {
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

//��ʼ������
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
                name: '��λʹ��',
                color: ['#de7065', '#009417'],
                data: [
                    { value: data.usedPlace, name: '��ͣ��λ' },
                    { value: data.emptyPlace, name: '���೵λ ' },
                ],
                //radius: [35, 55],
            })
            useChargeECharts = initPie('useCharge', {
                name: '���׮ʹ��',
                color: ['#4361d8', '#052af7'],
                data: [
                    { value: data.usedCharge, name: 'ʹ����' },
                    { value: data.emptyCharge, name: 'δʹ�� ' },
                ],
                radius: [35, 55],
            })
            initPie('appointment', {
                name: 'ԤԼ��λ',
                color: ['#2CDAFF', '#4A60CF'],
                data: [
                    { value: data.bookPlace, name: 'ԤԼ��λ' },
                    { value: data.totalPlace, name: '�ܳ�λ ' },
                ],
                radius: [35, 55],
            })
        },
        error: function (err) {
            tips('ϵͳ��æ');
        }
    })
}

//Υͣ��Ϣ
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
                html += '<td>' + (target.state ? '��' : '��') + '</td>';
                html += '</tr>';
            }
            $('#violation').html(html);
        }
    })
}

//��ʼ������
function placeAnalyze() {
    var time = $('#time').val();
    time = time.split(' �� ');
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
                name = 'Ƶ��';
            } else {
                name = 'ʱ��';
            }
            initBar(xData, data, name);
        },
        error: function (err) {
            tips('ϵͳ��æ');
        }
    })
}

//��ʼ����״ͼ
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
                let str = params.name + '</br>' + params.value + "��(Сʱ)";
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
            name: "��(Сʱ)",
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
                let str = params.name + '</br>' + params.value + "���";
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
            name: "���",
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

//�л���λʹ�÷���������
function switchParking(that, type) {
    if (barType === type) {
        return;
    }
    barType = type;

    switchSeleBox(that);
    placeAnalyze();
}

//�л�seleBox��ʽ
function switchSeleBox(that) {
    var _that = $(that);
    var parent = _that.parent();
    parent.find('.curSele').removeClass('curSele');
    _that.addClass('curSele');
}

//�л��豸ͳ��
function switchDevice(that, type) {
    if (deviceType === type) {
        return;
    }
    var target = contrastReturn(deviceChars, 'type', type);
    if (!target) {
        tips('�л�ʧ��');
        return;
    }
    deviceType = type;
    switchSeleBox(that);

    if (devicePie) {
        devicePie.setOption({
            series: [{
                name: target.title,
                data: [
                    { value: target.onLine, name: '����' },
                    { value: target.offLine, name: '���� ' },
                    { value: target.subLowPower, name: '�͵���' },
                ],
            }],
        })
    } else {
        devicePie = initPie('deviceStatistics', {
            name: target.title,
            color: ['#0990E2', '#8A8A8A', '#D5C417'],
            data: [
                { value: target.onLine, name: '����' },
                { value: target.offLine, name: '���� ' },
                { value: target.subLowPower, name: '�͵���' },
            ],
        })
    }
}

//�豸ͳ�Ʊ�ͼ
function deviceData(data, type) {
    if (type == 12) {
        // �ű�ͳ��
    };
    if (type == 13) {
        // ����ͳ��
    };
    if (type == 14) {
        // ��λ�����ͳ��
    }
};

//��ʾ����
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

//������ʾ
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


//��ȡurl�����в�
function getAllUrl() {
    var url = location.search; //��ȡurl��"?"������ִ�  
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

//��ת
function rotate(that) {
    if (!mapLoad) {
        tips('��ͼ����ʧ�ܣ�������');
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

//�����л�
function toggleOper(that, type) {
    if (operShow === type) {
        //�ر�
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

//�ű����
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
        tips('���ִ���');
        return;
    }

    var target = null;
    for (var i = 0; i < targetList.length; i++) {
        target = targetList[i];
        target.im.visible = flag;
        target.tm.visible = flag;
    }
}

//���ͼƬ
function addFMImageMarker(imgLayer, para) {
    var im = resImgMarker(para);
    imgLayer.addMarker(im);

    return im;
}

//����ı�
function addTextMarker(textLayer, para) {
    var tm = resTextMarker(para);
    textLayer.addMarker(tm);

    return tm;
}

//�ı�ͼƬ
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

//����λ����
function startRequest() {
    if (socket) {
        return;
    }
    if (typeof (WebSocket) == "undefined") {
        tips("�����������֧��WebSocket");
    } else {
        /*if(!uId){
            console.log('��λ��������ʧ�ܣ������Ƿ��¼');
            tips('��λ��������ʧ�ܣ������Ƿ��¼');
            return;
        }*/
        console.log("���������֧��WebSocket");
        //ʵ�ֻ�WebSocket����ָ��Ҫ���ӵķ�������ַ��˿�  ��������
        //socket = new WebSocket('ws://' + location.host + '/websocket/location/' + uId);
        socket = new WebSocket(websocketUrl + mapId);

        //���¼�
        socket.onopen = function () {
            console.log("WebSocket ���ӳɹ�ʱ����");
            //tips("WebSocket ���ӳɹ�ʱ����");
            //socket.send("�������Կͻ��˵���Ϣ" + location.href + new Date());
        };
        //�����Ϣ�¼�
        socket.onmessage = function (res) {
            processData(res.data);
        };
        //�ر��¼�
        socket.onclose = function () {
            console.log("Socket�ѹر�");
        };
        //�����˴����¼�
        socket.onerror = function () {
            tips("Socket�����˴����޷��յ�λ����Ϣ������ϵ����Ա", null, 5000);
        }
    }
}

//��������
function processData(res) {
    //���͵Ķ�Ϊ�����Ķ���
    var item = JSON.parse(res);
    // console.log(item);
    /*if(item.type != 2){
        if(item.map != mapId){
            return;
        }
    }*/
    /*var data = null;
    if(item.data instanceof Array){
        //�״�Ϊ����
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
        //��λ����
        fMapPos([data]);//�ݲ��޸�����ķ���
        fMapUsersNum([data]);// �����û���
    } else if (type == 5 || type == 6 || type == 7) {
        //�ű�״̬
        changeState(item);
    } else if (type == 8) {
        //��λ״̬
        changeModelRender(data);
    } else if (type == 9) {
        //��λʹ��
    } else if (type == 10) {
        //���׮ʹ��
    } else if (type == 11) {
        //ԤԼ��λ(�޸Ĵ����پͲ��������)
    } else if (type == 12 || type == 13 || type == 14) {
        //�豸ͳ��
        // deviceData(data, type);
    } else if (type == 15) {
        //�շ�ͳ��
        // chargeStatistics(data);
    } else if (type == 16) {
        //��������
        // inAndOutData(data);
    } else if (type == 17) {
        // ��ͼ��λʵʱ����-40
        allEnter = data;
        // initEnter(data);
    } else if (type == 18) {
        // ��ͼ��λʵʱ����-1
        // updateEnter(allEnter, data);
    } else {
        console.log('δ֪����');
    }
}

//�ű�״̬����
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
            //     console.log('¥�����');
            //     continue;
            // }
            html += '<tr><td class="order">' + (tablen) + '</td>';
            html += '<td class="user"><div class="checkbox detail-item" onclick="checkUser(this,\'' + userid + '\' ,\'' + tablen + '\')">';
            html += '<div class="checked-radiu"></div></div>';
            html += '<div class="user-text">' + '΢��' + (tablen) + '</div></tr>';
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

            // ��ʼ����ͼʵʱ�û�λ��
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
                avoidState: true,//����״̬
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
                            //�����ǰ�����Ƴ���ǰ��
                            removeNaviRoute(userid);
                        };
                        //����
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
                    //�����ǰ�����Ƴ���ǰ��
                    removeNaviRoute(userid);
                    result.hasNaviLine = false;
                }
            }
            setLocationMakerPosition(result, target);
            updateUserInfo(result, target);

            // ���µ�ͼ�ϵ��û�λ��
            updateMapUsersPoint(target);

            if (result.checked) {
                // �����û�ʵʱ���
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

//�ж��Ƿ�ƫ��
function isResetNavi(result, start) {
    var sx = start.x;
    var sy = start.y;
    var sgroupID = start.groupID;
    if (!sx || !sy || !sgroupID) {
        //ƫ��ʱ��Я��������Ϣ������ʱ��λ�ñ仯û��--�������֣�
        return false;
    }
    if (result.startX != sx || result.startY != sy || result.startFloor != sgroupID) {
        return true;
    }
    return false;
}

//������Ա��Ϣ
function updateUserInfo(result, target) {
    // if (JSON.stringify(result) == JSON.stringify(target)) {
    //     console.log('��������Ϣ��Ϊ��ͬ�����');
    //     return;
    // }
    //text����˵��������ɲ�д
    var list = [
        {
            key: 'target',
            text: 'Ŀ�ĵ�',
            write: true,
        }, {
            key: 'speed',
            text: '�ٶ�',
            write: true,
        }, {
            key: 'nav_state',
            text: '����ģʽ',
            write: true,
        }, {
            key: 'state',
            text: '״̬',
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

//����λ��
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
        //�պ��������ӷ���ᱨ��
        callback: function () {
        },
        update: function () {
        },
    }

    if (locationGid !== groupID) {
        //���
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

//ѡ����ʾ���û�
function checkUser(that, userid, index) {
    if (detailImgMarket) {
        detailImgMarket.remove();
    };
    var result = contrastReturn(userList, 'id', userid);
    if (!result) {
        console.log('�Ҳ������Ӧ���û�');
        return;
    }
    var im = result.im;
    var tm = result.tm;
    var dm = result.dm;
    // ֻ����һ��
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
            //��ʾ·�ߣ��ر�ʱ���Ƴ�����·�ߣ�
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

    // ����ѡ�е��û�����
    showRealtimeDetail(result, result.checked, index);
    // ����ѡ��ͼ����ɫ
    setMapUsersListColor(result);



}

//�ű�״̬����
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
        //�������ݲ���
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
                dom.find('.networkstate').html('����').removeClass('stateHas').addClass('stateNot');
            } else if (networkstate === 2) {
                dom.find('.networkstate').html('�͵���').removeClass('stateHas').addClass('stateWarn');
            } else {
                dom.find('.networkstate').html('����').removeClass('stateNot').addClass('stateHas');
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

//Ѱ�ҶԱȷ���
function contrastReturn(arr, attr, mVal) {
    var target = null;
    for (var i = 0; i < arr.length; i++) {
        target = arr[i];
        if (target[attr] == mVal) {
            return target;
        }
    }
}

//����¼�
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
        text = '�ű�';
    } else if (selfType === 'detector') {
        targetList = detectorList;
        text = '��λ�����';
    } else if (selfType === 'gateway') {
        targetList = gatewayList;
        text = '����';
    } else if (selfType === 'user') {
        //��Ա
        targetList = userList;
    } else {
        return;
    }
    target = contrastReturn(targetList, 'id', result.selfId);
    if (!target) {
        tips('û���ҵ���ص�����');
        return;
    }
    if (target.type === 'history') {
        //�ݲ���ʾ
        return;
    }
    createDomMarker(target, selfType, text || target.name);
    return false;
}

//������ص���
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
            text: '�豸ID',
        }, {
            key: 'power',
            text: '����',
        }, {
            key: 'networkstate',
            text: '״̬',
        }]
    } else if (type === 'detector') {
        writeList = [{
            key: 'name',
            text: '�豸ID',
        }, {
            key: 'power',
            text: '����',
        }, {
            key: 'networkstate',
            text: '״̬',
        }]
    } else if (type === 'gateway') {
        writeList = [{
            key: 'name',
            text: '�豸ID',
        }, {
            key: 'networkstate',
            text: '״̬',
        }]
    } else if (type === 'user') {
        writeList = [{
            key: 'target',
            text: 'Ŀ�ĵ�',
        }, {
            key: 'speed',
            text: '�ٶ�',
        }, {
            key: 'nav_state',
            text: '����ģʽ',
        }, {
            key: 'state',
            text: '״̬',
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
                html += ' stateHas">����</span>';
            } else if (target.networkstate === 2) {
                html += ' stateWarn">�͵���</span>';
            } else {
                html += ' stateHas">����</span>';
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

//���domģ��
function addFMDomMarker(domLayer, para) {
    var dm = new fengmap.FMDomMarker({
        x: para.x + 0.5,
        y: para.y,
        height: para.z || 2,
        //û��Ҫ��
        //domWidth: para.width || 20,
        //domHeight: para.high || 24,
        domContent: para.domHtml,
        anchor: fengmap.FMMarkerAnchor.LEFT_TOP,
    });
    domLayer.addMarker(dm);
    return dm;
}

//����Dm
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
        //�Ƴ������ʱ����ʾ

        target.dm.remove();
    }
}

//��ȡmarker
function getAllMarker() {
    var list = ['base', 'detector', 'gateway'];
    for (var i = 0; i < list.length; i++) {
        getAll(list[i]);
    }
}

//��������
function getAll(type) {
    var path = '';
    if (type === 'base') {
        path = 'bsconfig/getBsConfigSel';
    } else if (type === 'detector') {
        path = 'infrared/getInfraredSel';
    } else if (type === 'gateway') {
        path = 'gateway_lora/getGatewaySel';
    } else {
        tips('δ֪����');
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

//����marker����¼��Ϣ
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
        //     //�Ҳ�����Ӧ��¥��
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
        //�Զ�������
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
            //��ʾ������
            power: target.power || '',
            networkstate: target.networkstate,
        });
    }
}

//��ȡ��λ
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

//����fid�ı�״̬
function changeModelRender(data, fid, state) {
    var fid = data.fid;
    var state = data.state;
    setPlaceColor([fid], resStateColor(state));

}

//����״̬���Ӧ����ɫ
function resStateColor(state) {
    switch (state) {
        case 0:
            return '#1BC736';
        case 1:
            //��ͣ
            return '';
        // return '#FFDFAA';
        // case 2:
        //     //ԤԼ
        //     return '#cdf408';
        default:
            return '';
    }
}

//��ӳ�ʼ���ĵ�
function initDetail() {
    var target = null;
    var html = '';
    for (var i = 0; i < detailList.length; i++) {
        target = detailList[i];
        html += '<tr><td class="order">' + tablen + '</td>';
        html += '<td class="user"><div class="checkbox detail-item" onclick="checkDetail(this,\'' + target.id + '\',\'' + tablen + '\')">';
        html += '<div class="checked-radiu"></div></div>';
        html += '<div class="user-text moreTextHide">' + '΢��' + tablen + '</div></tr>';
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

    // ����ѡ�е��û�����
    showRealtimeDetail(result, result.checked, index);
}

//��ʾĬ�ϵ�������Ϣ
function showDetail(userid) {
    var result = contrastReturn(detailList, 'id', userid);
    if (!result) {
        console.log('�Ҳ������Ӧ��Ĭ���û�');
        return;
    }
    createDetailNavi(result.start, result.end);
}

//��������
function createDetailNavi(start, end) {
    if (!naviTarget) {
        //��ʼ����������

        let nnaviAnalyser = new fengmap.FMNaviAnalyser({ map: fMap }, function () {
            if (naviAnalyser) {
                naviTarget = new fengmap.FMNavigation({
                    map: fMap,
                    analyser: nnaviAnalyser,
                    //����ͼ��
                    locationMarkerUrl: '../image/park/location.png',
                    // locationMarkerUrl: '../image/fMap/location.png',
                    //����Marker�ߴ�
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
    //������
    naviTarget.setStartPoint({
        x: start.x,
        y: start.y,
        level: start.groupID,
        url: '../image/fMap/start.png',
        size: 32,
        collision: true
    });

    //����յ�
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



    // ��������
    naviTarget.route(
        {
            mode: fengmap.FMNaviMode.MODULE_BEST,
            priority: fengmap.FMNaviPriority.PRIORITY_DEFAULT
        },
        function (result) {
            // ���������ɹ��ص�
            var line = naviTarget.drawNaviLine();

            // ʹ�ú�ɫ�û�ͼ��
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

            // ����Ӧ·��ȫ��
            naviTarget.overview({
                ratio: 1.5
            }, function () {
                console.log('����Ӧȫ�����������ص�');
                // ��ʼģ�⵼��
                startImitateNavi();
                navigation = true
            });
        }, function (result) {
            // ��������ʧ�ܻص�
            console.log("failed", result);
        }
    );
    // ����������λ�÷����仯ʱ�������¼�, ģ�⵼������ʵ��������locate�󶼶��ᴥ����
    naviTarget.on('walking', function (data) {
        // ����·����Ϣ
        let point = data.point;
        if (detailImgMarket) {
            // ���
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
    // ģ�⵼�������¼�, ��ʵ�������ᴥ�����¼�����Ҫ�����߸���driving�¼��ķ�������, ����ҵ����
    naviTarget.on('complete', function (data) {
        console.log('����������');
        // navigation = false;
        // ���¿�ʼģ�⵼��
        startImitateNavi();
    })
};

// ��ʼģ�⵼��
function startImitateNavi() {
    naviTarget.simulate({
        speed: 7,               // ģ�⵼����λͼ���н����ٶ�, ��λm/s��Ĭ��7m/s��
        followPosition: true,   // ģ�⵼������λ�÷����仯ʱ, ��ͼ�Ƿ����λ�þ�����ʾ��Ĭ��true��
        followAngle: false,     // ģ�⵼�����ڷ������仯ʱ, ��ͼ�Ƿ���淽��仯, ����·�߳�����Ļ�Ϸ��� Ĭ��false��
        changeTiltAngle: true,  // ģ�⵼����¥�㷢���仯ʱ�Ƿ�ı��ͼ����б�Ƕ�, ��ִ�ж�����Ĭ��Ϊtrue��
    });
};

//����·���滮����
var naviAnalyser = null;
var naviList = [];//������Ϣ�б�
//����������Ϣ
function createNavi(id, start, end) {

    if (!start.x && !start.y && !start.groupID) {
        return false
    }
    //����������Ϣ
    if (!naviTarget) {
        //��ʼ����������

        let nnaviAnalyser = new fengmap.FMNaviAnalyser({ map: fMap }, function () {
            if (naviAnalyser) {
                naviTarget = new fengmap.FMNavigation({
                    map: fMap,
                    analyser: nnaviAnalyser,
                    //����ͼ��
                    locationMarkerUrl: '../image/park/location.png',
                    // locationMarkerUrl: '../image/fMap/location.png',
                    //����Marker�ߴ�
                    locationMarkerSize: 0,
                });

                createUserNaviObj(id, start, end);
            }
        });


    } else {
        createUserNaviObj(id, start, end);
    }
}

// �û�����
function createUserNaviObj(id, start, end) {
    //������
    naviTarget.setStartPoint({
        x: start.x,
        y: start.y,
        level: start.groupID,
        url: '../image/fMap/start.png',
        size: 32,
        collision: true
    });

    //����յ�
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



    // ��������
    naviTarget.route(
        {
            mode: fengmap.FMNaviMode.MODULE_BEST,
            priority: fengmap.FMNaviPriority.PRIORITY_DEFAULT
        },
        function (result) {
            // ���������ɹ��ص�
            naviTarget.drawNaviLine();
            // ����Ӧ·��ȫ��
            naviTarget.overview({
                ratio: 1.5
            }, function () {

            });
        }, function (result) {
            // ��������ʧ�ܻص�
            console.log("failed", result);
        }
    );
    // ����������λ�÷����仯ʱ�������¼�, ģ�⵼������ʵ��������locate�󶼶��ᴥ����
    naviTarget.on('walking', function (data) {
        // ����·����Ϣ
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
    //     console.log('�Ҳ������Ӧ�ĵ���·��');
    //     return;
    // }
    // //��յ�����
    // fMap.removeLineMarker(target.line);
    // //�����㡢�յ�marker
    // fMapLayer['f' + target.sIm.groupID].imgLayer.removeMarker(target.sIm);
    // fMapLayer['f' + target.eIm.groupID].imgLayer.removeMarker(target.eIm);
    // naviList.splice(i, 1);

    if (naviTarget) {
        naviTarget.clearAll();
        naviTarget.dispose();
        naviTarget = null;
    }
}

//�շ�ͳ��
function chargeStatistics(target) {
    $('#fee').html(target.fee);
    $('#totalCount').html(target.totalCount);
    $('#averageStaytime').html(target.averageStaytime);
    $('#monthlyRentCount').html(target.monthlyRentCount);
}

//ͣ������������
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

//������ʷ
function playHistory(item) {
    var result = contrastReturn(historyList, 'id', item.id);
    if (!result) {
        console.log('�Ҳ������Ӧ�ĵ�');
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

//����
function changeCoord() {
    clearTimeout(historyTimer);
    //��ʱ��
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
// ��ʾ�û�����
function showRealtimeDetail(userData, checked, index) {


    userNameIndex = index

    if (checked) {
        let name = '΢��' + index;
        $("#rDetailHeaderName").html(name);

        let type = userData.nav_state || '��������';
        $("#rDetailInfoType").html(type);

        let end = userData.target || '--';
        $("#rDetailInfoEnd").html(end);


        $("#realtimeDetail").css('display', 'block')

    } else {
        $("#realtimeDetail").css('display', 'none')

    }
}

// ���µ�ͼ�ϵ��û�λ��
function updateMapUsersPoint(data) {
    mapUserList.forEach((item) => {
        if (item.userid == data.userid) {
            // item.imgMarker.setPosition(data.x, data.y, data.floor, 2)
            if (item.imgMarker.level != (data.floor != 0 ? data.floor : data.startFloor)) {
                // ���
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

// ѡ���û�
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

// ��λʵʱ����
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
        let classStr = str2 === '����' ? 'green' : str2 === 'ռ��' ? 'orange' : '';
        htmlStr += "<li><span>" + item.time + "</span><span>" + item.parkingLot + "</span><span>" + str1 + ' <i class=\'' + classStr + '\'>' + str2 + "</i></span>";
    });
    htmlStr += "</ul>";
    carContent.innerHTML = htmlStr;
    $('.scrollDiv').liMarquee({
        direction: 'up',//���Ϲ��� 
        runshort: false,//���ݲ���ʱ������
        scrollamount: 16//�ٶ�
    });
}

var hotSettimeoutData = null;
// ���ŵص�
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

    // ��ʾһ�κ�ʱ
    hotSettimeoutData = setTimeout(() => {
        initPopularLocations();
    }, 12 * 3600 * 1000);
};



// ���ͼƬ
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

// �������
function newAddTextMarkerToMap(level, data) {
    let textMarker = new fengmap.FMTextMarker({
        height: data.height,
        fontFamily: '΢���ź�',
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

// dom������
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

/* ����ѡ�� */
function openSelect() {
    let hae = $(".selectUl").hasClass('showSelect');
    if (hae) {
        $(".selectUl").removeClass('showSelect')
    } else {
        $(".selectUl").addClass('showSelect')
    }
};


// api����
function apiGetDataState() {
    return false;
    // ��λ��������
    getPlaceNavigationTotal();
    // ��λ����ʹ����
    getPlaceNavigationUseRate();
    // ƽ̨��λ������
    getPlatformPlaceUtilizationRate();
    // ��λԤԼ����
    getReservationTotal();
    // ����Ѱ������
    getReverseCarSearchTotal();
    // ͣ������λ������
    getPlaceAvailabilityRate();

    // ÿСʱ�ճ�λ��
    getPerHourNullPlaceNumber();

    // ��λʹ�ô���
    getPlaceUseTotal();
    // ��λ������ʱ��
    getPlaceldleTotalDuration();

    // �û�����
    getMapUsersTotal();
    // �����ܴ���
    getCumulativeUseFrequency();
    // ��Ծ�û���
    getActiveUserNumber();
    // �����û���
    getWithinThreeMonthsNewUsers();
    // �û���������
    getUserSearchTotal();
    // λ�÷�������
    getLocationShareTotal();

    // �û�����ǰ10�̼�
    getTop10Business();

    // ���ŵص�
    initPopularLocations();

    // ��λ��
    getDeviceCount();
};

// ��λ��������
function getPlaceNavigationTotal() {
    $.ajax({
        url: url + 'view/getPlaceNavigationTotal',
        data: timeData,
        success: function (res) {
            console.log('��λ��������', res);
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

// ��λ����ʹ����
function getPlaceNavigationUseRate() {
    $.ajax({
        url: url + 'view/getPlaceNavigationUseRate',
        data: timeData,
        success: function (res) {
            console.log('��λ����ʹ����', res);
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

/* ƽ̨��λ������ */
function getPlatformPlaceUtilizationRate() {
    $.ajax({
        url: url + 'view/getPlatformPlaceUtilizationRate',
        data: timeData,
        success: function (res) {
            console.log('ƽ̨��λ������', res);
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

/* ��λԤԼ���� */
function getReservationTotal() {
    $.ajax({
        url: url + 'view/getReservationTotal',
        data: timeData,
        success: function (res) {
            console.log('��λԤԼ����', res);
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

// ����Ѱ������
function getReverseCarSearchTotal() {
    $.ajax({
        url: url + 'view/getReverseCarSearchTotal',
        data: timeData,
        success: function (res) {
            console.log('����Ѱ������', res);
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

/* ͣ������λ������ */
function getPlaceAvailabilityRate() {
    $.ajax({
        url: url + 'view/getPlaceAvailabilityRate',
        data: timeData,
        success: function (res) {
            console.log('ͣ������λ������', res);
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

// ÿСʱ�ճ�λ��
function getPerHourNullPlaceNumber() {
    $.ajax({
        url: url + 'view/getPerHourNullPlaceNumber',
        data: timeData,
        success: function (res) {
            console.log('ÿСʱ�ճ�λ��', res);
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
                        let str = arrH2[params.dataIndex] + '</br>' + "�ճ�λ��:" + params.value;
                        // let str = params.name + '</br>' + "�ճ�λ��:" + params.value;
                        if (params) {
                            str += "<span style='display:inline-block;width:10px;height:10px;border-radius:10px;></span>"
                        }
                        return str
                    },
                },
                xAxis: {
                    type: 'category',
                    name: '(Сʱ)',
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
                    name: '(�ճ�λ��)',
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

// ��λʹ�ô���
function getPlaceUseTotal() {
    $.ajax({
        url: url + 'view/getPlaceUseTotal',
        data: timeData,
        success: function (res) {
            console.log('��λʹ�ô�������', res);
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
            $("#carUseBarNum").html((dataTotal.toFixed(2)) + '��');
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

// ��λ������ʱ��
function getPlaceldleTotalDuration() {
    $.ajax({
        url: url + 'view/getPlaceIdleTotalDuration',
        data: timeData,
        success: function (res) {
            console.log('��λ������ʱ��', res);
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

// ��ͼ�û�����
function getMapUsersTotal() {
    $.ajax({
        url: url + 'view/getMapUsersTotal',
        data: timeData,
        success: function (res) {
            console.log('��ͼ�û�����', res);
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

// �����ܴ���
function getCumulativeUseFrequency() {
    $.ajax({
        url: url + 'view/getCumulativeUseFrequency',
        data: timeData,
        success: function (res) {
            console.log('�����ܴ���', res);
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

// �»�Ծ�û���
function getActiveUserNumber() {
    $.ajax({
        url: url + 'view/getActiveUserNumber',
        data: timeData,
        success: function (res) {
            console.log('�»�Ծ�û���', res);
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
                arrMonth.push(str + '��')
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
                        let str = params.name + "�»��û���<br/>" + params.value + "��";
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
                    name: '(��)',
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

/* �����û��� */
function getWithinThreeMonthsNewUsers() {
    $.ajax({
        url: url + 'view/getWithinThreeMonthsNewUsers',
        data: timeData,
        success: function (res) {
            console.log('�����û���', res);
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

// �û���������
function getUserSearchTotal() {
    $.ajax({
        url: url + 'view/getUserSearchTotal',
        data: timeData,
        success: function (res) {
            console.log('�û���������', res);
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

// λ�÷�������
function getLocationShareTotal() {
    $.ajax({
        url: url + 'view/getLocationShareTotal',
        data: timeData,
        success: function (res) {
            console.log('λ�÷�������', res);
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

/* �û�����ǰ10�̼� */
function getTop10Business() {
    $.ajax({
        url: url + 'view/getTop10Business',
        data: timeData,
        success: function (res) {
            console.log('�û�����ǰ10ͣ����', res);
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


// ��λ��
function getDeviceCount() {
    $.ajax({
        url: url + 'view/getDeviceCount',
        data: timeData,
        success: function (res) {
            console.log('��λ��', res);
            if (res.code != 200) {
                return;
            };

            if (!res.data.length) return false;

            let data = res.data[0];
            // ��λ����
            let total = data.carBitCount;
            $("#carTotal").html(total);
            // ռ�ó�λ
            let occupySpace = data.carBitOccupy;
            let occupySpaceD = ((data.carBitOccupy / total) * 100).toFixed(2);
            $("#occupySpace").html(occupySpace);
            $("#carYiOccupy2").html(occupySpaceD + '%');
            // ����
            let freeSpace = data.carBitIdle;
            let freeSpaceD = (100 - occupySpaceD).toFixed(2);
            $("#freeSpace").html(freeSpace);
            $("#carYiFree2").html(freeSpaceD + '%');
            // ���
            let chargeSpace = data.carBitChargePark;
            let chargeSpaceD = ((data.carBitChargePark / total) * 100).toFixed(2);
            $("#chargeSpace").html(chargeSpace);
            $("#carBitChargePark2").html(chargeSpaceD + '%');
            // ר��
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






//�������ʱ
function resError(jqXHR) {
    tips('ϵͳ��æ');
}