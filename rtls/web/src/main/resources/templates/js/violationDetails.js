var ID;
var mapList = [];
var currentMapId;//初始化的id，不同则重新初始化地图
//定义搜索分析类
var analyser = null;
//选中的模型
var selectedModel = null;
$(function(){
	var id = getUrlStr('id');
	if(id){
		ID = id;
		init();
	}
})
//初始化数据
function init(){
	$.ajax({
        url:url + 'park/getWeiTingById/' + ID,
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
                $('#state').val(data.state ? '否' : '是');
                var photolocal = data.photolocal;
                if(photolocal){
                    $('#photolocal').attr('src',url + photolocal);
                }
        	}else{
        		tips(res.message);
        	}
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}