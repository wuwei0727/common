package com.tgy.rtls.docking.service.park;

import org.apache.ibatis.annotations.Param;

public interface DockingService {
    Integer updatePlaceById(@Param("placeId")Integer placeId, @Param("state") Integer state, String license);
}
