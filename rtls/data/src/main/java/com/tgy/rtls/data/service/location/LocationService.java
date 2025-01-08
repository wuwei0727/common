package com.tgy.rtls.data.service.location;

import com.tgy.rtls.data.entity.location.Beacondata;
import com.tgy.rtls.data.entity.location.DebugRecord;
import com.tgy.rtls.data.entity.location.Originaldata;
import com.tgy.rtls.data.entity.location.Trailrecord;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.location
 * @date 2020/10/22
 */
public interface LocationService {
    /*
     * 定位原始数据存储
     * */
    Boolean addOriginaldata(Originaldata originaldata);


    /**
     * 添加同步数据
     */
    Boolean addBeacondata(Beacondata beacondata);

    /*
     * 定位数据存储
     * */
    Boolean addTrailrecord(Trailrecord trailrecord);
    /*
     * 轨迹回放
     * */
    List<Trailrecord> findByTrail(Integer personid, String name,String num,String startTime, String endTime, Integer map);


    List<DebugRecord>  findByTrailWithDebugData(Integer id, String startTime,String endTime,Integer map);




}
