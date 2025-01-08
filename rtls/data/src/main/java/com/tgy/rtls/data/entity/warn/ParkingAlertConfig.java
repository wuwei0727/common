package com.tgy.rtls.data.entity.warn;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tgy.rtls.data.entity.park.floorLock.BaseEntitys;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.warn
 * @Author: wuwei
 * @CreateTime: 2024-10-21 15:54
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "parking_alert_config")
public class ParkingAlertConfig extends BaseEntitys implements Serializable {
    @TableField(value = "`map`")
    private Integer map;

    /**
     * 配置名称
     */
    @TableField(value = "config_name")
    private String configName;

    /**
     * 车位变化时间阈值 t1，单位为小时，超过该时间车位未更新则触发报警
     */
    @TableField(value = "t1_threshold_hours")
    private Integer t1ThresholdHours;

    /**
     * 监控的时间段 t2，单位为小时，用于批量异动检测
     */
    @TableField(value = "t2_period_hours")
    private Integer t2PeriodHours;

    /**
     * t2 时间段内车位变动数 n，当车位变化超过 n 个时生成报警
     */
    @TableField(value = "t2_slot_change_limit")
    private Integer t2SlotChangeLimit;

    /**
     * 节假日监控的时间段t2，单位为小时
     */
    @TableField(value = "holiday_t2_period_hours")
    private Integer holidayT2PeriodHours;

    /**
     * 节假日t2时间段内车位变动数限制
     */
    @TableField(value = "holiday_t2_slot_change_limit")
    private Integer holidayT2SlotChangeLimit;

    /**
     * 监控的时间段 t3，单位为小时，用于统计车位使用频率
     */
    @TableField(value = "t3_period_hours")
    private Integer t3PeriodHours;

    /**
     * t3 时间段内车位使用次数 k，当使用次数超过该值时生成报警
     */
    @TableField(value = "t3_slot_usage_limit")
    private Integer t3SlotUsageLimit;

    /**
     * 节假日监控的时间段t3，单位为小时
     */
    @TableField(value = "holiday_t3_period_hours")
    private Integer holidayT3PeriodHours;

    /**
     * 节假日t3时间段内车位使用次数限制
     */
    @TableField(value = "holiday_t3_slot_change_limit")
    private Integer holidayT3SlotChangeLimit;

    /**
     * 配置状态，启用(1)或关闭(0)
     */
    @TableField(value = "`status`")
    private Integer status;

    // 非数据库字段
    @TableField(exist = false)
    private String mapName;

    @TableField(exist = false)
    private List<String> t2TimePeriods;  // 用于存储 t2 的时间段

    @TableField(exist = false)
    private List<String> t3TimePeriods;  // 用于存储 t3 的时间段

    @TableField(exist = false)
    private String t2TimePeriods1;  // 用于存储 t2 的时间段

    @TableField(exist = false)
    private String t3TimePeriods1;  // 用于存储 t3 的时间段

    @TableField(exist = false)
    private String startTime;

    @TableField(exist = false)
    private String endTime;

    private static final long serialVersionUID = 1L;
}