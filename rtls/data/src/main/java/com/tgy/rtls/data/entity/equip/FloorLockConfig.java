package com.tgy.rtls.data.entity.equip;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.park
*@Author: wuwei
*@CreateTime: 2024-06-06 14:24
*@Description: TODO
*@Version: 1.0
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "floor_lock_config")
public class FloorLockConfig implements Serializable{
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "map")
    private Integer map;
    @TableField(exist = false)
    private String mapName;

    @TableField(value = "call_code")
    private String callCode;

    @TableField(value = "data_interface")
    private String dataInterface;

    @TableField(value = "valid_start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime validStartTime;

    @TableField(value = "valid_end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime validEndTime;

    @TableField(value = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}