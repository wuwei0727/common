package com.tgy.rtls.data.mapper.park;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.park.ParkingElevatorBinding;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.mapper.park
 * @Author: wuwei
 * @CreateTime: 2023-09-12 09:43
 * @Description: TODO
 * @Version: 1.0
 */
public interface ParkingElevatorBindingMapper extends BaseMapper<ParkingElevatorBinding> {

    List<ParkingElevatorBinding> getByConditions(@Param("name") String name, @Param("map") Integer map, @Param("building") String building, @Param("floor") Integer floor, @Param("placeName") String placeName, @Param("desc") String desc, @Param("floorName") String floorName, @Param("objectType") String objectType, @Param("maps") String[] maps, @Param("fid") String fid);

    Boolean addParkingElevatorBinding(ParkingElevatorBinding peb);

    boolean updateParkingElevatorBinding(ParkingElevatorBinding peb);

    List<ParkingElevatorBinding> getParkingElevatorBindingById(@Param("id") String id);

    boolean deleteByPrimaryKey(String id);
}
