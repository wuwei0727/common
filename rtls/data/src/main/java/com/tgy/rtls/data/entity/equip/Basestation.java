package com.tgy.rtls.data.entity.equip;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import net.sf.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.equip
 * @date 2021/1/4
 * 微基站
 */
@Data
@ToString
@ApiModel(value = "微基站")
public class Basestation implements Serializable {
    @ApiModelProperty("基站自增id")
    private Integer id;
    @ApiModelProperty("基站编号")
    private String num;
    @ApiModelProperty("管理地图id")
    private String map;
    private String fmapID;
    @ApiModelProperty("网络状态")
    private int networkstate;
    @ApiModelProperty("天线延时")
    private String antennadelay;
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;//创建时间
    @ApiModelProperty("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;//修改时间
    private Double x;
    private Double y;
    private Double z;
    private Short floor;
    private Integer instanceid;
    private Short initiator;//0 c从基站 1主基站
    private String networkName;//网络状态名
    private String mapName;//关联地图名
    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }
}
