package com.tgy.rtls.data.service.vip;

import com.tgy.rtls.data.entity.vip.ParkingInfoStatistics;
import com.tgy.rtls.data.mapper.vip.ParkingInfoStatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParkingInfoStatisticsServiceImpl implements ParkingInfoStatisticsService {
    @Autowired
    private ParkingInfoStatisticsMapper parkingInfoStatisticsMapper;

    @Override
    public void addParkingInfoStatisticsfindCar(ParkingInfoStatistics parkingInfoStatistics) {
         parkingInfoStatisticsMapper.addParkingInfoStatisticsfindCar(parkingInfoStatistics);
    }

    @Override
    public void addParkingInfoStatisticsRecommend(ParkingInfoStatistics parkingInfoStatistics) {
         parkingInfoStatisticsMapper.addParkingInfoStatisticsRecommend(parkingInfoStatistics);
    }

    @Override
    public void addParkingInfoStatisticsUse(ParkingInfoStatistics parkingInfoStatistics) {
         parkingInfoStatisticsMapper.addParkingInfoStatisticsUse(parkingInfoStatistics);
    }
}
