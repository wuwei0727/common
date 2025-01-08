// 正式版-记录版本号 V:1.0.1
// 体验版-记录版本号 V:1.0.8

var socket;
var lockReconnect = false; //避免重复连接
var isIos = false;
var whetherBreakSocket = false; // socket是否断开连接
var websocket_time = new Date();
var mapInfo;
var imageMarker;
var placeData = [];// 车位数据
var getPlaceDataToMap = null;
var place_type = [200401, 340862, 340860, 340859, 340861, 340863, 340878, 340879];
var chooseData;

// var formal = 'https://tuguiyao-gd.com/UWB/';
var formal = 'http://192.168.1.124:8081/'
// var formal = 'http://112.94.22.123:10088/UWB/';

// 搜索结果
var searchData = [];

// 是否可以打开floorBox
var canOpenBox = true;
// 只做一次车位列表显示
var fristShowPlaceList = true;

$(function () {
  urlInitMap();
  isIos = isIosorAndroid();
  connectWebsocket();

  addInputDel();
});

function addInputDel() {
  let html = `
    <div class="delBtnBox2" onclick="delInputBtn()">
      <div class="delBtn2"></div>
    </div>
  `;
  $("#variable").append(html)
};

function noshownext() {
  $(".btnBox .btn").css('display', 'none');
};

function shownext() {
  $(".btnBox .btn").css('display', 'flex');

};

// 小程序url带地图初始化参数
function urlInitMap() {
  let encode_data = getUrlStr('mapInfo');
  let decode_data = decodeURIComponent(encode_data);
  mapInfo = JSON.parse(decode_data)

  initMap(mapInfo);
};

//重连
function reconnect() {
  if (lockReconnect) return;
  lockReconnect = true;
  //没连接上会一直重连，设置延迟避免请求过多
  setTimeout(function () {
    whetherBreakSocket = true; // socket断开连接-需要重连说明断开了
    connectWebsocket();
    lockReconnect = false;
  }, 5000);
};

//心跳检测
var heartCheck = {
  timeout: 10000, //10秒，针对不同项目设置不同时间，比如客服系统就不用那么频繁检测，股票交易所就必须很频繁
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

        if (diff > 15) {

          whetherBreakSocket = true; // socket断开连接

          if (isIos) {
            reconnect();
          } else {
            socket.close(); //如果onclose会执行reconnect，我们执行ws.close()就行了.如果直接执行reconnect 会触发onclose导致重连两次
          }

          websocket_time = new Date();
        } else {
        }


      }, self.timeout)
    }, this.timeout)
  }
};

function isIosorAndroid() {
  var u = navigator.userAgent;
  var isiOS = false;
  isiOS = !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/); //ios终端
  return isiOS;
}

