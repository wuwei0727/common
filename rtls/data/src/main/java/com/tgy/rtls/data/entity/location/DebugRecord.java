package com.tgy.rtls.data.entity.location;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.location
 * @date 2020/10/23
 * 定位数据
 */
@Data
@ToString
public class DebugRecord implements Serializable {
    private int status=1;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;
    private Double x;
    private Double y;
    private Double z;
    private String area;//区域
    private String debugdata;
    public JSONArray debugData;
    private String map;
    private String tagid;
    private Integer id;


}
