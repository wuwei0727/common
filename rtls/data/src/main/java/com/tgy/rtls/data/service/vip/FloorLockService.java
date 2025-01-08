package com.tgy.rtls.data.service.vip;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.park.ParkingCompanyVo;
import com.tgy.rtls.data.entity.vip.FloorLock;

import java.util.List;
public interface FloorLockService extends IService<FloorLock>{


    List<FloorLock> getFloorLockInfo(Long map, String deviceNum, String parkingName, Integer placeState, String desc, String floorName, Integer networkstate, Integer floorState, Integer state, String[] mapids);

    boolean addFloorLockInfo(FloorLock floorLock);

    void editFloorLockInfo(FloorLock floorLock);

    FloorLock getFloorLockInfoInfoById(Integer id);

    void delFloorLockInfo(String[] split);

    List<FloorLock> getConditionData(String deviceNum, Integer place, Integer id, Long map);
    List<FloorLock> getConditionDataById(String deviceNum, Integer place, Integer id, Long map);

    List<ParkingCompanyVo> getAllPlaceNameByMapId(String[] mapids, String type);
}
