var ids;//删除数据的ID
var where = {};
var list = [];

$(function () {
  $(document).on('click', function (e) {
    hideSele();
  })
  where.pageIndex = 1;
  where.pageSize = pageSize;
  init();
})

//初始化列表
function init() {
  $.ajax({
    url: url + 'park/getShangjiaType',
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
      if (res.code != 200) {
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
      if (pageIndex != data.pageIndex) {
        pageIndex = data.pageIndex;
      }
      var allName = getTheadName(tab.find('thead'));

      var html = '';
      var nameTar = null;
      var listTar = null;
      var lineNum = (pageIndex - 1) * pageSize + 1;
      for (var i = 0; i < len; i++) {
        listTar = list[i];
        html += '<tr>';
        for (var j = 0; j < allName.length; j++) {
          nameTar = allName[j];
          if (nameTar == 'line') {
            html += resTabCheck(listTar, lineNum + i);
          } else if (nameTar == 'url') {
            if (listTar[nameTar]) {
              html += '<td><img class="tabImg" src="' + (listTar[nameTar]) + '" /></td>';
            } else {
              html += '<td></td>';
            }
          } else if (nameTar == 'operating') {
            var editBtn = document.getElementById("editBtn");
            var deleteBtn = document.getElementById("deleteBtn");
            var editTxt = editBtn ? '<span class="tabOper" onclick="edit(\'' + listTar.id + '\')">编辑</span>' : '';
            var delteTxt = deleteBtn ? '<span class="tabOper deleOpa" onclick="showDele(\'' + listTar.id + '\',\'' + listTar.name + '\')">删除</span>' : '';
            html = html + '<td>' + editTxt + delteTxt + '</td>';
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
    url: url + 'park/delShangJiangType/' + ids,
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
}
//翻页
function turnPage() {
  where.pageIndex = pageIndex;
  init();
}
//编辑
function edit(id) {
  $('#editFrame').attr('style', 'z-index: 99')
  $('#editFrame').attr('src', './merchantTypeDetails.html?id=' + id)
  $('#editFrame').show()
}

function add() {
  $('#addFrame').attr('style', 'z-index: 99')
  $('#addFrame').attr('src', './merchantTypeDetails.html')
  $('#addFrame').show()
}

function closeFrame() {
  $('#addFrame').hide()
  $('#editFrame').hide()
  search()
}