// 定义请求地址
// const BASE_URL = "http://192.168.1.124:8081/";
// const BASE_URL = "http://192.168.8.144:8081/";
// const BASE_URL = "http://192.168.1.131:8083/UWB/";
const BASE_URL = "http://112.94.22.123:10088/UWB/";
// const BASE_URL = "https://tuguiyao-gd.com/UWB/";
const formal = 'https://tuguiyao-gd.com/UWB/';
const imgurl = 'http://192.168.1.95:7003/';
var env = 'develop'; // develop: 开发版；trial：体验版；release：正式版


var fMap = null; // 地图对象
var compass;
var scrollFloorControl;
var initMapData = {}; // 初始化地图的数据
var all_places = []; // 全部车位数据
var loadData = {};
var allmaplist = [];
var selectLoadDataType = false; // 选择寻车机位置
var carLocaImgMarger = null;//寻车机位置marker
var clickMapImgMarker = null; // 点击marker
var webSelectLocaData = {};
var levelPlaceNumData = {};
var destinationMarker = null;
var openTime = null;
var fmapSuccessType = false;

const privince = [
  ["京", "津", "冀", "晋", "蒙", "辽", "吉", "黑", "沪", "苏"],
  ["浙", "皖", "闽", "赣", "鲁", "豫", "鄂", "湘", "粤"],
  ["桂", "琼", "川", "贵", "云", "渝", "藏", "陕"],
  ["甘", "青", "宁", "新", "港", "澳", "台"]
];
const word = [
  ['1', '2', '3', '4', '5', '6', '7', '8', '9', '0'],
  ['Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'],
  ['A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'],
  ['Z', 'X', 'C', 'V', 'B', 'N', 'M'],
];

$(function () {
  getTime();

  let u = getUrlStr('u');
  let p = getUrlStr('p');


  let sm = getUrlStr('sm');
  sm = decodeURIComponent(sm);
  initMapData = JSON.parse(sm);

  let l = getUrlStr('l');
  l = decodeURIComponent(l);
  loadData = JSON.parse(l);


  if (initMapData && initMapData.id) {
    initMap(initMapData);
    $('title').html((initMapData.name));
  } else {
    loginGetMapList(u, p)
  }
});

function getTime() {
  let curTime = new Date();
  $(".headerTimeD")[0].innerText = curTime.getFullYear() + '-' + zero(curTime.getMonth() + 1) + '-' + zero(curTime.getDate());
  $(".headerTimeT")[0].innerText = zero(curTime.getHours()) + ':' + zero(curTime.getMinutes());

  let nowTime = curTime.getTime();

  if ((nowTime - openTime > 24 * 60 * 60 * 1000) && fmapSuccessType) {
    // 小时刷新一次
    openTime = nowTime;
    updateApiGetData()
  };

  setTimeout(() => {
    getTime();
  }, 1000);
}
function zero(str) {
  return str < 10 ? '0' + str : str;
}

function api(url, data, type = "GET", isjson = false, load = true) {
  return new Promise((resolve, reject) => {
    $.ajax({
      url: BASE_URL + url,
      data,
      type,
      headers: {
        "Content-Type": isjson ? "application/json" : 'application/x-www-form-urlencoded'
      },
      beforeSend: function () {
        if (load) {
          loading();
        }
      },
      complete: function () {
        removeLoad();
      },
      success: function (res) {
        removeLoad();
        resolve(res)
      },
      error: function (err) {
        removeLoad();
        reject(err)
      }
    })
  })
};

function showToast(txt) {
  $.toast(txt, 'text');
};

var isloading = false;
function loading() {
  if (isloading) return;
  isloading = true;
  $.showLoading();
};

function removeLoad() {
  setTimeout(() => {
    isloading = false;
    $.hideLoading();
  }, 500);
};

function getUrlStr(name) {
  var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
  var r = window.location.search.slice(1).match(reg);
  if (r != null) {
    return r[2];
  }
  return null;
};

/* 地图搜索model */
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

/* 发送数据 */
function sendMessage(msg) {
  console.log(msg);
  uni.postMessage({
    data: msg
  });
};

/* 初始化地图 */
function initMap(data) {
  var options = {
    container: document.getElementById('fengmapbox'),
    appName: data.appName,
    key: data.key,
    mapID: data.mapID,
    themeID: data.themeID || 'tgymap',
    level: 1,
    mapZoom: 18,
    zoomRange: [16, 23],
    floorSpace: 80,
    tiltAngle: 70,
    viewMode: fengmap.FMViewMode.MODE_2D
  };
  fMap = new fengmap.FMMap(options);
  fMap.on('loaded', function () {
    /* 全览模式 */
    let level = fMap.getLevel();
    let bound = fMap.getFloor(level).bound;
    fMap.setFitView(bound, false, () => { });
    /* 设置楼层控件 */
    setFloorControls();
    /* 图层控制 */
    layerControl();
    // 地图加载完后-页面处理
    fmapSuccess();
  });

  fMap.on('click', function (event) {
    // 调用 service 层的方法
    let target = event.targets[0];
    let coords = event.coords;

    if (!coords.x || !coords.y) {
      return false
    };

    if (!target.level) {
      return false
    };
    // 点击图标
    if (clickMapImgMarker) {
      clickMapImgMarker.remove();
    };
    if (selectLoadDataType) {
      // 选择寻车机位置
      clickMapImgMarker = new fengmap.FMImageMarker({
        x: +coords.x,
        y: +coords.y,
        url: 'static/images/location-marker.png',
        size: 30,
        height: 2,
        collision: false
      });
      const floor = fMap.getFloor(+target.level);
      clickMapImgMarker.addTo(floor);

      let data = {
        x: coords.x,
        y: coords.y,
        level: target.level,
      };
      webSelectLocaData = data;

      let html = '<div class="selectloadText2">已选择位置</div>'
      $("#selectloadTextId").html(html);
    }
  });

  fMap.on('levelChanged', function (event) {
    showEmptyPlaceHtml();

    let level = event.level;
    let bound = fMap.getFloor(level).bound;
    fMap.setFitView(bound, {
      animate: false,
      finish: function () { }
    });
  });
};

