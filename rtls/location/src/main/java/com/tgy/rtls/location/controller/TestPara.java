package com.tgy.rtls.location.controller;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TestPara {
    @ApiModelProperty(value = "测试编号",notes = "必填")
    String testId;
    @ApiModelProperty(value = "测试时间",notes = "必填")
    String  time;
}
