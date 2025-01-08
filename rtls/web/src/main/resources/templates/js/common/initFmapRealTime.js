var fMap = null;//fmap变量
var fmapID;//定义地图ID变量
// 导航对象
var navi = null;
//定义选中模型
var selectedModel = null;
var mapLoad = false;//是否加载成功
var placeDataList; // 后台车位数据

// var NewTextlayer;   //实例化TextMarkerLayer
var fMapLayer2 = {};

var placeMarker = []; // 车位名marker数据

//初始化
function openMap(params, callback, errorCallbak) {
    console.log('params', params);
    fmapID = params.fId;
    var focusFloor = params.focusFloor || 1;
    var defaultParams = {
        container: document.getElementById('fengmap'),
        appName: params.fName,
        key: params.fKey,
        //支持单击模型高亮，false为单击时模型不高亮
        modelSelectedEffect: false,
        defaultMapScale: 2012,
        //楼层高度
        defaultGroupSpace: 30,
        defaultViewMode: fengmap.FMViewMode.MODE_2D,
        //地图数据位置
        /*mapServerURL: url + params.path + '/',
        //主题数据位置
        mapThemeURL: url + 'theme/',*/
        // defaultThemeName:'4006',
        defaultThemeName: params.themeName || 'tgymap',

        //默认聚焦楼层
        defaultFocusGroup: focusFloor,
        //初始显示楼层ID数组
        defaultVisibleGroups: [focusFloor],
    }
    $.extend(defaultParams, params);
    //初始化地图对象
    if (fMap) {
        //销毁
        fMap.dispose();
        fMap = null;
        mapLoad = false;
    }

    fMap = new fengmap.FMMap(defaultParams);
    //打开Fengmap服务器的地图数据和主题
    fMap.openMapById(fmapID, function (error) {
        //打印错误信息
        console.log('Fmap地图加载出错', error);
        tips('地图加载出错，请重试');
        if (typeof errorCallbak === 'function') {
            errorCallbak();
        }
    });

    //地图加载完成事件
    fMap.on('loadComplete', function () {

        /* 加载后台车位数据 */
        if (params.mapId) {
            getAllDataToMap(params.mapId)
            /* 隐藏文字 */
            setTimeout(() => {
                changeLabelLayerVis(false);
            }, 16);
        };




        mapLoad = true;
        //加载滚动型楼层切换控件
        loadScrollFloorCtrl(params.floorTop);

        if (typeof fengmap.FMNavigation === 'function') {
            //需引入navi.min
            navi = new fengmap.FMNavigation({
                map: fMap,
            })
        }
        //初始化指南针
        if (defaultParams.compassSize) {
            loadCompass();
        }

        /* 隐藏文字 */
        setTimeout(() => {
            changeLabelLayerVis(false);
        }, 16);

        if (typeof callback === 'function') {
            callback();
        }
    });
    //地图点击事件，需要在地图加载完成之后操作
    fMap.on('mapClickNode', function (event) {
        //地图模型
        var target = event.target;
        console.log('mapClickNode', target);
        if (!target) {
            return;
        }
        var buttonType = event.domEvent.button;
        var buttonTypeText = '';
        if (buttonType === 2) {
            return;
        }
        var coord = null;
        if (typeof mapClickFn == 'function') {
            coord = event.eventInfo.coord;
            mapClickFn({
                fid: target.FID,
                x: coord.x,
                y: coord.y,
                z: target.mapCoord.z,
                floor: target.groupID,
                //模型的中点
                cx: target.mapCoord.x,
                cy: target.mapCoord.y,
                nodeType: target.nodeType,
                //自定义属性(没有则不用管)
                selfId: target.selfId || '',
                selfType: target.selfType || '',
            }, target);
        }
    });
}
//加载滚动型楼层切换控件
function loadScrollFloorCtrl(y) {
    if (!fengmap.FMScrollGroupsControl) {
        console.log('需引入control.js的文件才能初始化切换楼层');
        return;
    }
    var scrollFloorCtlOpt = {
        //默认在右上角
        position: fengmap.FMControlPosition.RIGHT_TOP,
        showBtnCount: 3,
        allLayer: false,
        needAllLayerBtn: true,
        offset: {
            x: -20,
            y: y || 72
        },
        imgURL: '../image/fMap/',
    };
    new fengmap.FMScrollGroupsControl(fMap, scrollFloorCtlOpt);
}
//覆盖蜂鸟的提示
// console.warn = tips;
//返回自定义几何体
function resExtrudeMarker(point, params) {
    params = params || {};
    // 创建自定义拉伸几何体
    var extrudeMarker = new fengmap.FMExtrudeMarker({
        // 颜色
        color: params.color,
        // 透明度
        alpha: params.alpha || 0.4,
        // 设置高度
        height: params.height || 0,
        // 拉伸高度
        extrudeHeight: params.extrudeHeight || 3,
        // 拉伸几何体的坐标点集数组，内部会改变数据结构
        points: point,
        lineType: 'full',
        // 边线颜色
        lineColor: params.color || '',
        // 边线透明
        lineAlpha: params.lineAlpha || 1.0
    });
    return extrudeMarker;
}
//返回文本
function resTextMarker(params) {
    var tm = new fengmap.FMTextMarker({
        x: params.x,
        y: params.y,
        height: params.height || 1,
        name: params.name || '',
        fillcolor: params.fillcolor || "",
        fontsize: params.fontsize || 16,
        strokecolor: params.strokecolor || "",
        anchor: fengmap.FMMarkerAnchor.CENTER,
    });
    if (params.avoid === false) {
        tm.avoid(false);
    }
    return tm;
}
//返回图片
function resImgMarker(params) {
    var im = new fengmap.FMImageMarker({
        x: params.x,
        y: params.y,
        height: params.height || 1,
        //设置图片路径
        url: params.url,
        //设置图片显示尺寸
        size: params.size || 32
    });
    if (params.avoid === false) {
        im.avoid(false);
    }
    return im;
}
//返回圆
function resCircleMarker(params) {
    var cm = new fengmap.FMPolygonMarker({
        //设置颜色
        color: params.color || '#3CF9DF',
        //设置透明度
        alpha: params.alpha || .3,
        //设置边框线的宽度
        lineWidth: params.lineWidth || 1,
        //设置高度
        height: 4,
        //多边形的坐标点集数组
        points: {
            //设置为圆形
            type: 'circle',
            //设置此形状的中心坐标
            center: {
                x: params.x,
                y: params.y
            },
            //设置半径
            radius: params.r || 2,
            //设置段数，默认为40段
            segments: 40
        }
    });
    return cm;
}
//隐藏显示label文本标注层
function changeLabelLayerVis(boolean) {
    var floorList = fMap.groupIDs;
    var group = null;
    for (var j = 0; j < floorList.length; j++) {
        group = fMap.getFMGroup(floorList[j]);
        //遍历图层
        group.traverse(function (fm) {
            if (fm instanceof fengmap.FMLabelLayer) {
                fm.show = boolean;
            };
            if (fm instanceof fengmap.FMFacilityLayer) {
                fm.show = boolean;
            }
        });
    }
};

