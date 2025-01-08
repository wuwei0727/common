package com.tgy.rtls.data.entity.vip;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
    * 车位信息统计表
    */
@ApiModel(description="车位信息统计表")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParkingInfoStatistics implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    @ApiModelProperty(value="地图Id")
    private Long map;

    /**
     * 车位Id
     */
    @ApiModelProperty(value="车位Id")
    private Long place;

    /**
     * 车位名称
     */
    @ApiModelProperty(value="车位名称")
    private String placename;

    /**
     * 开始时间
     */
    @ApiModelProperty(value="开始时间")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty(value="结束时间")
    private LocalDateTime endTime;

    /**
     * 区分:use使用；findCar寻车；recommend推荐
     */
    @ApiModelProperty(value="区分:use使用；findCar寻车；recommend推荐")
    private String desc;

    private static final long serialVersionUID = 1L;
}