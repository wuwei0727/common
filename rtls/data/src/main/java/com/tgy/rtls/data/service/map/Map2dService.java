package com.tgy.rtls.data.service.map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.map.Style;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.map
 * @date 2020/10/19
 */
public interface Map2dService extends IService<Map_2d> {
    /*
     * 查询2维地图信息
     * */
    List<Map_2d> findByAll(String name,Integer enable,String instanceid);
    List<Map_2d> findByAll2(String name, Integer enable, String instanceid, String comId, String floorName, String[] maps);
    List<Map_2d> findByAll3(String name, Integer enable, String instanceid, String floorName, String[] mapidAlls);
    List<Map_2d> findByAll4(String name,Integer enable,String instanceid,String[] mapidAlls);
    /*
     * 查询是否重复
     * */
    List<Map_2d> findByAllSame(String name);
    /*
     * 根据地图id获取地图名
     * */
    String findByNameId(String ids);

    /*
     * 2维地图详情
     * */
    Map_2d findById(Integer id);

    /*
     * 新增2维地图
     * */
    Boolean addMap2d(Map_2d map2d);
    Boolean deleteByMapIdMap(String mapId,String userId);

    int addUsermap(Integer uid,String mapId);

    /*
     * 修改2维地图
     * */
    Boolean updateMap2d(Map_2d map2d);

    /*
    * 删除实例时 将其相关的地图删除
    * */
    Boolean delMap2dInstanceid(Integer instanceid);

    /*
     * 删除2维地图
     * */
    Boolean delMap2d(String ids);

    Boolean delPlace(String ids);

    /*
     * 样式信息查询 type-->样式类型 tag标签 sub基站
     * */
    List<Style> findByStyle(String type);

    /*
     * 2维地图详情
     * */
    Map_2d findByfmapID(String mapId);

    int updateByIDCode(Map_2d map2d);

    List<Map_2d> findByfmapId(Integer mapId,String fmapId);

    List<Map_2d> getMapName(String[] mapids);
}
