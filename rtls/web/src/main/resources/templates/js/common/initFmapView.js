var fMap = null;//fmap变量
var fmapID;//定义地图ID变量
// 导航对象
var navi = null;
//定义选中模型
var selectedModel = null;
var mapLoad = false;//是否加载成功
//初始化
function openMap(params, callback, errorCallbak) {
  fmapID = params.fId;
  var focusFloor = params.focusFloor || 1;
  // var defaultParams = {
  //   container: document.getElementById('fengmap'),
  //   appName: params.fName,
  //   key: params.fKey,
  //   //支持单击模型高亮，false为单击时模型不高亮
  //   modelSelectedEffect: false,
  //   defaultMapScale: 2012,
  //   //楼层高度
  //   defaultGroupSpace: 30,
  //   defaultViewMode: fengmap.FMViewMode.MODE_2D,
  //   //地图数据位置
  //   /*mapServerURL: url + params.path + '/',
  //   //主题数据位置
  //   mapThemeURL: url + 'theme/',*/
  //   // defaultThemeName:'4006',
  //   defaultThemeName: params.themeName || 'tgymap',

  //   //默认聚焦楼层
  //   defaultFocusGroup: focusFloor,
  //   //初始显示楼层ID数组
  //   defaultVisibleGroups: [focusFloor],
  // }
  console.log('params', params);

  var defaultParams = {
    container: document.getElementById('fengmap'),
    appName: params.fName,
    key: params.fKey,
    mapID: params.fId,
    level: +focusFloor,
    mapZoom: 18,
    zoomRange: [12, 23],
    floorSpace: 80,
    tiltAngle: 70,
    viewMode: fengmap.FMViewMode.MODE_2D,
    // 设置地图背景颜色
    backgroundColor: '#153160',
    themeID: params.themeName || 'tgymap',
  };

  // $.extend(defaultParams, params);
  //初始化地图对象
  if (fMap) {
    //销毁
    fMap.dispose();
    fMap = null;
    mapLoad = false;
  }

  fMap = new fengmap.FMMap(defaultParams);


  //打开Fengmap服务器的地图数据和主题
  // fMap.openMapById(fmapID, function (error) {
  //   //打印错误信息
  //   console.log('Fmap地图加载出错', error);
  //   tips('地图加载出错，请重试');
  if (typeof errorCallbak === 'function') {
    errorCallbak();
  }
  // });

  //地图加载完成事件
  fMap.on('loaded', function () {
    mapLoad = true;

    if (params.mapId) {
      getAllDataToMap(params.mapId)
      /* 隐藏文字 */
      setTimeout(() => {
        /* 图层控制 */
        layerControl()
      }, 16);
    };
    //加载滚动型楼层切换控件
    loadScrollFloorCtrl(params.floorTop);

    // if (typeof fengmap.FMNavigation === 'function') {
    //   //需引入navi.min
    //   navi = new fengmap.FMNavigation({
    //     map: fMap,
    //   })
    // }
    //初始化指南针
    loadCompass();

    if (typeof callback === 'function') {
      callback();
    }
  });
  //地图点击事件，需要在地图加载完成之后操作
  fMap.on('click', function (event) {
    //地图模型
    var target = event.targets[0];
    console.log('click', target);

    if (!target) {
      return;
    }
    var buttonType = event.mouseEvent.button;
    if (buttonType === 2) {
      return;
    };

    if (!target.visible) {
      return
    };
    var coord = null;
    if (typeof mapClickFn == 'function') {
      coord = event.coords;
      mapClickFn({
        fid: target.FID,
        x: target.x,
        y: target.y,
        z: target.z || 0.2,
        floor: target.level,
        //模型的中点
        cx: coord.x,
        cy: coord.y,
        nodeType: target.nodeType || '',
        //自定义属性(没有则不用管)
        selfId: target.selfId || '',
        selfType: target.selfType || '',
      }, target);
    }
  });
}
//加载滚动型楼层切换控件
function loadScrollFloorCtrl(y) {
  // if (!fengmap.FMScrollGroupsControl) {
  //   console.log('需引入control.js的文件才能初始化切换楼层');
  //   return;
  // }
  // var scrollFloorCtlOpt = {
  //   //默认在右上角
  //   position: fengmap.FMControlPosition.RIGHT_TOP,
  //   showBtnCount: 3,
  //   allLayer: false,
  //   needAllLayerBtn: true,
  //   offset: {
  //     x: -20,
  //     y: y || 72
  //   },
  //   imgURL: '../image/fMap/',
  // };
  // new fengmap.FMScrollGroupsControl(fMap, scrollFloorCtlOpt);

  // 3.1.3
  if ($('.fm-control-groups').length < 1) {
    var scrollFloorCtlOpt = {
      position: fengmap.FMControlPosition.RIGHT_TOP,
      floorButtonCount: 3,
      offset: {
        x: -24,
        y: 100
      },
      viewModeControl: false,
      floorModeControl: true,
      needAllLayerBtn: true
    };
    scrollFloorControl = new fengmap.FMToolbar(scrollFloorCtlOpt);
    scrollFloorControl.addTo(fMap);
  };
}

