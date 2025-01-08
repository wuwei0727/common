package com.tgy.rtls.area.kafka;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.map.AreaVO;
import com.tgy.rtls.data.entity.message.WarnRecord;
import com.tgy.rtls.data.entity.message.WarnRule;
import com.tgy.rtls.data.service.map.AreaService;
import com.tgy.rtls.data.service.message.WarnRecordService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.kafka
 * @date 2020/10/23
 * 发送消息
 */
@Component
public class KafkaSender {
    @Autowired(required = false)
    private  KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private WarnRecordService warnRecordService;
    @Autowired
    private AreaService areaService;


    /*
     * 传输报警信息给前端id-->报警信息id
     * */
    public void sendWarn(Integer id){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        WarnRecord warnRecord=warnRecordService.findByRecordId(id);
        JSONObject objectdata = new JSONObject();
        objectdata.put("id",id);//报警信息编号
        objectdata.put("startTime",dateFormat.format(warnRecord.getStartTime()));
        objectdata.put("warntype",warnRecord.getType());
        objectdata.put("typeName",warnRecord.getTypeName());
        objectdata.put("describe",warnRecord.getDescribe());
        objectdata.put("area",warnRecord.getArea());
        objectdata.put("personid",warnRecord.getPersonid());
        objectdata.put("tagid",warnRecord.getTagid());//标签id
        objectdata.put("warnstate",0);//报警状态 0报警 1结束
        JSONObject objectWarn=new JSONObject();
        objectWarn.put("data",objectdata);
        objectWarn.put("type",2);
        objectWarn.put("map",warnRecord.getMap());
        kafkaTemplate.send(KafukaTopics.WARN_MESSAGE, objectWarn.toString());
    }

    /*
     * 传输报警结束信息给前端 id-->报警信息id
     * */
    public void sendEndWarn(Integer id, WarnRecord warnRecord){
        WarnRecord warnRecord1=warnRecordService.findByRecordId(id);
        JSONObject objectdata = new JSONObject();
        objectdata.put("id",id);//报警信息编号(唯一标识）
        objectdata.put("warnstate",1);//报警状态 0报警 1结束
        objectdata.put("warntype",warnRecord1.getType());
        objectdata.put("tagid",warnRecord1.getTagid());//标签id
        JSONObject objectWarn=new JSONObject();
        objectWarn.put("data",objectdata);
        objectWarn.put("type",2);
        objectWarn.put("map",warnRecord.getMap());
        kafkaTemplate.send(KafukaTopics.WARN_MESSAGE, objectWarn.toString());
    }

    /*
    * 传输区域信息给前端
    * */
    public void sendArea(Integer id){
        AreaVO area=areaService.findByAreaCount(id);
        if (!NullUtils.isEmpty(area)) {
            JSONObject objectdata = new JSONObject();
            objectdata.put("id",area.getId());//区域自增id 唯一标识
            objectdata.put("name",area.getName());//区域名
            objectdata.put("typeName",area.getTypeName());//类型名
            objectdata.put("count",area.getCount());//区域人数
            if (!NullUtils.isEmpty(area.getMaxnum())){
                objectdata.put("all",area.getMaxnum());//人数上限
            }else {
                objectdata.put("all",0);//人数上限
            }
            JSONObject objectArea = new JSONObject();
            objectArea.put("data", objectdata);
            objectArea.put("type", 4);
            objectArea.put("map",area.getMap());
            kafkaTemplate.send(KafukaTopics.WARN_MESSAGE, objectArea.toString());
        }
    }
    /*
    * 传输统计人数信息给前端 type-->类型 1井下人数  2离线人数 3超时人数 4地图分站数 map-->地图id
    * */

    public void sendCount(Integer type,Integer count,Integer map){
        JSONObject objectdata = new JSONObject();
        objectdata.put("counttype",type);
        objectdata.put("count",count);
        if (type==1){
            //地图超员人数上限
            WarnRule warnRule=warnRecordService.findByType(1,map,1);
            objectdata.put("all",warnRule.getRule());
        }
        JSONObject objectCount = new JSONObject();
        objectCount.put("data", objectdata);
        objectCount.put("type", 3);
        objectCount.put("map",map);
        kafkaTemplate.send(KafukaTopics.WARN_MESSAGE, objectCount.toString());
    }




}
