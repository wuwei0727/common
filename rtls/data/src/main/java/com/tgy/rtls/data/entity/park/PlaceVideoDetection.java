package com.tgy.rtls.data.entity.park;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.park
*@Author: wuwei
*@CreateTime: 2023-05-31 15:58
*@Description: TODO
*@Version: 1.0
*/
/**
    * 车位视频检测表
    */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "place_video_detection")
public class PlaceVideoDetection implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 地图id
     */
    @TableField(value = "`map`")
    private Long map;
    @TableField(exist = false)
    private String mapName;

    /**
     * 网关ip
     */
    @TableField(value = "ip")
    private String ip;

    /**
     * 车位查询地址
     */
    @TableField(value = "place_Inquire_address")
    private String placeInquireAddress;
    private String photo;
    private String photolocal;

    /**
     * 车牌查询地址
     */
    @TableField(value = "license_Inquire_address")
    private String licenseInquireAddress;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "addTime")
    private LocalDateTime addTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "updateTime")
    private LocalDateTime updateTime;
    private Integer status;
    private Integer serviceStatusTime;
    private static final long serialVersionUID = 1L;
}