package com.tgy.rtls.data.service.park;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.park.ParkingElevatorBinding;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.park.impl
*@Author: wuwei
*@CreateTime: 2023-09-12 09:36
*@Description: TODO
*@Version: 1.0
*/
public interface ParkingElevatorBindingService extends IService<ParkingElevatorBinding> {
    List<ParkingElevatorBinding> getByConditions(String name,Integer map, String building, Integer floor, String placeName, String desc, String floorName, String objectType, String[] maps,String fid);

    Boolean addParkingElevatorBinding(ParkingElevatorBinding peb);

    boolean updateParkingElevatorBinding(ParkingElevatorBinding peb);

    List<ParkingElevatorBinding> getParkingElevatorBindingById(String id);

    boolean delParkingElevatorBinding(String[] split, HttpServletRequest request, String[] placeId);

}
