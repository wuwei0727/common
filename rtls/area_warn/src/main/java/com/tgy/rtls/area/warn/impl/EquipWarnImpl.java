package com.tgy.rtls.area.warn.impl;

import com.tgy.rtls.area.kafka.KafkaSender;
import com.tgy.rtls.area.warn.EquipWarn;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.equip.SubSyn;
import com.tgy.rtls.data.entity.equip.Substation;
import com.tgy.rtls.data.entity.message.WarnRecord;
import com.tgy.rtls.data.entity.message.WarnRule;
import com.tgy.rtls.data.entity.user.Person;
import com.tgy.rtls.data.mapper.message.WarnRuleMapper;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.message.WarnRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.area.warn.impl
 * @date 2020/11/3
 */
@Service
public class EquipWarnImpl implements EquipWarn {
    @Autowired
    private WarnRecordService warnRecordService;
    @Autowired(required = false)
    private WarnRuleMapper warnRuleMapper;
    @Autowired
    private KafkaSender kafkaSender;
    @Autowired
    private SubService subService;

    @Override
    public void sosWarn(Person person, Integer sos) {
        //1.判断该人员有没有sos报警生成
        WarnRecord warnRecord=warnRecordService.findByType(0,0,person.getId(),6);
        //2.判断是不是sos报警
        if (sos==1){
            //报警
            if (NullUtils.isEmpty(warnRecord)){
                //2.1报警且当前人员没有sos报警记录生成
                //2.1.1生成sos报警信息 type-->6
                WarnRecord record = new WarnRecord();
                record.setMap(person.getMap());
                record.setArea(0);
                record.setDescribe(person.getName()+"发出SOS报警");//描述
                record.setType(6);
                record.setPersonid(String.valueOf(person.getId()));
                warnRecordService.addWarnRecord(record);
                //2.1.2传输报警信息
                kafkaSender.sendWarn(record.getId());
            }
        }else {
            //不报警
            if (!NullUtils.isEmpty(warnRecord)){
                //2.2不报警且当前人员有sos报警记录没有结束
                //2.2.1结束该sos报警记录
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentTime = dateFormat.format(new Date());
                warnRecordService.updateWarnRecord(currentTime,warnRecord.getId());
                kafkaSender.sendEndWarn(warnRecord.getId(),warnRecord);
            }
        }
    }

    @Override
    public void powerWarn(Person person, double power) {
        //1.判断该人员有没有低电量报警生成
        WarnRecord warnRecord=warnRecordService.findByType(0,0,person.getId(),7);
        //2.获取当前地图规定的最低电量 低电量-->4
        WarnRule rule = warnRuleMapper.findByType(4,person.getMap(),1);
        if(rule==null)
            return;
        int minpower = Integer.parseInt(rule.getRule());//允许的最低电量
        if (NullUtils.isEmpty(warnRecord)){
            //没有低电量报警生成
            if (power < minpower) {// 触发报警
                WarnRecord record = new WarnRecord();
                record.setMap(0);
                record.setArea(0);
                record.setDescribe(person.getName()+"所携带的定位卡电量低于"+minpower+"%");//描述
                record.setPersonid(String.valueOf(person.getId()));
                record.setType(7);
                warnRecordService.addWarnRecord(record);
                //传输报警信息
                kafkaSender.sendWarn(record.getId());
            }
        }else {
            //有低电量报警生成
            if ((power+15) >= minpower) {// 结束报警
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentTime = dateFormat.format(new Date());
                warnRecordService.updateWarnRecord(currentTime,warnRecord.getId());
                kafkaSender.sendEndWarn(warnRecord.getId(),warnRecord);
            }
        }
    }

    @Override
    public void subNetworkstate(Boolean state,Substation substation) {
        //1判断有没有分站离线报警生成  分站离线-->13
        WarnRecord warnRecord=warnRecordService.findByType(Integer.valueOf(substation.getMap()),0,substation.getId(),13);
        if (NullUtils.isEmpty(warnRecord)){
            //没有分站离线报警生成且现在是报警状态
            if (state) {
                WarnRecord record = new WarnRecord();
                record.setMap(Integer.valueOf(substation.getMap()));
                record.setArea(0);
                record.setDescribe(substation.getTypeName() + "" + substation.getNum() + "离线");//描述
                record.setPersonid(String.valueOf(substation.getId()));
                record.setType(13);
                warnRecordService.addWarnRecord(record);
                //传输报警信息
                kafkaSender.sendWarn(record.getId());
            }
        }else {
            //有分站离线报警生成且现在是不报警状态
            if (!state) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentTime = dateFormat.format(new Date());
                warnRecordService.updateWarnRecord(currentTime, warnRecord.getId());
                kafkaSender.sendEndWarn(warnRecord.getId(),warnRecord);
            }
        }
    }

    @Override
    public void subPowerstate(Boolean state,Substation substation) {
        //1判断有没有分站主供电异常生成  分站主供电异常-->14
        WarnRecord warnRecord=warnRecordService.findByType(Integer.valueOf(substation.getMap()),0,substation.getId(),14);
        if (NullUtils.isEmpty(warnRecord)){
            //没有分站主供电异常生成且现在是报警状态
            if (state) {
                WarnRecord record = new WarnRecord();
                record.setMap(Integer.valueOf(substation.getMap()));
                record.setArea(0);
                record.setDescribe(substation.getTypeName() + "" + substation.getNum() + "启用备用供电");//描述
                record.setPersonid(String.valueOf(substation.getId()));
                record.setType(14);
                warnRecordService.addWarnRecord(record);
                //传输报警信息
                kafkaSender.sendWarn(record.getId());
            }
        }else {
            //有分站主供电异常报警生成且现在是不报警状态
            if (!state) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentTime = dateFormat.format(new Date());
                warnRecordService.updateWarnRecord(currentTime, warnRecord.getId());
                kafkaSender.sendEndWarn(warnRecord.getId(),warnRecord);
            }
        }
    }

    @Override
    public void subOverload(String num,Integer map) {
        //1.查询该分站下人数和最大人数上限
        SubSyn subSyn=subService.findByMaxnum(num);
        //1.1当前分站人数超过最大人数上限触发报警
        if (!NullUtils.isEmpty(subSyn)){
            if (!NullUtils.isEmpty(subSyn.getMaxnum())) {
                //2判断有没有分站超员报警生成  分站超员-->9
                WarnRecord warnRecord=warnRecordService.findByType(map,0,subSyn.getId(),9);
                if (subSyn.getMaxnum() < subSyn.getCount()){
                    if(NullUtils.isEmpty(warnRecord)){
                    //2.1.超过最大人数上限且没有报警信息则结束报警
                        WarnRecord record = new WarnRecord();
                        record.setMap(map);
                        record.setArea(0);
                        record.setDescribe(subSyn.getTypeName()+""+subSyn.getNum() +"设定人数"+subSyn.getMaxnum()+"人,实际检测"+subSyn.getCount()+"人");//描述
                        record.setPersonid(String.valueOf(subSyn.getId()));
                        record.setType(9);
                        warnRecordService.addWarnRecord(record);
                        //传输报警信息
                        kafkaSender.sendWarn(record.getId());
                    }
                }else{
                    //2.2.没超过最大人数上限且有报警信息则结束报警
                    if (!NullUtils.isEmpty(warnRecord)){
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String currentTime = dateFormat.format(new Date());
                        warnRecordService.updateWarnRecord(currentTime, warnRecord.getId());
                        kafkaSender.sendEndWarn(warnRecord.getId(),warnRecord);
                    }
                }
            }
        }
    }

}
