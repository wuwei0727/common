package com.tgy.rtls.data.entity.eventserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleData {
    private Long id;
    private String x;
    private String y;
    private  Integer map;
    private  String mapName;
    private  String floor;
    private String floorName;
    private  Short state;
    private String fid;
    private String mapKey;
    private String appName;
    private String fmapID;
    private String placeName;
    private String license;

}