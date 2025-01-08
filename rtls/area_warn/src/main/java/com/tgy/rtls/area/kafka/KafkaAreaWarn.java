package com.tgy.rtls.area.kafka;


import com.tgy.rtls.area.util.TimeUtil;
import com.tgy.rtls.area.warn.*;
import com.tgy.rtls.data.algorithm.ArithmeticlUtil;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.common.Point2d;
import com.tgy.rtls.data.entity.equip.Substation;
import com.tgy.rtls.data.entity.equip.Tag;
import com.tgy.rtls.data.entity.map.*;
import com.tgy.rtls.data.entity.message.WarnRecord;
import com.tgy.rtls.data.entity.routing.Routedot;
import com.tgy.rtls.data.entity.routing.Routerecord;
import com.tgy.rtls.data.entity.user.Person;
import com.tgy.rtls.data.mapper.message.WarnRecordMapper;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.equip.TagService;
import com.tgy.rtls.data.service.lock.impl.RedissonDistributedLocker;
import com.tgy.rtls.data.service.map.AreaRuleService;
import com.tgy.rtls.data.service.map.AreaService;
import com.tgy.rtls.data.service.routing.RouteRecordService;
import com.tgy.rtls.data.service.user.InstanceService;
import com.tgy.rtls.data.service.user.PersonService;
import net.sf.json.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.kafka
 * @date 2020/10/23
 * 区域相关报警和记录
 */
@Service(value = "/areaWarn")
public class KafkaAreaWarn {
    private Logger logger = LoggerFactory.getLogger(KafkaAreaWarn.class);
   // private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
    @Autowired
    private PersonService personService;
    @Autowired
    private SubService subService;
    @Autowired
    private TagService tagService;
    @Autowired
    private AreaService areaService;
    @Autowired
    private AreaRuleService areaRuleService;
    @Autowired
    private DetectionWarn detectionWarn;
    @Autowired
    private IncoalWarn incoalWarn;
    @Autowired
    private AreaWarn areaWarn;
    @Autowired
    private TurnoverWarn turnoverWarn;
    @Autowired
    private RouteRecordService routeRecordService;
    @Autowired(required = false)
    private WarnRecordMapper warnRecordMapper;
    @Autowired
    private KafkaSender kafkaSender;
    @Autowired
    private RedisService redisService;
    @Autowired
    private InstanceService instanceService;
    @Autowired
    private EquipWarn equipWarn;
    @Autowired
    private RedissonDistributedLocker redissonDistributedLocker;
    @Autowired
    private GatherWarn gatherWarn;
    //用于存储区域点
    public static ConcurrentHashMap<String, LinkedBlockingDeque<Point2d>> detectionMap = new ConcurrentHashMap<>();
   // private static ConcurrentHashMap<String, Person> mapPeople = new ConcurrentHashMap<>();

