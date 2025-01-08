package com.tgy.rtls.data.entity.park;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@ToString
public class MapPlace implements Serializable {
   private Integer id;
   private String mapName;
   private String describe;
   private String lng;
   private String lat;
   private Integer total;
   private Integer empty;
   private Integer isReservableCount;
   private int dis;
   private String disText;
   private List<ParkingPlace> places;
   private String mapLogo;
   private String mapLogolocal;

   private String name;
   private String x;
   private String y;
   private  String z;
   private String fid;
   private  String floor;
   private  Short state;
   private String carbittype;
   private String companyCount;
   private Integer coordinate;

   private Double cost;
   private String desc;
   private Double evaluate= 5.0;

}
