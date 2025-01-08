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
    url: url + 'maintenanceStaff/getAllOrFilteredMaintenanceStaff',
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
            html += resTabCheck(target, lineNum + i, 'id', 'name');
          } else if (name === 'operating') {
            var editBtn = document.getElementById("editBtn");
            var deleteBtn = document.getElementById("deleteBtn");
            var editTxt = editBtn ? '<span class="tabOper" onclick="detail(\'' + target.id + '\')">编辑</span>' : '';
            var delteTxt = deleteBtn ? '<span class="tabOper deleOpa" onclick="showDele(\'' + target.id + '\',\'' + target.name + '\')">删除</span>' : '';
            html = html + '<td>' + editTxt + delteTxt + '</td>';
          } else if (name === 'status') {
            if (value == '0') {
              html += '<td class="stateNot">禁用</td>';
            } else if (value == '1') {
              html += '<td class="stateHas">启用</td>';
            }
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
    url: url + 'maintenanceStaff/deleteMaintenanceStaffById/' + ids,
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
  $('#editFrame').attr('src', './devopsDetail.html?id=' + id)
  $('#editFrame').show()
};

// 增加
function add() {
  $('#addFrame').attr('style', 'z-index: 99')
  $('#addFrame').attr('src', './devopsDetail.html')
  $('#addFrame').show()
};

function closeFrame() {
  $('#addFrame').hide()
  $('#editFrame').hide()
  search()
};