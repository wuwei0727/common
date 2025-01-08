/* 巡检app */

// 定义请求地址
// const BASE_URL = "http://192.168.1.124:8081/";
// const BASE_URL = "http://192.168.1.131:8083/UWB/";
const BASE_URL = "http://112.94.22.123:10088/UWB/";
// const BASE_URL = "https://tuguiyao-gd.com/UWB/";

var userName = '';
var password = '';
var initMapData = {}; // 初始化地图的数据
var fMap = null; // 地图对象
var clickMapImgMarker = null; // 点击marker
var locationMarker = null;
var fmapClickData = null;
var showImg = false;
var needFitView = true;
var compass;
var scrollFloorControl;

var offlineList = [];
var replaceArr = [];
var offlineIndex = 0;
/* 信标 */
var sortBaseStationList = [];
var unBaseStationList;
var showBeaconMarker = [];
var baseStationList = [];
var beaconList;
var dupliXinBiaoList = [];
var showBeacon = [];
/* 检测器 */
var sortBeStallList = [];
var unBeStallList;
var showDetectorMarker = [];
var showDetector = [];
var dupliStallList = [];
var beStallList = [];
/* 网关 */
var sortBstationList = [];
var unBstationList = [];
var showWangGMarker = [];
var showWangG = [];
var dupliWanggList = [];
var bstationList = [];
/* 地锁 */
var sortLockList = [];
var unLockList = [];
var showLockMarker = [];
var showLock = [];
var configLockList = [];
var timesConfigItemData = {};
var nowConfigLock = {};

var mapImgMarkerArr = [];

var installType; // 安装设备类型

var installText = '';
var installMapVal = {};
var texObj = {};

/* 列表 */
var showListIndex = 0;
var netIndex = 0; // 网络状态
var sortIndex = 0; // 排序方式
var deviceArray = ['信标', '检测器', '网关', '地锁'];
var netArray = ['全部', '在线', '离线', '低电量'];
var netArray2 = ['全部', '在线', '离线'];
var sortArray = ['不排序', '升序', '降序'];
var checked = false;
var locationChecked = false;
var listsearchValue = '';
var setlocateindex = 0;

/* 巡检 */
var fillTxt = '目标功率';
var beaconNum = '';
var beaconDir = '';
var beaconVolt = '';
var inputValue = '6';
var currentFunction = false;
var collectBeaconList = [];
var threshold = 0;
var currentCoord = {};
var inspectKey = 'txPower';
var saveStrArr = ['certification'];
var bluetoothArr = [];

var installErrorType = 0;

var locationChecked1 = false;
var checked1 = false;

var ewType = 0;
var ewLevels = 0;
var ewState = 0;
var ewDevice = 0;
var showAlarmsMarker = [];
var oldMapList;
var getPermissionTimes = null;

$(function () {
  userName = getUrlStr('u');
  password = getUrlStr('p');

  initLogin(userName, password);
  getPermissionTimes = setInterval(() => {
    initLogin(userName, password, 'tiemout');
  }, 15 * 60 * 1000);

  const fengmap = document.getElementById('fengmapbox');
  fengmap.addEventListener('touchend', function (e) {
    showMenu();
  });
});

/* 登录 */
function initLogin(u, p, type) {
  installErrorType = 'login'
  api('wxSmallAPP/login', {
    userName: u || 'admin',
    password: p || '123456'
  }, (res) => {
    if (res.code != 200) {
      showToast(res.message, true, 2000);
      if (getPermissionTimes) {
        clearInterval(getPermissionTimes);
        getPermissionTimes = null;
      }
      uni.redirectTo({
        url: '/pages/login/login?type=unlogin',
      });
      return
    }
    let sessionId = res.data.sessionId;
    document.cookie = `JSESSIONID=${sessionId};path=/`;

    if (type == 'tiemout') {
      let nowMapList = res.data.list;
      if (initMapData && initMapData.id) {
        let had = nowMapList.find((item) => {
          return item.id == initMapData.id
        });
        if (!had) {
          noPBack();
        }
      } else {
        if (nowMapList.length == oldMapList.length) {
          let had = nowMapList.every((item) => {
            let had = oldMapList.find((item2) => {
              return item2.id == item.id
            });
            return had
          });
          if (!had) {
            noPBack();
          }
        } else {
          noPBack();
        }
      }
    } else {
      let maplist = res.data.list;
      oldMapList = maplist;
      initSelectMap(maplist);
    };
  }, 'GET', false, type == 'timeout')
};

function noPBack() {
  showToast('用户权限已变更，请重新登录', true, 2000);
  if (getPermissionTimes) {
    clearInterval(getPermissionTimes);
    getPermissionTimes = null;
  };
  setTimeout(() => {
    uni.redirectTo({
      url: '/pages/login/login?type=unlogin',
    });
  }, 1800);
};

function initSelectMap(data, show) {
  let changeV = '';
  let arr = data.map((item) => {
    return item.name
  })
  $("#selectMap").picker({
    title: "请选择地图",
    cols: [
      {
        textAlign: 'center',
        values: [...arr]
      }
    ],
    onChange: function (p, v, dv) {
      changeV = v[0];
    },
    onClose: function (p, v, d) {
      setMenuListItemImgSrc(0);

      if (changeV) {
        let confirmText = '进入' + changeV
        $.confirm({
          title: '',
          text: confirmText,
          onOK: function () {
            if (networkTypeText == '网络异常') {
              showToast(networkTypeText, true, 2000)
              return
            };

            if (fMap) {
              fMap.dispose();
            };

            $('title').html(changeV)

            //点击确认
            let target = data.find((item) => {
              return item.name == changeV
            });
            initMap(target)
          },
          onCancel: function () { }
        });
      }
    }
  });

  $("#selectMap").picker("open");
};

/* 初始化地图 */
function initMap(data) {
  initMapData = {
    id: data.id + '',
    fmapID: data.fmapID,
    mapName: data.name,
    appName: data.appName,
    mapKey: data.mapKey,
    themeName: data.themeName
  };

  var options = {
    container: document.getElementById('fengmapbox'),
    appName: data.appName,
    key: data.mapKey,
    mapID: data.fmapID,
    themeID: data.themeName || 'tgymap',
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

    if (showImg) {
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
    };


    let clickData = {
      fid: target.FID,
      level: target.level,
      name: target.name,
      type: target.type,
      typeID: target.typeID,
      targetX: target.x,
      targetY: target.y,
      x: coords.x,
      y: coords.y,
      mapId: data.mapId,
      fmapId: data.fmapId,
      now: new Date().getTime(),
    }
    fmapClickData = clickData;

    if (installType == 0 || installType == 5) {
      document.getElementById('installMapVal0').value = '已选择';
    } else if (installType == 2) {
      document.getElementById('installMapVal2').value = '已选择';
    }
  });

  fMap.on('levelChanged', function (event) {
    if (!needFitView) {
      needFitView = true;
      return false
    };
    let level = event.level;
    let bound = fMap.getFloor(level).bound;
    fMap.setFitView(bound, false, () => { });
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
      position: fengmap.FMControlPosition.Left_TOP,
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
      position: fengmap.FMControlPosition.LEFT_TOP,
      floorButtonCount: 2,
      offset: {
        x: 10,
        y: 78
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

function fmapSuccess() {
  initMapPlace();
};

function initMapPlace() {
  api('wechat/getPlaceById', {
    map: initMapData.id
  }, (res) => {
    let data = res.data;
    if (!data || data.length == 0) return;
    let list = data;
    let current = [];
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
        }
        current.push(currentPushData);
      }
    };
    handlePlaceData(current);
  })
};

function handlePlaceData(data) {
  data.forEach((item) => {
    if (item.fid) {
      const model = getMapModel(item.fid);
      if (model) {
        /* 设置模型名字 */
        let nameInfo = {
          x: model.x,
          y: model.y,
          floor: model.level
        }
        setPlaceModelName(nameInfo, item, model.bound);
      } else {
        setPlaceModelName(null, item, null);
      }
    } else {
      setPlaceModelName(null, item, null);
    }
  })
};

function setPlaceModelName(target, data, bound) {
  if (!data.floor && !target) {
    return false
  };

  /* 名称 */
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
    x: (target && target.x) ? +target.x : +data.x,
    y: (target && target.y) ? +target.y : +data.y,
  });

  let floor = fMap.getFloor(+data.floor || +target.floor);
  textMarker.addTo(floor);


  let imgMarker;
  /* 图标 */
  if (bound) {
    let x = +bound.center.x;
    let y = +bound.center.y;
    let bx = (bound.min.x + bound.center.x) / 2;
    let by = (bound.min.y + bound.center.y) / 2;
    let url = '';
    if (data.type == 1) {
      /* 充电车位 */
      url = 'static/images/new/placeType1.png'
    } else if (data.type == 2) {
      /* 专属车位 */
      url = 'static/images/new/placeType2.png'
    } else if (data.type == 3) {
      /* 无障碍车位 */
      url = 'static/images/new/placeType3.png'
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
        size: 20,
        height: .2,
        collision: true
      };
      if (bound.size.x > bound.size.y) {
        imgdata.x = bx
      } else {
        imgdata.y = by
      }
      imgMarker = new fengmap.FMImageMarker(imgdata);
      let floor = fMap.getFloor(+data.floor || +target.floor)
      imgMarker.addTo(floor);
    }
  }
};

function getBeaconList(type) {
  api('bsconfig/getBsConfigSel', {
    map: initMapData.id,
    pageSize: -1
  }, (res) => {
    if (!res.data) return;
    const list = res.data;
    sortBaseStationList = list;
    unBaseStationList = list;
    baseStationList = list;

    if (type == 'list') {
      cshowBeacon(sortBaseStationList, 1);
      showDeviceListBox();
    } else if (type == 'install') {
      let target = null,
        result = [];
      for (let i = 0; i < list.length; i++) {
        target = list[i];
        let {
          x,
          y,
          floor,
          num: name,
          networkName
        } = target;
        if (!target.x || !target.y || !target.floor) {
          continue;
        };
        result.push({
          x,
          y,
          floor,
          checking: false,
          name,
          networkName,
          checked: false
        });
      }
      beaconList = result;
    } else if (type == 'insp') {
      cshowBeacon(sortBaseStationList, 1);
    }

  })
};

function getDetector(type) {
  api('wxInfrared/getInfraredSel', {
    map: initMapData.id,
    pageSize: -1
  }, (res) => {
    if (!res.data) return;
    const list = res.data;
    var filterItem = list;
    filterItem.forEach(item => {
      if (item.rawProductId) {
        item.rawProductId = item.rawProductId.slice(item.rawProductId.length - 6);
      }
    });
    sortBeStallList = filterItem;
    unBeStallList = filterItem;
    if (type === 'list') {
      sortBeStallList.forEach((item) => {
        item.y -= 0.3
        let place = item.place;
        let arr = sortBeStallList.filter((fitem) => {
          return fitem.place == place
        });
        if (arr.length >= 2) {
          arr.forEach((aitem, aindex) => {
            if (aitem.id == item.id) {
              if (aindex % 2 == 0) {
                item.y += 0.6 + (aindex * 0.2)
              }
            }
          })
        }
      });
      cshowBeacon(sortBeStallList, 3);
      showDeviceListBox();
    } else if (type === 'install') {
      let result = [];
      for (let i = 0; i < filterItem.length; i++) {
        target = filterItem[i];
        let {
          x,
          y,
          floor,
          placeName: placeName,
          networkName,
          num,
          rawProductId,
          status
        } = target;
        if (!target.x || !target.y || !target.floor) {
          continue;
        }
        result.push({
          x,
          y,
          floor,
          placeName,
          networkName,
          num,
          rawProductId,
          status,
          checked: false,
        });
      }
      beStallList = result;
    }
  })
};

function getGatewaySel(type) {
  api('wxGateway/getGatewaySel', {
    map: initMapData.id,
    pageSize: -1
  }, (res) => {
    const list = res.data;
    sortBstationList = list;
    unBstationList = list;

    if (type === 'list') {
      cshowBeacon(sortBstationList, 2);
      showDeviceListBox();
    } else if (type === 'install') {
      let result = [];
      for (let i = 0; i < list.length; i++) {
        target = list[i];
        let {
          x,
          y,
          floor,
          num: name,
          networkName,
          mapName
        } = target;
        if (!target.x || !target.y || !target.floor) {
          continue;
        }
        result.push({
          x,
          y,
          floor,
          name,
          networkName,
          mapName,
          checked: false
        });
      }
      bstationList = result;
    }
  })
};

function getLockDataList() {
  let url = 'wxSmallAPP/getFloorLockById/' + initMapData.id;
  api(url, {}, (res) => {
    const list = res.data;
    sortLockList = list;
    unLockList = list;
    cshowBeacon(sortLockList, 4);
    showDeviceListBox();

  })
};

function getLockTimesConfig() {
  let url = 'wxSmallAPP/getTimePeriodAdminInfoById/' + initMapData.id;
  api(url, {}, (res) => {
    const list = res.data;
    if (!(list && list.length)) return;

    let arr = [];
    list.forEach((item) => {
      if (arr.length) {
        let had = arr.some((aItem) => {
          return aItem.cId == item.companyId
        });
        if (had) {
          let index = arr.findIndex((aItem) => {
            return aItem.cId == item.companyId
          });
          if (index != -1) {
            let arrItem = arr[index];
            let tList = arrItem.tList
            let sT0 = item.startTime.split(":")[0];
            let sT1 = item.startTime.split(":")[1];
            let eT0 = item.endTime.split(":")[0];
            let eT1 = item.endTime.split(":")[1];
            if (item.dayOfWeek == 1) {
              tList[0] = sT0;
              tList[1] = sT1;
              tList[2] = eT0;
              tList[3] = eT1;
            } else if (item.dayOfWeek == 2) {
              tList[4] = sT0;
              tList[5] = sT1;
              tList[6] = eT0;
              tList[7] = eT1;
            } else if (item.dayOfWeek == 3) {
              tList[8] = sT0;
              tList[9] = sT1;
              tList[10] = eT0;
              tList[11] = eT1;
            } else if (item.dayOfWeek == 4) {
              tList[12] = sT0;
              tList[13] = sT1;
              tList[14] = eT0;
              tList[15] = eT1;
            } else if (item.dayOfWeek == 5) {
              tList[16] = sT0;
              tList[17] = sT1;
              tList[18] = eT0;
              tList[19] = eT1;
            } else if (item.dayOfWeek == 6) {
              tList[20] = sT0;
              tList[21] = sT1;
              tList[22] = eT0;
              tList[23] = eT1;
            } else if (item.dayOfWeek == 7) {
              tList[24] = sT0;
              tList[25] = sT1;
              tList[26] = eT0;
              tList[27] = eT1;
            }
            let newArrItem = {
              cId: arrItem.cId,
              cName: arrItem.cName,
              tList
            }
            arr.splice(index, 1, newArrItem)
          }
        } else {
          let tList = ['00', '00', '00', '00', '00', '00', '00', '00', '00', '00',
            '00', '00', '00', '00', '00', '00', '00', '00', '00', '00', '00', '00', '00', '00',
            '00', '00', '00', '00', '01', '02'
          ];
          let sT0 = item.startTime.split(":")[0];
          let sT1 = item.startTime.split(":")[1];
          let eT0 = item.endTime.split(":")[0];
          let eT1 = item.endTime.split(":")[1];
          if (item.dayOfWeek == 1) {
            tList[0] = sT0;
            tList[1] = sT1;
            tList[2] = eT0;
            tList[3] = eT1;
          } else if (item.dayOfWeek == 2) {
            tList[4] = sT0;
            tList[5] = sT1;
            tList[6] = eT0;
            tList[7] = eT1;
          } else if (item.dayOfWeek == 3) {
            tList[8] = sT0;
            tList[9] = sT1;
            tList[10] = eT0;
            tList[11] = eT1;
          } else if (item.dayOfWeek == 4) {
            tList[12] = sT0;
            tList[13] = sT1;
            tList[14] = eT0;
            tList[15] = eT1;
          } else if (item.dayOfWeek == 5) {
            tList[16] = sT0;
            tList[17] = sT1;
            tList[18] = eT0;
            tList[19] = eT1;
          } else if (item.dayOfWeek == 6) {
            tList[20] = sT0;
            tList[21] = sT1;
            tList[22] = eT0;
            tList[23] = eT1;
          } else if (item.dayOfWeek == 7) {
            tList[24] = sT0;
            tList[25] = sT1;
            tList[26] = eT0;
            tList[27] = eT1;
          }
          arr.push({
            cId: item.companyId,
            cName: item.companyName,
            tList
          });
        }
      } else {
        let tList = ['00', '00', '00', '00', '00', '00', '00', '00', '00', '00',
          '00', '00', '00', '00', '00', '00', '00', '00', '00', '00', '00', '00', '00', '00',
          '00', '00', '00', '00', '01', '02'
        ];
        let sT0 = item.startTime.split(":")[0];
        let sT1 = item.startTime.split(":")[1];
        let eT0 = item.endTime.split(":")[0];
        let eT1 = item.endTime.split(":")[1];
        if (item.dayOfWeek == 1) {
          tList[0] = sT0;
          tList[1] = sT1;
          tList[2] = eT0;
          tList[3] = eT1;
        } else if (item.dayOfWeek == 2) {
          tList[4] = sT0;
          tList[5] = sT1;
          tList[6] = eT0;
          tList[7] = eT1;
        } else if (item.dayOfWeek == 3) {
          tList[8] = sT0;
          tList[9] = sT1;
          tList[10] = eT0;
          tList[11] = eT1;
        } else if (item.dayOfWeek == 4) {
          tList[12] = sT0;
          tList[13] = sT1;
          tList[14] = eT0;
          tList[15] = eT1;
        } else if (item.dayOfWeek == 5) {
          tList[16] = sT0;
          tList[17] = sT1;
          tList[18] = eT0;
          tList[19] = eT1;
        } else if (item.dayOfWeek == 6) {
          tList[20] = sT0;
          tList[21] = sT1;
          tList[22] = eT0;
          tList[23] = eT1;
        } else if (item.dayOfWeek == 7) {
          tList[24] = sT0;
          tList[25] = sT1;
          tList[26] = eT0;
          tList[27] = eT1;
        }
        arr.push({
          cId: item.companyId,
          cName: item.companyName,
          tList
        });
      }
    });

    configLockList = arr;

  })
};

