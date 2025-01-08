var where = null;
var tabLen = 0;//表格的td列数，用于无数据时
var thead = null;
$(function(){
	//计算pageSize
    calcPage();
    where = {
        bname:'',
        btype:2,
        start_time:'',
        end_time:'',
        pageIndex:pageIndex,
        pageSize:pageSize,
    }
	init();
    //初始化时间范围
    laydate.render({
        elem: '#seleTime',
        theme: '#0876D7',
        type: 'datetime',
        range: '至',
        done:function(value){
            //确定清除的回调
            $('#timeRange').html(value);
        }
    });
    thead = document.getElementById('thead');
})
//加载数据
function init(){
    $.ajax({
        url: url + 'batch/getBatchSel',
        type:'post',
        data:where,
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success: function(data) {
            tips(data.msg);
            var tab = $('#tab');
            if(!data.status){
                tab.find('tbody').html(resErrTab(tabLen,tab));
                initDataPage(0,0,0);
                return;
            }
            var list = data.list || [];
            var len = list.length;

            if(!len){
                tab.find('tbody').html(noData(tabLen));
                initDataPage(0,0,0);
                return;
            }
            if(pageIndex != data.pageIndex){
                where.pageIndex = pageIndex = data.pageIndex;
            }
            
            var allName = getTheadName(tab.find('thead'));
            tabLen || (tabLen = allName.length);//为0时则赋值
            var html = '';
            var nameTar = null;
            var listTar = null;
            var lineNum = (pageIndex - 1) * pageSize + 1;
            for(var i = 0;i < len;i++){
                listTar = list[i];
                html += '<tr>';
                for(var j = 0;j < allName.length;j++){
                    nameTar = allName[j];
                    if(nameTar == 'line'){
                        html += '<td class="padLeft40">' + (lineNum + i) + '</td>';
                    }else if(nameTar == 'operating'){
                        html += '<td><span class="tabOper" onclick="javascript:tips(\'先不重要\')">下载</span><span class="tabOper delete" onclick="result(\'' + listTar.id + '\')">测试结果</span><span class="tabOper delete" onclick="dele(\'' + listTar.id + '\',\'' + listTar.bname + '\')">删除</span></td>';
                    }else{
                        html += '<td>' + (listTar[nameTar] || '') + '</td>';
                    }
                }
                html += '</tr>';
            }
            tab.find('tbody').html(html);
            //生成页码
            initDataPage(pageIndex,data.pages,data.total);
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
var deleId;
//删除
function dele(id,txt){
	deleId = id;
    $('#deleTxt').text(txt);
    showPop('#deleInq');
}
//确认删除
function entDele(){
    $.ajax({
        url:url + 'batch/delBatch',
        data:{
            ids:deleId
        },
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success:function(data){
            if(data.status){
                hidePop('#deleInq');
                deleId = '';
                tips(data.msg,function(){
                    init();
                },500);
            }else{
                tips(data.msg);
            }
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
//搜索事件
function search(num){
    if(num){
        where.bname = $('[name="sBname"]').val();
        var seleTime = $('#seleTime').val().split(' 至 ');
        where.start_time = seleTime[0] || '';
        where.end_time = seleTime[1] || '';
        where.pageIndex = 1;
    }else{
        where.pageIndex = pageIndex;
    }
    init();
}
var resultWhere = null;
//赋值
function result(id){
    resultWhere = null;
    resultWhere = {
        batch_id:id,
        pageSize:-1
    };
    $('[name="resultSele"]').val('');
    $('[name="htypeSele"]').val('');
    $('[name="hidInp"]').val('');
    getResult();
}
//获取测试结果
function getResult(){
    $.ajax({
        url:url + 'hoare/getHoareSel',
        data:resultWhere,
        type:'post',
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success:function(data){
            if(!data.status){
                tips(data.msg);
                return;
            }
            var list = data.list;
            var len = list.length;
            var tab = $('#resultTab');
            if(!len){
                tips('当前搜索返回的数据为空');
                tab.find('tbody').html('');
                return;
            }
            var html = '';
            var listTar = '';
            var nameTar = '';
            var allName = getTheadName(tab.find('thead'));
            for(var i = 0;i < list.length;i++){
                listTar = list[i];
                html += '<tr>';
                for(var j = 0;j < allName.length;j++){
                    nameTar = allName[j];
                    if(nameTar == 'line'){
                        html += '<td class="padLeft40">' + (1 + i) + '</td>';
                    }else{
                        html += '<td>' + listTar[nameTar] + '</td>';
                    }
                }
                html += '</tr>';
            }
            tab.find('tbody').html(html);
            showPop('#resultPop');
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
//弹窗搜索
function popSearch(){
    resultWhere.hid = $('[name="hidInp"]').val();
    resultWhere.htype = $('[name="htypeSele"]').val();
    resultWhere.result = $('[name="resultSele"]').val();
    getResult();
}
//表格滚动固定表头
function tabScroll(that){
    thead.style.transform = 'translateY(' + (that.scrollTop) + 'px)';
}