var ID;
var mapId;
var mapList = [];
var companyList = [];

var currentMapId;
var companyId;

var getallnowcompanyarr = [];

$(function () {
  $(document).on('click', function (e) {
    hideSele();
  })
  initDate1();
  initDate2();
  initDate3();
  initDate4();
  initDate5();
  initDate6();
  initDate7();
  var id = getUrlStr('id');
  if (id) {
    ID = id;
    init();
    $('#titleFlag').html('编辑');
  } else {
    // setDateBox()
  }
  loadSeleData();
});

function setDateBox(num) {
  let html = `
    <input value='1' ${num == 1 ? 'checked' : ''} ${num ? 'disabled' : ''} type="checkbox" class='${num && num != 1 ? 'tabCheck' : ''}' name="tabCheck"><span class="dateName">星期一</span>
    <input value='2' ${num == 2 ? 'checked' : ''} ${num ? 'disabled' : ''} type="checkbox" class='${num && num != 2 ? 'tabCheck' : ''}' name="tabCheck"><span class="dateName">星期二</span>
    <input value='3' ${num == 3 ? 'checked' : ''} ${num ? 'disabled' : ''} type="checkbox" class='${num && num != 3 ? 'tabCheck' : ''}' name="tabCheck"><span class="dateName">星期三</span>
    <input value='4' ${num == 4 ? 'checked' : ''} ${num ? 'disabled' : ''} type="checkbox" class='${num && num != 4 ? 'tabCheck' : ''}' name="tabCheck"><span class="dateName">星期四</span>
    <input value='5' ${num == 5 ? 'checked' : ''} ${num ? 'disabled' : ''} type="checkbox" class='${num && num != 5 ? 'tabCheck' : ''}' name="tabCheck"><span class="dateName">星期五</span>
    <input value='6' ${num == 6 ? 'checked' : ''} ${num ? 'disabled' : ''} type="checkbox" class='${num && num != 6 ? 'tabCheck' : ''}' name="tabCheck"><span class="dateName">星期六</span>
    <input value='7' ${num == 7 ? 'checked' : ''} ${num ? 'disabled' : ''} type="checkbox" class='${num && num != 7 ? 'tabCheck' : ''}' name="tabCheck"><span class="dateName">星期日</span>
  `;

  $('#dateBox').html(html)
};

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
    url: url + 'timePeriod/getTimePeriodAdminInfoById/' + ID,
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
        getCompanylist();

        currentMapId = $('[name="mapId"]').data('val');

        if (data.mapId) {
          $('[name="mapId"]').eq(0).addClass('batchTxtChange');
        };

        if (data.companyId) {
          $('[name="companyId"]').eq(0).addClass('batchTxtChange');
        };

        getallnowcompany(mapId, companyId);

        // if (data.startTime && data.endTime) {
        //   initDate((data.startTime + '至' + data.endTime))
        // };


        if (data.dayOfWeek) {
          // setDateBox(data.dayOfWeek)

          // $('[name="dayOfWeek"]')[0].innerText = getJtad(data.dayOfWeek);
          // $('[name="dayOfWeek"]').data('val', data.dayOfWeek);
          // $('[name="dayOfWeek"]').eq(0).addClass('batchTxtChange');
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

function getallnowcompany(m, c) {
  $.ajax({
    url: url + `timePeriod/getTimePeriodAdminInfo?pageSize=-1&map=${m}&companyId=${c}`,
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
        var data = res.data.list;

        data.forEach((item) => {
          getallnowcompanyarr.push({
            dayOfWeek: item.dayOfWeek,
            id: item.id
          });

          if (item.dayOfWeek == 1) {
            initDate1((item.startTime + '至' + item.endTime))
          }
          if (item.dayOfWeek == 2) {
            initDate2((item.startTime + '至' + item.endTime))
          }
          if (item.dayOfWeek == 3) {
            initDate3((item.startTime + '至' + item.endTime))
          }
          if (item.dayOfWeek == 4) {
            initDate4((item.startTime + '至' + item.endTime))
          }
          if (item.dayOfWeek == 5) {
            initDate5((item.startTime + '至' + item.endTime))
          }
          if (item.dayOfWeek == 6) {
            initDate6((item.startTime + '至' + item.endTime))
          }
          if (item.dayOfWeek == 7) {
            initDate7((item.startTime + '至' + item.endTime))
          }

        })
      } else {
        tips(res.message);
      }
    },
    error: function (jqXHR) {
      resError(jqXHR);
    }
  })
}

