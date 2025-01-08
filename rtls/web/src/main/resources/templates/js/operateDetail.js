var ID;

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
});

//初始化数据
function init() {
  $.ajax({
    url: url + 'variable_operational_data/getVariableOperationalData?id=' + ID,
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
  };

  if (!(sendData.placeNavigationTotal || sendData.placeNavigationTotal === 0)) {
    tips('请输入车位导航总数！！！');
    return;
  };
  if (!(sendData.placeNavigationUseRate || sendData.placeNavigationUseRate === 0)) {
    tips('请输入车位导航使用率！！！');
    return;
  };
  if (!(sendData.platformPlaceUtilizationRate || sendData.platformPlaceUtilizationRate === 0)) {
    tips('请输入平台车位利用率！！！');
    return;
  };
  if (!(sendData.reservationTotal || sendData.reservationTotal === 0)) {
    tips('请输入车位预约总数！！！');
    return;
  };
  if (!(sendData.reverseCarSearchTotal || sendData.reverseCarSearchTotal === 0)) {
    tips('请输入反向寻车总数！！！');
    return;
  };
  if (!(sendData.placeAvailabilityRate || sendData.placeAvailabilityRate === 0)) {
    tips('请输入停车场车位空闲率！！！');
    return;
  };
  if (!(sendData.userTotal || sendData.userTotal === 0)) {
    tips('请输入请输入用户总数！！！');
    return;
  };
  if (!(sendData.useFrequency || sendData.useFrequency === 0)) {
    tips('请输入访问总次数！！！');
    return;
  };
  if (!(sendData.activeUserTotal || sendData.activeUserTotal === 0)) {
    tips('请输入活跃用户数！！！');
    return;
  };
  if (!(sendData.newUsersNumber || sendData.newUsersNumber === 0)) {
    tips('请输入新增用户数！！！');
    return;
  };
  if (!(sendData.userSearchTotal || sendData.userSearchTotal === 0)) {
    tips('请输入用户检索总数！！！');
    return;
  };
  if (!(sendData.locationShareTotal || sendData.locationShareTotal === 0)) {
    tips('请输入位置分享总数！！！');
    return;
  };
  if (!(sendData.perHourNullPlaceNumber || sendData.perHourNullPlaceNumber === 0)) {
    tips('请输入每小时空车位数！！！');
    return;
  };
  if (!(sendData.detectorCount || sendData.detectorCount === 0)) {
    tips('请输入检测器数量！！！');
    return;
  };
  if (!(sendData.subCount || sendData.subCount === 0)) {
    tips('请输入信标数量！！！');
    return;
  };
  if (!(sendData.gatewayCount || sendData.gatewayCount === 0)) {
    tips('请输入网关数量！！！');
    return;
  };
  if (!(sendData.placeUseTotal || sendData.placeUseTotal === 0)) {
    tips('请输入车位使用次数！！！');
    return;
  };
  if (!(sendData.placeIdleTotalDuration || sendData.placeIdleTotalDuration === 0)) {
    tips('请输入车位空闲总时长！！！');
    return;
  };
  if (!(sendData.mapPlaceUtilizationRate || sendData.mapPlaceUtilizationRate === 0)) {
    tips('请输入车位利用率前10停车场！！！');
    return;
  };
  if (!(sendData.idlePlaceNumber || sendData.idlePlaceNumber === 0)) {
    tips('请输入空车位数据！！！');
    return;
  };
  if (!(sendData.monthlyActiveUsers || sendData.monthlyActiveUsers === 0)) {
    tips('请输入月活用户数！！！');
    return;
  };
  if (!(sendData.top10Business || sendData.top10Business === 0)) {
    tips('请输入检索前10商家！！！');
    return;
  };


  var path = url;
  if (ID) {
    path += 'variable_operational_data/editVariableOperationalData';
    sendData.id = +ID;
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