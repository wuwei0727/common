var time;
var socket;
var allEnter = [];

var timeData = {
    time: "1"
}; // 选择的时间段
var parkadeInfo = []; // 停车场信息

// 基础数据
var basicDataObj = {
    "placeNavigationTotal": 0, // 车位导航总数
    "placeNavigationUseRate": 0, // 车位导航使用率
    "platformPlaceUtilizationRate": 0, // 平台车位利用率
    "reservationTotal": 0, // 车位预约总数
    "reverseCarSearchTotal": 0, // 反向寻车总数
    "placeAvailabilityRate": 0, // 停车场车位空闲率

    "userTotal": 0, // 用户总数
    "useFrequency": 0, // 访问总数
    "activeUserTotal": 0, // 活跃用户数
    "newUsersNumber": 0, // 新用户数量
    "userSearchTotal": 0, // 用户检索总数
    "locationShareTotal": 0, // 位置分享数量

    "perHourNullPlaceNumber": 0, // 每小时空车位数

    "detectorCount": 0, // 检测器数量
    "subCount": 0, // 信标数量
    "gatewayCount": 0, // 网关数量

    "placeUseTotal": 0, // 车位使用总数

    "placeIdleTotalDuration": 0, // 车位空闲总时长

    "mapPlaceUtilizationRate": 0, // 车位利用率前10停车场

    "idlePlaceNumber": 0, // 空闲车位数量

    "monthlyActiveUsers": 0, // 活跃用户

    "top10Business": 0, // 检索前10商家
};

