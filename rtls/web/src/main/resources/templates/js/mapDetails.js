var ID;
var maiurl;
var local;
var maptheme;
var fdfsUrl = 'http://192.168.1.95:7003/';
let welcomePagePath;
let mapLogo;
$(function () {
    $(document).on('click', function (e) {
        hideSele();
    })
    var id = getUrlStr('id');
    maptheme = JSON.parse(decodeURI(getUrlStr('maptheme')));
    loadSeleData();
    if(id && id!=='undefined'){
        ID = id;
        init();
        $('#titleFlag').html('编辑');
        document.getElementById('daoru').style.visibility='visible'
    }
})

//加载下拉数据
function loadSeleData() {
    var html ='';
    for(var i = 0; i < maptheme.length; i++) {
        html += '<div onclick="seleBatch(this,\'' + maptheme[i].themeName + '\')" data-themeName="' + maptheme[i].themeName + '" title="' + maptheme[i].themeName + '">' + maptheme[i].themeName + '</div>';
    }
    $('#themeSelect').html(html);
}

//选点显示地图
function selectPoint(){

    $('#mapPop').show();
    $('#mask').show();
    // 在指定容器创建地图实例并设置最大最小缩放级别
    var map = new BMapGL.Map("allmap", {
        minZoom: 5,
        maxZoom: 19
    });

// 初始化地图，设置中心点和显示级别
    map.centerAndZoom(new BMapGL.Point(121.36564, 31.22611), 19);

// 开启鼠标滚轮缩放功能，仅对PC上有效
    map.enableScrollWheelZoom(true);

// 将控件（平移缩放控件）添加到地图上
    map.addControl(new BMapGL.NavigationControl());

// 为地图增加点击事件，为input赋值
    map.addEventListener("click", function(e) {
        var res= bMapTransQQMap(e.latlng.lat,e.latlng.lng);

        document.getElementById('latmai').value = res[0];
        document.getElementById('lngmai').value = res[1];

    });

// 创建位置检索、周边检索和范围检索
    local= new BMapGL.LocalSearch(map, {
        renderOptions: {
            map: map
        }
    });
}

function  bMapTransQQMap(lat ,lng) {

    var x_pi = 3.14159265358979324 * 3000.0 / 180.0;
    var x = lng - 0.0065;
    var y = lat - 0.006;
    var z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
    var theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
    var lng_new = z * Math.cos(theta);
    var lat_new = z * Math.sin(theta);
    console.log(lng);
    console.log(lat);
    var res=[lat_new,lng_new];
    return res;
}

// 弹出经纬度
function submit(id) {
    var lat = document.getElementById('latmai');
    var lng = document.getElementById('lngmai');
      if(lat.value===''||lng.value===''){
          tips('请选择经纬度!!!')
          return;
      }
    tips('提交经纬度成功')
    hidePop('#mapPop')
    maiurl=id
    console.log(maiurl);

    document.getElementById('lat').value = lat.value;
    document.getElementById('lng').value = lng.value;

};
// 发起检索
function theLocation() {
    var city = document.getElementById("cityName").value;
    console.log(city,'city');
    if (city != "") {
        local.search(city);
    }
};


