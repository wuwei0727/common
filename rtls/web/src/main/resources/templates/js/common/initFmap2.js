var fMap = null;//fmap变量

// 导航对象
var navi = null;
//定义选中模型
var selectedModel = null;
var mapLoad = false;//地图加载是否成功
//初始化
function openMap(para,callback) {
    var focusFloor = para.focusFloor || 1;
    var mapURL = '';
    var themeURL = '';
    if(para.path){
        mapURL = url + para.path + '/';
        themeURL = url + 'theme/';
    }
    var options = {
        appName:para.fName,
        key:para.fKey,
        mapID: para.fId,
    	//必要，地图容器
        container: document.getElementById('fengmap'),
        //地图数据位置
        mapURL: mapURL,
        mapURLAbsolute:true,//false会自动拼接mapID目录层级
        //主题数据位置
        themeURL: themeURL,
        themeID:'4006',
        //聚焦楼层
        level: focusFloor,
        //显示楼层
        visibleLevels: [focusFloor],
        //默认视图
		viewMode: para.FMViewMode || fengmap.FMViewMode.MODE_3D,
        //楼层高度
        floorSpace:30,
        //点击不高亮
        highlightPicker:para.highlight || [],
    };
    //初始化地图对象
    if(fMap){
    	//销毁
    	fMap.dispose();
    	fMap = null;
        mapLoad = false;
    }

	fMap = new fengmap.FMMap(options);

    //地图加载完成事件
    fMap.on('loaded', function () {
        console.log('地图加载完成！');
        mapLoad = true;
        //加载楼层控件
        initToolbar();
        if(typeof fengmap.FMNavigation === 'function'){
            //需引入navi.min
            navi = new fengmap.FMNavigation({
                map:fMap,
            })
        }
        if(typeof callback === 'function'){
            callback();
        }
    });
    //地图点击事件
    fMap.on('click', function (event) {
        if(typeof mapClickFn != 'function'){
            return;
        }
        var target = event.targets[0];
        if(!target){
            console.log('点在地图外');
            return;
        }
        var coords = event.coords;
        //点击的回调 参2为全部信息
        mapClickFn({
            fid:target.FID,
            x:coords.x,
            y:coords.y,
            z:coords.z,
            level:target.level,
            //模型的中点
            cx:target.x,
            cy:target.y,
            cz:target.height,
        },event);
    });
}
//加载滚动型楼层切换控件
function initToolbar() {
    if(!fengmap.FMControlPosition){
        console.log('需引入fengmap.plugin.min.js的文件才能初始化楼层控件');
        return;
    }
    var scrollFloorCtlOpt = {
        position: fengmap.FMControlPosition.RIGHT_TOP,
        floorButtonCount: 5,
        offset: {
            x: -20,
            y: 62
        },
        viewModeControl: true,
        floorModeControl: true,
        needAllLayerBtn: true
    };
    scrollFloorControl = new fengmap.FMToolbar(scrollFloorCtlOpt);
    scrollFloorControl.addTo(fMap)
}
//覆盖蜂鸟的提示
// console.warn = tips;
//返回自定义几何体
function resExtrudeMarker(point,para){
    para = para || {};
    // 创建自定义拉伸几何体
    var extrudeMarker = new fengmap.FMExtrudeMarker({
        // 颜色
        color: para.color,
        // 透明度
        alpha: para.alpha || 0.4,
        // 设置高度
        height: para.height || 0,
        // 拉伸高度
        extrudeHeight: para.extrudeHeight || 3,
        // 拉伸几何体的坐标点集数组，内部会改变数据结构
        points: point,
        lineType:'full',
        // 边线颜色
        lineColor: para.color || '',
        // 边线透明
        lineAlpha: para.lineAlpha || 1.0
    });
    return extrudeMarker;
}
//返回文本
function resTextMarker(para){
	var tm = new fengmap.FMTextMarker({
        x: para.x,
        y: para.y,
        height:para.height || 1,
        name: para.name ||'',
        fillcolor: para.fillcolor || "",
        fontsize: para.fontsize || 16,
        strokecolor:para.strokecolor || ""
    });
    if(para.avoid === false){
        tm.avoid(false);
    }
    //tm.selfAttr = '自定义属性selfAttr';
    return tm;
}
//返回图片
function resImgMarker(para){
    var im = new fengmap.FMImageMarker({
        x: para.x,
        y: para.y,
        height: para.height || 1,
        //设置图片路径
        url:para.url,
        //设置图片显示尺寸
        size: para.size || 32,
        //锚点位置
        anchor:fengmap.FMMarkerAnchor.CENTER,
    });
    if(para.avoid === false){
        im.avoid(false);
    }
    return im;
}
//返回圆
function resCircleMarker(para){
    var cm = new fengmap.FMPolygonMarker({
        //设置颜色
        color: para.color || '#3CF9DF',
        //设置透明度
        alpha:para.alpha || .3,
        //设置边框线的宽度
        lineWidth: para.lineWidth || 1,
        //设置高度
        height:4,
        //多边形的坐标点集数组
        points: {
            //设置为圆形
            type: 'circle',
            //设置此形状的中心坐标
            center: {
                x:para.x,
                y:para.y
            },
            //设置半径
            radius: para.r || 2,
            //设置段数，默认为40段
            segments: 40
        }
    });
    return cm;
}
//地图要素控制
function toggleLayer(layerName) {
    if (layerName === 'EXTENT') {
        getLayerByType(fengmap.FMType.EXTENT_LAYER);
    }
    if (layerName === 'MODEL') {
        getLayerByType( fengmap.FMType.MODEL_LAYER);
    }
    if (layerName === 'LABEL') {
        getLayerByType(fengmap.FMType.LABEL_LAYER);
    }
    if (layerName === 'FACILITY') {
        getLayerByType(fengmap.FMType.FACILITY_LAYER);
    }
}

