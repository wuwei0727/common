package com.tgy.rtls.location.model;

import java.util.concurrent.ConcurrentHashMap;

public class BsCheck {

   public volatile boolean flag;
   public ConcurrentHashMap<Long ,Boolean> random=new ConcurrentHashMap<>();
}