//初始化数据
function init(){
    $.ajax({
        url:url + 'map/getMap2dId/' + ID,
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success:function(res){
            if (!res.code) {
                let newWindow = window.open('about:blank');
                newWindow.document.write(res);
                newWindow.focus();
                window.history.go(-1);
            }else if(res.code == 200){
                var data = res.data;
                if(!data){
                    return;
                }
                if(data.type != 2){
                    tips('地图类型不正确，请检查数据');
                    return;
                }
                setData(data,'.main');
                if(data.themeName) {
                    $('[name="themeName"]')[0].innerText = data.themeName;
                    $('[name="themeName"]').eq(0).addClass('batchTxtChange');
                }
                if (data.welcomePagePath) {
                    $('.mapWelcome').find('.uploadImgShow').attr('src', fdfsUrl + data.welcomePagePath);
                    $('.mapWelcome').find('.mapWelPic').eq(0).addClass('mapWelPicHover');
                    $('.mapWelcome').find('.uploadImgIcon').addClass('mapNone');
                    $('.mapWelcome').find('.uploadImgIcon1').addClass('mapNone');
                }
                if(data.mapLogo) {
                    // $('#uploadImgShow').attr('src', fdfsUrl + data.mapLogo);
                    $('.mapLogo').find('.uploadImgShow').attr('src', fdfsUrl + data.mapLogo)
                    // $('.mapLogo').find('.mapPic').attr('src', fdfsUrl + data.welcomePagePath);
                    $('.mapLogo').find('.mapWelPic').addClass('mapWelPicHover');
                    $('.mapLogo').find('.uploadImgIcon').addClass('mapNone');
                    $('.mapLogo').find('.uploadImgIcon1').addClass('mapNone');
                }
                if(data.coordinate) {
                    var cooName = data.coordinate == 2 ? '高德' : data.coordinate == 1 ? '百度' : '';
                    $('[name="coordinate"]')[0].innerText = cooName;
                    $('[name="coordinate"]').eq(0).addClass('batchTxtChange');
                }
                if (ID) {
                    welcomePagePath = data.welcomePagePath ? data.welcomePagePath : '';
                    mapLogo = data.mapLogo ? data.mapLogo : '';
                }
            } else {
                tips(res.message);
            }
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
//保存
function save(){
    var sendData = getDataImg('.main');
    if(!sendData){
        return;
    }
    var enable = $('[name="enable"]').is(':checked') ? '1' : '0';
    sendData.append('enable', enable);
    var file = sendData.get('file2');
    var mapLogoPath = sendData.get('mapLogoPath');
    console.log(mapLogoPath)
    if (file || mapWelcValue !== '') {
        welcomePagePath = '';
    }
    console.log(mapLogoValue)
    if (mapLogoPath || mapLogoValue !== '') {
        mapLogo = '';
    }
    var path = url;
    if(ID){
        path += 'map/updateMap2d';
        sendData.append('id', ID);
        sendData.append('welcomePagePath', welcomePagePath);
        sendData.append('mapLogo', mapLogo);
    } else {
        path += 'map/addMap2d';
    }
    $.ajax({
        url:path,
        data:sendData,
        type:'post',
        processData: false,// jQuery不要去处理发送的数据
        contentType: false,// jQuery不要去设置Content-Type请求头
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success:function(res){
            if(res.code === 200){
                tips(res.message,function(){
                    if(!ID){
                        location.reload();
                    }
                },1000);
                init2();
            }else{
                tips(res.message);
            }
        },
        error:function(jqXHR){
            var status = jqXHR.status;
            if(status == 401){
                document.write(jqXHR.responseText)
            }
            resError(jqXHR);
        }
    })
}

function init2(){
    var sendData = getDataImg('.main');

    var fId = document.getElementById('fId');

    if(!sendData){
        return;
    }
    var files = fId.files[0];
    if (!files) {
        return;
    }
    // if(suffix !== '.xls'){
    //     tips('对不起，导入数据格式必须是xls格式文件哦，请您调整格式后重新上传，谢谢 ！');
    //     return;
    // }

    sendData.append('file', files);

    $.ajax({
        url: url + 'park/importPlace',
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
            fId.value = '';
            if(res.code === 220){
                init();
                tips(res.message,function(){
                    if(!ID){
                        location.reload();
                    }
                },5000);
            }
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })

}

//获取组装数据（图片）
function getDataImg(target){
    var formData = new FormData();
    var searchItem = $(target).find('[name]:visible');
    var target = null;
    var tarName = null;
    var tarVal;
    var reg = null;
    for(var i = 0;i < searchItem.length;i++){
        target = searchItem[i];
        reg = target.getAttribute('data-reg');
        tarName = target.getAttribute('name');
        if(target.className.indexOf('batchTxt') !== -1){
            //自定义的下拉
            tarVal = $(target).data('val');
        }else{
            if(target.type == 'file'){
                tarVal = target.files[0] || '';
            }else{
                tarVal = target.value;
            }
        }
        if(reg){
            if(target.type == 'file'){
                // if(!ID && !tarVal){
                //     tips('请上传文件');
                //     return false;
                // }
            }
            else{
                if(!new RegExp(reg).test(tarVal)){
                    if(tarName === 'name') {
                        tips('请输入地图名称');
                    } else if(tarName === 'describe') {
                        tips('请输入地图描述');
                    } else if(tarName === 'defaultFloor') {
                        tips('请输入默认楼层');
                    } else if(tarName === 'fmapID') {
                        tips('请输入地图ID');
                    } else if(tarName === 'lng' || tarName === 'lat') {
                        tips('请选择经纬度');
                    }
                    
                    return false;
                }
            }
        }
        formData.append(tarName,tarVal);
    }
    return formData;
}
//上传蜂鸟地图提示
function uploadFmap(that,txtTarget){
    var files = that.files[0];
    var txt = $('#' + txtTarget);
    if(!files){
        //取消
        txt.html('');
        return;
    }
    txt.html('已选择');
}

function cancelButton(that, domName) {
    if(domName == 'file2'){
        mapWelcValue = '';
        welcomePagePath = '';
    }else if(domName == 'mapLogoPath'){
        mapLogo = '';
        mapLogoValue = ''
    }
    // mapLogo = '';
    // welcomePagePath = '';
    // domName == 'file2' ?  mapWelcValue = '' : domName == 'mapLogoPath' ? mapLogoValue = '' : '';
    $('.mapWelInp').eq(0).val('');
    $('[name='+domName+']').val('');
    $(that).prevAll('.uploadImgShow').attr('src', '');
    $(that).parent().removeClass('mapWelPicHover');
    $(that).prevAll('.uploadImgIcon').removeClass('mapNone');
}

