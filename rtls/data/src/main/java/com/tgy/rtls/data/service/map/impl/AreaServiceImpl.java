package com.tgy.rtls.data.service.map.impl;

import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.map.*;
import com.tgy.rtls.data.entity.message.WarnRecord;
import com.tgy.rtls.data.entity.user.PersonVO;
import com.tgy.rtls.data.mapper.common.RecordMapper;
import com.tgy.rtls.data.mapper.map.AreaDotMapper;
import com.tgy.rtls.data.mapper.map.AreaMapper;
import com.tgy.rtls.data.mapper.map.AreaRuleMapper;
import com.tgy.rtls.data.mapper.message.WarnRecordMapper;
import com.tgy.rtls.data.mapper.user.PersonMapper;
import com.tgy.rtls.data.service.map.AreaService;
import com.tgy.rtls.data.websocket.WebSocketLocation;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.map.impl
 * @date 2020/10/22
 */
@Service
@Transactional
public class AreaServiceImpl implements AreaService {
    @Autowired(required = false)
    private AreaMapper areaMapper;
    @Autowired(required = false)
    private AreaRuleMapper areaRuleMapper;
    @Autowired(required = false)
    private AreaDotMapper areaDotMapper;
    @Autowired(required = false)
    private PersonMapper personMapper;
    @Autowired(required = false)
    private RecordMapper recordMapper;
    @Autowired(required = false)
    private WarnRecordMapper warnRecordMapper;
    @Autowired(required = false)
    private WebSocketLocation webSocketLocation;
    @Autowired
    private LocalUtil localUtil;

    @Override
    public List<Area> findByAll(Integer map, Integer type, String name) {
        List<Area> areas = areaMapper.findByAll(map, type, name);
        for (Area area : areas) {
            List<AreaDot> dots = areaDotMapper.findByArea(String.valueOf(area.getId()));
            if (!NullUtils.isEmpty(dots)) {
                area.setDots(dots);
            }
        }
        return areas;
    }

    @Override
    public List<Area> findByMap(Integer map) {
        List<Area> areas = areaMapper.findByMap(map);
        for (Area area : areas) {
            List<AreaDot> dots = areaDotMapper.findByArea(String.valueOf(area.getId()));
            if (!NullUtils.isEmpty(dots)) {
                area.setDots(dots);
            }
        }
        return areas;
    }


    @Override
    public Map<String, Object> findById(Integer id) {
        Map<String, Object> result = new HashMap<>();
        //查询出区域信息
        Area area = areaMapper.findById(id);
        List<AreaDot> dots = areaDotMapper.findByArea(String.valueOf(id));
        if (!NullUtils.isEmpty(dots)) {
            area.setDots(dots);
        }
        //该区域下的进出报警规则
        List<AreaTurnover> turnovers = areaRuleMapper.findByTurnover(id);
        for (AreaTurnover turnover : turnovers) {
            //白名单
            List<PersonVO> personSyns = personMapper.findByWhitelist(turnover.getId(),localUtil.getLocale());
            turnover.setPersonSynList(personSyns);
        }
        //该区域下的超员报警规则
        List<AreaOverload> overloads = areaRuleMapper.findByOverload(id);
        //该区域下的出入口检测规则
        List<AreaDetection> detections = areaRuleMapper.findByDetection(id);
        //存储数据
        result.put("area", area);
        result.put("turnovers", turnovers);
        result.put("overloads", overloads);
        result.put("detections", detections);
        return result;
    }

    @Override
    public Area findByName(String name) {
        return areaMapper.findByName(name);
    }

    @Override
    public Area findByAreaId(Integer id) {
        return areaMapper.findById(id);
    }

