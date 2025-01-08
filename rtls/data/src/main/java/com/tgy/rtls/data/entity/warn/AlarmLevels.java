package com.tgy.rtls.data.entity.warn;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tgy.rtls.data.entity.park.floorLock.BaseEntitys;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.warn
*@Author: wuwei
*@CreateTime: 2024-11-06 11:11
*@Description: TODO
*@Version: 1.0
*/
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "alarm_levels")
public class AlarmLevels extends BaseEntitys implements Serializable {
    @TableField(value = "`name`")
    private String name;

    @TableField(value = "code")
    private String code;

    @TableField(value = "description")
    private String description;

    private static final long serialVersionUID = 1L;
}