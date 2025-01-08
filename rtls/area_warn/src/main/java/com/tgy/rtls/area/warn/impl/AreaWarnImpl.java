package com.tgy.rtls.area.warn.impl;

import com.tgy.rtls.area.kafka.KafkaSender;
import com.tgy.rtls.area.warn.AreaWarn;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.Eventlog;
import com.tgy.rtls.data.entity.common.EventlogType;
import com.tgy.rtls.data.entity.equip.Substation;
import com.tgy.rtls.data.entity.map.Area;
import com.tgy.rtls.data.entity.message.WarnRecord;
import com.tgy.rtls.data.entity.user.Person;
import com.tgy.rtls.data.mapper.common.EventlogMapper;
import com.tgy.rtls.data.service.common.RecordService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.message.WarnRecordService;
import com.tgy.rtls.data.service.user.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.area.warn.impl
 * @date 2020/10/30
 */
@Service
public class AreaWarnImpl implements AreaWarn {
    //用于区域内的人员 判断区域超员报警
 //   private static ConcurrentHashMap<Integer, List<Person>> areaMap=new ConcurrentHashMap<>();
    @Autowired
    private WarnRecordService warnRecordService;
    @Autowired
    private RecordService recordService;
    @Autowired
    private PersonService personService;
    @Autowired(required = false)
    private EventlogMapper eventlogMapper;
    @Autowired
    private KafkaSender kafkaSender;
    @Autowired
    private SubService subService;
    @Autowired
    private RedisService redisService;

    @Override
    public void overlodaWarn(Boolean state, Area area, Integer maxnum,Person person) {
        //1.判断该人员有没有生成过区域超员报警
        WarnRecord warnRecord=warnRecordService.findByType(area.getMap(),area.getId(),0,3);
        if (state){
            if(NullUtils.isEmpty(warnRecord))
            {
                //人员进入该区域 存储人员信息到areaMap
                List<Person> personList;
                String areaId="area"+area.getId();
                List persons = redisService.getPersonList(areaId);
                if(persons==null){
                    personList = new ArrayList<>();
                } else {
                    personList = persons;
                }
                boolean result=false;
                for (Person p : personList) {
                    if (p.getId().equals(person.getId())){
                        result=true;
                    }
                }
                if (!result){
                    personList.add(person);
                }
                redisService.setPersonList(areaId,personList);


             /*   if (areaMap.get(area.getId()) == null) {
                    personList = new ArrayList<>();
                    areaMap.put(area.getId(),personList);
                } else {
                    personList = areaMap.get(area.getId());
                }
                boolean result=false;
                for (Person p : personList) {
                    if (p.equals(person)){
                        result=true;
                    }
                }
                if (!result){
                    personList.add(person);
                }
                areaMap.put(area.getId(), personList);*/
                //判断该区域内的人员数量有没有达到该区域的人数上限
                if (personList.size() > maxnum) {
                    //生成区域报警信息 type-->3
                    WarnRecord record = new WarnRecord();
                    record.setMap(area.getMap());
                    record.setArea(area.getId());
                    record.setDescribe(area.getName() + "设定人数" + maxnum + "人,实际检测" + personList.size() + "人");//描述
                    record.setType(3);
                    record.setPersonid("0");
                    warnRecordService.addWarnRecord(record);
                    //传输报警信息
                    kafkaSender.sendWarn(record.getId());
                }
            }
        }else {
            if (!NullUtils.isEmpty(warnRecord)){//该区域有区域超员报警报警 且在区域外
                //人员离开该区域 删除areamap的人员信息
                List<Person> personList=null;
                String areaId="area"+area.getId();
                List persons = redisService.getPersonList(areaId);
               if(persons!=null){
                  personList=persons;
                   boolean result=false;
                   for (Person p : personList) {
                       if (p.getId().equals(person.getId())){
                           result=true;
                           person=p;
                       }
                   }
                   if (result){
                       personList.remove(person);
                   }
               }else{
                   personList=new ArrayList<>();
               }
                redisService.setPersonList(areaId,personList);

              /*  List<Person> personList=areaMap.get(area.getId());*/
             /*   if (!NullUtils.isEmpty(personList)) {
                    personList.remove(person);
                    areaMap.put(area.getId(), personList);
                }*/
                //判断该区域内的人员数量有没有达到该区域的人数上限
                if (NullUtils.isEmpty(personList)||personList.size() <= maxnum){
                    //结束报警
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentTime = dateFormat.format(new Date());
                    warnRecordService.updateWarnRecord(currentTime,warnRecord.getId());
                    //2.2.2通知前端结束报警
                    kafkaSender.sendEndWarn(warnRecord.getId(),warnRecord);
                }
            }

        }
    }