/**
 * 二维/三维模式切换
 * fengmap.FMViewMode FMViewMode 模型视图模式枚举
 * https://developer.fengmap.com/docs/js/v2.7.1/fengmap.FMViewMode.html
 * */
function changeMode(domObj) {
  if (!mapLoad) {
    tips('地图加载失败，请重试');
    return;
  }
  if (fMap.getViewMode() == 2) {
    //切换地图为二维模式
    // fMap.viewMode = fengmap.FMViewMode.MODE_2D;
    fMap.setViewMode({
      mode: fengmap.FMViewMode.MODE_2D
    })

    $(domObj).removeClass('viewMode3D');
    $(domObj).addClass('viewMode');
  } else {
    //切换地图为三维模式
    // fMap.viewMode = fengmap.FMViewMode.MODE_3D;
    fMap.setViewMode({
      mode: fengmap.FMViewMode.MODE_3D
    })
    $(domObj).addClass('viewMode3D');
  }
}


//初始化指南针
function loadCompass() {
  var scrollCompassCtlOpt = {
    position: fengmap.FMControlPosition.Left_TOP,
    offset: {
      x: 24,
      y: 50
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
        //console.log('setRotation'); 
      }
    })
  });
}

// 设置模型颜色
function setPlaceColor(data, color) {
  var levels = fMap.getLevels();
  if (data.length) {
    for (var i = 0; i < levels.length; i++) {
      var floor = fMap.getFloor(levels[i]);
      var layers = floor.getLayers(fengmap.FMType.MODEL_LAYER)[0];
      data.forEach((fid) => {
        const model = layers.getFeatures().find(item => item.FID === fid);
        if (model) {
          if (color != '') {
            model.setColor(color, 1);
            model.setBorderColor(color, 1)
          } else {
            model.resetColor();
            model.resetBorderColor();
          }
        }
      })
    }
  }
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
};
function getLayerByType2(floor, layerType) {
  var layers = floor.getLayers();
  for (var index = 0; index < layers.length; index++) {
    const layer = layers[index];
    if (layer.type === layerType) {
      return layer
    }
  }
};

function getAllDataToMap(mapId) {
  /* 车位 */
  getPlaceData(mapId);

  /* 跨层通道 */
  crossLayerChannel(mapId);

  /* 电梯数据 */
  getApiElevatorData(mapId);

  /* 厕所-安全出口-建筑 */
  getApiServiceFacilitiesData(mapId);

  /* 商家 */
  getShangJiaData(mapId);

  /* 公司 */
  getCompanyData(mapId);

  /* 出入口 */
  getPlaceExit(mapId);

  /* vip区域 */
  getVipArea(mapId);
};

var placeExitData = []
function getPlaceExit(mapid) {
  $.ajax({
    url: `${url}park/getPlaceExit`,
    data: {
      map: mapid,
    },
    type: 'POST',
    success: function (res) {
      const data = res.data;
      if (!data || data.length == 0) {
        return;
      }
      placeExitData = data;
      setPlaceExitMarker();
    }
  })
};

function setModelColor6(model, item) {
  model.setColor('#CFEEDE', 1);
  model.setBorderColor('#ACDAC2', 1);
};

function setModelImage6(data, item) {
  if (!data.floor) return;
  /* 设置图标 */
  let iconUrl = '';
  if (item.type != 3) {
    iconUrl = '../image/mapIcon/Exit1.png';
  } else {
    iconUrl = '../image/mapIcon/Exit2.png';
  }
  var floor = fMap.getFloor(+item.floor);

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
      url: iconUrl, // 图片url地址
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
  compositeMarker.addTo(floor);

};

function setPlaceExitMarker() {
  if (placeExitData && placeExitData.length) {
    placeExitData.forEach(item => {
      if (item.fid) {
        let model = getMapModel(item.fid);
        if (model) {
          setModelColor6(model, item)
          let data = {
            x: model.bound.center.x,
            y: model.bound.center.y,
            floor: item.floor,
            name: item.name
          };
          setModelImage6(data, item)
        } else {
          let data = {
            x: item.x,
            y: item.y,
            floor: item.floor,
            name: item.name
          };
          setModelImage6(data, item)
        }
      } else {
        let data = {
          x: item.x,
          y: item.y,
          floor: item.floor,
          name: item.name
        };
        setModelImage6(data, item)
      }
    });
  }
};

