package com.tgy.rtls.data.entity.Camera;

import com.baomidou.mybatisplus.annotation.TableField;
import com.tgy.rtls.data.entity.park.floorLock.BaseEntitys;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

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
    private String radius;
    private Long areaId;


    private String floor;

    private String cameraVertexInfo;


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

    // 区域名称（非数据库字段）
    @TableField(exist = false)
    private String areaName;
    @TableField(exist = false)
    private String areaQuFen;

    // 推荐级别（非数据库字段）
    @TableField(exist = false)
    private String recommLevel;

    // 前端传递的车位列表（非数据库字段）
    @TableField(exist = false)
    private String placeList;

    // 前端传递的顶点信息（非数据库字段）
    @TableField(exist = false)
    private List<VertexInfo> vertexInfo;

    // 静态内部类，用于描述顶点信息
    @Data
    public static class VertexInfo {
        private String floor; // 楼层
        private String areaQuFen;
        private List<Point> points; // 顶点坐标列表

        @Data
        public static class Point {
            private String x; // X 坐标
            private String y; // Y 坐标
            private String floor;
        }
    }
}
