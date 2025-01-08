package com.tgy.rtls.data.entity.Camera;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.tgy.rtls.data.entity.park.floorLock.BaseEntitys;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.Camera
*@Author: wuwei
*@CreateTime: 2024-09-20 17:44
*@Description: TODO
*@Version: 1.0
*/
@Data
@Builder
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "camera_vehicle_capture")
public class CameraVehicleCapture extends BaseEntitys implements Serializable {

    private Integer eventId;

    @TableField(value = "serial_number")
    private String serialNumber;

    @TableField(value = "`number`")
    private String number;

    @TableField(value = "camera_record_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime cameraRecordTime;

    @TableField(value = "place")
    private String place;

    @TableField(value = "place_record_time")
    private String placeRecordTime;
    private Integer uniqueFlag;
    private String imgUrl;
    private String imgPathLocal;

    @TableField(exist = false)
    private String x;
    @TableField(exist = false)
    private String y;
    @TableField(exist = false)
    private  Integer map;
    @TableField(exist = false)
    private  String mapName;
    @TableField(exist = false)
    private  String floor;
    @TableField(exist = false)
    private String floorName;
    @TableField(exist = false)
    private  Short state;
    @TableField(exist = false)
    private String fid;
    @TableField(exist = false)
    private String mapKey;
    @TableField(exist = false)
    private String appName;
    @TableField(exist = false)
    private String fmapID;
    @TableField(exist = false)
    private String name;
    @TableField(exist = false)
    private String placeName;
    @TableField(exist = false)
    private String license;
    @TableField(exist = false)
    private Long configId;
    @TableField(exist = false)
    private String cameraVertexInfo;
    @TableField(exist = false)
    private String areaName;
    private Integer capturePlace;

    private static final long serialVersionUID = 1L;
}