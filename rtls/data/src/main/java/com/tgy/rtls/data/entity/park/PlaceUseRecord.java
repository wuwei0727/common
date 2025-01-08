package com.tgy.rtls.data.entity.park;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class PlaceUseRecord {
    private Long id;
    private String license;
    private Integer map;
    private String  start;
    private String end;
    private int isFakeData;
    private Integer place;
    private long timestamp;
    private LocalDateTime dateTime;


    public PlaceUseRecord(long id, Integer map, Integer place, String start, String end) {
        this.id = id;
        this.map = map;
        this.place = place;
        this.start = start;
        this.end = end;
    }

    public PlaceUseRecord() {

    }
}
