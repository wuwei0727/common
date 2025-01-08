package com.tgy.rtls.location.Utils;

import java.util.Arrays;
import java.util.List;

public class Constant {


    public static final String CURRENT_USER = "current_user";
    public static final String USER_SESSION_ID = "uid";
    public static final short USER_ONLINE_OFF = 0;
    public static final short USER_ONLINE_ON = 1;
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
    public static final String APP_ID = "wx312f384677c764d7";
    public static final String APP_SECRET="e4626e055a12f04833e40d13e2257f92";
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

    public static final int DefaultVolume = 50;//默认设备音量
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

    public final static  String PING="80001737";//
    public final static  String HEART="8000173B";//
    public final static  String DONW_LINK="00001750";//
    public final static  String DONW_ACK="00001751";//
    public final static  String TAGDATA_UPLINK="00001752";//
    public final static  String RANGE_REQ="00001753";//
    public final static  String RANGE_RES="00001754";//
    public final static  String BS_BEEP_REQ="00005018";//
    public final static  String BS_BEEP_RES="00005019";//

    public final static  String BS_POWER_REQ="00001770";//
    public final static  String BS_POWER_RES="00001771";//
    public final static  String COAL_BEACON="00002001";//
    public final static  String COAL_TAGDATA="00002002";//
    public final static  String COAL_C3="00002006";//
    public final static  String COAL_FILEPUSH_REQ="00002006";//文件推送请求，音频和固件等文件
    public final static  String COAL_FILEPUSH_REQ_PROCESS="0000200A";//文件推送进度
    public final static  String COAL_FILEPUSH_END="00002007";//文件推送结束

    public final static  String COAL_FILEUPLINK_REQ="00002008";//上行，文件上传请求,比如音频
/*    public final static  String COAL_FILEUPLINK_ACK="00002011";//下行，文件确认包含文件信息
    public final static  String COAL_FILEUPLINK_PROCESS="00002009";//上行，文件进度*/
    public final static  String COAL_FILEUPLINK_END="00002010";//上行，文件上传结束


    public final static  String COAL_HEART="00002012";//上行，基站心跳
    public final static  String COAL_BSERROR_QUERY="00002013";//下行，查询基站异常
    public final static  String COAL_BSERROR_UPLINK="00002014";//上行，基站异常上报
    public final static  String COAL_BACKUPFILE_UPLINK="00002015";//上行
    public final static  String COAL_BS_WARNING_REQ="00002016";//下行
    public final static  String COAL_BS_WARNING_RES="00002017";//下行
    public final static  String COAL_SINGLE_RANGE="00002022";//下行
    public final static  String COAL_TAG_SENSOR="00002002";//标签传感器数据
    public final static  String COAL_BSRANGE_RES="00001754";//测距结果应答
    public final static  String COAL_BSIMG_RES="00002021";//上行，文件上传结束
    public final static  String COAL_BSTEXT_RES="00002019";//上行，文件上传结束
    public final static  String COAL_BSOLD_RES="00002026";//上行，文件上传结束
    public final static  String COAL_BSERRORCODETEST_RES="00002028";//上行，文件上传结束
    public final static  String COAL_BSLOCATION_RES="00002030";//上行，文件上传结束
    public final static  String CMD_BS_LOCPARA_RES="00002032";//上行，文件上传结束
    public final static  String CMD_BS_UPDATE_RES="00002034";//上行文件传输成功
    public final static  String CMD_BS_VERSIONINFRES="00002036";//上行，基站固件信息
    public final static  String CMD_4BS_2D="00002037";//铁科院四基站二维定位
    public final static  String CMD_BS_NETRES= "00002038";//上行，网络配置响应
    public final static  String CMD_8BS_2D="00002039";//铁科院四基站二维定位
    public final static  String CMD_BS_TAGDIS_RES= "00002024";//上行，网络配置响应
    public final static  String CMD_COAL="00002040";//煤矿基站beacon信息
    public final static  String CMD_BSWIFI="0000203A";//煤矿基站beacon信息
    public final static  String CMD_PARK= "00000010";//
    public final static  String CMD_LORAHEART= "07000010";//
    public final static  String CMD_LORA_VERSION= "03000010";//
    public final static  String CMD_INFRARED_VERSION= "02000010";//
    public final static  String CMD_GATESTATE= "09000010";//
    public final static  String CMD_INFRAREDSTATE= "08000010";//
    public final static  String CMD_NED= "1000000A";//地锁数据
    public final static String[] GW = {Constant.CMD_PARK,
            Constant.CMD_LORAHEART,
            Constant.CMD_LORA_VERSION,
            Constant.CMD_INFRARED_VERSION,
            Constant.CMD_GATESTATE,
            Constant.CMD_INFRAREDSTATE,
            Constant.CMD_NED,
    };
    public static List<String> list_GW = Arrays.asList(GW);