//隐藏后台增加的车位名
function changeLabelLayerVis1(boolean) {
    placeMarker.forEach((item) => {
        item.tm.show = boolean
    })
};

//隐藏/显示公共设施标注层
function changeFacLayerVis(boolean) {
    var group = fMap.getFMGroup(fMap.focusGroupID);
    group.traverse(function (fm) {
        //FMFacilityLayer 公共设施标注层对象
        if (fm instanceof fengmap.FMFacilityLayer) {
            fm.show = boolean;
        }
    });
}

/**
* 设置model颜色、透明度、边线颜色
* */
function setModelRender(model, color, state) {
    var modelColor = color || ''; //模型颜色
    var lineColor = state === 0 ? "#009417" : '';
    var alpha = 1; //颜色透明度
    if (model && typeof model.setColor === 'function') {
        //修改模型颜色及透明度
        model.setColor(modelColor, alpha);
        //修改模型边线的颜色及透明度
        state === 0 ? model.setStrokeColor(lineColor, alpha) : '';
    }
}

/**
 * 修改model恢复到主题中的设置
 * */
function setModelToDefault(model) {
    if (model && typeof model.setColorToDefault === 'function') {
        //将模型的颜色及透明度恢复回主题中的设置
        model.setColorToDefault();
        //将模型边线的颜色及透明度恢复回主题中的设置
        //model.setStrokeColorToDefault();
    }
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
    if (fMap.viewMode === '3d') {
        //切换地图为二维模式
        fMap.viewMode = fengmap.FMViewMode.MODE_2D;
        $(domObj).removeClass('viewMode3D');
        $(domObj).addClass('viewMode');
    } else {
        //切换地图为三维模式
        fMap.viewMode = fengmap.FMViewMode.MODE_3D;
        $(domObj).addClass('viewMode3D');
    }
}

