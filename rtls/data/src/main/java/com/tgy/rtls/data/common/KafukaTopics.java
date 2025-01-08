package com.tgy.rtls.data.common;

public class KafukaTopics {

    //kafuka send topic

    public  final static String BS_RANGE_REQ="bsrangereq";//测距请求
    public  final static String File_REQ="filereq12";//音频下发请求
    public  final static String TEXT_REQ="textinfreq";//文字下发请求
    public  final static String BS_CONTROLREQ="bscontrolreq"; //基站参数配置请求
    public  final static String TAG_CONTROLREQ="tagcontrolreq1s";//标签参数配置请求
    public  final static String BS_ERRORCODETEST="bserrorcodetest";//基站误码率测试
    //kafuka send topic
    public  final static String BS_RANGE_RES="bsrangereq";//测距响应
    public  final static String FIlE_RES="fileres";//音频下发响应
    public  final static String TEXT_RES="textinfres";//文字下发响应
    public  final static String BS_CONTROLRES="bscontrolres"; //基站参数配置响应
    public  final static String TAG_CONTROLRES="tagcontrolres";//标签参数配置响应
    public  final static String TAG_LOCATION="taglocation12233";//标签定位数据
    public  final static String TAG_SENSOR="tagsensor";//标签传感器数据
    public  final static String TAG_INITPARA="taginitpara";//标签开机上传的参数
    public  final static String BS_STATE="bsstate";//基站状态数据
    public  final static String TAG_RANGE="tagrange";//标签测距
    public final static String WARN_MESSAGE="warnmessage";//报警模块 报警信息请求
    public final static String TAG_UPDATE="tagupdate";//标签升级

    public  final static String LORA_STATE="lora_state111";//网关状态
    public  final static String INFRARED_STATE="infrared_state";//红外状态
    public  final static String VISUAL_DATA_SEND="visual_data_send";//红外状态
    public  final static String INFRARED_STATE1="infrared_state1";//红外状态
    public  final static String NED_STATE="ned_state";//地锁设备状态   kafka主题

    public final static String CONNECT_GATEWAY="connectGateway";//网关连接
    public final static String UPDATE_DW1001BS="updatebsdw1001";//修改dw1001
    public static final String CAMERA_EXIST = "camera_exist";//摄像头存在


    public final int SOS_WARNING=6;//SOS报警
    public final int POWER_WARNING=7;//低电量报警
    public final int OFFLINE_OVERTIME_WARNING=10;//人员离线超时报警
    public final int INCOAL_OVERTIME_WARNING=2;//人员井下超时
    public final int BS_OFFLINE_WARNING=13;//分站离线


    /**
     * 返回结果提示
     */
    public static final String SYSTEM_BUSY="system_busy";//系统繁忙
    public static final String ADD_SUCCESS="add_success";//添加成功
    public static final String STORE_SUCCESS="store_success";//系统繁忙
    public static final String UNSTORE_SUCCESS="unstore_success";//添加成功
    public static final String ADD_FAIL="add_fail";//添加失败
    public static  final String QUERY_SUCCESS="query_success";//查询成功


    public static final String DELETE_SUCCESS="delete_success";//删除成功
    public static final String CANCEL_SUCCESS="cancel_success";//取消成功
    public static  final String DELETE_FAIL="delete_fail";//删除失败


    public static final String SAVE_SUCCESS="save_success";//保存成功
    public static final String SAVE_FAIL="save_fail";//保存失败

    public static final String WORK_ATTENDANCE="work_attendance";//考勤
    public static final String WORK_RANGE="work_range";//考勤排班
    public static final String EVENT_LOG="event_log";//事件日志
    public static final String OPERATION_LOG="operation_log";//事件日志



    public static final String PERSONINFO_INCOAL="personinfo_incoal";//井下人数信息
    public static final String PERSONINFO_OFFLINE="personinfo_offline";//离线人数信息
    public static final String PERSONINFO_OVERTIME="personinfo_overtime";//超时人数信息
    public static final String SUB_MAP="sub_map";//地图分站信息
    public static final String PERSONINFO_SUB="personinfo_sub";//地图分站人数信息
    public static final String MAPINFO="mapinfo";//地图信息
    public static final String AREAINFO ="areainfo";//重点区域信息 ;
    public static final String PERSONINFO_INAREA="personinfo_inarea";//区域人数信息

	
	
	
	