function getJtad(num) {
  let text = '';
  switch (num) {
    case '1':
      text = '星期一';
      break
    case '2':
      text = '星期二';
      break
    case '3':
      text = '星期三';
      break
    case '4':
      text = '星期四';
      break
    case '5':
      text = '星期五';
      break
    case '6':
      text = '星期六';
      break
    case '7':
      text = '星期日';
      break
  }
  return text
};

//保存
function save() {
  var sendData = getData('.main');

  console.log('sendData', sendData);

  if (!sendData) {
    return;
  };
  if (!sendData.mapId) {
    tips('请选择地图')
    return
  } else {
    let mid = sendData.mapId;
    let data = mapList.find((item) => {
      return item.id == mid
    });
    sendData.mapName = data.mapName
  }
  if (!sendData.companyId) {
    tips('请选择公司')
    return
  } else {
    let cid = sendData.companyId;
    let data = companyList.find((item) => {
      return item.id == cid
    });
    sendData.companyName = data.name
  }
  // if (!arr.length) {
  //   tips('请选择日期')
  //   return
  // };

  let arr = []
  if (!sendData.time1 && !sendData.time2 && !sendData.time3 && !sendData.time4 && !sendData.time5 && !sendData.time6 && !sendData.time7) {
    tips('请选择时间')
    return
  } else {
    if (sendData.time1) {
      let s = sendData.time1.split('至')[0];
      let e = sendData.time1.split('至')[1];
      arr.push({
        s,
        e,
        d: 1
      })
    };
    if (sendData.time2) {
      let s = sendData.time2.split('至')[0];
      let e = sendData.time2.split('至')[1];
      arr.push({
        s,
        e,
        d: 2
      })
    };
    if (sendData.time3) {
      let s = sendData.time3.split('至')[0];
      let e = sendData.time3.split('至')[1];
      arr.push({
        s,
        e,
        d: 3
      })
    };
    if (sendData.time4) {
      let s = sendData.time4.split('至')[0];
      let e = sendData.time4.split('至')[1];
      arr.push({
        s,
        e,
        d: 4
      })
    };
    if (sendData.time5) {
      let s = sendData.time5.split('至')[0];
      let e = sendData.time5.split('至')[1];
      arr.push({
        s,
        e,
        d: 5
      })
    };
    if (sendData.time6) {
      let s = sendData.time6.split('至')[0];
      let e = sendData.time6.split('至')[1];
      arr.push({
        s,
        e,
        d: 6
      })
    };
    if (sendData.time7) {
      let s = sendData.time7.split('至')[0];
      let e = sendData.time7.split('至')[1];
      arr.push({
        s,
        e,
        d: 7
      })
    };
  }

  delete sendData.time

  var path = url;
  var ptype = 'post'
  var pdata;
  if (ID) {
    for (let i = 1; i <= 7; i++) {

      let d = i;
      let hadList = getallnowcompanyarr.find((item) => {
        return item.dayOfWeek == d
      });
      let hadTime = arr.find((item) => {
        return item.d == d
      })

      if (hadList && hadTime) {
        path = 'timePeriod/editTimePeriodAdminInfo';
        ptype = 'put'

        pdata = {
          id: hadList.id,
          companyId: sendData.companyId,
          companyName: sendData.companyName,
          dayOfWeek: hadTime.d,
          endTime: hadTime.e,
          mapId: sendData.mapId,
          mapName: sendData.mapName,
          startTime: hadTime.s
        };

        api(path, ptype, pdata);
      };
      if (!hadList && hadTime) {
        path = 'timePeriod/addTimePeriodAdminInfo';
        ptype = 'post'
        pdata = [{
          companyId: sendData.companyId,
          companyName: sendData.companyName,
          dayOfWeek: hadTime.d,
          endTime: hadTime.e,
          mapId: sendData.mapId,
          mapName: sendData.mapName,
          startTime: hadTime.s
        }]

        api(path, ptype, pdata);
      }
      if (hadList && !hadTime) {
        path = 'timePeriod/editTimePeriodAdminInfo';
        ptype = 'put'

        pdata = {
          id: hadList.id,
          companyId: sendData.companyId,
          companyName: sendData.companyName,
          dayOfWeek: hadList.dayOfWeek,
          endTime: "00:00",
          mapId: sendData.mapId,
          mapName: sendData.mapName,
          startTime: "00:00"
        };

        api(path, ptype, pdata);
      }
    }
  } else {
    path = 'timePeriod/addTimePeriodAdminInfo';
    pdata = [];

    arr.forEach((item) => {
      pdata.push({
        companyId: sendData.companyId,
        companyName: sendData.companyName,
        dayOfWeek: item.d,
        endTime: item.e,
        mapId: sendData.mapId,
        mapName: sendData.mapName,
        startTime: item.s
      })
    });

    api(path, ptype, pdata);
  }
}

