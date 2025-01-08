package com.tgy.rtls.data.mapper.park;

import com.tgy.rtls.data.entity.park.ParkingLotCost;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.mapper.park
*@Author: wuwei
*@CreateTime: 2023-11-06 11:52
*@Description: TODO
*@Version: 1.0
*/
public interface ParkingLotCostMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ParkingLotCost record);

    int insertSelective(ParkingLotCost record);

    ParkingLotCost getParkLotCostById(@Param("id") Integer id, @Param("map") Integer map, @Param("desc") String desc);

    int updateByPrimaryKeySelective(ParkingLotCost record);

    int updateByPrimaryKey(ParkingLotCost record);

    List<ParkingLotCost> selectAllByMap(@Param("map")Integer map,  @Param("mapids")String[] mapids);

    int deleteByIdIn(@Param("ids")String[] ids);




}



