var curId;//当前操作的ID
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
    loadSeleData();
	$.ajax({
        url: url + 'company/getCompanySel',
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
            if (!res.code) {
                let newWindow = window.open('about:blank');
                newWindow.document.write(res);
                newWindow.focus();
                window.history.go(-1);
            }else if(res.code != 200){
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
        			if(nameTar === 'line'){
                        html += resTabCheck(listTar,lineNum + i,'id','cname');
        			}else if(nameTar == 'operating'){
                        var editBtn = document.getElementById("editBtn");
                        var deleteBtn = document.getElementById("deleteBtn");
                        var powerBtn = document.getElementById("powerBtn");
                        var editTxt = (editBtn && listTar.describe !== 'admin') ? '<span class="tabOper" onclick="edit(\'' + listTar.id + '\')">编辑</span>' : '';
                        var delteTxt = (deleteBtn && listTar.describe !== 'admin') ? '<span class="tabOper deleOpa" onclick="showDele(\'' + listTar.id + '\',\'' + listTar.cname + '\')">删除</span>' : '';
                        var powerTxt = (powerBtn && listTar.describe !== 'admin') ? '<span class="tabOper deleOpa" onclick="permissSet(\'' + listTar.id + '\')">权限设置</span>' : '';
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
    					html += '<td>' + (listTar[nameTar] || '') + '</td>';
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
//搜索
function search(){
	where.pageIndex = 1;
	var searchItem = $('.search').find('[name]');
	var target = null;
	for(var i = 0;i < searchItem.length;i++){
		target = searchItem[i];
		if(target.className.indexOf('batchTxt') !== -1){
			//自定义的下拉
			where[target.getAttribute('name')] = $(target).data('val');
		}else{
			where[target.getAttribute('name')] = target.value;
		}
	}
	init();
}
//单行的删除提示账户
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
        url:url + 'company/delCompany/' + curId,
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
function edit(id){
    $('#editFrame').attr('style', 'z-index: 99') 
    $('#editFrame').attr('src', './permissDetail.html?id=' + id) 
    $('#editFrame').show()
}
//保存
function save(){
    var cname = $.trim($('[name="cnamePop"]').val());
    if(!cname){
        tips('权限组名称不能为空');
        return;
    }
    var path = url;
    var data = {
        cname:cname,
        describe:$('[name="describePop"]').val(),
        enabled:document.getElementById('id_enablePop').checked ? 1 : 0
    }
    if(curId){
        data.id = curId;
        path += 'company/updateCompany';
    }else{
        path += 'company/addCompany';
    }
    $.ajax({
        url: path,
        type:'post',
        data:data,
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success: function(res) {
            tips(res.message);
            if(res.code === 200){
                closePop('#permissPop');
                init();
            }
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
//权限设置
function permissSet(id){
    $('#powerFrame').attr('style', 'z-index: 99') 
    $('#powerFrame').attr('src', './permissSet.html?id=' + id) 
    $('#powerFrame').show()
}
function closeFrame(){
    $('#addFrame').hide()
    $('#editFrame').hide()
    $('#powerFrame').hide()
    search()
}
//关闭弹窗
function closePop(target){
    //恢复状态
    $('[name="cnamePop"]').val('');
    $('[name="describePop"]').val('');
    var enablePop = document.getElementById('id_enablePop');
    if(enablePop.checked){
        enablePop.checked = false;
    }
    hidePop(target);
}
//新增
function addGroup(){
    $('#addFrame').attr('style', 'z-index: 99')
    $('#addFrame').attr('src', './permissDetail.html')
    $('#addFrame').show()
}