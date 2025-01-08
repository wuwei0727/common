var ID;
$(function(){
	$(document).on('click',function(e){
		hideSele();
	})
	var id = getUrlStr('id');
	if(id){
		ID = id;
		init();
		$('#titleFlag').html('详情');
	}
})
//初始化数据
function init(){
	$.ajax({
        url:url + 'nb/getNbById/' + ID,
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success:function(res){
        	if(res.code == 200){
                var data = res.data;
        		setData(data,'.main');
                $('[name="status"]').val(data.status == 0 ? '空闲' : '已停');
        	}else{
        		tips(res.message);
        	}
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}