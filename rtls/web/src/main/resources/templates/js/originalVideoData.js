var where = {};
var list = [];
var fdfsUrl = 'http://192.168.1.95:7003/';


$(function () {
  $(document).on('click', function (e) {
    hideSele();
  })

  where.pageIndex = 1;
  where.pageSize = pageSize;
  loadSeleData();

  jeDate("#timeDate", {
    theme: {
      bgcolor: "#4A60CF",
      pnColor: "#4A60CF"
    },
    multiPane: false,
    range: " 至 ",
    format: "YYYY-MM-DD hh:mm:ss"
  });

  init();
});

// 加载下拉数据
function loadSeleData() {
  loadFun('map/getMap2dSel', { pageSize: -1, enable: 1 }, '#mapSelect');
};

// 初始化列表
function init() {
  $.ajax({
    url: url + 'api/cameraVehicleCapture/getAllOrFilteredCameraVehicleCapture',
    data: where,
    type: 'get',
    beforeSend: function () {
      loading();
    },
    complete: function () {
      removeLoad();
    },
    success: function (res) {
      var tab = $('#tab');
      if (res.code !== 200) {
        tableException(tab, res.message);
        return;
      }
      var data = res.data;
      list = data.list;
      var len = list.length;
      if (!len) {
        tableException(tab, '当前搜索结果为空');
      }

      //全选按钮（取消）
      var allChe = $('#allCheck');
      if (allChe.hasClass('curSele')) {
        allChe.removeClass('curSele');
      }
      //全选按钮（取消）

      var allChe = $('#allCheck');
      if (allChe.prop('checked')) {
        allChe.prop('checked', false);
      }
      if (pageIndex != data.pageIndex) {
        pageIndex = data.pageIndex;
      }
      var allName = getTheadName(tab.find('thead'));

      var html = '';
      var target = null;
      var name = null;
      var value = null;
      var lineNum = (pageIndex - 1) * pageSize + 1;
      for (var i = 0; i < len; i++) {
        target = list[i];
        html += '<tr>';
        for (var j = 0; j < allName.length; j++) {
          name = allName[j];
          if (name === '') {
            continue;
          }
          value = target[name];
          if (name === 'line') {
            html += resTabCheck(target, lineNum + i, 'id', 'name');
          } else if (name === 'imgUrl') {
            if (convertNull(value)) {
              html += '<td><img class="tabImg pic" src="' + (fdfsUrl + value) + '" /></td>';
            } else {
              html += '<td></td>';
            }
          } else if (name == 'operating') {
            var deleteBtn = document.getElementById("deleteBtn");
            var delteTxt = '';
            if (deleteBtn) {
              delteTxt = '<span class="tabOper" onclick="showDele(\'' + target.id + '\',\'' + target.name + '\')">删除</span>'
            };

            html = html + '<td>' + delteTxt + '</td>';
          } else {
            html += '<td>' + convertNull(value) + '</td>';
          }
        }
        html += '</tr>';
      }
      tab.find('tbody').html(html);
      var htmlStr = "共 <span class='c4A60CF'>" + data.pages + " </span> 页 / <span class='c4A60CF'>" + data.total + " </span>条数据"
      $('[id="total"]').html(htmlStr);
      //生成页码
      initDataPage(pageIndex, data.pages, data.total);

      // 图片预览
      $(".pic").click(function () {
        var _this = $(this);//将当前的pimg元素作为_this传入函数 
        imgShow("#outerdiv", "#innerdiv", "#bigimg", _this);
      });
    },
    error: function (jqXHR) {
      resError(jqXHR);
    }
  })
};

// 翻页
function turnPage() {
  where.pageIndex = pageIndex;
  init();
};

// 位置显示商家信息的
function showlocation(id) {
  var idmai = id
  $('#mapPop').show();
  $('#mask').show();

  let result = contrastReturn(list, 'id', idmai);

  openMap({
    fKey: result.mapKey || '',
    fName: result.appName || '',
    fId: result.fmapID || '',
    focusFloor: Number(result.floor) || '',
    path: result.themeImg || '',
    x: result.x,
    y: result.y,
    num: result.name,
    //初始指北针的偏移量
    compassOffset: [20, 12],
    //指北针大小默认配置
    compassSize: 48,
    mapId: result.map
  }, function () {
    analyser = new fengmap.FMSearchAnalyser(fMap);
    var x = +$('[name="x"]').val();
    var y = +$('[name="y"]').val();
    if (x && y) {
      fMap.moveTo({
        x: x,
        y: y,
        groupID: floor,
      })
    }
    var fid = $('[name="fid"]').val();
    var target = null;
    if (fid) {
      target = findModel({ FID: fid });
      if (target) {
        target.selected = true;
        selectedModel = target;
      }
    }
  });
};

