var sendId;
var type;//区分权限组或成员
// let checkmai;//父级权限禁止
var cid;
$(function () {
    calcHeig();
    sendId = getUrlStr('id');
    if (!sendId) {
        tips('获取详情失败，请重试');
        return;
    }
    cid = getUrlStr('cid');
    $('.topHeadTitle').html('人员权限设置');
    init();
})

//计算高度
function calcHeig() {
    var heig = $(window).height();
    $('#proWrap').height(heig - 20 * 2 - 24 * 2 - 40 - 40 - 48 - 2 - 56);
}

var membername;
//查询权限组详情
function init() {
    var path;
    path = url + 'member/getMemberPermissons' + '?id=' + sendId + '&cid=' + cid;

    $.ajax({
        url: path,
        type: 'post',
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res) {
            console.log(res, 'res');
            if (res.code == 200) {
                var data = res.data;
                var projectChe = {};//已选的project
                var info = data.list;
                //设置标题
                membername = info.membername;
                $('#topTitle').html('成员名字：' + info.membername + '<span style="margin-left:50px">所属权限组：' + (info.cname || '') + '</span>');

                var allItem = assemblyData(data.allPermission || data.companyPermission);
                var hasPer = permissData(info.permissions);
                projectChe = proSeleData(data.projects);
                var html = '';
                var tdTar = null;
                var len;
                for (var i = 0; i < allItem.length; i++) {
                    len = allItem[i].child.length;
                    if (!len) {
                        continue;
                    }
                    // var checked = false;
                    // console.log(checkmai,'checkmai');
                    // if(checkmai){
                    //     checked = true;
                    // }
                    //
                    html += '<div class="permiss_info"><label class="seleLabel"><input type="checkbox" class="tabCheck" onclick="parCheck(this)" ><span class="seleIcon"></span><span>' + allItem[i].name + '</span></label></div>';

                    html += '<table class="permiss_tab"><tbody><tr>';
                    for (var j = 0; j < len; j++) {
                        if (j != 0 && j % 5 == 0) {
                            //每5个换行
                            html += '</tr><tr>';
                        }
                        tdTar = allItem[i].child[j];

                        html += resTd(tdTar, hasPer[allItem[i].id + '_' + tdTar.id]);
                    }
                    html += supplementTd(len);
                    html += '</tr></tbody></table>';
                }
                $('#permissBox').html(html);
            } else {
                tips(res.message);
            }
            setProjectHtml(data.allProject, projectChe);
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}

/*
//返回td
tdData  td的信息
check   是否勾选
*/
function resTd(tdData, check) {
    var checked = false;
    checkmai = check
    if (check) {
        checked = true;
    }
    // <label class="seleLabel"><input data-txt="奥园广场" value="74" onclick="cheItem()" type="checkbox" class="tabCheck" name="tabCheck"><span class="seleIcon"></span><span class="lineNo">2</span></label>
    return '<td>' +
        '<label class="seleLabel"><input  value="' + tdData.id + '" ' + (checked ? "checked" : "") + ' type="checkbox" class="tabCheck" name="tabCheck"><span class="seleIcon"></span>' +
        '<span>' + (tdData.name || tdData.name) + '</span>' +
        '</label></td>';
}

//组装数据
function assemblyData(data) {
    var len = data.length;
    if (!len) {
        return [];
    }
    //数据为根据pid排序后，即外层都在前面
    var parent = [];
    var target = null;
    var j = 0;
    var matchTar = {};
    for (var i = 0; i < len; i++) {
        target = data[i];
        if (target.parentid == '0') {
            parent.push({
                permission: target.permission,
                name: target.name,
                child: [],
                id: target.id
            });
        } else {
            //找parent数组
            if (matchTar.id != target.parentid) {
                //重置下，用于判断子级的pid是否有返回
                matchTar = {};
                for (j = 0; j < parent.length; j++) {
                    if (parent[j].id == target.parentid) {
                        matchTar = parent[j];
                        break;
                    }
                }
            }
            if (matchTar.id) {
                matchTar.child.push({
                    permission: target.permission,
                    name: target.name,
                    id: target.id
                })
            } else {
                console.log('当前子级的pid找不到相对应的数据，组装失败，请检查数据', target);
            }
        }
    }
    return parent;
}

//组装数据(当前有的权限)
function permissData(data) {
    if (!data.length) {
        return {};
    }
    var res = {};
    var target = null;
    var parentId = null;
    for (var i = 0; i < data.length; i++) {
        target = data[i];
        res[target.parentid + '_' + target.id] = true;
    }
    //用对象键值直接匹配，不添加分类（少循环一层）
    return res;
}

//组装已选的所属项目
function proSeleData(data) {
    if (!data || !data.length) {
        return {};
    }
    var res = {};
    var id;
    for (var i = 0; i < data.length; i++) {
        id = data[i].id;
        res[id] = 1;
        proArr.push(id);
    }
    return res;
}

//补充td
function supplementTd(len) {
    var temp = len % 5;
    if (temp === 0 && len !== 0) {
        //防止len为5的整数倍的情况
        return '';
    }
    var num = 5 - temp;
    var res = '';
    for (var i = 0; i < num; i++) {
        res += "<td></td>";
    }
    return res;
}

//模块的勾选全部
function parCheck(ele) {
    var checked = ele.checked;
    var that = $(ele);
    var nextTab = that.parent().parent().next();
    if (checked) {
        nextTab.find('[name="tabCheck"]:not(:checked)').prop('checked', true);
    } else {
        nextTab.find('[name="tabCheck"]:checked').prop('checked', false);
    }
}

//底下的勾选全部
function allCheck(ele, target) {
    var checked = ele.checked;
    var par = $('#' + target);
    if (checked) {
        par.find('[name="tabCheck"]:not(:checked)').prop('checked', true);
    } else {
        par.find('[name="tabCheck"]:checked').prop('checked', false);
    }
}

var proArr = [];

//保存
function save() {
    var par = $('#permissBox');
    var allCheck = par.find('[name="tabCheck"]:checked');
    var arr = [];
    var val;
    $.each(allCheck, function (i, ele) {
        val = ele.value;
        if (val) {
            arr.push(val);
        }
    })
    var Url;
    var sendData;
    Url = url + 'member/memberPermission';
    sendData = {
        uid: sendId,
    }
    sendData.permissionIds = arr.toString();
    sendData.projectIds = proArr.toString();
    $.ajax({
        url: Url,
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
                console.log('membername',membername);

                tips(res.message, function () {
                    window.parent.reloadPage1(membername);
                }, 1500);
            } else {
                tips(res.message);
            }
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}

//设置所属项目的html
function setProjectHtml(info, proCheck) {
    if (!info) {
        return;
    }
    var html = '';
    var checked = false;
    $.each(info, function (i, ele) {
        if (ele.length) {
            html += '<div class="proTitle">' + i + '</div>';
            html += '<div class="proList">';
            for (var j = 0; j < ele.length; j++) {
                if (proCheck[ele[j].id]) {
                    checked = true;
                }
                html += '<label class="proItem seleLabel"><input value="' + ele[j].id + '" type="checkbox" class="tabCheck" name="enable" /><span class="seleIcon"></span></span><span class="proTxt" title="' + ele[j].name + '">' + ele[j].name + '</span></label>';
                checked = false;
            }
            html += '</div>';
        }
    })
    $('#proWrap').html(html);
}

//弹窗搜索
function searchPro() {
    var val = $('[name="popSearch"]').val();
    $('.proList').hide();
    $('.proList').filter(":contains('" + (val) + "')").show();
}

//弹窗保存
function popSave() {
    var proCheck = $('#proWrap').find('[name="tabCheck"]:checked');
    proArr = [];
    $.each(proCheck, function (i, ele) {
        console.log(ele, 'ele');
        proArr.push(ele.value);
    })
    hidePop('#projectPop');
}