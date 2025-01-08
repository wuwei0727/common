package com.tgy.rtls.data.entity.park.floorLock;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.park.floorLock
*@Author: wuwei
*@CreateTime: 2024-07-16 17:00
*@Description: TODO
*@Version: 1.0
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "place_unlock_records")
public class PlaceUnlockRecords extends BaseEntitys implements Serializable {
    @TableField(value = "place_id")
    private Integer placeId;
    @TableField(value = "place_name")
    private String placeName;

    @TableField(value = "license_plate")
    private String licensePlate;

    @TableField(value = "phone")
    private String phone;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "mapId")
    private Long mapId;

    @TableField(value = "map_name")
    private String mapName;

    @TableField(value = "unlock_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime unlockTime;

    @TableField(value = "parking_status")
    private String parkingStatus;
    @TableField(value = "company_id")
    private Long companyId;

    @TableField(value = "company_name")
    private String companyName;
    @TableField(value = "is_exclusive_user")
    private Integer isExclusiveUser;

    private static final long serialVersionUID = 1L;
}