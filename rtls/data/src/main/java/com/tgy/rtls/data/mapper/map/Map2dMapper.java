package com.tgy.rtls.data.mapper.map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.map.Style;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.map
 * @date 2020/10/19
 */
public interface Map2dMapper extends BaseMapper<Map_2d> {
    /*
    * 查询2维地图信息
    * */
    List<Map_2d> findByAll(@Param("name")String name,@Param("enable")Integer enable,@Param("instanceid")String instanceid,String statusname);
    List<Map_2d> findByAll2(@Param("name")String name,@Param("enable")Integer enable,@Param("instanceid")String instanceid,String statusname,@Param("companyId") String companyId,@Param("floorName") String floorName,@Param("maps")String[] maps);
    List<Map_2d> findByAll3(@Param("name")String name,@Param("enable")Integer enable,@Param("instanceid")String instanceid,String statusname,@Param("floorName") String floorName,@Param("mapidAlls")String[] mapidAlls);
    List<Map_2d> findByAll4(@Param("name")String name,@Param("enable")Integer enable,@Param("instanceid")String instanceid,String statusname,@Param("mapidAlls")String[] mapidAlls);


    /*
     * 查询是否重复
     * */
    List<Map_2d> findByAllSame(@Param("name")String name);


    /*
    * 根据地图id获取地图名
    * */
    String findByNameId(@Param("ids")String[] ids);

    /*
    * 2维地图详情
    * */
    Map_2d findById(@Param("id")Integer id,String name);

    /*
    * 查询实例下地图集
    * */
    String findByMapInstance(@Param("instanceid")Integer instanceid);
    /*
     * 查询实例下分站集
     * */
    String findBySubInstance(@Param("instanceid")Integer instanceid);
    /*
    * 新增地图
    * */
    int addMap2d(@Param("map2d")Map_2d map2d);

    int deleteByUserIdMap(@Param("mapId")String mapId,@Param("userId")String userId);

    int addUserMap(@Param("uid")Integer uid,@Param("mapId")String mapId);
    /*
    * 修改地图
    * */
    int updateMap2d(@Param("map2d")Map_2d map2d);

    /*
    * 删除地图
    * */
    int delMap2d(@Param("ids")String[] ids);

    int delPlace(@Param("ids")String[] ids);

    int delMap2dInstance(@Param("instanceid")String instanceid);

    /*
    * 样式信息查询 type-->样式类型 tag标签 sub基站
    * */
    List<Style> findByStyle(@Param("type")String type);


    Map_2d findByfmapID(@Param("fmapID")String fmapID);

    Map_2d findByMapId(@Param("mapId")String mapId);

    int updateByIds(@Param("map2d") Map_2d map2d);

    List<Map_2d> findByfmapId(@Param("mapId") Integer mapId, @Param("fmapId") String fmapId);

    List<Map_2d> getMapName(@Param("mapIds") String[] mapIds);
}