    public static final String QUERY_MICROBSINFO="query_microbsinfo";//查询微基站信息
    public static final String ADD_MICROBS="add_microbs";//添加微基站
    public static final String MICROBS_EXIST="microbs_exist";//该微基站已存在
    public static final String UPDATE_MICROBS="update_microbs";//修改微基站
    public static final String DELETE_MICROBS="delete_microbs";//删除微基站
    public static final String QUERY_GATEWAY="query_gateway";//查询uwb网关信息
    public static final String ADD_GATEWAY="add_gateway";//添加uwb网关
    public static final String UDPATE_GATEWAY="udpate_gateway";//修改uwb网关
    public static final String DELETE_GATEWAY="delete_gateway";//删除网关

    public static final String QUERY_CAMERA="query_camera";//查询camera
    public static final String ADD_CAMERA="add_camera";//添加camera
    public static final String UDPATE_CAMERA="udpate_camera";//camera
    public static final String DELETE_CAMERA="delete_camera";//camera

    public static final String SELECT_GATEWAY="select_gateway";//请选择要连接的网关！
    public static final String DISCONNECT_GATEWAY="disconnect_gateway";//连接已断开
    public static final String CONNECTED_GATEWAY="connected_gateway";//连接成功
    public static final String OPERATION_SUCCESS="operation_success";//操作成功
    public static final String MAPSIZE_ERROR="mapsize_error";//提供的图片尺寸不符合要求


    public static final String ADD_SUB ="add_sub";//添加分站 ;
    public static final String UPDATE_SUB="update_sub";//修改分站
	public static final String DELETE_SUB="delete_sub";//删除分站
	public static final String SUB_INUSE="sub_inuse";//该分站已使用
    public static final String SYSTEM_ERROR="system_error";//系统异常
	public static final String UPLOADFILE_ERROR="uploadfile_error";//上传的文件包出错
	
	public static final String UPGRADE_SUCCESS="upgrade_success";//升级成功
	public static final String UPGRADE_FAIL="upgrade_fail";//升级失败

	
	
	
	public static final String TAG_INUSE="tag_inuse";//该卡号已使用
	public static final String ADD_TAG="add_tag";//添加标签
	public static final String UPDATE_TAG="update_tag";//修改标签
	public static final String DELETE_TAG="delete_tag";//删除标签
	
	
	
	public static final String CONTENT_EMPTY="content_empty";//内容为空
	public static final String IMPORT_SUCCESS="import_success";//导入成功，导入
	public static final String N_COUNTINFO="n_countinfo";//n条数据
	public static final String IMPORT_FAIL_ERRORFORMAT="import_fail_errorformat";//导入失败，数据格式不正确
	public static final String IMPORT_FAIL_EMPTYFILE="import_fail_emptyfile";//导入失败，空文件
	
	public static final String SEND_SUCCESS="send_success";//发送成功
	

	
	public static final String QUERY_AREAINFO="query_areainfo";//查询区域信息
	public static final String AREANAME_CONFIX="areaname_confix";//区域名冲突
	public static final String ADD_AREA="add_area";//添加区域
	public static final String UPDATE_AREA="update_area";//修改区域
	public static final String DELETE_AREA="delete_area";//删除区域

	public static final String QUERY_MAP="query_map";//查询地图信息
	public static final String ADD_MAP="add_map";//添加地图
	public static final String UPDATE_MAP="update_map";//修改地图
	public static final String DELETE_MAP="delete_map";//删除地图
		
		
		
	public static final String SEND_AUDIO="send_audio";//发送音频
	public static final String SEND_TXT="send_txt";//发送文本
	public static final String SEND_RETRIVE="send_retrive";//发送撤退命令
	public static final String RECOVERY_SUCCESS="recovery_success";//恢复成功
	public static final String SUCCESS="success";//成功
	public static final String FAIL="fail";//失败
		
		
	public static final String QUERY_WARNINGRECORD="query_warningrecord";//查询报警记录
	public static final String WARNINGRECORD="warningrecord";//报警记录
	public static final String LINECHECKLINE_FIRST="linecheckline_first";//请先规划好巡检路线
	public static final String LINECHECK_EXCEL="linecheck_excel";//巡检报表
	public static final String LINECHECK_TASK="linecheck_task";//巡检任务

		
		
		

	public static final String NAME_EXIST="name_exist";//名称不能重复
	public static final String ADD_GROUPPERMISSION="add_grouppermission";//新增权限组
	public static final String UPDATE_GROUPPEMISSION="update_grouppemission";//修改权限组
	public static final String DELETE_GROUPPERMISSION="delete_grouppermission";//删除权限组

