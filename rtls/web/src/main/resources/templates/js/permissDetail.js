var ID;
$(function () {
    $(document).on('click', function (e) {
        hideSele();
    })
    var id = getUrlStr('id');
    if (id) {
        ID = id;
        console.log(id)
        init();
        $('#titleFlag').html('编辑');
    }
})
//初始化数据
function init() {
    $.ajax({
        url: url + 'company/getCompanyId/' + ID,
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res) {
            console.log(res)
            if (!res.code) {
                let newWindow = window.open('about:blank');
                newWindow.document.write(res);
                newWindow.focus();
                window.history.go(-1);
            } else if (res.code == 200) {
                var data = res.data.list;
                setData(data, '.main');
                if(res.data.list.enabled){
                    document.getElementById('id_enablePop').checked = true;
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
    var sendData = getData('.main');
    if (!sendData) {
        return;
    }
    if (!sendData.cname) {
        tips('权限组名称不能为空');
        return;
    }
    console.log(sendData);
    var path = url;
    if (ID) {
        path += 'company/updateCompany';
        sendData.id = ID;
    } else {
        path += 'company/addCompany';
    }
    $.ajax({
        url: path,
        type: 'post',
        data: sendData,
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
            resError(jqXHR);
        }
    })
}