function scanCodeVal(data) {
  if (data.index == 0) {
    if (installType == 0 || installType == 5) {
      document.getElementById('installVal0').value = data.newResult;
    } else if (installType == 3) {
      document.getElementById('installMapVal3').value = data.newResult;
    }
  } else if (data.index == 1) {
    document.getElementById('installMapVal1').value = data.newResult;
  } else if (data.index == 2) {
    document.getElementById('installVal2').value = data.newResult;
  }
};

function scanCodeVal2(data) {
  document.getElementById('listsearchValue').value = data.newResult;
};

/* 发送数据 */
function sendMessage(msg) {
  uni.postMessage({
    data: msg
  });
};

/* 显示marker到地图上 */
function cshowBeacon(data, type) {
  showMarkerInMap(false, false, false, false, false);
  cshowLocationArr();

  if (data && data.length) {
    if (type === 1) {
      // 信标显示
      /* 只显示一种marker到地图上 */
      /* 信标-检测器-网关 */
      showMarkerInMap(true, false, false, false, false);
      data.forEach((item) => {
        if (!item.x || !item.floor) return;
        var color;
        // if (item.networkName === '在线') {
        //   color = '74, 96, 207';
        // } else if (item.networkName === '低电量') {
        //   color = '139, 69, 19';
        // } else {
        //   color = '160, 102, 211';
        // };

        if (item.networkName === '在线') {
          color = '74, 96, 207';
        };
        if (item.power <= 30) {
          color = '139, 69, 19';
        };
        if (item.networkName === '离线') {
          color = '160, 102, 211';
        }

        let Mindex = showBeaconMarker.findIndex((sitem) => {
          return sitem.num == item.num
        });
        if (Mindex != -1) {
          /* 已在地图上-判断是否需要更新 */
          let hasData = showBeaconMarker[Mindex];
          if (hasData.x == item.x && hasData.y == item.y && hasData.floor == item
            .floor) {
            // 位置一样--不需要更新
          } else {
            hasData.beaconT.remove();
            showBeaconMarker.splice(Mindex, 1);

            let beaconT = new fengmap.FMTextMarker({
              x: +item.x,
              y: +item.y,
              text: item.name,
              fillColor: color,
              strokeColor: '255,255,255',
              collision: false
            });
            var floor = fMap.getFloor(+item.floor);
            beaconT.addTo(floor);
            showBeaconMarker.push({
              ...item,
              beaconT
            });
          }
        } else {
          // 不在地图上
          let beaconT = new fengmap.FMTextMarker({
            x: +item.x,
            y: +item.y,
            text: item.name,
            fillColor: color,
            strokeColor: '255,255,255',
            collision: false
          });
          var floor = fMap.getFloor(+item.floor);
          beaconT.addTo(floor);
          showBeaconMarker.push({
            ...item,
            beaconT
          });
        }
      })
    } else if (type === 2) {
      // 网关显示
      showMarkerInMap(false, false, true, false, false);
      data.forEach((item) => {
        if (!item.x || !item.floor) return;
        var color = '74,96,207';

        let Mindex = showWangGMarker.findIndex((sitem) => {
          return sitem.num == item.num
        });
        if (Mindex != -1) {
          /* 已在地图上-判断是否需要更新 */
          let hasData = showWangGMarker[Mindex];
          if (hasData.x == item.x && hasData.y == item.y && hasData.floor == item
            .floor) {
            /* 位置一样--不需要更新 */
          } else {
            hasData.wangGT.remove();
            hasData.wangGI.remove();
            showWangGMarker.splice(Mindex, 1);

            let wangGT = new fengmap.FMTextMarker({
              x: +item.x,
              y: +item.y + 1.5,
              text: item.name || item.num,
              fillColor: color,
              strokeColor: '255,255,255',
              collision: false
            });
            let wangGI = new fengmap.FMImageMarker({
              x: +item.x,
              y: +item.y,
              url: item.networkName == '在线' ?
                'static/images/wang.png' :
                'static/images/wangli.png',
              size: 20,
              height: 2,
              collision: false
            });
            var floor = fMap.getFloor(+item.floor);
            wangGT.addTo(floor);
            wangGI.addTo(floor);
            showWangGMarker.push({
              ...item,
              wangGT,
              wangGI
            })
          }
        } else {
          // 不在地图上
          let wangGT = new fengmap.FMTextMarker({
            x: +item.x,
            y: +item.y + 1.5,
            text: item.name || item.num,
            fillColor: color,
            strokeColor: '255,255,255',
            collision: false
          });
          let wangGI = new fengmap.FMImageMarker({
            x: +item.x,
            y: +item.y,
            url: item.networkName == '在线' ? 'static/images/wang.png' :
              'static/images/wangli.png',
            size: 20,
            height: 2,
            collision: false
          });
          var floor = fMap.getFloor(+item.floor);
          wangGT.addTo(floor);
          wangGI.addTo(floor);
          showWangGMarker.push({
            ...item,
            wangGT,
            wangGI
          })
        }
      })
    } else if (type === 3) {
      // 检测器显示
      showMarkerInMap(false, true, false, false, false);
      data.forEach((item) => {
        if (!item.x || !item.floor) return;
        let networkName = item.networkName;
        var color;
        // if (networkName == '在线') {
        //   color = '46, 139, 87';
        // } else if (networkName == '离线') {
        //   color = '160, 102, 211';
        // } else if (networkName == '低电量') {
        //   color = '216, 110, 6';
        // };

        if (networkName == '在线') {
          color = '46, 139, 87';
        };
        if (item.power <= 30) {
          color = '216, 110, 6';
        };
        if (networkName == '离线') {
          color = '160, 102, 211';
        };

        let Mindex = showDetectorMarker.findIndex((sitem) => {
          return sitem.rawProductId == item.rawProductId || item
            .isreplace
        });
        if (Mindex != -1) {
          /* 已在地图上-判断是否需要更新 */
          let hasData = showDetectorMarker[Mindex];
          if (hasData.x == item.x && hasData.y == item.y && hasData.floor == item
            .floor && !
            item
              .isreplace) {
            // 位置一样且不是替换操作--不需要更新
          } else {
            hasData.detectorT.remove();
            showDetectorMarker.splice(Mindex, 1);

            let detectorT = new fengmap.FMTextMarker({
              x: +item.x,
              y: +item.y,
              text: item.rawProductId + '',
              fillColor: color,
              strokeColor: '255,255,255',
              collision: false
            });
            var floor = fMap.getFloor(+item.floor);
            detectorT.addTo(floor);
            showDetectorMarker.push({
              ...item,
              detectorT
            });
          }
        } else {
          // 不在地图上
          let detectorT = new fengmap.FMTextMarker({
            x: +item.x,
            y: +item.y,
            text: item.rawProductId + '',
            fillColor: color,
            strokeColor: '255,255,255',
            collision: false
          });
          var floor = fMap.getFloor(+item.floor);
          detectorT.addTo(floor);
          showDetectorMarker.push({
            ...item,
            detectorT
          });
        }
      })
    } else if (type == 4) {
      // 地锁显示
      showMarkerInMap(false, false, false, true, false);
      data.forEach((item) => {
        if (!item.x || !item.floor) return;
        let networkstate = item.networkstate;
        var color;
        if (networkstate == '1') {
          color = '46, 139, 87';
        } else if (networkstate == '0') {
          color = '160, 102, 211';
        };

        let Mindex = showLockMarker.findIndex((sitem) => {
          return sitem.deviceNum == item.deviceNum
        });
        if (Mindex != -1) {
          /* 已在地图上-判断是否需要更新 */
          let hasData = showLockMarker[Mindex];
          if (hasData.x == item.x && hasData.y == item.y && hasData.floor == item
            .floor) {
            // 位置一样且不是替换操作--不需要更新
          } else {
            hasData.lockT.remove();
            showLockMarker.splice(Mindex, 1);

            let lockT = new fengmap.FMTextMarker({
              x: +item.x,
              y: +item.y + 0.6,
              text: item.deviceNum + '',
              fillColor: color,
              strokeColor: '255,255,255',
              collision: false
            });
            var floor = fMap.getFloor(+item.floor);
            lockT.addTo(floor);
            showLockMarker.push({
              ...item,
              lockT
            });
          }
        } else {
          // 不在地图上
          let lockT = new fengmap.FMTextMarker({
            x: +item.x,
            y: +item.y + 0.6,
            text: item.deviceNum + '',
            fillColor: color,
            strokeColor: '255,255,255',
            collision: false
          });
          var floor = fMap.getFloor(+item.floor);
          lockT.addTo(floor);
          showLockMarker.push({
            ...item,
            lockT
          });
        }

      })
    } else if (type == 5) {
      // 报警显示
      showMarkerInMap(false, false, false, false, true);
      let levels = fMap.getLevels();
      data.forEach((item) => {
        if (!item.x || !item.floor) return;
        let had = levels.some((lItem) => {
          return lItem == item.floor
        });
        if (!had) {
          /* 处理数据中floor不是该地图中存在的楼层 */
          return
        };
        let priority = item.priority;
        var color;
        if (item.state == 1) {
          color = '103, 194, 58';
        } else {
          if (priority == '1') {
            color = '255, 0, 0';
          } else if (priority == '2') {
            color = '230, 162, 60';
          } else {
            color = '144, 147, 153';
          };
        }

        let Mindex = showAlarmsMarker.findIndex((sitem) => {
          return sitem.id == item.id
        });
        if (Mindex != -1) {
          /* 已在地图上-判断是否需要更新 */
          let hasData = showAlarmsMarker[Mindex];
          if (hasData.x == item.x && hasData.y == item.y && hasData.floor == item
            .floor) {
            // 位置一样且不是替换操作--不需要更新
          } else {
            hasData.alarmsT.remove();
            showAlarmsMarker.splice(Mindex, 1);

            let alarmsT = new fengmap.FMTextMarker({
              x: +item.x,
              y: +item.y + 0.6,
              text: item.num ? (item.num + '') : (item.placeName || '摄像头'),
              fillColor: color,
              strokeColor: '255,255,255',
              collision: false
            });
            var floor = fMap.getFloor(+item.floor);
            alarmsT.addTo(floor);
            showAlarmsMarker.push({
              ...item,
              alarmsT
            });
          }
        } else {
          // 不在地图上
          let alarmsT = new fengmap.FMTextMarker({
            x: +item.x,
            y: +item.y + 0.6,
            text: item.num ? (item.num + '') : (item.placeName || '摄像头'),
            fillColor: color,
            strokeColor: '255,255,255',
            collision: false
          });
          var floor = fMap.getFloor(+item.floor);
          alarmsT.addTo(floor);
          showAlarmsMarker.push({
            ...item,
            alarmsT
          });
        }

      })
    }
  } else {
    showMarkerInMap(false, false, false, false, false);
    return false;
  }
};

/* 只显示一种marker到地图上 */
/* 信标-检测器-网关-地锁 */
function showMarkerInMap(beacon, detector, wangg, lock, alarms) {
  if (!beacon) {
    /* 信标不显示 */
    if (showBeaconMarker.length) {
      showBeaconMarker.forEach((item) => {
        item.beaconT.remove();
      });
      showBeaconMarker = [];
    }
  };
  if (!detector) {
    /* 检测器 */
    if (showDetectorMarker.length) {
      showDetectorMarker.forEach((item) => {
        item.detectorT.remove();
      })
    };
    showDetectorMarker = [];
  };
  if (!wangg) {
    /* 网关 */
    if (showWangGMarker.length) {
      showWangGMarker.forEach((item) => {
        item.wangGT.remove();
        item.wangGI.remove();
      });
      showWangGMarker = [];
    }
  };
  if (!lock) {
    /* 地锁 */
    if (showLockMarker.length) {
      showLockMarker.forEach((item) => {
        item.lockT.remove()
      });
      showLockMarker = [];
    }
  };
  if (!alarms) {
    /* 报警 */
    if (showAlarmsMarker.length) {
      showAlarmsMarker.forEach((item) => {
        item.alarmsT.remove()
      });
      showAlarmsMarker = [];
    }
  }
};

function cshowLocationArr(data) {
  if (data && data.length) {
    needFitView = false; // 不需要全览

    data.forEach((item) => {
      let mapImgMarker = new fengmap.FMImageMarker({
        x: +item.x,
        y: +item.y + 0.5,
        url: 'static/images/circle.png',
        size: 30,
        height: 2,
        collision: false
      });
      const floor = fMap.getFloor(+item.floor);
      mapImgMarker.addTo(floor);

      mapImgMarkerArr.push(mapImgMarker)
    })
  } else {
    if (mapImgMarkerArr.length) {
      mapImgMarkerArr.forEach((item) => {
        item.remove();
      });
      mapImgMarkerArr = []
    }
  }
};

function api(url, data, cb, type = 'GET', isjson = false, showLoading = true) {
  $.ajax({
    url: BASE_URL + url,
    data,
    type,
    headers: {
      "Content-Type": isjson ? "application/json" : 'application/x-www-form-urlencoded'
    },
    beforeSend: function () {
      if (showLoading) {
        loading();
      }
    },
    complete: function () {
      removeLoad();
    },
    success: function (res) {
      removeLoad();
      installErrorType = '';
      cb(res)
    },
    error: function (err) {
      removeLoad();
      if (installErrorType === 'install') {
        showToast('安装失败，数据已保存在本地', true, 2000);
        installErrorType = '';
      } else if (installErrorType === 'login') {
        showToast('系统繁忙', true, 2000);
        uni.redirectTo({
          url: '/pages/login/login?type=unlogin',
        });
        installErrorType = '';
      } else {
        showToast(networkTypeText, true, 2000);
      }
    }
  })
};

$.ajaxSetup({
  xhrFields: {
    withCredentials: true
  }
});

function showToast(txt, mask, time) {
  let html = '';

  if (mask) {
    html = `
      <div class="toastMain">
        <div class="toastCenter">
          ${txt}
        </div>
      </div>
    `;
  } else {
    html = `
      <div class="toastCenter">
        ${txt}
      </div>
    `;
  }

  $("#toast").html(html);
  $("#toast").css('display', 'block');


  setTimeout(() => {
    $("#toast").css('display', 'none');
  }, time);
};

function loading() {
  $("#load").css('display', 'block');
  setTimeout(() => {
    removeLoad();
  }, 5 * 1000);
};

function removeLoad() {
  $("#load").css('display', 'none');
};

function quit() {
  sendMessage({
    todo: 'switchInspection',
    flag: true,
    threshold: threshold,
    map: initMapData.id
  });
  if (locationMarker) {
    locationMarker.remove();
  };
  currentFunction = false;

  showMenu();
  setMenuListItemImgSrc(0);

  cancelNavi();
  cancelSearch();
  cancelInspec();

  $.confirm({
    title: '',
    text: '确定退出登录',
    onOK: function () {
      //点击确认
      uni.redirectTo({
        url: '/pages/login/login?type=unlogin',
      });
    },
    onCancel: function () { }
  });
};

function openInspection() {
  /* 开始巡检 */
  showMenu();
  setMenuListItemImgSrc(4);

  cancelNavi();
  cancelSearch();

  if (!fMap) {
    showToast('请先选择地图', true, 2000);
    setTimeout(() => {
      $("#selectMap").picker("open");
    }, 2000);
    return
  };

  if (!currentFunction) {
    getBeaconList('insp');
  };

  showInspectionListBox();
};

