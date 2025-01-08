var ids;//删除数据的ID
var where = {};
var list= {};
var mapList= [];
var polygonColor = ['#009944', '#ff9600', '#d71618'];

$(function(){
    $(document).on('click',function(e){
        hideSele();
    })
    where.pageIndex = 1;
    where.pageSize = pageSize;
    loadSeleData();
    init();
})
//加载下拉数据
function loadSeleData() {
    loadSele('map/getMap2dSel', { pageIndex: 1, pageSize: 20,areaName:"areaName" }, '#mapSelect', undefined, undefined, undefined, 1);
}
function loadSele(path, where, dom, firstTxt, tarName, fn, a) {
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
        type: 'post',
        success: function (res) {
            if (res.code != 200) {
                tips('获取选项失败');
                return;
            }
            selAreaData = res.data.areaName;
            mapList = res.data.list;
            var selData = JSON.stringify(res.data.areaName).replace(/"/g, "'");
            var html = '';
            var curName = temp.tarName || 'mapName';
            var curId = temp.id || 'mapId';
            var curTxt = temp.txt || '--请选择关联地图--';
            if (typeof firstTxt !== "boolean") {
                html = '<div onclick="seleBatch(this,\'\')" data-id="">' + curTxt + '</div>';
            }
            var list = res.data.areaName || [];
            var firstTxt = {tarName: "areaName", id: "barrierGateId", txt: "--请选择绑定区域--"};
            for (var i = 0; i < list.length; i++) {
                html += '<div onclick="seleBatch(this,\'' + list[i][curId] +  '\',\'\',' + selData+  ',\'areaNameList\',\'#barrierGateId\','+ JSON.stringify(firstTxt).replace(/"/g, "'") +')" data-id="' + list[i][curId] + '" title="' + list[i][curName] + '">' + list[i][curName] + '</div>';
            }
            $(dom).html(html);
            fn ? fn() : '';
        },
        error: function (err) {
            resError(err);
        }
    })
}

