var mapList = []; // 下拉地图数据
var usersList = []; // 下拉用户数据
let nowDateStr;

$(function () {
  nowDateStr = timeStampForDate();
  $(document).on('click', function (e) {
    hideSele();
  });
  initDate();
  loadMapSeleData();
});

function initDate(appointmentSlot) {
  if (appointmentSlot) {
    $('#time').val(appointmentSlot);
  }
  jeDate('#time', {
    theme: {
      bgcolor: "#4A60CF",
      pnColor: "#4A60CF"
    },
    multiPane: true,
    format: "YYYY-MM",
    maxDate: nowDateStr,
    donefun: function () {
    }
  });
};

// 关联地图
function loadMapSeleData() {
  $.ajax({
    url: url + 'emsbp/getMapName',
    data: {
      pageSize: -1,
      enable: 1,
    },
    success: function (res) {
      if (res.code !== 200) {
        tips(res.message);
        return;
      }
      mapList = [];
      var list = res.data;
      var html = '';
      var target = null;
      for (var i = 0; i < list.length; i++) {
        target = list[i];
        mapList.push({
          id: target.id,
          type: target.type,
          fmapID: target.fmapID || '',
          appName: target.appName || '',
          mapKey: target.mapKey || '',
          path: target.themeImg || '',
          mapName: target.name || ''
        });
        html += '<div onclick="seleMap(this,\'' + list[i].id + '\')">' + list[i].name + '</div>';
      }
      var mapSelect = $('#mapSelect');
      mapSelect.html(html);
    },
    error: function (jqXHR) {
      resError(jqXHR);
    }
  })
};

function loadPlaceSeleData(id) {
  $.ajax({
    url: url + 'park/getPlace',
    data: {
      pageSize: -1,
      map: id,
    },
    success: function (res) {
      if (res.code !== 200) {
        tips(res.message);
        return;
      }
      placeList = [];
      var list = res.data;
      var html = '';
      var target = null;
      for (var i = 0; i < list.length; i++) {
        target = list[i];
        placeList.push({
          id: target.id,
          name: target.name
        });
        html += '<div onclick="selePlace(this,\'' + list[i].id + '\')">' + list[i].name + '</div>';
      }
      var placeId = $('#placeId');
      placeId.html(html);
    },
    error: function (jqXHR) {
      resError(jqXHR);
    }
  })
};

function selePlace(that, id) {
  let target = placeList.find((item) => item.id == id);
  seleBatch(that, id, function () {
    $('[name="placeName"]').val(target.name);
  });
};

function seleMap(that, id) {
  loadPlaceSeleData(id)
  seleBatch(that, id);
};


//保存
function save() {
  var sendData = getData('.main');
  if (!sendData) {
    return;
  };

  if (!sendData.map) {
    tips('请选择地图');
    return
  }
  if (!sendData.time) {
    tips('请选择月份');
    return
  };
  if (!sendData.hourDifference) {
    tips('请选择时长');
    return
  };
  if (!sendData.numbers) {
    tips('请输入数量');
    return
  };
  if (!sendData.placeId) {
    tips('请选择关联车位');
    return
  };
  let day
  let nowMonth = timeStampForDate('mm');
  if (nowMonth == sendData.time) {
    // 相同月份-上传当天天数
    day = new Date().getDate();
  } else {
    // 不同-上传选择月份的天数
    let timeArr = sendData.time.split('-');
    day = new Date(timeArr[0], timeArr[1], 0).getDate();
  };

  sendData.type = "12";
  sendData.year = sendData.time.split('-')[0];
  sendData.month = sendData.time.split('-')[1];
  sendData.numDays = day + '';
  delete sendData.time;
  console.log('sendData', sendData);
  $.ajax({
    url: url + 'variable_operational_data/addVariableOperationalData',
    data: sendData,
    type: 'post',
    // headers: {
    //   "Content-Type": "application/json;charset=UTF-8"
    // },
    beforeSend: function () {
      loading();
    },
    complete: function () {
      removeLoad();
    },
    success: function (res) {
      if (res.code == 200) {
        tips(res.message || res.msg, function () {
          location.reload();
        }, 1000);
      } else {
        tips(res.message || res.msg);
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

function timeStampForDate(type) {
  let date = new Date();
  let Y = date.getFullYear();
  let M = '-' + (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1);
  let D = '-' + (date.getDate() < 10 ? '0' + date.getDate() : date.getDate());
  let h = ' ' + (date.getHours() < 10 ? '0' + date.getHours() : date.getHours());
  let m = ':' + (date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes());
  let s = ':' + (date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds());
  if (type == 'mm') {
    return Y + M;
  } else {
    return Y + M + D + h + m + s;
  }
};