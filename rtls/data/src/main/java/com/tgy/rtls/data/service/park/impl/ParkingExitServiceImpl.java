package com.tgy.rtls.data.service.park.impl;

import com.tgy.rtls.data.entity.park.ParkingExit;
import com.tgy.rtls.data.mapper.park.ParkingExitMapper;
import com.tgy.rtls.data.service.park.ParkingExitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ParkingExitServiceImpl implements ParkingExitService{
    @Autowired
    private ParkingExitMapper parkingExitMapper;

    @Override
    public List<ParkingExit> getPlaceExit(String name, Integer map, Integer type, String floorName, String desc, String[] mapids) {
        return parkingExitMapper.getPlaceExit(name,map,type,floorName,desc,mapids);
    }

    @Override
    public int delPlaceExit(String[] ids) {
        return parkingExitMapper.delPlaceExit(ids);
    }

    @Override
    public ParkingExit getPlaceExitById(Integer id) {
        return parkingExitMapper.getPlaceExitById(id);
    }

    @Override
    public void addPlaceExit(ParkingExit parkingExit) {
        parkingExitMapper.addPlaceExit(parkingExit);
    }
    @Override
    public void editPlaceExitById(ParkingExit parkingExit) {
        parkingExitMapper.editPlaceExitById(parkingExit);
    }
}
