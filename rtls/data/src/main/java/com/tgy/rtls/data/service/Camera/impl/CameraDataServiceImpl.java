package com.tgy.rtls.data.service.Camera.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.Camera.CameraData;
import com.tgy.rtls.data.entity.park.PlaceVo;
import com.tgy.rtls.data.mapper.Camera.CameraDataMapper;
import com.tgy.rtls.data.service.Camera.CameraDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.CameraServiceImpl
 * @Author: wuwei
 * @CreateTime: 2023-03-31 14:23
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class CameraDataServiceImpl extends ServiceImpl<CameraDataMapper, PlaceVo> implements CameraDataService {
    @Autowired
    private CameraDataMapper cameraDataMapper;




    @Override
    public List<CameraData> gateLicense(List<CameraData> cameraList) {
        return cameraDataMapper.gateLicense(cameraList);
    }

    @Transactional
    @Override
    public void updateBatch(List<PlaceVo> places) {
        this.updateBatchById(places);
    }
}