/**
 * 根据条件搜索地图
 * */
function findModel(params) {
    params = params || {};
    if (params.FID && typeof params.FID === 'number') {
        //要转成字符串
        params.FID += '';
    }
    params.nodeType = fengmap.FMNodeType.MODEL;
    var result = searchByParams(params);
    var tm = null;
    var target = null;
    if (result.length > 0) {
        var model = result[0];

        if (model != null) {
            //模型节点对象
            target = model.target;
        }
    }
    return target;
}
/**
 * 根据参数信息进行搜索
 * keyword: 搜索关键字
 * gids： 楼层id，默认为'all':所有楼层
 * nodeType：fengmap.FMNodeType
 * fid: 模型FID,整个建筑内唯一ID。
 * */
function searchByParams(params) {
    if (fMap == null) {
        return;
    }

    /**
     * fengmap.FMSearchRequest 是可设置查询类型，查询关键字的请求类
     * https://developer.fengmap.com/docs/js/v2.7.1/fengmap.FMSearchRequest.html
     * */
    var searchRequest = new fengmap.FMSearchRequest();

    //配置keyword参数
    if (params.keyword && params.keyword != '') {
        searchRequest.keyword = params.keyword;
    }

    //配置groupID参数
    if (params.groupID) {
        searchRequest.groupID = params.groupID
    }

    //配置FID参数
    if (params.FID) {
        searchRequest.FID = params.FID;
    }
    //配置typeID参数
    if (params.typeID != null) {
        searchRequest.typeID = params.typeID;
    }

    //配置nodeType参数
    if (params.nodeType != null) {
        searchRequest.nodeType = params.nodeType;
    }

    /*
     //设置模糊搜索的语言类型，只针对keyword类型的搜索
     searchRequest.keywordLanguageTypes([fengmap.FMLanguageType.EN,fengmap.FMLanguageType.ZH]);
     */

    /*//周边查询
    searchRequest.circle = {
        //查询范围中心点坐标
        center: {
            x: map.center.x,
            y: map.center.y
        },
        radius: 50  //查询范围半径
    };*/

    /**
     * 根据请求对象查询分析方法
     * https://developer.fengmap.com/docs/js/v2.7.1/fengmap.FMSearchAnalyser.html#getQueryResult
     * */
    var sortRes = analyser.getQueryResult(searchRequest, ['SINGLE']);

    return sortRes;
}
//初始化指南针
function loadCompass() {
    /**
     * 显示指北针，设置背景色需要在加载指北针之前设置
     * */
    fMap.compass.setBgImage('../image/fMap/compass_bg.png'); //设置背景图片
    fMap.compass.setFgImage('../image/fMap/compass_fg.png'); //设置前景图片
    fMap.showCompass = true;

    //点击指北针事件, 使角度归0
    fMap.on('mapClickCompass', function () {
        fMap.rotateTo({
            //设置角度
            to: 0,
            //动画持续时间，默认为。3秒
            duration: 0.3,
            callback: function () { //回调函数
                console.log('rotateTo complete!');
            }
        })
    });
}
// 添加标注物
function addLayer(params, site) {
    var focusFloor = params.focusFloor || 1;
    // 文字标注
    var groupLayer = fMap.getFMGroup(focusFloor);
    var layer = new fengmap.FMTextMarkerLayer();   //实例化TextMarkerLayer
    var imgLayer = new fengmap.FMImageMarkerLayer();

    groupLayer.addLayer(layer);    //添加文本标注层到模型层。否则地图上不会显示
    groupLayer.addLayer(imgLayer);
    //图标标注对象，默认位置为该楼层中心点
    if (site) {
        site.tm.setPosition(params.x, params.y + 0.2, params.focusFloor, 1);
        // tm.alwaysShow(); 
        // tm.x =params.x;
        // tm.y = params.y;
        site.im.setPosition(params.x, params.y, params.focusFloor, 1);
    } else {
        var tm = new fengmap.FMTextMarker({
            name: params.num,
            x: params.x,
            y: params.y,
            //文字标注样式设置
            fillcolor: "255,0,0", //填充色
            fontsize: 12, //字体大小
            strokecolor: "255,255,0", //边框色
            alpha: 1,   //文本标注透明度
            anchor: fengmap.FMMarkerAnchor.RIGHT_TOP

        });
        layer.addMarker(tm);  //文本标注层添加文本Marker
        tm.alwaysShow();    // 在marker载入完成后，设置 "一直可见"，不被其他层遮挡
        // 图片标注
        var im = new fengmap.FMImageMarker({
            url: '../image/common/location.png',
            size: 36,  //设置图片显示尺寸
            height: 1, //marker标注高度
            anchor: '4',
            x: params.x,
            y: params.y,

        });
        imgLayer.addMarker(im);  //文本标注层添加文本Marker
        im.alwaysShow();
    }

    //     // 跳转中心点
    //     //中心点
    //     var pnt = {
    //         x:params.x,
    //         y:params.y,
    //         time:3,
    //         groupID:params.focusFloor ,      //目标层GroupID
    //         callback:function(){
    //             //跳转中心点完成
    //         }
    //     };
    // //跳转
    //     fMap.moveTo(pnt);
    return {
        tm,
        im
    }
}

