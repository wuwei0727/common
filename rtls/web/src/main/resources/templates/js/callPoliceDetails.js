var ID; // 数据id


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
})

//初始化数据
function init() {
  $.ajax({
    url: url + 'peb/getParkingElevatorBindingById/' + ID,
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
        var data = res.data[0];
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



//保存
function save() {
  var sendData = getData('.main');
  var nameReg = /^[\u4e00-\u9fa5a-zA-Z0-9]+$/;
  if (!sendData) {
    return;
  }
  if (!nameReg.test(sendData.name)) {
    tips('名称只能包含汉字、英文或者数字！！！');
    return;
  }

  if (!sendData.map) {
    tips('请选择关联地图');
    return;
  }
  if (!sendData.x || !sendData.y) {
    tips('请在地图中选择模型，进行选点');
    return;
  }
  var path = url;
  if (ID) {
    path += 'peb/updateParkingElevatorBinding';
    sendData.id = +ID;
  } else {
    path += 'peb/addParkingElevatorBinding';

  }

  delete sendData.addTime;
  delete sendData.updateTime;
  sendData.map = +sendData.map
  sendData.floor = +sendData.floor
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
}

