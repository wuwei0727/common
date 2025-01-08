package com.tgy.rtls.data.mapper.vip;

import com.tgy.rtls.data.entity.vip.ParkingInfoStatistics;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ParkingInfoStatisticsMapper{
    void addParkingInfoStatisticsfindCar(ParkingInfoStatistics parkingInfoStatistics);
    void addParkingInfoStatisticsRecommend(ParkingInfoStatistics parkingInfoStatistics);
    void addParkingInfoStatisticsUse(ParkingInfoStatistics parkingInfoStatistics);
}