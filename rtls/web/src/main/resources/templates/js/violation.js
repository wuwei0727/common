var ids;//删除数据的ID
var where = {};
$(function(){
	$(document).on('click',function(e){
		hideSele();
	})
    jeDate("#start",{
        theme:{
            bgcolor:"#4A60CF", 
            pnColor:"#4A60CF"
        },
        format: "YYYY-MM-DD hh:mm:ss"
    });
    jeDate("#end",{
        theme:{
            bgcolor:"#4A60CF", 
            pnColor:"#4A60CF"
        },
        format: "YYYY-MM-DD hh:mm:ss"
    });
	where.pageIndex = 1;
	where.pageSize = pageSize;
    loadSeleData();
	init();
})
//加载下拉数据
function loadSeleData(){
    loadFun('map/getMap2dSel',{pageSize:-1,enable:1},'#mapSelect');
}
//初始化列表
function init(){
	$.ajax({
        url: url + 'park/getWeiTing',
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
            if(res.code != 200){
                tableException(tab,res.message);
                return;
            }
            var data = res.data;
            var list = data.list;
            var len = list.length;
            if(!len){
                tableException(tab,'当前搜索结果为空');
            }
            //全选按钮（取消）
            var allChe = $('#allCheck');
            if(allChe.prop('checked')){
                allChe.prop('checked',false);
            }
            if(pageIndex != data.pageIndex){
                pageIndex = data.pageIndex;
            }
        	var allName = getTheadName(tab.find('thead'));

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
                        // html += '<td class="padLeft40">' + (lineNum + i) + '</td>'
                        html += resTabCheck(listTar,lineNum + i,'id','license');
        			}else if(nameTar == 'state'){
                        html += '<td>' + (listTar[nameTar] ? '否' : '是') + '</td>';
                    }else if(nameTar == 'photolocal'){
                        if(listTar[nameTar]){
                            html += '<td><img class="tabImg" src="' + (url + listTar[nameTar]) + '" /></td>';
                        }else{
                            html += '<td></td>';
                        }
                    }else if(nameTar == 'operating'){
        				html += '<td><span class="tabOper" onclick="detail(\'' + listTar.id + '\')">详情</span><span class="tabOper deleOpa" onclick="showDele(\'' + listTar.id + '\',\'' + listTar.license + '\')">删除</span></td>';
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
    console.log(txt);

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
        url:url + 'park/delViolate/' + ids,
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
function detail(id){
	location.href = 'violationDetails.html?id=' + id;
}