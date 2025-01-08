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
    url: url + 'deviceAlarms/getDeviceAlarmsTypeConfigById',
    data: {
      id: ID
    },
    beforeSend: function () {
      loading();
    },
    complete: function () {
      removeLoad();
    },
    success: function (res) {
      console.log('resa', res);
      if (!res.code) {
        let newWindow = window.open('about:blank');
        newWindow.document.write(res);
        newWindow.focus();
        window.history.go(-1);
      } else if (res.code === 200) {
        var data = res.data[0];
        mapData = data;
        setData(data, '.main');
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
  }


  if (!sendData.deviceName) {
    tips('请输入设备类别');
    return;
  }
  if (!sendData.alarmName) {
    tips('请输入报警类型');
    return;
  }
  var path = url;
  if (ID) {
    path += 'deviceAlarms/editDeviceAlarmsTypeConfig';
    sendData.id = +ID;
  } else {
    path += 'deviceAlarms/addDeviceAlarmsTypeConfig';

  }

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

