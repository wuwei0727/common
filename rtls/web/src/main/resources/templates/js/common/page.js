var pageIndex = 1;//当前页
var pageCount = 0;//总页数
var pageSize = 20;//请求的条数
//分页功能
//分页入口
function initDataPage(index, count, total,par,noOmitt) {
    var eParent = null;
    if(par){
        eParent = $(par);
    }else{
        eParent = $('.page').eq(0);
    }
    pageCount = count;//赋值
    /*eParent.find(".count").html(count);//总页
    eParent.find(".total").html(total);//总条
    eParent.find(".goPage").val(index);*/
    pageList(index, count, eParent, noOmitt);
    initPageList();
}
//翻页
function jumpPage(obj) {
    var curPage = obj.innerHTML;
    if (isNaN(curPage) || curPage < 1) {
        pageIndex = 1;
    } else if (curPage > pageCount) {
        pageIndex = pageCount;
    }else{
        pageIndex = curPage;
    }
    turnPage();
}
//#region 分页
function pageList(index,count,eParent,noOmitt){
    setDisPage(eParent,index,count);
    var PageS = 0;
    if (index % 5 == 0) {
        PageS = index - 4;
    } else {
        PageS = index - index % 5 + 1;
    }
    var pageList = eParent.find('.pageList');
    var j = count > 5 ? 5 : count;
    var listStr = '';
    var pageNum;
    if(!noOmitt){
        if(index > 5){
            listStr += '<li onclick="jumpPage(this)">1</li><li class="omitt">......</li>';
        }
    }
    
    for (var i = 0; i < j; i++) {
        pageNum = PageS + i;
        if(pageNum == index){
            listStr += '<li class="curPage" onclick="jumpPage(this)">' + pageNum + '</li>';
        }else{
            listStr += '<li onclick="jumpPage(this)">' + pageNum + '</li>';
        }
        if(pageNum + 1 > count){
            break;
        }
    }
    if(!noOmitt){
        if(count - 5 >= index){
            listStr += '<li class="omitt">......</li><li onclick="jumpPage(this)">' + count + '</li>';
        }
    }
    
    pageList.html(listStr);
}
//设置不可点击
function setDisPage(eParent,PageIndex,count) {
    eParent.find('.disabPage').removeClass("disabPage");
    if (PageIndex <= 1) {
        eParent.find(".prev").addClass("disabPage");
    }
    if (PageIndex >= count) {
        eParent.find(".next").addClass("disabPage");
    }
}
//首页
function firstPage(){
    pageIndex = 1;
    turnPage();
}
//尾页
function lastPage(){
    pageIndex = pageCount;
    turnPage();
}
//上一页
function prevPage(ele){
    if(!pageCount){
        tips('没有上一页');
        return;
    }
    if(pageIndex == 1){
        return;
    }
    if(ele){
        pageIndex = +$(ele).next().find('.curPage').text();
    }
    pageIndex--;
    turnPage();
}
//下一页
function nextPage(ele){
    if(!pageCount){
        tips('没有下一页');
        return;
    }
    var $ele = null;
    if(ele){
        //两个翻页在同一页面时
        //区分关闭前一个翻页弹窗
        $ele = $(ele);
        if($ele.hasClass('disabPage')){
            return;
        }
        pageIndex = +$ele.prev().find('.curPage').text();
    }
    if(pageIndex >= pageCount){
        return;
    }
    pageIndex++;
    turnPage();
}
//跳转到指定页
function jumpCurPage(obj,ev){
    var Ev = window.event ||ev;
    if(Ev.keyCode == 13){
        var val = obj.value;
        if(isNaN(val)){
            obj.value = '';
            obj.focus();
            return;
        }
        if(!pageCount){
            tips('跳转失败');
            return;
        }
        val = Math.floor(val);
        if(val > pageCount){
            pageIndex = pageCount;
        }else if(val <= 0){
            pageIndex = 1;
        }else{
            pageIndex = val;
        }
        if(val != pageIndex){
            obj.value = pageIndex;
        }
        turnPage();
    }
};

function initPageList() {
    let had = $("#initPage").length;
    if (had) return;
    let html = `<span class="total" id="initPage">
        每页<input type="text" value="${pageSize}" class="jumpPage" onkeyup="setPageNum(this,event)" />条
    </span>`
    $('.page').prepend(html)
};

function setPageNum(obj, ev) {
    var Ev = window.event || ev;
    if (Ev.keyCode == 13) {
        var val = obj.value;
        if (isNaN(val)) {
            obj.value = '';
            obj.focus();
            return;
        }
        pageSize = val;
        where.pageSize = pageSize;
        init()
    }
}