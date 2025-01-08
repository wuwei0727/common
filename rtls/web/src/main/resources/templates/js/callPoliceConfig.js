var ids;//删除数据的ID
var where = {}; // 分页信息
var list = []; // 数据列表

var frist = true;


$(function () {
  $(document).on('click', function (e) {
    hideSele();
  });

  where.pageIndex = 1;
  where.pageSize = pageSize;

  init();

})

// 加载下拉数据
function loadSeleData() {
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
      list = data.list;
      // 设置对应的下拉列表
      setcpConfigSelect(list);

    },
    error: function (jqXHR) {
      resError(jqXHR);
    }
  })
}

//初始化列表
function init() {
  loadSeleData();
  $.ajax({
    url: url + 'deviceAlarms/getDeviceAlarmsTypeConfig',
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
            html += resTabCheck(listTar, lineNum + i, 'id', 'deviceName');
          } else if (nameTar == 'operating') {
            var editBtn = document.getElementById("editBtn");
            var deleteBtn = document.getElementById("deleteBtn");
            var editTxt = editBtn ? '<span class="tabOper" onclick="detail(\'' + listTar.id + '\')">编辑</span>' : '';
            var delteTxt = deleteBtn ? '<span class="tabOper deleOpa" onclick="showDele(\'' + listTar.id + '\',\'' + listTar.deviceName + '\')">删除</span>' : '';
            html = html + '<td>' + editTxt + delteTxt + '</td>';
          } else {

            html += '<td>' + listTar[nameTar] + '</td>';

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

//单行的-删除
function showDele(id, txt) {
  ids = id + "";
  $('#deleTxt').text(txt);
  showPop('#delePop');
}

//多行的-删除
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

//确认-删除
function entDele() {
  $.ajax({
    url: url + 'deviceAlarms/delDeviceAlarmsConfig/' + ids,
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
  $('#editFrame').attr('src', './cpConfigDetails.html?id=' + id)
  $('#editFrame').show()
}

// 新增
function addCompany() {
  $('#addFrame').attr('style', 'z-index: 99')
  $('#addFrame').attr('src', './cpConfigDetails.html')
  $('#addFrame').show()
}



function closeFrame() {
  $('#addFrame').hide()
  $('#editFrame').hide()
  search()
}

// 设置对应的下拉列表
function setcpConfigSelect(list) {

  let deviceNameList = [];
  let alarmTypeList = [];

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
  let deviceNameHtml = '<div onclick="seleBatch(this,\'\')" data-id="">' + '全部' + '</div>';
  deviceNameList.forEach((item) => {
    deviceNameHtml += '<div onclick="seleBatch(this,\'' + item.deviceTypeId + '\')" data-id="' + item.deviceTypeId + '" title="' + item.deviceName + '">' + item.deviceName + '</div>'
  });
  $("#deviceNameSelect").html(deviceNameHtml)

  // 设置报警类型下拉
  let alarmTypeHtml = '<div onclick="seleBatch(this,\'\')" data-id="">' + '全部' + '</div>';
  alarmTypeList.forEach((item) => {
    alarmTypeHtml += '<div onclick="seleBatch(this,\'' + item.alarmsTypeId + '\')" data-id="' + item.alarmsTypeId + '" title="' + item.alarmName + '">' + item.alarmName + '</div>'
  });
  $("#alarmTypeSelect").html(alarmTypeHtml)
};