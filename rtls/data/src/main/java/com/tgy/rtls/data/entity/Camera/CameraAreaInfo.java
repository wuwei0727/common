package com.tgy.rtls.data.entity.Camera;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tgy.rtls.data.entity.park.floorLock.BaseEntitys;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.Camera
*@Author: wuwei
*@CreateTime: 2024-11-22 10:39
*@Description: TODO
*@Version: 1.0
*/
/**
 * 区域信息表
 */
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "camera_area_info")
public class CameraAreaInfo extends BaseEntitys implements Serializable {
    /**
     * 区域名称
     */
    @TableField(value = "area_name")
    private String areaName;

    /**
     * 地图名称
     */
    @TableField(value = "`map`")
    private String map;

    /**
     * 楼层信息
     */
    @TableField(value = "`floor`")
    private String floor;

    private static final long serialVersionUID = 1L;
}