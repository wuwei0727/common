package com.tgy.rtls.data.tool;


public class Constant {

    public static final String CURRENT_USER = "current_user";
    public static final String USER_SESSION_ID = "uid";
    public static final String USER_WXSESSION_ID = "wx_uid";
    public static final String USER_WXJSESSION_ID = "JsessionId";
    public static final String USER_LOGIN_TIME = "login_time";
    public static final int USER_ONLINE_OFF = 0;
    public static final int USER_ONLINE_ON = 1;
    public static String UPLOAD_FILE_PAHT = "";

    public static final String KEY_SUCCESS = "success";
    public static final String KEY_ERROR = "error";
    public static final String KEY_ERROR_CODE = "errorcode";
    public static final String KEY_DATA = "data";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TOTAL = "total";
    public static final String KEY_SAFEBELT = "safebelt";
    public static final String KEY_STORAGEAREA = "storagearea";
    public static final String KEY_WORKINGAREA = "workingarea";
    public static final String KEY_SESSION_ID = "sid";

    public static final int MESSAGE_TYPE_WORKINGAREA_ENTER = 0;
    public static final int MESSAGE_TYPE_WORKINGAREA_LEAVE = 1;
    public static final int MESSAGE_TYPE_RETURN_SUCCESS = 2;
    public static final int MESSAGE_TYPE_RETURN_FAILURE = 3;
    public static final int MESSAGE_TYPE_SHUTDOWN_RESPONSE = 4;
    public static final int MESSAGE_TYPE_BIND = 5;
    public static final int MESSAGE_TYPE_FIXPRESSURE = 6;


    public static final int MESSAGE_ANSWER_NO = 0;
    public static final int MESSAGE_ANSWER_YES = 1;
    public static final int MESSAGE_ANSWER_RETRY = 2;
    public static final int MESSAGE_ANSWER_TIMEOUT = 3;

    public static final int SAFEBELT_BIND_NO = 0;
    public static final int SAFEBELT_TYPE = 11;
    public static final int SAFEBELT_BIND_YES = 1;
    public static final int SAFEBELT_BIND_WAIT_ANSWER = 2;

    //<-- 微信小程序常量
    public static final String KEY_APP_ID = "appid";
    public static final String KEY_APP_SECRET = "secret";
    public static final String KEY_APP_GRANT_JS_CODE = "js_code";
    public static final String KEY_APP_GRANT_TYPE = "grant_type";

    //public static final String APP_ID = "wx0914d7188b9f3a98";
    //public static final String APP_SECRET = "605271959271e2bc2efade80edaef402";

    public static final String APP_ID = "wxf0f25ad3fc36365e"; //tuguiyao
    public static final String APP_SECRET="79c8b7c3e5cd646c08444bcdfd0b65c8"; //tuguiyao

    public static final String ServiceNumber_APP_ID = "wx4445241794495a18"; //tuguiyao
    public static final String ServiceNumber_APP_SECRET="f34ca55c79c96a65d64979f9202f80ec"; //tuguiyao


    public static final String LOCATE_APP_ID = "wxf7e7772c32ba7d00";
    public static final String LOCATE_APP_SECRET="b98a1fcb592b5b3672b220296d4c55e2";

    //public static final String APP_ID = "wxc4a87c5a918ff269"; //sinopec
    //public static final String APP_SECRET="473b740a891090bae74db2632d063c05";//sinopec

    public static final String APP_GRANT_TYPE = "authorization_code";
    //微信小程序常量 -->

    public static final int ERROR_CODE_NO_LOGIN = 1001;
    public static final int ERROR_CODE_NO_BIND = 1002;
    public static final int ERROR_CODE_UNBIND_NOT_IN_POLY = 1003;


    public static final int SAFEBELT_WARNING_F = -1;//SOS
    public static final int SAFEBELT_WARNING_G = 0;//坠落
    public static final int SAFEBELT_WARNING_A = 1;//在作业区域未穿戴安全带
    public static final int SAFEBELT_WARNING_B = 2;//在作业区域未系挂安全带
    public static final int SAFEBELT_WARNING_C = 3;//电量不足，少于20%电量
    public static final int SAFEBELT_WARNING_D = 4;//GPS无信号
    public static final int SAFEBELT_WARNING_E = 5;//设备无GPRS信号
    public static final int SAFEBELT_WARNING_H = 8;//高度过大

    public static final int DefaultVolume = 12;//默认设备音量
    public static final int MiniVolume = 8;//默认设备音量
    //public static final float MiddleRssi_2=62.7f;//两米左右的rssi  最大功率
    public static final float MiddleRssi_2=80.6f;//两米左右的rssi 最小功率
    public static final int MiddleRssi_15=42;//两米左右的rssi

    public static final int Rssi_2dis=5;//两米左右的rssi
    public static final int Rssi_15dis=10;//两米左右的rssi

    public static final int decive=11;//两米左右的rssi
    public static final int GPS_ACCURACY=20;//两米左右的rssi

    public static float OVERRANGE=2;

    //三个等级的报警频率
    public static int MIN_INTERVAL=5;
    public static int MIDDLE_INTERVAL=10;
    public static int MAX_INTERVAL=20;
    //三个等级的时间长度
    public static int MIN_TIME=10000;
    public static int MIDDLE_TIME=30000;
    public static int MAX_TIME=40000;

    //信标部署
    public static final String KEY_XbAPP_ID = "appid";
    public static final String XbAPP_ID = "wxafdbd64d0ab1382f";
    public static final String KEY_XbAPP_SECRET = "secret";
    public static final String XbAPP_SECRET="fcf58d357211ee9b2df4911ff30ba16f";
    public static final String KEY_XbAPP_GRANT_JS_CODE = "js_code";
    public static final String KEY_XbAPP_GRANT_TYPE = "grant_type";
    public static final String XbAPP_GRANT_TYPE = "authorization_code";





}
