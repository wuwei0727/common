package com.tgy.rtls.data.mapper.warn;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.warn.WhitelistSlots;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.mapper.warn
*@Author: wuwei
*@CreateTime: 2024-10-25 09:23
*@Description: TODO
*@Version: 1.0
*/
public interface WhitelistSlotsMapper extends BaseMapper<WhitelistSlots> {
    List<WhitelistSlots> getAllOrFilteredWhitelist(@Param("configName") String configName, @Param("status") String status, @Param("map") String map, @Param("mapId") String mapId, @Param("desc") String desc, @Param("mapids") String[] mapids);

    WhitelistSlots getWhitelistById(int id);



}