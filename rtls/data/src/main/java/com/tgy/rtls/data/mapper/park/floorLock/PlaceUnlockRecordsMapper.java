package com.tgy.rtls.data.mapper.park.floorLock;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.park.floorLock.PlaceUnlockRecords;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.mapper.park.floorLock
*@Author: wuwei
*@CreateTime: 2024-07-16 17:00
*@Description: TODO
*@Version: 1.0
*/
public interface PlaceUnlockRecordsMapper extends BaseMapper<PlaceUnlockRecords> {
    int insertSelective(PlaceUnlockRecords record);

    int updateByPrimaryKeySelective(PlaceUnlockRecords record);

    List<PlaceUnlockRecords> getPlaceUnlockRecords(@Param("map") Integer map, @Param("companyId") String companyId, @Param("placeName") String placeName, @Param("licensePlate") String licensePlate, @Param("phone") String phone, @Param("desc") String desc, @Param("floorLockState") String floorLockState, @Param("mapids") String[] mapids);

    PlaceUnlockRecords getPlaceUnlockRecordsById(@Param("id") Integer id);
}