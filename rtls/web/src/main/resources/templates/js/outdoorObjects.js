var ids;//删除数据的ID
var where = {};
var list = {};

$(function () {
  $(document).on('click', function (e) {
    hideSele();
  })
  where.pageIndex = 1;
  where.pageSize = pageSize;
  loadSeleData();
  init();
})

//加载下拉数据
function loadSeleData() {
  loadFun('map/getMap2dSel', { pageSize: -1, enable: 1 }, '#mapSelect');
}

//初始化列表
function init() {
  $.ajax({
    url: url + 'mapBuild/getMapBuild',
    data: {
      ...where,
      type: 'build'
    },
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
        // return;
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
          } else if (nameTar == 'operating') {
            var editBtn = document.getElementById("editBtn");
            var deleteBtn = document.getElementById("deleteBtn");
            var editTxt = editBtn ? '<span class="tabOper" onclick="detail(\'' + listTar.id + '\')">编辑</span>' : '';
            var delteTxt = deleteBtn ? '<span class="tabOper deleOpa" onclick="showDele(\'' + listTar.id + '\',\'' + listTar.name + '\')">删除</span>' : '';
            var posTxt = '<span class="tabOper deleOpa" onclick="showlocation(\'' + listTar.id + '\',\'' + listTar.num + '\',)">位置</span>';
            html = html + '<td>' + editTxt + delteTxt + posTxt + '</td>';
          } else {
            if (nameTar === 'objectType') {
              var value = listTar[nameTar];
              html += '<td>';
              if (value == '340873') {
                html += '一类建筑';
              } else if (value == '340874') {
                html += '二类建筑';
              } else if (value == '340875') {
                html += '三类建筑';
              } else if (value == '200110') {
                html += '外部道路'
              }
              html += '</td>';
            } else {
              html += '<td>' + convertNull(listTar[nameTar]) + '</td>';
            }

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
  };
  showTxt = showTxt.slice(0, -1);
  ids = cheId.slice(0, -1);
  $('#deleTxt').html(showTxt);
  showPop('#delePop');
}

//确认删除
function entDele() {
  $.ajax({
    url: url + 'mapBuild/delMapBuild/' + ids + '/build',
    type: 'delete',
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
function detail(id) {
  $('#editFrame').attr('style', 'z-index: 99')
  $('#editFrame').attr('src', './outdoorObjectsDetails.html?id=' + id)
  $('#editFrame').show()
}

// 新增
function addCompany() {
  $('#addFrame').attr('style', 'z-index: 99')
  $('#addFrame').attr('src', './outdoorObjectsDetails.html')
  $('#addFrame').show()
}


function closeFrame() {
  $('#addFrame').hide()
  $('#editFrame').hide()
  search()
}

// 位置显示
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