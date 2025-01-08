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
 * 区域车位表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "camera_parking_space")
public class CameraParkingSpace implements Serializable {
    /**
     * 区域ID，外键关联area_info
     */
    @TableField(value = "area_id")
    private Long areaId;

    /**
     * 车位名称，例如E437、E438
     */
    @TableField(value = "place_name")
    private String placeName;

    private static final long serialVersionUID = 1L;
}