package com.tgy.rtls.data.entity.video;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.video
*@Author: wuwei
*@CreateTime: 2023-12-13 11:01
*@Description: TODO
*@Version: 1.0
*/
@ApiModel(description="video_place_status")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoPlaceStatus {
    @TableId(type = IdType.AUTO)
    private Integer id;


    /**
    * 车位编号名称
    */
    private String name;

    /**
    * 地图id
    */
    private Integer place;
    private Integer map;

    /**
    * 配置方式1、超声2、视频3、超声+视频
    */
    private Integer configWay;

    /**
    * 占用状态0 未占用 1已使用 2已预约
    */
    private Short state;

    /**
    * 车牌号
    */
    private String license;

    /**
    * 添加时间
    */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("addTime")
    private LocalDateTime addTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("updateTime")
    private LocalDateTime updateTime;
}