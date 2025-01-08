var ID;
var mapId;
var mapList = [];

var currentMapId;

$(function () {
  $(document).on('click', function (e) {
    hideSele();
  })

  var id = getUrlStr('id');
  if (id) {
    ID = id;
    init();
    $('#titleFlag').html('编辑');
  }
  loadSeleData();
});


//加载下拉数据
function loadSeleData() {
  loadMapSeleData();
}

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
    url: url + 'maintenanceStaff/getMaintenanceStaffById/' + ID,
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

        mapId = data.mapId;
        setData(data, '.main');
        currentMapId = $('[name="map"]').data('val');

        if (data.map) {
          $('[name="map"]').eq(0).addClass('batchTxtChange');
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

  if (!sendData.map) {
    tips('请选择关联地图');
    return;
  };

  if (!sendData.name) {
    tips('请输入名称');
    return;
  };

  if (!sendData.phone) {
    tips('请输入手机号');
    return;
  } else {
    var reg_phone = /^1[3-9]\d{9}$/;
    if (!reg_phone.test(sendData.phone)) {
      tips('请输入正确的手机号码！！！');
      return;
    }
  };

  console.log('send', sendData);

  // sendData.status = sendData.status === true ? 1 : 0;

  var path = url;
  if (ID) {
    path += 'maintenanceStaff/updateMaintenanceStaff';
    sendData.id = +ID;
  } else {
    path += 'maintenanceStaff/addMaintenanceStaff';
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
            init();
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
};

//地图切换  
function seleMap(that, id) {
  currentMapId = id;
  mapId = id;
  seleBatch(that, id, function () {
    $('[name="map"]').val(id);
  });
};