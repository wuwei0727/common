package com.tgy.rtls.data.mapper.location;

import com.tgy.rtls.data.entity.check.TagcheckbsidEntity;
import com.tgy.rtls.data.entity.location.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.location
 * @date 2020/10/22
 */
public interface LocationMapper {
    /*
    * 定位原始数据存储
    * */
    int addOriginaldata(@Param("originaldata") Originaldata originaldata);

    /*
     * 定位原始数据存储
     * */
    int addTdoaData(@Param("tdoaData") TdoaData tdoaData);


    /*
    * 定位数据存储
    * */
    int addTrailrecord(@Param("trailrecord")Trailrecord trailrecord);



    int addBeacondata(@Param("beacondata")Beacondata beacondata);

    /**
     * 查询其他数据
     * @param start
     * @param end
     * @return
     */
    List<TagcheckbsidEntity> getLackTagidFromRecovery(@Param("start")String start, @Param("end")String end);
    /*
    * 数据恢复
    * */
    int insertRecovery(@Param("recovery") Recovery_data recovery);
    /*
    * 轨迹回放
    * */
    List<Trailrecord> findByTrail(@Param("personid")Integer personid,@Param("name")String name,@Param("num")String num,@Param("startTime")String startTime,@Param("endTime")String endTime,@Param("map")Integer map);


    /**
     * 获取基站定位原始数据
     * @param bsid
     * @param tagid
     * @return
     */
   List<Originaldata>  getOriginalDataByBsidAndTagid(@Param("bsid") Long bsid, @Param("tagid")Integer tagid);

    List<Batch>  gettest(@Param("batchID") String batchID);

   void addDiag(@Param("diagdata") DiagData diagdata);


    /** 获取标签回放数据
     * @param id   标签id
     * @param startTime
     * @param endTime
     * @param map
     * @return
     */
    List<DebugRecord>  findByTrailWithDebugData(@Param("id")Integer id, @Param("startTime")String startTime, @Param("endTime")String endTime, @Param("map")Integer map);

}
