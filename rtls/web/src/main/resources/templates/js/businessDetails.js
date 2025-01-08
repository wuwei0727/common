var ID;
var mapList = [];
var currentMapId;//初始化的id，不同则重新初始化地图
//定义搜索分析类
var analyser = null;
//选中的模型
var selectedModel = null;
var busData = {};
let photo;
let photo2;

// 裁剪上传图片
var fileName;
var treatUpdateFile;
var treatUpdateFile2;

$(function () {
    calcHeig();
    $(document).on('click', function (e) {
        hideSele();
    })
    loadSeleData();
    var id = getUrlStr('id');
    if (id) {
        ID = id;
        init();
        $('#titleFlag').html('编辑');
    }
})
//处理高度信息
function calcHeig() {
    var win = $(window);
    var fengmap = $('#fengmap');
    fengmap.css({
        height: win.height() - 40 - 52,
        width: win.width() - 40,
    })
}
//加载下拉数据
function loadSeleData() {
    loadFun('park/getShangjiaType', { pageSize: -1 }, '#type', '请选择商家类型');
    $.ajax({
        url: url + 'map/getMap2dSel',
        data: {
            pageSize: -1,
            enable: 1,
            // companyId: companyId
        },
        success: function (res) {
            if (res.code !== 200) {
                tips(res.message);
                return;
            }
            mapList = [];
            var list = res.data;
            var html = '';
            var first = list.find(item => item.id == currentMapId);
            var target = null;
            for (var i = 0; i < list.length; i++) {
                target = list[i];
                mapList.push({
                    id: target.id,
                    type: target.type,
                    fmapID: target.fmapID || '',
                    appName: target.appName || '',
                    mapKey: target.mapKey || '',
                    path: target.themeImg || '',
                    mapName: target.name || ''
                });
                html += '<div onclick="seleMap(this,\'' + list[i].id + '\')">' + list[i].name + '</div>';
            }
            var mapSelect = $('#mapSelect');
            mapSelect.html(html);
            if (first) {
                mapSelect.prev().html(first.name);
                seleMap(null, first.id);
            }
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}
//初始化数据
function init() {
    $.ajax({
        url: url + 'park/getShangjiaByid/' + ID,
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res) {
            if (!res.code) {
                let newWindow = window.open('about:blank');
                newWindow.document.write(res);
                newWindow.focus();
                window.history.go(-1);
            } else if (res.code == 200) {
                var data = res.data;
                busData = data;
                setData(data, '.main');
                if (ID) {
                    photo = data.photo;
                    photo2 = data.photo2;
                }
                var file = $('[name="file"]').val();
                currentMapId = $('[name="map"]').data('val');
                if (data.map) {
                    $('[name="map"]').eq(0).addClass('batchTxtChange');
                }
                if (data.type) {
                    $('[name="type"]').eq(0).addClass('batchTxtChange');
                }
            } else {
                tips(res.message);
            }
        },
        error: function (jqXHR) {
            resError(jqXHR);
        }
    })
}
//保存
function save() {
    var sendData = getDataImg('.main');
    var nameReg = /^[\u4e00-\u9fa5a-zA-Z0-9]+$/;
    var mapId = $('[name="map"]').data('val');
    var x = $('[name="x"]').val();
    var y = $('[name="y"]').val();

    // 修改file为裁剪后的文件
    if (treatUpdateFile) {
        sendData.set('file', treatUpdateFile)
    };
    if (treatUpdateFile2) {
        sendData.set('file2', treatUpdateFile2)
    };
    var file = sendData.get('file');
    console.log('aaaaaa', file)
    if (file) {
        photo = '';
        photo2 = ''
    }
    if (ID) {
        sendData.append('photo', photo);
        sendData.append('photo2', photo2);
    }
    // if(!sendData){
    //     return;
    // }
    if (!sendData.get('name')) {
        // if (!nameReg.test(sendData.get('name'))) {
        tips('请输入名称！！！');
        return;
    }
    if (!mapId) {
        tips("请选择关联地图");
        return;
    }
    if (!x || !y) {
        tips("请在地图中选点");
        return;
    }
    // if (!sendData.get('content')) {
    //     tips('请输入服务范围');
    //     return;
    // }
    // if (!sendData.get('time')) {
    //     tips('请输入营业时间');
    //     return;
    // }
    // if (!sendData.get('owner')) {
    //     tips('请输入负责人');
    //     return;
    // }
    // var reg_phone = /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/;
    // if (!sendData.get('phone')) {
    //     // if (!reg_phone.test(sendData.get('phone'))) {
    //     tips('请输入手机号码');
    //     return;
    // }
    // if (!sendData.get('address')) {
    //     tips('请输入地址');
    //     return;
    // }
    var path = url;
    if (ID) {
        path += 'park/updateShangjia';
        sendData.append('id', ID);
    } else {
        path += 'park/addShangjia';
    };
    $.ajax({
        url: path,
        data: sendData,
        type: 'post',
        processData: false,// jQuery不要去处理发送的数据
        contentType: false,// jQuery不要去设置Content-Type请求头
        beforeSend: function () {
            loading();
        },
        complete: function () {
            removeLoad();
        },
        success: function (res) {
            if (res.code == 200) {
                tips(res.message, function () {
                    if (ID) {
                        init();
                    } else {
                        location.reload();
                    }
                }, 1000);
            } else {
                tips(res.message);
            }
        },
        error: function (jqXHR) {
            var status = jqXHR.status;
            if (status == 401) {
                document.write(jqXHR.responseText)
            }
            resError(jqXHR);
        }
    })
}
//选点显示地图
function selectPoint() {
    var mapId = $('[name="map"]').data('val');
    if (!mapId) {
        tips('请选择关联地图');
        return;
    }
    $('#mapPop').show();
    $('#mask').show();
    var target = null;
    var floor = +$('[name="floor"]').val();
    if (currentMapId == mapId) {
        target = contrastReturn(mapList, 'id', mapId);
        openMap({
            fKey: target.mapKey,
            fName: target.appName,
            fId: target.fmapID,
            focusFloor: floor,
            path: target.path,
            //初始指北针的偏移量
            compassOffset: [20, 12],
            //指北针大小默认配置
            compassSize: 48,
            mapId
        }, function () {
            analyser = new fengmap.FMSearchAnalyser(fMap);
            addLayer({
                x: +busData.x,
                y: +busData.y,
                num: busData.name,
                focusFloor: Number(floor)
            });
            if (busData.x && busData.y) {
                fMap.moveTo({
                    x: +busData.x,
                    y: +busData.y,
                    groupID: +floor,
                })
            }
            var fid = $('[name="fid"]').val();
            var target = null;
            if (fid) {
                target = findModel({ FID: fid });
                if (target) {
                    target.selected = true;
                    selectedModel = target;
                }
            }
        });
    } else if (currentMapId != mapId) {
        target = contrastReturn(mapList, 'id', mapId);
        openMap({
            fKey: target.mapKey,
            fName: target.appName,
            fId: target.fmapID,
            path: target.path,
            //初始指北针的偏移量
            compassOffset: [20, 12],
            //指北针大小默认配置
            compassSize: 48,
            mapId
        }, function () {
            analyser = new fengmap.FMSearchAnalyser(fMap);
        });
    }
}
//地图的回调
function mapClickFn(info, target) {
    if (info.nodeType !== fengmap.FMNodeType.MODEL) {
        tips('请选择模型');
        return;
    } else {
        tips('已选择地点');
    }
    console.log(info)
    $('[name="fid"]').val(info.fid);
    $('[name="x"]').val(info.cx);
    $('[name="y"]').val(info.cy);
    $('[name="z"]').val(info.z);
    $('[name="floor"]').val(info.floor);
    if (selectedModel) {
        selectedModel.selected = false;
    }
    //染色
    target.selected = true;
    selectedModel = target;
}

/* 确定 */
function mapConfirm() {
    hidePop('#mapPop')
};

/* 取消 */
function mapCancel(){
    hidePop('#mapPop');
    tips('已取消选择的地点');
    $('[name="x"]').val('');
    $('[name="y"]').val('');
    $('[name="z"]').val('');
    $('[name="floor"]').val('');
    $('[name="fid"]').val('');
};

function cancelButton(that) {
    document.getElementById("file").value = "";
    var img = document.getElementById("cancelUploadImg");
    photo = '';
    img.src = '';

    document.getElementById("file2").value = "";
    var img2 = document.getElementById("cancelUploadImg2");
    photo2 = '';
    img2.src = ''
}

//地图切换
function seleMap(that, id) {
    seleBatch(that, id, function () {
        if (currentMapId != id) {
            $('[name="fid"]').val('');
            $('[name="x"]').val('');
            $('[name="y"]').val('');
            $('[name="z"]').val('');
            $('[name="floor"]').val('1');
            $('[name="map"]').val(id);
        } else {
            $('[name="fid"]').val(busData.fid);
            $('[name="x"]').val(busData.x);
            $('[name="y"]').val(busData.y);
            $('[name="z"]').val(busData.z);
            $('[name="floor"]').val(busData.floor);
            $('[name="map"]').val(id);
        }
    });
}

// 上传图片时裁剪
//弹出框水平垂直居中  
(window.onresize = function () {
    var win_height = $(window).height();
    var win_width = $(window).width();
    if (win_width <= 768) {
        $(".tailoring-content").css(
            {
                "top": (win_height - $(".tailoring-content")
                    .outerHeight()) / 2,
                "left": 0
            });
    } else {
        $(".tailoring-content").css(
            {
                "top": (win_height - $(".tailoring-content")
                    .outerHeight()) / 2,
                "left": (win_width - $(".tailoring-content")
                    .outerWidth()) / 2
            });
    }
})();
// 选择文件触发事件  
function selectImg(file) {
    console.log('file', file.files);

    //文件为空，返回  
    if (!file.files || !file.files[0]) {
        return;
    };
    var files = file.files[0];
    if (!files.type.match('image/jpeg') && !files.type.match('image/png')) {
        tips('只能上传JPG和PNG格式的图片!');
        return;
    }
    // 获取图片名
    fileName = file.files[0].name || new Date().getTime() + '.jpeg'
    $(".tailoring-container").toggle();
    var reader = new FileReader();
    reader.onload = function (evt) {
        var replaceSrc = evt.target.result;
        // 更换cropper的图片  
        $('#tailoringImg').cropper('replace', replaceSrc, false);// 默认false，适应高度，不失真  
    }
    reader.readAsDataURL(file.files[0]);

};
// cropper图片裁剪  
$('#tailoringImg').cropper({
    // viewMode: 0, // 无限制
    // dragMode: 'move', // 图片可移动
    // aspectRatio: 1 / 1,// 默认比例
    // preview: '.previewImg',// 预览视图
    // guides: false, // 裁剪框的虚线(九宫格)
    // autoCropArea: 1, // 0-1之间的数值，定义自动剪裁区域的大小，默认0.8
    // movable: true, // 是否允许移动图片
    // zoomable: true, // 是否允许缩放图片大小
    // rotatable: false, // 是否允许旋转图片
    // cropBoxMovable: false, // 是否可以拖拽裁剪框 默认true
    // cropBoxResizable: false,//是否通过拖动来调整剪裁框的大小，默认为true

    aspectRatio: 1 / 1,// 默认比例  
    preview: '.previewImg',// 预览视图  
    guides: false, // 裁剪框的虚线(九宫格)  
    autoCropArea: 0.5, // 0-1之间的数值，定义自动剪裁区域的大小，默认0.8  
    movable: false, // 是否允许移动图片  
    dragCrop: true, // 是否允许移除当前的剪裁框，并通过拖动来新建一个剪裁框区域  
    movable: true, // 是否允许移动剪裁框  
    resizable: false, // 是否允许改变裁剪框的大小  
    zoomable: false, // 是否允许缩放图片大小  
    mouseWheelZoom: false, // 是否允许通过鼠标滚轮来缩放图片  
    touchDragZoom: true, // 是否允许通过触摸移动来缩放图片  
    rotatable: true, // 是否允许旋转图片  
    crop: function (e) {
        // 输出结果数据裁剪图像
    }
});
// 确定按钮点击事件  
$("#sureCut").on("click", async function () {
    if ($("#tailoringImg").attr("src") == null) {
        return false;
    } else {
        var cas = $('#tailoringImg').cropper('getCroppedCanvas');// 获取被裁剪后的canvas  
        var base64 = cas.toDataURL('image/jpeg'); // 转换为base64  

        $("#cancelUploadImg").prop("src", base64);// 显示图片 
        treatUpdateFile = base64toFile(base64, fileName);
        console.log('treatUpdateFile', treatUpdateFile);

        /* 生成圆形 */
        let canvas = getRoundedCanvas();
        var dataurl = canvas.toDataURL("image/png");

        var canvas2 = await compositeImage(dataurl);
        var dataurl2 = canvas2.toDataURL("image/png");

        treatUpdateFile2 = base64toFile(dataurl2, 'circle' + fileName);
        console.log('treatUpdateFile2', treatUpdateFile2);

        $("#cancelUploadImg2").prop("src", dataurl2);// 显示图片 

        closeTailor();// 关闭裁剪框
    }
});

/* 生成圆形 */
function getRoundedCanvas() {
    var crop = $('#tailoringImg').data("cropper"); //获取crop对象
    var sourceCanvas = crop.getCroppedCanvas();
    var canvas = document.createElement('canvas');
    var context = canvas.getContext('2d');
    var width = sourceCanvas.width;
    var height = sourceCanvas.height;
    canvas.width = width;
    canvas.height = height;
    context.imageSmoothingEnabled = true;
    context.fillStyle = '#fff';
    context.fillRect(0, 0, width, height);
    context.drawImage(sourceCanvas, 0, 0, width, height);
    context.globalCompositeOperation = 'destination-in';
    context.beginPath();
    context.arc(width / 2, height / 2, Math.min(width, height) / 2, 0, 2 * Math.PI, true);
    context.fill();
    return canvas;
};

/* 合成图片 */
async function compositeImage(base2) {
    var canvas = document.createElement('canvas');
    var context = canvas.getContext('2d');
    var width = 250;
    var height = 250;
    canvas.width = width;
    canvas.height = height;
    context.imageSmoothingEnabled = true;

    var proResult = await new Promise((resolve, reject) => {
        var myImage1 = new Image();
        myImage1.src = '../image/common/merchantframe.png'; //背景图片
        myImage1.crossOrigin = 'Anonymous';
        myImage1.onload = () => {
            context.drawImage(myImage1, 0, 0, 250, 250);
            var myImage2 = new Image();
            myImage2.src = base2;
            myImage2.crossOrigin = 'Anonymous';
            myImage2.onload = () => {
                context.drawImage(myImage2, 32, 15, 186, 186);
                context.fill();
                resolve(canvas);
            }
        }
    })

    return canvas;
};

// 关闭裁剪框  
function closeTailor() {
    $(".tailoring-container").toggle();
    var _file = document.getElementById("file");
    _file.outerHTML = _file.outerHTML
}

// function base64toFile(base64Str, fileName) {
//     const formData = new FormData();
//     formData.append('croppingFile', dataUrlToBlob(base64Str), fileName);
//     return formData.get('croppingFile');
// };
// function dataUrlToBlob(dataUri) {
//     let arr = dataUri.split(',');
//     let mime = arr[0].match(/:(.*?);/)[1];
//     let bstr = atob(arr[1]);
//     let arrBuff = new ArrayBuffer(bstr.length);
//     let un8Arr = new Uint8Array(arrBuff);
//     for (let i = 0; i < bstr.length; i++) {
//         un8Arr = bstr[i].charCodeAt(i);
//     }
//     return new Blob([arrBuff], { type: mime });
// }
// base64转文件
function base64toFile(base64Str, fileName) {
    let arr = base64Str.split(',');
    let mime = arr[0].match(/:(.*?);/)[1];
    let bstr = atob(arr[1]);
    let n = bstr.length;
    let u8arr = new Uint8Array(n);
    while (n--) {
        u8arr[n] = bstr.charCodeAt(n);
    }
    return new File([u8arr], fileName, { type: mime })
};
