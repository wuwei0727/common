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
        url:url + 'sub/getSubId/' + ID,
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success:function(res){
			if (!res.code) {
				let newWindow = window.open('about:blank');
				newWindow.document.write(res);
				newWindow.focus();
				window.history.go(-1);
			}else if(res.code == 200){
                var data = res.data;
        		setData(data,'.main');
        	}else{
        		tips(res.message);
        	}
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}