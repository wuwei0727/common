package com.tgy.rtls.data.entity.park.floorLock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
*@Author: wuwei
*@CreateTime: 2024/7/23 10:26
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RequestDTO {
    private Long userId;
    private String phone;
    private String licensePlate;
    private Long mapId;
    private String mapName;
    private Long companyId;
    private String companyName;
    private Long placeId;
    private String code;
    private Integer type;
    private Long deviceId;
}