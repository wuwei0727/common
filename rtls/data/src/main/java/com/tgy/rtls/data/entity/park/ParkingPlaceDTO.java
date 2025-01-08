package com.tgy.rtls.data.entity.park;

import lombok.Data;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.park
 * @Author: wuwei
 * @CreateTime: 2024-07-19 11:24
 * @Description: TODO
 * @Version: 1.0
 */
@Data
public class ParkingPlaceDTO {
    private Integer id;
    private String name;
    private Double x;
    private Double y;
    private Integer floor;
    private String type;
    private String fid;
    private Integer state;
    private Integer carbittype;
}
