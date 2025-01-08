package com.tgy.rtls.data.service.location.impl;

import com.tgy.rtls.data.entity.location.Beacondata;
import com.tgy.rtls.data.entity.location.DebugRecord;
import com.tgy.rtls.data.entity.location.Originaldata;
import com.tgy.rtls.data.entity.location.Trailrecord;
import com.tgy.rtls.data.mapper.location.LocationMapper;
import com.tgy.rtls.data.service.location.LocationService;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.location.impl
 * @date 2020/10/22
 */
@Service
@ToString
public class LocationServiceImpl implements LocationService {
    @Autowired(required = false)
    private LocationMapper locationMapper;
    @Override
    public Boolean addOriginaldata(Originaldata originaldata) {
        return locationMapper.addOriginaldata(originaldata)>0;
    }

    @Override
    public Boolean addBeacondata(Beacondata beacondata) {
        return locationMapper.addBeacondata(beacondata)>0;
    }

    @Override
    public Boolean addTrailrecord(Trailrecord trailrecord) {
        return locationMapper.addTrailrecord(trailrecord)>0;
    }
    @Override
    public List<Trailrecord> findByTrail(Integer personid,String name,String num, String startTime, String endTime, Integer map) {
        return locationMapper.findByTrail(personid,name,num,startTime,endTime,map);
    }

    @Override
    public List<DebugRecord> findByTrailWithDebugData(Integer id, String startTime, String endTime, Integer map) {
        return locationMapper.findByTrailWithDebugData(id,startTime,endTime,map);
    }

}
