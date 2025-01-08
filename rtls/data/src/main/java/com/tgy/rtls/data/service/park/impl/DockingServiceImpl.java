package com.tgy.rtls.data.service.park.impl;

import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.park.PlaceVo;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.service.park.DockingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DockingServiceImpl implements DockingService {

    @Autowired
    private ParkMapper parkMapper;

    @Override
    public Integer updatePlaceByIdMaster(Integer placeId, Short state, String license) {
        return parkMapper.updatePlaceByTest(placeId, state,license);
    }

    @Override
    public Integer updatePlaceByIdSlave(Integer placeId, Short state, String license) {
        return parkMapper.updatePlaceByTest(placeId, state,license);
    }

    @Override
    public void updatePlaceTest(ParkingPlace parkingPlace) {
        parkMapper.updatePlaceTest(parkingPlace);
    }

    @Override
    public Integer updatePlaceById(Integer placeId, Short state, String license,String map,String name,Integer detectionException, LocalDateTime exceptionTime) {
        return parkMapper.updatePlaceById(placeId, state,license,map,name,detectionException, exceptionTime);
    }

    @Override
    public Integer updateBatchById(List<PlaceVo> placeVoList) {
        return parkMapper.updateBatchById(placeVoList);
    }

    @Override
    public void updateBatchById1(PlaceVo user) {
        parkMapper.updateBatchById1(user);
    }
}
