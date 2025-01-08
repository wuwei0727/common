var debounceT = null; // 防抖
var apiURL = 'https://tuguiyao-gd.com/UWB2/';
var apiURLPay = 'https://tuguiyao-gd.com/UWB2/';
var code = null;
var configObj = {};

var payDataObj = {}; // 下单信息

var openId = null;

var appid = 'wx4445241794495a18';
var redirect_uri = apiURL + 'wxOAuth2/webPageAuth'


let url_text = `https://tuguiyao-gd.com/UWB2/page/pay/wechatJSApiPay.html`
let url_text2 = `9742-112-94-22-123.ngrok-free.app/page/index1.html` // config认证删除http前缀

const href = `http://open.weixin.qq.com/connect/oauth2/authorize?appid=${appid}&redirect_uri=${encodeURIComponent(url_text)}&response_type=code&scope=snsapi_base#wechat_redirect`;
console.log('href', href);

// 
(function main() {
  // 获取code
  code = getUrlStr('code');
  if (!code) {
    // 网页授权-获取code
    window.location.href = href;
  } else {

    // 有code后，开始业务-开始wx.config认证
    authentication();

  }
  console.log('code', code);
})();


// 获取code
function getUrlStr(name) {
  let reg = new RegExp('(^|&)' + name + '=([^&]*)(&|$)')
  let r = window.location.search.slice(1).match(reg);
  if (r != null) {
    return r[2]
  }
  return null
};

// wx.config 认证
function authentication() {
  $.ajax({
    type: "get",
    url: apiURL + "wxOAuth2/verifyJsSdk?url=" + encodeURIComponent(url_text),

    success: (result) => {
      console.log('result', result);

      configObj = {
        appId: result.appId, // 必填，公众号的唯一标识
        timestamp: result.timestamp + '',  // 必填，生成签名的时间戳
        nonceStr: result.nonceStr + '',    // 必填，生成签名的随机串
        signature: result.signature,  // 必填，签名
      }

      wx.config({
        debug: true, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
        appId: result.appId, // 必填，公众号的唯一标识
        timestamp: result.timestamp,  // 必填，生成签名的时间戳
        nonceStr: result.nonceStr,    // 必填，生成签名的随机串
        signature: result.signature,  // 必填，签名
        jsApiList: [
          "chooseWXPay",              //微信支付接口
        ]
      })
    }
  });
};

// 支付防抖
function debounceFn(cb, data) {
  if (debounceT) {
    clearTimeout(debounceT);
  }
  var callNow = !debounceT;
  debounceT = setTimeout(() => {
    debounceT = null;
  }, 1000)
  if (callNow) {
    cb(data)
  }
};

// 支付
function toPay() {
  // 下单
  $.ajax({
    url: apiURLPay + 'wechat/jsApiPay?openId=' + (openId || 'oMgIe6ya_uVBVoVVOpUUMHxVMKLQ'),
    // url: apiURLPay + 'wechat/jsApiPay?code=' + code,
    type: 'get',
    headers: {
      "Content-Type": "application/json;charset=UTF-8"
    },
    success: function (res) {
      console.log('下单', res);
      if (res) {
        let data = {};
        try {
          data = JSON.parse(res)
        } catch (error) {

        };
        console.log(data);
        if (data.appId || data.paySign) {
          payDataObj = data;
          toBuy();
        }
      }
    },
    error: function (jqXHR) {

    }
  })
};

function toBuy() {
  console.log('pay');
  if (typeof WeixinJSBridge == "undefined") {
    if (document.addEventListener) {
      document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
    } else if (document.attachEvent) {
      document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
      document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
    }
  } else {
    onBridgeReady();
  }
}

function onBridgeReady() {
  console.log('WeixinJSBridge', WeixinJSBridge);
  WeixinJSBridge.invoke('getBrandWCPayRequest', {
    "appId": payDataObj.appId,     //公众号ID，由商户传入     
    "timeStamp": payDataObj.timeStamp,     //时间戳，自1970年以来的秒数     
    "nonceStr": payDataObj.nonceStr,      //随机串     
    "package": payDataObj.package, //JSAPI下单接口返回的prepay_id参数值，提交格式如：prepay_id=***
    "signType": payDataObj.signType,     //微信签名方式     
    "paySign": payDataObj.paySign //微信签名 
  }, function (res) {
    if (res.err_msg == "get_brand_wcpay_request:ok") {
      // 使用以上方式判断前端返回,微信团队郑重提示：
      //res.err_msg将在用户支付成功后返回ok，但并不保证它绝对可靠
      // wx.miniProgram.getEnv(function (resT) {
      //   console.log(resT.miniprogram) // true
      //   if (resT.miniprogram) {
      //     // 如果是小程序打开的webview页面-回退到小程序
      //     //打包数据，发送给小程序接收区
      //     wx.miniProgram.postMessage({
      //       data: res
      //     });
      //     //返回上级，触发微信小程序获取信息函数，读取之前发送的数据
      //     wx.miniProgram.navigateBack({
      //       delta: 1,
      //     });
      //   }
      // })
    }
  });
}



// test
function submit() {
  let value = $("#openid")[0].value;
  openId = value;
};

//test