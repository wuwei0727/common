package com.tgy.rtls.data.entity.park;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class StorePlace implements Serializable {
   private Integer id;
   private Integer map;
   private Integer userid;
   private String fid;
   private String x;
   private String y;
   private String floor;
   private String name;



   private  String icon;//图标
   private  String ename;//图标
   private String typeNum;
   private String  outdoorType;

}
