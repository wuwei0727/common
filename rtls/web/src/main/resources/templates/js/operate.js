var ids;//删除数据的ID
var where = {};
var list = {};

$(function () {
  $(document).on('click', function (e) {
    hideSele();
  })
  where.pageIndex = 1;
  where.pageSize = pageSize;
  init();
});

//初始化列表
function init() {
  $.ajax({
    url: url + 'variable_operational_data/getVariableOperationalData',
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
      list = data;
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
            html += resTabCheck2(listTar, '1');
          } else if (nameTar === 'operating') {
            var editBtn = document.getElementById("editBtn");

            var editTxt = editBtn ? '<span class="tabOper" onclick="edit(\'' + listTar.id + '\')">编辑</span>' : '';

            html = html + '<td>' + editTxt + '</td>';
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
}

//翻页
function turnPage() {
  where.pageIndex = pageIndex;
  init();
};


// 编辑
function edit(id) {
  $('#editFrame').attr('style', 'z-index: 99')
  $('#editFrame').attr('src', './operateDetail.html?id=' + id)
  $('#editFrame').show()
};

function closeFrame() {
  $('#editFrame').hide()
  search()
}