    public static final String ID_EXIST="id_exist";//id不能重复
		
		
	public static final String EDIT_SUCCESS="edit_success";//编辑成功
	public static final String EDIT_FAIL="edit_fail";//编辑失败
	public static final String ACOUNT_NOTUSE="acount_notuse";//账号未启用
	public static final String PASSWORD_ERROR="password_error";//密码不正确
	public static final String VCODE_ERROR="vcode_error";//验证码错误
	public static final String ADD_PROJECT="add_project";//添加项目
		
		
		
	public static final String IDCODE_INUSE="idcode_inuse";//识别码2已使用
	public static final String INSTANCE_INUSE="instance_inuse";//该实例已存在
	public static final String UPDATE_PROJECT="update_project";//修改项目
	public static final String DELETE_PROJECT="delete_project";//删除项目
	public static final String DELETE_PROJECT_FAIL="delete_project_fail";//删除失败,没有找到您要删除的实例
	public static final String GO_PROJECT="go_project";//进入项目
		
		

	public static final String SELECTPROJECT_FIRST="selectproject_first";//请先选择项目
		
		
		
	public static final String QUERY_GROUPPERMISSION="query_personpermission";//查询权限组信息
	public static final String QUERY_LOGINLOG="query_loginlog";//查询登录日志信息
	public static final String PHONENUM_EXIST="phonenum_exist";//手机号码不允许重复

		
		
		
				
		
	public static final String OPEN_SUCCESS="open_success";//开启成功
	public static final String OPEN_FAIL="open_fail";//开启失败

		
		
				

	public static final String QUERY_PERSONINFO="query_personinfo";//查询人员信息
	public static final String NUM_EXIST="num_exist";//该工号已存在
	public static final String ADD_PERSON="add_person";//添加人员
	public static final String DELETE_PERSON="delete_person";//删除人员
	public static final String DELETE_FAIL_NOPERSON="delete_fail_noperson";//删除失败,没有找到您要删除的人员
	public static final String PERSONINFO="personinfo";//人员信息




    public static final String QUERY_SUB ="query_sub";//查询分站 ;
    public static final String QUERY_TAG ="query_tag";//查询标签 ;
    public static final String TAG_EXIST ="tag_exist";//标签已存在 ;
    public static final String QUERY_MESSAGE ="query_message";//通讯记录查询 ;
    public static final String PICSIZE_ERROR ="picsize_error";//图片尺寸不适合 ;
    public static final String SEND_FAIL ="send_fail";//发送失败;
    public static final String UPDATE_SUCCESS ="update_success";//修改成功
    public static final String UPDATE_FAIL ="update_fail";//修改失败
    public static final String PHONENUM_INUSE ="phonenum_inuse";//电话号码已经使用
    public static final String CLOSE_SUCCESS ="close_success";//关闭成功
    public static final String UPDATE_PERSON="update_person";//修改人员
    public static final String WORK_RANGENAME_CONFIX="work_rangename_confix";//考勤排班冲突
     public static final String GATEWAYIP_CONFIX ="gatewayip_confix";//网关ip冲突

    public static final String IN ="in";//进
    public static final String OUT="out";//出
    public static final String SUB ="sub";//基站
    public static final String AREA="area";//区域
    public static final String COAL="coal";//矿井

    public static final String DELETE_AREAFIRST="delete_areafirst";//删除区域类型前，请先删除区域
    public static final String QUERY_PERSONPERMISSION="query_personpermission";//查询人员权限
    public static final String ADD_PERSONPERMISSION="add_personpermission";//添加人员权限
    public static final String UPDATE_PERSONPERMISSION="update_personpermission";//修改人员权限
    public static final String DELETE_PERSONPERMISSION="delete_personpermission";//删除人员权限


    //数据导出

    public static final String ID="id";//"序号"
    public static final String PERSON="person";// "操作人员",
    public static final String EVENT="event";// "事件",
    public static final String TIME="time";// "触发时间



    public static final String NAME="name";// "姓名",
    public static final String NUM="num";// "工号",
    public static final String WORKTYPE="worktype";// 工种
    public static final String CARDNUM="cardnum";//卡号
    public static final String JOB="job";// 职务
    public static final String LEVEL="level";// 级别
    public static final String DEPARTMENT="department";// 部门
    public static final String OFFLINETIME="offlinetime";// 离线时间
    public static final String OFFLINEDELAY="offlinedelay";//"离线时长"

    public static final String INCOALTIME="incoaltime";// 下井时间
    public static final String STAYINCOALTIME="stayincoaltime";// 逗留时间
    public static final String STATE="state";//状态


