package com.tgy.camerademo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.equip
 * @Author: wuwei
 * @CreateTime: 2023-12-26 14:51
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceAlarms implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 设备id
     */
    private Integer map;
    private Integer num;
    private Integer deviceId;

    private Integer equipmentType;
    /**
     * 1离线报警，2低电量报警
     */
    private Integer alarmType;
    private String serialNumber;

    /**
     * 1高，2中，3低
     */
    private Integer priority;

    /**
     * 0报警中，1结束
     */
    private Integer state;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    private static final long serialVersionUID = 1L;

    // private String deviceName;
    // private String alarmName;
}