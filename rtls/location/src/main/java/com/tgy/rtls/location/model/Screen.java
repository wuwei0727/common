package com.tgy.rtls.location.model;

import lombok.Data;

import java.util.concurrent.ConcurrentHashMap;

@Data
public class Screen {
  public String name;
   public ConcurrentHashMap<String ,Integer> screenName_count=new ConcurrentHashMap<>();

}
