package com.tgy.rtls.data.entity.park;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@ToString
public class CompanyPlace implements Serializable {
   private Integer id;
   private String companyName;
   private Integer total;
   private Integer empty;
   private List<ParkingPlaceDTO> places;
}
