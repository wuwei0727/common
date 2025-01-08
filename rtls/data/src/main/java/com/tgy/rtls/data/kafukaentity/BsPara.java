package com.tgy.rtls.data.kafukaentity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import net.sf.json.JSONObject;
@Data
public class BsPara {
    @ApiModelProperty(value = "指令参数",notes = "power：基站功率设置\n" +
            "beep：基站蜂鸣器控制\n" +
            "locpara：定位参数\n" +
            "general：通用\n" +
            "backgroundurl：基站背景图\n" +
            "word：公司名称\n" +
            "warning：报警控制（通过控制继电器报警）\n" +
            "locationword：基站位置信息\n" +
            "bsold：基站老化\n" +
            "bsslotinf：基站时隙配置" )
    private String keyOrder;
    @ApiModelProperty(value = "实例id",notes = "可不填")
    private Integer instanceId;
    @ApiModelProperty(value = "基站编号",notes = "必填")
    private long bsid;
    @ApiModelProperty(value = "指令参数",notes = "0：读功率\n" +"1：写功率\n" +"当type为0时，powerLevel可为空")
    private short type;
    @ApiModelProperty(value = "消息id",notes = "整数")
    private int messageid;
    @ApiModelProperty(value = "功率配置",notes = "范围0~33（keyOrder为power必填）")
    private short powerLevel;//0~33
     @ApiModelProperty(value = "基站鸣叫间隔",notes = "单位ms,keyOrder为beep必填")
    private int beepInterval;//间隔  单位ms
    @ApiModelProperty(value = "基站鸣叫状态",notes = "0:关闭 1打开 keyOrder为beep必填")
    private short beepState;// 0 : 蜂鸣器关闭  1： 打开
    @ApiModelProperty(value = "基站背景图",notes = "背景图地址url keyOrder为backgroundurl必填")
    private String backgroundUrl;//基站背景图片地址
    @ApiModelProperty(value = "基站显示的公司名",notes = "keyOrder为word必填")
    private String word;//基站公司信息
    @ApiModelProperty(value = "基站继电器控制",notes = "0:关闭 1:打开 keyOrder为warning必填")
    private short warningState;//基站报警状态
    @ApiModelProperty(value = "继电器编号",notes = "0 和 1号继电器 keyOrder为warning必填")
    private short relay_id;//继电器序号
    @ApiModelProperty(value = "老化距离",notes = "单位m,keyOrder为bsold必填")
    private float old_dis ;//基站老化距离
    @ApiModelProperty(value = "老化时间",notes = "单位s,keyOrder为bsold必填")
    private short old_time;//基站老化时间
    @ApiModelProperty(value = "误码率测试次数",notes = "keyOrder为errortest必填")
    private int count;//误码率测试次数
    @ApiModelProperty(value = "误码率测试发送间隔",notes = "keyOrder为errortest必填")
    private int sendInterval; //发送间隔;
    @ApiModelProperty(value = "工作模式",notes = "0:正常模式  1:测试模式,keyOrder为bsslotinf必填")
    private byte mode;	    // 工作模式
    @ApiModelProperty(value = "超帧长度",notes = "单位ms,keyOrder为bsslotinf必填")
    private short superFrame_interval;
    @ApiModelProperty(value = "时隙长度",notes = "单位ms,keyOrder为bsslotinf必填")
    private short slot_duration;
    @ApiModelProperty(value = "信号强度门限",notes = "power：基站功率设置\n,keyOrder为bsslotinf必填")
    private float bsrssi;
    @ApiModelProperty(value = "距离门限",notes = "power：基站功率设置\n,keyOrder为bsslotinf必填")
    private float bsrange;
    @ApiModelProperty(value = "通用测试",notes = "0:基站重启,keyOrder为general必填")
    private int general;
    @ApiModelProperty(value = "ip类型",notes = "keyOrder为net必填")
    private int ip_type;
    @ApiModelProperty(value = "ip地址",notes = "keyOrder为net必填")
    private String address="";
    @ApiModelProperty(value = "子网掩码",notes = "keyOrder为net必填")
    private String netmask="" ;
    @ApiModelProperty(value = "网段",notes = "keyOrder为net必填")
    private String network="";
    @ApiModelProperty(value = "网关地址",notes = "keyOrder为net必填")
    private String gateway="";

    private byte bssid_h;//识别码高位
    private byte bssid_m;//识别码低位
    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }

}