/* 显示后台车位名字 */
function getPlaceData(mapId) {
    console.log('显示后台车位名字', mapId);
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
        // NewTextlayer = new fengmap.FMTextMarkerLayer();   //实例化TextMarkerLayer
        var floorList = fMap.groupIDs;
        console.log('floorList', floorList);
        var group = null;
        var gid;
        for (var j = 0; j < floorList.length; j++) {
            //获取所有楼层的layer
            gid = floorList[j];
            group = fMap.getFMGroup(gid);
            fMapLayer2['f' + gid] = {
                textLayer: group.getOrCreateLayer('textMarker'),
                imgLayer: group.getOrCreateLayer('imageMarker'),
            }
        }
        placeDataList.forEach(item => {
            if (item.fid) {
                let model = findModel({ FID: item.fid });
                if (model) {
                    setModelColor(model, item)
                    let data = {
                        x: model.mapCoord.x,
                        y: model.mapCoord.y,
                        floor: item.floor,
                        name: item.name
                    };
                    setModelName(data, item)
                } else {
                    let data = {
                        x: item.x,
                        y: item.y,
                        floor: item.floor,
                        name: item.name
                    };
                    setModelName(data, item)
                }
            } else {
                let data = {
                    x: item.x,
                    y: item.y,
                    floor: item.floor,
                    name: item.name
                };
                setModelName(data, item)
            }
        });
    }
};

/* 设置模型颜色 */
function setModelColor(model, data) {
    if (data.state == 0) {
        model.setColor('#83F58D', 1);
        model.setStrokeColor('#45833B', 1);
    } else if (data.state == 1) {
        model.setColor('#FDBBC5', 1);
        model.setStrokeColor('#BA646E', 1);
    } else if (data.state == 3) {
        model.setColor('#C2C2C2', 1);
        model.setStrokeColor('#474545', 1);
    } else {
        model.setColorToDefault();
        model.setStrokeColorToDefault();
    }
};

function setModelColor2(model, item) {
    if (item.objectType == '200005') {
        /* 步行梯 */
        model.setColor('#daeed9', 1);
        model.setStrokeColor('#BCD4BD', 1);
    } else if (item.objectType == '340818') {
        /* 手扶电梯 */
        model.setColor('#D9F2FC', 1);
        model.setStrokeColor('#AAD2EB', 1);
    } else if (item.objectType == '340855') {
        /* 电梯前室 */
        model.setColor('#D3DEFE', 1);
        model.setStrokeColor('#ACBCFE', 1);
    } else if (item.objectType == '200004') {
        /* 直升电梯 */
        model.setColor('#8EEBFF', 1);
        model.setStrokeColor('#38A9C7', 1);
    };
};