function connectWebsocket() {

  userId = getUrlStr('userId');
  socket = new WebSocket('wss://tuguiyao-gd.com/UWB/websocket/h5WebSocket/' + userId);
  //socket = new WebSocket("ws://192.168.1.95:8083//UWB/websocket/h5WebSocket/" + userId);
  // socket = new WebSocket("ws://112.94.22.123:10087/UWB/websocket/h5WebSocket/" + userId);
  // socket = new WebSocket("ws://112.94.22.123:10088/UWB/websocket/h5WebSocket/" + userId);
  // socket = new WebSocket("ws://192.168.1.131:8083/UWB/websocket/h5WebSocket/" + userId);
  // socket = new WebSocket("ws://192.168.1.124:8081/websocket/h5WebSocket/" + userId);
  // socket = new WebSocket("ws://112.94.22.123:28080/UWB/websocket/h5WebSocket/1");

  socket.onopen = function () {

    websocket_time = new Date();
    if (!placeData.length) {
      socket.send(JSON.stringify({
        todo: "connect",
        data: {
          connect: true,
          userId: userId
        }
      }));
    }


    heartCheck.reset().start();//连接成功之后启动心跳检测机制

  };
  socket.onmessage = function (res) {

    websocket_time = new Date();
    var data
    if (res.data) {
      data = JSON.parse(res.data);
    } else {
      data = res.data;
    }
    if (data.todo == "getplaceData") {
      placeData = data.result;
      showPlaceDataList();
    }
  };

  socket.onclose = function (res) {
    reconnect();
  };

  socket.onerror = function (res) {
    reconnect();
  }
};

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
    mapZoom: 18,
    zoomRange: [12, 23],
    floorSpace: 20,
    tiltAngle: 70,
  };
  fMap = new fengmap.FMMap(options);
  fMap.on('loaded', function () {
    addFMapControl(10);
    initPlaceDomeColor();

    fmaploadedsuccess();

    if (placeData.length) {
      setPlaceDomeColor();
    } else {
      getPlaceDataToMap = setInterval(() => {
        if (placeData.length) {
          setPlaceDomeColor();
          clearInterval(getPlaceDataToMap);
          getPlaceDataToMap = null;
        }
      }, 500);
    };

    // 设置全览模式
    let bound = fMap.bound;
    fMap.setFitView(bound, false, () => { });
  });

  fMap.on('click', function (event) {
    if (!event.targets.length) return;


    let target = event.targets[0];
    if (!contains(place_type, target.typeID)) return;

    console.log(target);
    let { x, y, level, FID } = target;

    if (!placeData.length) {
      return
    };


    let hasFid = placeData.some((item) => {
      return item.fid == FID
    });

    if (hasFid) {

      shownext();
      chooseData = placeData.find((item) => item.fid == FID);
      let floorName = "(" + fMap.getFloor(+chooseData.floor).name + "层)";
      $(".inputBox .input").addClass("inputV");
      chooseData['floorName'] = floorName;

      $(".inputBox .delBtnBox").css('display', 'block')
      $("#variable").html(`
        <div class="textBox"><span class="textColor">${chooseData.name}</span><span class="textColor2">${floorName}</span></div>
      `)

      // 添加图片标注
      /* 构造 Marker */
      if (imageMarker) {
        imageMarker.remove();
      }
      imageMarker = new fengmap.FMImageMarker({
        x: +event.coords.x,
        y: +event.coords.y,
        url: './image/location-marker.png',
        size: 30,
        height: 2,
        collision: false
      });
      const floor = fMap.getFloor(+event.level)
      imageMarker.addTo(floor);
    } else {
      showTipNoPlace();
    }
  });
};

function fmaploadedsuccess() {
  socket.send(JSON.stringify({
    todo: "fmaploadedsuccess",
  }));
};


function getUrlStr(name) {
  var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
  var r = window.location.search.slice(1).match(reg);
  if (r != null) {
    return r[2];
  }
  return null;
};

function addFMapControl(y) {

  $('.emptybox').css('top', y + 'px');

  if ($('.fm-compass-container').length < 1) {
    var scrollCompassCtlOpt = {
      position: fengmap.FMControlPosition.Left_TOP,
      offset: {
        x: 13,
        y: y
      },
    };
    compass = new fengmap.FMCompass(scrollCompassCtlOpt);
    compass.addTo(fMap);
    compass.on('click', function () {
      fMap.setRotation({
        rotation: 0,
        animate: true,
        duration: 0.3,
        finish: function () {
        }
      })
    });
  }
  if ($('.fm-control-groups').length < 1) {
    var scrollFloorCtlOpt = {
      position: fengmap.FMControlPosition.RIGHT_TOP,
      floorButtonCount: 3,
      offset: {
        x: -7,
        y: y
      },
      viewModeControl: false,
      floorModeControl: true,
      needAllLayerBtn: true
    };
    scrollFloorControl = new fengmap.FMToolbar(scrollFloorCtlOpt);
    scrollFloorControl.addTo(fMap);
  }
};

