package com.tgy.rtls.data.entity.Camera;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
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
 * 区域顶点坐标表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "camera_coordinates")
public class CameraCoordinates implements Serializable {
    /**
     * 区域ID，外键关联area_info
     */
    @TableField(value = "area_id")
    private Long areaId;

    /**
     * X坐标
     */
    @TableField(value = "x")
    private String x;

    /**
     * Y坐标
     */
    @TableField(value = "y")
    private String y;

    private String floor;
    private String areaQuFen;
    private static final long serialVersionUID = 1L;

}