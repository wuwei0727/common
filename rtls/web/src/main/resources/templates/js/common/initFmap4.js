var fMap = null;//fmap变量

// 导航对象
var navi = null;
var mapLoad = false;//地图加载是否成功
var mapdraw;        // 点线面绘制类
var drawType;
var scrollFloorControl;
var mapArr = [];
var beforeEdit = false;
var beforePoint = [];
var curPoints;
var activeBtn;      // 当前选中btn
var first = false;

var floorInfo = [];
var selectMarker = null;

var themeList = []
//初始化
function openMap(params, callback) {
    $.ajax({
        url: url + 'map/getMap2dSel',
        data: {
            pageSize: -1,
            enable: 1,
        },
        type: 'get',
        success: function (res) {
            themeList = res.data;
            openMap2(params, callback);
        },
        error: function (jqXHR) {
            resError(jqXHR);
            openMap2(params, callback);
        }
    })
};

function openMap2(para, callback) {
    console.log(para)

    var hasThemeName = false;
    if (themeList && themeList.length && para.mapId) {
        let item = themeList.find((item) => item.id == para.mapId);
        if (item && item.themeName) {
            hasThemeName = item.themeName
        }
    };

    var focusFloor = para.focusFloor || 1;
    var themeURL = 'https://console.fengmap.com/api-s/webtheme/';
    drawType = [fengmap.FMType.POLYGON_MARKER, fengmap.FMType.IMAGE_MARKER, fengmap.FMType.LINE_MARKER]
    var options = {
        appName: para.fName,
        key: para.fKey,
        mapID: para.fId,
        //必要，地图容器
        container: document.getElementById('fengmap'),
        //主题数据位置
        // themeURL: themeURL,
        // themeID:'4006',
        themeID: hasThemeName ? hasThemeName : (para.themeName || 'tgymap'),
        //聚焦楼层
        level: focusFloor,
        //显示楼层
        visibleLevels: [focusFloor],
        //默认视图
        viewMode: para.FMViewMode || fengmap.FMViewMode.MODE_3D,
        //楼层高度
        floorSpace: 30,
        //点击不高亮
        highlightPicker: para.highlight || [],

    };
    //初始化地图对象
    if (fMap) {
        //销毁
        scrollFloorControl.remove();
        fMap.dispose();
        fMap = null;
        mapLoad = false;
    }
    fMap = new fengmap.FMMap(options);
    //地图加载完成事件
    fMap.on('loaded', function () {
        console.log('地图加载完成！', para);
        if (para.mapId) {
            getAllDataToMap(para.mapId)
            /* 隐藏文字 */
            setTimeout(() => {
                /* 图层控制 */
                layerControl()
            }, 16);
        };
        mapLoad = true;
        //加载楼层控件
        initToolbar();
        /* 初始化点线面编辑类 */
        if (para.wantToDo === "drawArea") {
            initMapEditor(para.polygonColor);
            if (para.initPoints) {
                mapArr = [];
                initMapPoints(para.initPoints, para.polygonColor);
            }
        }
        else if (para.wantToDo === "selectPoint") {
            callback && callback();
        }
    });
    //地图点击事件
    fMap.on('click', function (event) {
        console.log(event)
        if (para.wantToDo === 'drawArea') {
            // 拾取当前选中marker
            var marker = getMarker(event.targets);
            console.log(marker)
            if (marker) {
                if (mapdraw.DrawTool === 'del') {
                    // 删除绘制图形
                    if (marker.points[0].floor) {
                        marker.points.forEach(item => {
                            delete item.floor;
                        });
                    }
                    var index = mapArr.findIndex(item => JSON.stringify(item.points) === JSON.stringify(marker.points));
                    mapArr.splice(index, 1);
                    initAnalyser(marker.points)
                    marker.remove();
                } else if (mapdraw.DrawTool === 'edit') {
                    // 编辑绘制图形
                    var findItem = mapArr.find(item => {
                        if (item.points === event.targets[0].points) {
                            initAnalyser(item.points, false);
                            curPoints = item.points;
                            beforeEdit = true;
                            return item;
                        }
                    });

                    mapdraw.edit(marker);
                }
            } else if (mapdraw.DrawTool === 'edit') {
                // 取消编辑
                mapdraw.DrawTool = 'none';
                if (activeBtn) {
                    var activeDom = document.getElementById(activeBtn);
                    activeDom.classList.remove('avtiveSpan');
                }
            }
        } else if (para.wantToDo === 'selectPoint' && activeBtn) {
            let fid = event.targets[0].FID;
            if (placeDataList && placeDataList.length && placeDataList.some((item) => item.fid == fid)) {
                tips('该位置已有车位');
                return;
            } else if (crossLayerData && crossLayerData.length && crossLayerData.some((item) => item.fid == fid)) {
                tips('该位置已有跨层通道');
                return;
            } else if (elevatorData && elevatorData.length && elevatorData.some((item) => item.fid == fid)) {
                tips('该位置已有电梯');
                return;
            } else if (serviceFacilitiesData && serviceFacilitiesData.length && serviceFacilitiesData.some((item) => item.fid == fid)) {
                tips('该位置已有服务设施');
                return;
            } else if (shangjiaDataList && shangjiaDataList.length && shangjiaDataList.some((item) => item.fid == fid)) {
                tips('该位置已有商家');
                return;
            } else if (companyDataList && companyDataList.length && companyDataList.some((item) => item.fid == fid)) {
                tips('该位置已有公司');
                return;
            } else if (placeExitData && placeExitData.length && placeExitData.some((item) => item.fid == fid)) {
                tips('该位置已有出入口');
                return;
            } else if (vipAreaMapData && vipAreaMapData.length && vipAreaMapData.some((item) => item.fid == fid)) {
                tips('该位置已有VIP区域');
                return;
            }

            selectMarker && selectMarker.remove();
            selectMarker = null;
            selectMarker = new fengmap.FMImageMarker({
                url: "../image/common/location.png",
                x: event.coords.x,
                y: event.coords.y,
                anchor: fengmap.FMMarkerAnchor.BOTTOM,
                collision: false
            });
            var level = fMap.getLevel()
            var floor = fMap.getFloor(level);
            selectMarker.addTo(floor);
            mapClickFn({
                x: event.coords.x,
                y: event.coords.y,
                floor: level
            });
        }
    });
}

