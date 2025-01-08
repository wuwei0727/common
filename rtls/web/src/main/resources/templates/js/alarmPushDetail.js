var ID;
var mapId;
var mapList = [];

var currentMapId;

var currentA2 = [];
var currentA3 = [];
var currentA4 = [];

$(function () {
  $(document).on('click', function (e) {
    hideSele();
  })

  var id = getUrlStr('id');
  if (id) {
    ID = id;
    init();
    $('#titleFlag').html('编辑');
  } else {
    initAA();
    inita3();
  };
  loadSeleData();
});


//加载下拉数据
function loadSeleData() {
  loadMapSeleData();
};

// 关联地图
function loadMapSeleData() {
  $.ajax({
    url: url + 'map/getMap2dSel',
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
      var first = list.find(item => item.id == currentMapId);
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
      if (first) {
        mapSelect.prev().html(first.name);
        seleMap(null, first.id);
      }
    },
    error: function (jqXHR) {
      resError(jqXHR);
    }
  })
}

//初始化数据
function init() {
  $.ajax({
    url: url + 'apb/getParkingAlertConfigById/' + ID,
    beforeSend: function () {
      loading();
    },
    complete: function () {
      removeLoad();
    },
    success: function (res) {
      if (!res.code) {
        let newWindow = window.open('about:blank');
        newWindow.document.write(res);
        newWindow.focus();
        window.history.go(-1);
      } else if (res.code == 200) {
        var data = res.data;

        mapId = data.map;
        setData(data, '.main');
        currentMapId = $('[name="map"]').data('val');

        if (data.map) {
          $('[name="map"]').eq(0).addClass('batchTxtChange');
        };

        if (data.level) {
          currentA3 = data.level.split(',');
          inita3();
        };
        if (data.alarmTypeId) {
          currentA2 = data.alarmTypeId.split(',');
          initAA();
        };
        if (data.maintenanceStaffId) {
          currentA4 = data.maintenanceStaffId.split(',');
          getExtensionWorker(data.map);
        };

      } else {
        tips(res.message);
      }
    },
    error: function (jqXHR) {
      resError(jqXHR);
    }
  })
};


