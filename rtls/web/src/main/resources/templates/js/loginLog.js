var where = {
    pageIndex:pageIndex,
    pageSize:pageSize,
};
$(function(){
    jeDate("#timeDate",{
        theme:{
            bgcolor:"#4A60CF", 
            pnColor:"#4A60CF"
        },
        multiPane:false,
        range:" 至 ",
        format: "YYYY-MM-DD"
    });
	init();
})
//初始化列表
function init(){
	$.ajax({
        url: url + 'member/getLoginRecordSel',
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
                        html += '<td class="padLeft40">' + (lineNum + i) + '</td>';
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
    var tarName;
    var tarVal;
    var tarTxt;
	for(var i = 0;i < searchItem.length;i++){
		target = searchItem[i];
        tarName = target.getAttribute('name');
		if(target.className == 'batchTxt'){
			//自定义的下拉
			tarVal = $(target).data('val');
            tarTxt = target.innerHTML;
		}else{
            tarTxt = tarVal = target.value;
		}
        where[tarName] = tarVal;
	}
    var timeDate = $('#timeDate').val().split(' 至 ');
    var s = timeDate[0] || '';
    if(s){
        s += ' 00:00';
    }
    var e = timeDate[1] || '';
    if(e){
        e += ' 24:00';
    }
    where.startTime =  s;
    where.endTime = e;
	init();
}
//翻页
function turnPage(){
    where.pageIndex = pageIndex;
    init();
}
//页面重置
function pageReset(){
    $('#timeDate').val('');
    resetSearch();
}