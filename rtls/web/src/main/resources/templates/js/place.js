var where = {};
var ids;//删除数据的ID
var list = {};
$(function () {
    $(document).on('click', function (e) {
        hideSele();
    })
    var params = getAllUrl();
    where.pageIndex = 1;
    where.pageSize = pageSize;
    where.desc;
    loadSeleData();
    var search = $('.search');
    if (params.mapId) {
        where.map = params.mapId;
        search.find('[name="map"]').html(params.mapName).data('val', params.mapId);
        $(search.find('[name="map"]')[0]).addClass('batchTxtChange');
    }
    if (params.state) {
        where.state = params.state;
        search.find('[name="state"]').html(params.state == 0 ? '空闲' : '已停').data('val', params.state);
        $(search.find('[name="state"]')[0]).addClass('batchTxtChange');
    }
    if (params.states) {

        where.charge = params.states;
        where.type = params.type;

        search.find('[name="charge"]').html(params.states == 0 ? '空闲' : '已使用').data('val', params.states);
        search.find('[name="type"]').html(params.type == 1 ? '充电桩车位' : '普通车位').data('val', params.type);
        $(search.find('[name="charge"]')[0]).addClass('batchTxtChange');
    }

    if (params.type) {
        where.type = params.type;
        search.find('[name="type"]').html(params.type == 1 ? '充电桩车位' : '普通车位').data('val', params.type);
        $(search.find('[name="type"]')[0]).addClass('batchTxtChange');
    }
    if (params.charge) {
        where.charge = params.charge;
        where.type = params.type;
        search.find('[name="state"]').html(params.state == 0 ? '空闲' : '已停').data('val', params.state);
        search.find('[name="charge"]').html(params.charge == 0 ? '空闲' : '已使用').data('val', params.charge);
        search.find('[name="type"]').html(
            params.type == 1 ? '充电桩车位' :
                params.type === 0 ? '普通车位' :
                    params.type === 2 ? '专属车位' :
                        params.type === 3 ? '无障碍车位' :
                            params.type === 4 ? '超宽车位' :
                                params.type === 5 ? '子母车位' :
                                    params.type === 6 ? '小型车位' : 'VIP车位').data('val', params.type);
        $(search.find('[name="charge"]')[0]).addClass('batchTxtChange');
    }
    init();
})
//加载下拉数据
function loadSeleData() {
    loadSele('park/getPlace', { pageIndex: 1, pageSize: 20 }, '#mapSelect', undefined, undefined, undefined, 1);
    loadFun('map/getMap2dSel', { pageSize: -1, enable: 1 }, '#mapSelect1');
}

