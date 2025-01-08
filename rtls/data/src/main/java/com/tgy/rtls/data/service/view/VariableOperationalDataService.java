package com.tgy.rtls.data.service.view;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.view.UserVo;
import com.tgy.rtls.data.entity.view.VariableOperationalData;
import com.tgy.rtls.data.entity.vo.*;

import java.util.List;

/**
 *@author wuwei
 *@date 2024/3/25 - 12:02
 */
public interface VariableOperationalDataService extends IService<VariableOperationalData>{

    List<UserStatisticsVo> getAllUserData(Integer map,String time,String start,String end);
    List<HotLocationVo> getHotLocationData(Integer map,String time,String start,String end);

    List<DevicesVo> getDevicesData(Integer map, String time,String start,String end);
    List<ParkingReservationVo> getParkingReservationData(Integer map, String time,String start,String end);

    List<ReverseCarSearchVo> getReverseCarSearchData(Integer map, String time,String start,String end);

    List<ParkingDataVo> getParkingData(Integer map, String time,String start,String end);

    List<ParkingUsageVo> getParkingUsageData(Integer map, String time,Integer monthSecond,String start,String end);

    void deleteRecordsOlderThan(int minutes);
    long countNewUsers(List<UserVo> userTotalList, String map, String time, String start, String end);
}