var posHandler = {
    posMap: {},
    reset: function () {
        this.posMap = {};
    },
    getDeltaPos: function (params) {
        var rect = params.rect;
        var labelWidth = 170, labelHeight = 30;
        var gridx = Math.floor(rect.x / labelWidth);
        var gridy = Math.floor(rect.y / labelHeight);
        var currCell = [gridx, gridy], currPos = [];

        var increaseArr = [[-1, -1], [1, -1], [-1, -1], [0, -1], [-1, 0], [1, 0], [1, 1], [0, 1]];
        var found = false;
        if (this.posMap[currCell[0] + '-' + currCell[1]]) {
            while (!found) {
                for (var i = 0; i < increaseArr.length; i++) {
                    currCell[0] = currCell[0] + increaseArr[i][0];
                    currCell[1] = currCell[1] + increaseArr[i][1];
                    if (!this.posMap[currCell[0] + '-' + currCell[1]]) {
                        found = true;
                        this.posMap[currCell[0] + '-' + currCell[1]] = params.text;
                        currPos = [currCell[0] * labelWidth, currCell[1] * labelHeight];
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
        } else {
            this.posMap[gridx + '-' + gridy] = params.text
        }
        currPos = [currCell[0] * labelWidth, currCell[1] * labelHeight];
        var deltaPos = {
            dx: currPos[0] - rect.x,
            dy: currPos[1] - rect.y
        }
        return deltaPos;
    }
};
$(function () {
    getTime();
    time = setInterval(getTime, 1000);

    // 获取基础数据
    getBasicData();
});

// 获取基础数据
function getBasicData() {
    $.ajax({
        url: url + 'variable_operational_data/getVariableOperationalData',
        type: 'get',
        headers: {
            "Content-Type": "application/json;charset=UTF-8"
        },
        // beforeSend: function () {
        //     loading();
        // },
        // complete: function () {
        //     removeLoad();
        // },
        success: function (res) {
            if (res.code == 200) {
                if (res.data.length) {
                    basicDataObj = res.data[0]
                }
                startGetData();
            } else {
                startGetData();
            }
        },
        error: function (jqXHR) {
            startGetData();
        }
    })
};

function startGetData() {
    startRequest();
    // 使用api获取数据
    apiGetDataState();
};


function getTime() {
    clearInterval(time);
    let curTime = new Date();
    let dayArr = ['星期天', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
    $(".headerTimeD")[0].innerText = curTime.getFullYear() + '年' + zero(curTime.getMonth() + 1) + '月' + zero(curTime.getDate()) + '日';
    $(".headerTimeT")[0].innerText = zero(curTime.getHours()) + ':' + zero(curTime.getMinutes()) + ':' + zero(curTime.getSeconds());
    $(".headerTimeW")[0].innerText = dayArr[curTime.getDay()];
    time = setInterval(getTime, 1000);
}

function zero(str) {
    return str < 10 ? '0' + str : str;
}

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

        socket = new WebSocket(websocketAllMapUrl + '-1');
        // socket = new WebSocket(websocketUrl)
        //打开事件
        socket.onopen = function () {
            console.log("WebSocket 连接成功时触发");
            //tips("WebSocket 连接成功时触发");
            //socket.send("这是来自客户端的消息" + location.href + new Date());
        };
        //获得消息事件
        socket.onmessage = function (res) {
            processData(res.data);
            // console.log(res.data)
        };
        //关闭事件
        socket.onclose = function () {
            console.log("Socket已关闭");
            socket = null;
            setTimeout(() => {
                startRequest();
            }, 900000);
        };
        //发生了错误事件
        socket.onerror = function () {
            tips("Socket发生了错误，无法收到位置信息，请联系管理员", null, 5000);
            socket = null;
            setTimeout(() => {
                startRequest();
            }, 900000);
        }
    }
}
function processData(res) {
    var item = JSON.parse(res);
    console.log(item);
    var data = item.data;
    if (item.type === 20) {
        // let userCount = +basicDataObj.userTotal + data[0].userCount;
        // initNumBorder(userCount, "userTotal");
    } else if (item.type === 24) {
        // let useFrequency = (+basicDataObj.useFrequency) + (+data[0].useFrequency);
        // initNumBorder(useFrequency, "visits");
    } else if (item.type === 21) {
        // 月活用户数
        // initZhe(data);
    } else if (item.type === 22) {
        console.log('22', data);
        carCount(data[0].carBitIdle, data[0].carBitOccupy, data[0].carBitExclusive, data[0].carBitVIP, data[0].carBitCount, data[0].carBitChargePark);
        initNumBorder(data[0].carBitCount, "carTotal");


        let jiancePieToata = +basicDataObj.detectorCount + (+data[0].detectorCount);
        let jiancedetectorOnLine = +basicDataObj.detectorCount + (+data[0].detectorOnLine);
        initPie('jiancePie', {
            color: ['#FF7800', '#FFAD2F', '#00BAFF', '#00EED5', '#F34040', '#1CED00'],
            data: [
                { value: jiancedetectorOnLine, name: '在线' },
                { value: data[0].detectorOffLine, name: '离线' },
                { value: data[0].detectorLowPower || 0, name: '低电量' },
            ],
            // data2: [
            //     { value: data[0].detectorLowPower || 0, name: '低电量' },
            //     { value: jiancedetectorOnLine + data[0].detectorOffLine || 0, name: '电量正常' }
            // ]
        }, jiancePieToata);

        let xinbiaoPieTotal = +basicDataObj.subCount + (+data[0].subCount);
        let xinbiaoPiesubOnLine = +basicDataObj.subCount + (+data[0].subOnLine);
        initPie('xinbiaoPie', {
            color: ['#4AADFF', '#0667E8', '#7D55FF', '#5A28FE', '#F34040', '#1CED00'],
            data: [
                { value: xinbiaoPiesubOnLine, name: '在线' },
                { value: data[0].subOffLine, name: '离线' },
                { value: data[0].subLowPower || 0, name: '低电量' },
            ],
            // data2: [
            //     { value: data[0].subLowPower || 0, name: '低电量' },
            //     { value: xinbiaoPiesubOnLine + data[0].subOffLine || 0, name: '电量正常' }
            // ]
        }, xinbiaoPieTotal);

        let wangguanPieTotal = +basicDataObj.gatewayCount + (+data[0].gatewayCount);
        let wangguanPiegatewayOnLine = +basicDataObj.gatewayCount + (+data[0].gatewayOnLine);
        initPie('wangguanPie', {
            color: ['#8C69FF', '#5A28FE', '#88FBFF', '#3AE6FF'],
            data: [
                { value: wangguanPiegatewayOnLine, name: '在线' },
                { value: data[0].gatewayOffLine, name: '离线' }
            ],
            legendLeft: "6%"
        }, wangguanPieTotal);
    } else if (item.type === 23) {
        // initCompany(data[0].allMerchant, data[0].allFirm);
    } else if (item.type === 25) {
        var arr = [];
        data.forEach(item => {
            var obj = {
                name: item.mapName,
                value: [item.lng, item.lat]
            };
            arr.push(obj);
        });
        calculateMapData(arr, data);
    } else if (item.type === 26) {
        // 车位实时数据
        // allEnter = data;
        // initEnter(data);
    } else if (item.type === 27) {
        // 车位实时数据
        // updateEnter(allEnter, data);
    } else if (item.type === 28) {
        // 寻车次数
        // let carData = sortCar(data);
        // var findcar = getCarXY(carData.findcar, 5, "xValue", "findCarFrequency");
        // initBar("findCarBar", {
        //     xValue: findcar.xArr,
        //     yValue: findcar.yArr,
        //     color: ['#B6A4FF', '#5530E9']
        // });
    } else if (item.type === 29) {
        // 车位使用次数
        // let dataLength = 10;
        // let dataTotal = 0;
        // data.forEach((item) => {
        //     dataTotal += +item.useCarFrequency
        // });
        // dataTotal = dataTotal / 10000;
        // $("#carUseBarNum").html(Math.trunc(dataTotal) + '万')
        // let carData = sortCar(data);
        // var caruse = getCarXY(carData.caruse, dataLength, "xValue", "useCarFrequency");
        // initBar("carUseBar", {
        //     xValue: caruse.xArr,
        //     yValue: caruse.yArr,
        //     color: ['#B6A4FF', '#5530E9']
        // });
    } else if (item.type === 30) {
        // 车位推荐次数
        // let carData = sortCar(data);
        // var carRecommend = getCarXY(carData.carRecommend, 8, "xValue", "recommendFrequency");
        // initBar("carRecommendBar", {
        //     xValue: carRecommend.xArr,
        //     yValue: carRecommend.yArr,
        //     color: ['#00F6FF ', '#002DD3']
        // });
    }
}
function sortCar(data) {
    let findcar = [], caruse = [], carRecommend = [];
    data.forEach(item => {
        findcar.push({
            xValue: item.abbreviation,
            findCarFrequency: item.findCarFrequency / 10000
        });
        caruse.push({
            xValue: item.abbreviation,
            useCarFrequency: item.useCarFrequency / 10000
        });
        carRecommend.push({
            xValue: item.abbreviation,
            recommendFrequency: item.recommendFrequency / 10000
        });
    });
    findcar.sort((a, b) => a.findCarFrequency - b.findCarFrequency);
    caruse.sort((a, b) => b.useCarFrequency - a.useCarFrequency);
    carRecommend.sort((a, b) => a.recommendFrequency - b.recommendFrequency);
    return { findcar, caruse, carRecommend }
}
function getCarXY(data, length, x, y) {
    let xArr = [], yArr = [];
    data.forEach((item, index) => {
        if (data.length > length) {
            if (index > data.length - length - 1) {
                xArr.push(item[x]);
                yArr.push(item[y]);
            }
        } else {
            xArr.push(item[x]);
            yArr.push(item[y]);
        }
    });
    return { xArr, yArr }
}
function initNumBorder(count, dom) {
    // var htmlStr = addStr(count, 9, "countBorder", "countDou");
    var domSelector = document.getElementById(dom);
    domSelector.innerHTML = count;
    // domSelector.innerHTML = htmlStr;
}
function addStr(count, total, classStr1, classStr2) {
    var countStr = count + '';
    while (countStr.length < total)
        countStr = '0' + countStr;
    var htmlStr = '';
    for (var i = 0; i < countStr.length; i++) {
        htmlStr += '<span class="' + classStr1 + '">' + countStr[i] + '</span>';
        (i == 2 || i == 5) ? htmlStr += '<span class="' + classStr2 + '">,</span>' : '';
    }
    return htmlStr;
}
function calculatePercent(carbit, total) {
    return (carbit / total * 100).toFixed(2);
}
function carCount(carBitIdle, carBitOccupy, carBitExclusive, carBitVIP, carBitCount, carBitChargePark) {

    let num1 = calculatePercent(carBitIdle, carBitCount)

    $("#carYiFree2").html(num1 + '%');
    let num2 = calculatePercent(carBitOccupy, carBitCount)
    // $("#carYiOccupy2").html((100 - num1) + '%');
    $("#carYiOccupy2").html(num2 + '%');
    $("#carYiSpecial2").html(calculatePercent(carBitExclusive, carBitCount) + '%');
    $("#carYiVip2").html(calculatePercent(carBitVIP, carBitCount) + '%');
    $("#carBitChargePark2").html(calculatePercent(carBitChargePark, carBitCount) + '%');

    var space1 = document.getElementById('freeSpace');
    space1.innerText = carBitIdle;

    var space2 = document.getElementById('occupySpace');
    // space2.innerText = carBitCount - carBitIdle;
    space2.innerText = carBitOccupy;

    var space3 = document.getElementById('specialSpace');
    space3.innerText = carBitExclusive;

    var space4 = document.getElementById('vipSpace');
    space4.innerText = carBitVIP;

    var space5 = document.getElementById('chargeSpace');
    space5.innerText = carBitChargePark;

    // initYi('carYiFree', {
    //     dom: "freeSpace",
    //     domValue: carBitIdle,
    //     color: ['#65FF47', '#00B500', '#00FF00'],
    //     value: calculatePercent(carBitIdle, carBitCount),
    //     offsetCenter: '80%'
    // });
    // initYi('carYiOccupy', {
    //     dom: "occupySpace",
    //     domValue: carBitOccupy,
    //     color: ['#FFD725', '#FF7200', '#FFD725'],
    //     value: calculatePercent(carBitOccupy, carBitCount),
    //     offsetCenter: '80%'
    // });
    // initYi('carYiSpecial', {
    //     dom: "specialSpace",
    //     domValue: carBitExclusive,
    //     color: ['#FB98FF', '#AD1AC3', '#DF6AE9'],
    //     value: calculatePercent(carBitExclusive, carBitCount),
    //     offsetCenter: '80%'
    // });
    // initYi('carYiVip', {
    //     dom: "vipSpace",
    //     domValue: carBitVIP,
    //     color: ['#67FFE8', '#0063D3', '#00FFFC'],
    //     value: calculatePercent(carBitVIP, carBitCount),
    //     offsetCenter: '80%'
    // });
    // initYi('carBitChargePark', {
    //     dom: "chargeSpace",
    //     domValue: carBitChargePark,
    //     color: ['#11ECB6', '#6AF5E0', '#1FEDBD'],
    //     value: calculatePercent(carBitChargePark, carBitCount),
    //     offsetCenter: '80%'
    // });
}
function initYi(dom, param) {
    var space = document.getElementById(param.dom);
    space.innerText = param.domValue;
    var carYi = document.getElementById(dom);
    var myYi = echarts.init(carYi);
    var options = {
        series: [
            {
                type: 'gauge',
                progress: {
                    show: true,
                    width: 14,
                    itemStyle: {
                        color: {
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
                            global: false // 缺省为 false
                        }
                    }
                },
                axisLine: {
                    lineStyle: {
                        width: 14,
                        color: [
                            [1, '#062259']
                        ]
                    }
                },
                pointer: {
                    show: false
                },
                axisTick: {
                    show: false
                },
                splitLine: {
                    show: false
                },
                axisLabel: {
                    show: false
                },
                anchor: {
                    show: false,
                },
                title: {
                    show: false
                },
                radius: '100%',
                detail: {
                    valueAnimation: true,
                    fontSize: 13,
                    color: param.color[2],
                    offsetCenter: [0, param.offsetCenter],
                    formatter: '{value}%'
                },
                data: [
                    {
                        value: param.value
                    }
                ]
            }
        ]
    };
    myYi.setOption(options);
    window.addEventListener("resize", function () {
        myYi.resize();
    });
}
function initZhe(userTotalNumByMonth) {
    var arrNum = [];
    var arrMonth = [];
    if (userTotalNumByMonth) {
        userTotalNumByMonth.forEach(item => {
            arrNum.push(+item.activeUsers + (+basicDataObj.monthlyActiveUsers));
            // arrNum.push(+item.monthTotalNum + (+basicDataObj.monthlyActiveUsers));
            var str = item.perMonth.split('-')[1];
            var s = str.split('');
            if (s[0] === '0') {
                str = s[1];
            }
            arrMonth.push(str + "月");
        });
        var monthZhe = document.getElementById('monthZhe');
        var myZhe = echarts.init(monthZhe);
        var option = {
            tooltip: {
                trigger: 'item',
                extraCssText: 'padding: 8px 10px 4px; line-height: 1.5; font-size: 14px;text-align: center;',
                // extraCssText: 'padding: 8px 10px 4px; line-height: 1.5; font-size: 14px;background-color:rgba(77,81,255,0.17); color:#05C7FF; text-align: center;',
                position: "top",
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
    }

}
function initBar(dom, params, istodvn) {
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
            extraCssText: 'padding: 8px 10px 4px; line-height: 1.5; font-size: 16px;text-align: center;border: 1px solid #fff;',
            // extraCssText: 'padding: 8px 10px 4px; line-height: 1.5; font-size: 16px;background-color:rgba(77,81,255,0.17); color:#05C7FF; text-align: center;border: 1px solid #fff;',
            position: "top",
            formatter: function (params) {
                let str = params.name + '</br>' + params.value + (istodvn ? '万次' : '次');
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
            name: (istodvn ? '万次' : '次'),
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
            left: '4%',
            right: '2%',
            containLabel: true,
            borderWidth: 0
        }
    };
    bar.setOption(option);
    window.addEventListener("resize", function () {
        bar.resize();
    });
};

function initBar2(dom, params, istodvn) {
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
            extraCssText: 'padding: 8px 10px 4px; line-height: 1.5; font-size: 16px;text-align: center;',
            // extraCssText: 'padding: 8px 10px 4px; line-height: 1.5; font-size: 16px;background-color:rgba(77,81,255,0.17); color:#05C7FF; text-align: center;border: 1px solid #fff;',
            position: "top",
            formatter: function (params) {
                let str = params.name + '</br>' + params.value + (istodvn ? '万' : '') + "(小时)";
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
            name: (istodvn ? '万' : '') + "(小时)",
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
            left: '4%',
            right: '2%',
            containLabel: true,
            borderWidth: 0
        }
    };
    bar.setOption(option);
    window.addEventListener("resize", function () {
        bar.resize();
    });
};

function initPie(dom, param, total) {
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
                    sum += item.value;
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
            trigger: 'item'
        },
        series: series

    };
    pie.setOption(option);
    window.addEventListener("resize", function () {
        pie.resize();
    });
    let domNum = "#" + dom + "Total"
    $(domNum).html(total);


}
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

