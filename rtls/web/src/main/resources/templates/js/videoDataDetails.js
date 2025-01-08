var recommId;
var ID;
var areaId;
var mapList = [];
var currentMapId;//初始化的id，不同则重新初始化地图
//定义搜索分析类
var analyser = null;
//选中的模型
var selectedModel = null;
var placeList = null;
var hasPlace = [];//现有车位
var addPlace = [];//新增车位
var delPlace = [];//删除的车位
var fids = [];
var comData = {};
var map;//地图id
var mapId;
let mapName;
let desc;
var initData;
var placeNameList = [];
var allCheck = false;
var vertexInfo = [];
var polygonColor = ['#d71618', '#ff9600', '#009944'];
var initVertex = [];
var initVertexInfo = [];
var initPlaceNameList = [];

var initCameraVertexInfo;
var cameraVertexInfo;

$(function () {
	$(document).on('click', function (e) {
		hideSele();
	})
	var id = getUrlStr('id');
	if (id) {
		recommId = id;
		ID = id;
		initMap(() => {
			init();
		});
		$('#titleFlag').html('编辑');
	} else {
		initMap();
	}

	$("#r").on("change", function () {
		var val = $(this).val();

		if (val && pointCricle) {
			let info = {};
			info.x = $("[name='x']").val();
			info.y = $("[name='y']").val();
			info.floor = $("[name='floor']").val();
			info.r = val;
			addCircleLayer(info);
		}
	})
});

//初始化地图
function initMap(callback) {
	$.ajax({
		url: url + 'map/getMap2dSel',
		data: {
			pageSize: -1,
			enable: 1,
		},
		success: function (res) {
			if (res.code !== 200) {
				tips(res.message);
				return;
			}
			callback && callback();
			mapList = [];
			var list = res.data;
			var html = '';
			var first = list.find(item => item.id == currentMapId);
			var target = null;
			for (var i = 0; i < list.length; i++) {
				target = list[i];
				mapList.push({
					id: target.id,
					type: target.type,
					fmapID: target.fmapID || '',
					appName: target.appName || '',
					mapKey: target.mapKey || '',
					path: target.themeImg || '',
					mapName: target.name || ''
				});
				html += '<div onclick="seleMap(this,\'' + list[i].id + '\')">' + list[i].name + '</div>';
			}
			var mapSelect = $('#mapSelect');
			mapSelect.html(html);
			if (first) {
				mapSelect.prev().html(first.name);
				seleMap(null, first.id);
			}
		},
		error: function (jqXHR) {
			resError(jqXHR);
		}
	})
}

//地图切换
function seleMap(that, id) {
	seleBatch(that, id, function () {
		var resInfo = contrastReturn(mapList, 'id', +id);
		if (!resInfo) {
			tips('获取失败，请重试');
			return;
		}
		if (recommId) {
			if (resInfo.mapName !== mapName) {
				initPoints = [];
				placeNameList = [];
				vertexInfo = [];
				$('#recomPlaceList').html('');
			} else {
				initPoints = initVertex;
				placeNameList = initPlaceNameList;
				vertexInfo = initVertexInfo;
				cameraVertexInfo = initCameraVertexInfo
				var html = '';
				placeNameList.forEach(item => {
					html += '<span class="recomPlaceItem" onclick="checkRecomPlace(\'' + item.name + '\')"><span class="recomPlaceItemCheck" value="' + item.name + '">√</span><span>' + item.name + '</span></span>';
				});
				$('#recomPlaceList').html(html);
			}
		} else {
			initPoints = [];
			placeNameList = [];
			vertexInfo = [];
			$('#recomPlaceList').html('');
		}
		$('[name="map"]').val(id);

		if (recommId) {
			if (mapId != id) {
				openMap({
					fKey: resInfo.mapKey || '',
					fName: resInfo.appName || '',
					fId: resInfo.fmapID || '',
					focusFloor: Number(resInfo.floor) || '',
					path: resInfo.path || '',
					FMViewMode: fengmap.FMViewMode.MODE_2D,
					themeName: resInfo.themeName || '',
					//初始指北针的偏移量
					compassOffset: [20, 12],
					//指北针大小默认配置
					compassSize: 48,
					polygonColor: polygonColor[0],
					initPoints: initPoints,
					wantToDo: "all",
					mapId: id,
					noView: false,
				});
				$("[name='x']").val('');
				$("[name='y']").val('');
				$("[name='floor']").val('');
				$("[name='fid']").val('');
			} else {
				openMap({
					fKey: resInfo.mapKey || '',
					fName: resInfo.appName || '',
					fId: resInfo.fmapID || '',
					focusFloor: Number(resInfo.floor) || '',
					path: resInfo.path || '',
					FMViewMode: fengmap.FMViewMode.MODE_2D,
					themeName: resInfo.themeName || '',
					//初始指北针的偏移量
					compassOffset: [20, 12],
					//指北针大小默认配置
					compassSize: 48,
					polygonColor: polygonColor[0],
					initPoints: initPoints,

					initCameraArea: cameraVertexInfo,
					cArea: 'add',

					wantToDo: "all",
					mapId: id,
					noView: true,
				}, function () {
					addLayer({
						x: +initData.x,
						y: +initData.y,
						floor: Number(+initData.floor)
					});
					fMap.setLevel({
						level: +initData.floor

					})
					fMap.setCenter({
						x: +initData.x,
						y: +initData.y,
						animate: true
					})

					addCircleLayer({
						x: +initData.x,
						y: +initData.y,
						floor: Number(+initData.floor),
						r: +initData.radius,
					})
				});
			}
		} else {
			openMap({
				fKey: resInfo.mapKey || '',
				fName: resInfo.appName || '',
				fId: resInfo.fmapID || '',
				focusFloor: Number(resInfo.floor) || '',
				path: resInfo.path || '',
				FMViewMode: fengmap.FMViewMode.MODE_2D,
				themeName: resInfo.themeName || '',
				//初始指北针的偏移量
				compassOffset: [20, 12],
				//指北针大小默认配置
				compassSize: 48,
				polygonColor: polygonColor[0],
				initPoints: initPoints,
				wantToDo: "all",
				mapId: id,
				noView: false,
			});
		}
	});
}

