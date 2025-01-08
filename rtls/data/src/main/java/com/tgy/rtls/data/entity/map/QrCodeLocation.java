package com.tgy.rtls.data.entity.map;

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
*@BelongsPackage: com.tgy.rtls.data.entity.map
*@Author: wuwei
*@CreateTime: 2024-12-16 10:03
*@Description: TODO
*@Version: 1.0
*/
/**
 * 二维码位置信息表
 */
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "qr_code_location")
public class QrCodeLocation extends BaseEntitys implements Serializable {
    /**
     * 区域名称
     */
    @TableField(value = "area_name")
    private String areaName;

    /**
     * 停车场名称
     */
    @TableField(value = "`map`")
    private String map;

    @TableField(value = "`floor`")
    private String floor;

    /**
     * 二维码图片URL
     */
    @TableField(value = "qr_code_url")
    private String qrCodeUrl;

    @TableField(value = "area_info")
    private String areaInfo;


    @TableField(exist = false)
    private String mapName;
    @TableField(exist = false)
    private String floorName;
    @TableField(exist = false)
    private String mapKey;
    @TableField(exist = false)
    private String appName;
    @TableField(exist = false)
    private String fmapID;

    private static final long serialVersionUID = 1L;
}