    @Override
    public Boolean addArea(AreaSyn areaSyn, Integer instanceid) {
        try {
            //1.添加区域信息
            boolean result;
            if (NullUtils.isEmpty(areaSyn.getId())) {
                result = areaMapper.addArea(areaSyn) > 0;
                //1.1添加区域后 新增该区域的日志类型
                Area area = areaMapper.findById(areaSyn.getId());
              //  areaMapper.addEventlogType("离开" + area.getTypeName() + "-" + area.getName(), instanceid);
               // areaMapper.addEventlogType("进入" + area.getTypeName() + "-" + area.getName(), instanceid);
            } else {
                result = areaMapper.addAreaId(areaSyn) > 0;
            }
            if (result) {
                //2.添加区域点信息
                if (areaSyn.getDots() != null) {
                    for (AreaDot dot : areaSyn.getDots()) {
                        dot.setArea(areaSyn.getId());
                        areaDotMapper.addAreaDot(dot);
                    }
                }
                //3.添加进出报警规则
                if (areaSyn.getTurnovers() != null) {
                    for (AreaTurnover turnover : areaSyn.getTurnovers()) {
                        turnover.setArea(areaSyn.getId());
                        turnover.setInstanceid(instanceid);
                        turnover.setFloor(areaSyn.getFloor());
                        if (areaRuleMapper.addAreaTurnover(turnover) > 0) {
                            //白名单
                            if (!NullUtils.isEmpty(turnover.getPersonids())) {
                                String[] split = turnover.getPersonids().split(",");
                                for (String s : split) {
                                    areaRuleMapper.addWhitelist(turnover.getId(), Integer.valueOf(s));
                                }
                            }
                        }
                    }
                }
                //4.添加超员报警规则
                if (areaSyn.getOverloads() != null) {
                    for (AreaOverload overload : areaSyn.getOverloads()) {
                        overload.setArea(areaSyn.getId());
                        overload.setInstanceid(instanceid);
                        overload.setFloor(areaSyn.getFloor());
                        areaRuleMapper.addAreaOverload(overload);
                    }
                }
                //5.添加出入口检测规则
                if (areaSyn.getDetections() != null) {
                    for (AreaDetection detection : areaSyn.getDetections()) {
                        detection.setArea(areaSyn.getId());
                        detection.setInstanceid(instanceid);
                        detection.setFloor(areaSyn.getFloor());
                        areaRuleMapper.addAreaDetection(detection);
                    }
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Boolean updateArea(AreaSyn areaSyn, Integer instanceid) {
        //1.删除原数据
        Area area = areaMapper.findById(areaSyn.getId());
        if (delArea(areaSyn.getId(), false)){
            //2.添加新数据
            addArea(areaSyn, instanceid);
            Area area1 = areaMapper.findById(areaSyn.getId());
            //3修改该区域的日志类型
          //  areaMapper.updateEventlogType("离开" + area1.getTypeName() + "-" + area1.getName(), "离开" + area.getTypeName() + "-" + area.getName());
           // areaMapper.updateEventlogType("进入" + area1.getTypeName() + "-" + area1.getName(), "进入" + area.getTypeName() + "-" + area.getName());
            //4.1通知前端结束报警
            disWarnArea(areaSyn.getId());
            //4.2删除区域后结束该区域正在报警的信息
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currtTime= dateFormat.format(new Date());
            areaMapper.updateWarnRecordArea(areaSyn.getId(),currtTime);
            //4.3结束该区域的进出信息
            areaMapper.updateInArea(areaSyn.getId(),currtTime);

            return true;
        }
        return false;
    }

    @Override
    public Boolean delArea(Integer id, Boolean result) {
        try {
            //1.删除区域下的规则
            //超员报警规则
            areaRuleMapper.delAreaOverloadArea(id);
            //进出报警规则
            List<AreaTurnover> turnovers = areaRuleMapper.findByTurnover(id);
            for (AreaTurnover turnover : turnovers) {
                //删除白名单
                areaRuleMapper.delWhitelist(turnover.getId());
            }
            areaRuleMapper.delAreaTurnoverArea(id);
            //出入口检测规则
            areaRuleMapper.delAreaDetectionArea(id);
            //2.删除区域点
            areaDotMapper.delAreaDot(String.valueOf(id));
            //4.删除区域
            Area area = areaMapper.findById(id);
            areaMapper.delArea(id);
            if (result) {
                //3.1删除该区域的日志类型
             //   areaMapper.delEventlogType("离开" + area.getTypeName() + "-" + area.getName());
              //  areaMapper.delEventlogType("进入" + area.getTypeName() + "-" + area.getName());
                //3.2删除区域的进出记录
                recordMapper.delInArea(id);
                //3.3通知前端结束报警
                disWarnArea(id);
                //3.4删除区域后结束该区域正在报警的信息
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                areaMapper.updateWarnRecordArea(id, dateFormat.format(new Date()));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void delAreaMap(String[] mapids) {
        //找出地图下区域的信息
        for (String s : mapids) {
            List<Area> areas = areaMapper.findByAll(Integer.valueOf(s), null, null);
            for (Area area : areas) {
                //删除区域相关的信息
                delArea(area.getId(), true);
            }
        }
    }

    @Override
    public AreaVO findByAreaCount(Integer id) {
        return areaMapper.findByAreaCount(id);
    }

    /*
    * 区域删除或禁用时将与该区域相关的报警去除
    * */
    public void disWarnArea(Integer id){
        //1.查找与该区域相关的报警信息
        List<WarnRecord> warnRecords=warnRecordMapper.findByArea(id,localUtil.getLocale());
        //2.遍历报警信息 通知前端结束报警
        for (WarnRecord warnRecord:warnRecords) {
            //2.1通知前端报警结束
            JSONObject objectdata = new JSONObject();
            objectdata.put("id",warnRecord.getId());//报警信息编号(唯一标识）
            objectdata.put("warnstate",1);//报警状态 0报警 1结束
            JSONObject objectWarn=new JSONObject();
            objectWarn.put("data",objectdata);
            objectWarn.put("type",2);
            webSocketLocation.sendAll(objectWarn.toString());
        }
    }

    /*
    * 添加白名单的人员将其相关的报警信息去除
    * */
    public void disWarnAreaPerson(Integer id,String personids){
        //1.查找与该区域白名单相关的报警信息
        List<WarnRecord> warnRecords=warnRecordMapper.findByAreaPerson(id,personids,localUtil.getLocale());
        //2.遍历报警信息 通知前端结束报警
        for (WarnRecord warnRecord:warnRecords) {
            //2.1通知前端报警结束
            JSONObject objectdata = new JSONObject();
            objectdata.put("id",warnRecord.getId());//报警信息编号(唯一标识）
            objectdata.put("warnstate",1);//报警状态 0报警 1结束
            JSONObject objectWarn=new JSONObject();
            objectWarn.put("data",objectdata);
            objectWarn.put("type",2);
            webSocketLocation.sendAll(objectWarn.toString());
            //2.2结束报警
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            warnRecordMapper.updateWarnRecord(dateFormat.format(new Date()),warnRecord.getId());
        }
    }

}
