package com.tgy.rtls.web.controller.sinopec;

import lombok.Data;

@Data
public class SinopecWarnOptData {
    private String appId;
    private String callTime;
    private String callPwd;

    private String  infoId;
    private String  infoAppId;//信息来源
    private String  optTime;//操作时间
    private String  optDesc;//处理内容
    private String  optUserCode;//处理人操作码



}
