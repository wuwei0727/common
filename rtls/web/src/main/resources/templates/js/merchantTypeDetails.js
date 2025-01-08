var ID;
var mapList = [];
var currentMapId;//初始化的id，不同则重新初始化地图

let photo;

// 裁剪上传图片
var fileName;
var treatUpdateFile;

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
    url: url + 'park/getShangJiangTypeById/' + ID,
    type: 'POST',
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
        setData(data, '.main');
        if (data.url) {
          $("#cancelUploadImg").prop("src", data.url);// 显示图片
        }
        if (ID) {
          photo = data.url;
        }
      } else {
        tips(res.message);
      }
    },
    error: function (jqXHR) {
      resError(jqXHR);
    }
  })
}

//保存
function save() {
  var sendData = getDataImg('.main');

  // 修改file为裁剪后的文件
  if (treatUpdateFile) {
    sendData.set('file', treatUpdateFile)
  };
  var file = sendData.get('file');
  if (file) {
    photo = '';
  }
  if (ID) {
    sendData.append('photo', photo);
  }

  if (!sendData.get('name')) {
    tips('请输入名称！！！');
    return;
  };
  if (!sendData.get('file') && !sendData.get('photo')) {
    tips('请选择图片！！！');
    return;
  }

  var path = url;
  if (ID) {
    path += 'park/updateShangJiangType';
    sendData.append('id', ID);
  } else {
    path += 'park/addShangJiangType';
  };
  $.ajax({
    url: path,
    data: sendData,
    type: 'post',
    processData: false,// jQuery不要去处理发送的数据
    contentType: false,// jQuery不要去设置Content-Type请求头
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

function cancelButton(that) {
  document.getElementById("file").value = "";
  var img = document.getElementById("cancelUploadImg");
  photo = '';
  img.src = '';
}

// 上传图片时裁剪
//弹出框水平垂直居中  
(window.onresize = function () {
  var win_height = $(window).height();
  var win_width = $(window).width();
  if (win_width <= 768) {
    $(".tailoring-content").css(
      {
        "top": (win_height - $(".tailoring-content")
          .outerHeight()) / 2,
        "left": 0
      });
  } else {
    $(".tailoring-content").css(
      {
        "top": (win_height - $(".tailoring-content")
          .outerHeight()) / 2,
        "left": (win_width - $(".tailoring-content")
          .outerWidth()) / 2
      });
  }
})();

// 选择文件触发事件  
function selectImg(file) {
  //文件为空，返回  
  if (!file.files || !file.files[0]) {
    return;
  };
  var files = file.files[0];
  if (!files.type.match('image/jpeg') && !files.type.match('image/png')) {
    tips('只能上传JPG和PNG格式的图片!');
    return;
  }
  // 设置图片名
  let random = Math.random() * 10000;
  random = Math.round(random) + "";
  fileName = file.files[0].name || (random + new Date().getTime() + '.jpeg');
  $(".tailoring-container").toggle();
  var reader = new FileReader();
  reader.onload = function (evt) {
    var replaceSrc = evt.target.result;
    // 更换cropper的图片  
    $('#tailoringImg').cropper('replace', replaceSrc, false);// 默认false，适应高度，不失真  
  }
  reader.readAsDataURL(file.files[0]);
};

// cropper图片裁剪  
$('#tailoringImg').cropper({
  aspectRatio: 1 / 1,// 默认比例  
  preview: '.previewImg',// 预览视图  
  guides: false, // 裁剪框的虚线(九宫格)  
  autoCropArea: 0.5, // 0-1之间的数值，定义自动剪裁区域的大小，默认0.8  
  movable: false, // 是否允许移动图片  
  dragCrop: true, // 是否允许移除当前的剪裁框，并通过拖动来新建一个剪裁框区域  
  movable: true, // 是否允许移动剪裁框  
  resizable: false, // 是否允许改变裁剪框的大小  
  zoomable: false, // 是否允许缩放图片大小  
  mouseWheelZoom: false, // 是否允许通过鼠标滚轮来缩放图片  
  touchDragZoom: true, // 是否允许通过触摸移动来缩放图片  
  rotatable: true, // 是否允许旋转图片  
  crop: function (e) {
    // 输出结果数据裁剪图像
  }
});

// 确定按钮点击事件  
$("#sureCut").on("click", async function () {
  if ($("#tailoringImg").attr("src") == null) {
    return false;
  } else {
    /* 生成圆形 */
    let canvas = getRoundedCanvas();
    var dataurl = canvas.toDataURL("image/png");

    treatUpdateFile = base64toFile(dataurl, 'circle' + fileName);

    $("#cancelUploadImg").prop("src", dataurl);// 显示图片 

    closeTailor();// 关闭裁剪框
  }
});

/* 生成圆形 */
function getRoundedCanvas() {
  var crop = $('#tailoringImg').data("cropper"); //获取crop对象
  var sourceCanvas = crop.getCroppedCanvas();
  var canvas = document.createElement('canvas');
  var context = canvas.getContext('2d');
  var width = sourceCanvas.width;
  var height = sourceCanvas.height;
  canvas.width = width;
  canvas.height = height;
  context.imageSmoothingEnabled = true;
  context.fillStyle = '#fff';
  context.fillRect(0, 0, width, height);
  context.drawImage(sourceCanvas, 0, 0, width, height);
  context.globalCompositeOperation = 'destination-in';
  context.beginPath();
  context.arc(width / 2, height / 2, Math.min(width, height) / 2, 0, 2 * Math.PI, true);
  context.fill();
  return canvas;
};

// 关闭裁剪框  
function closeTailor() {
  $(".tailoring-container").toggle();
  var _file = document.getElementById("file");
  _file.outerHTML = _file.outerHTML
}

// base64转文件
function base64toFile(base64Str, fileName) {
  let arr = base64Str.split(',');
  let mime = arr[0].match(/:(.*?);/)[1];
  let bstr = atob(arr[1]);
  let n = bstr.length;
  let u8arr = new Uint8Array(n);
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n);
  }
  return new File([u8arr], fileName, { type: mime })
};