/* 工具方法，用于通过图层的类型，获取图层上的指定的 layer 对象 */
function getLayerByType(layerType) {
    var floorList = fMap.getLevels();
    var j;
    var layers = null;
    var layer = null;
    for(var i = 0;i < floorList.length;i++){
        layers = fMap.getFloor(floorList[i]).getLayers();
        for (j = 0; j < layers.length; j++) {
            layer = layers[j];
            if (layer.type === layerType) {
                layer.visible = !layer.visible;
                break;
            }
        }
    }
}

 /**
 * 设置model颜色、透明度、边线颜色
 * */
function setModelRender(model,color) {
    var modelColor = color || '#fcfb44'; //模型颜色
    var lineColor = '#3384fd'; //模型边线颜色
    var alpha = 1; //颜色透明度
    if (model && typeof model.setColor === 'function') {
        //修改模型颜色及透明度
        model.setColor(modelColor, alpha);
        //修改模型边线的颜色及透明度
        // model.setStrokeColor(lineColor, alpha);
    }
}
/**
 * 修改model恢复到主题中的设置
 * */
function setModelToDefault(model) {
    if (model && typeof model.resetColor === 'function') {
        //将模型的颜色及透明度恢复回主题中的设置
        model.resetColor();
    }
}
/**
 * 二维/三维模式切换
 * fengmap.FMViewMode FMViewMode 模型视图模式枚举
 * */
function changeMode(domObj) {
    if(!fMap){
       tips('地图加载失败，请重试');
       return;
    }
    var viewMode = fMap.getViewMode();
    if (viewMode === fengmap.FMViewMode.MODE_3D) {
        //切换地图为二维模式
        fMap.setViewMode({
            mode:fengmap.FMViewMode.MODE_2D,
        });
        $(domObj).addClass('viewMode2D');
    } else {
        //切换地图为三维模式
        fMap.setViewMode({
            mode:fengmap.FMViewMode.MODE_3D,
        });
        $(domObj).removeClass('viewMode2D');
    }
}