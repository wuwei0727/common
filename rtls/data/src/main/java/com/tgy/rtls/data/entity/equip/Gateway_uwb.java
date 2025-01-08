package com.tgy.rtls.data.entity.equip;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.equip
 * @date 2020/12/23
 * uwb网关信息
 */
@Data
@ToString
@ApiModel(value = "uwb网关")
public class Gateway_uwb implements Serializable {
    private Integer id;
    @ApiModelProperty(value = "网关名")
    private String name;//网关名
    @ApiModelProperty(value = "地图id")
    private String map;//地图id
    @ApiModelProperty(value = "网关ip")
    private String ip;//网关ip
    private Double x;
    private Double y;
    private Double z;
    @ApiModelProperty(value = "用户名")
    private String username;//用户名
    @ApiModelProperty(value = "密码")
    private String password;//密码
    @ApiModelProperty(value = "连接状态 0否 1是")
    private int connect;//连接状态 0否 1是
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;//创建时间
    @ApiModelProperty("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;//修改时间
    private Integer instanceid;//实例id
    private Short floor;//楼层

    private String mapName;//地图名

}
