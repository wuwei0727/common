package com.tgy.rtls.docking.mapper;

import com.tgy.rtls.docking.dao.ParkingPlace;
import com.tgy.rtls.docking.dao.PlaceVideoDetection;
import com.tgy.rtls.docking.dao.PlaceVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.docking.mapper
 * @Author: wuwei
 * @CreateTime: 2023-08-21 16:40
 * @Description: TODO
 * @Version: 1.0
 */
public interface PlaceMapper{
    List<PlaceVideoDetection> getAllGuideScreenDeviceOrConditionQuery(@Param("map") Integer map);

    List<ParkingPlace> getAllPlaceByMap(@Param("id") Integer id,@Param("map") Integer map);

    // Integer updatePlaceById(@Param("placeId")Integer placeId,@Param("state") Short state,@Param("license")String license);

    List<ParkingPlace> getAllPlace(@Param("map") Integer map, @Param("placeName") String placeName);

    Integer updatePlaceById(@Param("placeId") Integer placeId, @Param("state") Integer state, @Param("license") String license);
    Integer updateBatchById(@Param("placeVoList")List<PlaceVo> placeVoList);
    void batchInsertParkingPlaces(List<ParkingPlace> parkingPlaces);


}
