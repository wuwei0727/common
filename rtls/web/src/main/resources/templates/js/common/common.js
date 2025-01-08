
//禁用浏览器打开控制台
// window.onload = function () {
//     document.onkeydown = function () {
//         var e = window.event || arguments[0];
//         //屏蔽F12
//         if (e.keyCode === 123) {
//
//             return false;
//             //屏蔽Ctrl+Shift+I
//         } else if ((e.ctrlKey) && (e.shiftKey) && (e.keyCode === 73)) {
//
//             return false;
//             //屏蔽Shift+F10
//         } else if ((e.shiftKey) && (e.keyCode === 121)) {
//             return false;
//         }
//     };
//     //屏蔽右键单击
//     document.oncontextmenu = function () {
//         return false;
//     }
// }

// 在每个Ajax请求之前设置Cookie请求头
$.ajaxSetup({
    xhrFields: {
        withCredentials: true
    }
});

//计算page
function calcPage() {
    if (!window.pageSize) {
        return;
    }
    return (pageSize = 20);//固定
    var hei = $(window).height();
    var temp = hei - 46 - 51 - 16 * 2 - 50 - 46 - $('.search').outerHeight();
    temp = temp / 60;
    pageSize = temp < 5 ? '5' : Math.floor(temp) - 1;//给批量操作留空间
}

//阻止事件冒泡
function stopBuddle(e) {
    if (e && e.stopPropagation) {
        e.stopPropagation();
    } else {
        //IE
        window.event.cancelBubble = true;
    }
}

//显示弹窗
function showPop(target, mask) {
    if (!target) {
        return;
    }
    $((mask || '#mask')).show();
    $(target).show();
}

//隐藏弹窗
function hidePop(target, mask) {
    if (!target) {
        return;
    }
    $((mask || '#mask')).hide();
    $(target).hide();
}

//返回
function back() {
    window.parent.closeFrame();
}

//获取url的参数值
function getUrlStr(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = window.location.search.slice(1).match(reg);
    if (r != null) {
        return r[2];
    }
    ;
    return null;
}

//获取url的所有参
function getAllUrl() {
    var url = location.search; //获取url中"?"符后的字串  
    var theRequest = new Object();
    if (url.indexOf("?") != -1) {
        var str = url.substr(1);
        strs = str.split("&");
        for (var i = 0; i < strs.length; i++) {
            theRequest[strs[i].split("=")[0]] = window.decodeURIComponent(strs[i].split("=")[1]);
        }
    }
    return theRequest;
}

var tipsTimer = null;

//创建提示
function tips(str, fn, time) {
    var tips = $('#tip');
    if (tipsTimer) {
        clearTimeout(tipsTimer);
    } else {
        tips.css('top', '50px');
    }
    tips.html(str || '');
    tipsTimer = setTimeout(function () {
        tips.css('top', '-100px');
        clearTimeout(tipsTimer);
        tipsTimer = null;
        fn && fn();
    }, (time || 3000))
}

//提示（直接显示）
function showTips(str, fn, time) {
    $('body').append('<div class="showTips">' + str + '</div>');
    setTimeout(function () {
        $('.showTips').remove();
        fn && fn();
    }, (time || 3000))
}

//获取搜索的值
function getSearch(dom, where) {
    var searchItem = $(dom).find('[name]');
    var target = null;
    for (var i = 0; i < searchItem.length; i++) {
        target = searchItem[i];
        if (target.className == 'batchTxt') {
            //自定义的下拉
            where[target.getAttribute('name')] = $(target).data('val');
        } else {
            where[target.getAttribute('name')] = target.value;
        }
    }
}

//获取thead的name
function getTheadName(ele) {
    var res = [];
    var allName = ele.find('td[name]');
    for (var i = 0; i < allName.length; i++) {
        res.push(allName[i].getAttribute('name'));
    }
    return res;
}

