package com.tgy.rtls.data.entity.user;

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
*@BelongsPackage: com.tgy.rtls.data.entity.user
*@Author: wuwei
*@CreateTime: 2024-10-29 10:04
*@Description: TODO
*@Version: 1.0
*/
/**
 * 运维人员表
 */
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "maintenance_staff")
public class MaintenanceStaff extends BaseEntitys implements Serializable {
    /**
     * 运维人员姓名
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 手机号码
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 手机号码
     */
    @TableField(value = "`map`")
    private String map;

    /**
     * 状态：1-启用，0-禁用
     */
    @TableField(value = "`status`")
    private Integer status;

    @TableField(exist = false)
    private String mapName;
    private static final long serialVersionUID = 1L;
}