
//转换为像素位置 + 缩放偏移
function changePos(x,y){
    var res = {};
    res.x = (bgWid * (x + originPos.x) / actualWid) * scale + offset.x;
    res.y = (bgHei - bgHei * (y + originPos.y) / actualHei) * scale + offset.y;//加上原点偏移
    return res;
}
//鼠标按移事件--计算坐标
function calcPos(){
    var dom = null;
    var pos = null;
    var offset = null;
    $.each(domBag,function(i,item){
        dom = item.dom;
        offset = item.offset;
        pos = changePos(offset.x,offset.y);
        dom.style.left = pos.x - 10 + 'px';
        dom.style.top = pos.y - 24 + 'px';
    })
}
//重置canvas
function resetCanvas(){
    ctx.setTransform(1, 0, 0, 1, 0, 0);
}

$(function() {
    trackTransforms(ctx);
    function startDraw() {
        var p1 = ctx.transformedPoint(0, 0);
        var p2 = ctx.transformedPoint(canvas.width, canvas.height);
        ctx.clearRect(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
        ctx.save();
        ctx.setTransform(1, 0, 0, 1, 0, 0);
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        ctx.restore();
        redraw();
    }
    var lastX = canvas.width / 2,
        lastY = canvas.height / 2;

    var dragStart, dragged;

    canvas.addEventListener('mousedown', function(evt) {
        document.body.style.mozUserSelect = document.body.style.webkitUserSelect = document.body.style.userSelect = 'none';
        lastX = evt.offsetX || (evt.pageX - canvas.offsetLeft);
        lastY = evt.offsetY || (evt.pageY - canvas.offsetTop);
        dragStart = ctx.transformedPoint(lastX, lastY);
        dragged = false;
        evt.preventDefault(); // 阻止默认行为
        evt.stopPropagation(); // 阻止事件冒泡
        return false;
    }, false);

    canvas.addEventListener('mousemove', function(evt) {
        //放大的点
        lastX = evt.offsetX || (evt.pageX - canvas.offsetLeft);
        lastY = evt.offsetY || (evt.pageY - canvas.offsetTop);
        dragged || (dragged = true);
        if (dragStart) {
            var pt = ctx.transformedPoint(lastX, lastY);
            ctx.translate(pt.x - dragStart.x, pt.y - dragStart.y);
            offset = ctx.transformedPoint2(0,0);
            calcPos();
            startDraw();
        }
    }, false);

    canvas.addEventListener('mouseup', function(evt) {
        dragStart && (dragStart = null);
        if(!dragged){
            return;
        }
        dragged = false;
        offset = ctx.transformedPoint2(0,0);
        evt.preventDefault(); // 阻止默认行为
        evt.stopPropagation(); // 阻止事件冒泡
        return false;
    }, false);

    var scaleFactor = 1.1;

    var zoom = function(clicks) {
        var pt = ctx.transformedPoint(lastX, lastY);
        ctx.translate(pt.x, pt.y);
        var factor = Math.pow(scaleFactor, clicks);
        scale *= factor;
        ctx.scale(factor, factor);
        ctx.translate(-pt.x, -pt.y);
        offset = ctx.transformedPoint2(0,0);
        calcPos();
        startDraw();
    }

    var handleScroll = function(evt) {
        var delta = evt.wheelDelta ? evt.wheelDelta / 40 : evt.detail ? -evt.detail : 0;
        if (delta){
            zoom(delta);
        }
        return evt.preventDefault() && false;
    };
    //判断浏览器是否兼容滚轮事件（支持时为Null不支持时为undefined）
    if(document.onmousewheel!==undefined){
        //对事件监听进行兼容(此处针对滚轮事件)
        canvas.addEventListener("mousewheel",handleScroll,false);
    }else {
      //针对火狐（监听事件的兼容）
        canvas.addEventListener('DOMMouseScroll',handleScroll,false);
    }
});
// Adds ctx.getTransform() - returns an SVGMatrix
// Adds ctx.transformedPoint(x,y) - returns an SVGPoint
function trackTransforms(ctx) {
    var svg = document.createElementNS("http://www.w3.org/2000/svg", 'svg');
    var xform = svg.createSVGMatrix();
    ctx.getTransform = function() {
        return xform;
    };

    var savedTransforms = [];
    var save = ctx.save;
    ctx.save = function() {
        savedTransforms.push(xform.translate(0, 0));
        return save.call(ctx);
    };

    var restore = ctx.restore;
    ctx.restore = function() {
        xform = savedTransforms.pop();
        return restore.call(ctx);
    };

    var scale = ctx.scale;
    ctx.scale = function(sx, sy) {
        xform = xform.scaleNonUniform(sx, sy);
        return scale.call(ctx, sx, sy);
    };

    var rotate = ctx.rotate;
    ctx.rotate = function(radians) {
        xform = xform.rotate(radians * 180 / Math.PI);
        return rotate.call(ctx, radians);
    };

    var translate = ctx.translate;
    ctx.translate = function(dx, dy) {
        xform = xform.translate(dx, dy);
        return translate.call(ctx, dx, dy);
    };

    var transform = ctx.transform;
    ctx.transform = function(a, b, c, d, e, f) {
        var m2 = svg.createSVGMatrix();
        m2.a = a;
        m2.b = b;
        m2.c = c;
        m2.d = d;
        m2.e = e;
        m2.f = f;
        xform = xform.multiply(m2);
        return transform.call(ctx, a, b, c, d, e, f);
    };

    var setTransform = ctx.setTransform;
    ctx.setTransform = function(a, b, c, d, e, f) {
        xform.a = a;
        xform.b = b;
        xform.c = c;
        xform.d = d;
        xform.e = e;
        xform.f = f;
        return setTransform.call(ctx, a, b, c, d, e, f);
    };

    var pt = svg.createSVGPoint();
    ctx.transformedPoint = function(x, y) {
        pt.x = x;
        pt.y = y;
        //console.log(pt.matrixTransform(xform.inverse()))
        return pt.matrixTransform(xform.inverse())
    }
    var pt2 = svg.createSVGPoint();
        //当前坐标系中的的xy还原到原坐标系坐标值
    ctx.transformedPoint2 = function (x, y) {
        pt2.x = x; pt2.y = y;
        return pt2.matrixTransform(xform);
    }
}
