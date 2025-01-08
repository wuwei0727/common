package com.tgy.rtls.data.entity.park;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SimulateTrail {
   private Integer id;
   private String  niceName;
    private String  startX;
    private String  startY;
    private Integer endX;
    private String  endY;
    private Integer floor;



}
