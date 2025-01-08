package com.tgy.rtls.data.entity.park;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class RealTrail implements Serializable {
   private Integer id;
   private Integer uid;
   private String start;
   private String end;
   private Integer map;
   private String  name;

}