//全选
function allCheck(ele) {
    var that = $(ele);
    var flag;
    var tab = that.parent().parent().prev();
    var allCheck = tab.find('tbody [name="tabCheck"]');
    if (ele.checked) {
        flag = true;
    } else {
        flag = false;
    }
    $.each(allCheck, function (i, inp) {
        if (inp.checked == flag) {
            //情况相同跳过
            return true;
        }
        if (flag) {
            inp.checked = true;
        } else {
            inp.checked = false;
        }
    })
}

//单个的选中（触发全选）
function cheItem(that, par) {
    var tbody = null;
    var allChe = null;
    if (that) {
        tbody = $(that).parent().parent().parent();
        if (par) {
            allChe = tbody.parent().parent().next().find('.tabChe');
        } else {
            allChe = tbody.parent().next().find('.tabChe');
        }
    } else {
        tbody = $('#tab tbody');
        allChe = $('#allCheck');
    }
    var curChe = tbody.find('[name="tabCheck"]:checked');
    var allTr = tbody.children();
    if (curChe.length == allTr.length) {
        allChe.prop('checked', true);
    } else {
        allChe.prop('checked', false);
    }
}

//切换显示下拉框
//that 显示的下拉框父级
var sele;//选中的target
function switchSele(e, dom) {
    var _that = $(e.currentTarget);//this
    var target = _that.find(dom || '.batchList');
    if (!target.length) {
        return;
    }
    if (target.is(':visible')) {
        target.hide();
        _that.removeClass('levelZ');
        sele && (sele = null);
    } else {
        if (sele) {
            sele.hide();
            sele.parent().removeClass('levelZ');
            sele = null;
        }
        _that.addClass('levelZ');
        target.show();
        sele = target;
    }
    //阻止事件冒泡
    stopBuddle(e);
    return false;
}

//隐藏下拉
function hideSele() {
    //如果有显示的下拉框则隐藏
    if (sele) {
        sele.hide();
        sele.parent().removeClass('levelZ');
        sele = null;
    }
}

// load
function loading() {
    $('body').append('<div id="loadMask"></div><div id="load"><img src="../image/common/load.gif" /></div>');
}

//removeLoad
function removeLoad() {
    $('#loadMask').remove();
    $('#load').remove();
}

//textarea 计算字数
function calcTextNum(ele) {
    var num = 200 - ele.value.length;
    if (num < 0) {
        return;
    }
    var tar = calcTextNum.dom || (calcTextNum.dom = $('#areaNum'));
    tar.text(num);
}

// 判断浏览器是否支持 createObjectURL
function getObjectURL(file) {
    var url = null;
    if (window.createObjectURL != undefined) {
        url = window.createObjectURL(file);
    } else if (window.URL != undefined) { // mozilla(firefox)
        url = window.URL.createObjectURL(file);
    } else if (window.webkitURL != undefined) { // webkit or chrome
        url = window.webkitURL.createObjectURL(file);
    }
    return url;
}

//转换空值
function convertNull(val) {
    if (val == '0') {
        return val;
    }
    return val || '';

    if (val == undefined) {
        return val || '';
    }
    return val || 0;
}

var mapWelcValue = '';  // 地图管理详情页面上一次选择的欢迎页图片
var mapLogoValue = '';  // 地图管理详情页面上一次选择的logo图片
//处理上传图片
async function uploadImg(that, widthHeight, domName, size) {
    var files = that.files[0];
    if (!files) {
        return;
    }
    if (!files.type.match('image/jpeg') && !files.type.match('image/png')) {
        tips('只能上传JPG和PNG格式的图片!');
        return;
    }
    var img = $(that).prevAll('.uploadImgShow');
    if (size) {
        if (files.size > 1024 * 1024) {
            tips('文件大小不能超过1M，请重新上传');
            that.value = '';
            img.attr('src', '');
            return;
        }
    }
    if (widthHeight) {
        var img1 = new Image();
        $(img1).attr('src', getObjectURL(files));
        var flag = false;
        await new Promise((resolve, reject) => {
            img1.onload = () => {
                console.log(img1.width)
                console.log(img1.height)
                if (img1.width != widthHeight.width || img1.height != widthHeight.height) {
                    tips('请上传' + widthHeight.width + "*" + widthHeight.height + "像素图片");
                    that.value = '';
                    $('[name="file2"]').val(mapWelcValue);
                    flag = true;
                }
                resolve("img1加载完成");
            };
        });
        if (flag) {
            $(img1).attr('src', '');
            return;
        }
    }
    var Id = img.data('id');
    if (Id) {
        tips('请先删除当前已上传的图片');
        return;
    }
    var src = getObjectURL(files);
    if (widthHeight || domName) {
        $(that).next('#uploadImgIcon').addClass('mapNone');
        $(that).parent().addClass('mapWelPicHover');
        widthHeight ? mapWelcValue = files : mapLogoValue = files;
        widthHeight ? $(img1).attr('src', '') : '';
        $('[name=' + domName + ']').val(files);
    }
    img.attr('src', src);
}

