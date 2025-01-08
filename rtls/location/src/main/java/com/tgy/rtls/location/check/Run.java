package com.tgy.rtls.location.check;

import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.check.BserrorcodetestEntity;
import com.tgy.rtls.data.entity.check.BserrorcodetestrecordEntity;
import com.tgy.rtls.data.mapper.check.BserrorcodetestDao;
import com.tgy.rtls.data.mapper.check.BserrorcodetestrecordDao;
import com.tgy.rtls.location.netty.MapContainer;
import com.tgy.rtls.location.netty.SendData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.List;

import static com.tgy.rtls.location.Utils.Constant.CMD_BS_RANDOMKEY;

public class Run implements Runnable {
  /*  ReentrantLock lock;
    Long bsid;
    int messageid;*/
  private Logger logger = LoggerFactory.getLogger(this.getClass());
  ErrorCodeInf errorCodeInf;
    private SendData sendData = SpringContextHolder.getBean("sendData");;
    private MapContainer mapContainer= SpringContextHolder.getBean("mapContainer");
    private BserrorcodetestDao bserrorcodetestDao= SpringContextHolder.getBean("bserrorcodetestDao");
    private BserrorcodetestrecordDao bserrorcodetestrecordDao= SpringContextHolder.getBean("bserrorcodetestrecordDao");

 public    Run(ErrorCodeInf errorCodeInf){
        this.errorCodeInf=errorCodeInf;
    }
    @Override
    public void run() {
     Long i=0l;
     int interval=errorCodeInf.interval;
     while (!errorCodeInf.stopFlag&&(errorCodeInf.info_state.size()<errorCodeInf.count||errorCodeInf.count==0)) {
         ByteBuffer buffer = ByteBuffer.allocate(128);
         long time = new Date().getTime();
         buffer.order(ByteOrder.LITTLE_ENDIAN);
         buffer.putLong(errorCodeInf.messageid);
         for(int k=0;k<15;k++) {
             buffer.putLong(time);
         }
         logger.error("批次:"+errorCodeInf.messageid+"bsid:"+errorCodeInf.bsid + "误码率测试发送:" + time);
         errorCodeInf.info_state.put(time,false);
         BserrorcodetestEntity entity=new BserrorcodetestEntity();
         entity.setSend(time);
         entity.setTagcheckid((long)errorCodeInf.messageid);
         bserrorcodetestDao.insert(entity);
         sendData.sendDate(errorCodeInf.bsid, CMD_BS_RANDOMKEY, buffer.array());
         try {
           Thread.sleep(interval);
         } catch (InterruptedException e) {
          //   e.printStackTrace();
             logger.error("收到基站反馈，继续执行");
         }

     }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            //   e.printStackTrace();
            logger.error("误码测试结束");
        }

        Date date=new Date();
        BserrorcodetestrecordEntity res = bserrorcodetestDao.getByTagCheckId((long)errorCodeInf.messageid);
        List<BserrorcodetestrecordEntity> listc = bserrorcodetestrecordDao.getByTagCheckid((long) errorCodeInf.messageid);
        if(listc!=null&&listc.size()>0) {
            BserrorcodetestrecordEntity res1 = listc.get(0);
            res1.setEnd(date);
            res1.setState((short) 0);
            res1.setSendnum(res.getSendnum());
            res1.setReceivenum(res.getReceivenum());
            res1.setLost(res.getSendnum() - res.getReceivenum());
            res1.setErrornum(res.getErrornum());
            res1.setLostrate((res.getSendnum() - res.getReceivenum()) / (double) res.getSendnum());
            res1.setErrorrate(res.getErrornum() / (double) res.getSendnum());
            bserrorcodetestrecordDao.updateById(res1);
        }

    }

    public static void main(String[] args) {


    }
}


