package com.tgy.rtls.data.service.warn.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.view.ViewVo2;
import com.tgy.rtls.data.entity.warn.ParkingAlertConfig;
import com.tgy.rtls.data.mapper.warn.ParkingAlertConfigMapper;
import com.tgy.rtls.data.service.warn.ParkingAlertConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.warn
*@Author: wuwei
*@CreateTime: 2024-10-21 15:54
*@Description: TODO
*@Version: 1.0
*/
@Service
public class ParkingAlertConfigServiceImpl extends ServiceImpl<ParkingAlertConfigMapper, ParkingAlertConfig> implements ParkingAlertConfigService{
    @Autowired
    private ParkingAlertConfigMapper mapper;

    @Override
    public List<ParkingAlertConfig> getAllOrFilteredParkingAlertConfig(String configName, String status, String map, String desc, String[] mapids) {
        return mapper.getAllOrFilteredParkingAlertConfig(configName, status, map, desc, mapids);
    }

    @Override
    public ParkingAlertConfig getParkingAlertConfigById(int id) {
        return mapper.getParkingAlertConfigById(id);
    }

    @Override
    public List<ViewVo2> getBasicPlaceStatistics(Integer t2Hours, Integer threshold) {
        return baseMapper.getMapChangesInT2Period(t2Hours,threshold);
    }

    @Override
    public List<ViewVo2> getChangedPlacesInT2Period(Integer mapId,Integer t2Hours,Integer threshold, String timeUnit) {
        return baseMapper.getChangedPlacesInT2Period(mapId,t2Hours,threshold,timeUnit);
    }

    @Override
    public List<ViewVo2> getChangedPlacesInT3Period(Integer mapId,Integer t3Hours, Integer threshold,String timeUnit) {
        return baseMapper.getChangedPlacesInT3Period(mapId,t3Hours, threshold,timeUnit);
    }

}
