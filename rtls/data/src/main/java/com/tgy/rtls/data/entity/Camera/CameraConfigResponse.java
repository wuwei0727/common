package com.tgy.rtls.data.entity.Camera;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CameraConfigResponse {
    private Long cameraId;
    private String areaId;
    private String areaName;
    private String serialNumber;
    private String name;
    private String mapName;
    private String fmapID;
    private String appName;
    private String mapKey;
    private String floorName;
    private String networkState;
    private String map;
    private String floor;
    private String x;
    private String y;
    private String radius;
    private String cameraVertexInfo;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    private String placeList;  // 车位列表
    private List<CameraConfigResponse.VertexInfo> vertexInfo; // 顶点信息

    public double distance;

    // 静态内部类，用于描述顶点信息
    @Data
    public static class VertexInfo {
        private String floor; // 楼层
        private String areaQuFen;
        private List<CameraConfigResponse.VertexInfo.Point> points; // 顶点坐标列表

        @Data
        public static class Point {
            private String x; // X 坐标
            private String y; // Y 坐标
            private String floor;
        }
    }

}
