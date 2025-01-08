package com.tgy.rtls.data.entity.park;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
public class LicensePos implements Serializable {
   private Integer id;
   private String license;
   private Integer map;
   private String x;
   private String y;
   private String z;
   private String fid;
   private String floor;
   private Integer userid;
   private String name;
   private Integer state;
   private Date updatetime;
   private double dis;

}
