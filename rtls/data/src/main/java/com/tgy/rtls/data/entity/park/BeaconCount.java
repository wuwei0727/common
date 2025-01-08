package com.tgy.rtls.data.entity.park;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class BeaconCount {
   private Integer offLine;
   private Integer  onLine;
   private Integer total;
   private Integer subLowPower;
   private Integer detectorLowPower;
   private Integer roadSpikeCount;
   private Integer rsOnLine;
   private Integer rsOffLine;
}
