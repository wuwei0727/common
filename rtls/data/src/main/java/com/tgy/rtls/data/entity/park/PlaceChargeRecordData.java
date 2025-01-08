package com.tgy.rtls.data.entity.park;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PlaceChargeRecordData {
    private String placeName;//车位名称
    private Integer map;
    private Integer count;//频次
    private Float time;//充电总时长
    private Integer place;//车位id




}
