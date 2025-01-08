package com.tgy.rtls.data.entity.view;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("p_findcar")
@AllArgsConstructor
@NoArgsConstructor
public class PFindCar implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    /**
     * 地图Id
     */
    private Long map;

    /**
     * 车位Id
     */
    private Long place;

    /**
     * 车位名称
     */
    @TableField("placeName")
    private String placeName;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    @TableField("timestamp")
    private LocalDateTime timestamp;
    private String name;

    private static final long serialVersionUID = 1L;

}