//删除图片
/*
img  //显示的image
inp  //上传的input
*/
function deleUpload(img, inp, path) {
    if (!path) {
        tips('删除失败');
        return;
    }
    var Img = $('#' + img);
    var Id = Img.data('id');
    var Url = url + path;
    var res;
    if (Id) {
        res = confirm('继续该操作则会删除图片（不需点击保存），是否继续');
        if (!res) {
            return;
        }
        $.ajax({
            url: Url,
            data: {
                id: Id
            },
            beforeSend: function () {
                loading();
            },
            complete: function () {
                removeLoad();
            },
            success: function (data) {
                tips(data.msg);
                if (data.status) {
                    Img.attr('src', '');
                    $(img).val('');
                }
            },
            error: function (jqXHR) {
                resError(jqXHR);
            }
        })
    } else {
        Img.attr('src', '');
        $(img).val('');
    }
}

// 批量导入
function uploadTemp(ele, path) {
    if (!path) {
        ele.value = '';//清空赋值
        tips('上传失败');
        return;
    }
    if (!ele.value) {
        return;
    }
    var formData = new FormData();
    formData.append("file", ele.files[0]);
    $.ajax({
        url: url + path,
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function (data) {
            ele.value = '';//清空赋值
            if (data.status) {
                showTips(data.msg, function () {
                    init();
                })
            } else {
                showTips(data.msg, null, 5000);
            }
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    });
}

//请求错误时
function resError(jqXHR) {
    tips('系统繁忙');
}

/*
计算时间
num:x(返回X天的开始和结束时间)
*/
function calaTime(num) {
    if (num === undefined) {
        return;
    }
    num = num - 1;
    var now = new Date();
    var temp = num * 86400 * 1000;//转换为毫秒（天）
    var val = now - temp;
    var time = new Date(val);
    var year = time.getFullYear();
    var mon = time.getMonth() + 1;
    var day = time.getDate();
    var res = {};
    res.start = year + '-' + zero(mon) + '-' + zero(day) + ' 00:00:00';
    res.end = now.getFullYear() + '-' + zero(now.getMonth() + 1) + '-' + zero(now.getDate()) + ' 23:59:59';
    return res;
}

//补零
function zero(num) {
    return num > 9 ? '' + num : '0' + num;
}

//批量操作的item点击事件
function seleBatch(ele, val, fn, selData, selItemList, addom, firstTxt) {
    if (!ele) {
        seleFn(fn);
        return;
    }
    var showTxt = null;
    if (ele.className == 'curBatch') {
        return;
    } else {
        showTxt = $(ele).parent().prev();
        showTxt.html(ele.innerHTML).data('val', val);
        if (ele.innerHTML.indexOf("请选择") === -1) {
            $(showTxt[0]).addClass('batchTxtChange')
        } else {
            $(showTxt[0]).removeClass('batchTxtChange')
        }
        var oldItem = ele.parentNode.getElementsByClassName('curBatch')[0];
        if (oldItem) {
            oldItem.className = '';
        }
        ele.className = 'curBatch';
        if (selData) {
            if (selData.length >= 0) {
                var findItem = [];
                selData.forEach(item => {
                    if (item.mapId == val) {
                        findItem = item[selItemList];
                    }
                });
                addSelData(addom, firstTxt, undefined, findItem);
            }
        }
    }
    seleFn(fn);
}

//点击的回调
function seleFn(fn) {
    if (typeof fn === 'function') {
        fn();
    }
}

function addSelData(dom, firstTxt, tarName, selData) {
    var temp = null;
    if (typeof firstTxt === 'object') {
        temp = firstTxt;
    } else {
        temp = {
            txt: firstTxt,
            tarName: tarName,
        }
    }
    var html = '';
    var curName = temp.tarName || 'comName';
    var curId = temp.id || 'comId';
    var curTxt = temp.txt || '--请选择关联公司--';
    if (typeof firstTxt !== "boolean") {
        html = '<div onclick="seleBatch(this,\'\')" data-id="">' + curTxt + '</div>';
    }
    var list = selData || [];
    for (var i = 0; i < list.length; i++) {
        html += '<div onclick="seleBatch(this,\'' + list[i][curId] + '\')" data-id="' + list[i][curId] + '" title="' + list[i][curName] + '">' + list[i][curName] + '</div>';
    }
    $(dom).html(html);
    var showTxt = $(dom).prev();
    showTxt.text(temp.txt || '--请选择关联公司--').data('val', null);
    showTxt.removeClass('batchTxtChange');
    if (typeof fn === 'function') {
        fn(list);
    }
}


//多选
function multipleBatch(ev, that, detault, fn) {
    var _that = $(that);
    _that.toggleClass('curBatch');
    var par = _that.parent();
    var cheBatch = par.find('.curBatch');
    var id = '';
    var txt = '';
    for (var i = 0; i < cheBatch.length; i++) {
        id += cheBatch[i].getAttribute('data-id') + ',';
        txt += cheBatch[i].innerHTML + '、';
    }
    id = id.slice(0, -1);
    txt = txt.slice(0, -1);
    if (!txt) {
        txt = detault || '';
    }
    par.prev().html(txt).data('val', id);
    if (par.prev()[0].innerHTML.indexOf("请选择") === -1) {
        $(par.prev()[0]).addClass('batchTxtChange')
    } else {
        $(par.prev()[0]).removeClass('batchTxtChange')
    }
    if (typeof fn === 'function') {
        fn(id);
    }
    stopBuddle(ev);
}

//加载下拉通用
//firstTxt 为false 时不添加首条 或为对象（整合后面的参数）
function loadFun(path, where, dom, firstTxt, tarName, fn) {
    var temp = null;
    if (typeof firstTxt === 'object') {
        temp = firstTxt;
    } else {
        temp = {
            txt: firstTxt,
            tarName: tarName,
        }
    }
    $.ajax({
        url: url + path,
        data: where,
        success: function (res) {
            if (res.code != 200) {
                tips('获取选项失败');
                return;
            }
            var html = '';
            var curName = temp.tarName || 'name';
            var curId = temp.id || 'id';
            var curTxt = temp.txt || '--请选择关联地图--';
            if (typeof firstTxt !== "boolean") {
                html = '<div onclick="seleBatch(this,\'\')" data-id="">' + curTxt + '</div>';
            }
            var list = res.data || [];
            for (var i = 0; i < list.length; i++) {
                html += '<div onclick="seleBatch(this,\'' + list[i][curId] + '\')" data-id="' + list[i][curId] + '" title="' + list[i][curName] + '">' + list[i][curName] + '</div>';
            }
            $(dom).html(html);
            if (typeof fn === 'function') {
                fn(list);
            }
        },
        error: function (err) {
            resError(err);
        }
    })
}

function loadFun1(path, where, dom, firstTxt, tarName, fn) {
    var temp = null;
    if (typeof firstTxt === 'object') {
        temp = firstTxt;
    } else {
        temp = {
            txt: firstTxt,
            tarName: tarName,
        }
    }
    $.ajax({
        url: url + path,
        data: where,
        success: function (res) {
            if (res.code != 200) {
                // tips('获取选项失败1');
                return;
            }
            var html = '';
            var curName = temp.tarName || 'cname';
            var curId = temp.id || 'id';
            var curTxt = temp.txt || '--请选择--';
            if (typeof firstTxt !== "boolean") {
                html = '<div onclick="seleBatch(this,\'\')" data-id="">' + curTxt + '</div>';
            }
            var list = res.data || [];
            for (var i = 0; i < list.length; i++) {
                html += '<div onclick="seleBatch(this,\'' + list[i][curId] + '\')" data-id="' + list[i][curId] + '" title="' + list[i][curName] + '">' + list[i][curName] + '</div>';
            }
            $(dom).html(html);
            if (typeof fn === 'function') {
                fn(list);
            }
        },
        error: function (err) {
            resError(err);
        }
    })
}

function loadFun3(path, where, dom, firstTxt, tarName, fn) {
    var temp = null;
    if (typeof firstTxt === 'object') {
        temp = firstTxt;
    } else {
        temp = {
            txt: firstTxt,
            tarName: tarName,
        }
    }
    $.ajax({
        url: url + path,
        data: where,
        success: function (res) {
            if (res.code != 200) {
                tips('获取选项失败');
                return;
            }
            var html = '';
            var curName = temp.tarName || 'name';
            var curId = temp.id || 'id';
            var curTxt = temp.txt || '--请选择地图--';
            if (typeof firstTxt !== "boolean") {
                html = '<div onclick="seleBatch(this,\'\')" data-id="">' + curTxt + '</div>';
            }
            var list = res.data || [];
            for (var i = 0; i < list.length; i++) {
                html += '<div onclick="seleBatch(this,\'' + list[i][curId] + '\')" data-id="' + list[i][curId] + '" title="' + list[i][curName] + '">' + list[i][curName] + '</div>';
            }
            $(dom).html(html);
            if (typeof fn === 'function') {
                fn(list);
            }
        },
        error: function (err) {
            resError(err);
        }
    })
}

// 地图多选框,地图绑定
function loadFunMore(path, where, dom, defaultVal) {
    $.ajax({
        url: url + path,
        data: where,
        success: function (res) {
            if (res.code != 200) {
                tips('获取选项失败');
                return;
            }
            var newVal = (defaultVal || '').split(',');
            var html = '';
            tarName = 'name';
            var list = res.data;
            var txt = '';
            for (var i = 0; i < list.length; i++) {
                html += '<div onclick="multipleBatch(event,this)" data-id="' + list[i].id + '"';
                if (newVal.indexOf(list[i].id + '') > -1) {
                    html += ' class="curBatch"';
                    txt += list[i][tarName] + '、';
                }
                html += '>' + list[i][tarName] + '</div>';
            }
            var _that = $(dom);
            _that.html(html);
            if (defaultVal) {
                _that.prev().html(txt.slice(0, -1)).data('val', defaultVal);
            }
        },
        error: function (err) {
            resError(err);
        }
    })
}

//获取组装数据
function getData(target) {
    var res = {};
    var searchItem = $(target).find('[name]');
    var target = null;
    var tarName = null;
    var tarVal;
    var reg = null;
    for (var i = 0; i < searchItem.length; i++) {
        target = searchItem[i];
        reg = target.getAttribute('data-reg');
        tarName = target.getAttribute('name');
        if (target.className.indexOf('batchTxt') !== -1) {
            tarVal = $(target).data('val');
            //自定义的下拉
        } else {
            if (target.type == 'checkbox') {
                tarVal = (target.checked ? '1' : '0');
            } else {
                tarVal = target.value;
            }
        }
        // if (reg) {
        //     if (!new RegExp(reg).test(tarVal)) {
        //         tips('请填写带 * 号的必填项或填写正确的格式');
        //         return false;
        //     }
        // }
        res[tarName] = tarVal;
    }
    return res;
}

//获取组装数据（图片）
function getDataImg(target) {
    var formData = new FormData();
    var searchItem = $(target).find('[name]');
    var target = null;
    var tarName = null;
    var tarVal;
    var reg = null;
    for (var i = 0; i < searchItem.length; i++) {
        target = searchItem[i];
        reg = target.getAttribute('data-reg');
        tarName = target.getAttribute('name');
        if (target.className.indexOf('batchTxt') !== -1) {
            //自定义的下拉
            tarVal = $(target).data('val');
        } else {
            if (target.type == 'checkbox') {
                tarVal = (target.checked ? '1' : '0');
            } else if (target.type == 'file') {
                if (target.files[0]) {
                    if (!target.files[0].type.match('image/jpeg') && !target.files[0].type.match('image/png')) {
                        tarVal = '';
                    } else {
                        tarVal = target.files[0];
                    }
                } else {
                    tarVal = '';
                }

            } else {
                tarVal = target.value;
            }
        }
        if (reg) {
            if (target.type == 'file') {
                if (!tarVal) {
                    tips('请上传文件');
                    return false;
                }
            } else {
                // if (!new RegExp(reg).test(tarVal)) {
                //     tips('请填写必填项或正确填写');
                //     return false;
                // }
            }
        }
        formData.append(tarName, tarVal);
    }
    return formData;
}

//设置数据
function setData(data, target) {
    if (!data) {
        tips('查询失败，请重试');
        return;
    }
    var searchItem = $(target).find('[name]');
    var target = null;
    var tarName;
    var temp;
    for (var i = 0; i < searchItem.length; i++) {
        target = searchItem[i];
        tarName = target.getAttribute('name');
        if (target.className == 'batchTxt') {
            //自定义的下拉
            if (data[tarName] !== null && data[tarName] !== '') {
                $(target).data('val', data[tarName]).html(data[tarName + 'Name']);
            }
        } else {
            //表单元素
            if (target.type == 'file') {
                temp = data[target.getAttribute('data-show')];
                if (temp) {
                    $(target).prevAll('.uploadImgShow').attr('src', imgUrl + temp);
                }
            } else {
                if (target.type == 'checkbox') {
                    if (data[tarName]) {
                        target.checked = true;
                    }
                } else {
                    target.value = convertNull(data[tarName]);
                }
            }
        }
    }
}

//切换图标
function selectIcon(ev, par, icon) {
    var ev = ev || window.event;
    var target = ev.target || ev.srcElement;
    var tagName = target.nodeName.toLowerCase();
    var _that = null;
    if (tagName == 'span' || tagName == 'img') {
        _that = $(target);
        if (_that.hasClass(icon)) {
            return;
        }
        $(par).find('.' + icon).removeClass(icon);
        _that.addClass(icon);
    }
}

//像素转米
function changeMeter(x, y) {
    var res = {};
    var tempX = x * actualWid / bgWid;
    var tempY = actualHei - (y * actualHei / bgHei);
    res.x = tempX.toFixed(2);
    res.y = tempY.toFixed(2);
    return res;
}

//米转像素
function changePx(x, y) {
    var res = {};
    var tempX = x * bgWid / actualWid;
    var tempY = ((actualHei - y) * bgHei) / actualHei;
    res.x = +(tempX.toFixed(2));
    res.y = +(tempY.toFixed(2));
    return res;
}

//多边形质心
function Area(p0, p1, p2) {
    var area = 0.0;
    area = p0.x * p1.y + p1.x * p2.y + p2.x * p0.y - p1.x * p0.y - p2.x * p1.y - p0.x * p2.y;
    return area / 2;
}

//计算polygon的质心
function getPolygonAreaCenter(points) {
    var sum_x = 0;
    var sum_y = 0;
    var sum_area = 0;
    var p1 = points[1];
    for (var i = 2; i < points.length; i++) {
        p2 = points[i];
        area = Area(points[0], p1, p2);
        sum_area += area;
        sum_x += (points[0].x + p1.x + p2.x) * area;
        sum_y += (points[0].y + p1.y + p2.y) * area;
        p1 = p2;
    }
    var xx = sum_x / sum_area / 3;
    var yy = sum_y / sum_area / 3;
    return {
        x: xx,
        y: yy
    };
}

//表格异常的显示
function tableException(tab, msg, pagePar, noOmitt) {
    tips(msg);
    if (tab) {
        tab.find('tbody').html('');
    }
    if (pagePar) {
        initDataPage(0, 0, 0, pagePar, noOmitt);
    } else {
        initDataPage(0, 0, 0);
    }
}

//导出
function downloadExl(path) {
    if (!(exportArr instanceof Array)) {
        tips('导出失败，缺少必要参数');
        return
    }
    if (!path) {
        tips('导出失败');
        return;
    }
    var formUrl = url + path;
    var form = $("<form></form>").attr("action", formUrl).attr("method", "post");
    var html = '';
    var target = null;
    var txt = '';
    for (var i = 0; i < exportArr.length; i++) {
        target = exportArr[i];
        if (target.eVal) {
            html += '<input type="hide" name="' + target.eName + '" value="' + target.eVal + '" />';
        }
        txt += target.eTitle + '：' + target.eTxt + '  ';
    }
    form.append(html);
    form.append('<input type="hide" name="title" value="' + txt + '" />');
    form.appendTo('body').submit().remove();
}

//找某项修改
function exportArrMatch(arr, type, val, txt) {
    var target = null;
    for (var i = 0; i < arr.length; i++) {
        target = arr[i];
        if (target.eName == type) {
            target.eVal = val;
            target.eTxt = txt || val;
            break;
        }
    }
}

//打印
function toPrint(path, title, all) {
    var pageS;
    if (all == -1) {
        pageS = -1;
    } else {
        pageS = pageSize;
    }
    var strUrl = './prints.html?path=' + path + '&pageInfo=' + pageIndex + '_' + pageS + '&title=' + encodeURIComponent(title);
    sessionStorage.setItem('prints', JSON.stringify(exportArr));
    var strWinFeatures = '';
    window.open(strUrl, '_blank', strWinFeatures);
}

//较特殊的打印页面
function toPrintSpecial(path, title) {
    if (!path) {
        tips('打印失败');
        return;
    }
    sessionStorage.setItem('prints', JSON.stringify(exportArr));
    var strWinFeatures = '';
    var strUrl = './' + path + '.html?title=' + encodeURIComponent(title);
    window.open(strUrl, '_blank', strWinFeatures);
}

//寻找对比返回
function contrastReturn(arr, attr, mVal) {
    var target = null;
    for (var i = 0; i < arr.length; i++) {
        target = arr[i];
        if (target[attr] == mVal) {
            return target;
        }
    }
}

//寻找对比返回（返回多个）
function contrastReturnMore(arr, attr, mVal) {
    var target = null;
    var tempArr = [];
    for (var i = 0; i < arr.length; i++) {
        target = arr[i];
        if (target[attr] == mVal) {
            tempArr.push(target);
        }
    }
    return tempArr;
}

//寻找对比返回
function contrastReturnOpa(arr, attr, mVal, fn) {
    var target = null;
    for (var i = 0; i < arr.length; i++) {
        target = arr[i];
        if (target[attr] == mVal) {
            if (typeof fn === 'function') {
                fn(target, i);
                break;
            }
        }
    }
}

//表格排序
function switchSort(that, fn) {
    var className = that.className;
    var temp;
    var lastSort = null;
    var sort = '';
    if (className === 'tabSort') {
        lastSort = document.getElementById('curSort');
        if (lastSort) {
            curSort.className = 'tabSort';
            curSort.id = '';
        }
        that.id = 'curSort';
        temp = 'tabSort ascending';
        sort = 'asc';
    } else if (className.indexOf('ascending') != -1) {
        temp = 'tabSort descending';
        sort = 'desc';
    } else {
        temp = 'tabSort ascending';
        sort = 'asc';
    }
    that.className = temp;
    if (typeof fn === 'function') {
        fn(sort);
    }
}

//搜索
function search() {
    if (where) {
        where.pageIndex = 1;
    }
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
    }
    init();
}

