package com.tgy.rtls.data.mapper;

import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.park.ShangJia;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.mapper
 * @Author: wuwei
 * @CreateTime: 2024-12-25 14:44
 * @Description: TODO
 * @Version: 1.0
 */
public interface ImageSyncMapper {
    @Select("select id,photo,photo2,thumbnail from shangjia")
    List<ShangJia> findAllShangJia();

    @Select("select qrcode,welcome_page_path,map_logo from map_2d")
    List<Map_2d> findAllMap2d();


}
