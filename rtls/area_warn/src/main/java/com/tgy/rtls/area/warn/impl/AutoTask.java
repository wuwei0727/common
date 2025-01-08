package com.tgy.rtls.area.warn.impl;

import com.tgy.rtls.area.kafka.KafkaAreaWarn;
import com.tgy.rtls.area.kafka.KafkaSender;
import com.tgy.rtls.area.warn.GatherWarn;
import com.tgy.rtls.area.warn.IncoalWarn;
import com.tgy.rtls.data.entity.user.Person;
import com.tgy.rtls.data.mapper.message.WarnRecordMapper;
import com.tgy.rtls.data.service.message.WarnRecordService;
import com.tgy.rtls.data.service.user.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.area.warn
 * @date 2021/1/22
 * 定时器
 */
@Component
@Configuration
@EnableScheduling
@Transactional
public class AutoTask {
    @Autowired
    private PersonService personService;
    @Autowired
    private IncoalWarn incoalWarn;
    @Autowired
    private GatherWarn gatherWarn;
    @Autowired(required = false)
    WarnRecordMapper warnRecordMapper;
    @Autowired(required = false)
    WarnRecordService warnRecordService;
    @Autowired
    private KafkaSender kafkaSender;

    @Scheduled(cron = "0 */1 * * * ?")
    public void mapRestrict(){
        System.out.println("开启定时任务:判断人员离线和人员井下超时报警");
        //1.找到所有的离线人数，判断是否触发报警
        List<Person> personList=personService.findByPersonOff(null);
        for (Person person:personList){
            incoalWarn.personOffLine(person.getId(), person.getMap());
        }
        //2.找到所有的井下人数，判断是否触发报警
        List<Person> personList1=personService.findByInCoalPerson();
        for (Person person:personList1){
            incoalWarn.incoalOvertime(person.getId(), person.getMap());
        }


    }
    @Scheduled(fixedRate = 10000)
    public void gather(){
        System.out.println("开启定时任务:聚集报警");
      //.找到所有人员位置进行聚集报警触发
        gatherWarn.getGatherInfo(KafkaAreaWarn.detectionMap);

    }
/*
    @Scheduled(fixedRate = 10000)
    public void sampleCheck(){
        System.out.println("开启定时任务:样品超时预警");
        //.找到所有样品（人员）的应检时间
        List<Person> personList = personService.findAllPerson();
        Date now=new Date();
             for (Person person:personList
                  ) {
                 Date checkTime=person.getCheckTime();
                 WarnRecord record = warnRecordMapper.findByType(null, null, person.getId(), 2);
                 if(person.getTime()==null||person.getTime().getTime()!=checkTime.getTime()){
                     SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                     String currentTime = dateFormat.format(checkTime);
                     personService.updatePersonMine(person.getId(),0,currentTime);
                 }
                 if(person.getFinishTime()!=null){
                     record.setWarnstate(1);
                     record.setEndTime(now);
                     warnRecordService.updateWarnRecord(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now), record.getId());
                     kafkaSender.sendEndWarn( record.getId(),record);
                 }


             List<WarnRule> rules = warnRecordService.findByRuleAll(person.getInstanceid(), 2, null,1);
                 if(rules!=null&&rules.size()!=0) {
                     float hour=(now.getTime() - checkTime.getTime()) / 3600000f;
                     float ruleTime=10000000;
                     for (WarnRule rule : rules
                     ) {
                       Float value=  Float.valueOf(rule.getRule());
                        if(value<ruleTime)
                            ruleTime=value;
                     }
                     if ( hour> ruleTime) {
                         System.out.println(person.getName() + "应检超时"+hour);
                         if (record == null) {
                             record = new WarnRecord();
                             record.setType(2);
                             record.setPersonid(person.getId() + "");
                             record.setStartTime(checkTime);
                             record.setMap(0);
                             record.setWarnstate(0);
                             record.setDescribe("样品"+person.getName()+"应检超时");
                             warnRecordService.addWarnRecord(record);
                             //传输报警信息
                             kafkaSender.sendWarn(record.getId());
                         }
                     } else {
                         if (record != null) {
                             record.setWarnstate(1);
                             record.setEndTime(now);
                             warnRecordService.updateWarnRecord(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now), record.getId());
                             kafkaSender.sendEndWarn( record.getId(),record);
                         }
                     }
                 }else {
                     List<WarnRecord> records = warnRecordService.findByRecordAll(person.getInstanceid(),null,null,null,2,0,null,null);
                     for (WarnRecord warnRecord:records
                     ) {
                             warnRecordService.updateWarnRecord(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now),record.getId());
                         kafkaSender.sendEndWarn( record.getId(),record);
                         }
                     }


             }



    }*/
}