var placeDataList = [];
/* 显示后台车位名字 */
function getPlaceData(mapId) {
  $.ajax({
    url: url + 'park/getPlace',
    data: {
      map: mapId,
      pageSize: -1
    },
    success: function (res) {
      var data = res.data || [];
      var len = data.length;
      if (res.code != 200 || !len) {
        return;
      };
      placeDataList = data;
      setPlaceDataToMap();

    },
    error: function (jqXHR) {
      resError(jqXHR);
    }
  })
};

/* 设置车位信息到地图上 */
function setPlaceDataToMap() {
  if (placeDataList && placeDataList.length) {
    placeDataList.forEach(item => {
      if (item.fid) {
        let model = getMapModel(item.fid);
        if (model) {
          setModelColor(model, item)
          let data = {
            x: model.bound.center.x,
            y: model.bound.center.y,
            floor: item.floor,
            name: item.name
          };
          setPModelName(data, item)
        } else {
          let data = {
            x: item.x,
            y: item.y,
            floor: item.floor,
            name: item.name
          };
          setPModelName(data, item)
        }
      } else {
        let data = {
          x: item.x,
          y: item.y,
          floor: item.floor,
          name: item.name
        };
        setPModelName(data, item)
      }
    });
  }
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

function setModelColor(model, item) {
  /* 设置模型颜色 */
  if (item.state == '0') {
    model.setColor('#83F58D', 1);
    model.setBorderColor('#45833B', 1);
  } else if (item.state == '1') {
    model.setColor('#FDBBC5', 1);
    model.setBorderColor('#BA646E', 1);
  } else if (item.state == '3') {
    model.setColor('#C2C2C2', 1);
    model.setBorderColor('#474545', 1);
  } else {
    model.resetColor();
    model.resetBorderColor();
  };
};

function setPModelName(data, item) {
  if (!data.floor) return;

  let iconUrl = '';
  if (item.type == 1) {
    /* 充电车位 */
    iconUrl = '../image/mapIcon/placeType1.png'
  } else if (item.type == 2) {
    /* 专属车位 */
    iconUrl = '../image/mapIcon/placeType2.png'
  } else if (item.type == 3) {
    /* 无障碍车位 */
    iconUrl = '../image/mapIcon/placeType3.png'
  };

  if (iconUrl) {
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
        url: iconUrl, // 图片url地址
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
    let floor = fMap.getFloor(+data.floor);
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
    let floor = fMap.getFloor(+data.floor);
    textMarker.addTo(floor)
  }
};

var crossLayerData = [];
function crossLayerChannel(mapId) {
  $.ajax({
    url: `${url}crossLevelCorridor/getConditionalQuery2?map=${mapId}`,

    success: function (res) {
      const data = res.data;
      if (!data || data.length == 0) {
        return;
      }
      crossLayerData = data;
      setCrossLayerChannel();
    }
  })
};

function setCrossLayerChannel() {
  if (crossLayerData && crossLayerData.length) {
    crossLayerData.forEach(item => {
      if (item.fid) {
        let model = getMapModel(item.fid);
        if (model) {
          setModelColor3(model, item)
          let data = {
            x: model.bound.center.x,
            y: model.bound.center.y,
            floor: item.floor,
            name: item.name
          };
          setModelImage1(data, item);
        } else {
          let data = {
            x: item.x,
            y: item.y,
            floor: item.floor,
            name: item.name
          };
          setModelImage1(data, item);
        }
      } else {
        let data = {
          x: item.x,
          y: item.y,
          floor: item.floor,
          name: item.name
        };
        setModelImage1(data, item);
      }
    });
  }
};

function setModelColor3(model, item) {
  model.setColor('#E3F1FF', 1);
  model.setBorderColor('#597CBE', 1);
};

function setModelImage1(data, item) {
  if (!data.floor) return;
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
      url: '../image/mapIcon/Exit3.png', // 图片url地址
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
  let floor = fMap.getFloor(+data.floor);
  compositeMarker.addTo(floor);
};

var elevatorData = []
function getApiElevatorData(mapId) {
  $.ajax({
    url: `${url}peb/getParkingElevatorBinding?map=${mapId}&pageSize=-1`,
    success: function (res) {
      const data = res.data;
      if (!data || data.length == 0) {
        return;
      }
      elevatorData = data;
      setElevatorDataToMap();
    }
  })
};

function setElevatorDataToMap() {
  if (elevatorData && elevatorData.length) {
    elevatorData.forEach(item => {
      if (item.fid) {
        let model = getMapModel(item.fid);
        if (model) {
          setModelColor2(model, item)
          let data = {
            x: model.bound.center.x,
            y: model.bound.center.y,
            floor: item.floor,
            name: item.name
          };
          setModelImage(data, item);
        } else {
          let data = {
            x: item.x,
            y: item.y,
            floor: item.floor,
            name: item.name
          };
          setModelImage(data, item);
        }
      } else {
        let data = {
          x: item.x,
          y: item.y,
          floor: item.floor,
          name: item.name
        };
        setModelImage(data, item);
      }
    });
  }
};

function setModelColor2(model, item) {
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
};

function setModelImage(data, item) {
  if (!data.floor) return;

  /* 设置图标 */
  let iconUrl = '';
  if (item.iconType == '170001') {
    /* 步行梯 */
    iconUrl = '../image/mapIcon/Exit5.png'
  } else if (item.iconType == '170003') {
    /* 手扶电梯 */
    iconUrl = '../image/mapIcon/Exit7.png'
  } else if (item.iconType == '170006') {
    /* 电梯前室 */
    iconUrl = '../image/mapIcon/Exit6.png'
  };
  if (iconUrl) {
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
        url: iconUrl, // 图片url地址
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
    let floor = fMap.getFloor(+data.floor);
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
    let floor = fMap.getFloor(+data.floor);
    textMarker.addTo(floor)
  }
};

var serviceFacilitiesData = [];
function getApiServiceFacilitiesData(mapId) {
  $.ajax({
    url: `${url}mapBuild/getMapBuild2?map=${mapId}&pageSize=-1`,
    success: function (res) {
      const data = res.data;
      if (!data || data.length == 0) {
        return;
      }
      serviceFacilitiesData = data;
      setServiceFacilitiesData();
    }
  })
};

function setServiceFacilitiesData() {
  if (serviceFacilitiesData && serviceFacilitiesData.length) {
    serviceFacilitiesData.forEach(item => {
      if (item.fid) {
        let model = getMapModel(item.fid);
        if (model) {
          setModelColor4(model, item)
          let data = {
            x: model.bound.center.x,
            y: model.bound.center.y,
            floor: item.floor,
            name: item.name
          };
          setModelImage2(data, item);
        } else {
          let data = {
            x: item.x,
            y: item.y,
            floor: item.floor,
            name: item.name
          };
          setModelImage2(data, item);
        }
      } else {
        let data = {
          x: item.x,
          y: item.y,
          floor: item.floor,
          name: item.name
        };
        setModelImage2(data, item);
      }
    });
  }
};

function setModelColor4(model, item) {
  /* 设置模型颜色 */
  if (item.objectType == '340873') {
    /* 一类建筑 */
    model.setColor('#8EABF0', 1);
    model.setBorderColor('#486FCF', 1);
  } else if (item.objectType == '340874') {
    /* 二类建筑 */
    model.setColor('#BDDBFE', 1);
    model.setBorderColor('#597CBE', 1);
  } else if (item.objectType == '340875') {
    /* 三类建筑 */
    model.setColor('#D4F6FD', 1);
    model.setBorderColor('#597CBE', 1);
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
};

function setModelImage2(data, item) {
  if (!data.floor) return;
  /* 设置图标 */
  let iconUrl = '';

  if (item.iconType == '100004') {
    /* 男洗手间 */
    iconUrl = '../image/mapIcon/icon_wc_1.png'
  } else if (item.iconType == '100005') {
    /* 女洗手间 */
    iconUrl = '../image/mapIcon/icon_wc_2.png'
  } else if (item.iconType == '100003') {
    /* 母婴室 */
    iconUrl = '../image/mapIcon/icon_wc_4.png'
  } else if (item.iconType == '800008') {
    /* 护士站 */
    iconUrl = '../image/mapIcon/icon_wc_5.png'
  } else if (item.iconType == '110002') {
    /* 安全出口 */
    iconUrl = '../image/mapIcon/Exit4.png'
  };

  if (iconUrl) {
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
        url: iconUrl, // 图片url地址
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
    let floor = fMap.getFloor(+data.floor);
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
    let floor = fMap.getFloor(+data.floor);
    textMarker.addTo(floor)
  }
};

var shangjiaDataList = [];
function getShangJiaData(mapId) {
  $.ajax({
    url: url + 'park/getShangjia',
    data: {
      map: mapId,
      pageSize: -1
    },
    success: function (res) {
      var data = res.data || [];
      var len = data.length;
      if (res.code != 200 || !len) {
        return;
      };
      shangjiaDataList = data;
      setShangJiaDataToMap();
    },
    error: function (jqXHR) {
      resError(jqXHR);
    }
  })
};

function setShangJiaDataToMap() {
  if (shangjiaDataList && shangjiaDataList.length) {
    shangjiaDataList.forEach(item => {
      if (item.fid) {
        let model = getMapModel(item.fid);
        if (model) {
          setModelColor5(model, item)
          let data = {
            x: model.bound.center.x,
            y: model.bound.center.y,
            floor: item.floor,
            name: item.name
          };
          setModelImage3(data, item);
        } else {
          let data = {
            x: item.x,
            y: item.y,
            floor: item.floor,
            name: item.name
          };
          setModelImage3(data, item);
        }
      } else {
        let data = {
          x: item.x,
          y: item.y,
          floor: item.floor,
          name: item.name
        };
        setModelImage3(data, item);
      }
    });
  }
};

function setModelColor5(model, item) {
  model.setColor('#E4DDFF', 1);
  model.setBorderColor('#D7CFF1', 1);
};

function setModelImage3(data, item) {
  if (!data.floor) return;
  /* 设置图标 */
  let iconUrl = '';
  if (item.photolocal2) {
    iconUrl = url + item.photolocal2;
  };
  var floor = fMap.getFloor(+item.floor);

  if (iconUrl) {
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
        url: url + item.photolocal, // 图片url地址
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
      x: +data.x,
      y: +data.y
    };
    var compositeMarker = new fengmap.FMCompositeMarker(compositeOptions);
    compositeMarker.addTo(floor);
  } else {
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
      x: +data.x,
      y: +data.y,
    };
    let textMarker = new fengmap.FMTextMarker(textData);
    textMarker.addTo(floor);
  }
};

