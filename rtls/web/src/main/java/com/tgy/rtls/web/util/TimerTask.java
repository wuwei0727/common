package com.tgy.rtls.web.util;

import com.tgy.rtls.data.common.TimeUtil;
import com.tgy.rtls.data.entity.park.Fee;
import com.tgy.rtls.data.entity.park.FeeCalcul;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.mapper.park.ParkingRecordMapper;
import com.tgy.rtls.data.websocket.WebSocketLocation;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Random;

@Component
public class TimerTask {

    @Autowired(required = false)
    ParkMapper parkMapper;
    @Autowired(required = false)
    ParkingRecordMapper parkingRecordMapper;
    @Autowired
    private WebSocketLocation webSocketLocation;
 /*每5分钟启动*/
//  @Scheduled(cron = "*/10 * * * * ?")
  @Scheduled(cron = "0 */10 * * * ?")
  public void timerToNow(){

      try {
          Fee fee = new Fee();
          long time = System.currentTimeMillis();
          fee.setEnterTime(new Timestamp(time - new SecureRandom().nextInt(9)*3000000));
          fee.setExitTime(new Timestamp(time + ( new SecureRandom().nextInt(100000))));
          String[] abc = {"C", "B", "A", "B", "N", "A", "E"};
          String[] addr = {"粤", "湘", "粤", "粤", "粤", "桂", "粤"};
          Random random = new SecureRandom();
          int end = random.nextInt(89999) + 10000;
          String lincense = addr[random.nextInt(7)] + " " + abc[random.nextInt(6)] + end;
          fee.setLicense(lincense);
          fee.setMonthlyRent((short) (random.nextInt(5)%2));
          fee.setMap(75);
          fee.setEnterName(abc[random.nextInt(5)]+"口");
          fee.setExitName(abc[random.nextInt(5)]+"口");
          long second=(fee.getExitTime().getTime()-fee.getEnterTime().getTime())/1000;
          if(second<=900)
          fee.setFee(0f);
          else {
              fee.setFee((float)(Math.ceil(second/3600)*1.5));
          }
          parkMapper.addFeeRecord(fee);
          FeeCalcul ss = parkingRecordMapper.findFeeMap(75, TimeUtil.getDayStartTimeStr(),TimeUtil.getDayEndTimeStr());
          ss.setMonthlyRentCount(ss.getTotalCount()-ss.getNonMonthlyRentCount());

          JSONObject jsonArea = new JSONObject();
          JSONObject json = new JSONObject();

          jsonArea.put("data", ss);
          jsonArea.put("map", 75);
          jsonArea.put("type", 15);
          webSocketLocation.sendAll(jsonArea.toString());
          jsonArea.put("data", fee);
          jsonArea.put("type", 16);
          webSocketLocation.sendAll(jsonArea.toString());




      }catch (Exception e){
          e.printStackTrace();
      }

  }


    @Scheduled(cron = "0 */10 * * * ?")
    public void timerToNow5(){

        try {
            Fee fee = new Fee();
            long time = System.currentTimeMillis();
            fee.setEnterTime(new Timestamp(time - new SecureRandom().nextInt(9)*3000000));
            fee.setExitTime(new Timestamp(time + ( new SecureRandom().nextInt(100000))));
            String[] abc = {"C", "B", "A", "B", "N", "A", "E"};
            String[] addr = {"粤", "湘", "粤", "粤", "粤", "桂", "粤"};
            Random random = new SecureRandom();
            int end = random.nextInt(89999) + 10000;
            String lincense = addr[random.nextInt(7)] + " " + abc[random.nextInt(6)] + end;
            fee.setLicense(lincense);
            fee.setMonthlyRent((short) (random.nextInt(5)%2));
            fee.setMap(221);
            fee.setEnterName(abc[random.nextInt(5)]+"口");
            fee.setExitName(abc[random.nextInt(5)]+"口");
            long second=(fee.getExitTime().getTime()-fee.getEnterTime().getTime())/1000;
            if(second<=900)
                fee.setFee(0f);
            else {
                fee.setFee((float)(Math.ceil(second/3600)*1.5));
            }
            parkMapper.addFeeRecord(fee);
            FeeCalcul ss = parkingRecordMapper.findFeeMap(75, TimeUtil.getDayStartTimeStr(),TimeUtil.getDayEndTimeStr());
            ss.setMonthlyRentCount(ss.getTotalCount()-ss.getNonMonthlyRentCount());

            JSONObject jsonArea = new JSONObject();

            jsonArea.put("data", ss);
            jsonArea.put("map", 221);
            jsonArea.put("type", 15);
            webSocketLocation.sendAll(jsonArea.toString());
            jsonArea.put("data", fee);
            jsonArea.put("type", 16);
            webSocketLocation.sendAll(jsonArea.toString());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

//这是一个main方法，程序的入口
public static void main(String[] args){
    Fee fee = new Fee();
    long time = System.currentTimeMillis();
    fee.setEnterTime(new Timestamp(time - new Random().nextInt(9)*3000000));
    fee.setExitTime(new Timestamp(time + ( new Random().nextInt(100000))));
    System.out.println("time = " + time);
//    System.out.println("suiji = " + new Random().nextInt(9));
    System.out.println("time = " + fee.getEnterTime());
    System.out.println("time = " + fee.getExitTime());
}

}