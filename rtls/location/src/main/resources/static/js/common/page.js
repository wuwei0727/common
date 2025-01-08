var pageIndex = 1;//当前页
var pageCount = 1;//总页数
var pageSize = 20;//请求的条数
//分页功能
//分页入口
function initDataPage(index, count, total,par) {
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
    pageList(index,count,eParent);
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
    search();
}
//#region 分页
function pageList(index,count,eParent){
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
    if(index > 5){
        listStr += '<li onclick="jumpPage(this)">1</li><li class="omitt">......</li>';
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
    if(count - 5 >= index){
        listStr += '<li class="omitt">......</li><li onclick="jumpPage(this)">' + count + '</li>';
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
    search();
}
//尾页
function lastPage(){
    pageIndex = pageCount;
    search();
}
//上一页
function prevPage(ele){
    if(pageIndex == 1){
        return;
    }
    if(ele){
        pageIndex = +$(ele).next().find('.curPage').text();
    }
    pageIndex--;
    search();
}
//下一页
function nextPage(ele){
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
    search();
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
        pageIndex = val > pageCount ? pageCount : val;
        obj.value = pageIndex;
        search();
    }
}