function calculateMapData(data, allData) {
    var geoc = new BMap.Geocoder();
    let regions = [];
    data.forEach((item, index) => {
        geoc.getLocation(new BMap.Point(item.value[0], item.value[1]), function (rs) {
            if (rs) {
                let province = rs.addressComponents.province.split('省')[0];
                let findItem = regions.find(item => item.name === province)
                if (!findItem) {
                    regions.push({
                        name: province,
                        itemStyle: {
                            areaColor: "rgba(24, 67, 255, .6)",
                            borderColor: "#19F3FF",
                            borderWidth: 2
                        }
                    });
                }
            }
            if (index === data.length - 1) {
                initMap(data, allData, regions);
            }
        });
    });
}
function initMap(data, allData, regions) {
    posHandler.reset();
    var myMap = echarts.init(document.getElementById("chinaMap"));
    var option = {
        geo: {
            map: 'china',
            roam: true,
            top: '10%',
            label: {
                fontSize: 16,
                color: "#fff",
            },
            labelLine: {
                show: true
            },
            zoom: 1.25,
            itemStyle: {
                areaColor: "RGBA(7, 30, 68, .6)",
                borderColor: "#19F3FF",
            },
            emphasis: {
                label: {
                    show: true,
                    color: "#fff"
                },
                itemStyle: {
                    areaColor: "rgba(24, 67, 255, .6)",
                    borderColor: "#19F3FF",
                    borderWidth: 2
                }
            },
            regions: regions || '',
            tooltip: {
                show: true,
                trigger: 'item',
                formatter: function (params) {
                    if (params.value === undefined) {
                        return ""
                    } else {
                        var str = '';
                        allData.forEach(item => {
                            if (params.name === item.mapName) {
                                str = params.name + "<br />" + "空闲车位：" + item.free + "个<br />" + "占用车位：" + item.occupy + "个";
                            }
                        })
                        return str
                    }
                },
                textStyle: {
                    fontSize: 18
                }
            }
        },
        tooltip: {},
        series: [
            {
                type: "effectScatter",
                showEffectOn: 'render',
                coordinateSystem: "geo",
                itemStyle: {
                    color: "#00D2FF"
                },
                rippleEffect: {
                    brushType: "stroke"
                },
                zlevel: 2,
                hoverAnimation: true,
                data: data,
                label: {
                    show: true,
                    formatter: "{b}",
                    position: "right",
                    textBorderColor: '#003A9B',
                    textBorderWidth: 4,
                    color: "#fff",
                    fontSize: 18
                },
                labelLine: {
                    show: true,
                    length2: 2,
                    smooth: 0.2,
                    minTurnAngle: 90,
                    lineStyle: {
                        color: '#fff',
                        width: 2
                    }
                },
                labelLayout: function (params) {
                    var dPos = posHandler.getDeltaPos(params);
                    return {
                        dx: dPos.dx,
                        dy: dPos.dy
                    }
                },
                symbolSize: "14",
            }
        ]
    };
    myMap.setOption(option);
    myMap.on('georoam', function () {
        posHandler.reset();
        myMap.resize();
    })
    myMap.on('click', function (params) {
        if (params.data) {
            var mapId = '';
            allData.forEach(item => {
                if (item.mapName === params.data.name) {
                    mapId = item.mapId
                }
            });
            window.open(url + "page/view.html?mapId=" + mapId);
        }
    });
    window.addEventListener("resize", function () {
        posHandler.reset();
        myMap.resize();
    });
}
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

