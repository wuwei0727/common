var deleteId;//删除数据的ID
var where = {};
$(function(){
	$(document).on('click',function(e){
		hideSele();
	})
	where.pageIndex = 1;
	where.pageSize = pageSize;
	where.mapAll = 12;//用于区分地图管理页面
    loadSeleData();
    init();
})
//加载下拉数据
function loadSeleData(){
    loadFun3('map/getMap2dSel',{pageSize:-1,status:0,mapAll:13},'#mapSelect1');
}
//初始化列表
function init(){
	$.ajax({
        url: url + 'map/getMap2dSel',
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
            var theme = data.maptheme;
            theme = JSON.stringify(theme).replace(/\"/g,"'");
            var addTheme = document.getElementById('addTheme');
            if(addTheme){
                addTheme.outerHTML = '<span onclick="detail(' + theme +')">新增</span>';
            }
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
            var fillVal;
        	var nameTar = null;
        	var listTar = null;
            var lineNum = (pageIndex - 1) * pageSize + 1;
        	for(var i = 0;i < len;i++){
        		listTar = list[i];
        		html += '<tr>';

        		for(var j = 0;j < allName.length;j++){
        			nameTar = allName[j];
                    fillVal = listTar[nameTar];
        			if(nameTar == 'line'){
                        html += resTabCheck(listTar,lineNum + i);
        			}else if(nameTar == 'operating'){
                        var editBtn = document.getElementById("editBtn");
                        var deleteBtn = document.getElementById("deleteBtn");
                        // var editTxt = editBtn ? '<span class="tabOper" onclick="detail(\'' + listTar.id + '\',' + theme +')">编辑</span>' : '';
                        var editTxt = editBtn ? '<span class="tabOper" onclick="detail('+ theme + ',\'' + listTar.id +'\')">编辑</span>' : '';
                        var delteTxt = deleteBtn ? '<span class="tabOper deleOpa" onclick="showDele(\'' + listTar.id + '\',\'' + listTar.name + '\')">删除</span>' : '';
                        var posTxt = '<span class="tabOper deleOpa" onclick="showQrcode(\'' + listTar.id + '\',\'' + listTar.name + '\',\'' + listTar.qrcode + '\')">二维码</span>';
                        html = html + '<td>' + editTxt + delteTxt + posTxt + '</td>';
        			}else if(nameTar == 'enable'){
                        html += '<td><img class="imgStatus" src="../image/common/';
                        if(fillVal == 1){
                            html += 'Y';
                        }else{
                            html += 'N';
                        }
                        html += '.png"></td>';
                    }else if(nameTar == 'describe'){
                        html += describeElli(fillVal);
                    }else{
                        html += '<td>' + convertNull(fillVal) + '</td>';
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
    deleteId = id;
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
    deleteId = cheId.slice(0,-1);
    $('#deleTxt').html(showTxt);
	showPop('#delePop');
}
//确认删除
function entDele(){
	$.ajax({
        url:url + 'map/delMap2d/' + deleteId,
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success:function(res){
            tips(res.message);
            document.write(res);
        	if(res.code == 200){
                hidePop('#delePop');
        		init();
                location.reload();
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
//翻页
function turnPage(){
	where.pageIndex = pageIndex;
	init();
}
//编辑
function detail(theme, id){
    $('html').attr('style', 'overflow:hidden')
    theme = encodeURI(JSON.stringify(theme));
    $('#editFrame').attr('style', 'z-index: 99') 
    $('#editFrame').attr('src', './mapDetails.html?id=' + id + '&maptheme=' + theme) 
    $('#editFrame').show()
}

function addMap() {
    $('html').attr('style', 'overflow:hidden')
    $('#addFrame').attr('style', 'z-index: 99') 
    $('#addFrame').attr('src', './mapDetails.html') 
    $('#addFrame').show()
}

function closeFrame(){
    $('html').attr('style', 'overflow:auto')
    $('#addFrame').hide()
    $('#editFrame').hide()
    $('#codeFrame').hide()
    loadSeleData();
    search();
}

//处理地图描述的文本
function describeElli(str){
    if(!str || str.length < 30){
        return '<td>' + str + '</td>';
    }
    return '<td title="' + str + '">' + str.slice(0,30) + '...</td>';
}

// 地图二维码
function showQrcode(id,name,qrcode){
// location.href='./mapQrcode.html?id='+id+'&name='+name+'&qrcode='+qrcode
$('#codeFrame').attr('style', 'z-index: 99') 
$('#codeFrame').attr('src', './mapQrcode.html?id=' + id + '&name='+name+'&qrcode='+qrcode) 
$('#codeFrame').show()
}