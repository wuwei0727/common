var ids;//删除数据的ID
var where = {}; // 分页信息
var list = []; // 数据列表
var lDlist = []; // 下拉数据

var deviceNameList = [];
var alarmTypeList = [];

var showD = false; // 左上角进出

$(function () {
  $(document).on('click', function (e) {
    hideSele();
  });

  where.pageIndex = 1;
  where.pageSize = pageSize;

  loadSeleData();

  showD = getUrlStr('showD');
  if (showD === 'true') {
    // 左上角点击-打开详情操作
    let id = getUrlStr('id');
    // detail(id)
    init(id);
  } else if (showD === 'false') {
    var search = $('.search');
    where.state = '0';
    search.find('[name="state"]').html('报警中').data('val', '0');
    $(search.find('[name="state"]')[0]).addClass('batchTxtChange');
    init();
  } else {
    init();
  }
})

//加载下拉数据
function loadSeleData() {
  loadFun('map/getMap2dSel', { pageSize: -1, enable: 1 }, '#mapSelect');

  $.ajax({
    url: url + 'deviceAlarms/getDeviceAlarmsTypeConfig',
    data: {
      pageIndex: 1,
      pageSize: -1
    },
    type: 'get',
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
      lDlist = data.list;
      // 设置对应的下拉列表
      setcpConfigSelect(lDlist);

    },
    error: function (jqXHR) {
      resError(jqXHR);
    }
  })
}

