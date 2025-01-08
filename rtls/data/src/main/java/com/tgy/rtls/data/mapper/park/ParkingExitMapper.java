package com.tgy.rtls.data.mapper.park;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.park.ParkingExit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wuwei
 */
@Mapper
public interface ParkingExitMapper extends BaseMapper<ParkingExit> {
    int batchInsert(@Param("list") List<ParkingExit> list);

    List<ParkingExit> getPlaceExit(@Param("name") String name, @Param("map") Integer map, @Param("type") Integer type, @Param("floorName") String floorName, @Param("desc") String desc, @Param("mapIds") String[] mapids);
    int delPlaceExit(@Param("ids") String[] ids);

    ParkingExit getPlaceExitById(Integer id);

    void addPlaceExit(ParkingExit parkingExit);

    void editPlaceExitById(ParkingExit parkingExit);
}