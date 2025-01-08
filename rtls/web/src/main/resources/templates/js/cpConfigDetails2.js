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
  } else {
    initT1();
    initT3();
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
    url: url + 'pac/getParkingAlertConfigById/' + ID,
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

        setData(data, '.main');

        currentMapId = $('[name="map"]').data('val');

        if (data.map) {
          $('[name="map"]').eq(0).addClass('batchTxtChange');
        };

        $('[name="holiday"]').eq(0).addClass('disable');
        $('[name="holiday"]').attr('disabled', 'disabled');
        $('.holiday').addClass('disable');

        if (data.t2TimePeriods1) {
          let arr = data.t2TimePeriods1.split(',')
          arr.forEach((item, index) => {
            if (index == 0) {
              initT1(item)
            } else {
              addT2TimeSolt(null, item)
            }
          })
        };

        if (data.t3TimePeriods1) {
          let arr = data.t3TimePeriods1.split(',')
          arr.forEach((item, index) => {
            if (index == 0) {
              initT3(item)
            } else {
              addT3TimeSolt(null, item)
            }
          })
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
  let t2TimeSlotNArr = [];
  $("[name='t2TimeSlotN']").each(function (e) {
    let val = $(this).val();
    if (val) {
      t2TimeSlotNArr.push(val)
    }
  });
  sendData.t2TimePeriods = t2TimeSlotNArr;



  let t3TimeSlotNArr = [];
  $("[name='t3TimeSlotN']").each(function (e) {
    let val = $(this).val();
    if (val) {
      t3TimeSlotNArr.push(val)
    }
  });
  sendData.t3TimePeriods = t3TimeSlotNArr;


  if (!sendData.configName) {
    tips('请输入配置名称');
    return;
  }

  if (!sendData.map) {
    tips('请选择关联地图');
    return;
  };

  if (sendData.holiday == 1) {
    if (!sendData.t1ThresholdHours) {
      tips('请输入时间阈值t1');
      return
    } else {
      if (isNaN(sendData.t1ThresholdHours)) {
        tips('请输入数字');
        return
      }
    }
  };

  if (!sendData.t2PeriodHours) {
    tips('请输入时间阈值t2');
    return
  } else {
    if (isNaN(sendData.t2PeriodHours)) {
      tips('请输入数字');
      return
    }
  }

  if (!sendData.t2SlotChangeLimit) {
    tips('请输入t2时间内车位变动次数');
    return
  } else {
    if (isNaN(sendData.t2SlotChangeLimit)) {
      tips('请输入数字');
      return
    }
  }

  if (sendData.holidayT2PeriodHours && isNaN(sendData.holidayT2PeriodHours)) {
    tips('请输入数字');
    return
  }

  if (sendData.holidayT2SlotChangeLimit && isNaN(sendData.holidayT2SlotChangeLimit)) {
    tips('请输入数字');
    return
  }

  if (!(sendData.t2TimePeriods && sendData.t2TimePeriods.length)) {
    tips('请选择t2时间段');
    return
  }

  if (!sendData.t3PeriodHours) {
    tips('请输入时间阈值t3');
    return
  } else {
    if (isNaN(sendData.t3PeriodHours)) {
      tips('请输入数字');
      return
    }
  }

  if (!sendData.t3SlotUsageLimit) {
    tips('请输入t3时间内车位变动次数');
    return
  } else {
    if (isNaN(sendData.t3SlotUsageLimit)) {
      tips('请输入数字');
      return
    }
  };

  if (sendData.holidayT3PeriodHours && isNaN(sendData.holidayT3PeriodHours)) {
    tips('请输入数字');
    return
  }

  if (sendData.holidayT3SlotChangeLimit && isNaN(sendData.holidayT3SlotChangeLimit)) {
    tips('请输入数字');
    return
  }

  if (!(sendData.t3TimePeriods && sendData.t3TimePeriods.length)) {
    tips('请选择t3时间段');
    return
  }

  delete sendData.t2TimeSlotN;
  delete sendData.t3TimeSlotN;

  console.log('sendData', sendData);

  var path = url;
  if (ID) {
    path += 'pac/updateParkingAlertConfig';
    sendData.id = +ID;
  } else {
    path += 'pac/addParkingAlertConfig';
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
            // init();
            location.reload();
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
  currentMapId = id;
  mapId = id;
  seleBatch(that, id, function () {
    $('[name="map"]').val(id);
  });
};


function initT1(appointmentSlot) {
  if (appointmentSlot) {
    $('#t2TS1').val(appointmentSlot);
  }
  jeDate('#t2TS1', {
    theme: {
      bgcolor: "#4A60CF",
      pnColor: "#4A60CF"
    },
    multiPane: false,
    range: "~",
    format: "hh:mm",
    donefun: function () { }
  });
};

function initT3(appointmentSlot) {
  if (appointmentSlot) {
    $('#t3TS1').val(appointmentSlot);
  }
  jeDate('#t3TS1', {
    theme: {
      bgcolor: "#4A60CF",
      pnColor: "#4A60CF"
    },
    multiPane: false,
    range: "~",
    format: "hh:mm",
    donefun: function () { }
  });
};

function initT2TimeSolt(domId, num) {
  if (num) {
    $(`#${domId}`).val(num);
  }
  jeDate(`#${domId}`, {
    theme: {
      bgcolor: "#4A60CF",
      pnColor: "#4A60CF"
    },
    multiPane: false,
    range: "~",
    format: "hh:mm",
    donefun: function () { }
  });
};

function addT2TimeSolt(e, num) {
  let random = Math.random() * 1000;
  random = Math.round(random) + "";

  let t = new Date().getTime();

  let mId = "m" + random + t;
  let iId = "i" + random + t;

  let html = `
    <div class="mainItemB" id="${mId}">
      <span class="itemTxt"><span class="require">*</span>t2时间段：</span>
      <input autocomplete="off" type="text" name="t2TimeSlotN" class="itemInput t2TimeSlotN" placeholder="请输入t2时间段"
        id="${iId}" />
      <span class="t2TimeSlotBtn" onclick="delT2TimeSolt('${mId}')">删除</span>
    </div>
  `;

  $("#t2TimeSlot").append(html);
  initT2TimeSolt(iId, num)
};

function delT2TimeSolt(dom) {
  $(`#${dom}`).remove();
};

function addT3TimeSolt(e, num) {
  let random = Math.random() * 1000;
  random = Math.round(random) + "";

  let t = new Date().getTime();

  let mId = "m" + random + t;
  let iId = "i" + random + t;

  let html = `
    <div class="mainItemB" id="${mId}">
      <span class="itemTxt"><span class="require">*</span>t3时间段：</span>
      <input autocomplete="off" type="text" name="t3TimeSlotN" class="itemInput t2TimeSlotN" placeholder="请输入t3时间段"
        id="${iId}" />
      <span class="t2TimeSlotBtn" onclick="delT2TimeSolt('${mId}')">删除</span>
    </div>
  `;

  $("#t3TimeSlot").append(html);
  initT2TimeSolt(iId, num)
};