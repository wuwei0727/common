var ID;
var mapList = [];
var currentMapId;//初始化的id，不同则重新初始化地图
//定义搜索分析类
var analyser = null;
//选中的模型
var selectedModel = null;
var mapData = {};
var site = null;
var flag = false;

$(function () {
	calcHeig();
	$(document).on('click', function (e) {
		hideSele();
	})
	loadSeleData();
	var id = getUrlStr('id');
	if (id) {
		ID = id;
		init();
		$('#titleFlag').html('编辑');
	}
})

//初始化数据
function init() {
	$.ajax({
		url: url + 'peb/getParkingElevatorBindingById/' + ID,
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
			} else if (res.code === 200) {
				var data = res.data[0];
				mapData = data;
				setData(data, '.main');
				currentMapId = $('[name="map"]').data('val');
				if (data.map) {
					$('[name="map"]').eq(0).addClass('batchTxtChange')
				};

				$('[name="objectType"]')[0].innerText = getObjectType(data.objectType);
				if (data.objectType) {
					$('[name="objectType"]').eq(0).addClass('batchTxtChange');
				};

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

function getObjectType(type) {
	if (type == '200005') {
		return '步行梯'
		/* 步行梯 */
	} else if (type == '340818') {
		return '手扶电梯'
		/* 手扶电梯 */
	} else if (type == '340855') {
		return '电梯前室'
		/* 电梯前室 */
	} else if (type == '200004') {
		return '直升电梯'
		/* 直升电梯 */
	} else {
		return '--请选择--'
	}
}

//加载下拉数据-关联地图
function loadSeleData() {
	$.ajax({
		url: url + 'map/getMap2dSel',
		data: {
			pageSize: -1,
			enable: 1,
			// companyId: companyId
		},
		success: function (res) {
			if (res.code !== 200) {
				tips(res.message);
				return;
			}
			mapList = [];
			var list = res.data;
			var html = '';
			// mapName = res.data[0].mapName;
			var first = list.find(item => item.id == currentMapId);
			// var first = list[0]
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

//保存
function save() {
	var sendData = getData('.main');
	var nameReg = /^[\u4e00-\u9fa5a-zA-Z0-9]+$/;
	if (!sendData) {
		return;
	}
	if (!nameReg.test(sendData.name)) {
		tips('名称只能包含汉字、英文或者数字！！！');
		return;
	}
	if (!sendData.map) {
		tips('请选择关联地图');
		return;
	}
	if (!sendData.x || !sendData.y) {
		tips('请在地图中选择模型，进行选点');
		return;
	}
	if (!sendData.objectType) {
		tips('请选择类型');
		return;
	}

	var path = url;
	if (ID) {
		path += 'peb/updateParkingElevatorBinding';
		sendData.id = +ID;
	} else {
		path += 'peb/addParkingElevatorBinding';

	}

	delete sendData.addTime;
	delete sendData.updateTime;
	sendData.map = +sendData.map
	sendData.floor = +sendData.floor;

	/* 对应加上图标类型 */
	if (sendData.objectType == '200005') {
		/* 步行梯 */
		sendData.iconType = '170001'
	} else if (sendData.objectType == '340818') {
		/* 手扶电梯 */
		sendData.iconType = '170003'
	} else if (sendData.objectType == '340855') {
		/* 电梯前室 */
		sendData.iconType = '170006'
	} else if (sendData.objectType == '200004') {
		/* 直升电梯 */
		sendData.iconType = ''
	};
	console.log("sendData", sendData);
	$.ajax({
		url: path,
		data: JSON.stringify(sendData),
		type: 'post',
		headers: {
			"Content-Type": "application/json;charset=UTF-8"
		},
		beforeSend: function () {
			loading();
		},
		complete: function () {
			removeLoad();
		},
		success: function (res) {
			if (res.code == 200) {
				tips(res.message, function () {
					if (ID) {
						init();
					} else {
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

//选点显示地图
function selectPoint() {
	var mapId = $('[name="map"]').data('val');
	if (!mapId) {
		tips('请选择关联地图');
		return;
	}
	$('#mapPop').show();
	$('#mask').show();
	var target = null;
	var floor = +$('[name="floor"]').val();
	if (!ID) {
		currentMapId = undefined;
	}
	if (currentMapId == mapId) {
		target = contrastReturn(mapList, 'id', mapId);
		openMap({
			fKey: target.mapKey,
			fName: target.appName,
			fId: target.fmapID,
			focusFloor: floor,
			path: target.path,
			//初始指北针的偏移量
			compassOffset: [20, 12],
			//指北针大小默认配置
			compassSize: 48,
			mapId
		}, function () {
			analyser = new fengmap.FMSearchAnalyser(fMap);
			site = addLayer({
				x: +mapData.x,
				y: +mapData.y,
				num: mapData.name,
				focusFloor: Number(floor)
			});
			if (mapData.x && mapData.y) {
				fMap.moveTo({
					x: +mapData.x,
					y: +mapData.y,
					groupID: +floor,
				})
			}
			var fid = $('[name="fid"]').val();
			var target = null;
			if (fid) {
				target = findModel({ FID: fid });
				if (target) {
					target.selected = true;
					selectedModel = target;
				}
			}
		});
	} else if (currentMapId != mapId) {
		target = contrastReturn(mapList, 'id', mapId);
		openMap({
			fKey: target.mapKey,
			fName: target.appName,
			fId: target.fmapID,
			path: target.path,
			//初始指北针的偏移量
			compassOffset: [20, 12],
			//指北针大小默认配置
			compassSize: 48,
			mapId
		}, function () {
			analyser = new fengmap.FMSearchAnalyser(fMap);
		});
		flag = true;
	}
}

//地图的回调
function mapClickFn(info, target) {
	// if (info.nodeType !== fengmap.FMNodeType.MODEL) {
	//     tips('请选择模型');
	//     return;
	// } else {
	//     tips('已选择地点');
	// }
	tips('已选择地点');
	var mapId = $('[name="map"]').data('val');
	if (currentMapId != mapId) {
		if (flag) {
			site = addLayer({
				x: +info.x,
				y: +info.y,
				num: ID ? mapData.name : '',
				focusFloor: Number(info.floor)
			});
		} else {
			addLayer({
				x: +info.x,
				y: +info.y,
				num: ID ? mapData.name : '',
				focusFloor: Number(info.floor)
			}, site);
		}

		if (!ID) {
			currentMapId = mapId;
		}

	} else {
		addLayer({
			x: +info.x,
			y: +info.y,
			num: ID ? mapData.name : '',
			focusFloor: Number(info.floor)
		}, site);
	}
	flag = false;
	$('[name="fid"]').val(info.fid || '');
	$('[name="x"]').val(info.x);
	$('[name="y"]').val(info.y);
	// $('[name="z"]').val(info.z);
	$('[name="floor"]').val(info.floor);
	if (selectedModel) {
		selectedModel.selected = false;
	}
	//染色
	target.selected = true;
	selectedModel = target;
}

/* 确定 */
function mapConfirm() {
    hidePop('#mapPop')
};

/* 取消 */
function mapCancel(){
    hidePop('#mapPop');
    tips('已取消选择的地点');
    $('[name="x"]').val('');
    $('[name="y"]').val('');
    $('[name="floor"]').val('');
    $('[name="fid"]').val('');
};

//处理高度信息
function calcHeig() {
	var win = $(window);
	var fengmap = $('#fengmap');
	fengmap.css({
		height: win.height() - 40 - 52,
		width: win.width() - 40,
	})
}

//地图切换  
function seleMap(that, id) {
	seleBatch(that, id, function () {
		if (currentMapId != id) {
			// $('[name="fid"]').val('');
			// $('[name="x"]').val('');
			// $('[name="y"]').val('');
			// $('[name="z"]').val('');
			// $('[name="floor"]').val('1');
			$('[name="map"]').val(id);
		} else {
			// $('[name="fid"]').val(mapData.fid);
			// $('[name="x"]').val(mapData.x);
			// $('[name="y"]').val(mapData.y);
			// $('[name="z"]').val(mapData.z);
			// $('[name="floor"]').val(mapData.floor);
			$('[name="map"]').val(id);
		}
	});
}
