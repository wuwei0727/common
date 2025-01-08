var ID;
$(function(){
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
        url:url + 'infrared/getInfraredId/' + ID,
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
                if(!data){
                    return;
                }
                setData(data,'.main');
                $('[name="status"]').val(data.status === 0 ? '空闲':'已停' );
                $('[name="networkstate"]').val(
                    data.networkstate === 0 ? '离线' :
                    data.networkstate === 1 ? '在线' :'低电量'
                    // data.networkstate === 2 ? '低电量':
                    // data.networkstate === 3 ? '低电量在线': '低电量离线'
                );
            }else{
                tips(res.message);
            }
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}