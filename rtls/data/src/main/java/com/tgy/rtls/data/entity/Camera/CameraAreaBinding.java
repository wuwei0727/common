package com.tgy.rtls.data.entity.Camera;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tgy.rtls.data.entity.park.floorLock.BaseEntitys;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.Camera
*@Author: wuwei
*@CreateTime: 2024-11-22 10:39
*@Description: TODO
*@Version: 1.0
*/
/**
 * 摄像头与区域绑定表
 */
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "camera_area_binding")
public class CameraAreaBinding extends BaseEntitys implements Serializable {
    /**
     * 摄像头ID，外键关联camera_config
     */
    @TableField(value = "camera_id")
    private Long cameraId;

    /**
     * 区域ID，外键关联area_info
     */
    @TableField(value = "area_id")
    private Long areaId;

    private static final long serialVersionUID = 1L;
}