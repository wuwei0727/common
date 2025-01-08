package com.tgy.rtls.data.mapper.park;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.park.PlaceVideoDetection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.mapper.park
*@Author: wuwei
*@CreateTime: 2023-05-31 15:58
*@Description: TODO
*@Version: 1.0
*/
@Mapper
public interface PlaceVideoDetectionMapper extends BaseMapper<PlaceVideoDetection> {
    List<PlaceVideoDetection> getAllGuideScreenDeviceOrConditionQuery(@Param("map") String map, @Param("ip") String ip, @Param("placeInquireAddress") String placeInquireAddress, @Param("licenseInquireAddress") String licenseInquireAddress, @Param("desc") String desc, @Param("status") String status, @Param("mapids") String[] mapids);

    void addPlaceVideoDetection(PlaceVideoDetection placeVideoDetection);

    void updatePlaceVideoDetection(PlaceVideoDetection placeVideoDetection);

    void delPlaceVideoDetection(String id);

    List<PlaceVideoDetection> getPlaceVideoDetectionById(Integer id);

    List<PlaceVideoDetection> getPlaceVideoDetectiontByMap(@Param("map") String map, @Param("id") String id,@Param("update") String update);

    List<ParkingPlace> getAllPlaceByMap(@Param("map") String maps, @Param("placeId") String placeId);

    List<PlaceVideoDetection> getAllGuideScreenDeviceTest(@Param("map") String map);

    List<ParkingPlace> getAllPlaceByMapTest(@Param("map") String maps);

    ParkingPlace getPlaceByPlaceNamesTest(@Param("mapId")String mapId,@Param("placeName") String placeName);

}