    /*
     * 定位数据接收
     * */
    @KafkaListener(topics = {KafukaTopics.TAG_LOCATION})
    public void taglocation(ConsumerRecord<?, ?> record) {
        Executor executor = SpringContextHolder.getBean("threadPool1");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Optional<?> kafkaMessage = Optional.ofNullable(record.value());
                    if (kafkaMessage.isPresent()) {
                        Object message = kafkaMessage.get();
                      //  logger.info("------------------ 接收到标签定位数据用于报警判断 =" + message);
                        JSONObject json = JSONObject.fromObject(message);
                        String tagid = json.getString("tagid");//标签编号
                        int type = json.getInt("type");//定位类型  0：室内三维坐标 1：GPS，GPS时，x，y为经纬度，z为精度 2：蓝牙 3 uwb室内定位
                        //根据不同类型做不同的数据处理
                        double x = 0.0;
                        double y = 0.0;
                        double z = 0.0;
                        String bsid = null;//type=1时判断人员所在的分站
                        String floor=null;
                        Integer map = 0;
                        if (type == 0) {
                            x = json.getDouble("x");
                            y = json.getDouble("y");
                            // z = json.getDouble("z");
                            bsid = json.getString("bsid");//分站编号
                            floor=json.getString("floor").trim().isEmpty()?null:json.getString("floor");
                            //2.2判断区域属于哪个地图
                            Substation substation = subService.findByNum(bsid);
                            if (NullUtils.isEmpty(substation)){//没有找到地图信息 结束
                                return;
                            }
                            map = Integer.valueOf(substation.getMap());
                        } else if (type == 3) {//DWM1001定位
                            String position = json.getString("position");
                            JSONObject obj2 = JSONObject.fromObject(position);
                            x = Double.valueOf(String.format("%.2f", Double.valueOf(obj2.getString("x"))));
                            y = Double.valueOf(String.format("%.2f", Double.valueOf(obj2.getString("y"))));
                            //z = Double.valueOf(String.format("%.2f", Double.valueOf(obj2.getString("z"))));
                            map = json.getInt("map");
                            floor=json.getString("floor").trim().isEmpty()?null:json.getString("floor");
                        }
                        //判断该定位数据是否符合条件
                        //标签有没有和人员绑定
                        redissonDistributedLocker.lock("locationtagid"+tagid, TimeUnit.SECONDS,5);
                        Person person = personService.findByTagNum(tagid);
                        if (NullUtils.isEmpty(person)) {
                            redissonDistributedLocker.unlock("locationtagid"+tagid);
                            return;
                        }
                     /*   if (NullUtils.isEmpty(mapPeople.get(tagid))) {
                            mapPeople.put(tagid, person);
                        }*/
                       // synchronized (mapPeople.get(tagid))

                        {
                            //分站有没有关联到地图
                            if (map != 0) {
                                if (type == 0) {
                                    //人员进出分站记录统计
                                    boolean state = true;
                                    person.setMap(map);
                                    if (NullUtils.isEmpty(person.getSub())) {
                                        state = false;
                                    } else {
                                        state = person.getSub().equals(bsid);
                                    }
                                    areaWarn.inSubRecord(state, person.getId(), bsid, person.getSub());
                                    logger.info("tagid"+tagid+"occour bs change old bs "+person.getSub()+"new is"+bsid);
                                    //分站超员判断 分站发送 变化时
                                    if (!state)
                                    {
                                        equipWarn.subOverload(bsid,map);
                                        equipWarn.subOverload(person.getSub(),map);
                                    }else{

                                    }
                                }
                                //判断该人员是否符合判断井下超时报警的条件 ：状态为：井下 在线
                                if (person.getMinestate() == 0 && person.getStatus() == 1){
                                    //判断该人员的井下超时报警
                                    incoalWarn.incoalOvertime(person.getId(), map);
                                }

                                //遍历该人员当天的巡检路线和巡检点  判断是否有工作异常
                                routeRecord(x, y, person, map,floor);
                                //1.存储当前点带出入口检测map中
                                //1.1根据标签的频率 决定存储点的数量
                                Tag tag = tagService.findByNum(tagid);
                                Point2d p1 = new Point2d(x, y,person.getName(),floor,map+"");
                                p1.setTagid(tagid);
                                if (detectionMap.get(tagid) == null) {
                                    LinkedBlockingDeque<Point2d> queue = new LinkedBlockingDeque<>();
                                    queue.push(p1);
                                    detectionMap.put(tagid, queue);
                                } else {
                                    LinkedBlockingDeque<Point2d> queue = detectionMap.get(tagid);
                                    if(tag==null)
                                        return;
                                    if (queue.size() < tag.getFrequency()) {
                                        queue.push(p1);
                                    } else {
                                        //1.2遍历区域 并生成相关记录
                                        queue.push(p1);
                                        queue.pollLast();
                                        Object[] posList=   queue.toArray();
                                        areaRecord(posList, person, map,floor);
                                        //gatherWarn.getGatherInfo(detectionMap);

                                    /*    //删除存储的点
                                        detectionMap.remove(tagid);*/
                                    }
                                }
                            }
                        }
                        redissonDistributedLocker.unlock("locationtagid"+tagid);
                    }
                } catch (Exception e) {
                    logger.info("定位数据接收异常" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    /*
     * 定位卡与基站的测距信息
     * */
    private int range = 1;//出入口判断的临界值

    @KafkaListener(topics = {KafukaTopics.TAG_RANGE})
    public void tagrange(ConsumerRecord<?, ?> record) {
        Executor executor = SpringContextHolder.getBean("threadPool1");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Optional<?> kafkaMessage = Optional.ofNullable(record.value());
                    if (kafkaMessage.isPresent()) {
                        Object message = kafkaMessage.get();
                  //      logger.info("------------------ 接收到标签测距数据用于出入口检测 =" + message);

                        JSONObject json = JSONObject.fromObject(message);
                        String tagid = json.getString("tagid");//标签编号
                        String bsid = json.getString("bsid");//分站编号
                        //1.标签绑定人员情况
                        redissonDistributedLocker.lock("tagrange"+tagid,TimeUnit.SECONDS,5);
                        Person person = personService.findByTagNum(tagid);
                        if (NullUtils.isEmpty(person)) {
                            return;
                        }
                      /*  if (NullUtils.isEmpty(mapPeople.get(tagid))) {
                            mapPeople.put(tagid, person);
                        }*/
                        Substation substation = subService.findByNum(bsid);

                       // synchronized (mapPeople.get(tagid))

                        {
                            //2.查询分站信息
                            if (!NullUtils.isEmpty(substation) && substation.getMap() != null) {//要求是出入口分站
                                Area area = new Area();
                                area.setMap(Integer.valueOf(substation.getMap()));
                                if(substation.getType() == 2 )
                                {
                                    logger.info(person.getName()+"------------------ 接收到标签测距数据用于出入口检测 out" );
                                    detectionWarn.exitWarn(true, person, area);
                                }
                                else
                                {
                                    logger.info(person.getName()+"------------------ 接收到标签测距数据用于出入口检测 in" );
                                    detectionWarn.entranceWarn(true, person, area);
                                }


                            }
                        }

                        redissonDistributedLocker.unlock("tagrange"+tagid);
                    }
                } catch (Exception e) {
                    logger.info("定位数据接收异常" + e.getMessage());
                    e.printStackTrace();
                }

            }
        });
    }