function reset() {
    $('#barrierGateId').html('<div onclick="seleBatch(this,\'\')" data-id="">--请先选择关联地图--</div>');
    resetSearch();
}
//初始化列表
function init(){
    $.ajax({
        url: url + 'recommConfig/getAllRecommConfigOrConditionQuery',
        data:where,
        type:'post',
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success: function(res) {
            var tab = $('#tab');
            if(res.code !== 200){
                tableException(tab,res.message);
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
            if(allChe.prop('checked')){
                allChe.prop('checked',false);
            }
            if(pageIndex !== data.pageIndex){
                pageIndex = data.pageIndex;
            }
            var allName = getTheadName(tab.find('thead'));

            var html = '';
            var nameTar = null;
            var listTar = null;
            var value = null;
            var lineNum = (pageIndex - 1) * pageSize + 1;
            for(var i = 0;i < len;i++){
                listTar = list[i];
                html += '<tr>';
                for(var j = 0;j < allName.length;j++){
                    nameTar = allName[j];
                    if(nameTar === ''){
                        continue;
                    }
                    value = listTar[nameTar];
                    if (nameTar === 'line') {
                        html += resTabCheck(listTar, lineNum + i, "id", "areaname");
                    } else if (nameTar === 'operating') {
                        var editBtn = document.getElementById("editBtn");
                        var deleteBtn = document.getElementById("deleteBtn");
                        var editTxt = editBtn ? '<span class="tabOper" onclick="detail(\'' + listTar.id + '\')">编辑</span>' : '';
                        var delteTxt = deleteBtn ? '<span class="tabOper deleOpa" onclick="showDele(\'' + listTar.id + '\',\'' + listTar.areaname + '\')">删除</span>' : '';
                        var posTxt = '<span class="tabOper deleOpa" onclick="showlocation(\'' + listTar.id + '\',\'' + listTar.num + '\',)">位置</span>';
                        html = html + '<td>' + editTxt + delteTxt + posTxt + '</td>';
                    }else if(nameTar === 'recommelevel'){
                        html += '<td>';
                        if(value === 3){
                            html += '高';
                        } else if(value === 2){
                            html += '中';
                        } else if(value === 1){
                            html += '低';
                        }
                        html += '</td>';
                    }else{
                        html += '<td>' + convertNull(listTar[nameTar]) + '</td>';
                    }
                }
                html += '</tr>';
            }
            tab.find('tbody').html(html);
            var htmlStr ="共 <span class='c4A60CF'>"+ data.pages +" </span> 页 / <span class='c4A60CF'>" + data.total + " </span>条数据"
            $('[id="total"]').html(htmlStr);
            //生成页码
            initDataPage(pageIndex,data.pages,data.total);
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
//单行的删除提示
function showDele(id,txt){
    ids = id;
    $('#deleTxt').text(txt);
    showPop('#delePop');
}
//多行的删除
function showAllDele(){
    var cheInp = $('#tab tbody').find('input:checked');
    if(!cheInp.length){
        tips('请选择至少一条数据');
        return;
    }
    var showTxt = '';
    var cheId = '';
    for(var i = 0;i < cheInp.length;i++){
        showTxt += cheInp[i].getAttribute('data-txt') + '、';
        cheId += cheInp[i].value + ',';
    }
    showTxt = showTxt.slice(0,-1);
    ids = cheId.slice(0,-1);
    $('#deleTxt').html(showTxt);
    showPop('#delePop');
}
//确认删除
function entDele(){
    $.ajax({
        url:url + 'recommConfig/delRecommConfig/' + ids,
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success:function(res){
            tips(res.message);
            if(res.code == 200){
                hidePop('#delePop');
                search();
            }
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
//翻页
function turnPage(){
    where.pageIndex = pageIndex;
    init();
}
//编辑
function detail(id) {
    $('html').attr('style', 'overflow:hidden')
    $('#editFrame').attr('style', 'z-index: 99')
    $('#editFrame').attr('src', './recommendDetail.html?id=' + id)
    $('#editFrame').show()
}

function addRecommend() {
    $('html').attr('style', 'overflow:hidden')
    $('#addFrame').attr('style', 'z-index: 99')
    $('#addFrame').attr('src', './recommendDetail.html')
    $('#addFrame').show()
}

function closeFrame(){
    $('#addFrame').hide()
    $('#editFrame').hide()
    $('html').attr('style', 'overflow:auto')
    search()
}

// 位置显示推荐 （需要传优先级别、顶点数据）
function showlocation(id) {
    var initPoints = [];
    $('#mapPop').show();
    $('#mask').show();
    if (id) {
        $.ajax({
            url: url + 'recommConfig/getRecommConfigInfoById/' + id,
            beforeSend: function () {
                loading();
            },
            complete: function () {
                removeLoad();
            },
            success: function (res) {
                console.log(res)
                if (res.code == 200) {
                    var data = res.data[0];
                    var initPoints = [];
                    if (data.vertexInfo) {
                        data.vertexInfo.forEach(item => {
                            let obj = {};
                            let tempPoinArr = [];
                            item.points.forEach(subItem => {
                                obj = { x: subItem.x, y: subItem.y, floor: +subItem.floor };
                                tempPoinArr.push(obj);
                            });
                            initPoints.push(tempPoinArr);
                        });
                        var idmai = id

                        result = contrastReturn(list, 'id', idmai);
                        var findItem = mapList.find(item => item.id == data.map);
                        openMap({
                            fKey: result.mapKey || '',
                            fName: result.appName || '',
                            fId: result.fmapID || '',
                            // focusFloor: Number(result.floor) || '',
                            path: result.themeImg || '',
                            x: result.x,
                            y: result.y,
                            num: result.deviceNum,
                            //初始指北针的偏移量
                            compassOffset: [20, 12],
                            //指北针大小默认配置
                            compassSize: 48,
                            FMViewMode: fengmap.FMViewMode.MODE_2D,
                            polygonColor: polygonColor[result.recommelevel - 1],
                            wantToDo: "drawArea",
                            initPoints: initPoints,
                            themeName: findItem.themeName,
                            mapId: result.map
                        }, function () {
                            // analyser = new fengmap.FMSearchAnalyser(fMap);
                            // var x = +$('[name="x"]').val();
                            // var y = +$('[name="y"]').val();
                            // if (x && y) {
                            //     fMap.moveTo({
                            //         x: x,
                            //         y: y,
                            //         groupID: floor,
                            //     })
                            // }
                            // var fid = $('[name="fid"]').val();
                            // var target = null;
                            // if (fid) {
                            //     target = findModel({ FID: fid });
                            //     if (target) {
                            //         target.selected = true;
                            //         selectedModel = target;
                            //     }
                            // }
                        });
                    }
                }
            }
        })
    }
}