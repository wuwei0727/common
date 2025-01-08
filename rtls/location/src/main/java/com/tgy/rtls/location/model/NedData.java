package com.tgy.rtls.location.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public   class NedData  {
    private Integer nedid;
    private String time;
    private Integer cmd;
    private Integer warningState;
    private Integer mode;
    private Integer occupyState;
    private Integer position;
    private Integer power;
    private LocalDateTime date;


  public NedData(){

  }

}