// 显示与隐藏
function openANDclose(dom) {
    let hideDom;

    if (dom == 'mainMiddleTop') {
        hideDom = "." + dom + " .viewBGImage";
        let hae = $(hideDom).hasClass('closeClass1');
        if (hae) {
            $(hideDom).removeClass('closeClass1');
        } else {
            $(hideDom).addClass('closeClass1');
        }

    } else if (dom == 'mainMiddleFooter') {
        hideDom = "." + dom + " .footerView";
        let hae = $(hideDom).hasClass('closeClass2');
        if (hae) {
            $(hideDom).removeClass('closeClass2');
        } else {
            $(hideDom).addClass('closeClass2');
        }
    }
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
/* 选择下拉内容 */
function chooseTimeSlot(type, numData) {
    $(".selectUl").removeClass('showSelect');

    if (timeData.time == numData) return;
    timeData = {
        time: numData
    }

    if (type == 0) {
        $(".selectName").html('一年');
        $(".dayTitle").html('(近一年数据)')
    } else if (type == 1) {
        $(".selectName").html('一个月');
        $(".dayTitle").html('(近30天数据)')
    } else if (type == 2) {
        $(".selectName").html('六个月');
        $(".dayTitle").html('(近六个月数据)')
    } else if (type == 3) {
        $(".selectName").html('三个月');
        $(".dayTitle").html('(近90天数据)')
    };

    // 改变统计时间区间，重新调用接口
    apiGetDataState();
};

/* 使用api获取数据 */
function apiGetDataState() {

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

    // 用户总数
    getUsersTotal();
    // 访问总次数
    getVisitsTotal();
    // 新增用户数
    getWithinThreeMonthsNewUsers();
    // 月活跃用户数
    getActiveUserNumber();
    // 用户搜索总数
    getUserSearchTotal();
    // 位置分享总数
    getLocationShareTotal();

    // 用户检索前10停车场
    // getTop10ParkingPlaces();
    // 用户检索前10商家
    getTop10Business();

    // 停车场总数
    emsbpgetMapName();

    // 月活用户数-拆线图
    getMonthActiveUserNumber();

};

// 用户总数
function getUsersTotal() {
    $.ajax({
        url: url + 'view/getManyMapUsersTotal',
        data: timeData,
        success: function (res) {
            console.log('地图用户总数', res);
            if (res.code != 200) {
                return;
            };

            let activeUsers = +basicDataObj.userTotal;
            res.data.forEach((item) => {
                activeUsers += +item.userCount || 0
            });
            $("#userTotal").html(activeUsers);

        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

// 访问总次数
function getVisitsTotal() {
    $.ajax({
        url: url + 'view/getManyMapCumulativeUseFrequency',
        data: timeData,
        success: function (res) {
            console.log('访问总次数', res);
            if (res.code != 200) {
                return;
            };

            let useFrequency = +basicDataObj.useFrequency;
            res.data.forEach((item) => {
                useFrequency += +item.useFrequency || 0
            });
            $("#visits").html(useFrequency);

        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

function emsbpgetMapName() {
    $.ajax({
        url: url + 'emsbp/getMapName',
        data: timeData,
        success: function (res) {
            console.log('emsbpgetMapName', res);
            if (res.code != 200) {
                return;
            };

            let tottal = res.data.length;
            $("#parkadeTotal").html(tottal);

            parkadeInfo = res.data.map((item) => {
                return {
                    map: item.id,
                    mapName: item.name
                }
            });

            // 车位使用次数总数
            getPlaceUseTotal();
            // 车位空闲总时长
            getPlaceldleTotalDuration();
            // 停车场车位利用率
            getMapPlaceUtilizationRate();
            // 空车位数据
            getIdlePlaceNumber();

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
                let newUserTotal = +basicDataObj.newUsersNumber + data.newUserTotal
                $("#usersNew").html(newUserTotal)
            }

        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

/* 用户检索前10停车场 */
function getTop10ParkingPlaces() {
    $.ajax({
        url: url + 'view/getTop10ParkingPlaces',
        data: timeData,
        success: function (res) {
            console.log('用户检索前10停车场', res);
            if (res.code != 200) {
                return;
            }

            let html = ''
            res.data.forEach((item, index) => {

            });



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

            res.data.forEach((item) => {
                item.businessSearchCount = +item.businessSearchCount + (+basicDataObj.top10Business)
            })

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

/* 车位使用次数总数 */
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

            res.data.forEach((item) => {
                let findItem = parkadeInfo.find((pitem) => pitem.map == item.map);
                if (findItem) {
                    item.mapName = findItem.mapName
                }
            });

            res.data.length = 10;

            let dataTotal = +basicDataObj.placeUseTotal * 10;
            let xArr = [];
            let yArr = [];
            res.data.forEach((item) => {
                dataTotal = +item.total;
                xArr.push(item.mapName);
                yArr.push((+basicDataObj.placeUseTotal + (+item.placeUseTotal)));
            });
            dataTotal = dataTotal / 10000;
            $("#carUseBarNum").html((dataTotal).toFixed(2) + '万')

            let minYitem = Math.min.apply(null, yArr);
            if (minYitem > 10000) {
                yArr = yArr.map((item) => {
                    return (item / 10000).toFixed(2)
                })
            } else {
                yArr = yArr.map((item) => {
                    return item
                })
            }

            initBar("carUseBar", {
                xValue: xArr,
                yValue: yArr,
                color: ['#B6A4FF', '#5530E9']
            }, minYitem > 10000);

        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

/* 车位空闲总时长 */
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

            res.data.forEach((item) => {
                let findItem = parkadeInfo.find((pitem) => pitem.map == item.map);
                if (findItem) {
                    item.mapName = findItem.mapName
                }
            });

            let xArr = [];
            let yArr = [];

            res.data.sort((a, b) => {
                return b.totalVacantDuration - a.totalVacantDuration
            });

            res.data.forEach((item) => {
                xArr.push(item.mapName);
                yArr.push(+basicDataObj.placeIdleTotalDuration + (item.totalVacantDuration / 60 / 60))
            });

            let maxYitem = Math.max.apply(null, yArr);
            if (maxYitem > 10000) {
                yArr = yArr.map((item) => {
                    return (item / 10000).toFixed(2)
                })
            } else {
                yArr = yArr.map((item) => {
                    return item.toFixed(2)
                })
            }


            initBar2("spacesTotalTime", {
                xValue: xArr,
                yValue: yArr,
                color: ['#00F6FF', '#002DD3']
            }, maxYitem > 10000);

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
            let allTotal = +basicDataObj.reservationTotal;
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

            let platformUtilizationRate = +basicDataObj.platformPlaceUtilizationRate;
            res.data.forEach((item) => {
                platformUtilizationRate += +item.platformUtilizationRate
            });

            platformUtilizationRate = numbersToPercentage(platformUtilizationRate);

            $("#carParkUsage").html(platformUtilizationRate + '%');
            $("#carParkUsage").attr('title', platformUtilizationRate + '%');
            $("#carParkUsageH").css('height', (platformUtilizationRate >= 100 ? 100 : platformUtilizationRate) + '%');


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

            let placeAvailabilityRate = +basicDataObj.placeAvailabilityRate;
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

            let locationShareTotal = +basicDataObj.locationShareTotal;
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

// 每小时空车位数
function getPerHourNullPlaceNumber() {
    $.ajax({
        url: url + 'view/getPerHourNullPlaceNumber',
        data: timeData,
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res) {
            console.log('每小时空车位数', res);
            if (res.code != 200) {
                return;
            };
            if (res.data.length == 0) {
                return;
            };


            let addNum = +basicDataObj.perHourNullPlaceNumber

            let arrH = [];
            let arrH2 = [];
            let arrNum = [];
            let revData = res.data;
            revData.forEach((item) => {

                let sD = item.hourStart.split(" ")[0];
                let sT = item.hourStart.split(" ")[1];
                let sT0 = sT.split(":")[0];
                let sT1 = sT.split(":")[1];

                let eD = item.hourEnd.split(" ")[0];
                let eT = item.hourEnd.split(" ")[1];
                let eT0 = eT.split(":")[0];
                let eT1 = eT.split(":")[1];

                arrH.push(sT0);
                arrH2.push(sD + ' ' + sT0 + ':' + sT1 + '~' + eT0 + ':' + eT1);
                arrNum.push(addNum + item.nullPlaceNumber);
            });



            var emptyParkSpaces = document.getElementById('emptyParkSpaces');
            var myZhe = echarts.init(emptyParkSpaces);
            var option = {
                tooltip: {
                    trigger: 'item',
                    extraCssText: 'padding: 8px 10px 4px; line-height: 1.5; font-size: 14px;text-align: center;',
                    // extraCssText: 'padding: 8px 10px 4px; line-height: 1.5; font-size: 14px;background-color:rgba(77,81,255,0.17); color:#05C7FF; text-align: center;',
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
            window.addEventListener("resize", function () {
                myZhe.resize();
            });


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
            }
            let placeNavigationTotal = 0;
            res.data.forEach((item) => {
                placeNavigationTotal += +item.placeNavigationUseRate
            });

            placeNavigationTotal = numbersToPercentage((+basicDataObj.placeNavigationUseRate / 100) + placeNavigationTotal);

            $("#carParkNaviga").html(placeNavigationTotal + '%')
            $("#carParkNaviga").attr('title', placeNavigationTotal + '%')
            $("#carParkNavigaH").css('height', placeNavigationTotal >= 100 ? 100 : placeNavigationTotal + '%')


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

            let reverseCarSearchTotal = +basicDataObj.reverseCarSearchTotal;
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

// 停车场车位利用率
function getMapPlaceUtilizationRate() {
    $.ajax({
        url: url + 'view/getMapPlaceUtilizationRate',
        data: timeData,
        success: function (res) {
            console.log('停车场车位利用率', res);
            if (res.code != 200) {
                return;
            };
            if (!res.data.length) return;

            res.data.forEach((item) => {

                item.mapPlatformUtilizationRate = +item.mapPlatformUtilizationRate + (+basicDataObj.mapPlaceUtilizationRate)

                let findItem = parkadeInfo.find((pitem) => pitem.map == item.map);
                if (findItem) {
                    item.mapName = findItem.mapName
                }
            });

            let html = '';
            res.data.forEach((item, index) => {
                item.mapPlatformUtilizationRate = numbersToPercentage(+item.mapPlatformUtilizationRate)
                if (index == 0) {
                    html += `
                    <div class="tableUl">
                        <div class="tableLi1">
                            <img src="../image/common/ranking1.png" alt="" srcset="">
                        </div>
                        <div class="tableLi2">
                            <div class="tableLi2d">
                                <span>${item.mapName}</span>
                            </div>
                        </div>
                        <div class="tableLi3">${+item.mapPlatformUtilizationRate}%</div>
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
                                <span>${item.mapName}</span>
                            </div>
                        </div>
                        <div class="tableLi3">${+item.mapPlatformUtilizationRate}%</div>
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
                                <span>${item.mapName}</span>
                            </div>
                        </div>
                        <div class="tableLi3">${+item.mapPlatformUtilizationRate}%</div>
                    </div>
                    `
                } else {
                    html += `
                    <div class="tableUl">
                        <div class="tableLi1">${index + 1}</div>
                        <div class="tableLi2">
                            <div class="tableLi2d">
                                <span>${item.mapName}</span>
                            </div>
                        </div>
                        <div class="tableLi3">${+item.mapPlatformUtilizationRate}%</div>
                    </div>
                    `
                }
            });
            $("#parkingSpaceUtilizationRate").html(html)


        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
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

            let placeNavigationTotal = +basicDataObj.placeNavigationTotal;
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

// 空车位数据
function getIdlePlaceNumber() {
    $.ajax({
        url: url + 'view/getIdlePlaceNumber',
        data: timeData,
        success: function (res) {
            console.log('空车位数据', res);
            if (res.code != 200) {
                return;
            };
            if (!res.data.length) return;

            res.data.sort((a, b) => {
                return (+b.idlePlaceNumber) - (+a.idlePlaceNumber)
            });

            res.data.forEach((item) => {
                item.idlePlaceNumber = +item.idlePlaceNumber + (+basicDataObj.idlePlaceNumber)
                let findItem = parkadeInfo.find((pitem) => pitem.map == item.map);
                if (findItem) {
                    item.mapName = findItem.mapName
                }
            });

            let html = '';
            res.data.forEach((item) => {
                html += `
                <div class="tableUl">
                    <div class="tableLi1">
                        <div class="tableLi1d">
                            <span>${item.time.split(" ")[0]}</span>
                        </div>
                    </div>
                    <div class="tableLi2">
                        <div class="tableLi2d">
                            <span>${item.mapName}</span>
                        </div>
                    </div>
                    <div class="tableLi3">${item.idlePlaceNumber}</div>
                </div>
                `;
            });
            $("#emptyParkingSpaceData").html(html)

        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

// 月活跃用户数
function getActiveUserNumber() {
    $.ajax({
        url: url + 'view/getActiveUserNumber2',
        data: timeData,
        success: function (res) {
            console.log('月活跃用户数', res);
            if (res.code != 200) {
                return;
            };

            let activeUsers = +basicDataObj.activeUserTotal;
            res.data.forEach((item) => {
                activeUsers += +item.activeUsers
            });
            $("#usersActive").html(activeUsers);

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

            let userSearchTotal = +basicDataObj.userSearchTotal;
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


// 月活用户数-拆线图
function getMonthActiveUserNumber() {
    $.ajax({
        url: url + 'view/getActiveUserNumber',
        // url: url + 'view/getMonthActiveUserNumber',
        data: timeData,
        success: function (res) {
            console.log('月活用户数-拆线图', res);
            if (res.code != 200) {
                return;
            };
            if (!res.data.length) return;

            initZhe(res.data);

        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
};

/* 保留小数点后4位，再转保留小数点后2位的百分比 */
function numbersToPercentage(num) {
    var result = Math.round(num * 10000) / 100;
    return result;
};