package com.tgy.rtls.data.entity.park;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class BeaconVolt {
   private String num;
   private String  volt;
   private Integer  map;
   private double addVoltSection=0.1;
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
   private LocalDateTime batteryTime;//电压检测时间
}