function setModelColor3(model, item) {
    model.setColor('#E3F1FF', 1);
    model.setStrokeColor('#597CBE', 1);
};

function setModelColor4(model, item) {
    /* 设置模型颜色 */
    if (item.objectType == '340873') {
        /* 一类建筑 */
        model.setColor('#8EABF0', 1);
        model.setStrokeColor('#486FCF', 1);
    } else if (item.objectType == '340874') {
        /* 二类建筑 */
        model.setColor('#BDDBFE', 1);
        model.setStrokeColor('#597CBE', 1);
    } else if (item.objectType == '340875') {
        /* 三类建筑 */
        model.setColor('#D4F6FD', 1);
        model.setStrokeColor('#597CBE', 1);
    } else if (item.objectType == '100004') {
        /* 男洗手间 */
        model.setColor('#dcf6ff', 1);
        model.setStrokeColor('#9eb5bf', 1);
    } else if (item.objectType == '100005') {
        /* 女洗手间 */
        model.setColor('#FADADE', 1);
        model.setStrokeColor('#FADADE', 1);
    } else if (item.objectType == '200010') {
        /* 安全出口 */
        model.setColor('#daeed9', 1);
        model.setStrokeColor('#BCD4BD', 1);
    } else if (item.objectType == '200110') {
        /* 外部道路 */
        model.setColor('#FFFFFF', 1);
        model.setStrokeColor('#FFFFFF', 1);
    }
};

function setModelColor5(model, item) {
    model.setColor('#E4DDFF', 1);
    model.setStrokeColor('#D7CFF1', 1);
};

function setModelColor6(model, item) {
    model.setColor('#CFEEDE', 1);
    model.setStrokeColor('#ACDAC2', 1);
};

/* 设置模型名字 */
function setModelName(data, item) {
    // var groupLayer = fMap.getFMGroup(+data.floor);
    // var NewTextlayer = new fengmap.FMTextMarkerLayer();   //实例化TextMarkerLayer
    // groupLayer.addLayer(NewTextlayer);    //添加文本标注层到模型层。否则地图上不会显示
    // console.log(groupLayer);
    // NewTextlayer.avoid(true)

    let textMarker = new fengmap.FMTextMarker({
        name: data.name,
        x: data.x,
        y: data.y,
        //文字标注样式设置
        fillcolor: "#252525", //填充色
        fontsize: 12, //字体大小
        strokecolor: "#fff", //边框色
        alpha: 1,   //文本标注透明度
        anchor: fengmap.FMMarkerAnchor.BOTTOM

    });
    textMarker.avoid(true);

    let imgurl = '';
    if (item.type == 1) {
        /* 充电车位 */
        imgurl = '../image/mapIcon/placeType1.png'
    } else if (item.type == 2) {
        /* 专属车位 */
        imgurl = '../image/mapIcon/placeType2.png'
    } else if (item.type == 3) {
        /* 无障碍车位 */
        imgurl = '../image/mapIcon/placeType3.png'
    };

    // 图片标注
    var im = new fengmap.FMImageMarker({
        url: imgurl,
        size: 26,  //设置图片显示尺寸
        height: 1, //marker标注高度
        anchor: '4',
        x: data.x,
        y: data.y + 0.55,
    });
    im.avoid(true)

    if (data.floor) {
        let dataPlaceM = {
            name: data.name,
            x: data.x,
            y: data.y,
            floor: data.floor,
            tm: textMarker
        }
        fMapLayer2['f' + data.floor].textLayer.addMarker(textMarker);
        if (imgurl) {
            fMapLayer2['f' + data.floor].imgLayer.addMarker(im);
            dataPlaceM.im = im
        }
        placeMarker.push(dataPlaceM)
    }
};

