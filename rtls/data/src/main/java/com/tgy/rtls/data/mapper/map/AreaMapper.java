package com.tgy.rtls.data.mapper.map;

import com.tgy.rtls.data.entity.map.Area;
import com.tgy.rtls.data.entity.map.AreaSyn;
import com.tgy.rtls.data.entity.map.AreaVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.map
 * @date 2020/10/20
 */
public interface AreaMapper {
    /*
    * 地图下区域信息查询 map-->地图id type-->区域类型id name-->区域名称
    * */
    List<Area> findByAll(@Param("map")Integer map,@Param("type")Integer type,@Param("name")String name);

    List<Area> findByMap(@Param("map")Integer map);

    /*
    * 地图下区域信息详情
    * */
    Area findById(@Param("id")Integer id);

    /*
    *
    * */
    Area findByName(@Param("name")String name);

    /*
    * 新增区域 自增id
    * */
    int addArea(@Param("areaSyn") AreaSyn areaSyn);
    /*
     * 新增区域 赋值id
     * */
    int addAreaId(@Param("areaSyn") AreaSyn areaSyn);

    /*
    * 修改区域
    * */
    int updateArea(@Param("area")Area area);

    /*
    * 删除区域
    * */
    int delArea(@Param("id")Integer id);


    /*
    * 新增事件日志类型
    * */
    int addEventlogType(@Param("name")String name,@Param("instanceid")Integer instanceid);

    /*
    * 修改事件日志类型 name1-->新类型名 name2-->旧类型名
    * */
    int updateEventlogType(@Param("name1")String name1,@Param("name2")String name2);
    /*
     * 删除事件日志类型
     * */
    int delEventlogType(@Param("name")String name);

    /*
    * 查看当前区域的人数情况并发送给前端
    * */
    AreaVO findByAreaCount(@Param("id")Integer id);

    /*
    * 删除区域时 将与该区域相关的报警信息结束 id-->区域id
    * */
    int updateWarnRecordArea(@Param("id")Integer id,@Param("endTime")String endTime);

    /*
    * 将与该区域相关的进出记录结束
    * */
    int updateInArea(@Param("id")Integer id,@Param("endTime")String endTime);


}
