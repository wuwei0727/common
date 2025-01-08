package com.tgy.rtls.data.service.vip;

import com.tgy.rtls.data.entity.vip.ParkingInfoStatistics;

public interface ParkingInfoStatisticsService{
    void addParkingInfoStatisticsfindCar(ParkingInfoStatistics parkingInfoStatistics);
    void addParkingInfoStatisticsRecommend(ParkingInfoStatistics parkingInfoStatistics);
    void addParkingInfoStatisticsUse(ParkingInfoStatistics parkingInfoStatistics);

}
