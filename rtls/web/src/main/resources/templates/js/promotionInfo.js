var ID;


$(function () {
  $(document).on('click', function (e) {
    hideSele();
  })
  var id = getUrlStr('id');
  if (id) {
    ID = id;
    init();
  };
});

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
        mapData = data;
        setData(data, '.main');

        $("#distpicker").distpicker({
          province: data.province,
          city: data.city,
          district: data.area
        });

        let countList = res.data.promoterCount || [];

        if (!countList.length) return;

        let businessPlugNum = {}
        let mapPlugNum = {}
        countList.forEach((item) => {
          if (item.shangjiaName) {
            if (!businessPlugNum[item.shangjiaName]) {
              businessPlugNum[item.shangjiaName] = 1
            } else {
              businessPlugNum[item.shangjiaName] += 1
            }
          } else {
            if (!mapPlugNum[item.mapName]) {
              mapPlugNum[item.mapName] = 1
            } else {
              mapPlugNum[item.mapName] += 1
            }
          }
        })


        if (JSON.stringify(mapPlugNum) != "{}") {
          let html = '';
          for (key in mapPlugNum) {
            html += `<div class="disable noneData"> ${key} : ${mapPlugNum[key]}次 </div>`
          };
          $("#mappulg").html(html)
        };

        if (JSON.stringify(businessPlugNum) != "{}") {
          let html = '';
          for (key in businessPlugNum) {
            html += `<div class="disable noneData"> ${key} : ${businessPlugNum[key]}次 </div>`
          };
          $("#businesspulg").html(html)
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
};