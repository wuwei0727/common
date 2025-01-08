package com.tgy.rtls.data.entity.equip;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tgy.rtls.data.entity.map.Map_2d;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import net.sf.json.JSONObject;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.equip
 * @date 2020/12/23
 * uwb网关信息
 */
@Data
@ToString
@ApiModel(value = "uwb网关")
public class Gateway_lora implements Serializable {
    private Integer id;

    private String num;//id

    private Integer map;//地图id

    private String ip;//网关ip
    private Double x;
    private Double y;

    private Short floor;//楼层
    private String networkName;//网络状态名
    private Short networkstate;//网络状态名
    private String mapName;
    private String hardware;
    private String firmware;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;//创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;//修改时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime batteryTime;//创建时间
    //蜂鸟地图相关
    @ApiModelProperty(value = "key值")
    private String mapKey;
    @ApiModelProperty(value = "蜂鸟应用名称")
    private String appName;
    @ApiModelProperty(value = "蜂鸟地图ID")
    private String fmapID;
    private String mapImg;//地图路径
    private String themeImg;//主题路径
    private List<Map_2d> mapList;
    private String floorName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime offlineTime;

    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }

}