function setModelImage(data, item) {
    var tm = new fengmap.FMTextMarker({
        name: data.name,
        x: data.x,
        y: data.y,
        //文字标注样式设置
        //文字标注样式设置
        fillcolor: "#252525", //填充色
        fontsize: 12, //字体大小
        strokecolor: "#fff", //边框色
        alpha: 1,   //文本标注透明度
        anchor: fengmap.FMMarkerAnchor.BOTTOM

    });

    let imgurl = '';
    if (item.iconType == '170001') {
        /* 步行梯 */
        imgurl = '../image/mapIcon/Exit5.png'
    } else if (item.iconType == '170003') {
        /* 手扶电梯 */
        imgurl = '../image/mapIcon/Exit7.png'
    } else if (item.iconType == '170006') {
        /* 电梯前室 */
        imgurl = '../image/mapIcon/Exit6.png'
    };

    // 图片标注
    var im = new fengmap.FMImageMarker({
        url: imgurl,
        size: 26,  //设置图片显示尺寸
        height: 1, //marker标注高度
        anchor: '4',
        x: data.x,
        y: data.y + 0.55,
    });

    if (data.floor) {
        fMapLayer2['f' + data.floor].textLayer.addMarker(tm);
        if (imgurl) {
            fMapLayer2['f' + data.floor].imgLayer.addMarker(im);
        }
    }
};

function setModelImage1(data, item) {
    var tm = new fengmap.FMTextMarker({
        name: data.name,
        x: data.x,
        y: data.y,
        //文字标注样式设置
        //文字标注样式设置
        fillcolor: "#252525", //填充色
        fontsize: 12, //字体大小
        strokecolor: "#fff", //边框色
        alpha: 1,   //文本标注透明度
        anchor: fengmap.FMMarkerAnchor.BOTTOM

    });

    // 图片标注
    var im = new fengmap.FMImageMarker({
        url: '../image/mapIcon/Exit3.png',
        size: 26,  //设置图片显示尺寸
        height: 1, //marker标注高度
        anchor: '4',
        x: data.x,
        y: data.y + 0.55,
    });

    if (data.floor) {
        fMapLayer2['f' + data.floor].textLayer.addMarker(tm);
        fMapLayer2['f' + data.floor].imgLayer.addMarker(im);
    }
};

function setModelImage2(data, item) {
    var tm = new fengmap.FMTextMarker({
        name: data.name,
        x: data.x,
        y: data.y,
        //文字标注样式设置
        //文字标注样式设置
        fillcolor: "#252525", //填充色
        fontsize: 12, //字体大小
        strokecolor: "#fff", //边框色
        alpha: 1,   //文本标注透明度
        anchor: fengmap.FMMarkerAnchor.BOTTOM

    });

    /* 设置图标 */
    let imgurl = '';
    if (item.iconType == '100004') {
        /* 男洗手间 */
        imgurl = '../image/mapIcon/icon_wc_1.png'
    } else if (item.iconType == '100005') {
        /* 女洗手间 */
        imgurl = '../image/mapIcon/icon_wc_2.png'
    } else if (item.iconType == '100003') {
        /* 母婴室 */
        imgurl = '../image/mapIcon/icon_wc_4.png'
    } else if (item.iconType == '800008') {
        /* 护士站 */
        imgurl = '../image/mapIcon/icon_wc_5.png'
    } else if (item.iconType == '110002') {
        /* 安全出口 */
        imgurl = '../image/mapIcon/Exit4.png'
    };

    // 图片标注
    var im = new fengmap.FMImageMarker({
        url: imgurl,
        size: 26,  //设置图片显示尺寸
        height: 1, //marker标注高度
        anchor: '4',
        x: data.x,
        y: data.y + 0.55,
    });

    if (data.floor) {
        fMapLayer2['f' + data.floor].textLayer.addMarker(tm);
        if (imgurl) {
            fMapLayer2['f' + data.floor].imgLayer.addMarker(im);
        }
    }
};

function setModelImage3(data, item) {
    var tm = new fengmap.FMTextMarker({
        name: data.name,
        x: data.x,
        y: data.y,
        //文字标注样式设置
        //文字标注样式设置
        fillcolor: "#252525", //填充色
        fontsize: 12, //字体大小
        strokecolor: "#fff", //边框色
        alpha: 1,   //文本标注透明度
        anchor: fengmap.FMMarkerAnchor.BOTTOM

    });

    /* 设置图标 */
    let imgurl = '';
    if (item.photolocal2) {
        imgurl = url + item.photolocal2;
    }

    // 图片标注
    var im = new fengmap.FMImageMarker({
        url: imgurl,
        size: 26,  //设置图片显示尺寸
        height: 1, //marker标注高度
        anchor: '4',
        x: data.x,
        y: data.y + 0.55,
    });

    if (data.floor) {
        fMapLayer2['f' + data.floor].textLayer.addMarker(tm);
        if (imgurl) {
            fMapLayer2['f' + data.floor].imgLayer.addMarker(im);
        }
    }
};