function tapBtn() {
  hiddenMenu();
};

function changeInstall() {
  sendMessage({
    todo: 'switchInspection',
    flag: true,
    threshold: threshold,
    map: initMapData.id
  });
  if (locationMarker) {
    locationMarker.remove();
  };
  currentFunction = false;

  showMenu();
  setMenuListItemImgSrc(1);
  cancelSearch();
  cancelInspec();

  if (!fMap) {
    showToast('请先选择地图', true, 2000);
    setTimeout(() => {
      $("#selectMap").picker("open");
    }, 2000);
    return
  }

  showInstallList();
};

function searchAll() {
  sendMessage({
    todo: 'switchInspection',
    flag: true,
    threshold: threshold,
    map: initMapData.id
  });
  if (locationMarker) {
    locationMarker.remove();
  };
  currentFunction = false;

  showMenu();
  setMenuListItemImgSrc(2);
  cancelNavi();
  cancelInspec();

  if (!fMap) {
    showToast('请先选择地图', true, 2000);
    setTimeout(() => {
      $("#selectMap").picker("open");
    }, 2000);
    return
  }

  showListIndex = 0;
  netIndex = 0;
  sortIndex = 0;
  abctypeI = 0;
  getBeaconList('list');
};

function update() {
  sendMessage({
    todo: 'switchInspection',
    flag: true,
    threshold: threshold,
    map: initMapData.id
  });
  if (locationMarker) {
    locationMarker.remove();
  };
  currentFunction = false;

  showMenu();
  setMenuListItemImgSrc(3);

  cancelNavi();
  cancelSearch();
  cancelInspec();
};

function earlyWarn() {
  sendMessage({
    todo: 'switchInspection',
    flag: true,
    threshold: threshold,
    map: initMapData.id
  });
  if (locationMarker) {
    locationMarker.remove();
  };
  currentFunction = false;

  showMenu();
  setMenuListItemImgSrc(5);
  cancelNavi();
  cancelInspec();

  if (!fMap) {
    showToast('请先选择地图', true, 2000);
    setTimeout(() => {
      $("#selectMap").picker("open");
    }, 2000);
    return
  }

  ewType = 0;
  ewLevels = 0;
  ewState = 0;
  ewDevice = 0;

  getEWarnList('list');
};

function hiddenMenu() {
  $("#menu").hide();
  $("#menuList").show();
};

function showMenu() {
  $("#menuList").hide();
  $("#menu").show();
};

function setMenuListItemImgSrc(type) {
  $("#img1").attr('src', './static/images/noAnz.png');
  $("#img2").attr('src', './static/images/noSousuo.png');
  $("#img3").attr('src', './static/images/noMap.png');
  $("#img4").attr('src', './static/images/noInspection.png');
  $("#img5").attr('src', './static/images/noEwarn.png');

  if (type == 1) {
    $("#img1").attr('src', './static/images/anz.png');
  } else if (type == 2) {
    $("#img2").attr('src', './static/images/sousuo.png');
  } else if (type == 3) {
    $("#img3").attr('src', './static/images/map.png');
  } else if (type == 4) {
    $("#img4").attr('src', './static/images/inspeciton.png');
  } else if (type == 5) {
    $("#img5").attr('src', './static/images/ewarn.png');
  }
};

function showInstallList() {
  $("#installList").show();
};

function hideInstallList() {
  $("#installList").hide();
  setMenuListItemImgSrc(0);
};

function installItem(e, type) {
  e.stopPropagation();
  $("#installList").hide();
  showImg = true;
  installType = type;

  showMarkerInMap(false, false, false, false, false);

  if (type == 0 || type == 5) {
    /* 0-信标安装 */ /* 5-道钉安装 */
    getBeaconList('install');
  } else if (type == 1) {
    /* 检测器安装 */
    getDetector('install');
  } else if (type == 2) {
    /* 网关安装 */
    getGatewaySel('install')
  } else if (type == 3) {
    /* 地锁安装 */
    getLockTimesConfig();
  } else if (type == 4) {
    /* 离线存储 */
    let data = JSON.parse(localStorage.getItem('xinbiaoData')) || [];
    offlineIndex = 0;
    offlineList = data;
  }

  showInstallOperateBox();
};

var siob1 = true;
var offLineChecked = false;
function showInstallOperateBox() {
  let type = installType;
  let html = ''
  if (type == 0 || type == 5) {
    /* 信标安装 */ /* 道钉安装 */
    html = `
      <div class="installMain">
        <div class="footer-box">
          <div class="detail">
            <div class="detailTop">
              <div class="detail-name">${type === 0 ? '信标编号' : '道钉编号'}</div>
              <text class="colon">:</text>
              <input class="inputTop" type="text" id="installVal0" value="" data-val="text" placeholder="请输入${type === 0 ? '信标编号' : '道钉编号'}" />
              <div class="scanTopBtn" onclick="scanCodeList(0)">
                <img src="./static/images/scan.png"></img>
              </div>
            </div>
            <div class="detailBottom">
              <div class="detail-name">位置</div>
              <text class="colon">:</text>
              <input class="inputTop" disabled id="installMapVal0" type="text" value="" placeholder="请在地图上选点" />
              <div class="nullBtn"></div>
            </div>
          </div>
        </div>
        <div class="footer-features">
          <div class="features-cancel" onclick="cancelNavi(1)">取消</div>
          <div class="features-navi" onclick="affirmbtn()">确认</div>
        </div>
      </div>
    `;
  } else if (type == 1) {
    /* 检测器安装 */
    html = `
      <div class="installMain">
        <div class="footer-box">
          <div class="detail">
            <div class="detailTop">
              <div class="detail-name stallName">车位名称</div>
              <text class="colon">:</text>
              <input class="inputTop" type="text" id="installVal1"  value="" placeholder="请输入车位名称" />
              <div class="nullBtn"></div>
            </div>
            <div class="detailBottom">
              <div class="detail-name stallName">检测器编号</div>
              <text class="colon">:</text>
              <input class="inputTop" type="text" id="installMapVal1" value="" placeholder="请输入检测器编号" />
              <div class="scanTopBtn" onclick="scanCodeList('1')">
                <img src="./static/images/scan.png"></img>
              </div>
            </div>
          </div>
        </div>
        <div class="footer-features">
          <div class="features-cancel" onclick="cancelNavi(1)">取消</div>
          <div class="features-navi" onclick="affirmStall()">确认</div>
        </div>
      </div>
    `
  } else if (type == 2) {
    /* 网关安装 */
    html = `
      <div class="installMain">
        <div class="footer-box">
          <div class="detail">
            <div class="detailTop">
              <div class="detail-name">网关编号</div>
              <text class="colon">:</text>
              <input class="inputTop" type="text" id="installVal2"  value="" placeholder="请输入网关编号" />
              <div class="scanTopBtn" onclick="scanCodeList('2')">
                <img src="./static/images/scan.png"></img>
              </div>
            </div>
            <div class="detailBottom">
              <div class="detail-name">位置</div>
              <text class="colon">:</text>
              <input class="inputTop" disabled type="text" id="installMapVal2" value="" placeholder="请在地图上选点" />
              <div class="nullBtn"></div>
            </div>
          </div>
        </div>
        <div class="footer-features">
          <div class="features-cancel" onclick="cancelNavi(1)">取消</div>
          <div class="features-navi" onclick="affirmStation()">确认</div>
        </div>
      </div>
    `;
  } else if (type == 3) {
    /* 地锁安装 */
    html = `
      <div class="installMain">
        <div class="footer-box">
          <div class="detail">
            <div class="detailTop">
              <div class="detail-name stallName">车位名称</div>
              <text class="colon">:</text>
              <input class="inputTop" type="text" id="installVal3" value="" placeholder="请输入车位名称" />
              <div class="nullBtn"></div>
            </div>
            <div class="detailBottom">
              <div class="detail-name stallName">地锁编号</div>
              <text class="colon">:</text>
              <input class="inputTop" type="text" id="installMapVal3" value="" placeholder="请输入地锁编号" />
              <div class="scanTopBtn" onclick="scanCodeList(0)">
                <img src="./static/images/scan.png"></img>
              </div>
            </div>
          </div>
        </div>
        <div class="footer-features">
          <div class="features-cancel" onclick="cancelNavi(1)">取消</div>
          <div class="features-navi" onclick="affirmLock()">确认</div>
        </div>
      </div>
    `;
  } else if (type == 4) {
    /* 离线存储 */
    html = `
      <div class="offlineFooter">
        <div>
          <div class="cancelBtn" onclick="cancelNavi(1)">
            <img src="./static/images/cancel.png"></img>
          </div>
          <div class="offlineToolbar">
            <input class="picker devicePicker" id="offlinTypeBtn" type="text" value="信标">
            <div class="offlineBtn" onclick="affirmOffline()">安装</div>

            <label style="line-height: 0;margin-left: 2.6667vw;" id="offLineCheckedBox">
              <input type="checkbox" onclick="allOffLineChange()" ${offLineChecked ? 'checked' : ''}/>
              <span>全选</span>
            </label>

            <div class="offlineBtn" onclick="deleteOffline()">删除</div>
          </div>
          <div class="offlineScroll">
            <div class="offlineContent" id="offlineContentList">`


    html += `</div>
          </div>
        </div>
      </div>
    `;
  };

  $("#installOperateBox").html(html);

  showOfflineContetntListHtml();

  if (type == 4 && siob1) {
    siob1 = false;

    let changeV = '';
    let arr = ['信标', '检测器', '网关', '地锁']
    $("#offlinTypeBtn").picker({
      title: "请选择",
      cols: [
        {
          textAlign: 'center',
          values: [...arr]
        }
      ],
      onChange: function (p, v, dv) {
        changeV = v[0];
      },
      onClose: function (p, v, d) {
        if (changeV) {
          let index = arr.findIndex((item) => {
            return item == changeV
          });

          let offlineStr = index === 0 ? 'xinbiaoData' :
            index === 1 ? 'stallData' :
              index === 2 ? 'gatewayData' :
                index === 3 ? 'offLockData' : '';
          let data = JSON.parse(localStorage.getItem(offlineStr)) || [];
          if (offlineStr === 'stallData') {
            data ? data.forEach(item => item.macSuo = item.mac.slice(item.mac.length - 6)) : '';
          }
          offlineIndex = index;
          offlineList = data;

          showOfflineContetntListHtml();
        }
      }
    });
  }
};

function showOfflineContetntListHtml() {
  let len = offlineList.length;
  if (len) {
    let html = '';
    for (let i = 0; i < len; i++) {
      let item = offlineList[i];
      html += `
          <div class="col">
            <input type="checkbox" onclick="deviceChange('${(offlineIndex === 0 || offlineIndex === 2) ? item.num : item.mac}')" ${item.checked ? 'checked' : ''}/>
            <div
              class="${offlineIndex === 0 ? 'listName' : offlineIndex === 1 ? 'listNameMac' : 'wanggListName'}">
              ${offlineIndex === 0 || offlineIndex === 2 ? item.num : item.mac}
            </div>
            <div class="stallCol ${offlineIndex === 1 || offlineIndex === 3 ? '' : 'modalHideReplace'}">
              ${item.placeName}
            </div>
            <div class="deleteBtn" onclick="delOffline(this)" data-item='${JSON.stringify(item)}'>
              <img src="./static/images/delete.png"></img>
            </div>
          </div>
        `
    };

    $("#offlineContentList").html(html)
  } else {
    $("#offlineContentList").html('')
  }
};

function allOffLineChange() {
  let sortArr = offlineList;
  offLineChecked = !offLineChecked
  for (let i = 0, lenI = sortArr.length; i < lenI; ++i) {
    sortArr[i].checked = offLineChecked;
  };

  showOfflineContetntListHtml();
};

function deleteOffline() {
  offLineChecked = false;
  let sortArr = offlineList;
  let delArr = [];
  for (let i = 0, lenI = sortArr.length; i < lenI; ++i) {
    if (sortArr[i].checked) {
      delArr.push(sortArr[i]);
    }
  };

  if (delArr.length) {
    let num_mac = (offlineIndex === 0 || offlineIndex === 2) ? 'num' : 'mac';
    let index = offlineIndex;
    let offlineStr = index === 0 ? 'xinbiaoData' :
      index === 1 ? 'stallData' :
        index === 2 ? 'gatewayData' :
          index === 3 ? 'offLockData' : '';
    let data = JSON.parse(localStorage.getItem(offlineStr)) || [];
    data = data.filter(item => !delArr.some(delItem => {
      return delItem[num_mac] == item[num_mac]
    }));

    localStorage.setItem(offlineStr, JSON.stringify(data));
    offlineList = data;
    showOfflineContetntListHtml();
  }

  $("#offLineCheckedBox").html(`
    <input type="checkbox" onclick="allOffLineChange()" ${offLineChecked ? 'checked' : ''}/>
    <span>全选</span>
  `)
};

function clearInputValue() {
  if (installType == 0 || installType == 5) {
    document.getElementById('installVal0').value = '';
    document.getElementById('installMapVal0').value = '';
  } else if (installType == 1) {
    document.getElementById('installVal1').value = '';
    document.getElementById('installMapVal1').value = '';
  } else if (installType == 2) {
    document.getElementById('installVal2').value = '';
    document.getElementById('installMapVal2').value = '';
  } else if (installType == 3) {
    document.getElementById('installVal3').value = '';
    document.getElementById('installMapVal3').value = '';
  }
};

/* 信标安装扫码 */
function scanCodeList(index) {
  sendMessage({
    todo: 'scanCodeList',
    index
  })
};

function scanCodeList2(index) {
  sendMessage({
    todo: 'scanCodeList2',
    index
  })
};

/* 安装-取消 */
function cancelNavi(c) {
  showImg = false;
  siob1 = true;

  showBeacon = [];
  showDetector = [];
  showWangG = [];
  showLock = [];

  $("#installOperateBox").html('')
  if (c) {
    setMenuListItemImgSrc(0);
  }
  fmapClickData = {};
  if (clickMapImgMarker) {
    clickMapImgMarker.remove();
  };
};

/* 信标安装-确认 */
function affirmbtn() {
  installText = $("#installVal0")[0].value;
  installMapVal = JSON.parse(JSON.stringify(fmapClickData));

  if (!installText) {
    let text = installType === 0 ? '请输入信标编号' : '请输入道钉编号';
    showToast(text, false, 2000);
    return
  };

  if (!(installMapVal && installMapVal.x)) {
    showToast('请先在地图上选点', false, 2000);
    return
  }

  affirmbtn2(null, null);
};

/* 检测器安装-确认 */
function affirmStall() {
  installText = $("#installVal1")[0].value;
  installMapVal = $("#installMapVal1")[0].value;

  if (!installText) {
    showToast('请输入车位名称', false, 2000);
    return
  };

  if (!installMapVal) {
    showToast('请输入检测器编号', false, 2000);
    return
  }

  affirmStall2(null, null);
};

function affirmStation() {
  installText = $("#installVal2")[0].value;
  installMapVal = JSON.parse(JSON.stringify(fmapClickData));

  if (!installText) {
    showToast('请输入网关编号', false, 2000);
    return
  };

  if (!(installMapVal && installMapVal.x)) {
    showToast('请先在地图上选点', false, 2000);
    return
  }

  affirmStation2(null, null);
};

function affirmLock() {
  installText = $("#installVal3")[0].value;
  installMapVal = $("#installMapVal3")[0].value;

  if (!installText) {
    showToast('请输入车位名称', false, 2000);
    return
  };

  if (!installMapVal) {
    showToast('请输入地锁编号', false, 2000);
    return
  }

  affirmLock2(null);
};

