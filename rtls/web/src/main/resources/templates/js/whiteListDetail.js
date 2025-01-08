var ID;
var mapId;
var mapList = [];
var currentPArr;

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
  };
  setTimeout(() => {
    loadSeleData();
  }, 100);
});


//加载下拉数据
function loadSeleData() {
  loadMapSeleData();
}

// 关联地图
function loadMapSeleData() {
  $.ajax({
    url: url + 'pac/getAllOrFilteredParkingAlertConfig',
    data: {
      pageSize: -1,
      status: 1,
    },
    success: function (res) {
      if (res.code !== 200) {
        tips(res.message);
        return;
      }
      mapList = [];
      var list = res.data.list;
      var html = '';
      var first = list.find(item => item.id == currentMapId);
      var target = null;
      for (var i = 0; i < list.length; i++) {
        target = list[i];
        mapList.push({
          id: target.id,
          name: target.configName,
          mapId: target.map
        });
        html += '<div onclick="seleMap(this,\'' + list[i].id + '\')">' + list[i].configName + '</div>';
      }
      var mapSelect = $('#mapSelect');
      mapSelect.html(html);
      if (first) {
        mapSelect.prev().html(first.configName);
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
    url: url + 'whitelist/getWhitelistById/' + ID,
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

        mapId = data.configId;

        currentPArr = data.placeId.split(',');
        console.log('currentPArr', currentPArr);
        loadPlaceSeleData(data.mapId);


        setData(data, '.main');

        currentMapId = $('[name="configId"]').data('val');

        if (data.configId) {
          $('[name="configId"]').eq(0).addClass('batchTxtChange');
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
  console.log('sendData', sendData);
  if (!sendData) {
    return;
  }

  if (!sendData.configId) {
    tips('请选择关联配置');
    return;
  }
  if (!sendData.placeId) {
    tips('请选择关联车位');
    return;
  };
  var path = url;
  if (ID) {
    path += 'whitelist/updateWhitelist';
    sendData.id = +ID;
  } else {
    path += 'whitelist/addWhitelist';
  }

  $.ajax({
    url: path,
    data: JSON.stringify(sendData),
    type: 'post',
    headers: {
      "Content-Type": "application/json"
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
  if (that) {
    seleMoreSeleData = [];
    currentPArr = [];
    $('[name="placeId"]').data('val', '');
    $('[name="placeId"]').removeClass('batchTxtChange');
    $('[name="placeId"]').html('--请选择--');
    let target = mapList.find((item) => {
      return item.id == id
    });
    loadPlaceSeleData(target.mapId);
  }

  currentMapId = id;
  mapId = id;
  seleBatch(that, id, function () {
    $('[name="map"]').val(id);
  });
};

var placeList = [];
function loadPlaceSeleData(id) {
  if (!id) return;
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
        let index = currentPArr.findIndex((item) => {
          return item == target.id
        });
        if (index != -1) {
          seleMoreSeleData.push({
            id: target.id,
            name: target.name
          })
          html += '<div class="seleMoreItem seleMoreItemAction" onclick="seleMore1(this,\'' + list[i].id + '\')">' + list[i].name + '</div>';
        } else {
          html += '<div class="seleMoreItem" onclick="seleMore1(this,\'' + list[i].id + '\')">' + list[i].name + '</div>';
        }
      }
      var placeId = $('#placeId');
      placeId.html(html);

      if (seleMoreSeleData.length) {
        let textArr = seleMoreSeleData.map((item) => {
          return item.name
        });
        $('[name="placeId"]').addClass('batchTxtChange');
        $('[name="placeId"]').html(textArr.join());
      }
    },
    error: function (jqXHR) {
      resError(jqXHR);
    }
  })
};

var seleMoreSeleData = [];
function seleMore1(e, id) {
  window.event.stopPropagation();

  let had = $(e).hasClass('seleMoreItemAction');
  if (had) {
    $(e).removeClass('seleMoreItemAction');
    let index = seleMoreSeleData.findIndex((item) => {
      return item.id == id
    });
    seleMoreSeleData.splice(index, 1)
  } else {
    $(e).addClass('seleMoreItemAction');
    let item = placeList.filter((item) => {
      return item.id == id
    });
    seleMoreSeleData.push(item[0]);
  };

  let idArr = seleMoreSeleData.map((item) => {
    return item.id
  });
  let textArr = seleMoreSeleData.map((item) => {
    return item.name
  });
  var showTxt = $(e).parent().prev();
  let val = idArr.join();
  let text = textArr.join()
  if (idArr && idArr.length) {
    $(showTxt[0]).addClass('batchTxtChange')
  } else {
    $(showTxt[0]).removeClass('batchTxtChange')
    text = '--请选择--'
  };


  showTxt.html(text).data('val', val);
};