package com.tgy.rtls.location.Utils;

import com.tgy.rtls.data.service.common.MailService;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.update.TagFirmwareService;
import com.tgy.rtls.location.config.deviceconfig.BsParaConfig;
import com.tgy.rtls.location.config.deviceconfig.TagParaConfig;
import com.tgy.rtls.location.kafuka.KafukaSender;
import com.tgy.rtls.location.netty.MapContainer;
import com.tgy.rtls.location.netty.SendData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TimerTask {
    @Autowired
    MapContainer mapContainer;
    @Autowired
    BsParaConfig bsParaConfig;
    @Autowired
    SendData sendData;
    @Autowired
    KafukaSender kafukaSender;
    @Autowired
    SubService subService;
    @Autowired
    MailService mailService;
    @Autowired(required = false)
    TagFirmwareService tagFirmwareService;
    @Autowired
    private TagParaConfig tagParaConfig;
    @Value("${spring.mail.username}")
    private String username;
    @Value("${logpath}")
    private String logpath;
    private Logger logger= LoggerFactory.getLogger(TimerTask.class);
/*
//  每分钟启动
  @Scheduled(cron = "0 0/1 * * * ?")
  public void timerToNow(){
      System.out.println("now time:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
  }*/


   /* @Scheduled(fixedRate = 2000)
    public void sendHeart() {

       // logger.error("::heart send time:" );
        // TODO Auto-generated method stub
        //缁熻鏈甯镐笂鎶ョ殑鍩虹珯
        try {
            Iterator bsiter = mapContainer.all_channel_id.entrySet().iterator();
            while (bsiter.hasNext()) {

                Map.Entry entry = (Map.Entry) bsiter.next();
                Channel bsChannel = (Channel) entry.getValue();
                Long bsid = (Long) entry.getKey();


                try {

                    if (bsChannel != null) {
                        if (bsChannel.isActive()) {
                            Date date = new Date();
                            Timestamp timeStamp = new Timestamp(date.getTime());
                            System.out.println();
                            bsParaConfig.sendHeartData(bsid);


                        } else {


                        }
                    }

                } catch (Exception e) {
                    // TODO: handle exception
                }
            }


        } catch (Exception e) {
            // TODO: handle exception
        }
    }*/
   /* @Scheduled(fixedRate = 15000)
    public void timerToTag() {


        // TODO Auto-generated method stub
        //缁熻鏈甯镐笂鎶ョ殑鍩虹珯
        logger.error("timeer task period");
        try {
          //  Iterator bsiter = mapContainer.bsInf.entrySet().iterator();
            List<BsSyn> list = subService.findByAll(null,null,null, null, null, null, null, null, null);
           for(BsSyn bsSyn:list)
           {

                Long bsid = Long.valueOf( bsSyn.getNum());
               BsInf bsInf = mapContainer.bsInf.get(bsid+"");
                Channel bsChannel = mapContainer.all_channel_id.get(bsid+"");
                short netState=1;
                try {

                    if (bsChannel != null) {
                        if (bsChannel.isActive()) {
                            Date date = new Date();
                            Timestamp timeStamp = new Timestamp(date.getTime());
                            logger.error("::heart send time:"+bsid);
                            bsParaConfig.sendHeartData(bsid);
                            netState=0;

                        } else {
                            netState=1;

                        }
                    } else
                        netState=1;
                 if(bsInf==null||bsInf.netState!=netState){
                        BsState bsState=new BsState();
                        bsState.setErrorCode((short)2);
                        bsState.setState(netState);
                        bsState.setBsid(bsid+"");
                        bsState.setTime(new Date().getTime());
                        kafukaSender.send(KafukaTopics.BS_STATE,bsState.toString());
                    }
                 if(bsInf!=null) {
                     bsInf.netState = netState;
                     bsInf.ping_number = 0;
                     bsInf.Crystal_msg_number = 0;
                 }
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }


        } catch (Exception e) {
            // TODO: handle exception
        }
    }*/

    /**
     * 定时判断等待升级过程中标签，将升级中升级时间过长的标签置为失败
     */
/* @Scheduled(fixedRate = 25000)*/




/*  @Scheduled(initialDelay = 50000,fixedRate = 6000)
  public void timerToReport(){
      for (int i = 0; i < 10; i++){
          System.out.println("<================delay :" + i + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "count===============>");
      }
  }*/

   /* *//**
     * 每天定时发送log文件到邮箱10,19,22点发送
     *//*
    @Scheduled(cron = "0 0 10,19,22 * * ?")
    public void releasePackageMirror(){
        logger.info(new Timestamp(System.currentTimeMillis())+"释放到期任务包" );
        try {
            File file = new File(logpath);
            // get the folder list
            File[] array = file.listFiles();
            String[] path = new String[array.length];
            for (int i = 0; i < array.length; i++) {
                path[i] = array[i].getAbsolutePath();

            }
            mailService.sendAttachmentsMail(username, "mayh@tuguiyao-gd.com", "测试log", "测试", path);
        }catch (Exception e){
              e.printStackTrace();
        }
    }*/


}