// 搜索
function search() {
  where.pageIndex = 1;
  var searchItem = $('.search').find('[name]');
  var target = null;
  var tarName;
  var tarVal;
  var tarTxt;
  for (var i = 0; i < searchItem.length; i++) {
    target = searchItem[i];
    tarName = target.getAttribute('name');
    if (target.className.indexOf('batchTxt') !== -1) {
      //自定义的下拉
      tarTxt = target.innerHTML;
      tarVal = $(target).data('val');
    } else {
      tarTxt = tarVal = target.value;
    }
    where[tarName] = tarVal;
    if (typeof exportArr === "object") {
      exportArrMatch(exportArr, tarName, tarVal, tarTxt);
    }
  };

  var timeDate = $('#timeDate').val().split(' 至 ');
  var s = timeDate[0] || '';
  var e = timeDate[1] || '';
  where.start = s;
  where.end = e;
  init();
};

//页面重置
function pageReset() {
  $('#timeDate').val('');
  resetSearch();
};

// 图片预览
function imgShow(outerdiv, innerdiv, bigimg, _this) {
  var src = _this.attr("src");//获取当前点击的pimg元素中的src属性 
  $(bigimg).attr("src", src);//设置#bigimg元素的src属性 
  /*获取当前点击图片的真实大小，并显示弹出层及大图*/
  $("<img/>").attr("src", src).load(function () {
    var windowW = $(window).width();//获取当前窗口宽度 
    var windowH = $(window).height();//获取当前窗口高度 
    var realWidth = this.width;//获取图片真实宽度 
    var realHeight = this.height;//获取图片真实高度 
    var imgWidth, imgHeight;
    var scale = 0.8;//缩放尺寸，当图片真实宽度和高度大于窗口宽度和高度时进行缩放 
    if (realHeight > windowH * scale) {//判断图片高度 
      imgHeight = windowH * scale;//如大于窗口高度，图片高度进行缩放 
      imgWidth = imgHeight / realHeight * realWidth;//等比例缩放宽度 
      if (imgWidth > windowW * scale) {//如宽度扔大于窗口宽度 
        imgWidth = windowW * scale;//再对宽度进行缩放 
      }
    } else if (realWidth > windowW * scale) {//如图片高度合适，判断图片宽度 
      imgWidth = windowW * scale;//如大于窗口宽度，图片宽度进行缩放 
      imgHeight = imgWidth / realWidth * realHeight;//等比例缩放高度 
    } else {//如果图片真实高度和宽度都符合要求，高宽不变 
      imgWidth = realWidth;
      imgHeight = realHeight;
    }
    $(bigimg).css("width", imgWidth);//以最终的宽度对图片缩放 
    var w = (windowW - imgWidth) / 2;//计算图片与窗口左边距 
    var h = (windowH - imgHeight) / 2;//计算图片与窗口上边距 
    $(innerdiv).css({ "top": h, "left": w });//设置#innerdiv的top和left属性 
    $(outerdiv).fadeIn("fast");//淡入显示#outerdiv及.pimg 
  });
  $(outerdiv).click(function () {//再次点击淡出消失弹出层 
    $(this).fadeOut("fast");
  });
};

//单行的-解除报警提示
function showDele(id, txt) {
  ids = id + "";
  $('#deleTxt').text(txt);
  showPop('#delePop');
}

//多行的-解除报警
function showAllDele() {
  var cheInp = $('#tab tbody').find('input:checked');
  if (!cheInp.length) {
    tips('请选择至少一条数据');
    return;
  }
  var showTxt = '';
  var cheId = '';
  for (var i = 0; i < cheInp.length; i++) {
    showTxt += cheInp[i].getAttribute('data-txt') + '、';
    cheId += cheInp[i].value + ',';
  }

  showTxt = showTxt.slice(0, -1);
  ids = cheId.slice(0, -1);
  $('#deleTxt').html(showTxt);
  showPop('#delePop');
}

//确认-解除报警
function entDele() {
  $.ajax({
    url: url + 'api/cameraVehicleCapture/delCameraVehicleCapture/' + ids,
    type: 'DELETE',
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
      tips(res.message);
      if (res.code == 200) {
        hidePop('#delePop');
        search();
        // 刷新home中的报警内容
        window.parent.setCallPolice();
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
