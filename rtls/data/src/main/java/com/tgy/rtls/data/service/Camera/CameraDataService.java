package com.tgy.rtls.data.service.Camera;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.Camera.CameraData;
import com.tgy.rtls.data.entity.park.PlaceVo;
import java.util.List;

public interface CameraDataService extends IService<PlaceVo> {

    List<CameraData> gateLicense(List<CameraData> cameraList);
    void updateBatch(List<PlaceVo> places);
}
