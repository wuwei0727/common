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
    loadFun('map/getMap2dSel', { pageSize: -1, enable: 1 }, '#mapSelect1');
};

// 初始化列表
function init() {
    $.ajax({
        url: url + 'qrCodeLocation/getAllQrCodeLocationOrConditionQuery',
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
                        html += resTabCheck(target, lineNum + i, 'id', 'areaName');
                    } else if (name === 'operating') {
                        var editBtn = document.getElementById("editBtn");
                        var deleteBtn = document.getElementById("deleteBtn");
                        var qrcodeBtn = document.getElementById("qrcodeBtn");

                        var editTxt = editBtn ? '<span class="tabOper" onclick="detail(\'' + target.id + '\')">编辑</span>' : '';
                        var delteTxt = deleteBtn ? '<span class="tabOper deleOpa" onclick="showDele(\'' + target.id + '\',\'' + target.areaName + '\')">删除</span>' : '';
                        var downTxt = qrcodeBtn ? '<span class="tabOper deleOpa" onclick="download(\'' + target.id + '\')">二维码</span>' : '';
                        var posTxt = '<span class="tabOper deleOpa" onclick="showlocation(\'' + target.id + '\',\'' + target.name + '\',)">位置</span>';

                        html = html + '<td>' + editTxt + delteTxt + posTxt + downTxt + '</td>';
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
        url: url + 'qrCodeLocation//delQrCodeLocation/' + ids,
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

//编辑
function detail(id) {
    $('#editFrame').attr('style', 'z-index: 99')
    $('#editFrame').attr('src', './locaMarkerDetails.html?id=' + id)
    $('#editFrame').show()
}
//增加
function add() {
    $('#addFrame').attr('style', 'z-index: 99')
    $('#addFrame').attr('src', './locaMarkerDetails.html')
    $('#addFrame').show()
};
//二维码下载
function download(id) {
    $('#codeFrame').attr('style', 'z-index: 99')
    $('#codeFrame').attr('src', './locaMarkerCode.html?id=' + id)
    $('#codeFrame').show()
};
//关闭页面
function closeFrame() {
    $('#addFrame').hide()
    $('#editFrame').hide()
    $('#codeFrame').hide()
    search()
};

var polygonColor = ['#d71618', '#ff9600', '#009944'];

// 位置显示商家信息的
function showlocation(id) {
    var idmai = id
    $('#mapPop').show();
    $('#mask').show();

    let result = contrastReturn(list, 'id', idmai);

    let initPoints = [];
    let data = JSON.parse(result.areaInfo);
    let info = calculateCentroid(data);
    console.log('info', info);


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
        initCameraArea: result.areaInfo,
        cArea: 'show',
        wantToDo: "all",
        noView: true,
    }, function () {
        addLayer({
            x: +info.x,
            y: +info.y,
            floor: Number(+data[0].floor)
        });
        fMap.setLevel({
            level: +data[0].floor

        })
        fMap.setCenter({
            x: +info.x,
            y: +info.y,
            animate: true
        })
    });
};

/* 计算多边形的中心点 */
function calculateCentroid(points) {
    let sumX = 0;
    let sumY = 0;
    let numPoints = points.length;

    for (let i = 0; i < numPoints; i++) {
        sumX += points[i].x;
        sumY += points[i].y;
    }

    // 计算平均值
    let centerX = sumX / numPoints;
    let centerY = sumY / numPoints;

    return { x: centerX, y: centerY };
};

function exportShow() {
    showPop('#permissPop');
};

function save() {
    /* 导出 */
    var sendData = getData('.popCen');
    var mapId = sendData.map;
    if (!mapId) {
        tips('请先选择地图');
        return;
    }
    loading();
    var xhr = new XMLHttpRequest();
    xhr.open('POST', url + 'importExportDeviceInfo/exportQrCodeLocationExcel/' + mapId);
    xhr.onreadystatechange = function () {
        if (xhr.readyState === XMLHttpRequest.OPENED || xhr.readyState === XMLHttpRequest.HEADERS_RECEIVED) {
            if (xhr.status === 500) {
                xhr.responseType = 'text';
            } else if (xhr.status === 200) {
                xhr.responseType = 'blob';
            }
        }
    };
    xhr.onload = function () {
        if (this.status === 200) {
            removeLoad();
            hidePop('#permissPop');
            // 将二进制数据转换为 Blob 对象
            var blob = new Blob([this.response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8' });
            // 获取 Content-Disposition 头中的文件名
            var fileName = '';
            var disposition = xhr.getResponseHeader('Content-Disposition');
            console.log('disposition', disposition);

            if (disposition && disposition.indexOf('attachment') !== -1) {
                var filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
                var matches = filenameRegex.exec(disposition);
                if (matches != null && matches[1]) {
                    fileName = decodeURIComponent(matches[1].replace(/['"]/g, ''));
                }
            }
            // 创建 Object URL
            var url = URL.createObjectURL(blob);
            // 创建链接元素
            var link = document.createElement('a');
            link.href = url;
            link.download = fileName;
            // 添加链接到页面并执行下载
            document.body.appendChild(link);
            link.click();
            // 删除链接元素和 Object URL
            document.body.removeChild(link);
            URL.revokeObjectURL(url);
        } else {
            removeLoad();
            alert("导出Excel文件失败:" + xhr.responseText);
        }
    };
    console.log(this.statusText)
    xhr.onerror = function () {
        removeLoad();
        alert("导出Excel文件失败:" + this.responseText);
    }
    xhr.send();
};

//关闭弹窗
function closePop(target) {
    //恢复状态
    $('[name="cnamePop"]').val('');
    hidePop(target);
};