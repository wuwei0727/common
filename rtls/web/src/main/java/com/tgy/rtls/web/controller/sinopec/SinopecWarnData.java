package com.tgy.rtls.web.controller.sinopec;

import lombok.Data;

@Data
public class SinopecWarnData {
    private String appId;
    private String callTime;
    private String callPwd;

    private String  infoId;
    private String  infoAppId;//信息来源
    private String  infoType;//预警类型
    private String  infoDesc;//预警详情
    private String  infoImage;//详情地址
    private String  infoUrl;//图片或者视频地址
    private String  infoUserCode;//用户识别码
    private String  infoDeviceId;//设备id
    private String  needOpt;//是否需要处理



}
