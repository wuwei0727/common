package com.tgy.rtls.data.service.park;

import com.tgy.rtls.data.entity.park.ParkingLotCost;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.park.impl
*@Author: wuwei
*@CreateTime: 2023-11-06 11:52
*@Description: TODO
*@Version: 1.0
*/
public interface ParkingLotCostService{


    int deleteByPrimaryKey(Integer id);

    int insert(ParkingLotCost record);

    int insertSelective(ParkingLotCost record);

    ParkingLotCost getParkLotCostById(Integer id, Integer map,String desc);

    int updateByPrimaryKeySelective(ParkingLotCost record);

    int updateByPrimaryKey(ParkingLotCost record);

    List<ParkingLotCost> selectAllByMap(Integer map, String[] mapids);
	int deleteByIdIn(String[] ids);




}
