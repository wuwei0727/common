package com.tgy.rtls.data.entity.equip;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.equip
 * @Author: wuwei
 * @CreateTime: 2023-12-26 14:51
 * @Description: TODO
 * @Version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceAlarmsVo extends DeviceAlarms implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer deviceTypeId;
    private Integer alarmsTypeId;
    private String mapName;
    private Integer num;
    private String deviceName;
    private String alarmName;
    private String placeName;
    @TableField(exist = false)
    private String x;
    @TableField(exist = false)
    private String y;
    @TableField(exist = false)
    private String floor;
}