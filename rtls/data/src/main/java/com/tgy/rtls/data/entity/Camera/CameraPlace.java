package com.tgy.rtls.data.entity.Camera;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tgy.rtls.data.entity.park.floorLock.BaseEntitys;
import lombok.*;

import java.io.Serializable;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.Camera
*@Author: wuwei
*@CreateTime: 2024-11-25 17:52
*@Description: TODO
*@Version: 1.0
*/
/**
 * 摄像头车位关联表
 */
@Data
@Builder
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "camera_place")
public class CameraPlace extends BaseEntitys implements Serializable {
    /**
     * 摄像头捕获记录ID
     */
    @TableField(value = "camera_vehicle_capture_id")
    private Long cameraVehicleCaptureId;

    /**
     * 车位ID
     */
    @TableField(value = "place_id")
    private Long placeId;

    private static final long serialVersionUID = 1L;
}