//初始化数据
function init() {
	$.ajax({
		url: url + 'api/cameras/getCameraById/' + recommId,
		beforeSend: function () {
			loading();
		},
		complete: function () {
			removeLoad();
		},
		success: function (res) {
			if (!res.code) {
				let newWindow = window.open('about:blank');
				newWindow.document.write(res);
				newWindow.focus();
				window.history.go(-1);
			} else if (res.code == 200) {
				var data = res.data;
				initData = data;
				setData(data, '.main');
				mapId = data.map;
				mapName = data.mapName;
				areaId = data.areaId;
				if (data.map) {
					$('[name="map"]').eq(0).addClass('batchTxtChange');
				}
				if (data.networkState || data.networkState == 0) {
					let txt = ''
					$('[name="networkState"]').eq(0).addClass('batchTxtChange');
					if (data.networkState == 0) {
						txt = '离线';
					} else if (data.networkState == 1) {
						txt = '在线';
					}
					$('[name="networkState"]')[0].innerText = txt;
				}

				if (data.cameraVertexInfo) {
					initCameraVertexInfo = data.cameraVertexInfo
					cameraAreaData = JSON.parse(data.cameraVertexInfo);
				}

				if (data.placeList) {
					var arr = data.placeList.split(',');
					arr.forEach(item => {
						placeNameList.push({ name: item, check: false });
					});
					var html = '';
					placeNameList.forEach(item => {
						html += '<span class="recomPlaceItem" onclick="checkRecomPlace(\'' + item.name + '\')"><span class="recomPlaceItemCheck" value="' + item.name + '">√</span><span>' + item.name + '</span></span>';
					});
					$('#recomPlaceList').html(html);
					initPlaceNameList = placeNameList;
				}
				var initPoints = [];
				if (data.vertexInfo) {
					data.vertexInfo.forEach(item => {
						let obj = {}, obj1 = {}, tempPointObj = {};
						let tempPoinArr = [], tempPoinArr1 = [];
						item.points.forEach(subItem => {
							obj = { x: subItem.x, y: subItem.y, floor: +subItem.floor };
							obj1 = { x: subItem.x, y: subItem.y };
							tempPoinArr.push(obj);
							tempPoinArr1.push(obj1);
							tempPointObj = { floor: +subItem.floor, points: tempPoinArr1 };
						});
						initPoints.push(tempPoinArr);
						vertexInfo.push(tempPointObj);
					});
					initVertex = initPoints;
					initVertexInfo = vertexInfo;
					let first = mapList.find(item => item.id == mapId);
					if (first) {
						$('#mapSelect').prev().html(first.name);
						seleMap(null, first.id, initPoints);
					}
				}
			}
			else {
				tips(res.message);
			}
		},
		error: function (jqXHR) {
			resError(jqXHR);
		}
	})
}