var searchkeyTime = null;
// 输入框输入
function searchInput(e) {
  let value = $("#searchInput")[0].value;

  if (!value.length) {
    $(".inputBox .input").removeClass("inputV");
    $("#variable .delBtnBox2").css('display', 'none');
    chooseData = {};
    noshownext();
    // delBtn();
  } else {
    $("#variable .delBtnBox2").css('display', 'block');


    if (!searchkeyTime) {
      searchkeyTime = setTimeout(() => {
        clearTimeout(searchkeyTime);
        searchkeyTime = null;

        onSearchKeyWork(value);

      }, 16);
    }

  }
  // if (!placeData.length) {
  //   return
  // };
  // let hasFid = placeData.some((item) => {
  //   return item.name == value
  // });
  // if (hasFid) {
  //   chooseData = placeData.find((item) => item.name == value);
  //   setTimeout(() => {
  //     pointAndNaviFid(chooseData.fid)
  //   }, 100);
  // }
};

/* 清空输入框 */
function delInputBtn() {
  document.getElementById('searchInput').value = '';

  $("#variable .delBtnBox2").css('display', 'none');

  $(".inputBox .input").removeClass("inputV");
  chooseData = {};
  noshownext();
  delBtn();
};

// 初始化车位颜色
function initPlaceDomeColor() {
  var levels = fMap.getLevels();
  for (var i = 0; i < levels.length; i++) {
    var floor = fMap.getFloor(levels[i]);
    var layers = floor.getLayers(fengmap.FMType.MODEL_LAYER)[0];
    const model = layers.getFeatures();
    model.forEach((item) => {
      if (contains(place_type, item.typeID)) {
        item.setColor('#aaa', 1);
        item.setBorderColor('#737373', 1);
      }
    })

  }
};

// 设置可选车位颜色
function setPlaceDomeColor() {
  var levels = fMap.getLevels();
  let data = placeData;
  if (data.length) {
    for (var i = 0; i < levels.length; i++) {
      var floor = fMap.getFloor(levels[i]);
      var layers = floor.getLayers(fengmap.FMType.MODEL_LAYER)[0];
      data.forEach((d) => {
        const model = layers.getFeatures().find(item => item.FID === d.fid);
        if (model) {
          if (d.nervous) {
            model.setColor('#FFB22B', 1);
          } else {
            model.setColor('#07C864', 1);
          }
        }
      })
    }
  }
};

function contains(arr, val) {
  return arr.some(item => item === val);
};

function showTipNoPlace(text) {
  if (text) {
    $("#showText").html(text)
  }
  $("#showTip").css('display', 'block');
  setTimeout(() => {
    $("#showTip").css('display', 'none');
  }, 1500);
};

// 下一步
function next() {
  if (!chooseData || JSON.stringify(chooseData) == '{}') {
    showTipNoPlace('请选择车位');
    return
  };
  //打包数据，发送给小程序接收区
  wx.miniProgram.postMessage({
    data: JSON.stringify(chooseData)
  });
  //返回上级，触发微信小程序获取信息函数，读取之前发送的数据
  wx.miniProgram.navigateBack({
    delta: 1,
  });

};


async function pointAndNaviFid(fid) {
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
      zoom: 21
    });
    if (imageMarker) {

      imageMarker.remove();
    }
    /* 构造 Marker */
    imageMarker = new fengmap.FMImageMarker({
      x: model.center.x,
      y: model.center.y,
      url: './image/location-marker.png',
      size: 30,
      height: 2,
      collision: false
    });
    const floor = fMap.getFloor(model.level)
    imageMarker.addTo(floor);


    let floorName = "(" + fMap.getFloor(+chooseData.floor).name + "层)";
    chooseData['floorName'] = floorName;
    $(".inputBox .delBtnBox").css('display', 'block')
    $("#variable").html(`
        <div class="textBox"><span class="textColor">${chooseData.name}</span><span class="textColor2">${floorName}</span></div>
      `)
  } else {
    //设置视图
    fMap.setLevel({
      level: +chooseData.floor,
    });
    fMap.setCenter({
      x: +chooseData.x,
      y: +chooseData.y,
    });
    fMap.setZoom({
      zoom: 21
    });
    if (imageMarker) {

      imageMarker.remove();
    }
    /* 构造 Marker */
    imageMarker = new fengmap.FMImageMarker({
      x: +chooseData.x,
      y: +chooseData.y,
      url: './image/location-marker.png',
      size: 30,
      height: 2,
      collision: false
    });
    const floor = fMap.getFloor(+chooseData.floor)
    imageMarker.addTo(floor);


    let floorName = "(" + fMap.getFloor(+chooseData.floor).name + "层)";
    chooseData['floorName'] = floorName;
    $(".inputBox .delBtnBox").css('display', 'block')
    $("#variable").html(`
        <div class="textBox"><span class="textColor">${chooseData.name}</span><span class="textColor2">${floorName}</span></div>
      `)
  }
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
};

