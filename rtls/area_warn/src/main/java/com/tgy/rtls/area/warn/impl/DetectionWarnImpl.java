package com.tgy.rtls.area.warn.impl;

import com.tgy.rtls.area.kafka.KafkaSender;
import com.tgy.rtls.area.warn.DetectionWarn;
import com.tgy.rtls.area.warn.IncoalWarn;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.Eventlog;
import com.tgy.rtls.data.entity.map.Area;
import com.tgy.rtls.data.entity.message.WarnRecord;
import com.tgy.rtls.data.entity.message.WarnRule;
import com.tgy.rtls.data.entity.user.Person;
import com.tgy.rtls.data.mapper.common.EventlogMapper;
import com.tgy.rtls.data.mapper.message.WarnRecordMapper;
import com.tgy.rtls.data.mapper.message.WarnRuleMapper;
import com.tgy.rtls.data.service.common.RecordService;
import com.tgy.rtls.data.service.common.StatisticsService;
import com.tgy.rtls.data.service.user.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.area.warn.impl
 * @date 2020/10/28
 * 用于判断出入口逻辑
 */
@Service
public class DetectionWarnImpl implements DetectionWarn {
    @Autowired
    private PersonService personService;
    @Autowired
    private RecordService recordService;
    @Autowired(required = false)
    private WarnRecordMapper warnRecordMapper;
    @Autowired
    private IncoalWarn incoalWarn;
    @Autowired(required = false)
    private EventlogMapper eventlogMapper;
    @Autowired
    private KafkaSender kafkaSender;
    @Autowired(required = false)
    private WarnRuleMapper warnRuleMapper;
    @Autowired
    private StatisticsService statisticsService;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void entranceWarn(Boolean state, Person person, Area area) {
        //1.根据该人员出入井状态做不同判断
        Integer minestate=person.getMinestate();//1井外  0井下
       // logger.info("initial PersonMine "+person.getName()+"旧状态"+minestate+"新状态"+state);

        if (state){
            //2.在入口区时  如果状态为1井外则生成入井记录 反正不生成
            if(minestate==1){
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentTime = dateFormat.format(new Date());
               //3.修改人员的下井时间和井下状态
                personService.updatePersonMine(person.getId(),0,currentTime);
                Person update = personService.findById(person.getId());
                Person update1 = personService.findByNum((person.getNum()));
                //4.生成人员下井记录
              //  logger.info("updatePersonMine "+person.getName()+"update"+update.getMinestate()+":"+update1.getMinestate());
                recordService.addIncoal(person.getId(),area.getMap());
                //5.判断井下超员报警
                incoalWarn.incoalOverload(area.getMap());
                //6.生成下井的事件日志
                Eventlog eventlog=new Eventlog();
                eventlog.setEvent(person.getName()+LocalUtil.get(KafukaTopics.IN)+LocalUtil.get(KafukaTopics.COAL));
                eventlog.setPersonid(person.getId());
                eventlog.setMap(area.getMap());
                eventlog.setTypeSimple(1);
            //    eventlog.setType(eventlogMapper.findByEventlogType(LocalUtil.get(KafukaTopics.IN)+LocalUtil.get(KafukaTopics.COAL),person.getInstanceid()));
                eventlogMapper.addEventlog(eventlog);
                //7.传输井下人数给前端
                int incoal=personService.findByCount(area.getMap());
                kafkaSender.sendCount(1,incoal,area.getMap());
                //8.统计一次人流量
                //8.1结束当一次统计的数据
                statisticsService.updateManFlow(area.getMap(),currentTime);
                //8.2生成新的数据
                statisticsService.addManFlow(area.getMap(),incoal);
            }
        }
    }

    @Override
    public void exitWarn(Boolean state, Person person, Area area) {
        //1.根据该人员出入井状态做不同判断
        Integer minestate=person.getMinestate();//1井外  0井下
        logger.info("initial PersonMine "+person.getName()+"旧状态"+minestate+"新状态"+state);
        if (state){
            //2.在出口区时  如果状态为0井下则生成入井记录 反正不生成
            if(minestate==0){
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentTime = dateFormat.format(new Date());
                //3.修改人员的井下状态
                personService.updatePersonMine(person.getId(),1,null);
                //4.结束该人员的下井记录
                recordService.updateIncoal(person.getId(),area.getMap(),currentTime);
                //5.结束该人员井下超时报警 2-->井下超时
                WarnRecord warnRecord=warnRecordMapper.findByType(area.getMap(),0,person.getId(),2);
                if (!NullUtils.isEmpty(warnRecord)) {
                    warnRecordMapper.updateWarnRecord(currentTime, warnRecord.getId());
                    //5.1通知前端结束报警
                    kafkaSender.sendEndWarn(warnRecord.getId(),warnRecord);
                    //5.2传输超时人数
                    int overtiem=personService.findByOvertime(area.getMap());
                    kafkaSender.sendCount(3,overtiem,area.getMap());
                }
                //6.结束该人员井下超员报警 1-->超员
                WarnRecord warnRecord1=warnRecordMapper.findByType(area.getMap(),0,null,1);
                if (!NullUtils.isEmpty(warnRecord1)) {
                    WarnRule rule = warnRuleMapper.findByType(1, area.getMap(),1);
                    Integer count = Integer.parseInt(rule.getRule());//允许的最大人数
                    //获取井下下 有多少人在线
                    Integer incoalCount = personService.findByCount(area.getMap());
                    if (incoalCount <= count) {
                        warnRecordMapper.updateWarnRecord(currentTime, warnRecord1.getId());
                        //6.1通知前端结束报警
                        kafkaSender.sendEndWarn(warnRecord1.getId(),warnRecord);
                    }
                }
                //7.生成出井的事件日志
                Eventlog eventlog=new Eventlog();
                eventlog.setEvent(person.getName()+ LocalUtil.get(KafukaTopics.OUT)+LocalUtil.get(KafukaTopics.COAL));
                eventlog.setPersonid(person.getId());
                eventlog.setMap(area.getMap());
                eventlog.setTypeSimple(2);
               // eventlog.setType(eventlogMapper.findByTypeSimple(LocalUtil.get(KafukaTopics.OUT)+LocalUtil.get(KafukaTopics.COAL),person.getInstanceid()));
                eventlogMapper.addEventlog(eventlog);
                //8.传输井下人数给前端
                int incoal=personService.findByCount(area.getMap());
                kafkaSender.sendCount(1,incoal,area.getMap());
                //9.统计一次人流量
                //9.1结束当一次统计的数据
                statisticsService.updateManFlow(area.getMap(),currentTime);
                //9.2生成新的数据
                statisticsService.addManFlow(area.getMap(),incoal);
            }
        }
    }

}