function setModelImage4(data, item) {
    var tm = new fengmap.FMTextMarker({
        name: data.name,
        x: data.x,
        y: data.y,
        //文字标注样式设置
        //文字标注样式设置
        fillcolor: "#252525", //填充色
        fontsize: 12, //字体大小
        strokecolor: "#fff", //边框色
        alpha: 1,   //文本标注透明度
        anchor: fengmap.FMMarkerAnchor.BOTTOM
    });

    if (data.floor) {
        fMapLayer2['f' + data.floor].textLayer.addMarker(tm);
    }
};

function setModelImage5(data, item) {
    var tm = new fengmap.FMTextMarker({
        name: data.name,
        x: data.x,
        y: data.y,
        //文字标注样式设置
        //文字标注样式设置
        fillcolor: "#252525", //填充色
        fontsize: 12, //字体大小
        strokecolor: "#fff", //边框色
        alpha: 1,   //文本标注透明度
        anchor: fengmap.FMMarkerAnchor.BOTTOM

    });

    /* 设置图标 */
    let imgurl = '';
    if (item.type != 3) {
        imgurl = '../image/mapIcon/Exit1.png'
    } else {
        imgurl = '../image/mapIcon/Exit2.png'
    }

    // 图片标注
    var im = new fengmap.FMImageMarker({
        url: imgurl,
        size: 26,  //设置图片显示尺寸
        height: 1, //marker标注高度
        anchor: '4',
        x: data.x,
        y: data.y + 0.55,
    });

    if (data.floor) {
        fMapLayer2['f' + data.floor].textLayer.addMarker(tm);
        if (imgurl) {
            fMapLayer2['f' + data.floor].imgLayer.addMarker(im);
        }
    }
};

