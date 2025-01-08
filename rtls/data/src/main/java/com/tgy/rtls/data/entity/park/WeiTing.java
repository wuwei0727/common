package com.tgy.rtls.data.entity.park;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
@Data
@ToString
@TableName(value = "noparking")
public class WeiTing {
    private Integer id;
    private String license;
    private Short state;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;
    private String camera;
    private String photo;
    private String photolocal;
    private String floor;
    private Integer map;
    private String mapName;
    private Float x;
    private Float y;


}
