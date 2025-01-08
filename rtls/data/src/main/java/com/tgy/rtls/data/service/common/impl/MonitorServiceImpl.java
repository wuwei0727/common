package com.tgy.rtls.data.service.common.impl;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.common.*;
import com.tgy.rtls.data.entity.equip.Substation;
import com.tgy.rtls.data.entity.map.Area;
import com.tgy.rtls.data.mapper.common.MonitorMapper;
import com.tgy.rtls.data.service.common.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.common.impl
 * @date 2020/11/5
 */
@Service
@Transactional
public class MonitorServiceImpl implements MonitorService {
    @Autowired(required = false)
    private MonitorMapper monitorMapper;
    @Autowired
    LocalUtil localUtil;

    @Override
    public Monitor findByProject(Integer instanceid, String map, String departmentid, String worktypeid, String jobid, String startTime, String endTime) {
        return monitorMapper.findByProject(instanceid,map,departmentid,worktypeid,jobid,startTime,endTime, LocalUtil.get(KafukaTopics.TIMEFORMAT));
    }

    @Override
    public List<Monitor> findByMap(Integer instanceid, String map, String departmentid, String worktypeid, String jobid, String startTime, String endTime) {
        return monitorMapper.findByMap(instanceid,map,departmentid,worktypeid,jobid,startTime,endTime, LocalUtil.get(KafukaTopics.TIMEFORMAT));
    }

    @Override
    public Monitor findByMapId(Integer instanceid, String map, String departmentid, String worktypeid, String jobid, String startTime, String endTime) {
        return monitorMapper.findByMapId(instanceid,map,departmentid,worktypeid,jobid,startTime,endTime, LocalUtil.get(KafukaTopics.TIMEFORMAT));
    }

    @Override
    public List<MonitorArea> findByArea(Integer instanceid, String map, String departmentid, String worktypeid, String jobid, String startTime, String endTime) {
        return monitorMapper.findByArea(instanceid,map,departmentid,worktypeid,jobid,startTime,endTime, LocalUtil.get(KafukaTopics.TIMEFORMAT));
    }

    @Override
    public MonitorArea findByAreaId(Integer instanceid, String map, String area, String departmentid, String worktypeid, String jobid, String startTime, String endTime) {
        return monitorMapper.findByAreaId(instanceid,map,area,departmentid,worktypeid,jobid,startTime,endTime, LocalUtil.get(KafukaTopics.TIMEFORMAT));
    }

    @Override
    public List<MonitorSub> findBySub(Integer instanceid, String map, String departmentid, String worktypeid, String jobid, String startTime, String endTime) {
        return monitorMapper.findBySub(instanceid,map,departmentid,worktypeid,jobid,startTime,endTime,localUtil.getLocale(), LocalUtil.get(KafukaTopics.TIMEFORMAT));
    }

    @Override
    public MonitorSub findBySubId(Integer instanceid, String map, String sub, String departmentid, String worktypeid, String jobid, String startTime, String endTime) {
        return monitorMapper.findBySubId(instanceid,map,sub,departmentid,worktypeid,jobid,startTime,endTime,localUtil.getLocale(), LocalUtil.get(KafukaTopics.TIMEFORMAT));
    }

    @Override
    public List<MonitorAreatype> findByAreatype(String map) {
        String[] split=map.split(",");
        List<MonitorAreatype> areatypes=monitorMapper.findByAreatype(split);
        for (MonitorAreatype areatype:areatypes){
            List<Area> areas=monitorMapper.findByAreatypeName(areatype.getTypeName(),split);
            areatype.setAreas(areas);
        }
        return areatypes;
    }

    @Override
    public List<MonitorSubtype> findBySubtype(String map) {
        String[] split=map.split(",");
        List<MonitorSubtype> subtypes=monitorMapper.findBySubtype(split,localUtil.getLocale());
        for (MonitorSubtype subtype:subtypes){
            List<Substation> substations=monitorMapper.findBySubtypeName(subtype.getTypeName(),split,localUtil.getLocale());
            subtype.setSubstations(substations);
        }
        return subtypes;
    }

    @Override
    public List<MonitorPerson> findByProjectPerson(Integer instanceid, String departmentid, String worktypeid, String jobid, String startTime, String endTime) {
        return monitorMapper.findByProjectPerson(instanceid,departmentid,worktypeid,jobid,startTime,endTime, LocalUtil.get(KafukaTopics.TIMEFORMAT));
    }

    @Override
    public List<MonitorPerson> findByMapPerson(Integer instanceid, String map, String departmentid, String worktypeid, String jobid, String startTime, String endTime) {
        return monitorMapper.findByMapPerson(instanceid,map,departmentid,worktypeid,jobid,startTime,endTime, LocalUtil.get(KafukaTopics.TIMEFORMAT));
    }

    @Override
    public List<MonitorPerson> findByAreaPerson(Integer instanceid, String map, String area,String departmentid, String worktypeid, String jobid, String startTime, String endTime) {

        return monitorMapper.findByAreaPerson(instanceid,map,area,departmentid,worktypeid,jobid,startTime,endTime, LocalUtil.get(KafukaTopics.TIMEFORMAT));
    }

    @Override
    public List<MonitorPerson> findBySubPerson(Integer instanceid, String map, String sub,String departmentid, String worktypeid, String jobid, String startTime, String endTime) {
        return monitorMapper.findBySubPerson(instanceid,map,sub,departmentid,worktypeid,jobid,startTime,endTime, LocalUtil.get(KafukaTopics.TIMEFORMAT));
    }
}
