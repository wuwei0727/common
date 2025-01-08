var ID;
var mapList = [];
var currentMapId;//初始化的id，不同则重新初始化地图


$(function () {
	calcHeig();
	initDate();
	$(document).on('click', function (e) {
		hideSele();
	});
	var id = getUrlStr('id');
	if (id) {
		ID = id;
		init();
		$('#titleFlag').html('编辑');
	} else {
		loadSeleData();
	}
})
//初始化数据
function init() {
	$.ajax({
		url: url + 'flc/getFloorLockInfoInfoById/' + ID,
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
				var data = res.data;
				mapData = data;
				setData(data, '.main');
				currentMapId = $('[name="map"]').data('val');
				if (data.map) {
					$('[name="map"]').eq(0).addClass('batchTxtChange')
				};
				if (data.validEndTime && data.validStartTime) {
					initDate(data.validStartTime + '至' + data.validEndTime);
				}
				loadSeleData();
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
};

function seleMap(that, id) {
	seleBatch(that, id);
};

//加载下拉数据
function loadSeleData(fn) {
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
};

//处理高度信息
function calcHeig() {
	var win = $(window);
	var fengmap = $('#fengmap');
	fengmap.css({
		height: win.height() - 40 - 40,
		width: win.width() - 40,
	})
}

function save() {
	var sendData = getData('.main');
	if (!sendData) {
		return;
	}
	if (!sendData.map) {
		tips('请选择关联地图');
		return;
	} else if (!sendData.callCode) {
		tips('请输入调用码');
		return;
	} else if (!sendData.dataInterface) {
		tips('请输入数据传输接口');
		return;
	} else if (!sendData.time) {
		tips('请选择有效期');
		return;
	}
	sendData.validStartTime = sendData.time.split('至')[0];
	sendData.validEndTime = sendData.time.split('至')[1];
	delete sendData.time;

	var path = url;
	if (ID) {
		path += 'flc/editFloorLockConfigInfo';
		sendData.id = ID;
	} else {
		path += 'flc/addFloorLockConfigInfo';
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
};

function initDate(appointmentSlot) {
	if (appointmentSlot) {
		$('#time').val(appointmentSlot);
	}
	jeDate('#time', {
		theme: {
			bgcolor: "#4A60CF",
			pnColor: "#4A60CF"
		},
		multiPane: false,
		range: "至",
		format: "YYYY-MM-DD hh:mm",
		donefun: function () {
		}
	});
}