var where = {};
var list = [];
var mapList = [];

$(function () {
  $(document).on('click', function (e) {
    hideSele();
  });
  loadSeleData();
  where.pageIndex = 1;
  where.pageSize = pageSize;
});

//加载下拉数据
function loadSeleData() {
  loadFun('emsbp/getMapName', { pageSize: -1, enable: 1 }, '#mapSelect', undefined, 'name', function (list) {
    mapList = list;
    init();
  });
}

//初始化列表
function init() {
  where.type = '6'
  $.ajax({
    url: url + 'variable_operational_data/getVariableOperationalData2',
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
      if (allChe.prop('checked')) {
        allChe.prop('checked', false);
      }
      if (pageIndex !== data.pageIndex) {
        pageIndex = data.pageIndex;
      }
      var allName = getTheadName(tab.find('thead'));

      var html = '';
      var nameTar = null;
      var listTar = null;
      var value = null;
      var lineNum = (pageIndex - 1) * pageSize + 1;
      for (var i = 0; i < len; i++) {
        listTar = list[i];
        html += '<tr>';
        for (var j = 0; j < allName.length; j++) {
          nameTar = allName[j];
          if (nameTar === '') {
            continue;
          }
          value = listTar[nameTar];
          if (nameTar === 'line') {
            html += resTabCheck2(listTar, lineNum + i);
          } else if (nameTar === 'map') {
            html += '<td>' + getMapName(listTar[nameTar]) + '</td>';
          } else {
            html += '<td>' + convertNull(listTar[nameTar]) + '</td>';
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

function resTabCheck2(listTar, line, id, name) {
  return '<td class="padLeft40">' +
    '<label class="seleLabel">' +
    '<span class="lineNo">' + (line) + '</span></label></td>';
};

function getMapName(map) {
  let target = mapList.find((item) => item.id == map);
  if (target && target.name) {
    return target.name
  };
  return map;
};

//翻页
function turnPage() {
  where.pageIndex = pageIndex;
  init();
};


// 添加
function add() {
  $('#addFrame').attr('style', 'z-index: 99')
  $('#addFrame').attr('src', './parkSpaceNaviAddDetail.html')
  $('#addFrame').show()
};

function closeFrame() {
  $('#addFrame').hide()
  search()
}