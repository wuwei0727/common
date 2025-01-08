package com.tgy.rtls.data.entity.park;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class FloorPlace implements Serializable {
   private Integer id;
   private String name;
   private Float x;
   private Float y;
   private Float z;
   private String  floor;
}