function search1() {
    if (where) {
        where.pageIndex = 1;
        where.ena = 3;
    }
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
    }
    init();
}

//重置搜索
function resetSearch() {
    var allName = $('.search').find('[name]');
    var target = null;
    var tarClass;
    var seleList = null;
    var firstSele = null;
    for (var i = 0; i < allName.length; i++) {
        target = allName[i];
        tarClass = target.className;

        if (tarClass.indexOf('searchInp') != -1) {
            target.value = '';
            continue;
        }
        if (tarClass.indexOf('batchTxt') != -1) {
            target = $(target);
            if (target.data('val')) {
                seleList = target.next();
                seleList.find('.curBatch').removeClass('curBatch');
                firstSele = seleList.children().eq(0);
                target.data('val', firstSele.data('id') || '').html(firstSele.html());
                continue;
            }
        }
    }
    //默认
    if (typeof search === 'function') {
        search();
        return;
    }
}

//多选的重置搜索
function resetMoreSearch(txt) {
    var allName = $('.search').find('[name]');
    var target = null;
    var tarClass;
    var seleList = null;
    var firstSele = null;
    for (var i = 0; i < allName.length; i++) {
        target = allName[i];
        tarClass = target.className;

        if (tarClass.indexOf('searchInp') != -1) {
            target.value = '';
            continue;
        }
        if (tarClass.indexOf('batchTxt') != -1) {
            target = $(target);
            if (target.data('val')) {
                seleList = target.next();
                seleList.find('.curBatch').removeClass('curBatch');
                target.data('val', '').html(txt || '全部');
                continue;
            }
        }
    }
    //默认
    if (typeof search === 'function') {
        search();
        return;
    }
}