async function searchByParams(params) {
  let sortRes = [];
  var proResult = await new Promise((resolve, reject) => {
    searchAnalyser = new fengmap.FMSearchAnalyser({ map: fMap }, function () {
      const searchRequest1 = new fengmap.FMSearchRequest();
      let obj = {};

      //配置FID参数
      if (params.FID) {
        obj.FID = params.FID;
      }


      searchRequest1.addCondition(obj);
      searchAnalyser.query(searchRequest1, (result) => {
        sortRes = result;
      });

      resolve(sortRes);

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

// 删除按钮
function delBtn() {
  $(".inputBox .delBtnBox").css('display', 'none')
  $("#variable").html(`
    <input class="input" id="searchInput" onInput="searchInput(event)" type="text" placeholder="搜索目的地">
    <div class="delBtnBox2" onclick="delInputBtn()">
      <div class="delBtn2"></div>
    </div>
  `)
  chooseData = {};
  noshownext();
  if (imageMarker) {
    imageMarker.remove();
  };

  // 还原到可约车位列表
  fristShowPlaceList = true;
  $("#list_title").html('可约车位');
  showPlaceDataList();
};

// 显示车位列表
function showPlaceDataList() {
  console.log('placeData', placeData);
  if (placeData.length && fristShowPlaceList) {
    let html = '';
    placeData.forEach((item) => {
      let floorname = "(" + fMap.getFloor(+item.floor).name + "层)"
      html += `
        <div class="list_main_item" onclick="clickItem(${item.id})">${item.name}<span class="floorname">${floorname}</span></div>
      `
    });
    $("#list_main").html(html);
    fristShowPlaceList = false;
  }
};

// 点击可约车位
function clickItem(id) {
  try {
    shownext();
    chooseData = placeData.find((item) => item.id == id);
    console.log('点击可约车位', chooseData);
    if (chooseData && chooseData.id) {
      if (chooseData.fid) {
        setTimeout(() => {
          pointAndNaviFid(chooseData.fid)
        }, 100);
      }
    } else {
      showTipNoPlace('请稍后再试');
    }

  } catch (error) {
    showTipNoPlace('请稍后再试');
  }


};

// 搜索
async function onSearchKeyWork(keyword) {
  searchData = [];

  let searchData1 = await searchCBData(keyword);
  let searchData2 = await searchPlaceData(keyword);

  searchData.push(...searchData1, ...searchData2)
  console.log('searchData', searchData);

  if (searchData.length) {
    switchToSearchData(searchData);
  } else {
    // // 还原到可约车位列表
    // fristShowPlaceList = true;
    // $("#list_title").html('可约车位');
    // showPlaceDataList();
    switchToSearchData(null);
  }
};

// 搜索公司或商家
async function searchCBData(keyword) {
  let sortRes = [];
  var proResult = await new Promise((resolve, reject) => {
    $.ajax({
      url: `${formal}wechat/getTargetByName`,
      type: 'GET',
      data: {
        name: keyword,
        map: mapInfo.mapId
      },
      success: function (res) {
        console.log('搜索公司或商家', res);

        const data = res.data;
        if (!data || data.length == 0) {
          resolve(sortRes)
          return;
        };

        // 查询到数据后
        sortRes = data.map((item) => {
          return {
            name: item.name,
            x: item.x,
            y: item.y,
            level: item.floor,
            fid: item.fid,
            category: '2', // 搜索公司或商家
            nodeType: item.type,
          }
        });
        resolve(sortRes)
      }
    })
  });
  let res = sortRes;
  return res;
};

// 搜索车位
async function searchPlaceData(keyword) {
  let sortRes = [];
  var proResult = await new Promise((resolve, reject) => {
    $.ajax({
      url: `${formal}wechat/getPlaceById?map=${mapInfo.mapId}&name=${keyword}`,

      success: function (res) {
        console.log('搜索车位', res);

        const data = res.data;
        if (!data || data.length == 0) {
          resolve(sortRes);
          return false;
        };

        var data2 = [];
        data.forEach((item) => {
          data2.push(...(item.places))
        });


        sortRes = data2.map((item) => {
          return {
            name: item.name,
            x: item.x,
            y: item.y,
            level: item.floor,
            fid: item.fid,
            category: '1', // 搜索车位
            nodeType: item.type,
          }
        });
        resolve(sortRes)
      }
    });
  });
  let res = sortRes;
  return res;
};

// 显示搜索的内容
function switchToSearchData(data) {
  $("#list_title").html('目的地');
  let html = '';
  if (data && data.length) {
    data.forEach((item) => {
      let floorname = ''
      if (item.level) {
        floorname = "(" + fMap.getFloor(+item.level).name + "层)"
      }
      html += `
          <div class="list_main_item" onclick="clickSearchItem(${item.fid})">${item.name}<span class="floorname">${floorname}</span></div>
        `
    });
  } else {
    html = '<div style="text-align:center;padding:15px 0;color:#919191">搜索不到该目的地</div>'
  }

  $("#list_main").html(html);
};

// 选择搜索的目的地
function clickSearchItem(fid) {
  let clickData = searchData.find((item) => item.fid == fid);
  recommendedParkingSpaces(clickData)
};

// 走推荐车位逻辑
function recommendedParkingSpaces(target) {
  console.log('走推荐车位逻辑');
  $.ajax({
    url: `${formal}park/reservePlace`,
    type: 'GET',
    data: {
      // companyName: '522',
      x: target.x,
      y: target.y,
      floor: target.level,

      carBitType: '0',
      isVip: '1',
      map: mapInfo.mapId
    },
    success: function (res) {
      console.log('走推荐车位逻辑', res);

      const data = res.data;
      if (!data || data.length == 0) {
        return;
      };

      // 查询到数据后
      // chooseReservePlaceData(data[0]);
      /* 查询到数据后 */
      showReservePlaceData(data)
    }
  })
};

function showReservePlaceData(data) {
  $("#list_title").html('可约车位');

  let html = '';

  data.forEach((item, index) => {
    let floorname = "(" + fMap.getFloor(+item.floor).name + "层)"
    html += `
        <div class="list_main_item" onclick="clickItem(${item.id})">
          ${item.name}
          <span class="floorname">${floorname}</span>
          <span class="floorname">${index == 0 ? '(最近)' : ''}</span>
        </div>
      `
  });
  $("#list_main").html(html);

};

// 选择推荐车位
function chooseReservePlaceData(data) {
  console.log('选择推荐车位', data);
  shownext();
  chooseData = placeData.find((item) => item.fid == data.fid);
  console.log('点击可约车位', chooseData);

  setTimeout(() => {
    pointAndNaviFid(chooseData.fid)
  }, 100);
};

// 打开与收起floor
function openBox(e) {
  e.stopPropagation();
  e.preventDefault();
  e.stopImmediatePropagation();
  let h = canOpenBox ? '100vw' : '56.5333vw';
  if (canOpenBox) {
    $(".footerBox .icon").removeClass('upClass')
  } else {
    $(".footerBox .icon").addClass('upClass')
  };
  $(".footerBox").css('height', h);
  canOpenBox = !canOpenBox
};