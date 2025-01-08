package com.tgy.rtls.check.kafka;

import com.tgy.rtls.check.Utils.CheckDataProcess;
import com.tgy.rtls.check.controller.TagCheck;
import com.tgy.rtls.check.entity.TagCache;
import com.tgy.rtls.check.entity.TagValidate;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.entity.check.BserrorcodetestrecordEntity;
import com.tgy.rtls.data.kafukaentity.BsPara;
import com.tgy.rtls.data.kafukaentity.TagLocation;
import com.tgy.rtls.data.kafukaentity.TagPara;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component("checkfka")
public class KafukaListener {
    Logger logger = LoggerFactory.getLogger(KafukaListener.class);

@Autowired
    CheckDataProcess checkDataProcess;



    @KafkaListener(topics = {KafukaTopics.TAG_LOCATION})
    public void listenTagLocation(ConsumerRecord<?, ?> record) {
            if(TagCheck.addFile>0) {
            /*  Executor executor = SpringContextHolder.getBean("checkthreadPool1");

                executor.execute(new Runnable() {
                    @Override
                    public void run() {*/

                            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
                            if (kafkaMessage.isPresent()) {
                                Object message = kafkaMessage.get();
                                // logger.error(new Timestamp(new Date().getTime())+":message:" + message);
                                JSONObject json = JSONObject.fromObject(message);
                                TagLocation tagLocation = (TagLocation) JSONObject.toBean(json, TagLocation.class);

                                if (TagCheck.addFile == 1) {
                                    //logger.error("缓存添加"+new Timestamp(tagLocation.getTime()) + ":message:" + message);
                                   String tag= tagLocation.getTagid()+"";
                                    TagCheck.filterRes.putIfAbsent(tag,tagLocation);
                                } else {
                                    checkDataProcess.processTagLocation(tagLocation);
                                    logger.error("统计添加"+new Timestamp(tagLocation.getTime()) + ":message:" + message);
                                 if(TagCheck.filterRes.size()!=0) {
                                     logger.error("缓存开始时间"+new Timestamp(tagLocation.getTime()) );
                                     Iterator<Map.Entry<String, TagLocation>> iter = TagCheck.filterRes.entrySet().iterator();
                                     int kk = 0;
                                     ConcurrentHashMap<String, TagCache> tagCacheList=new ConcurrentHashMap<>();
                                     while (iter.hasNext()) {
                                         TagLocation tagLocation1 = iter.next().getValue();
                                         tagCacheList.putIfAbsent(tagLocation1.getBsid()+"",new TagCache());
                                         TagCache tagCache1 = tagCacheList.get(tagLocation1.getBsid() + "");
                                         tagCache1.listTaglocation.add(tagLocation1);
                                         tagCache1.set.add(tagLocation1.getTagid()+"");
                                        // checkDataProcess.processTagLocation(tagLocation1);
                                        // kk++;
                                       //  logger.error("缓存取出" + ":message:" + tagLocation1.getTagid() + "剩余" + kk);
                                     }
                                     TagCheck.filterRes.clear();
                                     Iterator<Map.Entry<String, TagCache>> iter1 = tagCacheList.entrySet().iterator();
                                     System.out.println("缓存处理开始时间："+new Timestamp(new Date().getTime()));
                                     while (iter1.hasNext()){
                                         Map.Entry<String, TagCache> entry = iter1.next();
                                         TagCache tagCache = entry.getValue();
                                         String bsid = entry.getKey();
                                       checkDataProcess.processTagCache(Long.valueOf(bsid).longValue(),tagCache.listTaglocation,tagCache.set);
                                     }
                                     System.out.println("缓存处理结束时间："+new Timestamp(new Date().getTime()));

                                     logger.error("缓存结束时间"+new Timestamp(tagLocation.getTime()) );

                                 }

                                    /*   while (TagCheck.filterRes.size() > 0) {
                                        TagLocation tagLocation1 = TagCheck.filterRes.poll();
                                        checkDataProcess.processTagLocation(tagLocation1);
                                        logger.error("缓存取出"+ ":message:" + tagLocation1.getTagid());
                                    }*/
                                    //
                                }

                            }
               /*         }

                });*/
            }
    }
   /* @KafkaListener(topics = {KafukaTopics.TAG_SENSOR})
    public void listenTagSensor(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            logger.debug("message:" + message);
            JSONObject json = JSONObject.fromObject(message);
            TagSensor tagSensor = (TagSensor) JSONObject.toBean(json, TagSensor.class);
            checkDataProcess.processTagSensor(tagSensor);

        }
    }*/

    @KafkaListener(topics = {KafukaTopics.BS_CONTROLRES})
    public void listenBsConfig(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            logger.debug("message:" + message);
            JSONObject json = JSONObject.fromObject(message);
            BsPara bsPara = (BsPara) JSONObject.toBean(json, BsPara.class);
            String keyOrder=bsPara.getKeyOrder();
            switch (keyOrder){
                case "bsslotinf" :
                    String bsid= bsPara.getBsid()+"";
                    if(TagCheck.bsState.containsKey(bsid))
                        TagCheck.bsState.replace(bsid,true);
                    break;
            }


        }
    }


    @KafkaListener(topics = {KafukaTopics.TAG_CONTROLRES})
    public void listenTagConfig(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            logger.debug("message:" + message);
            JSONObject json = JSONObject.fromObject(message);
            TagPara tagPara = (TagPara) JSONObject.toBean(json, TagPara.class);
            String keyOrder=tagPara.getKeyOrder();
            switch (keyOrder){
                case "mode" :
                   String tagid= tagPara.getTagid()+"";
                    if(TagCheck.tagState.containsKey(tagid))
                        TagCheck.tagState.replace(tagid,new TagValidate(new Date()));
                    break;
            }


        }
    }

    public static void main(String[] args) {
        BserrorcodetestrecordEntity s=new BserrorcodetestrecordEntity();
        s.setStart(new Date());
        String ss=s.toString();
        JSONArray array=new JSONArray();
        array.add(s);
        String jsonstring=array.toString();
        System.out.println(ss);
    }





}
