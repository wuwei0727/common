var curId;//
var where = {};
$(function(){
	$(document).on('click',function(e){
		hideSele();
	})
	where.pageIndex = pageIndex;
	where.pageSize = pageSize;
    loadSeleData();
	init();
})

//加载下拉数据
function loadSeleData(){
    loadFun1('company/getCompanySel',{pageSize:-1},'#permissSelect');
}

//初始化列表
function init(){
	$.ajax({
        url: url + 'member/getMemberSel',
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
            if(pageIndex !== data.pageIndex){
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
        			if(nameTar === 'line'){
                        html += resTabCheck(listTar,lineNum + i,'uid','membername');
        			}else if(nameTar === 'operating'){
                        var editBtn = document.getElementById("editBtn");
                        var deleteBtn = document.getElementById("deleteBtn");
                        var powerBtn = document.getElementById("powerBtn");
                        var editTxt = '', delteTxt = '', powerTxt = '';
                        editTxt = (editBtn && listTar.describe !== 'admin') ? '<span class="tabOper" id="editBtn" onclick="edit(\'' + listTar.uid + '\',\'' + listTar.cid +'\')">编辑</span>' : '';
                        delteTxt = (deleteBtn && listTar.describe !== 'admin') ? '<span class="tabOper deleOpa" onclick="showDele(\'' + listTar.uid + '\',\'' + listTar.membername + '\')">删除</span>' : '';
                        powerTxt = (powerBtn && listTar.describe !== 'admin') ? '<span class="tabOper deleOpa" onclick="permissSet(\'' + listTar.uid + '\',\'' + listTar.cid +'\')">权限设置</span>' : '';
                        html = html + '<td>' + editTxt + delteTxt + powerTxt + '</td>';
        			}else if(nameTar === 'enabled'){
                        html += '<td><img class="imgStatus" src="../image/common/';
                        if(listTar[nameTar] === 1){
                            html += 'Y';
                        }else{
                            html += 'N';
                        }
                        html += '.png"></td>';
                    }else{
                        if(nameTar === 'mapName'){
                            html += '<td class="cheweitd">' + convertNull(listTar[nameTar]) + '</td>';
                        }else {
                            html += '<td>' + convertNull(listTar[nameTar]) + '</td>';
                        }
    					// html += '<td>' + (listTar[nameTar] || '') + '</td>';
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
    curId = id;
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
    curId = cheId.slice(0,-1);
    $('#deleTxt').html(showTxt);
	showPop('#delePop');
}
//确认删除
function entDele(){
	$.ajax({
        url:url + 'member/delMember/' + curId,
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
function edit(id,cid){
    $('#editFrame').attr('style', 'z-index: 99') 
    $('#editFrame').attr('src', './memberDetails.html?id=' + id+'&cid='+cid) 
    $('#editFrame').show()
}
function addMemeber() {
    $('#addFrame').attr('style', 'z-index: 99') 
    $('#addFrame').attr('src', './memberDetails.html') 
    $('#addFrame').show()
}
function closeFrame(){
    $('#addFrame').hide()
    $('#editFrame').hide()
    $('#powerFrame').hide()
    search()
}
//权限设置
function permissSet(id,cid){
    if(cid==="null"){
        cid=''
    }
    // location.href = 'memberPermissSet.html?id=' + id+'&cid='+cid;
    $('#powerFrame').attr('style', 'z-index: 99') 
    $('#powerFrame').attr('src', './memberPermissSet.html?id=' + id +'&cid='+cid) 
    $('#powerFrame').show()
}

//刷新
function reloadPage1(membername) {
    console.log('刷新页面afadfadfadf',membername);
    window.parent.reloadPage1(membername);
}