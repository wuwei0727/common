package com.tgy.rtls.data.entity.park;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ShangJia {
  private   Integer id;
  private Integer map;
  private String mapName;
  private String name;
  private String owner;
  private String phone;
  private String content;
  private String address;
  private String floor;
  private Integer instanceid;
  private Integer type;
  private String typeName;
  private String x;
  private String y;
  private String z;
  private String fid;
  private String time;
  private String photo;
  private String photolocal;
  private String photo2;
  private String photolocal2;
  private String thumbnail;
  private String thumbnaillocal;
  private String types;
  private String url;

  //蜂鸟地图相关
  @ApiModelProperty(value = "key值")
  private String mapKey;
  @ApiModelProperty(value = "蜂鸟应用名称")
  private String appName;
  @ApiModelProperty(value = "蜂鸟地图ID")
  private String fmapID;
  private String mapImg;//地图路径
  private String themeImg;//主题路径

  private String objectType;
  private String iconType;
  private String floorName;
}
