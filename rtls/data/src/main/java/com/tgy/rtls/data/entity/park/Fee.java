package com.tgy.rtls.data.entity.park;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.text.SimpleDateFormat;
import java.util.Date;


@Data
@ToString
public class Fee {
    private Integer id;
    private String license;
    private Integer map;
    private Float fee;

    private String  inTime;
    private String  outTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date enterTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date  exitTime;


    private Short  monthlyRent;
    private String enterName;
    private String exitName;

    public void setEnterTime(Date enterTime) {
        this.enterTime = enterTime;
        SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate3 = sdf3.format(enterTime);
        this.inTime=strDate3;
    }
    public void setExitTime(Date exitTime){
        this.exitTime=exitTime;
        SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate3 = sdf3.format(exitTime);
        this.outTime=strDate3;
    }
}
