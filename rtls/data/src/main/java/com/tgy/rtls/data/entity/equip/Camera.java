package com.tgy.rtls.data.entity.equip;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.equip
 * @date 2020/12/23
 * uwb网关信息
 */
@Data
@ToString
@ApiModel(value = "摄像头")
public class Camera implements Serializable {
    private Integer id;
    @ApiModelProperty(value = "名称")
    private String name;//名称
    @ApiModelProperty(value = "地图id")
    private String map;//地图id
    @ApiModelProperty(value = "地址")
    private String address;//网关ip
    private Float x;
    private Float y;
    private Float z;
    private Integer instanceid;//实例id
    private Short floor;//楼层
    private String mapName;//地图名
   /* private String num;//设备编号*/

}