//初始化列表
function init(id) {
  if (id) {
    where['id'] = id
  };
  $.ajax({
    url: url + 'deviceAlarms/getDeviceAlarmsData',
    data: where,
    type: 'get',
    beforeSend: function () {
      loading();
    },
    complete: function () {
      removeLoad();
    },
    success: function (res) {

      where['id'] = ''

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
            if (listTar.num) {
              html += resTabCheck(listTar, lineNum + i, 'id', 'num');
            } else {
              html += resTabCheck(listTar, lineNum + i, 'id', 'placeName');
            }
          } else if (nameTar == 'operating') {
            var deleteBtn = document.getElementById("deleteBtn");
            var delteTxt = '';
            if (deleteBtn) {
              if (listTar.num) {
                delteTxt = '<span class="tabOper" onclick="showDele(\'' + listTar.id + '\',\'' + listTar.num + '\')">解除报警</span>'
              } else {
                delteTxt = '<span class="tabOper" onclick="showDele(\'' + listTar.id + '\',\'' + listTar.placeName + '\')">解除报警</span>'
              }
            };

            if (listTar.state == 1) {
              delteTxt = ''
            }

            html = html + '<td>' + delteTxt + '</td>';
          } else if (nameTar == 'desc') {
            html += '<td class="cheweitd" style="max-width:100px">' + convertNull(listTar[nameTar]) + '</td>'
          } else {
            if (nameTar == 'alarmType2') {
              html += '<td>' + getATypeListName(alarmTypeList, listTar['alarmType']) + '</td>';
            } else if (nameTar == 'equipmentType') {
              html += '<td>' + getDTypeListName(deviceNameList, listTar[nameTar]) + '</td>';
            } else if (nameTar == 'priority') {
              html += '<td>' + (listTar[nameTar] == 1 ? '高' : (listTar[nameTar] == 2 ? '中' : '低')) + '</td>';
            } else if (nameTar == 'state') {
              html += '<td>' + (listTar[nameTar] == 0 ? '报警中' : '已结束') + '</td>';
            } else {
              html += '<td>' + (listTar[nameTar] == null ? '' : listTar[nameTar]) + '</td>';
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

// 获取type对应的名字
function getATypeListName(list, type) {
  let name = list.filter((item) => item.alarmsTypeId == type);
  if (name.length) {
    return name[0].alarmName || ''
  } else {
    return ''
  }
}

function getDTypeListName(list, type) {
  let name = list.filter((item) => item.deviceTypeId == type);
  if (name.length) {
    return name[0].deviceName || ''
  } else {
    return ''
  }
}

//单行的-解除报警提示
function showDele(id, txt) {
  ids = id + "";
  $('#deleTxt').text(txt);
  showPop('#delePop');
}

//多行的-解除报警
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

//确认-解除报警
function entDele() {
  $.ajax({
    url: url + 'deviceAlarms/updateDeviceAlarms/' + ids,
    type: 'get',
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
        // 刷新home中的报警内容
        window.parent.setCallPolice();
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
  $('#editFrame').attr('src', './callPoliceDetails.html?id=' + id)
  $('#editFrame').show()
}

// 新增
function addCompany() {
  $('#addFrame').attr('style', 'z-index: 99')
  $('#addFrame').attr('src', './callPoliceDetails.html')
  $('#addFrame').show()
}

function closeFrame() {
  $('#addFrame').hide()
  $('#editFrame').hide()
  search()
}

// 设置对应的下拉列表
function setcpConfigSelect(list) {

  deviceNameList = [];
  alarmTypeList = [];

  list.forEach((item) => {
    let hasD = deviceNameList.some((dl) => dl.deviceTypeId == item.deviceTypeId);
    if (!hasD) {
      deviceNameList.push({
        deviceName: item.deviceName,
        deviceTypeId: item.deviceTypeId
      })
    };

    let hasA = alarmTypeList.some((al) => al.alarmsTypeId == item.alarmsTypeId);
    if (!hasA) {
      alarmTypeList.push({
        alarmName: item.alarmName,
        alarmsTypeId: item.alarmsTypeId
      })
    }
  });

  // 设置设备类别下拉
  let deviceNameHtml = '<div onclick="seleDeviceTypes(this,\'\')" data-id="">' + '全部' + '</div>';
  deviceNameList.forEach((item) => {
    deviceNameHtml += '<div onclick="seleDeviceTypes(this,\'' + item.deviceTypeId + '\')" data-id="' + item.deviceTypeId + '" title="' + item.deviceName + '">' + item.deviceName + '</div>'
  });
  $("#deviceNameSelect").html(deviceNameHtml)

  // 设置报警类型下拉
  let alarmTypeHtml = '<div onclick="seleBatch(this,\'\')" data-id="">' + '全部' + '</div>';
  alarmTypeList.forEach((item) => {
    alarmTypeHtml += '<div onclick="seleBatch(this,\'' + item.alarmsTypeId + '\')" data-id="' + item.alarmsTypeId + '" title="' + item.alarmName + '">' + item.alarmName + '</div>'
  });
  $("#alarmTypeSelect").html(alarmTypeHtml)
};

function seleDeviceTypes(that, id) {
  $('[name="alarmType"]').data('val', '');
  $('[name="alarmType"]').removeClass('batchTxtChange');
  $('[name="alarmType"]').html('--请选择--');

  setAlarmTypeHtml(id);
  seleBatch(that, id);
};

function setAlarmTypeHtml(id) {
  let list = []
  if (id) {
    let arr = lDlist.filter((item) => {
      return item.deviceTypeId == id
    });

    list = arr.map((item) => {
      return {
        alarmName: item.alarmName,
        alarmsTypeId: item.alarmsTypeId
      }
    })
  } else {
    list = alarmTypeList
  };

  // 设置报警类型下拉
  let alarmTypeHtml = '<div onclick="seleBatch(this,\'\')" data-id="">' + '全部' + '</div>';
  list.forEach((item) => {
    alarmTypeHtml += '<div onclick="seleBatch(this,\'' + item.alarmsTypeId + '\')" data-id="' + item.alarmsTypeId + '" title="' + item.alarmName + '">' + item.alarmName + '</div>'
  });
  $("#alarmTypeSelect").html(alarmTypeHtml)
};

function reset() {
  setAlarmTypeHtml('');
  resetSearch();
};