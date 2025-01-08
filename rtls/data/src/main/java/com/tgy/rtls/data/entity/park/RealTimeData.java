package com.tgy.rtls.data.entity.park;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class RealTimeData implements Serializable {
   private Integer usedPlace;
   private Integer  emptyPlace;
   private Integer  usedCharge;
   private Integer emptyCharge;
   private Integer bookPlace;
   private Integer  totalPlace;
   private Integer totalCharge;
   private Integer offLine;
   private Integer  onLine;
   private Integer total;
   private Integer subLowPower;
   private Integer detectorLowPower;



}
