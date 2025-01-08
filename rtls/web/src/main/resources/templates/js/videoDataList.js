var where = {};
var ids;
var list = [];

$(function () {
	$(document).on('click', function (e) {
		hideSele();
	})
	where.pageIndex = 1;
	where.pageSize = pageSize;
	loadSeleData();

	init();
});

// 加载下拉数据
function loadSeleData() {
	loadFun('map/getMap2dSel', { pageSize: -1, enable: 1 }, '#mapSelect');
};

// 初始化列表
function init() {
	$.ajax({
		url: url + 'api/cameras/getAllOrFilteredCameras',
		data: where,
		type: 'get',
		beforeSend: function () {
			loading();
		},
		complete: function () {
			removeLoad();
		},
		success: function (res) {
			var tab = $('#tab');
			if (res.code !== 200) {
				tableException(tab, res.message);
				return;
			}
			var data = res.data;
			list = data.list;
			var len = list.length;
			if (!len) {
				tableException(tab, '当前搜索结果为空');
			}

			//全选按钮（取消）
			var allChe = $('#allCheck');
			if (allChe.hasClass('curSele')) {
				allChe.removeClass('curSele');
			}
			//全选按钮（取消）

			var allChe = $('#allCheck');
			if (allChe.prop('checked')) {
				allChe.prop('checked', false);
			}
			if (pageIndex != data.pageIndex) {
				pageIndex = data.pageIndex;
			}
			var allName = getTheadName(tab.find('thead'));

			var html = '';
			var target = null;
			var name = null;
			var value = null;
			var lineNum = (pageIndex - 1) * pageSize + 1;
			for (var i = 0; i < len; i++) {
				target = list[i];
				html += '<tr>';
				for (var j = 0; j < allName.length; j++) {
					name = allName[j];
					if (name === '') {
						continue;
					}
					value = target[name];
					if (name === 'line') {
						html += resTabCheck(target, lineNum + i, 'cameraId', 'name');
					} else if (name === 'operating') {
						var editBtn = document.getElementById("editBtn");
						var deleteBtn = document.getElementById("deleteBtn");
						var editTxt = editBtn ? '<span class="tabOper" onclick="detail(\'' + target.cameraId + '\')">编辑</span>' : '';
						var delteTxt = deleteBtn ? '<span class="tabOper deleOpa" onclick="showDele(\'' + target.cameraId + '\',\'' + target.name + '\')">删除</span>' : '';
						var posTxt = '<span class="tabOper deleOpa" onclick="showlocation(\'' + target.cameraId + '\',\'' + target.name + '\',)">位置</span>';
						html = html + '<td>' + editTxt + delteTxt + posTxt + '</td>';
					} else if (name === 'networkState') {
						if (value == '0') {
							html += '<td class="stateNot">离线</td>';
						} else if (value == '1') {
							html += '<td class="stateHas">在线</td>';
						}
					} else if(name === 'placeList'){
						html += `<td>
							<div class="cheweitd2">${convertNull(value)}</div>
						</td>`;
					} else {
						html += '<td>' + convertNull(value) + '</td>';
					}
				}
				html += '</tr>';
			}
			tab.find('tbody').html(html);
			var htmlStr = "共 <span class='c4A60CF'>" + data.pages + " </span> 页 / <span class='c4A60CF'>" + data.total + " </span>条数据"
			$('[id="total"]').html(htmlStr);
			//生成页码
			initDataPage(pageIndex, data.pages, data.total);
		},
		error: function (jqXHR) {
			resError(jqXHR);
		}
	})
};

// 单行的删除提示
function showDele(id, txt) {
	ids = id;
	$('#deleTxt').text(txt);
	showPop('#delePop');
};

// 多行的删除
function showAllDele() {
	var cheInp = $('#tab tbody').find('input:checked');
	if (!cheInp.length) {
		tips('请选择至少一条数据');
		return;
	}
	var showTxt = '';
	var cheId = '';
	for (var i = 0; i < cheInp.length; i++) {
		showTxt += cheInp[i].getAttribute('data-txt') + '、';
		cheId += cheInp[i].value + ',';
	}
	showTxt = showTxt.slice(0, -1);
	ids = cheId.slice(0, -1);
	$('#deleTxt').html(showTxt);
	showPop('#delePop');
};

// 确认删除
function entDele() {
	$.ajax({
		url: url + 'api/cameras/deleteCameraById/' + ids,
		type: 'DELETE',
		beforeSend: function () {
			loading();
		},
		complete: function () {
			removeLoad();
		},
		success: function (res) {
			tips(res.message);
			if (res.code == 200) {
				hidePop('#delePop');
				search();
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

// 翻页
function turnPage() {
	where.pageIndex = pageIndex;
	init();
};

// 编辑
function detail(id) {
	$('#editFrame').attr('style', 'z-index: 99')
	$('#editFrame').attr('src', './videoDataDetails.html?id=' + id)
	$('#editFrame').show()
};

// 增加
function addNewData() {
	$('#addFrame').attr('style', 'z-index: 99')
	$('#addFrame').attr('src', './videoDataDetails.html')
	$('#addFrame').show()
};

function closeFrame() {
	$('#addFrame').hide()
	$('#editFrame').hide()
	search()
};

var polygonColor = ['#d71618', '#ff9600', '#009944'];

// 位置显示商家信息的
function showlocation(id) {
	var idmai = id
	$('#mapPop').show();
	$('#mask').show();

	let result = contrastReturn(list, 'cameraId', idmai);
	console.log('aaa', result);
	
	let initPoints = [];
	if (result.vertexInfo) {
		result.vertexInfo.forEach(item => {
			let obj = {};
			let tempPoinArr = [];
			item.points.forEach(subItem => {
				obj = { x: subItem.x, y: subItem.y, floor: +subItem.floor };
				tempPoinArr.push(obj);
			});
			initPoints.push(tempPoinArr);
		});
	}	

	openMap({
		fKey: result.mapKey || '',
		fName: result.appName || '',
		fId: result.fmapID || '',
		focusFloor: Number(result.floor) || '',
		path: result.themeImg || '',
		x: result.x,
		y: result.y,
		num: result.name,
		//初始指北针的偏移量
		compassOffset: [20, 12],
		//指北针大小默认配置
		compassSize: 48,
		mapId: result.map,

		FMViewMode: fengmap.FMViewMode.MODE_2D,
		themeName: result.themeName || '',

		polygonColor: polygonColor[0],
		initPoints: initPoints,
		initCameraArea: result.cameraVertexInfo,
		cArea: 'show',
		wantToDo: "all",
		noView: true,
	}, function () {
		addLayer({
			x: +result.x,
			y: +result.y,
			floor: Number(+result.floor)
		});
		fMap.setLevel({
			level: +result.floor

		})
		fMap.setCenter({
			x: +result.x,
			y: +result.y,
			animate: true
		})

		addCircleLayer({
			x: +result.x,
			y: +result.y,
			floor: Number(+result.floor),
			r: +result.radius,
		})
	});
};