function loadSele(path, where, dom, firstTxt, tarName, fn, a) {
    var temp = null;
    if (typeof firstTxt === 'object') {
        temp = firstTxt;
    } else {
        temp = {
            txt: firstTxt,
            tarName: tarName,
        }
    }
    $.ajax({
        url: url + path,
        data: where,
        type: 'post',
        success: function (res) {
            if (res.code != 200) {
                tips('获取选项失败');
                return;
            }
            var selData = JSON.stringify(res.data.mapComName).replace(/"/g, "'");
            var html = '';
            var curName = temp.tarName || 'mapName';
            var curId = temp.id || 'mapId';
            var curTxt = temp.txt || '--请选择关联地图--';
            if (typeof firstTxt !== "boolean") {
                // html = '<div onclick="seleBatch(this,\'\')" data-id="">' + curTxt + '</div>';
                html = '<div onclick="seleBatch(this,\'' + '' + '\',\'\', ' + '[]' + ',\'comNameList\', \'#companySelect\')" data-id="' + '' + '" title="' + curTxt + '">' + curTxt + '</div>';
            }
            var list = res.data.mapComName || [];
            for (var i = 0; i < list.length; i++) {
                html += '<div onclick="seleBatch(this,\'' + list[i][curId] + '\',\'\', ' + selData + ',\'comNameList\', \'#companySelect\')" data-id="' + list[i][curId] + '" title="' + list[i][curName] + '">' + list[i][curName] + '</div>';
            }
            $(dom).html(html);
            if (typeof fn === 'function') {
                fn(list);
            }
        },
        error: function (err) {
            resError(err);
        }
    })
}
//初始化列表
function init() {
    if (where.charge === '0' || where.charge === '1') {
        where.type = 1;
    }
    $.ajax({
        url: url + 'park/getPlace',
        data: where,
        type: 'post',
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
                        html += resTabCheck(target, lineNum + i, 'id', 'name');
                    } else if (name === 'operating') {
                        var editBtn = document.getElementById("editBtn");
                        var deleteBtn = document.getElementById("deleteBtn");
                        var editTxt = editBtn ? '<span class="tabOper" onclick="detail(\'' + target.id + '\')">编辑</span>' : '';
                        var delteTxt = deleteBtn ? '<span class="tabOper deleOpa" onclick="showDele(\'' + target.id + '\',\'' + target.name + '\')">删除</span>' : '';
                        var posTxt = '<span class="tabOper deleOpa" onclick="showlocation(\'' + target.id + '\',\'' + target.name + '\',)">位置</span>';
                        html = html + '<td>' + editTxt + delteTxt + posTxt + '</td>';
                    } else if (name === 'state') {
                        html += '<td>';
                        if (value === 0) {
                            html += '<span class="stateHas">空闲</span>';
                        } else if (value === 1) {
                            html += '<span class="stateNot">已停</span>';
                        } else if (value === 2) {
                            html += '<span class="reserve">已预约</span>';
                        } else if (value === 3) {
                            html += '<span>未检</span>';
                        }
                        html += '</td>';
                    } else if (name === 'type') {
                        html += '<td>';
                        if (value === 1) {
                            html += '充电车位';
                        } else if (value === 2) {
                            html += '专属车位';
                        } else if (value === 3) {
                            html += '无障碍车位';
                        } else if (value === 4) {
                            html += '超宽车位';
                        } else if (value === 5) {
                            html += '子母车位';
                        } else if (value === 6) {
                            html += '小型车位';
                        } else if (value === 7) {
                            html += 'VIP车位';
                        } else {
                            html += '普通车位';
                        }
                        html += '</td>';
                    } else if (name === 'charge') {
                        html += '<td>';
                        if (value === 1) {
                            html += '已使用';
                        } else if (value === 0) {
                            html += '空闲';
                        } else if (value === 3) {
                            html += '无'
                        } else {
                            html += '--'
                        }
                        html += '</td>';
                    } else if (name === 'carbittype') {
                        html += '<td>';
                        if (value === '0') {
                            html += '否'
                        } else if (value === '1') {
                            html += '是'
                        }
                        html += '</td>';
                    } else if (name === 'isReservable') {
                        html += '<td>';
                        if (value == '0') {
                            html += '否'
                        } else if (value == '1') {
                            html += '是'
                        }
                        html += '</td>';
                    } else if (name === 'configWay') {
                        html += '<td>';
                        if (value == '1') {
                            html += '超声检测器'
                        } else if (value == '2') {
                            html += '视频方式'
                        } else if (value == '3') {
                            html += '视频方式加超声检测器'
                        }
                        html += '</td>';
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
}

//单行的删除提示
function showDele(id, txt) {
    ids = id;
    $('#deleTxt').text(txt);
    showPop('#delePop');
}
//多行的删除
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
}
//确认删除
function entDele() {
    $.ajax({
        url: url + 'park/delPlace/' + ids,
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
}
//搜索
function search() {
    where.pageIndex = 1;
    if (where.desc) {
        var curSort = document.getElementById('curSort');
        curSort.className = 'tabSort';
        curSort.id = '';
        delete where.desc;
    }
    var searchItem = $('.search').find('[name]');
    var target = null;
    for (var i = 0; i < searchItem.length; i++) {
        target = searchItem[i];
        if (target.className.indexOf('batchTxt') !== -1) {
            //自定义的下拉
            where[target.getAttribute('name')] = $(target).data('val');
        } else {
            where[target.getAttribute('name')] = target.value;
        }
    }
    init();
}
//翻页
function turnPage() {
    where.pageIndex = pageIndex;
    init();
}
//编辑
function detail(id) {
    $('#editFrame').attr('style', 'z-index: 99')
    $('#editFrame').attr('src', './placeDetails.html?id=' + id)
    $('#editFrame').show()
}
//增加
function addNewData() {
    $('#addFrame').attr('style', 'z-index: 99')
    $('#addFrame').attr('src', './placeDetails.html')
    $('#addFrame').show()
};

function closeFrame() {
    $('#addFrame').hide()
    $('#editFrame').hide()
    search()
}

//表格排序
function tabSort(that, sortName) {
    switchSort(that, function (sortVal) {
        if (sortVal === 'desc') {
            where.desc = sortName + ' desc';
        } else {
            where.desc = sortName;
        }
        where.pageIndex = 1;
        init();
    })
}
// 重置
function reset() {
    $('#companySelect').html('<div onclick="seleBatch(this,\'\')" data-id="">--请先选择关联地图--</div>');
    resetSearch();
}

// 位置显示商家信息的
function showlocation(id) {
    var idmai = id
    $('#mapPop').show();
    $('#mask').show();

    result = contrastReturn(list, 'id', idmai);
    console.log('result', result);

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
        mapId: result.map
    }, function () {
        analyser = new fengmap.FMSearchAnalyser(fMap);
        var x = +$('[name="x"]').val();
        var y = +$('[name="y"]').val();
        if (x && y) {
            fMap.moveTo({
                x: x,
                y: y,
                groupID: floor,
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
}

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
    xhr.open('POST', url + 'park/exportFloorLockExcel/' + mapId);
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
            hidePop('#delePop');
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
}

//关闭弹窗
function closePop(target) {
    //恢复状态
    $('[name="cnamePop"]').val('');
    hidePop(target);
}