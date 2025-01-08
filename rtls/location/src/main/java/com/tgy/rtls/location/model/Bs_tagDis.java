package com.tgy.rtls.location.model;

import lombok.Data;

@Data
public class Bs_tagDis {
  public String name;
   public  String  tagid;
   public   long bsid;
   public byte move;
   public float volt;
   public float dis;
   public byte lr;
   public Bs_tagDis(String name,String tagid,long bsid,float dis,byte move,float volt,byte lr){
      this.bsid=bsid;
      this.tagid=tagid;
      this.dis=dis;
      this.lr=lr;
      this.move=move;
      this.volt=volt;
      this.name=name;
   }
}
