var ID;// url携带的id
var mapList = []; // 关联地图的下拉数据
var currentMapId;//初始化的id，不同则重新初始化地图
var mapData = {};// 详情数据

$(function () {
  $(document).on('click', function (e) {
    hideSele();
  })
  loadSeleData();
  var id = getUrlStr('id');
  if (id) {
    ID = id;
    init();
    $('#titleFlag').html('编辑');
  }
})

//初始化数据
function init() {
  $.ajax({
    url: url + 'parkingLotCost/getParkLotCostById',
    data: { id: ID },
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
      if (!res.code) {
        let newWindow = window.open('about:blank');
        newWindow.document.write(res);
        newWindow.focus();
        window.history.go(-1);
      } else if (res.code === 200) {
        var data = res.data;
        mapData = data;
        setData(data, '.main');
        currentMapId = $('[name="map"]').data('val');
        if (data.mapName) {
          $('[name="map"]').eq(0).addClass('batchTxtChange')
        }
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

//加载下拉数据-关联地图
function loadSeleData() {
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
      // mapName = res.data[0].mapName;
      var first = list.find(item => item.id == currentMapId);
      // var first = list[0]
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

//保存
function save() {
  var sendData = getData('.main');
  console.log('sendData', sendData);
  if (!sendData) {
    return;
  }

  if (!sendData.map) {
    tips('请选择关联地图');
    return;
  }

  if (!sendData.cost) {
    tips('请输入收费标准');
    return;
  }


  var path = url;
  if (ID) {
    // 编辑
    path += 'parkingLotCost/updateParkLotCost';
    sendData.id = +ID;
  } else {
    // 新增
    path += 'parkingLotCost/addParkLotCost';

  }


  sendData.map = +sendData.map
  $.ajax({
    url: path,
    data: JSON.stringify(sendData),
    type: ID ? 'put' : 'post',
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
}

//地图切换  
function seleMap(that, id) {
  seleBatch(that, id, function () {
    if (currentMapId != id) {
      $('[name="map"]').val(id);
    } else {
      $('[name="map"]').val(id);
    }
  });
}
