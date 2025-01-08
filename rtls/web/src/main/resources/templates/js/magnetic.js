var where = {};
$(function(){
    $(document).on('click',function(e){
        hideSele();
    })
    where.pageIndex = 1;
    where.pageSize = pageSize;
    init();
})
//初始化列表
function init(){
    $.ajax({
        url: url + 'nb/getNbDevice',
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
            var target = null;
            var name = null;
            var value = null;
            var lineNum = (pageIndex - 1) * pageSize + 1;
            for(var i = 0;i < len;i++){
                target = list[i];
                html += '<tr>';
                for(var j = 0;j < allName.length;j++){
                    name = allName[j];
                    if(name === ''){
                        continue;
                    }
                    value = target[name];
                    if(name == 'line'){
                        html += '<td class="padLeft40">' + (lineNum + i) + '</td>';
                    }else if(name == 'operating'){
                        html += '<td><span class="tabOper" onclick="detail(\'' + target.id + '\')">详情</span></td>';
                    }else if(name == 'status'){
                        html += '<td>';
                        if(value == 1){
                            html += '<span class="stateHas">已停</span>';
                        }else if(value == 2){
                        }else{
                            html += '空闲';
                        }
                        html += '</td>';
                    }else{
                        html += '<td>' + convertNull(value) + '</td>';
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
//搜索
function search(){
    where.pageIndex = 1;
    if(where.desc){
        var curSort = document.getElementById('curSort');
        curSort.className = 'tabSort';
        curSort.id = '';
        delete where.desc;
    }
    var searchItem = $('.search').find('[name]');
    var target = null;
    for(var i = 0;i < searchItem.length;i++){
        target = searchItem[i];
        if(target.className == 'batchTxt'){
            //自定义的下拉
            where[target.getAttribute('name')] = $(target).data('val');
        }else{
            where[target.getAttribute('name')] = target.value;
        }
    }
    init();
}
//翻页
function turnPage(){
    where.pageIndex = pageIndex;
    init();
}
//编辑
function detail(id){
    location.href = 'magneticDetails.html?id=' + id;
}