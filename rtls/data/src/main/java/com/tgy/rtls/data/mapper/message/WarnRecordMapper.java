package com.tgy.rtls.data.mapper.message;

import com.tgy.rtls.data.entity.message.WarnRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.message
 * @date 2020/10/29
 */
public interface WarnRecordMapper {
    /*
    * 实例下查询报警记录 instanceid-->实例id startTime-->开始时间  endTime-->结束时间  type-->报警类型
    * */
    List<WarnRecord> findByAll(@Param("instanceid")String instanceid, @Param("map")Integer map, @Param("startTime")String startTime,
                               @Param("endTime")String endTime, @Param("type")Integer type, @Param("warnState")Integer warnState, @Param("areaType") Integer areaType, @Param("areaName")String areaName, String name,String timeformat);

    /*
    * 查询正在报警的记录
    * */
    List<WarnRecord> findByWarn(@Param("map")Integer map,String name);


    WarnRecord findById(@Param("id")Integer id,String name);

    /*
    * 查询与区域相关的正在报警的记录
    * */
    List<WarnRecord> findByArea(@Param("area")Integer area,String name);

    List<WarnRecord> findByAreaPerson(@Param("area")Integer area,@Param("personids")String personids,String name);

    /*
     * 查询特定地图下的报警类型记录
     * */
    List<WarnRecord> findByWarnType(@Param("type")Integer type,@Param("map")Integer map);


    /*
     * 查询聚集报警
     * */
    List<WarnRecord> findGatherWarn(@Param("type")Integer type,@Param("map")Integer map,@Param("floor")String floor,@Param("personid")String personid,@Param("describe")String describe);

    /*
    * 报警记录存储
    * */
    int addWarnRecord(WarnRecord warn);

    /*
    * 结束报警记录
    * */
    int updateWarnRecord(@Param("endTime")String endTime,@Param("id")Integer id);

    /*
    * 删除与人和物相关的报警信息
    * */
    int delWarnRecordSub(@Param("nums")String[] nums);

    /*
    * 查看人员是否有生成对应类型的报警信息 map-->地图id area-->区域id personid-->人员id  type-->类型id
    * */
    WarnRecord findByType(@Param("map")Integer map,@Param("area")Integer area,@Param("personid")Integer personid,@Param("type")Integer type);

}
