package com.tgy.rtls.docking.dao;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.equip
 * @Author: wuwei
 * @CreateTime: 2023-06-08 19:50
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@Accessors(chain = true)
public class PlaceVo implements Serializable {
    private Integer id;
    private String name;
    private Integer state;
    private Integer type;
    private Integer configWay;
    private String license;
    private String carBitNum;
    @JSONField (name = "PlateNo")
    private String plateNo;
    private Integer map;
    private Integer place;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime entryTime;//进入时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime fullUploadTime;

}
