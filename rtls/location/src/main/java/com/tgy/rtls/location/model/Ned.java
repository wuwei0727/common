package com.tgy.rtls.location.model;

import lombok.Data;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
@Data
public class Ned {
    volatile public  Long bsid;
    volatile public Date date;
  public  int id;
  volatile public Integer cmd;
  volatile public Integer state;


  public String toJsonString(){

      ObjectMapper mapper = new ObjectMapper();
      String jsonString="";
      try {
          jsonString = mapper.writeValueAsString(this);

      } catch (Exception e) {
          e.printStackTrace();
      }
      return jsonString;
  }

}
