package com.tgy.rtls.data.mapper.park.floorLock;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.park.floorLock.CarPlate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.mapper.park.floorLock
 * @Author: wuwei
 * @CreateTime: 2024-07-17 15:49
 * @Description: TODO
 * @Version: 1.0
 */
public interface CarPlateMapper extends BaseMapper<CarPlate> {
    List<CarPlate> getCarPlate(@Param("map") Integer map, @Param("companyId") String companyId, @Param("licensePlate") String licensePlate, @Param("phone") String phone, @Param("desc") String desc, @Param("mapids") String[] mapids);

    CarPlate getCarPlateById(@Param("id") Integer id);
}