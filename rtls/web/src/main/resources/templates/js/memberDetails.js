var ID;
var cId;
$(function(){
 
    
	$(document).on('click',function(e){
		hideSele();
	})
	loadSeleData();
	var id = getUrlStr('id');
	var cid = getUrlStr('cid');
	if(id){
		ID = id;
        cId = cid;
		init(url + 'member/getMemberId'+'?id=' + ID+ '&cid='+cId);
		$('#titleFlag').html('编辑');
       
	}else{
        loadFunMore('map/getMap2dSel',{pageSize:-1,enable:1},'#mapid');
    }
    var account = getUrlStr('account');
    if(account){
        //账户设置过来的
        //获取当前账户的信息
        init(url + 'member/getCurrentMember',account);
        $('.topTitle').html('账户设置');
    }
})
//加载下拉数据
function loadSeleData(){
    loadFun1('company/getCompanySel',{pageSize:-1},'#cidSele',false,'cname');
}
//初始化数据
function init(url,account){
	$.ajax({
        url:url,
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
                var list = res.data.list || {};
                var mapList = list.mapList;
                let arr=[]
                for(let i=0;i<mapList.length;i++){
                   arr.push(mapList[i].id)
                }
        		setData(list,'.main');
                
                // $('[name="passWs"]').val(list.password);
                // $('[name="entPassword"]').val(list.password);
                $('[name="cid"]').html(list.cname);
                if(list.cid) {
                    $('[name="cid"]').eq(0).addClass('batchTxtChange');
                }
                if(list.mapList.length > 0) {
                    $('[name="mapid"]').eq(0).addClass('batchTxtChange');
                }
                if(account){
                    ID = list.uid;
                }
                loadFunMore('map/getMap2dSel',{pageSize:-1,enable:1},'#mapid',arr.join(','));
        	}else{
        		tips(res.message);
        	}
        },
        error:function(jqXHR){
            resError(jqXHR);
        }
    })
}
//保存
function save(){
	var sendData = getData('.main');
    // if(!sendData){
    //     return;
    // }
    //防止浏览器的填充,修改name
    sendData.password = sendData.passWs;
    delete sendData.passWs;
    if(!sendData.membername){
        tips('成员名称不能为空');
        return;
    }
    if(!sendData.phone){
        tips('手机号码不能为空');
        return;
    }
    if(!sendData.cid){
        tips('请选择所属权限组');
        return;
    }
    if(!sendData.mapid){
        tips('当前新增用户未分配地图');
        return;
    }

    var reg_phone = /^1[3-9]\d{9}$/;
    if(!reg_phone.test(sendData.phone)){
        tips('请输入正确的手机号码！！！');
        return;
    }
    // if(!sendData.password){
    //     tips('登录密码不能为空');
    //     return;
    // }
    if(sendData.password !== sendData.entPassword){
        tips('两次输入的密码不一致，请检查');
        return;
    }
    delete sendData.entPassword;
    if(!sendData.cid){
        tips('所属权限组不能为空，请选择');
        return;
    }
  /*  if(!sendData.mapid){
        tips('所属地图不能为空，请选择');
        return;
    }*/
	var path = url;
	if(ID){
		path += 'member/updateMember';
		sendData.uid = ID;
	}else{
		path += 'member/addMember';
	}
	$.ajax({
        url:path,
        data:sendData,
        type:'post',
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success:function(res){
        	if(res.code === 200){
        		tips(res.message,function(){
        			if(!ID){
        				location.reload();
        			}
        		},1000);
        	}else{
        		tips(res.message);
                if (!res.code) {
                    let newWindow = window.open('about:blank');
                    newWindow.document.write(res);
                    newWindow.focus();
                    window.history.go(-1);
                }
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