function affirmbtn2(dupliXinBiao, offlineArr) {
  var addParams;
  var xinbiaoData = JSON.parse(localStorage.getItem('xinbiaoData')) || [];
  if (offlineArr) {
    addParams = offlineArr;
  } else if (dupliXinBiao) {
    let findItemDupli = xinbiaoData.find(item => item.num === dupliXinBiao.num);
    if (findItemDupli) {
      addParams = [{
        num: dupliXinBiao.num,
        x: findItemDupli.x,
        y: findItemDupli.y,
        floor: findItemDupli.floor,
        map: findItemDupli.map || '75',
        fMapId: findItemDupli.fMapId || 'tgymap',
        z: 1,
        type: findItemDupli.type,
        changePlace: dupliXinBiao.changePlace
      }];
    }
  } else {
    const xbiao = {
      num: installText,
      x: installMapVal.x,
      y: installMapVal.y,
      floor: installMapVal.level,
      map: initMapData.id || '75',
      fMapId: initMapData.fmapID || 'tgymap',
      z: 1,
      type: installType === 0 ? 1 : 7,
      changePlace: true
    };
    let index = xinbiaoData.findIndex(item => item.num === installText);
    index >= 0 ? xinbiaoData[index] = xbiao : xinbiaoData.unshift(xbiao);
    localStorage.setItem('xinbiaoData', JSON.stringify(xinbiaoData));
    addParams = [xbiao];
  };

  try {
    let postData = JSON.stringify({
      params: addParams
    });
    installErrorType = 'install'

    api('wxGateway/beaconPos', postData, (res) => {
      if (res.code === 200) {
        var list = res.data;
        for (var i = 0; i < list.length; i++) {
          if (list[i].code === 200) {
            let info = list[i].data;
            let findItem4 = xinbiaoData.find(item => item.num == info.num);
            let obj = {
              num: info.num,
              batteryVolt: 0,
              networkName: "离线",
              x: findItem4.x,
              y: findItem4.y,
              floor: findItem4.floor,
              id: info.id,
              name: info.num
            };
            addMarkerToMap('0', obj)
            clearInputValue();

            if (list[0].code === 200) {
              let title = list[0].message;
              showToast(title, false, 2000);
            }

            let index3 = xinbiaoData.findIndex(item => item.num === obj.num);
            index3 >= 0 ? xinbiaoData.splice(index3, 1) : '';
          } else if (list[i].code === 402) {
            let multiObj = {
              message: list[i].message,
              multiData: list[i].data
            }
            dupliXinBiaoList.push(multiObj);
          }
        };
        localStorage.setItem('xinbiaoData', JSON.stringify(xinbiaoData));
        offlineList = xinbiaoData;
        if (offlineArr) {
          showOfflineContetntListHtml();
        };

        if (!dupliXinBiao) {
          if (list[0].code !== 200) {
            showInstallModal('beacon');
          } else {
            setTimeout(() => {
              showInstallModal('beacon');
            }, 2000);
          }
        } else {
          return new Promise((resolve, reject) => {
            let title = list[0].message;
            showToast(title, false, 2000)
            setTimeout(() => {
              resolve("1111")
            }, 2000);
          })
        }
      }
    }, 'POST', true)

  } catch (e) {
    showToast('安装失败，数据已保存在本地', false, 2000)
  }
};

function affirmStall2(dupliStall, offlineArr, istwoData = 1) {
  var addParams;
  var stallData = JSON.parse(localStorage.getItem('stallData')) || [];
  if (offlineArr) {
    addParams = offlineArr;
  } else if (dupliStall) {
    let findItemDupli = stallData.find(item => item.mac === dupliStall.offlineData.mac && item
      .placeName === dupliStall.placeName[0].name);
    if (findItemDupli) {
      addParams = [{
        placeName: findItemDupli.placeName,
        mac: findItemDupli.mac,
        map: findItemDupli.map || '75',
        changePlace: dupliStall.changePlace,
        changeDevice: dupliStall.changeDevice,
      }];
    }
  } else {
    const chewei = {
      placeName: installText.trim().toUpperCase(),
      mac: installMapVal,
      map: initMapData.id || '75',
      changeDevice: true,
      changePlace: true,
    };
    let index1 = stallData.findIndex(item => item.mac === installMapVal);
    let index2 = stallData.findIndex(item => item.mac === installMapVal && item.placeName === installText && item.map === initMapData.id);
    index2 >= 0 ? '' : index1 >= 0 ? stallData.splice(index1, 1) : '';
    index2 >= 0 ? '' : stallData.unshift(chewei);
    localStorage.setItem('stallData', JSON.stringify(stallData));
    addParams = [chewei];
  };

  try {
    let postData = JSON.stringify({
      params: addParams
    });
    installErrorType = 'install'

    api('wxInfrared/bindInfraredtoPlace', postData, (res) => {
      if (res.code === 200) {
        var list = res.data;
        for (var i = 0; i < list.length; i++) {
          if (list[i].code === 200) {
            let info = list[i].data;
            let obj = {
              placeName: info.placeName[0].name,
              num: info.infred.num,
              rawProductId: info.infred.rawProductId.slice(info.infred.rawProductId.length -
                6),
              networkName: '离线',
              status: 1,
              id: info.infred.id,
              x: +info.placeName[0].x,
              y: istwoData == 2 ? +info.placeName[0].y + 0.5 : +info.placeName[0].y - 0.5,
              floor: +info.placeName[0].floor,
              isreplace: false
            };
            addMarkerToMap('1', obj)

            clearInputValue();
            showToast(list[i].message, false, 2000)

            if (list[0].code === 200) {
              showToast(list[0].message, false, 2000);
            }
            let index3 = stallData.findIndex(item => item.placeName === obj.placeName && item
              .mac === info.infred.rawProductId);
            index3 >= 0 ? stallData.splice(index3, 1) : '';
          } else if (list[i].code === 402 || list[i].code === 502) {
            let multiObj = {
              code: list[i].code,
              message: list[i].message,
              multiData: list[i].data
            }
            dupliStallList.push(multiObj);
          } else if (list[i].code === 400) {
            let index4 = stallData.findIndex(item => item.placeName === list[i].data.placeName &&
              item.mac === list[i].data.mac && item.map == list[i].data.map);
            index4 >= 0 ? stallData.splice(index4, 1) : '';
            showToast(list[0].message, false, 2000)
          }
        }
        localStorage.setItem('stallData', JSON.stringify(stallData));
        offlineList = stallData;
        if (offlineArr) {
          showOfflineContetntListHtml();
        };

        if (!dupliStall) {
          if (list[0].code !== 200 && list[0].code !== 400) {
            showInstallModal('stall');
          } else {
            setTimeout(() => {
              showInstallModal('stall');
            }, 2000);
          }
        } else {
          return new Promise((resolve, reject) => {
            showToast(list[0].message, false, 2000)
            setTimeout(() => {
              resolve("1111")
            }, 2000);
          })
        }
      }
    }, 'POST', true)

  } catch (e) {
    showToast('安装失败，数据已保存在本地', false, 2000)
  }
};

function affirmStation2(dupliWangg, offlineArr) {
  var addParams;
  var gatewayData = JSON.parse(localStorage.getItem('gatewayData')) || [];
  if (offlineArr) {
    addParams = offlineArr;
  } else if (dupliWangg) {
    let findItemDupli = gatewayData.find(item => item.num === dupliWangg.num);
    if (findItemDupli) {
      addParams = [{
        num: dupliWangg.num,
        x: findItemDupli.x,
        y: findItemDupli.y,
        floor: findItemDupli.floor,
        map: findItemDupli.map || '75',
        z: 1,
        changePlace: dupliWangg.changePlace
      }];
    }
  } else {
    const wangg = {
      num: installText,
      x: installMapVal.x,
      y: installMapVal.y,
      floor: installMapVal.level,
      map: initMapData.id || '75',
      z: 1,
      changePlace: true
    };
    let index = gatewayData.findIndex(item => item.num === installText);
    index >= 0 ? gatewayData[index] = wangg : gatewayData.unshift(wangg);
    localStorage.setItem('gatewayData', JSON.stringify(gatewayData));
    addParams = [wangg];
  };

  try {
    let postData = JSON.stringify({
      params: addParams
    });
    installErrorType = 'install'

    api('wxGateway/addGateway', postData, (res) => {
      if (res.code === 200) {
        var list = res.data;
        for (var i = 0; i < list.length; i++) {
          if (list[i].code === 200) {
            let info = list[i].data;
            let obj = {
              num: info.num,
              networkName: "离线",
              x: info.x,
              y: info.y,
              floor: info.floor,
              id: info.id,
              name: info.num
            };
            addMarkerToMap('2', obj)

            clearInputValue();
            showToast(list[i].message, false, 2000)


            if (list[0].code === 200) {
              showToast(list[0].message, false, 2000);
            }
            let index2 = gatewayData.findIndex(item => item.num === obj.num);
            index2 >= 0 ? gatewayData.splice(index2, 1) : '';
          } else if (list[i].code === 402) {
            let multiObj = {
              message: list[i].message,
              multiData: list[i].data
            }
            dupliWanggList.push(multiObj);
          }
        }
        localStorage.setItem('gatewayData', JSON.stringify(gatewayData));
        offlineList = gatewayData;
        if (offlineArr) {
          showOfflineContetntListHtml();
        };

        if (!dupliWangg) {
          if (list[0].code !== 200) {
            showInstallModal('wangg');
          } else {
            setTimeout(() => {
              showInstallModal('wangg');
            }, 2000);
          }
        } else {
          return new Promise((resolve, reject) => {
            showToast(list[0].message, false, 2000)
            setTimeout(() => {
              resolve("1111")
            }, 2000);
          });
        }
      }
    }, 'POST', true)

  } catch (e) {
    showToast('安装失败，数据已保存在本地', false, 2000)
  }
};

function affirmLock2(offlineArr) {
  let addParams;
  var offLockData = JSON.parse(localStorage.getItem('offLockData')) || [];

  if (offlineArr) {
    addParams = offlineArr;
  } else {
    const chewei = {
      placeName: installText.trim().toUpperCase(),
      mac: installMapVal,
      map: initMapData.id || '75',
      changeDevice: true,
      changePlace: true,
    };
    let index1 = offLockData.findIndex(item => item.mac === installMapVal);
    let index2 = offLockData.findIndex(item => item.mac === installMapVal && item.placeName === installMapVal && item.map === initMapData.id);
    index2 >= 0 ? '' : index1 >= 0 ? offLockData.splice(index1, 1) : '';
    index2 >= 0 ? '' : offLockData.unshift(chewei);
    localStorage.setItem('offLockData', JSON.stringify(offLockData));
    addParams = [chewei];
  };

  try {
    let postData = JSON.stringify({
      params: addParams
    });
    installErrorType = 'install'

    api('wxSmallAPP/addFloorLock', postData, (res) => {
      if (res.code === 200) {
        var list = res.data;
        for (var i = 0; i < list.length; i++) {
          if (list[i].code === 200) {
            let info = list[i].data;
            let obj = {
              placeName: info.floorLock.parkingName,
              num: info.decimalNum,
              deviceNum: info.decimalNum,
              networkstate: 0,
              id: info.placeName[0].id,
              x: +info.placeName[0].x,
              y: +info.placeName[0].y,
              floor: +info.placeName[0].floor,
            };

            addMarkerToMap('3', obj);
            clearInputValue();

            if (list[0].code === 200) {
              showToast(list[0].message, false, 2000);
            }

            let index3 = offLockData.findIndex(item => item.placeName === obj.placeName && item
              .mac === obj.num);
            index3 >= 0 ? offLockData.splice(index3, 1) : '';

          } else if (list[i].code === 400) {
            showToast(list[i].message, false, 4000)
          } else if (list[i].code === 502) {
            let info = list[i].data;

            let obj = {
              placeName: info.offlineData.placeName,
              num: info.offlineData.mac
            }

            let index3 = offLockData.findIndex(item => item.placeName === obj.placeName && item
              .mac === obj.num);
            index3 >= 0 ? offLockData.splice(index3, 1) : '';
            showToast(list[i].message, false, 4000)
          }
        };

        if (!offlineArr) {
          // 暂时去除-有需要再加上
          // $.confirm({
          //   title: '',
          //   text: "是否配置地锁",
          //   onOK: function () {
          //     //点击确认
          //     sendMessage({
          //       todo: 'openB'
          //     });
          //     nowConfigLock = JSON.parse(JSON.stringify(addParams[0]));
          //     showConfigLockBox(true);
          //   },
          //   onCancel: function () {

          //   }
          // });
        }
        localStorage.setItem('offLockData', JSON.stringify(offLockData));
        offlineList = offLockData;
        if (offlineArr) {
          showOfflineContetntListHtml();
        }
      }
    }, 'POST', true)
  } catch (e) {
    showToast('安装失败，数据已保存在本地', false, 2000)
  }
};

function deviceChange(data) {
  let sortArr = offlineList;
  let len = offlineList.length;
  let deviceName = (offlineIndex === 1 || offlineIndex === 3) ? 'mac' : 'num';

  for (let i = 0, lenI = sortArr.length; i < lenI; ++i) {
    if (sortArr[i][deviceName] == data) {
      sortArr[i].checked = !sortArr[i].checked;
      break;
    }
  };

  let cArr = offlineList.filter((item) => {
    return item.checked
  });

  if (cArr.length === len) {
    offLineChecked = true;
  } else {
    offLineChecked = false;
  };

  $("#offLineCheckedBox").html(`
    <input type="checkbox" onclick="allOffLineChange()" ${offLineChecked ? 'checked' : ''}/>
    <span>全选</span>
  `)
};

function affirmOffline() {
  let filterArr = offlineList.filter(item => item.checked === true);
  if (filterArr.length > 0) {
    if (offlineIndex === 0) {
      affirmbtn2(null, filterArr);
    } else if (offlineIndex === 1) {
      affirmStall2(null, filterArr);
    } else if (offlineIndex === 2) {
      affirmStation2(null, filterArr);
    } else if (offlineIndex === 3) {
      affirmLock2(filterArr);
    }
  } else {
    showToast('请选择需要安装的数据', false, 2000)
  }
};

function delOffline(e) {
  let data = e.getAttribute('data-item')
  let info = JSON.parse(data);
  let index, offlineStr;

  $.confirm({
    title: '',
    text: '请问是否删除该设备',
    onOK: function () {
      if (offlineIndex === 0) {
        index = offlineList.findIndex(item => item.num === info.num && item
          .map ===
          info.map);
        offlineStr = 'xinbiaoData';
      } else if (offlineIndex === 1) {
        index = offlineList.findIndex(item => item.placeName === info.placeName &&
          item.mac === info.mac && item.map === info.map);
        offlineStr = 'stallData';
      } else if (offlineIndex === 2) {
        index = offlineList.findIndex(item => item.num === info.num && item
          .map ===
          info.map);
        offlineStr = 'gatewayData';
      } else if (offlineIndex === 3) {
        index = offlineList.findIndex(item => item.placeName === info.placeName &&
          item.mac === info.mac && item.map === info.map);
        offlineStr = 'offLockData';
      }
      index >= 0 ? offlineList.splice(index, 1) : '';
      localStorage.setItem(offlineStr, JSON.stringify(offlineList));
      offlineList = offlineList;

      showOfflineContetntListHtml();
    },
    onCancel: function () { }
  });
};

function showConfigLockBox(show) {
  if (show) {
    html = `
      <div class="configLockBox">
        <div class="configLockMain">
          <div class="clm_content">
            <div class="configText"> 公司时段： </div>
            <input class="configPicker" id="configPicker" type="text" value=""  placeholder="请选择">
          </div>
          <div class="clm_footer">
            <div class="clmf_b1" onclick="configClose()"> 取消 </div>
            <div class="clmf_b2" onclick="configConfirm()"> 确定 </div>
          </div>
        </div>
      </div>
    `;

    $("#showConfigLockBox").html(html);

    let changeV = '';
    let arr = configLockList.map((item) => {
      return item.cName
    })
    $("#configPicker").picker({
      title: "请选择",
      cols: [
        {
          textAlign: 'center',
          values: ['', ...arr]
        }
      ],
      onChange: function (p, v, dv) {
        changeV = v[0];
      },
      onClose: function (p, v, d) {
        if (changeV) {
          timesConfigItemData = configLockList.find((item) => {
            return item.cName == changeV
          })
        }
      }
    });

  } else {
    $("#showConfigLockBox").html('')
  }
};

function configClose() {
  showConfigLockBox(false)
};

function configConfirm() {
  if (!(timesConfigItemData && timesConfigItemData.tList)) {
    showToast('请选择公司', true, 2000);
    return
  };
  let data = timesConfigItemData.tList;
  let num = nowConfigLock.mac;
  sendMessage({
    todo: 'configTime',
    data,
    num
  })
};

function addMarkerToMap(type, data) {
  // type -- 0:信标 1:车位检测器 2:网关 3:地锁
  if (type == 0) {
    /* 信标 */
    let index = showBeacon.findIndex(item => item.num === data.num);
    if (index == -1) {
      showBeacon.push(data);
    } else {
      showBeacon.splice(index, 1, data);
    };
    cshowBeacon(showBeacon, 1)
  } else if (type == 1) {
    /* 车位检测器 */
    let index = showDetector.findIndex(item => item.rawProductId === data.rawProductId || data
      .isreplace);
    if (index == -1) {
      showDetector.push(data);
    } else {
      showDetector.splice(index, 1, data);
    };
    cshowBeacon(showDetector, 3)
  } else if (type == 2) {
    /* 网关 */
    let index = showWangG.findIndex(item => item.num === data.num);
    if (index == -1) {
      showWangG.push(data);
    } else {
      showWangG.splice(index, 1, data);
    }
    cshowBeacon(showWangG, 2)
  } else if (type == 3) {
    /* 地锁 */
    let index = showLock.findIndex(item => item.num === data.num);
    if (index == -1) {
      showLock.push(data);
    } else {
      showLock.splice(index, 1, data);
    }
    cshowBeacon(showLock, 4)
  }
};

