package com.tgy.rtls.data.entity.equip;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class InfraredMessage implements Serializable {
    private Integer id;
    private String fid;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime localDateTime;
    private Date addTime;
    private Date batteryTime;

    private String rawProductId;
    private String num;
    private Integer lifetimeMonths;
    private String networkName;
    private Integer placeId;
    private Integer networkstate;
    private String mapName;
    private Integer place;
    private short power;
    private Integer state;
    private Integer floor;
    private Integer map;
    private Double x;
    private Double y;
    private Integer infraredId;
    private String placeName;
    private Integer status;
    private Integer count;
    @Override
    public String toString() {
        return JSON.toJSONStringWithDateFormat(this,"yyyy-MM-dd HH:mm:ss",SerializerFeature.WriteDateUseDateFormat);
    }
}