package com.tgy.rtls.data.entity.park;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ChargeUseRecord {
    private Integer id;
    private String license;
    private Integer map;
    private String  start;
    private String end;
    private String place;


}
