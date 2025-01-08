var slideNum = 10;
var winH;
var winW;
var iframeH;
var iframeW;
var iframeBox = null;

var deviceNameList = [];
var alarmTypeList = [];

$(function () {
    iframeBox = document.getElementById('iframeBox');
    var eUl = document.getElementsByClassName('nav');
    var targetLi = eUl[0].getElementsByTagName('li');
    var slide = document.getElementById('slide');
    for (var i = 0; i < targetLi.length; i++) {
        targetLi[i].index = i;
        targetLi[i].onmouseenter = function () {
            if (this.className == 'curNav') {
                return;
            }
            slide.style.left = this.index * 110 + 10 + 'px';
        }
        targetLi[i].onmouseleave = function () {
            if (this.className == 'curNav') {
                return;
            }
            slide.style.left = slideNum + 'px';
        }
    }
    //设置高度
    setIframeHei();

    // 获取报警配置
    getCallPoliceConfig();

    // 设置报警内容
    setCallPolice();
    window.addEventListener('resize', function () {
        location.reload();
    });
})

//设置iframe的高度
function setIframeHei() {
    winH = $(window).height();
    winW = $(window).width();
    iframeH = Math.floor(winH - 60);
    iframeW = iframeBox.offsetWidth;

    //设置宽度
    iframeBox.style.height = iframeH + 'px';
    iframeBox.style.width = iframeW + 'px';
    setUrl('./realtime.html', 1);
}

//设置是否全屏
function setParentInfo(hide, fn) {
    if (hide) {
        $('.main_left').hide();
        $('.header').hide();
        $('.spacing').hide();
        $('.content').css('margin-left', 0);
        iframeBox.style.width = winW + 'px';
        iframeBox.style.height = winH + 'px';
    } else {
        iframeBox.style.width = iframeW + 'px';
        iframeBox.style.height = iframeH + 'px';
        $('.content').css('margin-left', 220);
        $('.main_left').show();
        $('.header').show();
        $('.spacing').show();
    }
    if (typeof fn === 'function') {
        fn(winW - 20 * 2 - 2, winH - 20 * 2 - 2);
    }
}

/*
* 设置iframe地址
* autofill 不需补全
*/
function setUrl(url, autofill) {
    var Url = '';
    if (autofill) {
        Url = url;
    } else {
        Url = './' + url + '.html';
    }
    iframeBox.src = Url;
}

//切换菜单
function switchMenu(ele, autofill) {
    var curPath = ele.getAttribute('data-path');
    if (!curPath) {
        alert('开发中...');
        return;
    }
    var parent = ele.parentNode;
    var curMenu = document.getElementById('curMenu');
    if (curMenu) {
        if (curMenu !== parent) {
            parent.id = 'curMenu';
            //处理子菜单
            curMenu.id = '';
            $(curMenu).find('.memuList').slideUp(300);
        }
    } else {
        parent.id = 'curMenu';
    }
    setUrl(curPath, autofill);
}

//子级调用改变菜单
function childMenu(that, autofill) {
    var curA = $('.curA:visible').get(0);
    var curMenu = null;
    if (curA) {
        if (curA != that) {
            curA.className = '';
            that.className = 'curA';
        }
    } else {
        that.className = 'curA';
    }
    var curPath = that.getAttribute('data-path');
    setUrl(curPath, autofill);
}

//退出
function exit() {
    let res = confirm('确定退出登录吗？')
    if (!res) return false;

    $.ajax({
        url: url + 'logout',
        type: 'post',
        success: function () {
            sessionStorage.removeItem('mapClick');
            location.reload();
        },
        error: function (jqXHR) {
            tips(jqXHR);
        }
    })
}

function modifyPermissionsLogout() {    
    $.ajax({
        url: url + 'logout',
        type: 'post',
        success: function () {
            sessionStorage.removeItem('mapClick');
            location.reload();
        },
        error: function (jqXHR) {
            tips(jqXHR);
        }
    })
};
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
        tips.css('top', '-50px');
        clearTimeout(tipsTimer);
        tipsTimer = null;
        fn && fn();
    }, (time || 3000))
}

