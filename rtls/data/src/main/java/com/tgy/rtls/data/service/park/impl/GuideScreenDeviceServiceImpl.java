package com.tgy.rtls.data.service.park.impl;

import com.tgy.rtls.data.entity.park.GuideScreenDevice;
import com.tgy.rtls.data.mapper.park.GuideScreenDeviceMapper;
import com.tgy.rtls.data.service.park.GuideScreenDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.park.impl
 * @Author: wuwei
 * @CreateTime: 2023-05-24 23:12
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class GuideScreenDeviceServiceImpl implements GuideScreenDeviceService {
    @Autowired
    private GuideScreenDeviceMapper guideScreenDeviceMapper;
    @Override
    public List<GuideScreenDevice> getAllGuideScreenDeviceOrConditionQuery(String map, String deviceNum, String ip, String locationName, String networkStatus, String desc, String floorName, String[] mapids) {
        return guideScreenDeviceMapper.getAllGuideScreenDeviceOrConditionQuery(map, deviceNum,ip, locationName,networkStatus,desc,floorName,mapids);
    }

    @Override
    public void addGuideScreenDevice(GuideScreenDevice guideScreenDevice) {
        guideScreenDeviceMapper.addGuideScreenDevice(guideScreenDevice);
    }

    @Override
    public void delGuideScreenDevice(String[] split) {
        for (String id : split) {
            guideScreenDeviceMapper.updateByDeviceIdShowScreenConfigIsNull(id);
            guideScreenDeviceMapper.delGuideScreenDevice(id);
        }
    }

    @Override
    public void updateGuideScreenDevice(GuideScreenDevice guideScreenDevice) {
        guideScreenDeviceMapper.updateGuideScreenDevice(guideScreenDevice);
    }

    @Override
    public List<GuideScreenDevice> getGuideScreenDeviceById(Integer id,String deviceId,String desc) {
        return guideScreenDeviceMapper.getGuideScreenDeviceById(id,deviceId,desc);
    }
}