    //RS_485数据,主要是心跳数据
    public final static  String CMD_4G_485= "07000020";//4G_485心跳
    public final static String[] RS485 = {Constant.CMD_4G_485};
    public final static List<String> list_485 = Arrays.asList(RS485);

    //地锁数据，心跳数据和地锁状态数据
    public final static  String CMD_NED_HEART= "1000000B";//地锁心跳数据
    public final static  String CMD_NED_DATA= "1000000C";//地锁状态数据
    public final static  String CMD_NED_DEC_DATA= "1000000D";//地锁传输检测器数据
    public final static String[] CAT1 = {Constant.CMD_NED_HEART,
            Constant.CMD_NED_DATA,
            Constant.CMD_NED_DEC_DATA,};
    public final static List<String> list_CAT1 = Arrays.asList(CAT1);

    /*
        下行配置基站参数
     */
    public final static  byte[] CMD_BS_TEXT= {0x18,0x20,0x00,0x00};//下行
    public final static  byte[] CMD_BS_IMG= {0x20,0x20,0x00,0x00};//下行
    public final static  byte[] CMD_BS_POWER= {0x70,0x17,0x00,0x00};//下行
    public final static  byte[] CMD_BS_BEEP= {0x18,0x50,0x00,0x00};//下行
    public final static  byte[] CMD_BS_GENERAL= {0x3f,0x17,0x00,0x00};//下行
    public final static  byte[] CMD_BS_SENDFILE= {0x06,0x20,0x00,0x00};//下行
    public final static  byte[] CMD_BS_WARNING= {0x16,0x20,0x00,0x00};//下行
    public final static  byte[] CMD_COAL_FILEUPLINK_ACK= {0x09,0x20,0x00,0x00};//下行,文件上传请求
    public final static  byte[] CMD_COAL_FILEUPLINKFINISH_ACK= {0x11,0x20,0x00,0x00};//下行，文件接收完成确认
    public final static  byte[] CMD_BS_SENDHEART= {0x12,0x20,0x00,0x00};//下行
    public final static  byte[] CMD_BS_RANGE= {0x53,0x17,0x00,0x00};//下行
    public final static  byte[] CMD_BS_TAGDIS= {0x23,0x20,0x00,0x00};//下行
    public final static  byte[] CMD_BS_OLDTIMEDIS= {0x25,0x20,0x00,0x00};//下行
    public final static  byte[] CMD_BS_RANDOMKEY= {0x27,0x20,0x00,0x00};//下行
    public final static  byte[] CMD_BS_LOCATION= {0x29,0x20,0x00,0x00};//下行
    public final static  byte[] CMD_BS_LOCPARA= {0x31,0x20,0x00,0x00};//下行
    public final static  byte[] CMD_BS_UPDATE= {0x33,0x20,0x00,0x00};//下行
    public final static  byte[] CMD_BS_VERSIONINF= {0x35,0x20,0x00,0x00};//下行
    public final static  byte[] CMD_BS_NET= {0x37,0x20,0x00,0x00};//下行
    public final static  byte[] CMD_BS_WIFINET= {0x3a,0x20,0x00,0x00};//下行配置基站wifi地址

    public final static  byte[] CMD_LORA_NET= {0x10,0x00,0x00,0x06};//

    public final static  byte[] CMD_INFRARED_PARA= {0x10,0x00,0x00,0x01};//
    public final static  byte[] CMD_LORA_UPDATE= {0x10,0x00,0x00,0x04};//
    public final static  byte[] CMD_LORA_REBOOT= {0x11,0x11,0x11,0x11};//

    public final static  byte[] CMD_INFRARED_UPDATE= {0x10,0x00,0x00,0x05};//下



    /*
        下行配置标签参数
     */
    public final static  byte[] CMD_TAG_PARA= {0x50,0x17,0x00,0x00};//下行
    //public final static  byte[] CMD_BS_IMG= {0x20,0x20,0x00,0x00};//下行

    /*
    标签上行数据
     */
    public final static  byte[] CMD_TAG_SENSOR= {0x18,0x20,0x00,0x00};//下行

/*
车位检测
 */
public final static  byte[] CMD_PARK_STATE= {0x10,0x00,0x00,0x00};//车位检测器状态上报
    /*
433Mhz网关数据
 */
    public final static  byte[] CMD_GATEWAY_STATE= {0x07,0x00,0x00,0x10};//网关状态上报

    public final static  byte[] CMD_NED_CONTROL= {0x0a,0x00,0x00,0x10};//控制地锁


    public final static  byte[] CMD_CAT1_STATE= {0x0b,0x00,0x00,0x10};//cat1心跳

    public final static  byte[] CMD_CAT1_CMD= {0x0c,0x00,0x00,0x10};//cat1控制

}