/*    *//*
     * 定位卡与基站的测距信息
     * *//*
    private int range = 1;//出入口判断的临界值

    @KafkaListener(topics = {KafukaTopics.TAG_RANGE})
    public void tagrange(ConsumerRecord<?, ?> record) {
        scheduledExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Optional<?> kafkaMessage = Optional.ofNullable(record.value());
                    if (kafkaMessage.isPresent()) {
                        Object message = kafkaMessage.get();
                        logger.info("------------------ 接收到标签测距数据用于出入口检测 =" + message);
                        JSONObject json = JSONObject.fromObject(message);
                        String tagid = json.getString("tagid");//标签编号
                        String bsid = json.getString("bsid");//分站编号
                        int type = json.getInt("type");//0: 煤矿  1：平面定位  2：蓝牙
                        int lr = json.getInt("lr");//lr 0 左 1 右
                        double dis = json.getDouble("dis");//距离
                        if (dis < range) {//距离不到临界值不处理
                            return;
                        }
                        //1.标签绑定人员情况
                        Person person = personService.findByTagNum(tagid);
                        if (NullUtils.isEmpty(person)) {
                            return;
                        }
                        if (NullUtils.isEmpty(mapPeople.get(tagid))) {
                            mapPeople.put(tagid, person);
                        }
                        Substation substation = subService.findByNum(bsid);
                        synchronized (mapPeople.get(tagid)) {
                            //2.查询分站信息
                            if (!NullUtils.isEmpty(substation) && substation.getType() == 2 && substation.getMap() != null) {//要求是出入口分站
                                //判断该标签在出口还是入口
                                int exit = 1;//1出口 2入口
                                if (substation.getExitDirection() == 1) {//规定左边是出口
                                    if (lr == 1) {//入口
                                        exit = 2;
                                    }
                                } else {//规定右边是出口
                                    if (lr == 0) {//入口
                                        exit = 2;
                                    }
                                }
                                Area area = new Area();
                                area.setMap(substation.getMap());
                                if (exit == 1) {
                                    //出口检测
                                    detectionWarn.exitWarn(true, person, area);
                                } else {
                                    //入口检测
                                    detectionWarn.entranceWarn(true, person, area);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.info("定位数据接收异常" + e.getMessage());
                    e.printStackTrace();
                }

            }
        });
    }*/


    /*
     * 遍历区域 生成相关记录 出入口  进出报警  超员报警
     * */
    private void areaRecord(Object[]  posList, Person person, Integer map,String floor) {
        //1.获取当前地图的区域 启用状态
        List<Area> areas = areaService.findByMap(map);
        //1.1遍历区域
        for (Area area : areas) {
            //1.1.1判断点是否在区域里
            List<AreaDot> dots = area.getDots();
            if((floor==null||floor.trim().isEmpty())&&area.getFloor()==null||area.getFloor().shortValue()==Short.valueOf(floor)) {
                if (NullUtils.isEmpty(dots)) {
                    continue;
                }
                List<Point2d> points = new ArrayList<>();
                for (AreaDot dot : dots) {
                    Point2d p = new Point2d(dot.getX(), dot.getY());
                    points.add(p);
                }
                //状态 true-->在区域内  false-->不在
                boolean state = isBoolean(posList, points);
                //1.1.2记录进出区域信息
                areaWarn.inAreaRecord(state, area, person.getId());
                //1.2规则信息
                //1.2.1入口区
                List<AreaDetection> entranceArea = areaRuleService.findByDetectionEnable(area.getId(), 0);
                //1.2.2出口区
                List<AreaDetection> exitArea = areaRuleService.findByDetectionEnable(area.getId(), 1);
                //1.2.3超员报警区
                List<AreaOverload> overloadArea = areaRuleService.findByOverloadEnable(area.getId());
                //1.2.4进入报警区
                List<AreaTurnover> intoArea = areaRuleService.findByTurnoverEnable(area.getId(), 1);
                //1.2.5离开报警区
                List<AreaTurnover> outArea = areaRuleService.findByTurnoverEnable(area.getId(), 0);
                //2遍历该区域的规则
                //2.1入口区规则判断
                for (AreaDetection entrance : entranceArea) {
                    //判断该规则是否在生效时间
                    if (TimeUtil.RuleEfficient(entrance.getStartTime(), entrance.getEndTime())) {
                        //  detectionWarn.entranceWarn(state, person, area);
                    }
                }
                //2.2出口区规则判断
                for (AreaDetection exit : exitArea) {
                    if (TimeUtil.RuleEfficient(exit.getStartTime(), exit.getEndTime())) {
                        // detectionWarn.exitWarn(state, person, area);
                    }
                }
                //2.3超员报警规则判断
                for (AreaOverload overload : overloadArea) {
                    if (TimeUtil.RuleEfficient(overload.getStartTime(), overload.getEndTime())) {
                        areaWarn.overlodaWarn(state, area, overload.getMaxnum(), person);
                    }
                }
                //2.4离开区规则判断
                for (AreaTurnover out : outArea) {
                    if (TimeUtil.RuleEfficient(out.getStartTime(), out.getEndTime())) {
                        turnoverWarn.outWarn(state, person, area, out.getId());
                    }
                }
                //2.5进入去规则判断
                for (AreaTurnover into : intoArea) {
                    if (TimeUtil.RuleEfficient(into.getStartTime(), into.getEndTime())) {
                        turnoverWarn.intoWarn(state, person, area, into.getId());
                    }
                }
            }
        }
        //判断完后清除map
    }

    /*
     * 查找该人员当天的巡检路线
     * */
    private void routeRecord(Double x, Double y, Person person, Integer map,String floor) {
       // logger.info(person.getName() + "巡检任务判断");
        //1.判断当天该人员是否有巡检任务
        String month = new SimpleDateFormat("yyyyMM").format(new Date());//年月
        int day = Integer.parseInt(new SimpleDateFormat("dd").format(new Date()));//天
        List<Routedot> routedots = routeRecordService.findByRoutedotId(month, day, person.getId());
        if (!NullUtils.isEmpty(routedots)) {
            //2.遍历巡检点
            for (Routedot routedot : routedots) {
                //3.判断是否到达了巡检点检测时间
                if(floor==null&&routedot.getFloor()==null||routedot.getFloor().shortValue()==Short.valueOf(floor)) {
                    String time = new SimpleDateFormat("HH:mm").format(new Date());
                    Integer current = Integer.valueOf(time.replace(":", ""));
                    Integer start = Integer.valueOf(routedot.getStartTime().replace(":", ""));
                    Integer end = Integer.valueOf(routedot.getEndTime().replace(":", ""));
                    if (current >= start && current <= end) {
                        //4.判断是否在巡检点范围内
                        double dis = Math.sqrt(Math.abs((routedot.getX() - x) * (routedot.getX() - x) + (routedot.getY() - y) * (routedot.getY() - y)));
                        if (dis < routedot.getRange()) {//在范围内
                            //4.1判断是否有正常的记录生成
                            Routerecord routerecord = routeRecordService.findByRouteRecordId(month, day, person.getId(), routedot.getId());
                            if (NullUtils.isEmpty(routerecord)) {
                                //4.2生成巡检点记录-->正常
                                Routerecord record = new Routerecord();
                                record.setMonth(month);
                                record.setDay(day);
                                record.setPersonid(person.getId());
                                record.setRdid(routedot.getId());
                                record.setStatus(1);
                                record.setArriveTime(time);
                                routeRecordService.addRouteRecord(record);
                            }
                        }
                    } else if (current > end) {
                        //5.如果超过了结束时间还没生成正常的巡检点记录那么就认为工作异常
                        //5.1查询人员当天的巡检记录
                        Routerecord routerecord = routeRecordService.findByRouteRecordId(month, day, person.getId(), routedot.getId());
                        if (NullUtils.isEmpty(routerecord)) {
                            //4.2生成巡检点记录-->异常
                            Routerecord record = new Routerecord();
                            record.setMonth(month);
                            record.setDay(day);
                            record.setPersonid(person.getId());
                            record.setRdid(routedot.getId());
                            record.setStatus(0);
                            routeRecordService.addRouteRecord(record);
                            //4.3生成报警记录
                   /*     WarnRecord warnRecord = new WarnRecord();
                        warnRecord.setMap(map);
                        warnRecord.setArea(0);
                        warnRecord.setDescribe(person.getName() + "未按时间到达巡检点" + routedot.getName());//描述
                        warnRecord.setPersonid(String.valueOf(person.getId()));
                        warnRecord.setType(12);
                        warnRecordMapper.addWarnRecord(warnRecord);*/
                            //4.4推送报警记录数据
                            // kafkaSender.sendWarn(record.getId());
                            warnRecordMapper.updateWarnRecord(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), record.getId());
                        }
                    }
                }
            }
        }
    }

    /*
     * 点在区域的判断方法
     * */
    private static Boolean isBoolean(Object[]  points, List<Point2d> points1) {
        int num = 0;
        try {
            for (Object o:points
                 ) {
                Point2d pos=(Point2d) o;
                if (ArithmeticlUtil.isInPolygon(pos, points1)) {
                    num++;
                }
            }

        } catch (Exception e) {
            //logger.error(e.getMessage(), e);
            return false;
        }

        return num > (points.length / 2);
    }


    /*
    * 报警规则修改时触发
    * */
    @KafkaListener(topics = {"warnRule"})
    public void warnRule(ConsumerRecord<?, ?> record){Executor executor = SpringContextHolder.getBean("threadPool1");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Optional<?> kafkaMessage = Optional.ofNullable(record.value());
                    if (kafkaMessage.isPresent()) {
                        Object message = kafkaMessage.get();
                     //   logger.info("------------------ 接收到报警规则修改操作,进行相关报警的判断：" + message);
                        JSONObject json = JSONObject.fromObject(message);
                        int type=json.getInt("type");
                        int map=json.getInt("map");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String currentTime = dateFormat.format(new Date());
                        if (type==1) {//1.井下超员
                            //1.1清除当前地图的井下超员报警
                            WarnRecord warnRecord=warnRecordMapper.findByType(map,0,null,1);
                            if (!NullUtils.isEmpty(warnRecord)) {
                                warnRecordMapper.updateWarnRecord(currentTime, warnRecord.getId());
                                //1.1.2通知前端结束报警
                                kafkaSender.sendEndWarn(warnRecord.getId(),warnRecord);
                            }
                            //1.2重新判断
                            incoalWarn.incoalOverload(map);
                        }else if (type==2){//2.井下超时
                            //2.1清除当前地图所有的井下超时报警
                            List<WarnRecord> warnRecords=warnRecordMapper.findByWarnType(2,map);
                            for (WarnRecord warnRecord:warnRecords){
                                warnRecordMapper.updateWarnRecord(currentTime, warnRecord.getId());
                                //2.1.2通知前端结束报警
                                kafkaSender.sendEndWarn(warnRecord.getId(),warnRecord);
                            }
                            //2.2找到当前还在井下的人，判断是否触发超时报警
                            List<Person> personList=personService.findByInCoal(map);
                            for (Person person:personList){
                                incoalWarn.incoalOvertime(person.getId(), map);
                            }
                        }else if(type==3){//3.离线报警
                            //3.1清除当前地图所有的离线报警
                            List<WarnRecord> warnRecords=warnRecordMapper.findByWarnType(10,map);
                            for (WarnRecord warnRecord:warnRecords){
                                warnRecordMapper.updateWarnRecord(currentTime, warnRecord.getId());
                                //3.1.2通知前端结束报警
                                kafkaSender.sendEndWarn(warnRecord.getId(),warnRecord);
                            }
                            //3.2找到当前地图上的离线人数，判断是否触发报警
                            List<Person> personList=personService.findByPersonOff(map);
                            for (Person person:personList){
                                incoalWarn.personOffLine(person.getId(), map);
                            }
                        }else if (type==4){//4.低电量报警
                            //4.1清除当前所有低电量报警
                            List<WarnRecord> warnRecords=warnRecordMapper.findByWarnType(7,0);
                            for (WarnRecord warnRecord:warnRecords){
                                warnRecordMapper.updateWarnRecord(currentTime, warnRecord.getId());
                                //4.1.2通知前端结束报警
                                kafkaSender.sendEndWarn(warnRecord.getId(),warnRecord);
                            }
                            //4.2找到当前地图上的在线数，判断是否触发报警
                            List<Tag> tags=tagService.findByTagOnLine(map);
                            for (Tag tag:tags){
                                double power= Double.parseDouble(tag.getBatteryVolt());
                                if (power >= 4.1) {
                                    power = 100;
                                } else if (power >= 4.05) {
                                    power = 95;
                                } else if (power >= 4.0) {
                                    power = 90;
                                } else if (power >= 3.95) {
                                    power = 85;
                                } else if (power >= 3.9) {
                                    power = 80;
                                } else if (power >= 3.85) {
                                    power = 70;
                                } else if (power >= 3.8) {
                                    power = 60;
                                } else if (power >= 3.75) {
                                    power = 50;
                                } else if (power >= 3.7) {
                                    power = 40;
                                } else if (power >= 3.65) {
                                    power = 30;
                                } else if (power >= 3.6) {
                                    power = 20;
                                } else if (power >= 3.55) {
                                    power = 10;
                                } else {
                                    power = 1;
                                }
                                Person person = personService.findByTagNum(tag.getNum());
                                equipWarn.powerWarn(person, power);
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /*
     * 分站修改时触发
     * */
    @KafkaListener(topics = {"subWarn"})
    public void subWarn(ConsumerRecord<?, ?> record){
        Executor executor = SpringContextHolder.getBean("threadPool1");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Optional<?> kafkaMessage = Optional.ofNullable(record.value());
                    if (kafkaMessage.isPresent()) {
                        Object message = kafkaMessage.get();
                      //  logger.info("------------------ 接收到分站修改操作,进行相关报警的判断：" + message);
                        JSONObject json = JSONObject.fromObject(message);
                        String num=json.getString("num");
                        int map=json.getInt("map");
                        //分站超员判断
                        equipWarn.subOverload(num,map);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /*
    * 区域修改是触发
    * */
    @KafkaListener(topics = {"areaWarn"})
    public void areaWarn(ConsumerRecord<?, ?> record){
        Executor executor = SpringContextHolder.getBean("threadPool1");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Optional<?> kafkaMessage = Optional.ofNullable(record.value());
                    if (kafkaMessage.isPresent()) {
                        Object message = kafkaMessage.get();
                   //     logger.info("------------------ 接收到分站修改操作,进行相关报警的判断：" + message);
                        JSONObject json = JSONObject.fromObject(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
