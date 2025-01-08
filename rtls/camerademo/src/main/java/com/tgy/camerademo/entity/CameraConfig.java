package com.tgy.camerademo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CameraConfig extends BaseEntitys implements Serializable {
    private static final long serialVersionUID = 1L;

    private String serialNumber;

    private String name;

    private String map;

    @TableField(value = "networkstate")
    private int networkState;

    private String x;

    private String y;

    private String fid;

    private int floor;
    @TableField(exist = false)
    private String mapKey;
    @TableField(exist = false)
    private String appName;
    @TableField(exist = false)
    private String fmapID;
    @TableField(exist = false)
    private String floorName;
    @TableField(exist = false)
    private String mapName;
}
