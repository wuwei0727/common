package com.tgy.rtls.data.mapper.Camera;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.Camera.CameraData;
import com.tgy.rtls.data.entity.park.PlaceVo;

import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.CameraServiceImpl
 * @Author: wuwei
 * @CreateTime: 2023-03-31 14:23
 * @Description: TODO
 * @Version: 1.0
 */

public interface CameraDataMapper extends BaseMapper<PlaceVo> {

    List<CameraData> gateLicense(List<CameraData> cameraList);
}