var companyDataList = [];
function getCompanyData(mapId) {
  $.ajax({
    url: url + 'park/getCompany',
    data: {
      map: mapId,
      pageSize: -1
    },
    success: function (res) {
      var data = res.data || [];
      var len = data.length;
      if (res.code != 200 || !len) {
        return;
      };
      companyDataList = data;
      setCompanyDataToMap();
    },
    error: function (jqXHR) {
      resError(jqXHR);
    }
  })
};

function setCompanyDataToMap() {
  if (companyDataList && companyDataList.length) {
    companyDataList.forEach(item => {
      if (item.fid) {
        let model = getMapModel(item.fid);
        if (model) {
          setModelColor5(model, item)
          let data = {
            x: model.bound.center.x,
            y: model.bound.center.y,
            floor: item.floor,
            name: item.name
          };
          setModelImage5(data, item);
        } else {
          let data = {
            x: item.x,
            y: item.y,
            floor: item.floor,
            name: item.name
          };
          setModelImage5(data, item);
        }
      } else {
        let data = {
          x: item.x,
          y: item.y,
          floor: item.floor,
          name: item.name
        };
        setModelImage5(data, item);
      }
    });
  }
};

function setModelImage5(data, item) {
  if (!data.floor) return;
  var floor = fMap.getFloor(+item.floor);
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
    text: data.name,
    x: +data.x,
    y: +data.y,
  };
  let textMarker = new fengmap.FMTextMarker(textData);
  textMarker.addTo(floor);
};


var vipAreaMapData = [];
function getVipArea(mapid) {
    $.ajax({
        url: `${url}vip/getBarrierGateInfo`,
        data: {
            map: mapid,
            pageSize: -1
        },
        type: 'POST',
        success: function (res) {
            const data = res.data.list;
            if (!data || data.length == 0) {
                return;
            }
            vipAreaMapData = data;
            setVipAreaMarker();
        }
    })
};

function setVipAreaMarker() {
    if (vipAreaMapData && vipAreaMapData.length) {
        vipAreaMapData.forEach(item => {
            if (item.fid) {
                let model = getMapModel(item.fid);
                if (model) {
                    // setModelColor6(model, item)
                    let data = {
                        x: model.bound.center.x,
                        y: model.bound.center.y,
                        floor: item.floor,
                        name: item.bindingArea
                    };
                    setModelImage5(data, item)
                } else {
                    let data = {
                        x: item.x,
                        y: item.y,
                        floor: item.floor,
                        name: item.bindingArea
                    };
                    setModelImage5(data, item)
                }
            } else {
                let data = {
                    x: item.x,
                    y: item.y,
                    floor: item.floor,
                    name: item.bindingArea
                };
                setModelImage5(data, item)
            }
        });
    }
};