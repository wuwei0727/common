package com.tgy.rtls.area.kafka;

import com.tgy.rtls.area.warn.EquipWarn;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.equip.Substation;
import com.tgy.rtls.data.entity.equip.Tag;
import com.tgy.rtls.data.entity.user.Person;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.equip.TagService;
import com.tgy.rtls.data.service.lock.impl.RedissonDistributedLocker;
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
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @author 许强
 * @Package com.tgy.rtls.area.kafka
 * @date 2020/11/3
 * 设备相关报警和记录
 */
@Service(value = "/equipWarn")
public class KafkaEquipWarn {
    private Logger logger = LoggerFactory.getLogger(KafkaEquipWarn.class);
    @Autowired
    private EquipWarn equipWarn;
    @Autowired
    private PersonService personService;
    @Autowired
    private TagService tagService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private InstanceService instanceService;
    @Autowired
    private SubService subService;

    @Autowired
    private RedissonDistributedLocker redissonDistributedLocker;
    private double batter=0.15;//标签电压变化临界值
    /*
     * 传感数据接收
     * */
    @KafkaListener(topics = {KafukaTopics.TAG_SENSOR})
    public void tagsensor(ConsumerRecord<?, ?> record) {
        Executor executor = SpringContextHolder.getBean("threadPool1");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Optional<?> kafkaMessage = Optional.ofNullable(record.value());
                    if (kafkaMessage.isPresent()) {
                        Object message = kafkaMessage.get();
                     //  logger.info("------------------ 接收到标签传感器数据 =" + message);
                        JSONObject json = JSONObject.fromObject(message);
                        String tagid = json.getString("tagid");
                        double power = (double)Math.round(json.getDouble("power") * 10) / 10;//电量
                        double temper = json.getDouble("temper");//温度
                        int broken = json.getInt("broken");//破拆 0正常 1破拆
                        int moveState = json.getInt("moveState");//运动状态 0静止 1运动
                        int heart = json.getInt("heart");//0心率
                        int sos = json.getInt("sos");//0不报警 1报警
                        long time=json.getLong("time");
                        //1.判断标签是否属于当前项目-->通过识别码判断
                        //修改标签电压信息
                        redissonDistributedLocker.lock("sensor"+tagid, TimeUnit.SECONDS,5);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Tag tag=tagService.findByNum(tagid);
                        if (!NullUtils.isEmpty(tag)) {
                            if (NullUtils.isEmpty(tag.getBatteryVolt())) {
                                //修改电压信息
                                tagService.updateTagBatteryTime(tagid,power,dateFormat.format(time));
                            }else {
                                //如果电压变化超过了临界值就修改
                               boolean result=Math.abs(Double.parseDouble(tag.getBatteryVolt())-power)>=batter;
                               if (result){
                                   tagService.updateTagBatteryTime(tagid,power,dateFormat.format(time));
                                }
                            }
                        }
                        //1.判断标签是否绑定了人员
                        Person person = personService.findByTagNum(tagid);
                        if (!NullUtils.isEmpty(person)) {             //绑定了人
                            //2.设备报警判断
                            //2.1SOS报警
                            equipWarn.sosWarn(person, sos);
                            //2.2低电量报警-->如果人员没有绑定到地图则不判断 因为低电量的标准会根据地图变化
                            if (!NullUtils.isEmpty(person.getMap())) {
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
                                equipWarn.powerWarn(person, power);
                            }
                        }
                        redissonDistributedLocker.unlock("sensor"+tagid);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*
     * 基站状态接收做分站报警
     * */
    @KafkaListener(topics = {KafukaTopics.BS_STATE})
    public void bsstate(ConsumerRecord<?, ?> record) {
        Executor executor = SpringContextHolder.getBean("threadPool1");
        executor.
        execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Optional<?> kafkaMessage = Optional.ofNullable(record.value());
                    if (kafkaMessage.isPresent()) {
                        Object message = kafkaMessage.get();
                       // logger.info("------------------ 接收到基站状态数据 =" + message);
                        JSONObject json = JSONObject.fromObject(message);

                        int type = 1;//1煤炭 2uwb
                        //根据不同类型的基站走不同分支
                        if (type == 1) {

                        String bsid = json.getString("bsid");//基站编号
                            redissonDistributedLocker.lock("bsid"+bsid, TimeUnit.SECONDS,5);
                        Substation sub = subService.findByNum(bsid);
                        if (!NullUtils.isEmpty(sub)&&!NullUtils.isEmpty(sub.getMap())) {
                                int errorCode = json.getInt("errorCode");//异常码 1：供电状态   2：网络状态  3：CAN口状态  4：UWB状态
                                int state = json.getInt("state");//状态 0正常 1异常
                                if (errorCode==1){   //1.供电状态
                                    //1.1分站在线
                                    equipWarn.subNetworkstate(false, sub);
                                    if (state==0) {//1.2正常
                                        equipWarn.subPowerstate(false,sub);
                                    }else {//1.3异常
                                            equipWarn.subPowerstate(true,sub);
                                    }
                                }else if (errorCode==2){//2.网络状态
                                    if (state==0) {//2.1正常
                                        equipWarn.subNetworkstate(false,sub);
                                    }else {//2.2异常
                                        equipWarn.subNetworkstate(true,sub);
                                    }
                                }
                            }
                            redissonDistributedLocker.unlock("bsid"+bsid);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.info("基站状态数据异常");
                }
            }
        });
    }
}
