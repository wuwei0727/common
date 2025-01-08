package com.tgy.rtls.data.mapper.park.floorLock;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.park.floorLock.UserCompanyMap;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.mapper.park.floorLock
*@Author: wuwei
*@CreateTime: 2024-07-18 14:29
*@Description: TODO
*@Version: 1.0
*/
public interface UserCompanyMapMapper extends BaseMapper<UserCompanyMap> {
    List<UserCompanyMap> getUserCompanyMap(@Param("map") Integer map, @Param("companyId") Integer companyId, @Param("userName") String userName, @Param("phone") String phone, @Param("licensePlate") String licensePlate, @Param("desc") String desc, @Param("mapids") String[] mapids);

}