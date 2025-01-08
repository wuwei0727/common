package com.tgy.rtls.data.entity.nb_device;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class Nb_device {
    private Integer id;
    private String mac;
    private String    berthcode;//
    private Short    status=0;//
    private String voltage;
    private long uploadtime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String time;//修改时间
    private Integer instanceid;
    private Integer place;
    private String placeName;
    private String license;
    private Integer map;
  /*  private        System.out.println("datatype");
    private        System.out.println("rsrp" + nb_device.r);
     private       Long */


}
