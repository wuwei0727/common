// 搜索功能
function search(type) {
	loadData();
}

// 初始化时间
function initDateTime() {
	let now = new Date();  // 当前时间
	let start = new Date(now.getTime() - 12 * 60 * 1000);  // 12分钟前的时间

	// 格式化时间
	let nowStr = formatDateTime(now);
	let startStr = formatDateTime(start);

	// 设置输入框的值
	$('#start').val(startStr);
	$('#end').val(nowStr);
}

// 格式化日期时间
function formatDateTime(date) {
	let year = date.getFullYear();
	let month = (date.getMonth() + 1).toString().padStart(2, '0');
	let day = date.getDate().toString().padStart(2, '0');
	let hours = date.getHours().toString().padStart(2, '0');
	let minutes = date.getMinutes().toString().padStart(2, '0');
	let seconds = date.getSeconds().toString().padStart(2, '0');
	return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

// 页面加载完成后执行
$(function() {
	initDateTime();  // 初始化时间
	loadData();     // 加载数据
});

// 加载数据
function loadData() {
	let params = {
		num: $('input[name="num"]').val(),
		startTime: $('input[name="start"]').val(),
		endTime: $('input[name="end"]').val()
	};

	$.ajax({
		url: '/kk/getMag',
		type: 'GET',
		data: params,
		success: function(res) {
			if (res.code === 200) {
				renderTable(res.data);
			} else {
				alert(res.message || '获取数据失败');
			}
		},
		error: function(err) {
			console.error('请求失败：', err);
			alert('网络错误，请稍后重试');
		}
	});
}

// 渲染表格
function renderTable(data) {
	if (!Array.isArray(data)) {
		console.error('数据格式错误');
		return;
	}

	let html = '';
	data.forEach((item, index) => {
		html += `<tr>
            <td>${index + 1}</td>
            <td>${item.time || ''}</td>
            <td>${item.num || ''}</td>
            <td>${item.x || ''}</td>
            <td>${item.y || ''}</td>
            <td>${item.z || ''}</td>
            <td>${item.x_fix || ''}</td>
            <td>${item.y_fix || ''}</td>
            <td>${item.z_fix || ''}</td>
            <td>${item.x_diff || ''}</td>
            <td>${item.y_diff || ''}</td>
            <td>${item.z_diff || ''}</td>
            <td>${item.occupy_x || ''}</td>
            <td>${item.occupy_y || ''}</td>
            <td>${item.occupy_z || ''}</td>
            <td>${item.empty_x || ''}</td>
            <td>${item.empty_y || ''}</td>
            <td>${item.empty_z || ''}</td>
            <td>${item.state === 0 ? '空闲' : '占用'}</td>
        </tr>`;
	});
	$('#tab tbody').html(html);
}