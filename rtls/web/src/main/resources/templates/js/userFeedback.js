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
        url: url + 'feedback/getFeedbackInfo',
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
                        html += resTabCheck(target, lineNum + i, 'id', 'placeName');
                    } else if (name === 'operating') {
                        var deleteBtn = document.getElementById("deleteBtn");
                        var delteTxt = deleteBtn ? '<span class="tabOper" onclick="showDele(\'' + target.id + '\',\'' + target.placeName + '\')">删除</span>' : '';
                        html = html + '<td>' + delteTxt + '</td>';
                    } else if (name === 'content') {
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
        url: url + 'feedback/delFeedback/' + ids,
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