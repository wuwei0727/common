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
  } else {
    // 初始化地区选择区
    initAreaSelect();
  }
});

function initAreaSelect() {
  $("#distpicker").distpicker({
    province: '--- 所在省 ---',
    city: '--- 所在市 ---',
    district: '--- 所在区 ---'
  });
};

//初始化数据
function init() {
  $.ajax({
    url: url + 'promoterInfo/selectOne?id=' + ID,
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
        $("#distpicker").distpicker({
          province: data.province,
          city: data.city,
          district: data.area
        });


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
  };

  // 获取地区值
  let province = $("#select1").val();
  let city = $("#select2").val();
  let district = $("#select3").val();

  if (!nameReg.test(sendData.name)) {
    tips('名称只能包含汉字、英文或者数字！！！');
    return;
  };

  if (!sendData.phone) {
    tips('请输入手机号码');
    return;
  } else {
    var reg_phone = /^1[3-9]\d{9}$/;
    if (!reg_phone.test(sendData.phone)) {
      tips('请输入正确的手机号码！！！');
      return;
    };
  };

  if (!province || !city || !district) {
    tips('请选择地区！！！');
    return;
  };

  sendData['province'] = province;
  sendData['city'] = city;
  sendData['area'] = district;

  var path = url;
  if (ID) {
    path += 'promoterInfo/updatePromoterInfo';
    sendData.id = +ID;
  } else {
    path += 'promoterInfo/addPromoterInfo';
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
}