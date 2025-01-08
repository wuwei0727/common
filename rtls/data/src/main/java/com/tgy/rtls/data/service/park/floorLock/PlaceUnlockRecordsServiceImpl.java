package com.tgy.rtls.data.service.park.floorLock;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.park.floorLock.PlaceUnlockRecords;
import com.tgy.rtls.data.mapper.park.floorLock.PlaceUnlockRecordsMapper;
import com.tgy.rtls.data.service.park.floorLock.impl.PlaceUnlockRecordsService;
import org.springframework.stereotype.Service;

import java.util.List;
/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.park.floorLock
*@Author: wuwei
*@CreateTime: 2024-07-16 17:00
*@Description: TODO
*@Version: 1.0
*/
@Service
public class PlaceUnlockRecordsServiceImpl extends ServiceImpl<PlaceUnlockRecordsMapper, PlaceUnlockRecords> implements PlaceUnlockRecordsService{

    @Override
    public int insertSelective(PlaceUnlockRecords record) {
        return baseMapper.insertSelective(record);
    }
    @Override
    public int updateByPrimaryKeySelective(PlaceUnlockRecords record) {
        return baseMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<PlaceUnlockRecords> getPlaceUnlockRecords(Integer map, String companyId, String placeName, String licensePlate, String phone, String desc, String floorLockState, String[] mapids) {
        return baseMapper.getPlaceUnlockRecords(map, companyId,placeName, licensePlate, phone, desc,floorLockState,mapids);
    }

    @Override
    public PlaceUnlockRecords getPlaceUnlockRecordsById(Integer id) {
        return baseMapper.getPlaceUnlockRecordsById(id);
    }
}
