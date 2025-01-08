var ID;
var mapList = [];
var currentMapId;//初始化的id，不同则重新初始化地图
//定义搜索分析类
var analyser = null;
//选中的模型
var selectedModel = null;
var mapData = {};
var site = null;
var flag = false;
var buyTime = null;
var buyObj = {
  map: '',
  id: '',
};
var orderStateTime = null; // 轮询订单状态
var out_trade_no;// 订单号
var overdueTime = null; // 二维码过期定时器
var scanCodeType = false; // 是否扫码了

var wxorderNo; // 微信下单支付单号
var wxcodetime = 12; // 微信支付二维码过期时间
// 充值数据
var rechargeList = [{
  id: 1,
  num: 1000,
  money: 0.01
}, {
  id: 2,
  num: 2000,
  money: 70.00
}, {
  id: 3,
  num: 5000,
  money: 175.00
}, {
  id: 4,
  num: 15000,
  money: 521.70
}, {
  id: 5,
  num: 50000,
  money: 1720.00
}]

$(function () {
  // 设置充值内容
  initCharge();
  $(document).on('click', function (e) {
    hideSele();
  })
  var id = getUrlStr('id');
  if (id) {
    ID = id;
    init();
  }
});

// 遍历显示充值内容
function initCharge() {
  // 后台获取充值数量内容
  $.ajax({
    url: url + 'product/list',
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
        // todo
        console.log(res);
        rechargeList = res.data;
        showChargeItemToHtml();
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

function showChargeItemToHtml() {
  let html = '';
  rechargeList.forEach((item) => {
    html += `
      <div class="rechargeItem" onclick="rechargeItem(this, ${item.id}, ${item.price})">
        ${item.count / 10000 > 1 ? (item.count / 10000) + '万' : item.count}条
      </div>
    `
  });
  $("#rechargeBox").html(html)
};

//初始化数据
function init() {
  $.ajax({
    url: url + 'smsQuota/getSmsQuotaById?id=' + ID,
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

// 选择充值数量
function rechargeItem(e, id, money) {
  buyObj = {
    productId: id,
    map: +mapData.map,
  }
  $("#recharge_money").html(money.toFixed(2))
  $(".rechargeItem").removeClass('rechargeItem_active')
  $(e).addClass('rechargeItem_active');
}

// 立即充值
function toBuy() {
  // 防抖
  if (buyTime) {
    clearTimeout(buyTime);
  }
  var callNow = !buyTime;
  buyTime = setTimeout(() => {
    buyTime = null;
  }, 800)
  if (callNow) {
    if (!buyObj.productId) {
      tips('请选择充值条数！');
      return false
    };
    console.log('todo', buyObj);
    // 清除轮询
    clearInterval(orderStateTime);
    orderStateTime = null;
    clearInterval(overdueTime);
    overdueTime = null;
    $("#QrBox").css('display', 'none')
    $("#wxQrBox").css('display', 'none')
    $("#wxqrcode").html('');
    $("#iframe").attr('src', 'about:blank');


    placeOrder(); // 下单
  }
}

// 下单
function placeOrder() {
  $.ajax({
    url: url + 'aliPay/pcPay',
    data: buyObj,
    type: 'get',
    headers: {
      "Content-Type": "application/json;charset=UTF-8"
    },
    beforeSend: function () {
      loading();
    },
    success: function (res) {
      // 下单成功后显示二维码
      let ifame = document.getElementById("iframe");
      let ed = document.all ? ifame.contentWindow.document : ifame.contentDocument
      ed.open();
      ed.write(res);
      ed.close();
      ed.contentEditable = true;
      ed.designMode = 'on'
      $("#QrBox").css('display', 'table')
      removeLoad();

      let dom = JSON.parse(JSON.stringify(res));
      let domarr = dom.split('<script');
      $("#form_hide").html(domarr[0]);
      // 获取订单号
      var orderData = $('[name="biz_content"]').val();
      var orderObj = JSON.parse(orderData);
      console.log('orderObj', orderObj);
      out_trade_no = orderObj.out_trade_no;
      let overdue = orderObj.time_expire;
      orderState(out_trade_no);//轮询订单支付状态
      let overduetime = new Date(overdue).getTime();
      orderOverdueTime(overduetime);// 二维码过期时间

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

// 轮询订单支付状态
function orderState(id) {
  orderStateTime = setInterval(() => {
    $.ajax({
      url: url + 'aliPay/getOrderStatus?orderNo=' + id,
      type: 'get',
      headers: {
        "Content-Type": "application/json;charset=UTF-8"
      },
      success: function (res) {
        console.log('orderState', res);
        if (res.code == 200) {
          // 扫码后-等待支付
          if (res.message == '支付成功') {
            // 支付成功

            // 清除轮询
            clearInterval(orderStateTime);
            orderStateTime = null;
            clearInterval(overdueTime);
            overdueTime = null;

            // 提示支付成功
            tips('支付成功', function () {
              // 刷新页面
              location.reload();
            });
          };
          if (res.message == '超时已关闭') {
            // 超时未支付
            // 清除轮询
            clearInterval(orderStateTime);
            orderStateTime = null;
            clearInterval(overdueTime);
            overdueTime = null;
            // 提示订单过期
            tips('订单过期，请重新选择', function () {
              // 刷新页面
              location.reload();
            });
          };
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
  }, 5000);
};

// 二维码过期时间
function orderOverdueTime(overdue) {
  let nowtime = new Date().getTime();
  let lifespan = (overdue - nowtime) / 1000;
  let time = Math.round(lifespan)
  overdueTime = setInterval(() => {
    let h = parseInt(time / 3600);
    let m = parseInt(time / 60 % 60);
    let s = parseInt(time % 60);
    let showTime = addZero(h) + ':' + addZero(m) + ':' + addZero(s);
    $("#QrOverdue").html(showTime)
    if (time <= 0) {
      clearInterval(overdueTime);
      overdueTime = null;
      if (!scanCodeType) {
        // 提示二维码过期
        tips('二维码过期，请重新选择', function () {
          // 刷新页面
          location.reload();
        });
      }
    }
    time--;
  }, 1000);

};
// 封装函数，当数值小于10时在前面加“0”
function addZero(num) {
  if (num < 10) {
    return "0" + num;
  }
  return num;
}

// 返回
function back() {
  clearInterval(orderStateTime);
  orderStateTime = null;
  clearInterval(overdueTime);
  overdueTime = null;
  location.reload();
  window.parent.closeFrame();
}


// 微信支付
function wxtoBuy() {
  // 防抖
  if (buyTime) {
    clearTimeout(buyTime);
  }
  var callNow = !buyTime;
  buyTime = setTimeout(() => {
    buyTime = null;
  }, 800)
  if (callNow) {
    if (!buyObj.productId) {
      tips('请选择充值条数！');
      return false
    };
    console.log('todo', buyObj);
    // 清除轮询
    clearInterval(orderStateTime);
    orderStateTime = null;
    clearInterval(overdueTime);
    overdueTime = null;
    $("#QrBox").css('display', 'none')
    $("#wxQrBox").css('display', 'none')
    $("#wxqrcode").html('');
    $("#iframe").attr('src', 'about:blank');


    wxplaceOrder(); // 微信下单
  }
};

function wxplaceOrder() {
  $.ajax({
    url: url + 'wechat/nativePay',
    data: buyObj,
    type: 'get',
    headers: {
      "Content-Type": "application/json;charset=UTF-8"
    },
    beforeSend: function () {
      loading();
    },
    success: function (res) {
      removeLoad();
      console.log('wxres', res);
      if (res.code == '200') {
        // 下单成功
        if (res.data && res.data.codeUrl) {
          // 用返回支付链接
          var wxqrcode = new QRCode(document.getElementById('wxqrcode'));
          wxqrcode.makeCode(res.data.codeUrl);
          $("#wxQrBox").css('display', 'table');
          wxorderOverdueTime();
          wxorderNo = res.data.orderNo;
          orderState(wxorderNo);//轮询订单支付状态
        };

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

// 微信二维码过期时间
function wxorderOverdueTime() {
  let overdue = new Date().getTime() + (wxcodetime * 60 * 1000);
  let nowtime = new Date().getTime();
  let lifespan = (overdue - nowtime) / 1000;
  let time = Math.round(lifespan);

  overdueTime = setInterval(() => {
    let h = parseInt(time / 3600);
    let m = parseInt(time / 60 % 60);
    let s = parseInt(time % 60);
    let showTime = addZero(h) + ':' + addZero(m) + ':' + addZero(s);
    $("#wxQrOverdue").html(showTime)
    if (time <= 0) {
      clearInterval(overdueTime);
      overdueTime = null;
      if (!scanCodeType) {
        // 提示二维码过期
        tips('二维码过期，请重新选择', function () {
          // 刷新页面
          location.reload();
        });
      }
    }
    time--;
  }, 1000);
}