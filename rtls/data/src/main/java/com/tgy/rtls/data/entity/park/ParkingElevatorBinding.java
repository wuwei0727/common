package com.tgy.rtls.data.entity.park;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.park
*@Author: wuwei
*@CreateTime: 2023-09-12 09:43
*@Description: TODO
*@Version: 1.0
*/

/**
 * 车位电梯关联表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParkingElevatorBinding implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String placeList;
    private String x;
    private String y;
    private String name;
    private String mapName;
    private String placeName;
    private Integer map;
    private String place;
    private String building;
    private String fid;
    private Integer floor;

    //蜂鸟地图相关
    private String mapKey;
    private String appName;
    private String fmapID;
    private String mapImg;//地图路径
    private String themeImg;//主题路径


    private String ids;
    private String placeId;
    private String objectType;
    private String iconType;
    private String floorName;
}
