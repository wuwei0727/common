package com.tgy.rtls.data.entity.park;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PlaceUseRecordData {
    private String placeName;//车位名称
    private Integer map;
    private Integer count;//次数
    private Float time;//停留时长
    private Integer place;//车位id




}
