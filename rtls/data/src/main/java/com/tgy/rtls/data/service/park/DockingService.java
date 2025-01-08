package com.tgy.rtls.data.service.park;

import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.park.PlaceVo;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DockingService {

    Integer updatePlaceByIdMaster(Integer placeId, Short state, String license);
    Integer updatePlaceByIdSlave(Integer placeId, Short state, String license);

    Integer updatePlaceById(Integer placeId,Short state, String license,String map,String name,Integer detectionException, LocalDateTime exceptionTime);

    void updatePlaceTest(@Param("parkingPlace") ParkingPlace parkingPlace);

    Integer updateBatchById(List<PlaceVo> placeVoList);


    void updateBatchById1(PlaceVo user);
}
