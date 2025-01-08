package com.tgy.rtls.data.entity.warn;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.warn
*@Author: wuwei
*@CreateTime: 2024-11-06 15:36
*@Description: TODO
*@Version: 1.0
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "alarm_personnel_level_mappings")
public class AlarmPersonnelLevelMappings implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "binding_id")
    private Long bindingId;

    @TableField(value = "level_id")
    private Integer levelId;

    private static final long serialVersionUID = 1L;
}