function api(path, ptype, pdata) {
  $.ajax({
    url: url + path,
    data: JSON.stringify(pdata),
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


function initDate1(appointmentSlot) {
  if (appointmentSlot) {
    $('#time1').val(appointmentSlot);
  }
  jeDate('#time1', {
    theme: {
      bgcolor: "#4A60CF",
      pnColor: "#4A60CF"
    },
    multiPane: false,
    range: "至",
    format: "hh:mm",
    donefun: function () {
    }
  });
}

function initDate2(appointmentSlot) {
  if (appointmentSlot) {
    $('#time2').val(appointmentSlot);
  }
  jeDate('#time2', {
    theme: {
      bgcolor: "#4A60CF",
      pnColor: "#4A60CF"
    },
    multiPane: false,
    range: "至",
    format: "hh:mm",
    donefun: function () {
    }
  });
}

function initDate3(appointmentSlot) {
  if (appointmentSlot) {
    $('#time3').val(appointmentSlot);
  }
  jeDate('#time3', {
    theme: {
      bgcolor: "#4A60CF",
      pnColor: "#4A60CF"
    },
    multiPane: false,
    range: "至",
    format: "hh:mm",
    donefun: function () {
    }
  });
}

function initDate4(appointmentSlot) {
  if (appointmentSlot) {
    $('#time4').val(appointmentSlot);
  }
  jeDate('#time4', {
    theme: {
      bgcolor: "#4A60CF",
      pnColor: "#4A60CF"
    },
    multiPane: false,
    range: "至",
    format: "hh:mm",
    donefun: function () {
    }
  });
}

function initDate5(appointmentSlot) {
  if (appointmentSlot) {
    $('#time5').val(appointmentSlot);
  }
  jeDate('#time5', {
    theme: {
      bgcolor: "#4A60CF",
      pnColor: "#4A60CF"
    },
    multiPane: false,
    range: "至",
    format: "hh:mm",
    donefun: function () {
    }
  });
}

function initDate6(appointmentSlot) {
  if (appointmentSlot) {
    $('#time6').val(appointmentSlot);
  }
  jeDate('#time6', {
    theme: {
      bgcolor: "#4A60CF",
      pnColor: "#4A60CF"
    },
    multiPane: false,
    range: "至",
    format: "hh:mm",
    donefun: function () {
    }
  });
}

function initDate7(appointmentSlot) {
  if (appointmentSlot) {
    $('#time7').val(appointmentSlot);
  }
  jeDate('#time7', {
    theme: {
      bgcolor: "#4A60CF",
      pnColor: "#4A60CF"
    },
    multiPane: false,
    range: "至",
    format: "hh:mm",
    donefun: function () {
    }
  });
}