/* 初始化点线面编辑类 */
function initMapEditor(polygonColor, flag) {
    if (flag) {
        mapArr.forEach(item => {
            item.pm.color = polygonColor;
            item.pm.borderColor = polygonColor;
        });
    }
    mapdraw = new fengmap.FMMapEditor(fMap);
    // // 绘制点样式
    mapdraw.PointStyle = {
        url: 'https://developer.fengmap.com/fmAPI/images/point.png',
        size: 30,
        height: 1,
        collision: true
    }
    // 绘制线样式
    mapdraw.LineStyle = {
        width: 5,
        type: fengmap.FMLineType.FULL,
        color: '#FF0000',
        height: 10,
        smooth: true
    }
    // 绘制多边形样式
    mapdraw.PolygonStyle = {
        color: polygonColor,
        borderWidth: 2,
        borderColor: polygonColor,
        opacity: 0.3,
        height: 1
    }
    // 绘制完成事件监听
    mapdraw.on('drawend', function (e) {
        console.log('绘制完成事件', e);
        mapArr.push({ points: e.info.points, floor: fMap.getLevel(), pm: e.object });
        beforeEdit = false;
        curPoints = e.info.points;
        initAnalyser(e.info.points, false);
    });
    // 绘制中事件监听
    mapdraw.on('drawing', function (e) {
        console.log('绘制中的事件');
    });
    // 编辑完成事件监听
    mapdraw.on('editend', function (e) {
        console.log('编辑完成事件', e);
        if (first) {
            initAnalyser(e.info.points, true);
        } else {
            if (beforeEdit) {
                initAnalyser(e.info.points, true);
            } else {
                initAnalyser(e.info.points);
            }
        }
        var index = mapArr.findIndex(item => item.points === curPoints);
        if (index !== -1) {
            mapArr[index] = { points: e.info.points, floor: fMap.getLevel(), pm: e.object };
        } else {
            mapArr.push({ points: e.info.points, floor: fMap.getLevel(), pm: e.object });
        }
        curPoints = e.info.points
    });
}
/* 拾取选中Marker */
function getMarker(targets) {
    var result = null;
    if (targets.length > 0) {
        result = targets.find(function (item) {
            return drawType.indexOf(item.type) !== -1;
        });
    }
    return result;
}
/* 设置绘制类型 */
function drawOpetion(type) {
    // 设置类型
    mapdraw.DrawTool = type;
    curPoints = '';
    if (activeBtn) {
        var activeDom = document.getElementById(activeBtn);
        activeDom.classList.remove('avtiveSpan');
    }
    var dom = document.getElementById(type);
    dom.classList.add('avtiveSpan');
    activeBtn = type;
    if (first = true) {
        first = false;
        beforeEdit = false;
    }
    if (type !== 'edit') beforeEdit = false;
}

//加载滚动型楼层切换控件
function initToolbar() {
    if (!fengmap.FMControlPosition) return;
    var scrollFloorCtlOpt = {
        position: fengmap.FMControlPosition.RIGHT_TOP,
        floorButtonCount: 5,
        offset: {
            x: -20,
            y: 20
        },
        viewModeControl: true,
        floorModeControl: true,
        needAllLayerBtn: true
    };
    scrollFloorControl = new fengmap.FMToolbar(scrollFloorCtlOpt);
    scrollFloorControl.addTo(fMap)
}
/* 初始化分析器，这里初始化分析器使用 Map 实例进行构造，因此 分析器必须在 Map 的 Loaded 事件回调中使用，如果希望并行使用，请采用 option 的配置方案。 */
function initAnalyser(points, flag) {

    points.forEach((item) => {
        item.x = +item.x;
        item.y = +item.y
    });

    analyser = new fengmap.FMSearchAnalyser({
        map: fMap
    }, function () {
        request = initRequest(points);
        analyser.query(request, function (result) {
            markSearchResult(result, flag, points);
        });
    });
}