//展开、收起子菜单
function unfold(that) {
    var curMenu = document.getElementById('curMenu');
    var parent = that.parentNode;
    var _that = $(that);
    if (curMenu) {
        if (curMenu === parent) {
            //相同的dom
            _that.next().slideUp(300);
            parent.id = '';
            return;
        } else {
            //不同的dom
            $(curMenu).find('.memuList').slideUp(300);
            curMenu.id = '';
        }
    }
    _that.next().slideDown(300);
    parent.id = 'curMenu';
}

//切换显示下拉框
//that 显示的下拉框父级
var sele;//选中的target
function switchSele(e) {
    var _that = $(e.currentTarget);//this
    var target = _that.find('.batchList');
    if (!target.length) {
        return;
    }
    if (target.is(':visible')) {
        target.hide();
        _that.removeClass('levelZ ');
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

//阻止事件冒泡
function stopBuddle(e) {
    if (e && e.stopPropagation) {
        e.stopPropagation();
    } else {
        //IE
        window.event.cancelBubble = true;
    }
}

//批量操作的item点击事件
function seleBatch(ele, val, fn) {
    var showTxt = null;
    if (ele.className == 'curBatch') {
        return;
    } else {
        showTxt = $(ele).parent().prev();
        showTxt.html(ele.innerHTML).data('val', val);
        var oldItem = ele.parentNode.getElementsByClassName('curBatch')[0];
        if (oldItem) {
            oldItem.className = '';
        }
        ele.className = 'curBatch';
    }
}

//切换导航栏
function switchNav(ele, target, index) {
    if (ele.className == 'curNav') {
        return;
    }
    seleProject(ele, target, index);
}

//判断是否选中项目
function seleProject(ele, target, index) {
    slideNum = index * 110 + 10;
    var curNav = document.getElementsByClassName('curNav')[0];
    if (curNav) {
        curNav.className = '';
    }

    ele.className = 'curNav';
    var curShow = document.getElementsByClassName('menu')[0].getElementsByClassName('show')[0];
    if (curShow) {
        curShow.className = 'hide';
    }
    document.getElementById(target).className = 'show';
    var menuSlider;
    if (ele.id === 'dataCenterNav') {
        menuSlider = document.getElementById('realtime');
    } else if (ele.id === 'parkingManagNav') {
        menuSlider = document.getElementById('place');
    } else if (ele.id === 'businessManagNav') {
        menuSlider = document.getElementById('business');
    } else if (ele.id === 'deviceManagNav') {
        menuSlider = document.getElementById('baseStation');
    } else if (ele.id === 'callpoliceManagNav') {
        menuSlider = document.getElementById('cpid');
    } else if (ele.id === 'userManagNav') {
        menuSlider = document.getElementById('member');
    }
    switchMenu(menuSlider);
}

//进入项目后的操作
function entProjectCallback() {
    slideNum = 110 + 10;
    slide.style.left = slideNum + 'px';
    $('.curNav').removeClass('curNav').next().addClass('curNav');
    document.getElementById('indexMenu').className = 'hide';
    document.getElementById('projectMenu').className = 'show';
    setUrl('./tempPos.html', 1);
}

//刷新
function reloadPage() {
    location.reload();
}

function reloadPage1(membername) {
    let loginName = $(".user").text().trim();
    console.log('membername', membername);
    console.log('loginName', loginName);
    if (loginName === membername) {
        tips('三秒后重新登录', function () {
            modifyPermissionsLogout();
        }, 3000);
    }
};
function getCallPoliceConfig() {
    $.ajax({
        url: url + 'deviceAlarms/getDeviceAlarmsTypeConfig',
        data: {
            pageIndex: 1,
            pageSize: -1
        },
        type: 'get',
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res) {

            var tab = $('#tab');
            if (res.code != 200) {
                tableException(tab, res.message);
                return;
            }
            var data = res.data;
            list = data.list;
            // 设置对应的下拉列表
            setcpConfigSelect(list);

        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
};

function setcpConfigSelect(list) {

    deviceNameList = [];
    alarmTypeList = [];

    list.forEach((item) => {
        let hasD = deviceNameList.some((dl) => dl.deviceTypeId == item.deviceTypeId);
        if (!hasD) {
            deviceNameList.push({
                deviceName: item.deviceName,
                deviceTypeId: item.deviceTypeId
            })
        };

        let hasA = alarmTypeList.some((al) => al.alarmsTypeId == item.alarmsTypeId);
        if (!hasA) {
            alarmTypeList.push({
                alarmName: item.alarmName,
                alarmsTypeId: item.alarmsTypeId
            })
        }
    });
};

// 报警内容设置
function setCallPolice() {
    $.ajax({
        url: url + '/deviceAlarms/getDeviceAlarmsData',
        type: 'get',
        data: {
            state: 0,
            priority: 1,
            pageIndex: 1,
            pageSize: 20
        },
        success: function (res) {
            if (res.code == 200) {
                let list = res.data;
                let total = list.total > 99 ? '99+' : list.total;
                $("#noticeNum").html(total);

                if (list.total > 0) {
                    $("#noticeNum").css('display', 'block');
                } else {
                    $("#noticeNum").css('display', 'none');
                }
                let html = '';
                if (list.list.length) {
                    list.list.forEach((item) => {
                        html += `
                            <div class="cp_list_item" onclick="openNoticeDetail(${item.id})">
                                <div class="cp_list_item_top">
                                    <div class="cp_top_name"><span class="big">设备类型：</span>${getDTypeListName(deviceNameList, item.equipmentType)}</div>
                                    <div class="${'cp_top_priority' + item.priority}"></div>
                                </div>

                                <div class="cp_list_item_bottom">`

                        if (item.num) {
                            html += `<div class="cp_bottom_state"><span class="big">设备编号：</span>${(item.num || '未知')}</div>`
                        } else {
                            html += `<div class="cp_bottom_state"><span class="big">车位编号：</span>${(item.placeName || '未知')}</div>`
                        };

                        html += `<div class="cp_bottom_state"><span class="big">报警类型：</span>${getATypeListName(alarmTypeList, item.alarmType)}</div>
                                    <div class="cp_bottom_state"><span class="big">报警级别：</span>${item.priority == 1 ? '高' : (item.priority == 2 ? '中' : '低')}</div>
                                    <div class="cp_bottom_state"><span class="big">开始时间：</span>${item.startTime}</div>
                                </div>
                                
                            </div>
                        `
                    });
                } else {
                    html += `<div class="nolistData">暂无高级别报警</div>`
                }


                $("#showNoticeList").html(html);

            }
        },
        error: function (jqXHR) {
            tips(jqXHR);
        }
    })
};

// 获取type对应的名字
function getATypeListName(list, type) {
    let name = list.filter((item) => item.alarmsTypeId == type);
    if (name.length) {
        return name[0].alarmName || ''
    } else {
        return ''
    }
}

function getDTypeListName(list, type) {
    let name = list.filter((item) => item.deviceTypeId == type);
    if (name.length) {
        return name[0].deviceName || ''
    } else {
        return ''
    }
}

// 通知按钮
function notice(even) {
    even.stopPropagation();
    even.stopImmediatePropagation();
    let dom = $("#showNoticeBox")
    if (dom.hasClass('showBox')) {
        $("#showNoticeBox").hide();
        $("#showNoticeBox").removeClass('showBox')
    } else {
        $("#showNoticeBox").show()
        $("#showNoticeBox").addClass('showBox')
    };

    // $('#showNoticeBox').bind("mouseleave", function () {
    //     $(this).hide();
    //     $('#showNoticeBox').removeClass('showBox');
    // });
}

// 打开通知详情
function openNoticeDetail(id) {
    $("#showNoticeBox").hide();
    $("#showNoticeBox").removeClass('showBox')

    switchNav({ id: 'callpoliceManagNav', className: '' }, 'callpolice', 4)
    slide.style.left = 4 * 110 + 10 + 'px';
    setTimeout(() => {
        document.getElementById('curMenu').setAttribute('id', '');
        document.getElementsByClassName('callPolice')[0].setAttribute('id', 'curMenu')
        iframeBox.src = './callPolice.html?showD=true&id=' + id;
    }, 0);
}

// 查看更多
function seeMoreData() {

    $("#showNoticeBox").hide();
    $("#showNoticeBox").removeClass('showBox')

    switchNav({ id: 'callpoliceManagNav', className: '' }, 'callpolice', 4)
    slide.style.left = 4 * 110 + 10 + 'px';
    setTimeout(() => {
        document.getElementById('curMenu').setAttribute('id', '');
        document.getElementsByClassName('callPolice')[0].setAttribute('id', 'curMenu')
        iframeBox.src = './callPolice.html?showD=false';
    }, 0);
};