//产生随机数
function randomNumber(n, m) {
    return Math.floor(Math.random() * (m - n + 1) + n);
}

//返回表格的check
function resTabCheck(listTar, line, id, name) {
    return '<td class="padLeft40">' +
        '<label class="seleLabel">' +
        '<input data-txt="' + listTar[name || 'name'] + '" value="' + listTar[id || 'id'] + '" onclick="cheItem()" type="checkbox" class="tabCheck" name="tabCheck" />' +
        '<span class="seleIcon"></span>' +
        '<span class="lineNo">' + (line) + '</span></label></td>';
}

function CountWords() {
    var txt = document.getElementById("password");
    var txtNum = document.getElementById("txtNum");
    var sw = false; //定义关闭的开关
    txt.addEventListener("keyup", function () {
        if (sw == false) {
            countTxt();
        }
    });
    txt.addEventListener("compositionstart", function () {
        sw = true;
    });
    txt.addEventListener("compositionend", function () {
        sw = false;
        countTxt();
    });

    function countTxt() { //计数函数
        if (sw === false) { //只有开关关闭时，才赋值
            txtNum.textContent = txt.value.length;
        }
    }
}


function CountWords2() {
    var txt = document.getElementById("pwd");
    var txtNum = document.getElementById("txtNum");
    var sw = false; //定义关闭的开关
    txt.addEventListener("keyup", function () {
        if (sw == false) {
            countTxt();
        }
    });
    txt.addEventListener("compositionstart", function () {
        sw = true;
    });
    txt.addEventListener("compositionend", function () {
        sw = false;
        countTxt();
    });

    function countTxt() { //计数函数
        if (sw === false) { //只有开关关闭时，才赋值
            txtNum.textContent = txt.value.length;
        }
    }
}

function CountWords1() {
    var txt = document.getElementById("password1");
    var txtNum = document.getElementById("txtNum1");
    var sw = false; //定义关闭的开关
    txt.addEventListener("keyup", function () {
        if (sw == false) {
            countTxt();
        }
    });
    txt.addEventListener("compositionstart", function () {
        sw = true;
    });
    txt.addEventListener("compositionend", function () {
        sw = false;
        countTxt();
    });

    function countTxt() { //计数函数
        if (sw === false) { //只有开关关闭时，才赋值
            txtNum.textContent = txt.value.length;
        }
    }
}

function hint() {
    //鼠标悬浮提示账号信息
    var zIndex = 1;
    $(".tipsSpan").mouseover(function () {
        if ($(this).find("span").is(":visible")) {
            $(this).find("span").css({ "visibility": "visible", "zIndex": zIndex++ });
        }
    });
    $(".tipsSpan").mouseout(function () {
        $(this).find("span").css({ "visibility": "hidden" });
    });
}
