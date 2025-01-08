package com.tgy.rtls.data.entity.park;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.park
*@Author: wuwei
*@CreateTime: 2023-11-06 11:52
*@Description: TODO
*@Version: 1.0
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParkingLotCost implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private Integer map;
    private Double cost;
    private String desc;
    private Double evaluate= 5.0;
    private String mapName;
}