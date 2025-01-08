package com.tgy.rtls.area.warn.impl;

import com.tgy.rtls.area.kafka.KafkaSender;
import com.tgy.rtls.area.warn.IncoalWarn;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.message.WarnRecord;
import com.tgy.rtls.data.entity.message.WarnRule;
import com.tgy.rtls.data.entity.user.Person;
import com.tgy.rtls.data.mapper.message.WarnRuleMapper;
import com.tgy.rtls.data.service.message.WarnRecordService;
import com.tgy.rtls.data.service.user.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author 许强
 * @Package com.tgy.rtls.area.warn.impl
 * @date 2020/10/30
 */
@Service
public class IncoalWarnImpl implements IncoalWarn
{
    @Autowired(required = false)
    private WarnRuleMapper warnRuleMapper;
    @Autowired
    private PersonService personService;
    @Autowired
    private WarnRecordService warnRecordService;
    @Autowired
    private KafkaSender kafkaSender;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    public void incoalOverload(Integer map) {
        //1.判断该人员有没有生成过井下超员报警
        WarnRecord warnRecord=warnRecordService.findByType(map,0,0,1);
        if(NullUtils.isEmpty(warnRecord)) {
            //获取当前地图规定的超员人数 井下超员-->1
            WarnRule rule = warnRuleMapper.findByType(1, map,1);
            if(rule==null)
                return;
            Integer count = Integer.parseInt(rule.getRule());//允许的最大人数
            //获取井下下 有多少人在线
            Integer incoalCount = personService.findByCount(map);
            if (incoalCount > count) {//如果井下人数大于地图允许的最大人数 触发报警
                WarnRecord record = new WarnRecord();
                record.setMap(map);
                record.setArea(0);
                record.setDescribe("井下设定人数" + count + "人,实际检测" + incoalCount + "人");//描述
                record.setPersonid("0");
                record.setType(1);
                warnRecordService.addWarnRecord(record);
                //传输报警信息
                kafkaSender.sendWarn(record.getId());
            }
        }
    }

    @Override
    public void incoalOvertime(Integer personid,Integer map){
        //1.判断该人员有没有生成过井下超时报警
        WarnRecord warnRecord=warnRecordService.findByType(map,0,personid,2);
        if(NullUtils.isEmpty(warnRecord)){
            //获取当前地图规定的超时时间 井下超时-->2
            if(map==null)
                return;
            WarnRule rule=warnRuleMapper.findByType(2,map,1);
            if(rule==null)
                return;
            //超时时长-->在井下停留时间不能超过这个值
            Integer  time= Integer.valueOf(rule.getRule());
            //获取该人员的下井时间
            Person person=personService.findById(personid);
            Integer duration= Integer.valueOf(person.getDuration());
            if(!NullUtils.isEmpty(duration)&&duration>time*60){//下井时长>超时时长  触发报警
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //2.没有 记录报警信息
                WarnRecord record=new WarnRecord();
                record.setMap(map);
                record.setArea(0);
                record.setDescribe(person.getName()+"，下井时间"+dateFormat.format(person.getTime()));//描述
                record.setPersonid(String.valueOf(personid));
                record.setType(2);
               // record.setStartTime(person.getTime());
                warnRecordService.addWarnRecord(record);
                //传输报警信息
                kafkaSender.sendWarn(record.getId());
                //传输超时人数
                int overtiem=personService.findByOvertime(map);
                kafkaSender.sendCount(3,overtiem,map);
            }
        }
    }

    @Override
    public void personOffLine(Integer personid, Integer map) {
        //1.判断该人员有没有生成过离线报警
        WarnRecord warnRecord=warnRecordService.findByType(map,0,personid,10);
        logger.info(map+"包含离线时间类型");
        if(map==null)
            return;
        if(NullUtils.isEmpty(warnRecord)){
            //获取当前地图规定的离线时间 离线-->3
            WarnRule rule=null;
            try {
                 rule = warnRuleMapper.findByType(3, map,1);
            }catch (Exception e){
                return;
            }
            if (NullUtils.isEmpty(rule)){
                return;
            }
            //离线时长-->离线时间不能超过这个值
            Integer  time= Integer.valueOf(rule.getRule());
            //获取该人员的离线时间
            Person person=personService.findByOffLine(personid);
            Integer duration= Integer.valueOf(person.getDuration());
            if(!NullUtils.isEmpty(duration)&&duration>=time*60){//离线时长>超时时长  触发报警
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //2.没有 记录报警信息

                WarnRecord record=new WarnRecord();
                record.setMap(map);
                record.setArea(0);
                record.setDescribe(person.getName()+"，离线时间"+(person.getOffTime()));//描述
                record.setPersonid(String.valueOf(personid));
                record.setType(10);
                record.setWarnstate(0);
                try {
                    record.setStartTime(dateFormat.parse(person.getOffTime()));
                    logger.info("离线"+new Timestamp(record.getStartTime().getTime()).toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                logger.info(record.getDescribe());
                warnRecordService.addWarnRecord(record);
                //传输报警信息
                kafkaSender.sendWarn(record.getId());
                //传输超时人数
                int offLine=personService.findByOff(map);
                kafkaSender.sendCount(2,offLine,map);
            }
        }
    }
}
