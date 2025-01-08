package com.tgy.rtls.data.entity.park;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ParkingCrossFloor {
    private Integer id;
    private String name;
    private String x;
    private String y;
    private  Integer map;
    private String fid;
    private String floor;


}
