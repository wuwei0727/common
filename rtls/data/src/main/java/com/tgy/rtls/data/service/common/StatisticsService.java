package com.tgy.rtls.data.service.common;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.common
 * @date 2021/1/15
 */
public interface StatisticsService {

    /*
     * 人流量统计：记录井下人数变化 manflow map-->地图id  count-->井下人数
     * */
    boolean addManFlow(Integer map,int count);

    /*
     * 人流量统计：结束最近一条记录
     * */
    boolean updateManFlow(Integer map,String endTime);

    /*
    * 人流量统计：计算
    * */
    List<Object> getManFlowSel(Integer map, int day);

    /*
     * 人流量统计：计算
     * */
    List<Object> getManFlowSelFromIncoalRecord(Integer map, int day);

    /*
    * 报警数量统计：计算
    * */
    List<Object> getWarnFlowSel(Integer map, int day,String startTime,int number);

    /*
    * 删除地图上的人流量信息
    * */
    boolean delManFlow(String[] maps);

}
