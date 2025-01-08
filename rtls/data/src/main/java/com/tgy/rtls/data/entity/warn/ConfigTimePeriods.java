package com.tgy.rtls.data.entity.warn;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalTime;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.warn
*@Author: wuwei
*@CreateTime: 2024-11-05 10:38
*@Description: TODO
*@Version: 1.0
*/
/**
 * 报警配置时间段表，用于存储每个配置的多组时段
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "config_time_periods")
public class ConfigTimePeriods implements Serializable {
    /**
     * 关联到原来的报警配置表的ID
     */
    @TableField(value = "config_id")
    private Long configId;

    /**
     * 时段开始时间
     */
    @TableField(value = "start_time")
    private LocalTime startTime;

    /**
     * 时段结束时间
     */
    @TableField(value = "end_time")
    private LocalTime endTime;
    @TableField(value = "period_type")
    private String periodType;

    private static final long serialVersionUID = 1L;
}