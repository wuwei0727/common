package com.tgy.rtls.location.kafuka;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.equip.Basestation;
import com.tgy.rtls.data.entity.equip.BsSyn;
import com.tgy.rtls.data.entity.map.BsConfig;
import com.tgy.rtls.data.kafukaentity.*;
import com.tgy.rtls.data.service.equip.GatewayService;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.map.BsConfigService;
import com.tgy.rtls.location.config.deviceconfig.BsParaConfig;
import com.tgy.rtls.location.config.deviceconfig.TagParaConfig;
import com.tgy.rtls.location.mqtt.Client;
import com.tgy.rtls.location.mqtt.Connect;
import com.tgy.rtls.location.netty.DataProcess;
import net.sf.json.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component("dasdsad")
public class KafukaListener{
    Logger logger = LoggerFactory.getLogger(KafukaListener.class);
    @Autowired
    DataProcess dataProcess;
    @Autowired
    BsParaConfig bsParaConfig;
    @Autowired
    TagParaConfig tagParaConfig;
    @Autowired
    SubService subService;
    @Autowired
    BsConfigService bsConfigService;
    @Autowired
    GatewayService gatewayService;
    public  ConcurrentHashMap<Integer,Integer> gatewayList=new ConcurrentHashMap<>();



    @KafkaListener(topics = {KafukaTopics.File_REQ})
    private void listenFile(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());

        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            logger.info("------------------ message =" + message);
            JSONObject json =  JSONObject.fromObject(message);
            FilePara filePara=(FilePara)JSONObject.toBean(json, FilePara.class);
            bsParaConfig.sendBsFile((int)(filePara.getInstanceid()),filePara.getBsid(),(int)filePara.getTarget().longValue(),(byte)filePara.getFileType(),filePara.getMessageid(),filePara.getUrl(),""+(int)(filePara.getTime()/1000));

        }
    }

    @KafkaListener(topics = {KafukaTopics.TEXT_REQ})
    public void listenText(ConsumerRecord<?, ?> record) throws UnsupportedEncodingException {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());

        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            logger.info("----------------- record =" + record);
            JSONObject json =  JSONObject.fromObject(message);
            TextPara textPara=(TextPara)JSONObject.toBean(json, TextPara.class);
           // tagParaConfig.setTagText(textPara.getBsid(),textPara.getTagid(),textPara.getMessageid(),textPara.getText(),textPara.getLevel());
            short  type=textPara.getType();
            short   level=textPara.getLevel();
            switch (type){
                case 1:
                    tagParaConfig.setTagCommonText(textPara.getBsid(),textPara.getTarget(),textPara.getMessageid(),textPara.getText(),textPara.getTime());
                    break;
                case 2:
                    tagParaConfig.setTagPosition(textPara.getBsid(),textPara.getTarget(),textPara.getMessageid(),(byte) level,textPara.getText(),textPara.getTime());
                    break;
                case 3:
                    tagParaConfig.setTagWarningText(textPara.getBsid(),textPara.getTarget(),textPara.getMessageid(),textPara.getText(),level,textPara.getTime());
                    break;
            }
        }

    }






    @KafkaListener(topics = {KafukaTopics.BS_RANGE_REQ})
    public void listenBsStartrange(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());

        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            logger.info("----------------- record =" + record);
            JSONObject json =  JSONObject.fromObject(message);
            BsRange bsRange=(BsRange)JSONObject.toBean(json, BsRange.class);
            Long source= bsRange.getSource();
            Long target=bsRange.getTarget();
            if(source!=null&&target!=null) {
                if(source.longValue()!=-1) {
                    //int countnum=Integer.valueOf(count);
                    int countnum=200;
                    for(int i=0;i<countnum;i++) {
                        try {
                            Thread.sleep(10);
                            if(!source.equals(target))
                                bsParaConfig.startBsRange(source,countnum,bsRange.getType(),target);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }

                }else{
                    int countnum=200;
                    List<BsSyn> list = subService.findByAll(null,null,null, null, null, null, null, null,null,null, bsRange.getInstanceId());
                    for (BsSyn bsSyn:list
                         ) {
                        BsConfig bs = bsConfigService.findByNum(bsSyn.getNum());
                        Integer leftBsid = bs.getLeftid();
                        Integer rightBsid= bs.getRightid();

                        for(int i=0;i<countnum;i++) {
                            try {
                                Thread.sleep(10);
                                if(leftBsid!=null)
                                    bsParaConfig.startBsRange(source,countnum,bsRange.getType(),(long)leftBsid);
                                if(rightBsid!=null)
                                    bsParaConfig.startBsRange(source,countnum,bsRange.getType(),(long)rightBsid);

                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }
                    }
            }
            }



        }

    }
    @KafkaListener(topics = {KafukaTopics.BS_CONTROLREQ})
    public void listenBsControl(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());

        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            logger.info("message:"+message);
            JSONObject json =  JSONObject.fromObject(message);
            BsPara bsPara=(BsPara)JSONObject.toBean(json, BsPara.class);
            dataProcess.processBsparaConfig(bsPara);

        }

    }
    @KafkaListener(topics = {KafukaTopics.TAG_CONTROLREQ})
    public void listenTagControl(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());

        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
           logger.info("message:"+message);
            JSONObject json =  JSONObject.fromObject(message);
            TagPara tagconf=(TagPara)JSONObject.toBean(json, TagPara.class);
            dataProcess.processTagparaConfig(tagconf);

        }

    }

    @KafkaListener(topics = {KafukaTopics.BS_ERRORCODETEST})
    public void listenBsErrorCodeTestControl(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());

        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            JSONObject json =  JSONObject.fromObject(message);
            BsPara bsPara=(BsPara)JSONObject.toBean(json, BsPara.class);
            dataProcess.processBsparaConfig(bsPara);


        }

    }

    @KafkaListener(topics = KafukaTopics.CONNECT_GATEWAY)
    public void connectGateway(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            JSONObject json = JSONObject.fromObject(message);
            int id=json.getInt("id");
            int type=json.getInt("type");
            gatewayList.putIfAbsent(id,type);
            gatewayList.replace(id,type);
            if (type==0){//断开连接
                //查询当前网关是否连接
                Client client= Connect.connect.get(id);
                if (!NullUtils.isEmpty(client)){
                    //断开连接 修改状态  清除缓存
                    client.reconnectFlag=false;
                    client.disconnect();
                   // gatewayService.updateGatewayConnect(id,0);
                   // Connect.connect.remove(id);
                }
            }else {//连接
                //连接 修改状态 添加缓存
                Client client= Connect.connect.get(id);
                if(client==null){
                    client = new Client();
                    Connect.connect.put(id,client);
                    client.reconnectFlag=true;
                    client.start(id);
                }else {
                    client.reconnectFlag=true;
                    client.startReconnect();
                }
                }
            }


    }

    @KafkaListener(topics = KafukaTopics.UPDATE_DW1001BS)
    public void updateBase(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());

        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            JSONObject json =  JSONObject.fromObject(message);
            Basestation bsPara=(Basestation)JSONObject.toBean(json, Basestation.class);
            for (Client clent: Connect.connect.values()
            ) {
                clent.publishBsLocation(bsPara);
            }
        }


    }


}
