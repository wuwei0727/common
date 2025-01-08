package com.tgy.rtls.data.service.park.floorLock.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.park.floorLock.PlaceUnlockRecords;

import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.park.floorLock.impl
 * @Author: wuwei
 * @CreateTime: 2024-07-16 17:00
 * @Description: TODO
 * @Version: 1.0
 */
public interface PlaceUnlockRecordsService extends IService<PlaceUnlockRecords> {


    int insertSelective(PlaceUnlockRecords record);

    int updateByPrimaryKeySelective(PlaceUnlockRecords record);

    List<PlaceUnlockRecords> getPlaceUnlockRecords(Integer map, String companyId, String placeName, String licensePlate, String phone, String desc, String floorLockState, String[] mapids);

    PlaceUnlockRecords getPlaceUnlockRecordsById(Integer id);
}