function showInstallModal(flag) {
  var list;
  var dataStr;

  if (flag === 'beacon') {
    // 信标安装
    list = dupliXinBiaoList;
    dataStr = 'xinbiaoData';
  } else if (flag === 'wangg') {
    list = dupliWanggList;
    dataStr = 'gatewayData';
  } else {
    list = dupliStallList;
    dataStr = 'stallData';
  };

  if (list.length > 0) {
    let tex = list.shift();
    if (flag === 'stall' && tex.code === 402 && tex.multiData.duplicateData.length > 0) {
      let isModal = true;
      let isAddOrReplace = true;
      let modalContent = tex.message + "是否进行添加或替换";

      showModalReplace(isModal, isAddOrReplace, modalContent);
      texObj = tex;

    } else {
      $.confirm({
        title: '',
        text: tex.message + "是否确认修改",
        onOK: function () {
          //点击确认
          installModalConfirm(null, flag, tex, list);
        },
        onCancel: function () {
          texObj = tex;
          installModalCancel(null, dataStr, flag, list);
        }
      });
    }
  }
};

function installModalConfirm(etext, flag, tex, list) {
  let installFlag = flag || (etext == 'tex' ? 'stall' : '');
  let installTex = tex || (etext == 'tex' ? texObj : '');
  let istwodata = etext ? 2 : 1;
  if (installFlag !== 'stall') {
    installTex.multiData.changePlace = false;
  } else {
    if (installTex.code === 402) {
      installTex.multiData.changePlace = false;
      installTex.multiData.changeDevice = true;
    } else {
      installTex.multiData.changeDevice = false;
      installTex.multiData.changePlace = true;
    }
  }
  showModalReplace(false, null, null);

  if (flag === 'beacon') {
    affirmbtn2(installTex.multiData, null)
  } else if (flag == 'wangg') {
    affirmStation2(installTex.multiData, null)
  } else {
    affirmStall2(installTex.multiData, null, istwodata)
  }
};

function installModalCancel(domtext, dataStr, flag, list) {
  let str = dataStr || domtext;
  let dataList = JSON.parse(localStorage.getItem(str)) || [];
  let tex = texObj;
  let index3 = dataList.findIndex(item => {
    if (flag === 'stall') {
      if (item.mac === tex.multiData.offlineData.mac && item.placeName === tex.multiData
        .placeName[0].name)
        return item;
    } else {
      if (item.num === tex.multiData.num)
        return item;
    }
  });
  index3 >= 0 ? dataList.splice(index3, 1) : '';
  localStorage.setItem(str, JSON.stringify(dataList));

  showModalReplace(false, null, null);

  offlineList = dataList;
  if (list) {
    if (list.length > 0) {
      showInstallModal(flag)
    }
  } else {
    if (dupliStallList.length > 0) {
      showInstallModal("stall")
    }
  }
};

function showModalReplace(isModal, isAddOrReplace, modalContent) {
  if (isModal) {
    let html = `
    <div class="DedtiOrPlaceDialog">
      <div class="weui-mask"></div>
      <div class="weui-dialog">
        <div class="weui-dialog__cancel ${modalContent == '' ? 'modalHideReplace' : ''}">
          <img src="./static/images/close.png" onclick="installModalCancel('stall')"></img>
        </div>
        <div class="weui-dialog__hd">
          <strong class="weui-dialog__title"></strong>
        </div>
        <div class="weui-dialog__bd" >${modalContent}</div>
        <div class="weui-dialog__ft ${isAddOrReplace ? '' : 'modalHideReplace'}">
          <div class="weui-dialog__ftItem weui-dialog__ftItem1">
            <div class="weui-dialog__btn weui-dialog__btn_default" onclick="installModalConfirm('tex')"
              hover-class="weui-dialog__btn_active">添加</div>
          </div>
          <div class="weui-dialog__ftItem weui-dialog__ftItem1">
            <div class="weui-dialog__btn weui-dialog__btn_primary" onclick="replaceStallConfirm()"
             hover-class="weui-dialog__btn_active">替换</div>
          </div>
        </div>
      </div>
      <div class="weui-dialog__fBContainer ${isAddOrReplace ? 'modalHideReplace' : ''}">
        <div class="weui-dialog__fB">`

    for (let i = 0; i < replaceArr.length; i++) {
      let item = replaceArr[i];
      html += `<div  class="weui-dialog__fBI">
        <div class="weui-dialog__fBItem" onclick="replaceStall(this)" data-item='${JSON.stringify(item)}'>${item.mac}</div>
        <div class="weui-dialog__fBSplit ${item.isSplit ? '' : 'modalHideReplace'}"></div>
      </div>`
    }

    html += `</div>
        <div class="weui-close">
          <img src="./static/images/close_white.png" onclick="installModalCancel('stall')" class="weui-dialog__fBClose"></img>
        </div>
      </div>
    </div>
    `

    $("#modelReplaceBox").html(html)

  } else {
    $("#modelReplaceBox").html('')
  }
};

function replaceStallConfirm() {
  let tex = texObj;
  tex.multiData.duplicateData.forEach((item, index) => {
    if (index < tex.multiData.duplicateData.length) {
      item.isSplit = true;
    } else {
      item.isSplit = false;
    }
  });
  let isModal = true;
  let isAddOrReplace = false;
  let modalContent = '';
  replaceArr = tex.multiData.duplicateData;

  showModalReplace(isModal, isAddOrReplace, modalContent);
};

