var timeTxt = null;
var qua;//数量
var count = 0;
var timer = null;
var quantity = null;
var detail = null;
var inputVal = null;
$(function(){
	timeTxt = document.getElementById('timeTxt');
	quantity = document.getElementById('quantity');
	detail = document.getElementById('detail');
})
function start(){
	if(timer){
		alert('请先结束当前请求后再发起');
		return;
	}
	inputVal = $.trim(document.getElementById('input').value);
	if(inputVal !== ''){
		var reg = /^[1-9]\d*$/g;
		if(!reg.test(inputVal)){
			alert('请输入正整数');
			return;
		}
	}
	inputVal *= 1000;
	startRequest();
}
function startRequest(timeVal){
	$.ajax({
		url:'/check/start',
		success:function(res){
			if(res){
				calcTime();
			}else{
				alert('开始返回错误');
			}
		},
		error:function(xhr){
			alert('开始错误');
		}
	})
}
function end(){
	$.ajax({
		url:'/check/stop',
		success:function(res){
			clearInterval(timer);
			timer = null;
		},
		error:function(xhr){
			alert('结束错误');
			console.log(xhr);
		}
	})
}
//计算时间
function calcTime(){
	count = 0;
	timer = setInterval(function() {
        var h = parseInt(count / 1000 / 60 / 60);
        var m = parseInt(count / 1000 / 60) % 60;
        var s = parseInt(count / 1000) % 60;
        var ms = parseInt(count / 10) % 100;
        h = h < 10 ? '0' + h : h;
        m = m < 10 ? '0' + m : m;
        s = s < 10 ? '0' + s : s;
        ms = ms < 10 ? '0' + ms : ms;
        timeTxt.innerHTML = h + '时' + m + '分' + s + '秒' + ms;
        getQuantity();
        getDetail();
        if(inputVal){
        	if(count >= inputVal){
	        	clearInterval(timer);
				timer = null;
	        	end();
	        }
        }
        count += 100;
    }, 100)
}
//获取数量
function getQuantity(){
	$.ajax({
		url:'/check/getCount',
		success:function(res){
			if(qua == res){
				return;
			}
			qua = res;
			quantity.innerHTML = res;
		},
		error:function(xhr){
			alert('获取数量错误');
			console.log(xhr);
		}
	})
}
//获取详情
function getDetail(){
	$.ajax({
		url:'/check/getDetail',
		success:function(res){
			create(res);
		},
		error:function(xhr){
			alert('获取详情错误');
			console.log(xhr);
		}
	})
}
//处理数据
function dataProcessing(data){
	var obj = {};
	var newArr = [];
	var target = null;
	var temp = null;
	for(var i = 0;i < data.length;i++){
		target = data[i];
		
		if(obj[target.bsid]){
			newArr[obj[target.bsid] - 1].len++;//统计数量
			if(target.dir === -1){
				continue;
			}
			if(target.dir == 0){
				temp = newArr[obj[target.bsid] - 1].L;
			}else if(target.dir == 1){
				temp = newArr[obj[target.bsid] - 1].R;
			}
			temp.push({
				bsid:target.bsid,
				tagid:target.tagid,
				time:target.time,
				dis:target.dis,
				dir:target.dir,
			});
		}else{
			obj[target.bsid] = i + 1;
			if(target.dir == 0){
				newArr.push({
					L:[{
						bsid:target.bsid,
						tagid:target.tagid,
						time:target.time,
						dis:target.dis,
						dir:target.dir,
					}],
					R:[],
					bsid:target.bsid,
					len:1
				})
			}else if(target.dir == 1){
				newArr.push({
					L:[],
					R:[{
						bsid:target.bsid,
						tagid:target.tagid,
						time:target.time,
						dis:target.dis,
						dir:target.dir,
					}],
					bsid:target.bsid,
					len:1
				})
			}else{
				newArr.push({
					L:[],
					R:[],
					bsid:target.bsid,
					len:1
				})
			}
		}
	}
	//console.log(newArr);
	return newArr;
}
function create(res){
	var data = dataProcessing(res);
	var html = '';
	var target = null;
	for(var i = 0;i < data.length;i++){
		target = data[i];
		if(!target.bsid){
			continue;
		}
		html += '<div>bsID：' + target.bsid + ' 检卡数量：' + target.len + '</div><div class="item">';
		html += createDom(target.L,'itemL');
		html += createDom(target.R,'itemR');
		html += '</div>';
	}
	detail.innerHTML = html;
}
//创建dom
function createDom(data,classN){
	var html = '';
	var len = data.length;
	if(!len){
		return '';
	}
	html += '<div class="' + classN + '"><div class="bsQua">数量：' + len + '</div><div class="bsInfo">';
	var target = null;
	for(var i = 0;i < len;i++){
		target = data[i];
		html += '<div>';
		html += '<p>tagid：' + target.tagid + ' dis：' + target.dis + '</p>';
		html += '<p> 时间：' + target.time + '</p></div>';
	}
	html += '</div></div>';
	return html;
}