function getAllDataToMap(mapid) {
    /* 车位 */
    getPlaceData(mapid);

    /* 跨层通道 */
    crossLayerChannel(mapid);

    /* 电梯数据 */
    getApiElevatorData(mapid);

    /* 厕所-安全出口-建筑 */
    getApiServiceFacilitiesData(mapid);

    /* 商家 */
    getShangJiaData(mapid);

    /* 公司 */
    getCompanyData(mapid);

    /* 出入口 */
    getPlaceExit(mapid);

    /* vip区域 */
    getVipArea(mapid);
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

function setPlaceExitMarker() {
    if (placeExitData && placeExitData.length) {
        var floorList = fMap.groupIDs;
        var group = null;
        var gid;
        for (var j = 0; j < floorList.length; j++) {
            //获取所有楼层的layer
            gid = floorList[j];
            group = fMap.getFMGroup(gid);
            fMapLayer2['f' + gid] = {
                textLayer: group.getOrCreateLayer('textMarker'),
                imgLayer: group.getOrCreateLayer('imageMarker'),
            }
        }
        placeExitData.forEach(item => {
            if (item.fid) {
                let model = findModel({ FID: item.fid });
                if (model) {
                    setModelColor6(model, item)
                    let data = {
                        x: model.mapCoord.x,
                        y: model.mapCoord.y,
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
        var floorList = fMap.groupIDs;
        var group = null;
        var gid;
        for (var j = 0; j < floorList.length; j++) {
            //获取所有楼层的layer
            gid = floorList[j];
            group = fMap.getFMGroup(gid);
            fMapLayer2['f' + gid] = {
                textLayer: group.getOrCreateLayer('textMarker'),
                imgLayer: group.getOrCreateLayer('imageMarker'),
            }
        }
        crossLayerData.forEach(item => {
            if (item.fid) {
                let model = findModel({ FID: item.fid });
                if (model) {
                    setModelColor3(model, item)
                    let data = {
                        x: model.mapCoord.x,
                        y: model.mapCoord.y,
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
        var floorList = fMap.groupIDs;
        var group = null;
        var gid;
        for (var j = 0; j < floorList.length; j++) {
            //获取所有楼层的layer
            gid = floorList[j];
            group = fMap.getFMGroup(gid);
            fMapLayer2['f' + gid] = {
                textLayer: group.getOrCreateLayer('textMarker'),
                imgLayer: group.getOrCreateLayer('imageMarker'),
            }
        }
        serviceFacilitiesData.forEach(item => {
            if (item.fid) {
                let model = findModel({ FID: item.fid });
                if (model) {
                    setModelColor4(model, item)
                    let data = {
                        x: model.mapCoord.x,
                        y: model.mapCoord.y,
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
        var floorList = fMap.groupIDs;
        var group = null;
        var gid;
        for (var j = 0; j < floorList.length; j++) {
            //获取所有楼层的layer
            gid = floorList[j];
            group = fMap.getFMGroup(gid);
            fMapLayer2['f' + gid] = {
                textLayer: group.getOrCreateLayer('textMarker'),
                imgLayer: group.getOrCreateLayer('imageMarker'),
            }
        }
        shangjiaDataList.forEach(item => {
            if (item.fid) {
                let model = findModel({ FID: item.fid });
                if (model) {
                    setModelColor5(model, item)
                    let data = {
                        x: model.mapCoord.x,
                        y: model.mapCoord.y,
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
        var floorList = fMap.groupIDs;
        var group = null;
        var gid;
        for (var j = 0; j < floorList.length; j++) {
            //获取所有楼层的layer
            gid = floorList[j];
            group = fMap.getFMGroup(gid);
            fMapLayer2['f' + gid] = {
                textLayer: group.getOrCreateLayer('textMarker'),
                imgLayer: group.getOrCreateLayer('imageMarker'),
            }
        }
        elevatorData.forEach(item => {
            if (item.fid) {
                let model = findModel({ FID: item.fid });
                if (model) {
                    setModelColor2(model, item)
                    let data = {
                        x: model.mapCoord.x,
                        y: model.mapCoord.y,
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
        var floorList = fMap.groupIDs;
        var group = null;
        var gid;
        for (var j = 0; j < floorList.length; j++) {
            //获取所有楼层的layer
            gid = floorList[j];
            group = fMap.getFMGroup(gid);
            fMapLayer2['f' + gid] = {
                textLayer: group.getOrCreateLayer('textMarker'),
            }
        }
        companyDataList.forEach(item => {
            if (item.fid) {
                let model = findModel({ FID: item.fid });
                if (model) {
                    setModelColor5(model, item)
                    let data = {
                        x: model.mapCoord.x,
                        y: model.mapCoord.y,
                        floor: item.floor,
                        name: item.name
                    };
                    setModelImage4(data, item);
                } else {
                    let data = {
                        x: item.x,
                        y: item.y,
                        floor: item.floor,
                        name: item.name
                    };
                    setModelImage4(data, item);
                }
            } else {
                let data = {
                    x: item.x,
                    y: item.y,
                    floor: item.floor,
                    name: item.name
                };
                setModelImage4(data, item);
            }
        });
    }
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

function setVipAreaMarker(){
    if (vipAreaMapData && vipAreaMapData.length) {
        var floorList = fMap.groupIDs;
        var group = null;
        var gid;
        for (var j = 0; j < floorList.length; j++) {
            //获取所有楼层的layer
            gid = floorList[j];
            group = fMap.getFMGroup(gid);
            fMapLayer2['f' + gid] = {
                textLayer: group.getOrCreateLayer('textMarker'),
                imgLayer: group.getOrCreateLayer('imageMarker'),
            }
        }
        vipAreaMapData.forEach(item => {
            if (item.fid) {
                let model = findModel({ FID: item.fid });
                if (model) {
                    // setModelColor6(model, item)
                    let data = {
                        x: model.mapCoord.x,
                        y: model.mapCoord.y,
                        floor: item.floor,
                        name: item.bindingArea
                    };
                    setModelImage4(data, item);
                } else {
                    let data = {
                        x: item.x,
                        y: item.y,
                        floor: item.floor,
                        name: item.bindingArea
                    };
                    setModelImage4(data, item);
                }
            } else {
                let data = {
                    x: item.x,
                    y: item.y,
                    floor: item.floor,
                    name: item.bindingArea
                };
                setModelImage4(data, item);
            }
        });
    }
};
