package com.tgy.rtls.data.entity.park;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
public class ParkingCompany implements Serializable {
   private Integer id;
   private String name;
   private String instanceid;
   private String places;
   private String user;
   private String pwd;
   private String phone;
   private Integer uid;
   private Integer role=1;
   private String floor;//楼层
   private String x;
   private String y;
   private String z;
   private String fid;
   private String map;
   private String types;

   private String membername;
   private String password;
   private Integer cid;  //部门
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
   private Date addTime;
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
   private String loginTime;
   private String url;
   private Integer enabled;//0不启用1启用
   private String describe;
   private String cname;
   private String mapName;

   //蜂鸟地图相关
   @ApiModelProperty(value = "key值")
   private String mapKey;
   @ApiModelProperty(value = "蜂鸟应用名称")
   private String appName;
   @ApiModelProperty(value = "蜂鸟地图ID")
   private String fmapID;
   private String mapImg;//地图路径
   private String themeImg;//主题路径
   private String floorName;
}
