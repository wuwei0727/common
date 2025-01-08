package com.tgy.rtls.data.entity.park.floorLock;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.park.floorLock
*@Author: wuwei
*@CreateTime: 2024-07-18 14:29
*@Description: TODO
*@Version: 1.0
*/
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "user_company_map")
public class UserCompanyMap extends BaseEntitys implements Serializable {
    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "user_name")
    private String userName="微信用户";
    @TableField(value = "license_plate_id")
    private Long licensePlateId;

    @TableField(value = "license_plate")
    private String licensePlate;

    @TableField(value = "phone")
    private String phone;

    @TableField(value = "map_id", updateStrategy = FieldStrategy.IGNORED)
    private Long mapId;

    @TableField(value = "map_name", updateStrategy = FieldStrategy.IGNORED)
    private String mapName;

    @TableField(value = "company_id", updateStrategy = FieldStrategy.IGNORED)
    private Long companyId;

    @TableField(value = "company_name", updateStrategy = FieldStrategy.IGNORED)
    private String companyName;

    private static final long serialVersionUID = 1L;
}