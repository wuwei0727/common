package com.tgy.rtls.data.mapper.park;

import com.tgy.rtls.data.entity.park.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 车位使用记录查询
 */
public interface ParkingRecordMapper {

    List<PlaceUseRecordData> findPlaceUserRecordByTime(@RequestParam("map") Integer map, @RequestParam("start") String start, @RequestParam("end") String end, @RequestParam("content")String content);
    List<PlaceChargeRecordData> findPlaceChargeRecordByTime(@RequestParam("map") Integer map, @RequestParam("start") String start, @RequestParam("end") String end, @RequestParam("content")String content);
    //List<Object> getPlaceUseRecordRange(@Param("start")String start, @Param("end") String end, @Param("map") Integer map, @Param("content") String content);
    FeeAndFlow getPlaceMapFeeAndFlow(@RequestParam("map") Integer map, @RequestParam("start") String start, @RequestParam("end") String end);


    FeeCalcul findFeeMap(@Param("map") Integer map,String start,String end);



}
