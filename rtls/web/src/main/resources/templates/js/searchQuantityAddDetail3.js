var mapList = []; // 下拉地图数据
var businessList = [];
var allListData = [];
var currentObj = {};
let nowDateStr;

$(function () {
  nowDateStr = timeStampForDate();
  $(document).on('click', function (e) {
    hideSele();
  });
  initDate();
  loadMapSeleData();
});

function initDate(appointmentSlot) {
  if (appointmentSlot) {
    $('#time').val(appointmentSlot);
  }
  jeDate('#time', {
    theme: {
      bgcolor: "#4A60CF",
      pnColor: "#4A60CF"
    },
    multiPane: true,
    format: "YYYY-MM",
    maxDate: nowDateStr,
    donefun: function () {
    }
  });
};

// 关联地图
function loadMapSeleData() {
  $.ajax({
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
    },
    error: function (jqXHR) {
      resError(jqXHR);
    }
  })
};

async function loadBusinessSeleData(id) {

  let a = await loadBusinessSeleData1(id);
  let b = await loadBusinessSeleData2(id);
  let c = await loadBusinessSeleData3(id);


  allListData.push(...a, ...c, ...b);
  console.log('allListData', allListData);


  var list = allListData;
  var html = '';
  var target = null;
  for (var i = 0; i < list.length; i++) {
    target = list[i];
    businessList.push({
      id: target.id,
      name: target.name
    });
    html += '<div onclick="selePlace(this,\'' + list[i].id + '\')">' + list[i].name + '</div>';
  }
  var businessId = $('#businessId');
  businessId.html(html);

};

async function loadBusinessSeleData1(id) {
  let sortRes = [];
  var proResult = await new Promise((resolve, reject) => {
    $.ajax({
      url: url + 'park/getShangjia',
      data: {
        pageSize: -1,
        map: id,
      },
      success: function (res) {
        const data = res.data;
        if (!data || data.length == 0) {
          resolve(sortRes);
        };
        sortRes = data.map((item) => {
          return {
            id: item.id + 'n1',
            name: item.name,
            type: '3',
            x: item.x,
            y: item.y,
            z: '0',
            map: item.map,
            icon: 'search-map-icon',
            ename: '',
            floor: item.floor,
            fid: item.fid,
            desc: '类型：室内地图',
            outdoorType: '',
            databaseId: item.id
          }
        })
        resolve(sortRes)
      },
      error: function (err) {
        resolve(sortRes);
      }
    })
  });
  let res = sortRes;
  return res;
};

async function loadBusinessSeleData2(id) {
  let sortRes = [];
  var proResult = await new Promise((resolve, reject) => {
    $.ajax({
      url: url + 'park/getPlace',
      data: {
        pageSize: -1,
        map: id,
      },
      success: function (res) {
        const data = res.data;
        if (!data || data.length == 0) {
          resolve(sortRes);
        };
        sortRes = data.map((item) => {
          return {
            id: item.id + 'n2',
            name: item.name,
            type: '3',
            x: item.x,
            y: item.y,
            z: '0',
            map: item.map,
            icon: 'search-map-icon',
            ename: '',
            floor: item.floor,
            fid: item.fid,
            desc: '类型：室内地图',
            outdoorType: '',
            databaseId: item.id
          }
        })
        resolve(sortRes)
      },
      error: function (err) {
        resolve(sortRes);
      }
    })
  });
  let res = sortRes;
  return res;
};

async function loadBusinessSeleData3(id) {
  let sortRes = [];
  var proResult = await new Promise((resolve, reject) => {
    $.ajax({
      url: url + 'peb/getParkingElevatorBinding',
      data: {
        pageSize: -1,
        map: id,
      },
      success: function (res) {
        const data = res.data;
        if (!data || data.length == 0) {
          resolve(sortRes);
        };
        sortRes = data.map((item) => {
          return {
            id: item.id + 'n3',
            name: item.name,
            type: '6',
            x: item.x,
            y: item.y,
            z: '0',
            map: item.map,
            icon: 'search-map-icon',
            ename: '',
            floor: item.floor,
            fid: item.fid,
            desc: '类型：室内地图',
            outdoorType: '',
            databaseId: item.id
          }
        })
        resolve(sortRes)
      },
      error: function (err) {
        resolve(sortRes);
      }
    })
  });
  let res = sortRes;
  return res;
};

function selePlace(that, id) {
  currentObj = allListData.find((item) => item.id == id)
  seleBatch(that, id);
};

function seleMap(that, id) {
  loadBusinessSeleData(id)
  seleBatch(that, id);
};

function seleBatch2(that, num) {
  seleBatch(that, num)
}

//保存
function save() {
  var sendData = getData('.main');
  if (!sendData) {
    return;
  };

  if (!sendData.map) {
    tips('请选择地图');
    return
  }
  if (!sendData.time) {
    tips('请选择月份');
    return
  };
  if (!sendData.numbers) {
    tips('请输入检索数');
    return
  };
  if (!sendData.businessId) {
    tips('请选择关联目的地');
    return
  };
  let day
  let nowMonth = timeStampForDate('mm');
  if (nowMonth == sendData.time) {
    // 相同月份-上传当天天数
    day = new Date().getDate();
  } else {
    // 不同-上传选择月份的天数
    let timeArr = sendData.time.split('-');
    day = new Date(timeArr[0], timeArr[1], 0).getDate();
  };

  if (currentObj && currentObj.id) {
    sendData.id = currentObj.databaseId;
    sendData.name = currentObj.name;
    sendData.type = currentObj.type;
    sendData.x = currentObj.x;
    sendData.y = currentObj.y;
    sendData.z = currentObj.z;
    // sendData.map = currentObj.map;
    sendData.icon = currentObj.icon;
    sendData.ename = currentObj.ename;
    sendData.floor = currentObj.floor;
    sendData.fid = currentObj.fid;
    sendData.desc = currentObj.desc;
    sendData.outdoorType = currentObj.outdoorType;
    sendData.databaseId = currentObj.databaseId;
  } else {
    tips('请选择关联目的地');
    return
  }

  // sendData.type = "10";
  var year = sendData.time.split('-')[0];
  var month = sendData.time.split('-')[1];
  var numDays = day + '';
  var numbers = sendData.numbers;

  delete sendData.time;
  delete sendData.businessId;
  delete sendData.numbers;
  console.log('sendData', sendData);
  $.ajax({
    url: url + `variable_operational_data/addVariableOperationalData1?type=10&year=${year}&month=${month}&numDays=${numDays}&numbers=${numbers}`,
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
        tips(res.message || res.msg, function () {
          location.reload();
        }, 1000);
      } else {
        tips(res.message || res.msg);
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

function timeStampForDate(type) {
  let date = new Date();
  let Y = date.getFullYear();
  let M = '-' + (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1);
  let D = '-' + (date.getDate() < 10 ? '0' + date.getDate() : date.getDate());
  let h = ' ' + (date.getHours() < 10 ? '0' + date.getHours() : date.getHours());
  let m = ':' + (date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes());
  let s = ':' + (date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds());
  if (type == 'mm') {
    return Y + M;
  } else {
    return Y + M + D + h + m + s;
  }
};