function replaceStall(e) {
  let data = e.getAttribute('data-item')
  var info = JSON.parse(data);
  var tex = texObj;
  let stallData = JSON.parse(localStorage.getItem('stallData')) || [];
  var findItem = stallData.find(item => item.mac === tex.multiData.offlineData.mac && item.placeName ===
    tex.multiData.placeName[0].name);
  var replaceParams = {
    infraredId: info.infraredId,
    placeId: info.placeId,
    mac: findItem.mac,
    map: findItem.map,
    placeName: findItem.placeName,
    fid: info.fid
  };

  try {
    api('wxInfrared/updateInfrared', replaceParams, (res) => {
      if (res.code === 200) {
        var data = res.data
        const resultPlace = beStallList.find(item => item.placeName === findItem.placeName);
        if (resultPlace) {
          let obj = {
            placeName: resultPlace.placeName,
            num: resultPlace.num,
            rawProductId: data.rawProductId.slice(data.rawProductId.length - 6),
            networkName: resultPlace.networkName,
            status: resultPlace.status,
            id: resultPlace.id || null,
            x: +resultPlace.x,
            y: +resultPlace.y,
            floor: +resultPlace.floor,
            isreplace: true
          };
          addMarkerToMap('1', obj)
        }
        let index3 = stallData.findIndex(item => item.placeName === findItem.placeName && item.mac ===
          findItem.mac);
        index3 >= 0 ? stallData.splice(index3, 1) : '';
        localStorage.setItem('stallData', JSON.stringify(stallData));
        offlineList = stallData;

        showModalReplace(false);
        clearInputValue()

        showToast(res.message, false, 2000)
        if (dupliStallList.length > 0) {
          showInstallModal("stall")
        }
      }
    })
  } catch (e) {

  }
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

/* evalJS */
function getUNIAPPPOSTDATA(data) {
  if (data && data.length) {
    if (bluetoothArr.length) {
      let datalen = data.length;
      for (let i = 0; i < datalen; i++) {
        let target = data[i];
        let had = bluetoothArr.some((item) => {
          return item.name == target.name
        });

        if (had) {
          let iIndex = bluetoothArr.findIndex((item) => {
            return item.name == target.name
          });
          let ch = bluetoothArr[iIndex].check;
          target.check = ch;
          bluetoothArr.splice(iIndex, 1, target)
        } else {
          bluetoothArr.push(target);
        }
      }
    } else {
      bluetoothArr = data;
    }
  };

  showsilb_scroll_box();
};

function getNowDeviceInfo(data) {
  beaconNum = data.beaconNum;
  beaconDir = data.beaconDir;
  beaconVolt = data.beaconVolt;

  showsilb_info_box();
};

function modifyBeacon(data) {
  if (!(data && data.length)) return;

  let html = `
    <div class="modifyBeaconBox">
      <div class="modifyBeaconMain">
        <div class="MBH">
          <div class="MBH_text"> 地锁配置 </div>
          <div class="MBH_icon">
            <img onclick="closeMBH_ICON()" class="MBH_i_i" src="./static/images/close.png" ></img>
          </div>
        </div>

        <div class="MBC">
          <div class="MBC_s">
            <div class="MBC_s_t"> 配置成功： </div>
            <div class="MBC_s_n">`

  for (let i = 0; i < data.length; i++) {
    let item = data[i];
    if (item.s) {
      html += `<text class="MVC_s_n_t">${item.num}</text>`
    }
  }
  html += `</div>
          </div>
          <div class="MBC_f">
            <div class="MBC_f_t"> 配置失败： </div>
            <div class="MBC_f_n">`
  for (let i = 0; i < data.length; i++) {
    let item = data[i];
    if (!item.s) {
      html += `<text class="MVC_s_n_t">${item.num}</text>`
    }
  }
  html += `</div>
          </div>
        </div>
      </div>
    </div>
  `;

  $("#showModifyBeaconBox").html(html)
};

function closeMBH_ICON() {
  $("#showModifyBeaconBox").html('')
};

var abctype = ['全部', '信标', '道钉'];
var abctypeI = 0;
function showDeviceListBox() {
  console.log('12', showListIndex);

  let html = `
    <div class="deviceListView">
      <div class="searchContainer">
        <div class="cancelBtn" onclick="cancelSearch()">
          <img src="./static/images/cancel.png"></img>
        </div>
        <div class="toolbar2">
          <div class="toobarTop">
            <input class="picker devicePicker marR" readonly id="deviceArray" type="text" value="${deviceArray[showListIndex]}">
            <input class="picker marR" id="netArray" readonly type="text" value="${netArray[netIndex]}">
            ${showListIndex === 0 ? `<input class="picker marR" id="abctype" readonly type="text" value="${abctype[abctypeI]}">` : ''}
            <div class="toolIBtn marR p1b">
              <image class="sortI ${sortIndex == 0 ? '' : 'modalHideReplace'}" src="./static/images/noSort.png"></image>
              <image class="sortI ${sortIndex == 1 ? '' : 'modalHideReplace'}" src="./static/images/up.png"></image>
              <image class="sortI ${sortIndex == 2 ? '' : 'modalHideReplace'}" src="./static/images/down.png"></image>
              <input class="p1" id="sortArray" type="text" value="">
            </div>
            <div class="toolIBtn marR" onclick="refreshList()">
              <img class="refreshBtn" src="./static/images/refresh.png"></img>
            </div>
            <div class="toolIBtn ${showListIndex != 3 ? '' : 'modalHideReplace'}" onclick="scanCodeList2('${showListIndex}')">
              <img class="scanBtn" src="./static/images/scan.png"></img>
            </div>
            <div class="toolIBtn lockconfig ${showListIndex == 3 ? '' : 'modalHideReplace'}" onclick="lockConfig()">
              配置
            </div>
          </div>
          <div class="toolbarMiddle">
            <div class="icsbox ${showListIndex == 1 ? '' : 'modalHideReplace'}">
              <input class="radio-group" type="checkbox" onclick="radioChange()" ${checked ? 'checked' : ''}/>
              <span class="cheRadio">车位名</span>
            </div>
            <input
              class="searchInput ${showListIndex === 1 ? 'searchCheInput marLeft' : ''} ${showListIndex === 0 ? 'searchXinInput' : ''}"
              type="text"
              placeholder="${showListIndex !== 1 ? '请输入编号' : showListIndex === 1 && checked === false ? '请输入检测器编号后6位' : showListIndex === 1 && checked === true ? '请输入车位名' : ''}"
              id="listsearchValue" />
            <div class="toolBtn sousuoBtn" onclick="sortList()">搜索</div>
            <div class="icsbox ${(netIndex === 2 && (showListIndex == 0 || showListIndex == 2 || showListIndex == 3)) || (netIndex === 3 && showListIndex === 0) ? '' : 'modalHideReplace'}">
              <input class="radio-group marLeft" type="checkbox" onclick="allCheckedChange()" ${locationChecked ? 'checked' : ''}/>
              <span class="radio">全选</span>
            </div>
            <div class="toolBtn marLeft ${(netIndex === 2 && (showListIndex == 0 || showListIndex == 2 || showListIndex == 3)) || (netIndex === 3 && showListIndex === 0) ? '' : 'modalHideReplace'}" onclick="setLocate()">
              定位
            </div>
          </div>
          <div class="toolbarBottom">
            <div class="span1">
              <span>${deviceArray[showListIndex]}</span>
              <span>${netArray[netIndex]}:</span>
              <span class="${showListIndex == 0 ? '' : 'modalHideReplace'} showDataListText0">${sortBaseStationList.length}</span>
              <span class="${showListIndex == 1 ? '' : 'modalHideReplace'} showDataListText1">${sortBeStallList.length}</span>
              <span class="${showListIndex == 2 ? '' : 'modalHideReplace'} showDataListText2">${sortBstationList.length}</span>
              <span class="${showListIndex == 3 ? '' : 'modalHideReplace'} showDataListText3">${sortLockList.length}</span>
            </div>
            <div class="toolbarBottomR ${(netIndex === 2 || netIndex === 3) && showListIndex === 1 ? '' : 'modalHideReplace'}">
              <input class="radio-group marLeft" type="checkbox" onclick="allCheckedChange()" ${locationChecked ? 'checked' : ''}/>
              <span class="toolBtn marLeft" onclick="setLocate()">定位</span>
            </div>
          </div>
        </div>
        <div id="showDeviceListItemListBox"></div>
      </div>
    </div>
  `;

  $("#showDeviceListBox").html(html);

  let changeV1 = '';
  $("#deviceArray").picker({
    title: "请选择",
    cols: [
      {
        textAlign: 'center',
        values: [...deviceArray]
      }
    ],
    onChange: function (p, v, dv) {
      changeV1 = v[0];
    },
    onClose: function (p, v, d) {
      if (changeV1) {
        let index = deviceArray.findIndex((item) => {
          return item == changeV1;
        });
        if (showListIndex == index) return;
        showListIndex = index;
        netIndex = 0;
        sortIndex = 0;
        listsearchValue = '';
        locationChecked = false;
        checked = false;

        if (index == 0) {
          getBeaconList('list')
        } else if (index == 1) {
          getDetector('list')
        } else if (index == 2) {
          getGatewaySel('list');
        } else if (index == 3) {
          getLockDataList();
        }
      }
    }
  });

  let changeV2 = '';
  if (showListIndex == 2 || showListIndex == 3) {
    $("#netArray").picker({
      title: "请选择",
      cols: [
        {
          textAlign: 'center',
          values: [...netArray2]
        }
      ],
      onChange: function (p, v, dv) {
        changeV2 = v[0];
      },
      onClose: function (p, v, d) {
        if (changeV2) {
          let index = netArray2.findIndex((item) => {
            return item == changeV2;
          });
          if (netIndex == index) return;
          netIndex = index;
          /* 重置排序方式 */
          sortIndex = 0;
          /* 重置类型 */
          abctypeI = 0;

          changeData('state');
        }
      }
    });
  } else {
    $("#netArray").picker({
      title: "请选择",
      cols: [
        {
          textAlign: 'center',
          values: [...netArray]
        }
      ],
      onChange: function (p, v, dv) {
        changeV2 = v[0];
      },
      onClose: function (p, v, d) {
        if (changeV2) {
          let index = netArray.findIndex((item) => {
            return item == changeV2;
          });
          if (netIndex == index) return;
          netIndex = index;
          /* 重置排序方式 */
          sortIndex = 0;
          /* 重置类型 */
          abctypeI = 0;

          changeData('state');
        }
      }
    });
  };

  let changeV3 = '';
  $("#sortArray").picker({
    title: "请选择",
    cols: [
      {
        textAlign: 'center',
        values: [...sortArray]
      }
    ],
    onChange: function (p, v, dv) {
      changeV3 = v[0];
    },
    onClose: function (p, v, d) {
      if (changeV3) {
        let index = sortArray.findIndex((item) => {
          return item == changeV3;
        });
        if (sortIndex == index) return;
        sortIndex = index;
        changeData('sort');
      }
    }
  });

  if (showListIndex == 0) {
    let changeV4 = '';
    $("#abctype").picker({
      title: "请选择",
      cols: [
        {
          textAlign: 'center',
          values: [...abctype]
        }
      ],
      onChange: function (p, v, dv) {
        changeV4 = v[0];
      },
      onClose: function (p, v, d) {
        if (changeV4) {
          let index = abctype.findIndex((item) => {
            return item == changeV4;
          });
          if (abctypeI == index) return;
          abctypeI = index;
          changeData('atype');
        }
      }
    });
  }

  showDeviceListItemListBox();
};

function cancelSearch() {
  $("#showDeviceListBox").html('');

  cshowLocationArr();
};

function showDeviceListItemListBox() {
  let data = [];
  let html = '';

  if (showListIndex == 0) {
    data = sortBaseStationList;
    $(".showDataListText0").html(data.length);
    html += `
      <div class="listH">
        <div class="listItemNum">编号</div>
        <div class="listItem">网络</div>
        <div class="listItem">电压</div>
        <div class="listItem imgBoxWidth">定位</div>
        <div class="listItem imgBoxWidth">删除</div>
      </div>
      <div class="searchScroll">
    `;

    let len = data.length;
    for (let i = 0; i < len; i++) {
      let item = data[i];
      html += `
        <div class="listContent">
          <div class="listItemNum"> 
            <input type="checkbox" onclick="deviceChange2(${item.name})" ${item.checked ? 'checked' : ''} class="${netIndex === 2 || netIndex === 3 ? '' : 'modalHideReplace'}"/>
            <div>${item.name}</div>
          </div>
          <div class="listItem">
            <div class="online ${item.networkName === '在线' ? '' : 'modalHideReplace'}">在线</div>
            <div class="offline ${item.networkName === '在线' ? 'modalHideReplace' : ''}">${item.networkName}</div>
          </div>
          <div class="listItem">
            ${item.batteryVolt === null ? 0 : item.batteryVolt}
          </div>
          <div class="listItem imgBoxWidth">
            <img class="lockloca" onclick="locationB(this)" data-info='${JSON.stringify(item)}' src="./static/images/site.png"></img>
          </div>
          <div class="listItem imgBoxWidth">
            <img class="lockd" onclick="delXinbiao(this)" data-info='${JSON.stringify(item)}' src="./static/images/delete.png"></img>
          </div>
        </div>
      `;
    }
  } else if (showListIndex == 1) {
    data = sortBeStallList;
    $(".showDataListText1").html(data.length);
    html += `
      <div class="listH">
        <div class="listItemNum">编号</div>
        <div class="listItem imgBoxWidth">网络</div>
        <div class="listItem" style="flex:4">车位号</div>
        <div class="listItem imgBoxWidth">状态</div>
        <div class="listItem imgBoxWidth">定位</div>
        <div class="listItem imgBoxWidth">删除</div>
      </div>
      <div class="searchScroll">
    `;

    let len = data.length;
    for (let i = 0; i < len; i++) {
      let item = data[i];
      html += `
        <div class="listContent">
          <div class="listItemNum"> 
            <input type="checkbox" onclick="deviceChange2(${item.num})" ${item.checked ? 'checked' : ''} class="${netIndex === 2 || netIndex === 3 ? '' : 'modalHideReplace'}"/>
            <div>${item.rawProductId}</div>
          </div>
          <div class="listItem imgBoxWidth">
            <div class="online ${item.networkName === '在线' ? '' : 'modalHideReplace'}">在线</div>
            <div class="offline ${item.networkName === '在线' ? 'modalHideReplace' : ''}">${item.networkName}</div>
          </div>
          <div class="listItem"  style="flex:4"> ${item.placeName} </div>
          <div class="listItem imgBoxWidth">
            <div class="online ${item.status === '0' ? '' : 'modalHideReplace'}">空闲</div>
            <div class="offline ${item.status === '0' ? 'modalHideReplace' : ''}">已停</div>
          </div>
          <div class="listItem imgBoxWidth">
            <img class="lockloca" onclick="locationB(this)" data-info='${JSON.stringify(item)}' src="./static/images/site.png"></img>
          </div>
          <div class="listItem imgBoxWidth">
            <img class="lockd" onclick="deljiance(this)" data-info='${JSON.stringify(item)}' src="./static/images/delete.png"></img>
          </div>
        </div>
      `;
    }
  } else if (showListIndex == 2) {
    data = sortBstationList;
    $(".showDataListText2").html(data.length);
    html += `
      <div class="listH">
        <div class="listItemNum">编号</div>
        <div class="listItem">网络</div>
        <div class="listItem imgBoxWidth">定位</div>
        <div class="listItem imgBoxWidth">删除</div>
      </div>
      <div class="searchScroll">
    `;

    let len = data.length;
    for (let i = 0; i < len; i++) {
      let item = data[i];
      html += `
        <div class="listContent">
          <div class="listItemNum"> 
            <input type="checkbox" onclick="deviceChange2(${item.num})" ${item.checked ? 'checked' : ''} class="${netIndex === 2 ? '' : 'modalHideReplace'}"/>
            <div>${item.num}</div>
          </div>
          <div class="listItem">
            <div class="online ${item.networkName === '在线' ? '' : 'modalHideReplace'}">在线</div>
            <div class="offline ${item.networkName === '在线' ? 'modalHideReplace' : ''}">离线</div>
          </div>
          <div class="listItem imgBoxWidth">
            <img class="lockloca" onclick="locationB(this)" data-info='${JSON.stringify(item)}' src="./static/images/site.png"></img>
          </div>
          <div class="listItem imgBoxWidth">
            <img class="lockd" onclick="delwangguan(this)" data-info='${JSON.stringify(item)}' src="./static/images/delete.png"></img>
          </div>
        </div>
      `;
    }
  } else if (showListIndex == 3) {
    data = sortLockList;
    $(".showDataListText3").html(data.length);
    html += `
      <div class="listH">
        <div class="listItemNum">编号</div>
        <div class="listItem">网络</div>
        <div class="listItem">状态</div>
        <div class="listItem">电量</div>
        <div class="listItem imgBoxWidth">位置</div>
        <div class="listItem imgBoxWidth">删除</div>
      </div>
      <div class="searchScroll">
    `;

    let len = data.length;
    for (let i = 0; i < len; i++) {
      let item = data[i];
      html += `
        <div class="listContent">
          <div class="listItemNum">
            <input type="checkbox" onclick="deviceChange2(${item.deviceNum})" ${item.checked ? 'checked' : ''} class="${netIndex === 2 ? '' : 'modalHideReplace'}"/>
            <div>${item.deviceNum}</div>
          </div>
          <div class="listItem">
            <div class="online ${item.networkstate == 1 ? '' : 'modalHideReplace'}">在线</div>
            <div class="offline ${item.networkstate == 1 ? 'modalHideReplace' : ''}">离线</div>
          </div>
          <div class="listItem"> ${item.floorLockState == 0 ? '降锁' : (item.floorLockState == 1 ? '升锁' : '异常')} </div>
          <div class="listItem">
            <div class="online ${item.power >= 60 ? '' : 'modalHideReplace'}">${item.power}%</div>
            <div class="offline ${item.power >= 60 ? 'modalHideReplace' : ''}">${(item.power != null && item.power != 'null') ? item.power : '0'}%</div>
          </div>
          <div class="listItem imgBoxWidth">
            <img class="lockloca" onclick="locationB(this)" data-info='${JSON.stringify(item)}' src="./static/images/site.png"></img>
          </div>
          <div class="listItem imgBoxWidth">
            <img class="lockd" onclick="delLock(this)" data-info='${JSON.stringify(item)}' src="./static/images/delete.png"></img>
          </div>
        </div>
      `;
    }
  };

  html += `</div>`;

  $("#showDeviceListItemListBox").html(html);
};

function locationB(e) {
  let data = e.getAttribute('data-info');
  let info = JSON.parse(data);

  let resData = {
    x: +info.x,
    y: +info.y,
    floor: +info.floor,
  };
  cshowLocationObj(resData);
};

function cshowLocationObj(data) {
  if (data && data.x) {
    needFitView = false; // 不需要全览

    // 设置地图角度
    fMap.setRotation({
      rotation: 0,
      animate: false,
      duration: 0
    });
    // 设置聚焦楼层
    fMap.setLevel({
      level: +data.floor,
    });
    // 设置缩放级别
    fMap.setZoom({
      zoom: 22.5,
      animate: false,
      duration: 0
    })
    // 设置地图中心点
    fMap.setCenter({
      x: +data.x,
      y: +data.y - 2,
    });
    if (locationMarker) {
      locationMarker.remove();
    };

    if (clickMapImgMarker) {
      clickMapImgMarker.remove();
    };

    if (data.type != 2) {
      clickMapImgMarker = new fengmap.FMImageMarker({
        x: +data.x,
        y: +data.y + 0.5,
        url: data.type === 2 ? 'static/images/circle.png' :
          'static/images/location-marker.png',
        size: 30,
        height: 2,
        collision: false
      });
      const floor = fMap.getFloor(+data.floor);
      clickMapImgMarker.addTo(floor);
    }
  }
};

function cshowLocationObj2(newValue) {
  if (newValue && newValue.x) {
    needFitView = false; // 不需要全览

    // 获取地图角度
    // 设置地图中心点偏上方
    let rota = fMap.getRotation();
    let PI = Math.PI;
    let rotaX = 2 * PI / 360 * rota;
    let x = newValue.x - Math.sin(rotaX) * 3;
    let y = newValue.y - Math.cos(rotaX) * 3;

    // 设置聚焦楼层
    fMap.setLevel({
      level: +newValue.floor || +newValue.groupID,
    });

    // 设置地图中心点
    fMap.setCenter({
      x: +x,
      y: +y,
    });

    if (clickMapImgMarker) {
      clickMapImgMarker.remove();
    };

    if (locationMarker) {
      locationMarker.remove();
    };
    locationMarker = new fengmap.FMLocationMarker({
      x: +newValue.x,
      y: +newValue.y,
      url: './static/images/location.png',
      size: 30,
      height: 2,
      level: +newValue.floor || +newValue.groupID,
    });
    locationMarker.addTo(fMap);
  }
};

function changeData(type) {
  var needData = [];
  var sortData = [];

  if (type == 'state') {
    /* 网络状态筛选 */
    locationChecked = false;
    if (showListIndex == 0) {
      /* 信标数据 */
      needData = JSON.parse(JSON.stringify(unBaseStationList));
    } else if (showListIndex == 1) {
      /* 检测器数据 */
      needData = JSON.parse(JSON.stringify(unBeStallList));
    } else if (showListIndex == 2) {
      /* 网关数据 */
      needData = JSON.parse(JSON.stringify(unBstationList));
    } else if (showListIndex == 3) {
      needData = JSON.parse(JSON.stringify(unLockList));
    }
    if (showListIndex == 3) {
      if (netIndex == 0) {
        sortData = needData;
      } else if (netIndex == 1) {
        sortData = needData.filter((item) => item.networkstate == 1);
      } else if (netIndex == 2) {
        sortData = needData.filter((item) => item.networkstate == 0);
      }
    } else {
      if (netIndex == 0) {
        sortData = needData;
      } else if (netIndex == 1) {
        sortData = needData.filter((item) => item.networkName.includes('在线'));
      } else if (netIndex == 2) {
        sortData = needData.filter((item) => item.networkName.includes('离线'));
      } else {
        if (showListIndex == 1) {
          // 检测器数据-低电量-power <= 30;
          sortData = needData.filter((item) => item.power <= 30);
        } else {
          // sortData = needData.filter((item) => item.networkName.includes('低电量'));
          sortData = needData.filter((item) => item.power <= 30);
        }
      };
    }
  } else if (type == 'sort') {
    /* 排序 */
    let n1, n2;
    if (showListIndex == 0) {
      /* 信标数据 */
      needData = sortBaseStationList;
    } else if (showListIndex == 1) {
      /* 检测器数据 */
      needData = sortBeStallList;
    } else if (showListIndex == 2) {
      /* 网关数据 */
      needData = sortBstationList;
    } else if (showListIndex == 3) {
      /* 地锁数据 */
      needData = sortLockList;
    }

    sortData = needData.sort((a, b) => {
      if (showListIndex == 0) {
        n1 = +a.name
        n2 = +b.name
      } else if (showListIndex == 3) {
        n1 = +a.deviceNum
        n2 = +b.deviceNum
      } else {
        n1 = +a.num
        n2 = +b.num
      }
      if (sortIndex === 1) {
        return n1 - n2
      } else {
        return n2 - n1
      }
    });
  } else if (type == 'atype') {
    /* 类型筛选 */
    locationChecked = false;
    if (showListIndex == 0) {
      /* 信标数据 */
      needData = JSON.parse(JSON.stringify(unBaseStationList));
    }
    if (abctypeI == 0) {
      sortData = needData;
    } else if (abctypeI == 1) {
      sortData = needData.filter((item) => item.typeName == '信标');
    } else if (abctypeI == 2) {
      sortData = needData.filter((item) => item.typeName == '道钉');
    }
  };

  if (showListIndex == 0) {
    /* 信标数据 */
    sortBaseStationList = sortData;
    cshowBeacon(sortBaseStationList, 1);
  } else if (showListIndex == 1) {
    /* 检测器数据 */
    sortBeStallList = sortData;
    cshowBeacon(sortBeStallList, 3);
  } else if (showListIndex == 2) {
    /* 网关数据 */
    sortBstationList = sortData;
    cshowBeacon(sortBstationList, 2);
  } else if (showListIndex == 3) {
    /* 地锁 */
    sortLockList = sortData;
    cshowBeacon(sortLockList, 4);
  };

  showDeviceListBox();
};

function refreshList() {
  /* 重置搜索条件 */
  netIndex = 0;
  sortIndex = 0;
  listsearchValue = '';
  locationChecked = false;
  checked = false;

  let index = showListIndex;
  if (index == 0) {
    getBeaconList('list')
  } else if (index == 1) {
    getDetector('list')
  } else if (index == 2) {
    getGatewaySel('list');
  } else if (index == 3) {
    getLockDataList();
  }
};

function lockConfig() {
  uni.navigateTo({
    url: '/pages/lockList/lockList',
  });
};

function radioChange() {
  checked = !checked;
  searchValue = '';

  try {
    document.getElementById('listsearchValue').value = '';
  } catch (error) { };

  if (checked) {
    $("#listsearchValue").attr('placeholder', '请输入车位名')
  } else {
    $("#listsearchValue").attr('placeholder', '请输入检测器编号后6位')
  }
};

function sortList() {
  searchValue = $("#listsearchValue")[0].value;
  var result;
  if (searchValue.trim() === '') {
    // netIndex = 0;
    sortIndex = 0;
    if (showListIndex == 0) {
      sortBaseStationList = unBaseStationList;
    } else if (showListIndex == 1) {
      sortBeStallList = unBeStallList;
    } else if (showListIndex == 2) {
      sortBstationList = unBstationList;
    } else if (showListIndex == 3) {
      sortLockList = unLockList
    };

    changeData('state');
  } else {
    if (showListIndex === 0) {
      result = fuzzyQuery(unBaseStationList, netIndex, searchValue);
      sortBaseStationList = result;
    } else if (showListIndex === 1) {
      if (checked === true) {
        searchValue = searchValue.trim().toUpperCase();
        result = fuzzyQuery(unBeStallList, netIndex, searchValue, 'placeName');
      } else {
        searchValue = searchValue.trim().toUpperCase();
        result = fuzzyQuery(unBeStallList, netIndex, searchValue, 'rawProductId');
      };
      sortBeStallList = result;
    } else if (showListIndex == 2) {
      result = fuzzyQuery(unBstationList, netIndex, searchValue, 'num');
      sortBstationList = result;
    } else if (showListIndex == 3) {
      result = fuzzyQuery(unLockList, netIndex, searchValue, 'deviceNum');
      sortLockList = result;
    };
    sortIndex = 0;

    if (result.length === 0) {
      if (checked === true) {
        showToast('车位号绑定的车位检测器不存在', false, 2000);
      } else {
        showToast('设备不存在', false, 2000);
      }
      return;
    };
  };

  showDeviceListItemListBox();
};

/* 搜索查询数据 */
function fuzzyQuery(list, netIndex, keyword, attribute = 'name') {
  const reg = new RegExp(keyword);
  const arr = [];
  list = netIndex === 0 ? list : list.filter(item => item.networkName === this.netArray[netIndex]);
  for (let i = 0; i < list.length; i++) {
    if (attribute === 'rawProductId') {
      var findSearch = list[i].rawProductId
      if (reg.test(findSearch))
        arr.push(list[i])
    } else {
      if (reg.test(list[i][attribute]))
        arr.push(list[i])
    }
  }
  return arr;
};

/* 全选 */
function allCheckedChange() {
  locationChecked = !locationChecked;
  if (showListIndex == 0) {
    /* 信标 */
    var arr = sortBaseStationList;
    arr.forEach(item => {
      item.checked = locationChecked;
    });
    sortBaseStationList = arr;
  } else if (showListIndex == 1) {
    /* 检测器 */
    var arr = sortBeStallList;
    arr.forEach(item => {
      item.checked = locationChecked;
    });
    sortBeStallList = arr;
  } else if (showListIndex == 2) {
    /* 网关 */
    var arr = sortBstationList;
    arr.forEach(item => {
      item.checked = locationChecked;
    });
    sortBstationList = arr;
  } else if (showListIndex == 3) {
    /* 地锁 */
    var arr = sortLockList;
    arr.forEach(item => {
      item.checked = locationChecked;
    });
    sortLockList = arr;
  };

  showDeviceListItemListBox();

};

/* 全选-定位 */
function setLocate() {
  var arr = [];
  if (showListIndex == 0) {
    arr = sortBaseStationList.filter(item => item.checked === true);
  } else if (showListIndex == 1) {
    arr = sortBeStallList.filter(item => item.checked === true);
  } else if (showListIndex == 2) {
    arr = sortBstationList.filter(item => item.checked === true);
  } else if (showListIndex == 3) {
    arr = sortLockList.filter(item => item.checked === true);
  };
  if (arr.length == 0) {
    showToast('未选择数据', false, 2000)
    return false;
  };
  if (setlocateindex >= arr.length) {
    setlocateindex = 0;
  };
  let data = arr[setlocateindex];
  let poshdata = {
    x: +data.x,
    y: +data.y,
    floor: +data.floor,
    type: 2
  };
  cshowLocationObj(poshdata);
  cshowLocationArr(arr);
  setlocateindex++;
};

function deviceChange2(id) {
  let index = showListIndex;
  let sortArr = [];
  let deviceName = 'num';
  if (index == 0) {
    sortArr = sortBaseStationList;
    deviceName = 'name';
  } else if (index == 1) {
    sortArr = sortBeStallList;
    deviceName = 'num';
  } else if (index == 2) {
    sortArr = sortBstationList;
    deviceName = 'num';
  } else if (index == 3) {
    sortArr = sortLockList;
    deviceName = 'deviceNum'
  };

  for (let i = 0, lenI = sortArr.length; i < lenI; ++i) {
    if (sortArr[i][deviceName] == id) {
      sortArr[i].checked = !sortArr[i].checked;
      break;
    }
  }
};

function deviceChange3(id) {
  for (let i = 0, lenI = sortAlarmsList.length; i < lenI; ++i) {
    if (sortAlarmsList[i]['id'] == id) {
      sortAlarmsList[i].checked = !sortAlarmsList[i].checked;
      break;
    }
  }
};

function delXinbiao(e) {
  let data = e.getAttribute('data-info');
  let info = JSON.parse(data);
  var num = info.num;
  var id = info.id;

  let confirmText = '是否删除编号为' + num + '的信标';
  $.confirm({
    title: '',
    text: confirmText,
    onOK: function () {
      let url = 'wxGateway/delSub/' + id;
      api(url, {}, (res) => {
        if (res.code === 200) {
          showToast(res.message, false, 2000);

          let list1 = delList(sortBaseStationList, num);
          let list2 = delList(unBaseStationList, num);
          if (list1) {
            sortBaseStationList = list1
          };
          if (list2) {
            unBaseStationList = list2
          };

          showDeviceListItemListBox();

        }
      })
    },
    onCancel: function () { }
  });
};

function deljiance(e) {
  let data = e.getAttribute('data-info');
  let info = JSON.parse(data);
  var rawProductId = info.rawProductId;
  var num = info.num;
  var id = info.id;
  let confirmText = '是否删除编号为' + rawProductId + '的检测器';
  $.confirm({
    title: '',
    text: confirmText,
    onOK: function () {
      let url = 'wxInfrared/delInfrared/' + id;
      api(url, {}, (res) => {
        if (res.code === 200) {
          showToast(res.message, false, 2000);

          let list1 = delList(sortBeStallList, num, 'num');
          let list2 = delList(unBeStallList, num, 'num');
          if (list1) {
            sortBeStallList = list1
          };
          if (list2) {
            unBeStallList = list2
          };

          showDeviceListItemListBox();

        }
      })
    },
    onCancel: function () { }
  });
};

function delwangguan(e) {
  let data = e.getAttribute('data-info');
  let info = JSON.parse(data);
  var num = info.num;
  var id = info.id;
  let confirmText = '是否删除编号为' + num + '的网关';
  $.confirm({
    title: '',
    text: confirmText,
    onOK: function () {
      let url = 'wxGateway/delGateway/' + id;
      api(url, {}, (res) => {
        if (res.code === 200) {
          showToast(res.message, false, 2000);

          let list1 = delList(sortBstationList, num, 'num');
          let list2 = delList(unBstationList, num, 'num');
          if (list1) {
            sortBstationList = list1
          };
          if (list2) {
            unBstationList = list2
          };

          showDeviceListItemListBox();

        }
      })
    },
    onCancel: function () { }
  });
};

function delLock(e) {
  let data = e.getAttribute('data-info');
  let info = JSON.parse(data);
  var num = info.deviceNum;
  var id = info.id;
  let confirmText = '是否删除编号为' + num + '的地锁';
  $.confirm({
    title: '',
    text: confirmText,
    onOK: function () {
      let url = 'wxSmallAPP/delFloorLockById/' + id;
      api(url, {}, (res) => {
        if (res.code === 200) {
          showToast(res.message, false, 2000);

          let list1 = delList(sortLockList, num, 'deviceNum');
          let list2 = delList(unLockList, num, 'deviceNum');
          if (list1) {
            sortLockList = list1
          };
          if (list2) {
            unLockList = list2
          };

          showDeviceListItemListBox();

        }
      }, 'DELETE')
    },
    onCancel: function () { }
  });
};

/* 删除数据 */
function delList(list, num, attribute = 'name') {
  var listN = list.map(item => {
    return item[attribute];
  });
  var a = listN.indexOf(num);
  if (a >= 0) {
    list.splice(listN.indexOf(num), 1);
    return list;
  }
};

function delList2(list, ids, attribute) {
  ids = ids + '';
  let idArr = ids.split(',');
  idArr.forEach((item) => {
    let index = list.findIndex((fitem) => {
      return fitem[attribute] == item
    });
    if (index != -1) {
      // list[index].state = 1
      list.splice(index, 1)
    }
  });
  return list
};

/* 显示巡检 */
function showInspectionListBox() {
  let html = `
    <div class="inspectionView">
      <div class="cancelBtn" onclick="cancelInspec(1)">
        <img src="./static/images/cancel.png" />
      </div>
      <div class="inspectionB">
        <div class="inspectionBT">
          <button onclick="inspection()" id="inspectionID">${currentFunction ? '结束巡检' : '开始巡检'}</button>
          <button onclick="showCurrent()">当前定位</button>
          <button onclick="showInspecList()">未添加信标</button>
          <button onclick="clearBluetooth()">清空</button>
        </div>
        <div class="radioGroup">
          <input type="radio" onclick="radioConfigChange(this)" name="drone" value="1" checked />
          <label>功率</label>

          <input type="radio" onclick="radioConfigChange(this)" name="drone" value="2" />
          <label>一米处rssi</label>
        </div>
        <div class="inputView" id="silb_inputView"> </div>
      </div>
      <div class="info" id="silb_info_box"></div>
      <div class="scroll" id="silb_scroll_box"></div>
    </div>
  `;

  $("#showInspectionListBox").html(html);
  showsilb_inputView();
  showsilb_info_box();
  showsilb_scroll_box();
};

function cancelInspec(c) {
  beaconNum = '';
  beaconDir = '';
  beaconVolt = '';
  bluetoothArr = [];
  if (c && !currentFunction) {
    setMenuListItemImgSrc(0);
  }
  // sendMessage({
  //   todo: 'switchInspection',
  //   flag: true,
  //   threshold: threshold,
  //   map: initMapData.id
  // });
  // if (locationMarker) {
  //   locationMarker.remove();
  // };

  $("#showInspectionListBox").html('');
};

function showsilb_inputView() {
  let html = `
    <text class="inputTip">${fillTxt}</text>
    <input type="text" class="valInput" id="inputValue" value="${inputValue}" />
    <text class="fillTxt ${fillTxt === '目标1米处rssi' ? '' : 'modalHideReplace'}">dbm</text>
    <button class="updateBtn" onclick="updateConfig()">更改</button>
  `;

  $("#silb_inputView").html(html);
};

function showsilb_info_box() {
  let html = `
    <div>定位信标编号：${beaconNum}</div>
    <div>
      <text>距离：${beaconDir}</text>
      <text style="margin-left: 20px;">电压：${beaconVolt}</text>
    </div>
  `;

  $("#silb_info_box").html(html);
};

function showsilb_scroll_box() {
  let html = '';
  let len = bluetoothArr.length;
  for (let i = 0; i < len; i++) {
    let item = bluetoothArr[i];
    html += `
      <div class="col">
        <div class="slaveid">
          <input type="checkbox" onclick="configCheckChange('${item.slaveid}')" ${item.check ? 'checked' : ''}/>
          ${item.slaveid}
        </div>
        <div class="type">${item.type}</div>
        <div class="txPower ${fillTxt === '目标功率' ? '' : 'modalHideReplace'}">功率：${item.txPower}</div>
				<div class="rssi ${fillTxt === '目标1米处rssi' ? '' : 'modalHideReplace'}">1米RSSI：${item.rssi}</div>
				<div class="signal">RSSI：${item.signal}dbm</div>
      </div>
    `;
  };

  $("#silb_scroll_box").html(html);
};

function inspection() {
  if (currentFunction) {
    /* 结束巡检 */
    switchInspection(true);
    collectBeaconList = [];
    return false;
  };

  $.prompt({
    title: '',
    text: '请输入门限',
    input: '2',
    empty: false, // 是否允许为空
    onOK: function (input) {
      //点击确认
      if (isNaN(input)) {
        showToast('请输入正确的数字', true, 3000)
        return false;
      }
      threshold = +input;
      switchInspection(false);
    },
    onCancel: function () {
      //点击取消
    }
  });
};

function switchInspection(type) {
  currentFunction = !type;
  let text = currentFunction ? '结束巡检' : '开始巡检';
  $("#inspectionID").html(text);

  sendMessage({
    todo: 'switchInspection',
    flag: type,
    threshold: threshold,
    map: initMapData.id
  })
};

/* 当前位置-巡检 */
function showCurrent() {
  sendMessage({
    todo: 'showCurrent',
  })
};

function showInspecList() {
  /* 未添加信标 */
  sendMessage({
    todo: 'showNoneInspecList',
  })
};

/* 清空 */
function clearBluetooth() {
  beaconNum = '';
  beaconDir = '';
  beaconVolt = '';
  bluetoothArr = [];
  showsilb_info_box();
  showsilb_scroll_box();
};

function radioConfigChange(e) {
  let val = e.value;

  var value, key, txt;
  if (val == 1) {
    value = 6;
    key = "txPower";
    txt = '目标功率';
  } else if (val == 2) {
    value = -65;
    key = "rssi";
    txt = '目标1米处rssi';
  };
  fillTxt = txt;
  inputValue = value;
  inspectKey = key;

  showsilb_inputView();
  showsilb_scroll_box();
};

function updateConfig() {
  inputValue = $("#inputValue").val();

  if (inputValue === '') {
    showToast('请输入数据', false, 2000);
    return
  };
  if (isNaN(inputValue)) {
    showToast('填写的数值有误，请检查', false, 2000);
    return;
  };
  let cheItem = [];
  let arr = bluetoothArr;
  for (let i = 0; i < arr.length; i++) {
    if (arr[i].check) {
      cheItem.push({
        key: arr[i].key,
        txPower: arr[i].txPower,
        slaveid: arr[i].slaveid,
      });
    }
  }
  if (!cheItem.length) {
    showToast('请选择要修改的信标', true, 2000);
    return;
  };
  let flag = verifyTheData([inspectKey], inputValue);
  if (!flag) {
    return;
  }
  let saveStrArr1 = saveStrArr;
  saveStrArr1.push(inspectKey);
  saveStrArr = saveStrArr1

  reviseBeacon = cheItem;

  $.confirm({
    title: '',
    text: '确定更改',
    onOK: function () {
      sendMessage({
        todo: 'updateConfig',
        data: {
          saveStrArr,
          reviseBeacon,
          key: inspectKey,
          cheLen: cheItem.length,
          inputValue
        }
      })
    },
    onCancel: function () { }
  });
};

// 统一校验数据
function verifyTheData(arr, val) {
  let temp;
  let curVal = +val;
  for (let i = 0; i < arr.length; i++) {
    temp = arr[i];
    //功率
    if (temp == 'txPower') {
      if (curVal > 12 || curVal < 0) {
        showToast('功率可设置的范围为 0-12', false, 2000);
        return false;
      }
    }
    //1米RSSI值(批量校准距离)
    if (temp == 'distance' || temp == 'rssi') {
      if (isNaN(curVal)) {
        showToast('请填写正确1米处信号强度，范围为-128至0', false, 2000);
        return false;
      };
      if (curVal > 0 || curVal < -128) {
        showToast('1米处信号强度的范围为-128至0', false, 2000);
        return false;
      };
    }
  }
  return true;
};

function configCheckChange(id) {
  let itemIndex = bluetoothArr.findIndex((item) => item.slaveid == id);

  bluetoothArr[itemIndex].check = !bluetoothArr[itemIndex].check;
};

function showLocation1(data) {
  cshowLocationObj2(data);
};

function inspectionData(data) {
  cinspectionDataObj(data);
};

function cinspectionDataObj(newValue) {
  if (newValue && newValue.length) {
    newValue.forEach((item) => {
      if (!(item && item.num)) return false;
      let Mindex = showBeaconMarker.findIndex((sitem) => {
        return sitem.num == item.num
      });
      if (Mindex != -1) {
        /* 已在地图上-更新 */
        let hasData = showBeaconMarker[Mindex];
        hasData.beaconT.remove();
        showBeaconMarker.splice(Mindex, 1);

        var color = '139, 69, 19';

        var floor = fMap.getFloor(+item.floor);

        if (item.xunjianPower <= 30) {
          var compositeOptions = {
            height: 0.2,
            depth: false,
            anchor: {
              baseon: 'all',
              anchor: 'CENTER',
            },
            collision: false,
            render: 'billboard',
            layout: {
              style: 'limage-rtext',
              align: 'center'
            },
            image: {
              url: 'static/images/lowBattery.png',
              size: [50, 50],
            },
            text: {
              padding: [0, 0, 0, 0],
              plateStrokeWidth: 1,
              content: {
                textAlign: 'Center',
                lineSpacing: 2,
                fontSize: 20,
                fillColor: item.fillcolor || color,
                strokeColor: item.strokecolor || '255,255,255',
                strokeWidth: 1,
                text: item.name,
              }
            },
            x: +item.x,
            y: +item.y
          };

          var beaconT = new fengmap.FMCompositeMarker(compositeOptions);
          beaconT.addTo(floor);

          showBeaconMarker.push({
            ...item,
            beaconT,
          });
        } else {
          let beaconT = new fengmap.FMTextMarker({
            x: +item.x,
            y: +item.y,
            text: item.name,
            fillColor: item.fillcolor || color,
            strokeColor: item.strokecolor || '255,255,255',
            collision: false
          });
          beaconT.addTo(floor);
          showBeaconMarker.push({
            ...item,
            beaconT,
          });
        }
      }
    })
  }
};

function showInspecListuni(str) {
  let html = `
    <div class="noneInspeclistBox">
      <div class="inspectionFooter">
        <div class="noInspecTitle">未添加的信标</div>
        <div class="noInspectionScroll">
          ${str}
        </div>
        <div class="noInspectionB">
          <button onclick="cancelNoInspec()">关闭页面</button>
          <button class="clearNoInspecBtn" onclick="clearNoInspec()">清空</button>
        </div>
      </div>
    </div>
  `;

  $("#showNoneInspecList").html(html);
};

function cancelNoInspec() {
  $("#showNoneInspecList").html('');
};

function clearNoInspec() {
  sendMessage({
    todo: 'clearNoInspec',
  })
  cancelNoInspec()
};

function unisuccessTip(data) {
  if (data) return;
  let html = `
    <div class="tips">
      <div class="tipsTxt">完成</div>
      <div class="tipsBtn">
        <div class="enter" onclick="hideTip()">确定</div>
      </div>
    </div>
    `;

  $("#unisuccessTip").html(html)
};

function hideTip() {
  sendMessage({
    todo: 'hideTip',
  });
  bluetoothArr = [];
  showsilb_scroll_box();
  $("#unisuccessTip").html('')
};

function unishowFailPop(data) {
  if (data.showFailPop) return;

  let html = `
    <div class="failTips">
			<div class="failTxt">
				<div>失败数量：${data.failLen}</div>
				<div>失败名称：${data.failTxt}</div>
			</div>
			<div class="failClose" onclick="closeFail()">
				<img src="./static/images/close.png" class="failCloseImg"></img>
			</div>
		</div>
    `;

  $("#unishowFailPop").html(html)
};

function closeFail() {
  sendMessage({
    todo: 'closeFail',
  })
  $("#unishowFailPop").html('')
};

function unimodifyTip(data) {
  if (data.modifyTip) {
    let html = `
      <div class="tips">
        <div class="loading">
          <div></div>
          <div></div>
          <div></div>
          <div></div>
          <div></div>
          <div></div>
          <div></div>
          <div></div>
        </div>
        <div class="loadTxt">
          <div>已完成</div>
          <div class="modify">${data.modify}</div>
          <div>/${data.cheLen}</div>
        </div>
        <div class="tipsBtn">
          <div class="enter" onclick="stopChangeC()" id="stopChangeC">停止</div>
        </div>
      </div>
    `;

    $("#unimodifyTip").html(html)
  } else {
    $("#unimodifyTip").html('')
  }
};

function stopChangeC() {
  $("#unimodifyTip").html('停止中');

  sendMessage({
    todo: 'stopChangeC',
  })
};


var networkTypeText = '系统繁忙'
function monitorNetworkStatus(data) {
  if (data) {
    let hData = JSON.parse(localStorage.getItem('delAlarmsData')) || [];
    if (hData) {
      hData.forEach((item) => {
        trueDelAlarms2(item.ids, item.desc, false)
      })
    };
    networkTypeText = '系统繁忙'
  } else {
    networkTypeText = '网络异常'
  }
};

var alarmsDevice = [];
var alarmsTypes = [];
var alarmsTypes2 = [];
var sortAlarmsList = [];
var unAlarmsList = [];
var alarmsList = [];
var alarmsLevels = [{
  id: 0,
  txt: '全部'
}, {
  id: 1,
  txt: '高'
}, {
  id: 2,
  txt: '中'
}, {
  id: 3,
  txt: '低'
}];
var alarmsState = [{
  id: '3',
  txt: '全部'
}, {
  id: '0',
  txt: '报警中'
}, {
  id: '1',
  txt: '已结束'
}];
function getEWarnList(type) {
  alarmsDevice = [{
    id: '-1',
    txt: '全部'
  }];
  alarmsTypes = [{
    cid: '-1',
    c: [{
      id: '-1',
      txt: '全部'
    }]
  }];
  alarmsTypes2 = [{
    id: '-1',
    txt: '全部'
  }];
  api('WxMiniApp/getDeviceAlarmsTypeConfig', {}, (res) => {
    let data = [];
    if (res.data) {
      data = res.data
    };

    data.forEach((item) => {
      let had = alarmsDevice.some((sitem) => {
        return sitem.id == item.deviceTypeId
      });
      if (!had) {
        alarmsDevice.push({
          id: item.deviceTypeId,
          txt: item.deviceName
        })
      };

      let had2 = alarmsTypes2.some((sitem) => {
        return sitem.id == item.alarmsTypeId
      });

      if (!had2) {
        alarmsTypes2.push({
          id: item.alarmsTypeId,
          txt: item.alarmName
        })
      }


      let index = alarmsTypes.findIndex((sitem) => {
        return sitem.cid == item.deviceTypeId
      });
      if (index != -1) {
        let c = alarmsTypes[index].c;
        let had1 = c.some(citem => {
          return citem.id == item.alarmsTypeId
        });
        if (!had1) {
          c.push({
            id: item.alarmsTypeId,
            txt: item.alarmName
          });
          alarmsTypes[index].c = c
        }
      } else {
        let c = {
          id: item.alarmsTypeId,
          txt: item.alarmName
        };
        alarmsTypes.push({
          cid: item.deviceTypeId,
          c: [c]
        })
      }
    });

    getEWarnList2(type)
  })
};

function getEWarnList2(type) {
  api('deviceAlarms/getDeviceAlarmsData', {
    map: initMapData.id,
    pageSize: -1,
    state: 0
  }, (res) => {
    const list = res.data;
    sortAlarmsList = list;
    unAlarmsList = list;
    alarmsList = list;

    if (type == 'list') {
      ewDevice = 1;
      locationChecked1 = false;
      checked1 = false;
      changeAlarmsData();

      showEWarnListBox();
      cshowBeacon(sortAlarmsList, 5);
    };
  })
};

function showEWarnListBox() {
  let html = `
    <div class="deviceListView">
      <div class="searchContainer">
        <div class="cancelBtn" onclick="cancelSearch()">
          <img src="./static/images/cancel.png"></img>
        </div>
        <div class="toolbar2">
          <div class="toobarTop" style="justify-content:space-between">
            <input class="picker devicePicker" readonly id="alarmsDevice" type="text" value="${alarmsDevice[ewDevice].txt}">
            <input class="picker devicePicker" readonly id="alarmsTypes" type="text" value="全部">
            <input class="picker" readonly id="alarmsLevels" type="text" value="${alarmsLevels[ewLevels].txt}">
            <!--// <input class="picker" readonly id="alarmsState" type="text" value="${alarmsState[ewState].txt}">-->
            <div class="toolIBtn" onclick="refreshList1()">
              <img class="refreshBtn" src="./static/images/refresh.png"></img>
            </div>
          </div>
          <div class="toolbarMiddle">
            <div class="icsbox">
              <input class="radio-group" type="checkbox" onclick="radioChange1()" ${checked1 ? 'checked' : ''}/>
              <span class="cheRadio">车位名</span>
            </div>
            <input class="searchInput searchCheInput" type="text"
              placeholder="${checked === true ? '请输入车位名' : '请输入设备编号'}"
              id="listsearchValue" style="flex:1" />
            <div class="toolBtn" onclick="sortList1()">搜索</div>
          </div>
          <div class="toolbarBottom">
            <div class="span1">
              <span class="lh0">数量:</span>
              <span class="showDataListText0"></span>
            </div>
            <div class="colorBox">
             <!-- // <span class="colorBox1"></span><span>已结束</span>-->
              <span class="colorBox2"></span><span>高报警</span>
              <span class="colorBox3"></span><span>中报警</span>
              <span class="colorBox4"></span><span>低报警</span>
            </div>
            <div class="toolbarBottomR">
              <input class="radio-group" type="checkbox" onclick="allCheckedChange1()" ${locationChecked1 ? 'checked' : ''}/>
              <div class="toolBtn" onclick="liftAlarms()" style="margin-left:1.3333vw">解除</div>
            </div>
          </div>
        </div>
        <div id="showDeviceListItemListBox"></div>
      </div>
    </div>
  `;

  $("#showDeviceListBox").html(html);

  let alaChangeV1;
  $("#alarmsDevice").picker({
    title: '请选择',
    cols: [{
      textAlign: 'center',
      values: alarmsDevice.map((item) => {
        return item.txt
      })
    }],
    onChange: function (p, v, dv) {
      alaChangeV1 = v[0]
    },
    onClose: function (p, v, d) {
      if (alaChangeV1) {
        let index = alarmsDevice.findIndex((item) => {
          return item.txt == alaChangeV1;
        });
        if (ewDevice == index) return;
        ewDevice = index;

        locationChecked1 = false;
        checked1 = false;

        changeAlarmsData();
      }
    }
  });

  setPickerV2();

  let alaChangeV3;
  $("#alarmsLevels").picker({
    title: '请选择',
    cols: [{
      textAlign: 'center',
      values: alarmsLevels.map((item) => {
        return item.txt
      })
    }],
    onChange: function (p, v, dv) {
      alaChangeV3 = v[0]
    },
    onClose: function (p, v, d) {
      if (alaChangeV3) {
        let index = alarmsLevels.findIndex((item) => {
          return item.txt == alaChangeV3;
        });
        if (ewLevels == index) return;
        ewLevels = index;

        locationChecked1 = false;
        checked1 = false;

        changeAlarmsData();
      }
    }
  });

  let alaChangeV4;
  $("#alarmsState").picker({
    title: '请选择',
    cols: [{
      textAlign: 'center',
      values: alarmsState.map((item) => {
        return item.txt
      })
    }],
    onChange: function (p, v, dv) {
      alaChangeV4 = v[0]
    },
    onClose: function (p, v, d) {
      if (alaChangeV4) {
        let index = alarmsState.findIndex((item) => {
          return item.txt == alaChangeV4;
        });
        if (ewState == index) return;
        ewState = index;

        locationChecked1 = false;
        checked1 = false;

        changeAlarmsData();
      }
    }
  });

  showDeviceListItemListBox1();
};

function setPickerV2() {
  let alaChangeV2;
  $("#alarmsTypes").picker({
    title: '请选择',
    cols: [{
      textAlign: 'center',
      values: alarmsTypes2.map((item) => {
        return item.txt
      })
    }],
    onChange: function (p, v, dv) {
      alaChangeV2 = v[0]
    },
    onClose: function (p, v, d) {
      if (alaChangeV2) {
        let index = alarmsTypes2.findIndex((item) => {
          return item.txt == alaChangeV2;
        });
        if (ewType == index) return;
        ewType = index;

        locationChecked1 = false;
        checked1 = false;

        changeAlarmsData();
      }
    }
  });
};

function showDeviceListItemListBox1() {
  let data = sortAlarmsList;
  let html = '';
  // <div class="listItem">级别</div>
  // <div class="listItem">状态</div>
  // <div class="listItem">车位</div>


  // <div class="listItem">
  //   ${item.placeName ? item.placeName : ''}
  // </div>
  // <div class="listItem">
  //   ${getAlarmsName('priority', item.priority)}
  // </div>
  // <div class="listItem">
  //   ${getAlarmsName('state', item.state)}
  // </div>
  $(".showDataListText0").html(data.length);
  html += `
      <div class="listH">
        <div class="listItemNum" style="flex:2">编号</div>
        <div class="listItem" style="margin-right:4px;flex:4">类别</div>
        <div class="listItem">类型</div>
        
        <div class="listItem imgBoxWidth">定位</div>
        <div class="listItem imgBoxWidth">解除</div>
      </div>
      <div class="searchScroll">
    `;

  let len = data.length;
  for (let i = 0; i < len; i++) {
    let item = data[i];
    html += `
        <div class="listContent">
          <div class="listItemNum"  style="flex:2"> 
            <input type="checkbox" onclick="deviceChange3('${item.id}')" ${item.checked ? 'checked' : ''}/>
            <div>${item.num ? item.num : (item.placeName ? item.placeName : '摄像')}</div>
          </div>
          <div class="listItem" style="margin-right:4px;flex:4">
            ${getAlarmsName('equipmentType', item.equipmentType)}
          </div>
          
          <div class="listItem ${item.state == 1 ? 'state1' : ('priority' + item.priority)}">
            ${getAlarmsName('alarmType', item.alarmType)}
          </div>
          
          <div class="listItem imgBoxWidth">
            <img class="lockloca" onclick="locationB(this)" data-info='${JSON.stringify(item)}' src="./static/images/site.png"></img>
          </div>
          <div class="listItem imgBoxWidth">
            <img class="lockd" onclick="delAlarms(this)" data-info='${JSON.stringify(item)}' src="./static/images/delete.png"></img>
          </div>
        </div>
      `;
  }

  html += `</div>`;

  $("#showDeviceListItemListBox").html(html);
};

function allCheckedChange1() {
  locationChecked1 = !locationChecked1;

  var arr = sortAlarmsList;
  arr.forEach(item => {
    item.checked = locationChecked1;
  });
  sortAlarmsList = arr;

  showDeviceListItemListBox1();
};

function radioChange1() {
  checked1 = !checked1;
  searchValue = '';

  try {
    document.getElementById('listsearchValue').value = '';
  } catch (error) { };

  if (checked1) {
    $("#listsearchValue").attr('placeholder', '请输入车位名')
  } else {
    $("#listsearchValue").attr('placeholder', '请输入检测器编号后6位')
  }
};


function changeAlarmsData(name, key) {
  var needData = JSON.parse(JSON.stringify(unAlarmsList));

  var filter1 = [];
  var filter2 = [];
  var filter3 = [];
  var filter4 = [];

  if (ewDevice == 0) {
    filter1 = needData;
  } else {
    let id = alarmsDevice[ewDevice].id;
    filter1 = needData.filter((item) => {
      return item.equipmentType == id
    })
  };

  if (ewType == 0) {
    filter2 = filter1
  } else {
    let id = alarmsTypes2[ewType].id;
    filter2 = filter1.filter((item) => {
      return item.alarmType == id
    })
  };

  if (ewLevels == 0) {
    filter3 = filter2
  } else {
    let id = alarmsLevels[ewLevels].id;
    filter3 = filter2.filter((item) => {
      return item.priority == id
    })
  };

  if (ewState == 0) {
    filter4 = filter3
  } else {
    let id = alarmsState[ewState].id;
    filter4 = filter3.filter((item) => {
      return item.state == id
    })
  };

  if (name && key) {
    const reg = new RegExp(key);
    let arr = [];
    let list = filter4;

    for (let i = 0; i < list.length; i++) {
      if (reg.test(list[i][name])) {
        arr.push(list[i])
      }
    }
    return arr;

  } else {
    sortAlarmsList = filter4;
    cshowBeacon(sortAlarmsList, 5);

    showDeviceListItemListBox1();
  }
};

function sortList1() {
  searchValue = $("#listsearchValue")[0].value;
  var result;
  if (searchValue.trim() === '') {
    changeAlarmsData();
    return;
  } else {
    if (checked1 === true) {
      searchValue = searchValue.trim().toUpperCase();
      result = changeAlarmsData('placeName', searchValue)
    } else {
      searchValue = searchValue.trim().toUpperCase();
      result = changeAlarmsData('num', searchValue)
    };
    sortAlarmsList = result;

    if (result.length === 0) {
      if (checked1 === true) {
        showToast('该车位号报警不存在', false, 2000);
      } else {
        showToast('该设备报警不存在', false, 2000);
      }
      return;
    };
  };
  cshowBeacon(sortAlarmsList, 5);

  showDeviceListItemListBox1();
};

function getAlarmsName(name, id) {
  let data;
  if (name == 'equipmentType') {
    data = alarmsDevice;
  } else if (name == 'alarmType') {
    data = alarmsTypes2;
  } else if (name == 'priority') {
    data = alarmsLevels;
  } else if (name == 'state') {
    data = alarmsState;
  };

  let target = data.find((item) => {
    return item.id == id
  });
  if (target && target.txt) {
    if (name == 'alarmType') {
      let a = target.txt.slice(0, -2)
      return a
    }
    return target.txt;
  } else {
    return ''
  }
};

function delAlarms(e) {
  let data = e.getAttribute('data-info');
  let info = JSON.parse(data);

  var num = info.num;
  var placeName = info.placeName;
  var id = info.id;
  let confirmText = '解除编号为' + (num || placeName || '摄像头') + '的报警备注';

  $.prompt({
    text: confirmText,
    input: '',
    empty: false,
    onOK: function (input) {
      //点击确认
      trueDelAlarms(id, input)
    },
    onCancel: function () {
      //点击取消
    }
  });
};

function trueDelAlarms(id, text, tips = true) {
  let hData = JSON.parse(localStorage.getItem('delAlarmsData')) || [];
  if (hData && hData.length) {
    hData.forEach((item) => {
      trueDelAlarms2(item.ids, item.desc, false)
    })
  };
  let hData2 = JSON.parse(JSON.stringify(hData))
  hData2.push({
    ids: id,
    desc: text
  });
  localStorage.setItem('delAlarmsData', JSON.stringify(hData2));

  trueDelAlarms2(id, text, tips)
};

function trueDelAlarms2(id, text, tips = true) {
  api('WxMiniApp/updateDeviceAlarms', {
    ids: id,
    desc: text
  }, (res) => {
    if (res.code === 200) {
      let list1 = delList2(sortAlarmsList, id, 'id');
      let list2 = delList2(unAlarmsList, id, 'id');
      if (list1) {
        sortAlarmsList = list1
      };
      if (list2) {
        unAlarmsList = list2
      };
      localStorage.setItem('delAlarmsData', JSON.stringify([]));

      if (tips) {
        showToast(res.message, false, 2000);
      };
      cshowBeacon(sortAlarmsList, 5);
      showDeviceListItemListBox1();
    }
  }, 'GET')
};

function liftAlarms() {
  var arr = [];
  arr = sortAlarmsList.filter(item => item.checked === true);

  if (arr.length == 0) {
    showToast('未选择数据', false, 2000)
    return false;
  };

  let idArr = arr.map((item) => {
    return item.id
  });

  let ids = idArr.join(',');

  $.prompt({
    text: '批量解除报警备注',
    input: '',
    empty: false,
    onOK: function (input) {
      //点击确认
      trueDelAlarms(ids, input);
    },
    onCancel: function () {
      //点击取消
    }
  });
};

function refreshList1() {
  ewType = 0;
  ewLevels = 0;
  ewState = 0;
  ewDevice = 0;

  getEWarnList('list');
};