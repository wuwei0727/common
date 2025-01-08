package com.tgy.rtls.data.entity.park;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ToString
public class BookPlace implements Serializable {
   private Integer id;
   private String start;
   private String  end;
   private Integer place;
   private String placeName;
   private Float fee;//费用
   private String duration;//停留时间
   private String license;
   private Integer charge;
   private Integer map;
   private Integer mapId;
   private String mapName;
   private Integer userid;
   private String fid;
   private String x;
   private String y;
   private String floor;
   private String floorLockId;
   private String phone;
   private String reservationPerson="微信用户";
   private Integer source;
   private Integer status;
   private LocalDateTime time;

}