    @Override
    public void inAreaRecord(Boolean state, Area area, Integer personid) {
        //1.判断当前区域是否有记录生成且还未结束
        boolean inarea=recordService.findByInArea(personid,area.getId())>0;
        //2.判断是否在区域内
        if(state){
            //2.1 在区域内且还没有记录生成则添加进出区域记录
            if (!inarea){
                //2.1.1生成进入区域的记录
                recordService.addInArea(personid,area.getId(),area.getMap());
                //2.1.2生成进入区域的日志
                Person person=personService.findById(personid);
                Eventlog eventlog=new Eventlog();
                eventlog.setEvent(person.getName()+ LocalUtil.get(KafukaTopics.IN)+area.getTypeName()+"-"+area.getName());
                eventlog.setPersonid(personid);
                eventlog.setMap(area.getMap());
                eventlog.setTypeSimple(3);
               // eventlog.setType(eventlogMapper.findByEventlogType(LocalUtil.get(KafukaTopics.IN)+area.getTypeName()+"-"+area.getName(),person.getInstanceid()));
                eventlogMapper.addEventlog(eventlog);
                //2.1.3传输该区域人数情况
                kafkaSender.sendArea(area.getId());
            }
        }else {
            //2.2 不在区域内且还有记录生成则结束进出区域记录
            if (inarea){
                //2.2.1 结束进入区域的记录
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentTime = dateFormat.format(new Date());
                recordService.updateInArea(personid,area.getId(),currentTime);
                //2.2.2生成离开区域的日志
                Person person=personService.findById(personid);
                Eventlog eventlog=new Eventlog();
                eventlog.setEvent(person.getName()+LocalUtil.get(KafukaTopics.OUT)+area.getTypeName()+"-"+area.getName());
                eventlog.setPersonid(personid);
                eventlog.setMap(area.getMap());
             //   eventlog.setType(eventlogMapper.findByEventlogType(LocalUtil.get(KafukaTopics.OUT)+area.getTypeName()+"-"+area.getName(),person.getInstanceid()));
                eventlog.setTypeSimple(4);
                eventlogMapper.addEventlog(eventlog);
                //2.2.3传输该区域人数情况
                kafkaSender.sendArea(area.getId());
            }
        }
    }

    @Override
    public void inSubRecord(boolean state, Integer personid, String bsid,String oldBsid) {
        Person person=personService.findById(personid);
        if(person==null)
            return;
        //2.2.4存储该人员的出入分站信息
        //1.判断当前分站是否有记录生成且还未结束
        boolean insub=recordService.findByInSub(personid,bsid)>0;
        //2.判断是否和之前所在的分站是不是一样的
        if(!state){
            //不是
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String currentTime = dateFormat.format(date);
            //2.1修改人员所在分站信息  所在分站和进入分站时间
            personService.updatePersonSub(personid,bsid,currentTime);
            //2.2 没有记录生成则添加进出分站记录
            Substation substation = subService.findByNum(bsid);
            if (!insub){
                //2.2.1生成进入区域的记录

                recordService.addInsub(personid,bsid,Integer.valueOf(substation.getMap()));

            }else {
                //2.3 有记录生成则结束进出分站记录 生成一条新的进入分站记录
                //2.3.1 结束进入分站的记录
                recordService.updateInsub(personid,currentTime);
                //2.3.2 生成新的进入分站的记录
              //  recordService.addInsub(personid,bsid,substation.getMap());
            }

            //7.生成进入分站事件日志
            Eventlog eventlog=new Eventlog();
            eventlog.setEvent(person.getName()+LocalUtil.get(KafukaTopics.IN)+bsid+LocalUtil.get(KafukaTopics.SUB));
            eventlog.setPersonid(person.getId());
            eventlog.setMap(person.getMap());
            eventlog.setTypeSimple(5);
           // eventlog.setType(null);
            eventlog.setBsid(bsid);
            eventlog.setTime(date);
            eventlogMapper.addEventlog(eventlog);
            if(oldBsid!=null&&!oldBsid.equals(bsid)) {
                EventlogType oldEvent = eventlogMapper.findByTypeSimpleBsid(oldBsid, personid);
                if (oldEvent != null && oldBsid != null) {
                    Eventlog eventlog1 = new Eventlog();
                    eventlog1.setEvent(person.getName() +LocalUtil.get(KafukaTopics.OUT) + oldBsid + LocalUtil.get(KafukaTopics.SUB));
                    eventlog1.setPersonid(person.getId());
                    eventlog1.setMap(person.getMap());
                    eventlog1.setTypeSimple(6);
                 //   eventlog1.setType(null);
                    eventlog1.setTime(date);
                    eventlogMapper.addEventlog(eventlog1);
                }
            }


        }
    }
}
