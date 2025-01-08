package com.tgy.rtls.data.mapper.common;

import com.tgy.rtls.data.entity.common.WarnFlow;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.common
 * @date 2021/1/15
 * 统计数据管理
 */
public interface StatisticsMapper {

    /*
     * 人流量统计：记录井下人数变化 manflow map-->地图id  count-->井下人数
     * */
    int addManFlow(@Param("map")Integer map,@Param("count")int count);

    /*
     * 人流量统计：结束最近一条记录
     * */
    int updateManFlow(@Param("map")Integer map,@Param("endTime")String endTime);

    /*
     * 人流量统计：查询startTime——>endTime时间段内井下最大人数
     * */
    Integer selectManFlow(@Param("map")Integer map,@Param("startTime")String startTime,@Param("endTime")String endTime);

    /*
     * 人流量统计：查询startTime——>endTime时间段内井下最大人数
     * */
    Integer selectManFlowFromIncoalRecord(@Param("map")Integer map,@Param("startTime")String startTime,@Param("endTime")String endTime);

    /*
    * 人流量统计：查询startTime前最近一条数据的人数
    * */
    Integer selectManFlowLately(@Param("map")Integer map,@Param("startTime")String startTime);

    /*
    * 人流量删除
    * */
    int delManFlow(@Param("maps")String[] maps);

    /*
     *报警数量统计；查询startTime到endTime时间段内报警数量
     * */
    List<WarnFlow> findByWarnFlow(@Param("map")Integer map,@Param("startTime")String startTime,@Param("endTime")String endTime,String name);

}