//保存
function save() {
	var sendData = getData('.main');
	console.log(sendData);

	if (!sendData) {
		return;
	}
	var placeList = '';
	placeNameList.forEach(item => {
		placeList === '' ? placeList += item.name : placeList += "," + item.name
	});
	sendData.placeList = placeList;
	vertexInfo.forEach(item => {
		delete item.pm;
	});
	sendData.vertexInfo = vertexInfo;

	sendData.cameraVertexInfo = JSON.stringify(cameraAreaData);

	if (!sendData.serialNumber) {
		tips('请输入摄像头ID')
		return;
	}
	if (!sendData.name) {
		tips('请输入摄像头名称')
		return;
	}
	if(!sendData.areaName) {
		tips('请输入区域名称')
		return;
	}
	if (!sendData.radius) {
		tips('请输入摄像头半径')
		return;
	}
	if (!sendData.map) {
		tips('请选择关联地图');
		return;
	}
	if (!sendData.x) {
		tips('请选择摄像头位置')
		return;
	}
	// if (sendData.vertexInfo.length === 0) {
	// 	tips('请在地图上选择区域-绑定车位');
	// 	return;
	// }

	if (!sendData.cameraVertexInfo || sendData.cameraVertexInfo == '[]') {
		tips('请在地图上选择摄像头区域');
		return;
	}

	console.log('sendData', sendData);
	

	var path = url;
	if (recommId) {
		path += 'api/cameras/updateCamera';
		sendData.id = recommId;
		sendData.areaId = areaId;
	} else {
		path += 'api/cameras/addCamera';
	}
	$.ajax({
		url: path,
		data: JSON.stringify(sendData),
		contentType: "application/json",
		type: 'post',
		beforeSend: function () {
			loading();
		},
		complete: function () {
			removeLoad();
		},
		success: function (res, jqXHR) {
			if (res.code == 200) {
				tips(res.message, function () {
					if (!recommId) {
						location.reload();
					}
				}, 1000);
			} else {
				tips(res.message);
			}
		},
		error: function (jqXHR) {
			var status = jqXHR.status;
			if (status == 401) {
				document.write(jqXHR.responseText)
			}
			resError(jqXHR);
		}
	})
}
function addRecomPlace(place, mapPointsArr) {
	mapPointsArr ? vertexInfo = mapPointsArr : '';
	var newPlaceName = [...place, ...placeNameList];
	var obj = {};
	placeNameList = newPlaceName.reduce(function (item, next) {
		obj[next.name] ? "" : obj[next.name] = true && item.push(next);
		return item;
	}, []);
	var html = '';
	placeNameList.forEach(item => {
		html += '<span class="recomPlaceItem" onclick="checkRecomPlace(\'' + item.name + '\')"><span class="recomPlaceItemCheck" value="' + item.name + '">√</span><span>' + item.name + '</span></span>';
	});
	$('#recomPlaceList').html(html);
}
function delRecomPlace(place, mapPointsArr) {
	vertexInfo = mapPointsArr;
	var html = '';
	placeNameList = placeNameList.filter(item => !place.some(subItem => subItem.name === item.name));
	placeNameList.forEach(item => html += '<span class="recomPlaceItem"  onclick="checkRecomPlace(\'' + item.name + '\')"><span class="recomPlaceItemCheck"  value="' + item.name + '">√</span><span>' + item.name + '</span></span>');
	$('#recomPlaceList').html(html);
}
function checkRecomPlace(itemName) {
	var findItem = placeNameList.find(item => item.name === itemName);
	if (findItem) {
		findItem.check = !findItem.check;
		findItem.check ? $('[value="' + itemName + '"]').addClass('recomPlaceItemChecked') : $('[value="' + itemName + '"]').removeClass('recomPlaceItemChecked');
	}
}
function allcheckPlace() {
	allCheck = !allCheck;
	allCheck ? $(".seleAllPlaceLabel").eq(0).addClass('seleAllPlaceCheck') : $(".seleAllPlaceLabel").eq(0).removeClass('seleAllPlaceCheck');
	placeNameList.forEach(item => {
		item.check = allCheck;
		allCheck ? $('[value="' + item.name + '"]').addClass('recomPlaceItemChecked') : $('[value="' + item.name + '"]').removeClass('recomPlaceItemChecked');
	});
}
function delCheckPlace() {
	var list = placeNameList.filter(item => item.check);

	let allLen = placeNameList.length;
	let seleLen = list.length;

	if (allLen == seleLen) {
		mapArr.forEach((item) => {
			item.pm.remove();
		})
		mapArr = [];
		vertexInfo = [];
		placeNameList = [];
	};
	$(".seleAllPlaceLabel").eq(0).removeClass('seleAllPlaceCheck');

	delRecomPlace(list, vertexInfo);
}
function reload() {
	location.reload()
};

/* 选点 */
function selectPoint1() {
	CLICKTYPE = 'selectPoint';
	drawOpetion('del2');
};

/* 选点回调 */
function mapClickFn(info) {
	$('[name="x"]').val(info.x);
	$('[name="y"]').val(info.y);
	$('[name="floor"]').val(info.floor);
	$('[name="fid"]').val(info.fid);
	let r = $('[name="radius"]').val();
	if (!r) {
		tips('请先输入半径');
		return;
	};
	info['r'] = r;

	addCircleLayer(info)
};

/* 框选 */
function drawOpetion1(type) {
	if (type == 'vpolygon' && cameraAreaId) {
		tips('只能设置一个摄像头区域');
		return;
	}
	CLICKTYPE = 'drawArea';
	drawOpetion(type);
};

var cameraAreaData = [];
var cameraAreaId = '';
/* 摄像头的区域 */
function initCameraArea(points, level, id) {
	cameraAreaId = id;
	points.forEach((item) => {
		item.floor = level;
	})
	cameraAreaData = points;
};

function addCameraAreaId(id) {
	cameraAreaId = id;
}