function setFloorControls() {
  if (compass) {
    compass.remove();
    compass = null;
  };
  if (scrollFloorControl) {
    scrollFloorControl.remove();
    scrollFloorControl = null;
  };
  // 指南针
  let dom = document.getElementsByClassName('fm-compass-container')
  if (dom.length < 1) {
    var scrollCompassCtlOpt = {
      position: fengmap.FMControlPosition.LEFT_TOP,
      offset: {
        x: 10,
        y: 10
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
  let dom2 = document.getElementsByClassName('fm-control-groups')
  if (dom2.length < 1) {
    var scrollFloorCtlOpt = {
      position: fengmap.FMControlPosition.RIGHT_BOTTOM,
      floorButtonCount: 3,
      offset: {
        x: -10,
        y: 0
      },
      viewModeControl: false,
      floorModeControl: true,
      showMap: true,
      entranceIcon: false
    };
    scrollFloorControl = new fengmap.FMToolbar(scrollFloorCtlOpt);
    scrollFloorControl.addTo(fMap);
  };
};

/* 图层控制 */
function layerControl() {
  fMap.getLevels().forEach(level => {
    let floor = fMap.getFloor(level);
    /* 去除原地图上的文字 */
    let layer = getLayerByType(floor, fengmap.FMType.LABEL_LAYER);

    /* 去除地图上的车位图标 */
    getLayerByType2(floor, fengmap.FMType.FACILITY_LAYER);

    layer.visible = false;
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
};

function getLayerByType2(floor, layerType) {
  var layers = floor.getLayers();
  for (var index = 0; index < layers.length; index++) {
    const layer = layers[index];
    if (layer.type === layerType) {
      if (layer.children.length) {
        layer.children.forEach((item) => {
          if (item.typeID == '800004' || item.typeID == '800005' || item.typeID ==
            '800006' || item.typeID == '800013' || item.typeID == '110001') {
            item.visible = false
          }
        })
      }
    }
  }
};

var crossLayerData = [];
var elevatorData = [];
var serviceFacilitiesData = [];
var showIconExitData = [];
var showBusinessList1 = [];
var showBusinessList2 = [];
function fmapSuccess() {
  fmapSuccessType = true;
  openTime = new Date().getTime();

  removeLoad();
  if (loadData && loadData.x) {
    showLocaDataToMapCenter(loadData)
    $("#selectMapBox").css('display', 'none');
  } else {
    showSelectLoadData();
  };

  /* 跨层通道 */
  crossLayerChannel();
  /* 电梯数据 -3*/
  getApiElevatorData();
  /* 厕所-安全出口-建筑-2 */
  getApiServiceFacilitiesData();
  getPlaceExith();
  getCompanyBusiness1();
  getCompanyBusiness2();

  initMapPlace(true);
  palceTypeTime = setInterval(() => {
    initMapPlace(false);
  }, 30000);
};

function updateApiGetData() {
  /* 跨层通道 */
  crossLayerChannel(false);
  /* 电梯数据 -3*/
  getApiElevatorData(false);
  /* 厕所-安全出口-建筑-2 */
  getApiServiceFacilitiesData(false);
  getPlaceExith(false);
  getCompanyBusiness1(false);
  getCompanyBusiness2(false);
};

function initMapPlace(first) {
  api('wechat/getPlaceById', {
    map: initMapData.id
  }, 'GET', false, first).then((res) => {
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
      levelPlaceNumData = dataNumObj;
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
  })
};

function showEmptyPlaceHtml() {
  let data = levelPlaceNumData;
  let floor = fMap.getLevel();
  let name = fMap.getFloor(floor).name;
  let passF1 = getFloorNamePassF1(name);
  if (passF1) {
    $("#levelPlaceNumBox").html('')
    return
  }
  let html = `
      <div class="emptybox">
        ${name}层：空车位${data[floor] || 0}个
      </div>
    `;
  $("#levelPlaceNumBox").html(html)

};

// 空闲车位颜色
function addPlaceColor(data, init) {
  if (data.length) {

    data.forEach((dt) => {
      if (dt.fid) {
        const model = getMapModel(dt.fid);
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

            let nameInfo = {
              x: x,
              y: y,
              level: model.level || dt.floor,
              name: dt.name,
              target: dt
            };

            setPlaceNameAndImage(nameInfo);
          }
        }
      } else {
        if (init == 'first') {
          let nameInfo = {
            x: dt.x,
            y: dt.y,
            level: dt.floor,
            name: dt.name,
            target: dt
          };
          setPlaceNameAndImage(nameInfo);
        }
      }
    })

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
    url = 'static/images/placeType1.png'
  } else if (target.type == 2) {
    /* 专属车位 */
    url = 'static/images/placeType2.png'
  } else if (target.type == 3) {
    /* 无障碍车位 */
    url = 'static/images/placeType3.png'
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

/* 跨层通道 */
function crossLayerChannel(a = true) {
  api('crossLevelCorridor/getConditionalQuery2', {
    map: initMapData.id
  }, 'GET', false, a).then((res) => {
    const data = res.data;
    if (!data || data.length == 0) {
      return;
    }
    crossLayerData = data;
    setCrossLayerChannel(data);
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
        url: 'static/images/Exit3.png', // 图片url地址
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

/* 电梯数据 */
function getApiElevatorData(a = true) {
  api('peb/getParkingElevatorBinding', {
    map: initMapData.id,
    pageSize: -1
  }, 'GET', false, a).then((res) => {
    if (res.code != 200) {
      return;
    }
    const data = res.data;
    elevatorData = data;
    setElevatorDataToMap(elevatorData);
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
    url = 'static/images/Exit5.png'
  } else if (data.iconType == '170003') {
    /* 手扶电梯 */
    url = 'static/images/Exit7.png'
  } else if (data.iconType == '170006') {
    /* 电梯前室 */
    url = 'static/images/Exit6.png'
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
function getApiServiceFacilitiesData(a = true) {
  api('mapBuild/getMapBuild2', {
    map: initMapData.id,
    pageSize: -1
  }, 'GET', false, a).then((res) => {
    if (res.code != 200) {
      return;
    };
    const data = res.data;
    serviceFacilitiesData = data;
    setServiceFacilitiesData(serviceFacilitiesData);
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
    url = 'static/images/icon_wc_1.png'
  } else if (data.iconType == '100005') {
    /* 女洗手间 */
    url = 'static/images/icon_wc_2.png'
  } else if (data.iconType == '100003') {
    /* 母婴室 */
    url = 'static/images/icon_wc_4.png'
  } else if (data.iconType == '800008') {
    /* 护士站 */
    url = 'static/images/icon_wc_5.png'
  } else if (data.iconType == '110002') {
    /* 安全出口 */
    url = 'static/images/Exit4.png'
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

function getPlaceExith(a = true) {
  api('park/getPlaceExit2', {
    map: initMapData.id
  }, 'GET', false, a).then((res) => {
    if (res.code != 200) return;
    let resData = res.data.list;
    showIconExitData = resData;
    showExitIconToMap();
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
          url = 'static/images/Exit1.png'
        } else {
          url = 'static/images/Exit2.png'
        }
      } else {
        // 通行
        if (dt.type != 3) {
          url = 'static/images/Exit1.png'
        } else {
          url = 'static/images/Exit2.png'
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

        let textMarker = new fengmap.FMTextMarker(textData);
        let f = (model && model.level) ? model.level : dt.floor;
        let floor = fMap.getFloor(+f);
        textMarker.addTo(floor)
      }
    })
  }
};

function getCompanyBusiness1(a = true) {
  api('park/getCompany1', {
    map: initMapData.id
  }, 'GET', false, a).then((res) => {
    if (res.data && res.data.length) {
      let data = res.data.map((item) => {
        return {
          id: item.id,
          fid: item.fid,
          name: item.name,
          x: item.x,
          y: item.y,
          level: item.floor,
          photolocal: '',
          type_num: 8
        }
      });
      showBusinessList1 = data;
      addbusinesscompanytomap(data);
    }
  })
};

function getCompanyBusiness2(a = true) {
  api('park/getShangjia1', {
    map: initMapData.id
  }, 'GET', false, a).then((res) => {
    if (res.data && res.data.length) {
      let data = res.data.map((item) => {
        return {
          id: item.id,
          fid: item.fid,
          name: item.name,
          x: item.x,
          y: item.y,
          level: item.floor,
          photolocal: item.photolocal2,
          type_num: 3
        }
      });
      showBusinessList2 = data;
      addbusinesscompanytomap(data);
    }
  })
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

    var floor = fMap.getFloor(+item.level);
    if (item.photolocal) {
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
          url: formal + item.photolocal, // 图片url地址
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
      };

      let textMarker = new fengmap.FMTextMarker(textData);
      textMarker.addTo(floor);
    }
  });
};

function loginGetMapList(u, p) {
  api('wxSmallAPP/login', {
    userName: u,
    password: p
  }).then((res) => {
    if (res.code != 200) {
      uni.redirectTo({
        url: '/pages/login/login',
      });
      return
    };
    allmaplist = res.data.list;
    if (allmaplist.length === 1) {
      let target = allmaplist[0]
      setInitMapData(target)
    } else {
      showSelectMapListData();
    }
  })
};

function showSelectMapListData() {
  let html = `
    <div class="headerText">选择地图</div>
    <div class="selectMapListMain">
      <div class="selectMapListBox" onclick="openSelectList()">
        <div class="selectMapListText" id="selectMapListTextId">请选择</div>
        <div class="selectMapListIcon">
          <img class="selectMapListIconImg" src="static/images/down.png">
        </div>
      </div>
      <div class="selectMapListList" id="selectMapListListId">`
  let len = allmaplist.length
  for (let i = 0; i < len; i++) {
    let item = allmaplist[i]
    html += `<div class="selectMapListItem" onclick="selectTarget('${item.id}')">${item.name}</div>`
  }

  html += `</div>
    </div>
  `;
  $("#selectMapBox").html(html)
};

function openSelectList() {
  $("#selectMapListListId").css('display', 'block')
};

function selectTarget(id) {
  let target = allmaplist.find((item) => {
    return item.id == id
  });

  $("#selectMapListTextId").html(target.name);
  $("#selectMapListListId").css('display', 'none');

  loading();
  setInitMapData(target);
};

function setInitMapData(target) {
  let data = {
    id: target.id,
    appName: target.appName,
    key: target.mapKey,
    mapID: target.fmapID,
    themeID: target.themeName || 'tgymap',
    name: target.name
  };
  initMapData = data;
  initMap(initMapData);
  $('title').html((initMapData.name));

  sendMessage({
    todo: 'selectMap',
    data
  });
};

function showSelectLoadData() {
  let html = `
    <div class="headerText">选择寻车机位置</div>
    <div class="selectloadTextBox" id="selectloadTextId">
      <div class="selectloadText">请在地图上选择寻车机位置</div>
    </div>
    <div class="selectloadSuccess" onclick="successLocaData()">确定</div>
  `;

  if (allmaplist.length > 1) {
    html += `
    <div class="selectloadBack" onclick="backMapSelect()">
      <div class="arrow"></div>
      <div class="backText">返回</div>
    </div>
    `
  }

  selectLoadDataType = true;
  $("#selectMapBox").html(html);
};

function successLocaData() {
  let data = webSelectLocaData;

  if (JSON.stringify(data) == '{}') {
    showToast('请选择寻车机位置');
    return
  };
  selectLoadDataType = false;
  // 点击图标
  if (clickMapImgMarker) {
    clickMapImgMarker.remove();
  };
  showLocaDataToMapCenter(data);
  sendMessage({
    todo: 'loadData',
    data
  });
};

function backMapSelect() {
  showSelectMapListData();
};

function showLocaDataToMapCenter(data) {
  uploadCarLocaImgMargerData = data;
  showMainBox();


  fMap.setLevel({
    level: +data.level,
    finish: function () {
      setTimeout(() => {
        fMap.setCenter({
          x: +data.x,
          y: +data.y,
          animate: true,
          duration: 0.5,
          finish: function () { }
        });
      }, 600);
    }
  });

  uploadCarLocaImgMarger();
};

/* 显示寻车机主要页面-查询 */
function showMainBox() {
  $("#selectMapBox").html('')
  showLicensePlate();
  // showParkingNumber();
  // showFacilities();
};

var isNewLP = false;
var lpInputIndex = 1;
var licensePlateText = '';
function showLicensePlate() {
  aaclear();

  isNewLP = false;
  lpInputIndex = 1;
  licensePlateText = '';

  let html = `
    <div class="searchMainBox">
      <div class="searchTypeNav">
        <div class="navItem active">车牌号查询</div>
        <div class="navItem" onclick="showParkingNumber()">车位号查询</div>
        <div class="navItem" onclick="showFacilities()">设施查询</div>
      </div>
      <div class="searchCenterMain">
        <div class="lp_input_box">
          <div class="lp_input">
            <div id="lpIItemId1" class="lp_input_item lpActive" data-i="1" onclick="lpItemClick(this)"></div>
            <div id="lpIItemId2" class="lp_input_item" data-i="2" onclick="lpItemClick(this)"></div>
            <div id="lpIItemId3" class="lp_input_item" data-i="3" onclick="lpItemClick(this)"></div>
            <div id="lpIItemId4" class="lp_input_item" data-i="4" onclick="lpItemClick(this)"></div>
            <div id="lpIItemId5" class="lp_input_item" data-i="5" onclick="lpItemClick(this)"></div>
            <div id="lpIItemId6" class="lp_input_item" data-i="6" onclick="lpItemClick(this)"></div>
            <div id="lpIItemId7" class="lp_input_item" data-i="7" onclick="lpItemClick(this)"></div>
            <div id="lpIItemId8" class="lp_input_item noneIsNewLP" data-i="8" onclick="lpItemClick(this)"></div>
          </div>
          <div class="lp_input_new">
            <input type="checkbox" onclick="configCheckChange()" ${isNewLP ? 'checked' : ''}/>
            <div class="lp_input_new_text">新能源车牌</div>
          </div>
        </div>
        <div class="lp_input_search_list">
          <div class="lpInputSearchListBox" id="lpInputSearchList"></div>
        </div>
        <div class="lp_btn_box">
          <div class="lp_search" onclick="lpSearch()">查询</div>
          <div class="lp_clear" onclick="lpClearText()">
            <img class="lp_clear_icon" src="static/images/reactIcon.png" />
            <div>清空</div>
          </div>
        </div>
      </div>
      <div class="licenseInputBox" id="licenseInputBox"></div>
    </div>
  `;

  $("#searchListBox").html(html);
  showKeyboard();
};

function configCheckChange() {
  isNewLP = !isNewLP;

  if (isNewLP) {
    $("#lpIItemId8").removeClass("noneIsNewLP");

    let len = licensePlateText.length;
    if (len == 7) {
      lpInputIndex = 8;
      $(".lpActive").removeClass("lpActive");
      $("#lpIItemId8").addClass('lpActive');
    }
  } else {
    $("#lpIItemId8").addClass("noneIsNewLP");

    let len = licensePlateText.length;
    if (len == 8) {
      licensePlateText = licensePlateText.slice(0, -1);

      $("#lpIItemId8").html('');

      lpInputIndex = 7;
      $(".lpActive").removeClass("lpActive");
      $("#lpIItemId7").addClass('lpActive');
    };

    if (len == 7) {
      lpInputIndex = 7;
      $(".lpActive").removeClass("lpActive");
      $("#lpIItemId7").addClass('lpActive');
    }
  }
};

function showKeyboard() {
  let html = ''
  if (lpInputIndex == 1) {
    privince.forEach((item) => {
      html += '<div class="keyList">'
      item.forEach((target) => {
        html += `
          <div class="keyItem" onclick="keycode('${target}')">${target}</div>
        `;
      })
      html += '</div>'
    })
  } else {
    word.forEach((item) => {
      html += '<div class="keyList">'
      item.forEach((target) => {
        html += `
          <div class="keyItem" onclick="keycode('${target}')">${target}</div>
        `;
      })
      html += '</div>'
    })
  };

  $("#licenseInputBox").html(html)
};

var debounceTimes = null;
function keycode(text) {
  let arr = licensePlateText.split('');
  arr.splice(lpInputIndex - 1, 1, text);
  licensePlateText = arr.join('');

  arr.forEach((item, index) => {
    $("#lpIItemId" + (index + 1)).html(item);
  });

  lpInputIndex = lpInputIndex + 1;
  $(".lpActive").removeClass("lpActive");
  if (lpInputIndex == 8 && !isNewLP) {
    lpInputIndex = 7
  };

  if (lpInputIndex >= 8) {
    lpInputIndex = 8
  };

  $("#lpIItemId" + (lpInputIndex)).addClass('lpActive');
  showKeyboard();

  if (licensePlateText.length != 1) {
    if (debounceTimes) {
      clearTimeout(debounceTimes);
    }

    debounceTimes = setTimeout(() => {
      lpSearch(false);
    }, 500)
  }
};

function lpItemClick(e) {
  let i = e.getAttribute('data-i');
  let len = licensePlateText.length;
  if (i <= (len + 1)) {
    lpInputIndex = +i;
    $(".lpActive").removeClass("lpActive");
    $("#lpIItemId" + i).addClass('lpActive');
  };

  showKeyboard();
};

function lpClearText() {
  licensePlateText = '';
  lpInputIndex = 1;
  $(".lpActive").removeClass("lpActive");
  $("#lpIItemId1").addClass('lpActive');
  $(".lp_input_item").html('');

  showKeyboard();
};

function lpSearch(a = true) {
  if (!licensePlateText) {
    showToast('请输入车牌');
    return
  }
  api('placeVideoDetection/getPlaceByLicense', {
    license: licensePlateText,
    map: initMapData.id
  }, 'GET', false, a).then((res) => {
    if (res.code != 200) {
      showToast(res.message || '系统繁忙')
      return
    };
    let data = res.data;
    if (!(data && data.length)) {
      showToast(res.message)
      return
    };
    let resArr = data.map((item) => {
      return {
        ...item,
        newStr: resNameList(item.license, licensePlateText)
      }
    });
    showLPSearchData(resArr)
  })
};

function resNameList(str, keyword) {
  if (typeof str !== 'string') {
    str += '';
  }
  let keywordArr = keyword.split('');
  let strArr = str.split('');
  let result = [];
  let key = 1;
  for (var i = 0; i < strArr.length; i++) {
    let flag = false;
    if (keywordArr[i]) {
      let regStr = new RegExp(keywordArr[i], 'i');
      if (regStr.test(strArr[i])) {
        flag = true;
      }
    }
    result.push({
      key: key++,
      text: strArr[i],
      type: flag === true ? 'keyword' : 'text',
    })
  }
  return result;
};

function resNameList2(str, keyword) {
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
};

function showLPSearchData(data) {
  let html = '';
  data.forEach((item) => {
    html += `
      <div class="LPsearchItem" onclick="clickSearchLPItem(this)" data-item='${JSON.stringify(item)}'>
        <img class="LPsearchIcon" src="static/images/searchIcon.png" />
        <div class="LPsearchItemLPName">`

    item.newStr.forEach((target) => {
      html += `<span class="${target.type === 'keyword' ? 'LPstrong' : ''}">${target.text}</span>`
    })

    html += `</div>
      </div>
    `;
  });

  $("#lpInputSearchList").html(html)
};

var aabbType;
/* 1：车牌
 * 2：车位
 * 3：出入口
 * 4：电梯
 * 5：楼栋
 * 6：商家
 * 7：公司
 * 8：车牌-摄像头
 * 9：洗手间
*/
function clickSearchLPItem(e) {
  let data = e.getAttribute('data-item')
  var info = JSON.parse(data);
  console.log('点击的车牌', info);
  if (info.serialNumber) {
    aabbType = 8
  } else {
    aabbType = 1;
  };

  let sdata = {
    id: info.id,
    x: info.x,
    y: info.y,
    level: info.floor,
    name: info.license,
    ename: info.placeName
  };
  showDestination(sdata);

  showQRCode(sdata, 1);
};

function clickSheShiItem(e) {
  let data = e.getAttribute('data-item')
  var info = JSON.parse(data);
  console.log('点击的设施', info);
  if (FLTypeIndex == 1) {
    aabbType = 3;
  } else if (FLTypeIndex == 2) {
    aabbType = 4;
  } else if (FLTypeIndex == 3) {
    aabbType = 5;
  } else if (FLTypeIndex == 4) {
    aabbType = 9;
  } else if (FLTypeIndex == 5) {
    aabbType = 6;
  } else if (FLTypeIndex == 6) {
    aabbType = 7;
  };

  let sdata = {
    id: info.id,
    x: info.x,
    y: info.y,
    level: info.floor || info.level,
    name: info.name,
    ename: ''
  };
  showDestination(sdata);

  showQRCode(sdata, 3);
};

function clickSearchPNItem(e) {
  let data = e.getAttribute('data-item')
  var info = JSON.parse(data);
  console.log('点击的车位', info);
  aabbType = 2;

  let sdata = {
    id: info.id,
    x: info.x,
    y: info.y,
    level: info.floor,
    name: info.name,
    ename: ''
  };
  showDestination(sdata);

  showQRCode(sdata, 2);
};


function showFacilities() {
  aaclear();

  let html = `
    <div class="searchMainBox">
      <div class="searchTypeNav">
        <div class="navItem" onclick="showLicensePlate()">车牌号查询</div>
        <div class="navItem" onclick="showParkingNumber()">车位号查询</div>
        <div class="navItem active" >设施查询</div>
      </div>
      <div class="searchCenterMain">
        <div class="FLTargetBox">
          <div class="FLTarget FLTarget1" onclick="FLClickTarget(1)">车场出入口</div>
          <div class="FLTarget FLTarget2" onclick="FLClickTarget(2)">电梯</div>
          <div class="FLTarget FLTarget3" onclick="FLClickTarget(3)">楼栋</div>
          <div class="FLTarget FLTarget4" onclick="FLClickTarget(4)">洗手间</div>
          <div class="FLTarget FLTarget5" onclick="FLClickTarget(5)">商家</div>
          <div class="FLTarget FLTarget6" onclick="FLClickTarget(6)">公司</div>
        </div>
      </div>
    </div>
  `;

  $("#searchListBox").html(html);
};

var FLTypeIndex;
function FLClickTarget(type) {
  FLTypeIndex = type;
  let data = [];
  let title = '';
  if (type == 1) {
    /* 车场出入口 */
    data = showIconExitData;
    title = '车场出入口';
  } else if (type == 2) {
    /* 电梯 */
    data = elevatorData;
    title = '电梯';
  } else if (type == 3) {
    /* 楼栋 */
    data = serviceFacilitiesData.filter((item) => {
      return (item.objectType == '340873' || item.objectType == '340874' || item.objectType == '340875')
    });
    title = '楼栋';
  } else if (type == 4) {
    /* 洗手间 */
    data = serviceFacilitiesData.filter((item) => {
      return (item.objectType == '100004' || item.objectType == '100005')
    });
    title = '洗手间';
  } else if (type == 5) {
    /* 商家 */
    data = showBusinessList2;
    title = '商家';
  } else if (type == 6) {
    /* 公司 */
    data = showBusinessList1;
    title = '公司';
  };

  showFLClickTargetList(data, type, title);
};

function showFLClickTargetList(data, type, title) {
  data.forEach((item) => {
    if (!item.floorName) {
      item.floorName = fMap.getFloor(+item.level || +item.floor).name
    }
  })

  let html = `
    <div class="searchMainBox">
      <div class="searchTypeNav">
        <div class="navItem" onclick="showLicensePlate()">车牌号查询</div>
        <div class="navItem" onclick="showParkingNumber()">车位号查询</div>
        <div class="navItem active">设施查询</div>
      </div>
      <div class="searchCenterMain">
        <div class="FLTargetListHeader">
          <img class="FLTHicon" src="static/images/sheshitype${type}.png" />
          <div class="FLTargetListHeaderText">${title}</div>
        </div>
        <div class="FLTargetListBox">`
  data.forEach((item, index) => {
    html += `
      <div class="mapSheShiItem" onclick="clickSheShiItem(this)" data-item='${JSON.stringify(item)}'>
        <div class="mapSheShiItemIndex">${index}.</div>
        <div class="mapSheShiItemName">${item.name}</div>
        <div class="mapSheShiItemLevel ${item.floorName ? '' : 'noshow'}">(${item.floorName})</div>
      </div>
    `;
  })
  html += `</div>
        <div class="FLTargetListBockBox" onclick="showFacilities()">
          <div class="arrow"></div>
          <div class="backText">返回</div>
        </div>
      </div>
    </div>
  `;

  $("#searchListBox").html(html);
};

function showParkingNumber() {
  aaclear();

  let html = `
    <div class="searchMainBox">
      <div class="searchTypeNav">
        <div class="navItem" onclick="showLicensePlate()">车牌号查询</div>
        <div class="navItem active">车位号查询</div>
        <div class="navItem" onclick="showFacilities()">设施查询</div>
      </div>
      <div class="searchCenterMain">
        <div class="PN_input_box">
          <input class="PN_input" id="PNInputId" placeholder="请输入车位号" oninput="inputText()" />
        </div>
        <div class="PN_input_search_list">
          <div class="lpInputSearchListBox" id="PNInputSearchList"></div>
        </div>
        <div class="lp_btn_box">
          <div class="lp_search" onclick="PNSearch()">查询</div>
          <div class="lp_clear" onclick="PNClearText()">
            <img class="lp_clear_icon" src="static/images/reactIcon.png" />
            <div>清空</div>
          </div>
        </div>
      </div>
    </div>
  `;

  $("#searchListBox").html(html);
};

function PNClearText() {
  document.getElementById('PNInputId').value = '';
  $("#PNInputSearchList").html('');
};

function inputText() {
  if (debounceTimes) {
    clearTimeout(debounceTimes);
  }

  debounceTimes = setTimeout(() => {
    PNSearch(false);
  }, 500)
};

function PNSearch(a = true) {
  let value = $("#PNInputId")[0].value;
  if (value) {
    aaasearch(value, a);
  } else {
    showToast('请输入车位号')
  }
};

function aaasearch(val, a) {
  let keyword = val.trim().toUpperCase();
  api('wechat/getPlaceById', {
    name: keyword,
    map: initMapData.id,
  }, "GET", false, a).then((res) => {
    if (res.code != 200) {
      showToast('系统繁忙');
      return
    };
    var data = [];
    if (res && res.data && res.data.list) {
      data = res.data.list
    } else if (res && res.data) {
      data = res.data
    } else {
      data = res;
    };
    if (!(data && data.length)) {
      showToast('查询不到该车位数据');
      return
    };

    var data2 = [];
    data.forEach((item) => {
      data2.push(...(item.places))
    });

    let resArr = data2.map((item) => {
      return {
        ...item,
        newStr: resNameList2(item.name, keyword)
      }
    });

    showPNSearchData(resArr)
  })
};

function showPNSearchData(data) {
  let html = '';
  data.forEach((item) => {
    html += `
      <div class="LPsearchItem" onclick="clickSearchPNItem(this)" data-item='${JSON.stringify(item)}'>
        <img class="LPsearchIcon" src="static/images/searchIcon.png" />
        <div class="LPsearchItemLPName">`

    item.newStr.forEach((target) => {
      html += `<span class="${target.type === 'keyword' ? 'LPstrong' : ''}">${target.text}</span>`
    })

    html += `</div>
      </div>
    `;
  });

  $("#PNInputSearchList").html(html)
};

function showQRCode(data, type) {
  let scene = `mapId=${initMapData.id}&id=${data.id}&ftype=${aabbType}`
  api('WxMiniApp/generateQrCode', {
    scene,
    env
  }).then((res) => {
    if (res.code != 200) {
      showToast('系统繁忙，请重试')
      return
    };
    if (!res.data) {
      showToast('获取不到当前数据，请重试');
      return
    }
    data.image = imgurl + res.data;
    showQRCodeTrue(data, type)
  })
};

function showQRCodeTrue(data, type) {
  let html = `
  <div class="searchMainBox">
    <div class="searchTypeNav">
      <div class="navItem ${type == 1 ? 'active' : ''}" onclick="showLicensePlate()">车牌号查询</div>
      <div class="navItem ${type == 2 ? 'active' : ''}" onclick="showParkingNumber()">车位号查询</div>
      <div class="navItem ${type == 3 ? 'active' : ''}" onclick="showFacilities()">设施查询</div>
    </div>
    <div class="searchCenterMain">
      <div class="QRCodeMainBox">
        <img class="QRCodeLoad" src="static/images/FMImageMarker.png" />
        <div class="QRCodeLoadName">
          <div>${data.name}</div>
          <div>${data.ename}</div>
        </div>
        <div class="QRCodeImgBox">
          <img class="QRCodeImg" src="${data.image}" />
        </div>
        <div class="QRCodeBoxText">微信扫码，手机实时导航</div>
      </div>

      <div class="FLTargetListBockBox" onclick="QRCodeBack('${type}')">
          <div class="arrow"></div>
          <div class="backText">返回</div>
        </div>
    </div>
  </div>
`;

  $("#searchListBox").html(html);
};

function QRCodeBack(type) {
  aaclear();

  if (type == 1) {
    showLicensePlate();
  } else if (type == 2) {
    showParkingNumber();
  } else if (type == 3) {
    FLClickTarget(FLTypeIndex)
  }
};


function showDestination(data) {
  fMap.setLevel({
    level: +data.level,
    finish: function () {
      setTimeout(() => {
        fMap.setCenter({
          x: +data.x,
          y: +data.y,
          animate: true,
          duration: 0.5,
          finish: function () {
            showNavigationRoute(data);
          }
        });
      }, 0);
    }
  });

  if (destinationMarker) {
    destinationMarker.remove();
  };

  destinationMarker = new fengmap.FMImageMarker({
    x: +data.x,
    y: +data.y,
    url: 'static/images/location-marker.png',
    size: 30,
    height: 2,
    collision: false
  });
  const floor = fMap.getFloor(+data.level);
  destinationMarker.addTo(floor);

};

function showNavigationRoute(data) {
  if (naviAnalyser) {
    naviAnalyser.dispose();
    naviAnalyser = null;
  }
  naviAnalyser = new fengmap.FMNaviAnalyser({ map: fMap }, function () {
    createNavi(naviAnalyser, data);
  });

};

var navi = null;
var naviAnalyser = null;
function createNavi(naviAnalyser, data) {
  if (navi) {
    navi.clearAll();

    navi.dispose();
    navi = null;
  }
  navi = new fengmap.FMNavigation({
    map: fMap,
    analyser: naviAnalyser,
  });

  var naviPersonRequest = {
    start: {
      x: +loadData.x,
      y: +loadData.y,
      level: +loadData.level,
      height: 3,
      url: 'https://developer.fengmap.com/fmAPI/images/start.png',
      size: 32,
    },
    dest: {
      x: +data.x,
      y: +data.y,
      level: +data.level,
      height: 3,
      url: 'https://developer.fengmap.com/fmAPI/images/end.png',
      size: 32
    },
    mode: fengmap.FMNaviMode.MODULE_BEST,           // 导航中路径规划模式
    priority: fengmap.FMNaviPriority.PRIORITY_DEFAULT   // 导航中的路线规划梯类优先级
  };

  // 起点绑路
  var scons = naviAnalyser.pathConstraint({
    x: +loadData.x,
    y: +loadData.y,
    level: +loadData.level,
    buildingID: null
  });

  // 终点绑路
  var econs = naviAnalyser.pathConstraint({
    x: +data.x,
    y: +data.y,
    level: +data.level,
    buildingID: null
  });

  if (scons) {
    naviPersonRequest.start.x = +scons.coords.x;
    naviPersonRequest.start.y = +scons.coords.y;
  };

  if (econs) {
    naviPersonRequest.dest.x = +econs.coords.x;
    naviPersonRequest.dest.y = +econs.coords.y;
  };

  navi.setStartPoint(naviPersonRequest.start, true);
  navi.setDestPoint(naviPersonRequest.dest);
  naviPersonRequest.priority = fengmap.FMNaviPriority.PRIORITY_LIFTFIRST1;

  uploadCarLocaImgMarger(true);

  navi.route({
    mode: naviPersonRequest.mode,
    priority: naviPersonRequest.priority,
    toDoors: true,
  }, function (result) {
    navi.drawNaviLine();

    // 设置路径全览模式
    if (naviPersonRequest.start.level == naviPersonRequest.dest.level) {
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
      };
      fMap.setFitView(bound, {
        animate: false,
        finish: function () { }
      });
    }
  }, function (result) {
    let aaa = '失败' + result;
    console.log(aaa);
    console.log('失败', result);
  });
};

function aaclear() {
  if (destinationMarker) {
    destinationMarker.remove();
  };
  if (navi) {
    navi.clearAll();
  };

  uploadCarLocaImgMarger();
};


var uploadCarLocaImgMargerData;
function uploadCarLocaImgMarger(type) {
  let data = uploadCarLocaImgMargerData;
  let url = 'static/images/FMImageMarker.png';
  if (type) {
    url = 'https://developer.fengmap.com/fmAPI/images/start.png'
  };

  if (carLocaImgMarger) {
    carLocaImgMarger.remove()
  };

  var compositeOptions = {
    height: 0,      // 离地高度, 默认 1
    depth: false,   // 文本渲染深度 【true】：开启深度；【false】：关闭深度
    anchor: {
      baseon: 'image',    // 锚点基于图片还是文字还是全部（all：全部，image：图片，text：文字）
      anchor: 'CENTER',   // 锚点位置
    },
    collision: false,        // true:开启避让 false:关闭避让 默认 true，注：render为flat平铺时不支持避让
    render: 'billboard',    // 渲染方式，billboard始终面向屏幕，flat平铺
    layout: {
      style: 'timage-btext',
      align: 'center'
    },
    image: {
      url, // 图片url地址
      size: [98, 98],       // 图片大小，数组第一个元素是宽，第二个元素为高，默认为[32, 32]
    },
    text: {
      padding: [0, 0, 0, 0],      // 文字内边距默认值为 [0,0,0,0]
      plateStrokeWidth: 1,       // 文字背景边框宽度，注：只有描边颜色没有描边宽度时，默认描边宽度为 1
      content: {
        textAlign: 'Center',  // 文字对齐方式
        lineSpacing: 2,                        // 行间距默认为2，当小于2时也等于2
        fontSize: 14,                           // 文本字号, 默认20px
        fillColor: '#252525',           // 文本填充色，rgb类型字符串，默认 "255,0,0"
        strokeColor: '#fff',     // 文本边框填充色，rgb类型字符串。默认 '255,255,0'
        strokeWidth: 1,  // 描边线宽
        text: '当前位置',  // 文字内容，必填
      }
    },
    x: +data.x,
    y: +data.y
  };

  carLocaImgMarger = new fengmap.FMCompositeMarker(compositeOptions);
  let floor = fMap.getFloor(+data.level);
  carLocaImgMarger.addTo(floor);
};