    public static final String NUM_SUB="num_sub";// "分站编号",
    public static final String ERROR_SUB="error_sub";// "故障信息",
    public static final String POWER_SUB="power_sub";// "供电状态",
    public static final String NET_SUB="net_sub";// "网络状态",
    public static final String TOPCOUNT_PERSON="topcount_person";// 人数上限",
    public static final String CURRENTCOUNT_PERSON="currentcount_person";// 检测人数",
    public static final String COUNT_STATE="count_state";// "人数状态


    public static final String AREA_NAME="area_name";//  "区域名称",
    public static final String AREA_TYPE="area_type";// "区域类型",
    public static final String AREA_ISUSED="area_isused";// "是否启用",

    public static final String INSUB_TIME="insub_time";// 进入分站时间,
    public static final String STAYSUB_TIME="staysub_time";// 分站逗留时间


    public static final String INAREA_TIME="inarea_time";// 进入区域时间,
    public static final String STAYAREA_TIME="stayarea_time";// 区域逗留时间

    public static final String EXPORT_FAIL="export_fail";// 导出失败

    public static final String WARNING_TYPE="warning_type";// 报警类型",
    public static final String WARNING_REASON="warning_reason";// "报警原因",
    public static final String STARTTIME="starttime";// "开始时间",
    public static final String ENDTIME="endtime";// "结束时间",
    public static final String DURATION="duration";// "持续时间",
    public static final String ISEND="isend";// "是否结束",
    public static final String RELATION_MAP="relation_map";// "关联地图",

    public static final String MORNING="morning";// "早",
    public static final String NOON="noon";// "中",
    public static final String NIGHT="night";// "晚",

    public static final String ONE="one";// "一",
    public static final String TWO="two";// "二",
    public static final String THREE="three";// "三",
    public static final String FOUR="four";// "四",
    public static final String FIVE="five";// "五",
    public static final String SIX="six";// "六",
    public static final String SEVEN="seven";// "七",
    public static final String EIGHT="eight";// "八",


    public static final String CHART="chart";// 图例,
    public static final String TODAY="today";// 当日,
    public static final String TOMORROW="tomorrow";// 次日,

    public static final String ATTENDENCE="attendence";// 出勤,
    public static final String HOLIDAY="holiday";// 休假,
    public static final String LATE="late";// 迟到,
    public static final String EARLY_LEAVE="early_leave";// 早退,
    public static final String ABSENT="absent";// 旷工,
    public static final String DAYS="days";// 天数,


    public static final String NORMAL="normal";// 正常,
    public static final String ABNORMAL="abnormal";// 异常,
    public static final String ROUTER="router";// 路线,


    public static final String WEEK="week";// 周
    public static final String MON="mon";// "一",
    public static final String TUS="tus";// "二",
    public static final String WED="wed";// "三",
    public static final String THU="thu";// "四",
    public static final String FRI="fri";// "五",
    public static final String SAT="sat";// "六",
    public static final String SUN="sun";// "周日",
    public static final String DAY="day";// "日",
    public static final String MONTH="month";// "月",
    public static final String HOUR="hour";// "小时",

    public static final String TIMEFORMAT="timeformat";// 时间格式
    public static final String ONLINE="online";// 在线
    public static final String OFFLINE="offline";// 离线

    public static final String SUBOFFLINE="suboffline";// 离线


    public static final String NAME_CONFLICT="nameconflict";// 时间格式
    public static final String NAME_EMPTY="nameempty";// 时间格式
    public static final String NUM_EMPTY="numempty";// 工号为空
    public static final String PHONENUM_ERROR="phonenum_error";//手机号码错误
    public static final String WORKTYPE_EMPTY="worktypeempty";// 工种为空
    public static final String WORKTYPE_EXIST="worktypeexist";// 工种已存在
    public static final String JOB_EMPTY="jobempty";// 职务为空
    public static final String LEVEL_EMPTY="levelempty";// 级别为空
    public static final String DEPARTMNET_EMPTY="departmentexist";// 部门为空
    public static final String JOB_EXIST="jobexist";// 职务已存在
    public static final String LEVEL_EXIST="levelexist";// 级别已存在
    public static final String DEPARTMNET_EXIST="departmentexist";// 部门已存在
    public static final String DATE="date";// "日期
    public static final String PASSWORD_LENGTH="password_length";// "密码长度不够

    public static  final String TOKEN_ERROR="token错误";

    public static  final String UserName_EXIST_ERROR="userNameExistError";//用户名不能重复
    public static  final String USERNAME_NULL="userNameNull";//用户名为空
    public static  final String PASSWORD_NULL="PasswordNull";//密码为空
    public static  final String userNameError="userNameError";//用户名错误
    public static final String PASSWORD_ERROR1="password_error1";//密码不正确
    public static  final String login_Success="loginSuccess";//登录成功
    public static  final String login_Error="loginError";//登录失败
    public static  final String zhanghao_0="zhanghao_0";//账号禁用
    public static  final String userNamePasswordError="userNamePasswordError";//用户名或密码错误
    public static final String AccountNotEna="AccountNotEna";//账号未启用

