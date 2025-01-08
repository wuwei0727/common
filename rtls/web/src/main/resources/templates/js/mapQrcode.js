var ID;
var imgurl
$(function(){
// 图片预览
    $(".pic").click(function () {
        var _this = $(this);//将当前的pimg元素作为_this传入函数 
        imgShow("#outerdiv", "#innerdiv", "#bigimg", _this);
      });
	$(document).on('click',function(e){
		hideSele();
	})
	var id = getUrlStr('id');
    // console.log(id,'地图id');
    
	if(id){
		ID = id;
		init();
		$('#titleFlag').html('管理');
	}
})
//初始化数据
function init(){
	$.ajax({
        url:url + 'map/getMap2dQrCode/' + ID,
        beforeSend:function(){
            loading();
        },
        complete:function(){
            removeLoad();
        },
        success:function(res){
        	if(res.code == 200){
                var data = res.data;
                if(!data){
                    return;
                }
                if(data.type != 2){
                    tips('地图类型不正确，请检查数据');
                    return;
                }
        		setData(data,'.main');
                // var url='http://192.168.1.95:7003/'
                // imgurl=url+data.qrcode
                var url='../'
                imgurl=url+data.qrcodelocal


           document.getElementById("testimg1").src = imgurl
           document.getElementById("down_btn_a").href =imgurl 
           function downloadFile(url, fileName){
            var download = new XMLHttpRequest();
             download.open("GET", url, true);
             download.responseType = 'blob';
             download.onload=function(e) {
                 var url = window.URL.createObjectURL(download.response)
                 var a = document.createElement('a');
                 a.href = url
                 a.download = fileName;
                 a.click()
             }
             download .send();
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

//获取组装数据（图片）
function getDataImg(target){
    var formData = new FormData();
    var searchItem = $(target).find('[name]:visible');
    var target = null;
    var tarName = null;
    var tarVal;
    var reg = null;
    for(var i = 0;i < searchItem.length;i++){
        target = searchItem[i];
        reg = target.getAttribute('data-reg');
        tarName = target.getAttribute('name');
        if(target.className === 'batchTxt'){
            //自定义的下拉
            tarVal = $(target).data('val');
        }else{
            if(target.type === 'file'){
                tarVal = target.files[0] || '';
            }else{
                tarVal = target.value;
            }
        }
        if(reg){
            if(target.type === 'file'){
                if(!ID && !tarVal){
                    tips('请上传文件');
                    return false;
                }
            }else{
                if(!new RegExp(reg).test(tarVal)){
                    tips('请填写必填项或正确填写');
                    return false;
                }
            }
        }
        formData.append(tarName,tarVal);
    }
    return formData;
}
//上传蜂鸟地图提示
function uploadFmap(that,txtTarget){
    var files = that.files[0];
    var txt = $('#' + txtTarget);
    if(!files){
        //取消
        txt.html('');
        return;
    }
    txt.html('已选择');
}


// 图片预览


  function imgShow(outerdiv, innerdiv, bigimg, _this) {
    var src = _this.attr("src");//获取当前点击的pimg元素中的src属性 
    $(bigimg).attr("src", src);//设置#bigimg元素的src属性 
    /*获取当前点击图片的真实大小，并显示弹出层及大图*/
    $("<img/>").attr("src", src).load(function () {
      var windowW = $(window).width();//获取当前窗口宽度 
      var windowH = $(window).height();//获取当前窗口高度 
      var realWidth = this.width;//获取图片真实宽度 
      var realHeight = this.height;//获取图片真实高度 
      var imgWidth, imgHeight;
      var scale = 0.8;//缩放尺寸，当图片真实宽度和高度大于窗口宽度和高度时进行缩放 
      if (realHeight > windowH * scale) {//判断图片高度 
        imgHeight = windowH * scale;//如大于窗口高度，图片高度进行缩放 
        imgWidth = imgHeight / realHeight * realWidth;//等比例缩放宽度 
        if (imgWidth > windowW * scale) {//如宽度扔大于窗口宽度 
          imgWidth = windowW * scale;//再对宽度进行缩放 
        }
      } else if (realWidth > windowW * scale) {//如图片高度合适，判断图片宽度 
        imgWidth = windowW * scale;//如大于窗口宽度，图片宽度进行缩放 
        imgHeight = imgWidth / realWidth * realHeight;//等比例缩放高度 
      } else {//如果图片真实高度和宽度都符合要求，高宽不变 
        imgWidth = realWidth;
        imgHeight = realHeight;
      }
      $(bigimg).css("width", imgWidth);//以最终的宽度对图片缩放 
      var w = (windowW - imgWidth) / 2;//计算图片与窗口左边距 
      var h = (windowH - imgHeight) / 2;//计算图片与窗口上边距 
      $(innerdiv).css({ "top": h, "left": w });//设置#innerdiv的top和left属性 
      $(outerdiv).fadeIn("fast");//淡入显示#outerdiv及.pimg 
    });
    $(outerdiv).click(function () {//再次点击淡出消失弹出层 
      $(this).fadeOut("fast");
    });
  }


 