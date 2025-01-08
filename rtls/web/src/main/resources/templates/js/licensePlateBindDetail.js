var ID;
var mapId;
var mapList = [];
var companyList = [];

var currentMapId;
var companyId;

$(function () {
  $(document).on('click', function (e) {
    hideSele();
  })
  var id = getUrlStr('id');
  if (id) {
    ID = id;
    init();
    $('#titleFlag').html('编辑');
  };
  loadSeleData();
})
//加载下拉数据
function loadSeleData() {
  loadMapSeleData();
}

// 关联公司
function getCompanylist() {
  $.ajax({
    url: url + 'park/getCompany',
    data: {
      pageSize: -1,
      pageIndex: 0,
      map: mapId
    },
    success: function (res) {
      if (res.code !== 200) {
        tips(res.message);
        return;
      }
      var list = res.data;
      companyList = list;
      var html = '';
      var first = list.find(item => item.id == companyId);
      var target = null;
      for (var i = 0; i < list.length; i++) {
        target = list[i];

        html += '<div onclick="seleelCompany(this,\'' + list[i].id + '\')">' + list[i].name + '</div>';
      }
      var mapSelect = $('#companySelect');
      mapSelect.html(html);
      if (first) {
        mapSelect.prev().html(first.name);
        seleelCompany(null, first.id, first.name, first.floor, first.building);
      }
    },
    error: function (jqXHR) {
      resError(jqXHR);
    }
  })
};

// 关联地图
function loadMapSeleData() {
  $.ajax({
    url: url + 'map/getMap2dSel',
    data: {
      pageSize: -1,
      enable: 1,
      // companyId: companyId
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
    url: url + 'carPlate/getCarPlateById/' + ID,
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
        companyId = data.companyId;
        getCompanylist();

        setData(data, '.main');
        currentMapId = $('[name="mapId"]').data('val');

        if (data.mapId) {
          $('[name="mapId"]').eq(0).addClass('batchTxtChange');
        };

        if (data.companyId) {
          $('[name="companyId"]').eq(0).addClass('batchTxtChange');
        }

        // if (data.elevatorName) {
        //   $('[name="elevatorId"]')[0].innerText = data.elevatorName;
        //   $('[name="elevatorId"]').eq(0).addClass('batchTxtChange');
        // }
      } else {
        tips(res.message);
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
  };
  if (!sendData.mapId) {
    tips('请选择地图');
    return;
  } else {
    let mid = sendData.mapId;
    let data = mapList.find((item) => {
      return item.id == mid
    });
    sendData.mapName = data.mapName
  };
  if (!sendData.companyId) {
    tips('请选择公司');
    return;
  } else {
    let cid = sendData.companyId;
    let data = companyList.find((item) => {
      return item.id == cid
    });
    sendData.companyName = data.name
  };
  if (!sendData.plateNumber) {
    tips('请输入车牌号');
    return;
  };
  if (!sendData.phoneNumber) {
    tips('请输入手机号');
    return;
  } else {
    var reg_phone = /^1[3-9]\d{9}$/;
    if (!reg_phone.test(sendData.phoneNumber)) {
      tips('请输入正确的手机号码！！！');
      return;
    }
  }
  var path = url;
  var ptype;
  if (ID) {
    path += 'carPlate/editCarPlate';
    sendData.id = ID;
    ptype = 'put'
  } else {
    path += 'carPlate/addCarPlate';
    ptype = 'post'
  }


  $.ajax({
    url: path,
    data: JSON.stringify(sendData),
    type: ptype,
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
          if (!ID) {
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
  if (currentMapId != id) {
    $('[name="companyId"]').eq(0).removeClass('batchTxtChange');
    $('[name="companyId"]').data('val', '');
    $('[name="companyId"]')[0].innerText = '--请选择--';
  };
  currentMapId = id;
  mapId = id;
  getCompanylist();
  seleBatch(that, id, function () {
    $('[name="map"]').val(id);
  });
}

// 选择公司
function seleelCompany(that, id) {
  seleBatch(that, id);
}