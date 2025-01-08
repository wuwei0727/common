package com.tgy.rtls.docking.service.park;

import com.tgy.rtls.docking.dao.ParkingPlace;
import com.tgy.rtls.docking.dao.PlaceVideoDetection;
import com.tgy.rtls.docking.dao.PlaceVo;

import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.docking.service.park
 * @Author: wuwei
 * @CreateTime: 2023-08-21 16:51
 * @Description: TODO
 * @Version: 1.0
 */
public interface PlaceVideoDetectionService {
    List<PlaceVideoDetection> getAllGuideScreenDeviceOrConditionQuery(Integer map);

    List<ParkingPlace> getAllPlaceByMap(Integer id,Integer maps);

    void getPlaceVideoDetectionData(String placeName);
    void getPlaceVideoDetectionDataq();

    void clearMap();

    Map<String, PlaceVo> getMap();
}