//保存
function save() {
  var sendData = getData('.main');
  if (!sendData) {
    return;
  };

  console.log('send', sendData);

  if (!sendData.map) {
    tips('请选择关联地图');
    return;
  };

  if (!sendData.alarmTypeId) {
    tips('请选择关联内容');
    return;
  };

  if (!sendData.level) {
    tips('请选择关联级别');
    return;
  };

  if (!sendData.maintenanceStaffId) {
    tips('请选择关联运维人员');
    return;
  };

  var path = url;
  if (ID) {
    path += 'apb/updateParkingAlertConfig';
    sendData.id = +ID;
  } else {
    path += 'apb/addAlarmPersonnelBindings';
  };

  $.ajax({
    url: path,
    data: JSON.stringify(sendData),
    type: 'post',
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
      if (res.code == 200) {
        tips(res.message, function () {
          if (ID) {
            location.reload();
            // init();
          } else {
            location.reload();
          }
        }, 1000);
      } else {
        tips(res.message);
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

//地图切换  
function seleMap(that, id) {
  if (that) {
    seleMoreSeleData4 = [];
    currentA4 = [];
    $('[name="maintenanceStaffId"]').data('val', '');
    $('[name="maintenanceStaffId"]').removeClass('batchTxtChange');
    $('[name="maintenanceStaffId"]').html('--请选择--');
    getExtensionWorker(id);
  };

  currentMapId = id;
  mapId = id;
  seleBatch(that, id, function () {
    $('[name="map"]').val(id);
  });
};


let a3 = [{
  id: 1,
  text: '高'
}, {
  id: 2,
  text: '中'
}, {
  id: 3,
  text: '低'
}];
function inita3() {
  let data = a3;

  let html = '';
  data.forEach((item) => {
    // html += '<div class="seleMoreItem" onclick="seleMoreA3(this,\'' + item.id + '\')">' + item.text + '</div>';

    let index = currentA3.findIndex((citem) => {
      return citem == item.id
    });
    if (index != -1) {
      seleMoreSeleData3.push({
        id: item.id,
        text: item.text
      })
      html += '<div class="seleMoreItem seleMoreItemAction" onclick="seleMoreA3(this,\'' + item.id + '\')">' + item.text + '</div>';
    } else {
      html += '<div class="seleMoreItem" onclick="seleMoreA3(this,\'' + item.id + '\')">' + item.text + '</div>';
    }
  });

  $('#a3').html(html);

  if (seleMoreSeleData3.length) {
    let textArr = seleMoreSeleData3.map((item) => {
      return item.text
    });
    $('[name="level"]').addClass('batchTxtChange');
    $('[name="level"]').html(textArr.join());
  }
};

var seleMoreSeleData3 = [];
function seleMoreA3(e, id) {
  window.event.stopPropagation();

  let had = $(e).hasClass('seleMoreItemAction');
  if (had) {
    $(e).removeClass('seleMoreItemAction');
    let index = seleMoreSeleData3.findIndex((item) => {
      return item.id == id
    });
    seleMoreSeleData3.splice(index, 1)
  } else {
    $(e).addClass('seleMoreItemAction');
    let item = a3.filter((item) => {
      return item.id == id
    });
    seleMoreSeleData3.push(item[0]);
  };

  let idArr = seleMoreSeleData3.map((item) => {
    return item.id
  });
  let textArr = seleMoreSeleData3.map((item) => {
    return item.text
  });

  let val = idArr.join();
  let text = textArr.join()
  var showTxt = $(e).parent().prev();

  if (idArr && idArr.length) {
    $(showTxt[0]).addClass('batchTxtChange')
  } else {
    $(showTxt[0]).removeClass('batchTxtChange')
    text = '--请选择--'
  };

  showTxt.html(text).data('val', val);
};

var a2 = [];
function initAA() {
  $.ajax({
    url: url + 'deviceAlarms/getDeviceAlarmsTypeConfig',
    data: {
      pageSize: -1
    },
    type: 'get',
    success: function (res) {
      var data = res.data;
      list = data.list;
      // 设置对应的下拉列表
      console.log('res', list);
      a2 = list.map((item) => {
        return {
          id: item.id,
          text: item.deviceName + '-' + item.alarmName
        }
      });

      inita2();
    },
    error: function (jqXHR) {
      resError(jqXHR);
    }
  })
};
function inita2() {
  let data = a2;

  let html = '';
  data.forEach((item) => {
    // html += '<div class="seleMoreItem" onclick="seleMoreA2(this,\'' + item.id + '\')">' + item.text + '</div>';

    let index = currentA2.findIndex((citem) => {
      return citem == item.id
    });
    if (index != -1) {
      seleMoreSeleData2.push({
        id: item.id,
        text: item.text
      })
      html += '<div class="seleMoreItem seleMoreItemAction" onclick="seleMoreA2(this,\'' + item.id + '\')">' + item.text + '</div>';
    } else {
      html += '<div class="seleMoreItem" onclick="seleMoreA2(this,\'' + item.id + '\')">' + item.text + '</div>';
    }
  });

  $('#a2').html(html);

  if (seleMoreSeleData2.length) {
    let textArr = seleMoreSeleData2.map((item) => {
      return item.text
    });
    $('[name="alarmTypeId"]').addClass('batchTxtChange');
    $('[name="alarmTypeId"]').html(textArr.join());
  }
};
var seleMoreSeleData2 = [];
function seleMoreA2(e, id) {
  window.event.stopPropagation();

  let had = $(e).hasClass('seleMoreItemAction');
  if (had) {
    $(e).removeClass('seleMoreItemAction');
    let index = seleMoreSeleData2.findIndex((item) => {
      return item.id == id
    });
    seleMoreSeleData2.splice(index, 1)
  } else {
    $(e).addClass('seleMoreItemAction');
    let item = a2.filter((item) => {
      return item.id == id
    });
    seleMoreSeleData2.push(item[0]);
  };

  let idArr = seleMoreSeleData2.map((item) => {
    return item.id
  });
  let textArr = seleMoreSeleData2.map((item) => {
    return item.text
  });

  let val = idArr.join();
  let text = textArr.join()
  var showTxt = $(e).parent().prev();

  if (idArr && idArr.length) {
    $(showTxt[0]).addClass('batchTxtChange')
  } else {
    $(showTxt[0]).removeClass('batchTxtChange')
    text = '--请选择--'
  };

  showTxt.html(text).data('val', val);
};

var a4 = [];
function getExtensionWorker(id) {
  console.log('ididid', id);
  if (!id) return;
  $.ajax({
    url: url + 'maintenanceStaff/getAllOrFilteredMaintenanceStaff',
    data: {
      map: id,
      status: 1,
      pageSize: -1
    },
    type: 'get',
    success: function (res) {
      var data = res.data;
      list = data.list;
      // 设置对应的下拉列表
      a4 = list.map((item) => {
        return {
          id: item.id,
          text: item.name
        }
      });

      inita4();
    },
    error: function (jqXHR) {
      resError(jqXHR);
    }
  })
};

function inita4() {
  let data = a4;

  let html = '';
  data.forEach((item) => {
    // html += '<div class="seleMoreItem" onclick="seleMoreA4(this,\'' + item.id + '\')">' + item.text + '</div>';

    let index = currentA4.findIndex((citem) => {
      return citem == item.id
    });
    if (index != -1) {
      seleMoreSeleData4.push({
        id: item.id,
        text: item.text
      })
      html += '<div class="seleMoreItem seleMoreItemAction" onclick="seleMoreA4(this,\'' + item.id + '\')">' + item.text + '</div>';
    } else {
      html += '<div class="seleMoreItem" onclick="seleMoreA4(this,\'' + item.id + '\')">' + item.text + '</div>';
    }
  });

  $('#a4').html(html);

  if (seleMoreSeleData4.length) {
    let textArr = seleMoreSeleData4.map((item) => {
      return item.text
    });
    $('[name="maintenanceStaffId"]').addClass('batchTxtChange');
    $('[name="maintenanceStaffId"]').html(textArr.join());
  }
};
var seleMoreSeleData4 = [];
function seleMoreA4(e, id) {
  window.event.stopPropagation();

  let had = $(e).hasClass('seleMoreItemAction');
  if (had) {
    $(e).removeClass('seleMoreItemAction');
    let index = seleMoreSeleData4.findIndex((item) => {
      return item.id == id
    });
    seleMoreSeleData4.splice(index, 1)
  } else {
    $(e).addClass('seleMoreItemAction');
    let item = a4.filter((item) => {
      return item.id == id
    });
    seleMoreSeleData4.push(item[0]);
  };

  let idArr = seleMoreSeleData4.map((item) => {
    return item.id
  });
  let textArr = seleMoreSeleData4.map((item) => {
    return item.text
  });

  let val = idArr.join();
  let text = textArr.join()
  var showTxt = $(e).parent().prev();

  if (idArr && idArr.length) {
    $(showTxt[0]).addClass('batchTxtChange')
  } else {
    $(showTxt[0]).removeClass('batchTxtChange')
    text = '--请选择--'
  };

  showTxt.html(text).data('val', val);
};