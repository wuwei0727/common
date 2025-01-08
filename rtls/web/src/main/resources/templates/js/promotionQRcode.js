var ID;
var mapList = []; // 下拉地图数据
var currentMapId; // 选择的地图id
var chooseMapObj;
var mapId;
var businessId;
var businessList = []; // 商家列表
var chooseBusinessObj = {}; // 选择的商家信息
var currentProId;
var ProList = [];
var chooseProObj;

$(function () {
  $(document).on('click', function (e) {
    hideSele();
  });
  var id = getUrlStr('id');
  if (id) {
    ID = id;
    init();
  }

  // 推广人
  loadProInfo();
  // 地图下拉
  loadMapSeleData();

});

function loadProInfo() {
  $.ajax({
    url: url + 'promoterInfo/getPromoterInfo',
    data: {
      pageSize: -1,
    },
    type: 'post',
    success: function (res) {
      var tab = $('#tab');
      if (res.code !== 200) {
        tableException(tab, res.message);
        return;
      }
      var data = res.data;
      var list = data.list;
      var html = '';
      var first = list.find(item => item.id == currentProId);
      var target = null;
      for (var i = 0; i < list.length; i++) {
        target = list[i];
        ProList.push(target);
        html += '<div onclick="selePro(this,\'' + list[i].id + '\')">' + list[i].name + '</div>';
      }
      var pro_info_id = $('#proInfoId');
      pro_info_id.html(html);
      if (first) {
        pro_info_id.prev().html(first.name);
        selePro(null, first.id);
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
    // url: url + 'map/getMap2dSel',
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
};

// 选择地图
function seleMap(that, id) {
  mapId = id;
  chooseMapObj = mapList.find((item) => item.id == id);
  // 查询商家
  getbusinesslist();
  seleBatch(that, id, function () {

    $('[name="map"]').val(id);
    $('[name="destId"]').val(id);
    $('[name="mapName"]').val(chooseMapObj.mapName);
    $('[name="desc"]').val('地图');

    $('[name="fid"]').val('');
    $('[name="shangjiaName"]').val('');
    $('[name="shangjiaName"]').eq(0).removeClass('batchTxtChange');
    $('[name="shangjiaName"]')[0].innerText = '--请选择--';
  });
};

function selePro(that, id) {
  currentProId = id;

  chooseProObj = ProList.find((item) => item.id == id);

  seleBatch(that, id, function () {
    if (currentProId != id) {
      $('[name="proInfoId"]').val(id);
    } else {
      $('[name="proInfoId"]').val(id);
    }
  });
};

//初始化数据
function init() {
  $.ajax({
    url: url + 'promoter_qr_code/selectOne?id=' + ID,
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
        setData(data, '.main');

        if (data.proInfoId) {
          $('[name="proInfoId"]').eq(0).addClass('batchTxtChange');
          $('[name="proInfoId"]')[0].innerText = data.promoterPersonName;
        };

        if (data.shangjiaName) {
          $('[name="shangjiaName"]').eq(0).addClass('batchTxtChange');
          $('[name="shangjiaName"]')[0].innerText = data.shangjiaName;
          businessId = data.destId
        };

        if (data.map) {
          currentMapId = $('[name="map"]').data('val');

          $('[name="map"]').eq(0).addClass('batchTxtChange');
          // 查询商家
          // getbusinesslist();
        };

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

//保存
function save() {
  var sendData = getData('.main');
  if (!sendData) {
    return;
  };

  if (!sendData.proInfoId) {
    tips('请选择推广人');
    return;
  }

  if (!sendData.map) {
    tips('请选择推广地图');
    return;
  };

  // sendData['destId'] = sendData.map;
  // sendData['mapName'] = chooseMapObj.mapName;
  // sendData['desc'] = '地图';
  sendData['type'] = 'promotion';

  // if (sendData.shangjiaName) {
  //   sendData['destId'] = chooseBusinessObj.id;
  //   sendData['floor'] = chooseBusinessObj.floor;
  //   sendData['x'] = chooseBusinessObj.x;
  //   sendData['y'] = chooseBusinessObj.y;
  //   sendData['fid'] = chooseBusinessObj.fid;
  //   sendData['desc'] = '商家';
  // };

  if (!sendData.fid) {
    sendData['floor'] = '';
    sendData['x'] = '';
    sendData['y'] = '';
    sendData['fid'] = '';
    sendData['shangjiaName'] = '';
  }


  console.log('sendData', sendData);

  var path = url;
  if (ID) {
    path += 'promoter_qr_code/updatePromoterQrCode';
    sendData.id = +ID;
  } else {
    path += 'promoter_qr_code/addPromoterQrCode';
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

// 查询商家
function getbusinesslist() {
  $.ajax({
    url: url + 'park/getShangjia',
    data: {
      pageSize: -1,
      map: mapId
    },
    type: 'post',
    success: function (res) {
      var tab = $('#tab');
      if (res.code != 200) {
        tableException(tab, res.message);
        return;
      };

      businessList = [];
      var list = res.data;
      var html = '';
      var first = list.find(item => item.id == businessId);
      var target = null;
      for (var i = 0; i < list.length; i++) {
        target = list[i];
        businessList.push({
          id: target.id,
          type: target.type,
          fmapID: target.fmapID || '',
          appName: target.appName || '',
          mapKey: target.mapKey || '',
          mapName: target.name || '',
          map: target.map || '',
          fid: target.fid || '',
          floor: target.floor || '',
          x: target.x || '',
          y: target.y || '',
          name: target.name || ''
        });
        html += '<div onclick="seleBusiness(this,\'' + list[i].id + '\')">' + list[i].name + '</div>';
      }
      var businessSelect = $('#shangjiaName');
      businessSelect.html(html);
      if (first) {
        businessSelect.prev().html(first.name);
        seleBusiness(null, first.id);
      }

    },
    error: function (jqXHR) {
      resError(jqXHR);
    }
  })
};

// 选择商家
function seleBusiness(that, id) {
  businessId = id;
  chooseBusinessObj = businessList.find((item) => item.id == id);
  seleBatch(that, chooseBusinessObj.name, function () {

    $('[name="destId"]').val(chooseBusinessObj.id);
    $('[name="shangjiaName"]').val(chooseBusinessObj.name);
    $('[name="fid"]').val(chooseBusinessObj.fid);
    $('[name="floor"]').val(chooseBusinessObj.floor);
    $('[name="x"]').val(chooseBusinessObj.x);
    $('[name="y"]').val(chooseBusinessObj.y);
    $('[name="desc"]').val('商家');

  });
};