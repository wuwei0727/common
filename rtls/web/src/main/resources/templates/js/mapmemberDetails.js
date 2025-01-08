// 移动端地图账号管理
var ID;
$(function () {

    $(document).on('click', function (e) {
        hideSele();
    })
    // loadSeleData();
    var id = getUrlStr('id');
    if (id) {
        ID = id;
        init(url + 'user/getAppUserId/' + ID);
        $('#titleFlag').html('编辑');

    } else {
        loadFunMore('map/getMap2dSel', {pageSize: -1, enable: 1}, '#mapid');
    }
    var account = getUrlStr('account');
    if (account) {
        //账户设置过来的
        //获取当前账户的信息
        init(url + 'user/getAppUserId', account);
        $('.topTitle').html('账户设置');
    }
})
// //加载下拉数据
// function loadSeleData(){
//     loadFun('company/getCompanySel',{pageSize:-1},'#cidSele',false,'cname');
// }
//初始化数据
function init(url, account) {
    $.ajax({
        url: url,
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res) {
            if (res.code === 200) {
                var list = res.data.list || {};

                var mapList = list.mapList;
                let arr = []
                for (let i = 0; i < mapList.length; i++) {
                    arr.push(mapList[i].id)
                }
                setData(list, '.main');
                setData(list, '.main1');

                $('[name="passWs"]').val(list.password);
                $('[name="entPassword"]').val(list.password);
                $('[name="cid"]').html(list.cname);

                if (account) {
                    ID = list.uid;
                }
                // $('[name="userName"]').val(list.userName);
                $('[name="userName"]').val(list.userName);
                $('[name="password"]').html(list.password);
                if(list.mapList.length > 0) {
                    $('[name="mapid"]').eq(0).addClass('batchTxtChange');
                }
                loadFunMore('map/getMap2dSel', {pageSize: -1, enable: 1}, '#mapid', arr.join(','));
            } else {
                tips(res.message);
                if (!res.code) {
                    let newWindow = window.open('about:blank');
                    newWindow.document.write(res);
                    newWindow.focus();
                    window.history.go(-1);
                }
            }
        },
        error: function (jqXHR) {
            var status = jqXHR.status;
            if(status == 401){
                document.write(jqXHR.responseText)
            }
            resError(jqXHR);
        }
    })
}

//保存
function save() {
    var sendData = getData('.main');
    if (!sendData) {
        return;
    }

    var path = url;
    if (ID) {
        console.log(ID)
        path += 'user/updateAppUser';
        sendData.userId = ID;
    } else {
        path += 'user/addAppUser';
    }
    delete sendData.loginTime;
    delete sendData.updatedTime;
    $.ajax({
        url: path,
        data: sendData,
        type: 'post',
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res) {
            if (res.code === 200) {
                tips(res.message, function () {
                    if (!ID) {
                        location.reload();
                    }
                }, 1000);
            }else {
                tips(res.message,null,1000)
            }
            if (!res.code) {
                document.write(res);
            }
        },
        error:function(jqXHR) {
            var status = jqXHR.status;
            if(status == 401){
                document.write(jqXHR.responseText)
            }
            resError(jqXHR);
        }
    })
}