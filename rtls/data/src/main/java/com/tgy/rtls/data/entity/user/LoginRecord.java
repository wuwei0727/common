package com.tgy.rtls.data.entity.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.user
 * @date 2021/1/13
 * 登录记录
 */
@Data
@ToString
@ApiModel("登录记录")
public class LoginRecord implements Serializable {
    private Integer id;
    private Integer uid;
    @ApiModelProperty("登录ip")
    private String ip;
    @ApiModelProperty("登录地址")
    private String address;
    @ApiModelProperty("登录手机号")
    private String phone;
    @ApiModelProperty("登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;
    @ApiModelProperty("smallApp登录用户名")
    private String userName;
}