    public static final String MAP_NAME_LENGTH="mapNameLength";//地图名称长度大于
    public static final String ROAD_NAME="roadName";//地图名称不能为空
    public static final String MAP_ID="mapIdIsNotNull";//地图ID不能为空
    public static final String MAP_X="mapXIsNotNull";//地图经度不能为空
    public static final String MAP_Y="mapYIsNotNull";//地图纬度不能为空
    public static final String CURRENT_MAP_EXISTS="current_map_exists";//当前地图下已经存在该信息,请检查
    public static final String INTERIOR_NAME="interiorName";//内部名称不能为空
    public static final String delSelectHaveYourAccoutCannotDel="delSelectHaveYourAccoutCannotDel";//删除所选账号中有自己的账号不能删除

    public static final String THERE_ARE_ALREADY_OTHER_MERCHANTS_AT_THE_CURRENT_LOCATION="there_are_already_other_merchants_at_the_current_location";//当前位置已有其它商家
    public static final String THERE_ARE_ALREADY_OTHER_COMPANY_AT_THE_CURRENT_LOCATION="there_are_already_other_company_at_the_current_location";//当前位置已经有其它公司

    public static final String PLEASE_ENTER_THE_DEVICE_INFORMATION_YOU_WANT_TO_BIND="please_enter_the_device_information_you_want_to_bind";//请输入你要绑定的设备信息！！！
    public static final String MAP_IS_NOT_NULL="map_Is_Not_Null";//请输入你要绑定的设备信息！！！
    public static final String PARKING_SPACE_IS_EMPTY="parking_space_is_empty";
    public static final String COORDINATES_IS_EMPTY="coordinates_is_empty";

    public static final String ADD="add";
    public static final String DELETE="delete";
    public static final String EDIT="edit";

    public static final String PLACE_PLANNING="place_planning";
    public static final String VIP_PLACE="vip_place";
    public static final String VIP_AREA="vip_area";
    public static final String PLACE_VIDEO="place_video";
    public static final String PLACE_EXIT="place_exit";
    public static final String RECOMM_CONFIG="recomm_config";
    public static final String C_L_C="c_l_c";
    public static final String t_p="t_p";
    public static final String p_u_r="p_u_r";
    public static final String cp="cp";
    public static final String GARAGE_ENTRANCE="garage_entrance";
    public static final String MAP_PATH_INFO="mapPath_info";
    public static final String COMPANY_INFO="company_info";
    public static final String PEB_INFO="peb_info";
    public static final String BUSINESS_INFO="business_info";
    public static final String BEACON_INFO="beacon_info";
    public static final String GATEWAY_INFO="gateway_info";
    public static final String DETECTOR_INFO="detector_info";
    public static final String FLOOR_LOCK="floor_lock";
    public static final String FLOOR_LOCK_CONFIG="floor_lock_config";
    public static final String BARRIER_INFO="barrier_info";
    public static final String GUIDE_SCREEN_DEVICE="guideScreen_device";
    public static final String GUIDE_SCREEN_CONFIG="guideScreen_config";
    public static final String MAP_INFO="map_info";
    public static final String MEMBER_INFO="member_info";
    public static final String ROLE_INFO="role_info";
    public static final String MOBILE_USER_INFO="mobileUser_info";
    public static final String MWC_INFO="mwc_info";
    public static final String MB_INFO="mb_info";
    public static final String SMS_INFO="sms_info";
    public static final String P_L_C_INFO="p_l_c_info";
    public static final String D_A_C_INFO="d_a_c_info";
    public static final String D_A_A_INFO="d_a_a_info";
    public static final String PRO_INFO="pro_info";
    public static final String PRO_CODE_INFO="pro_code_info";
    public static final String CAMERA_CONFIG="camera_config";
    public static final String PARKINGALERTCONFIG="parkingAlertConfig";
    public static final String WHITELIST="whitelist";
    public static final String MS="ms";
    public static final String APB="apb";
    public static final String SJT="sjt";
    public static final String TIME_EXIST = "time_exist";//摄像头存在
    public static final String FEED_BACK = "feed_back";//摄像头存在
    public static final String FEED_BACK_TYPE = "feed_back_type";//摄像头存在
    public static final String QCL = "qcl";//摄像头存在

































}
