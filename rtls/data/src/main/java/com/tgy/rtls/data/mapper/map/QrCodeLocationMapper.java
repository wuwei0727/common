package com.tgy.rtls.data.mapper.map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.excel.QrCodeLocationDTO;
import com.tgy.rtls.data.entity.map.QrCodeLocation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.mapper.map
*@Author: wuwei
*@CreateTime: 2024-12-16 10:03
*@Description: TODO
*@Version: 1.0
*/
public interface QrCodeLocationMapper extends BaseMapper<QrCodeLocation> {
    List<QrCodeLocation> getAllQrCodeLocationOrConditionQuery(@Param("map") String map, @Param("areaName") String areaName, @Param("floorName") String floorName, @Param("desc") String desc, @Param("mapids") String[] mapids);

    QrCodeLocation getQrCodeLocationById(Integer id);

    List<QrCodeLocationDTO> getByMapId(String mapId);
}