var uid = 75;
var imgUrl, audioUrl, websocketUrl, websocketAllMapUrl;

// websocketUrl = 'ws://' + '192.168.1.95:8083' + '/UWB/websocket/location/';
// websocketAllMapUrl = 'ws://' + '192.168.1.95:8083' + '/UWB/appletsWebSocket/';

websocketUrl = 'ws://' + location.host + '/UWB/websocket/location/';
websocketAllMapUrl = 'ws://' + location.host + '/UWB/appletsWebSocket/';

// websocketUrl = 'ws://' + location.host + '/websocket/location/';
// websocketAllMapUrl = 'ws://' + location.host + '/appletsWebSocket/';


// websocketUrl = 'ws://' + '192.168.1.131:8083' + '/UWB/websocket/location/';
// websocketAllMapUrl = 'ws://' + '192.168.1.131:8083' + '/UWB/appletsWebSocket/';
// audioUrl = imgUrl = url='http://192.168.1.131:8083/UWB/';

audioUrl = imgUrl = url='../';

// var host='http://192.168.1.124:8081/'
// websocketUrl = 'ws://' + host + '/websocket/location/';
// websocketAllMapUrl = 'ws://' + host + '/appletsWebSocket/';
// audioUrl = imgUrl = url=host;

/*************/
$(function () {
	return;
	if (self == top) {
		getUrl();//�Ȼ�ȡ����ĵ�ַ
		var uidDom = document.getElementById('websocket');
		if (uidDom) {
			uid = $.trim(uidDom.innerText);
		}
	} else {
		websocketUrl = parent.websocketUrl;
		audioUrl = imgUrl = parent.imgUrl;
	}
})
//��ȡ��ַ
function getUrl() {
	$.ajax({
		url: url + 'map/url',
		async: false,//ͬ��
		timeout: 5000,//��ʱ
		success: function (res) {
			if (res.code != 200) {
				tips('��ȡ��ַʧ�ܣ���ˢ�����ԣ�');
				return;
			}
			var data = res.data;
			websocketUrl = data.webSocketUrl + 'websocket/location/';
			audioUrl = imgUrl = data.fdfsUrl;
		},
		error: function (err) {
			tips('ϵͳ��æ����ȡ��ַʧ�ܣ�');
		}
	})
}