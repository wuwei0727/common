package com.tgy.rtls.data.service.warn;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.view.ViewVo2;
import com.tgy.rtls.data.entity.warn.ParkingAlertConfig;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.warn.impl
*@Author: wuwei
*@CreateTime: 2024-10-21 15:54
*@Description: TODO
*@Version: 1.0
*/
public interface ParkingAlertConfigService extends IService<ParkingAlertConfig>{
    List<ParkingAlertConfig> getAllOrFilteredParkingAlertConfig(String configName, String status, String map, String desc, String[] mapids);

    ParkingAlertConfig getParkingAlertConfigById(int id);
    List<ViewVo2> getBasicPlaceStatistics(Integer t2Hours, Integer threshold);
    List<ViewVo2> getChangedPlacesInT2Period(Integer mapId,Integer t2Hours,Integer threshold, String timeUnit);
    List<ViewVo2> getChangedPlacesInT3Period(Integer mapId,Integer t3Hours, Integer threshold,String timeUnit);

}