function initRequest(points) {
    var searchRequest = new fengmap.FMSearchRequest();
    searchRequest.levels = [fMap.getLevel()];
    searchRequest.type = fengmap.FMType.MODEL;
    searchRequest.addCondition({ 'typeID': [200401, 340862, 340860, 340859, 340861, 340863, 340864, 340878, 340879] });
    searchRequest.addCondition({
        'polygon': points
    });
    return searchRequest;
}
function markSearchResult(result, flag, points) {
    var newResult = result.map((it, index) => { it['id'] = index + 1; return it });
    console.log('xx-newResult', newResult)
    var newPlace = newResult;
    var newPlaceName = [];
    newPlace.forEach(item => {
        // if (item.name !== undefined) {
        //     newPlaceName.push({ name: item.name, check: false });
        // }

        /* 地图绘制时不添加车位名字,导至车位推荐框选不上问题 */
        let target = placeDataList.find((pitem) => {
            return pitem.fid == item.FID;
        });
        if (target && target.name) {
            newPlaceName.push({ name: target.name, check: false });
        }
    });
    console.log(newPlaceName)
    if (mapdraw.DrawTool === 'del') {
        delRecomPlace(newPlaceName, mapArr)
    } else if ((mapdraw.DrawTool === 'edit' || mapdraw.DrawTool === 'polygon') && beforeEdit == false) {
        addRecomPlace(newPlaceName, mapArr);
        first = true;
        beforeEdit = true;
        beforePlace = newPlaceName;
        console.log("meiyoubefore")
        if (mapdraw.DrawTool === 'polygon') {
            mapdraw.DrawTool = 'none';
            if (activeBtn) {
                var activeDom = document.getElementById(activeBtn);
                activeDom.classList.remove('avtiveSpan');
            }
        }
    } else if (mapdraw.DrawTool === 'edit' && beforeEdit == true && !flag) {
        console.log("之前的车位")
        beforePlace = newPlaceName;
    } else if (mapdraw.DrawTool === 'edit' && beforeEdit == true && flag) {
        console.log("之后的车位")
        const arr__1 = newPlaceName.filter(x => !beforePlace.some(y => y.name === x.name))
        addRecomPlace(arr__1, mapArr);
        const arr__2 = beforePlace.filter(x => !newPlaceName.some(y => y.name === x.name))
        delRecomPlace(arr__2, mapArr)
        beforePlace = newPlaceName;
    }
    if (newPlaceName.length === 0 && mapdraw.DrawTool !== 'del') {
        tips("请重新选择有车位号的区域");
        if (points[0].floor) {
            points.forEach(item => {
                delete item.floor;
            });
        }
        var index = mapArr.findIndex(item => JSON.stringify(item.points) === JSON.stringify(points));
        mapArr[index].pm.remove();
        mapArr.splice(index, 1);
        if (mapdraw.DrawTool === 'edit') {
            mapdraw.DrawTool = 'none';
            if (activeBtn) {
                var activeDom = document.getElementById(activeBtn);
                activeDom.classList.remove('avtiveSpan');
            }
        }
    }
};
function initMapPoints(polygonCoords, polygonColor) {
    /* 添加任意多边形 */
    var polygonOption = {
        color: polygonColor,
        borderWidth: 2,
        borderColor: polygonColor,
        opacity: 0.3
    }
    polygonCoords.forEach(item => {
        polygonOption.points = item;
        var arr = [];
        item.forEach(subItem => {
            var obj = { x: subItem.x, y: subItem.y };
            arr.push(obj);
        });
        var floor = fMap.getFloor(item[0].floor);
        polygon = new fengmap.FMPolygonMarker(polygonOption);
        polygon.addTo(floor);
        mapArr.push({ points: arr, floor: item[0].floor, pm: polygon });
    });
}
function addLayer(info) {
    console.log(info)
    selectMarker = new fengmap.FMImageMarker({
        url: "../image/common/location.png",
        x: info.x,
        y: info.y,
        anchor: fengmap.FMMarkerAnchor.BOTTOM,
        collision: false
    });

    var level = fMap.getLevel()
    var floorNum = info.floor ? info.floor : level;
    var floor = fMap.getFloor(floorNum);
    selectMarker.addTo(floor);
    if (info.num) {
        var textMarker = new fengmap.FMTextMarker({
            text: info.num,
            fillColor: "255,0,0", //填充色
            fontsize: 18, //字体大小
            strokeColor: "255,255,0", //边框色
            x: info.x - 0.6,
            y: info.y - 0.12,
            anchor: fengmap.FMMarkerAnchor.RIGHT_TOP,
            collision: false
        });
        textMarker.addTo(floor)
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
            fontsize: 13,
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
            fontsize: 13,
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
            fontsize: 13,
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
                url: iconUrl, // 图片url地址
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
                    text: data.name,  // 文字内容，必填
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
            fontsize: 13,
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
        fontsize: 13,
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