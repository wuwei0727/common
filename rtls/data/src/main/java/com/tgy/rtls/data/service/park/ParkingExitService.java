package com.tgy.rtls.data.service.park;

import com.tgy.rtls.data.entity.park.ParkingExit;

import java.util.List;
public interface ParkingExitService{


    List<ParkingExit> getPlaceExit(String name, Integer map, Integer type, String floorName, String desc, String[] mapids);

    int delPlaceExit(String[] ids);

    ParkingExit getPlaceExitById(Integer id);

    void addPlaceExit(ParkingExit parkingExit);

    void editPlaceExitById(ParkingExit parkingExit);
}
