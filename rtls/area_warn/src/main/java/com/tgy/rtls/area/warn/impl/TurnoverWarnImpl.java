package com.tgy.rtls.area.warn.impl;

import com.tgy.rtls.area.kafka.KafkaSender;
import com.tgy.rtls.area.warn.TurnoverWarn;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.map.Area;
import com.tgy.rtls.data.entity.message.WarnRecord;
import com.tgy.rtls.data.entity.user.Person;
import com.tgy.rtls.data.service.map.AreaRuleService;
import com.tgy.rtls.data.service.message.WarnRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.area.warn.impl
 * @date 2020/11/2
 */
@Service
public class TurnoverWarnImpl implements TurnoverWarn {
    @Autowired
    private WarnRecordService warnRecordService;
    @Autowired
    private AreaRuleService areaRuleService;
    @Autowired
    private KafkaSender kafkaSender;
    @Override
    public void intoWarn(Boolean state, Person person, Area area,Integer turnoverid) {
        //1.判断当前有没有非授权进入报警-->4
        WarnRecord warnRecord=warnRecordService.findByType(area.getMap(),area.getId(),person.getId(),4);
        //1.1判断该人员是否属于规则的白名单
        boolean whitelist=areaRuleService.findByWhitelist(turnoverid,person.getId())>0;
        //2.判断是否在区域内
        if (state){
            //2.1在区域内 且没有生成过非授权进入报警 人员不属于白名单
            if (NullUtils.isEmpty(warnRecord)&&!whitelist){
                //2.1.1生成报警
                WarnRecord record=new WarnRecord();
                record.setMap(area.getMap());
                record.setArea(area.getId());
                record.setPersonid(String.valueOf(person.getId()));
                record.setType(4);
                record.setDescribe(person.getName()+"非授权进入"+area.getName());
                warnRecordService.addWarnRecord(record);
                //2.1.2传输给前端
                kafkaSender.sendWarn(record.getId());

            }
        }else{
            //2.2 不在区域内 且有生成过非授权进入报警 人员不属于白名单
            if (!NullUtils.isEmpty(warnRecord)){
                //2.2.1结束非授权进入报警-->4
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentTime = dateFormat.format(new Date());
                warnRecordService.updateWarnRecord(currentTime,warnRecord.getId());
                //2.2.2通知前端结束报警
                kafkaSender.sendEndWarn(warnRecord.getId(),warnRecord);

            }
        }


    }

    @Override
    public void outWarn(Boolean state, Person person, Area area,Integer turnoverid) {
        //1.判断当前有没有非授权离开报警-->5
        WarnRecord warnRecord=warnRecordService.findByType(area.getMap(),area.getId(),person.getId(),5);
        //1.1判断该人员是否属于规则的白名单
        boolean whitelist=areaRuleService.findByWhitelist(turnoverid,person.getId())>0;
        //2.判断是否在区域内
        if (!state){
            //2.1不在在区域内 且没有生成过非授权离开报警
            if (NullUtils.isEmpty(warnRecord)&&!whitelist){
                //2.1.1生成报警
                WarnRecord record=new WarnRecord();
                record.setMap(area.getMap());
                record.setArea(area.getId());
                record.setPersonid(String.valueOf(person.getId()));
                record.setType(5);
                record.setDescribe(person.getName()+"非授权离开"+area.getName());
                warnRecordService.addWarnRecord(record);
                //2.1.2传输给前端
                kafkaSender.sendWarn(record.getId());
            }
        }else{
            //2.2 不在区域内 且有生成过非授权离开报警
            if (!NullUtils.isEmpty(warnRecord)){
                //2.2.1结束非授权离开报警-->5
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentTime = dateFormat.format(new Date());
                warnRecordService.updateWarnRecord(currentTime,warnRecord.getId());
                //2.2.2通知前端结束报警
                kafkaSender.sendEndWarn(warnRecord.getId(),warnRecord);
            }
        }
    }
}
