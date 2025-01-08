package com.tgy.rtls.location.tdoa;

import java.util.concurrent.ConcurrentHashMap;
/*

 */
public class BsTimestamps implements Comparable<BsTimestamps> {
    public ConcurrentHashMap<String,BsTimestamp> bsinf=new ConcurrentHashMap<String,BsTimestamp>();// 基站id- 时间戳
    public volatile String mbs="";
    public volatile  Long synid=null;
    public volatile  Long rangeid=null;
    public volatile Long time=null;


    @Override
    public int compareTo(BsTimestamps o) {
      return this.time.compareTo(o.time);
    }
}
