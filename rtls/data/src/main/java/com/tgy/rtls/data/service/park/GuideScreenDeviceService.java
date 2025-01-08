package com.tgy.rtls.data.service.park;

import com.tgy.rtls.data.entity.park.GuideScreenDevice;

import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.park
 * @Author: wuwei
 * @CreateTime: 2023-05-24 23:12
 * @Description: TODO
 * @Version: 1.0
 */
public interface GuideScreenDeviceService {
    List<GuideScreenDevice> getAllGuideScreenDeviceOrConditionQuery(String map, String deviceNum, String ip, String locationName, String networkStatus, String desc, String floorName, String[] mapids);

    void addGuideScreenDevice(GuideScreenDevice guideScreenDevice);

    void delGuideScreenDevice(String[] split);

    void updateGuideScreenDevice(GuideScreenDevice guideScreenDevice);

    List<GuideScreenDevice> getGuideScreenDeviceById(Integer id,String deviceId,String desc);
}
