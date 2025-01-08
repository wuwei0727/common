package com.tgy.rtls.data.entity.warn;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tgy.rtls.data.entity.park.floorLock.BaseEntitys;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.warn
*@Author: wuwei
*@CreateTime: 2024-10-29 16:01
*@Description: TODO
*@Version: 1.0
*/
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "alarm_personnel_bindings")
public class AlarmPersonnelBindings extends BaseEntitys implements Serializable {
    @TableField(value = "`map`")
    private String map;


    @TableField(value = "maintenance_staff_id")
    private String maintenanceStaffId;

    @TableField(exist = false)
    private String level;

    @TableField(exist = false)
    private String alarmTypeId;
    @TableField(exist = false)
    private String mapName;
    @TableField(exist = false)
    private String name;
    @TableField(exist = false)
    private String alarmTypeNames;
    @TableField(exist = false)
    private String maintenanceStaffNames;
    @TableField(exist = false)
    private String levelNames;
    @TableField(exist = false)
    private String phone;
    private Integer configId;
    private static final long serialVersionUID = 1L;

}