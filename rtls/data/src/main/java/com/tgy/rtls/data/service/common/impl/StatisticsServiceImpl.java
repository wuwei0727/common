package com.tgy.rtls.data.service.common.impl;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.WarnFlow;
import com.tgy.rtls.data.mapper.common.StatisticsMapper;
import com.tgy.rtls.data.service.common.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.common.impl
 * @date 2021/1/15
 */
@Service
public class StatisticsServiceImpl implements StatisticsService{
    @Autowired(required = false)
    private StatisticsMapper statisticsMapper;
    @Autowired(required = false)
    private LocalUtil localUtil;
    @Value("${web.lang}")
    private String lang;

    @Override
    public boolean addManFlow(Integer map, int count) {
        return statisticsMapper.addManFlow(map,count)>0;
    }

    @Override
    public boolean updateManFlow(Integer map, String endTime) {
        return statisticsMapper.updateManFlow(map,endTime)>0;
    }

    @Override
    public List<Object> getManFlowSel(Integer map, int day) {
        List<Object> list=new ArrayList<>();
        try {
             //1 根据查询不同的天数 划分时间段 1->24小时 7->7天 30->30tian
            //1.1 拿当前时间往前推day天
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",localUtil.getCurrentLocale());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, -day);
            String startTime=dateFormat.format(calendar.getTime());//开始时间
            String endTime=null;//结束时间
            //2 根据不同的天数 走不同分支
            if (day==1){
                //2.1 1天划分24小时查询每个小时内井下最大人数
                for (int i=1;i<=24;i++){
                    Map<String,Object> manFlowMap=new HashMap<>();
                    calendar.add(Calendar.HOUR_OF_DAY,1);
                    endTime=dateFormat.format(calendar.getTime());
                    //查询startTime-->endTime时间段内的井下最大人数
                    Integer count=statisticsMapper.selectManFlow(map,startTime,endTime);
                    //如果没有查到 就拿startTime前一条数据
                    Integer count1=statisticsMapper.selectManFlowLately(map,startTime);
                    if (NullUtils.isEmpty(count)){
                        count=count1;
                    }else {
                        if (!NullUtils.isEmpty(count1)&&count<count1){
                            count=count1;
                        }
                    }
                    if (NullUtils.isEmpty(count)){
                        count=0;
                    }
                    manFlowMap.put("txt",dateFormat.parse(startTime).getHours()+"h");
                    manFlowMap.put("val",count);
                    list.add(manFlowMap);
                    startTime=endTime;
                }
            }else{
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.DAY_OF_MONTH,1);
                 startTime=dateFormat.format(calendar.getTime());//重新设置开始时间

                day=day+1;
                for (int i=2;i<=day;i++){
                    Map<String,Object> manFlowMap=new HashMap<>();
                    calendar.add(Calendar.DAY_OF_MONTH,1);

                    endTime=dateFormat.format(calendar.getTime());

                    //查询startTime-->endTime时间段内的井下最大人数
                    Integer count=statisticsMapper.selectManFlow(map,startTime,endTime);
                    //如果没有查到 就拿startTime前一条数据
                    Integer count1=statisticsMapper.selectManFlowLately(map,startTime);
                    if (NullUtils.isEmpty(count)){
                        count=count1;
                    }else {
                        if (!NullUtils.isEmpty(count1)&&count<count1){
                            count=count1;
                        }
                    }
                    if (NullUtils.isEmpty(count)){
                        count=0;
                    }
                    if (day==8){//周
                     // int num=dateFormat.parse(startTime).getDay();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dateFormat.parse(startTime));
                        int num = cal.get(Calendar.DAY_OF_WEEK) - 1;
                        if (num < 0)
                            num = 0;
                        String week=null;
                        switch (num){
                            case 0:
                                week=LocalUtil.get(KafukaTopics.SUN);
                                break;
                            case 1:
                                week=LocalUtil.get(KafukaTopics.MON);
                                break;
                            case 2:
                                week=LocalUtil.get(KafukaTopics.TUS);;
                                break;
                            case 3:
                                week=LocalUtil.get(KafukaTopics.WED);;
                                break;
                            case 4:
                                week=LocalUtil.get(KafukaTopics.THU);;
                                break;
                            case 5:
                                week=LocalUtil.get(KafukaTopics.FRI);;
                                break;
                            case 6:
                                week=LocalUtil.get(KafukaTopics.SAT);
                                break;
                        }
                        manFlowMap.put("txt",week);
                    }else {//日
                     String date="";
                            switch (lang) {
                                case "ko_KR":
                                 //   date = "name_ko";
                                    break;
                                case "zh_CN":
                                    date = (dateFormat.parse(startTime).getMonth() + 1) + LocalUtil.get(KafukaTopics.MONTH) + dateFormat.parse(startTime).getDate() + LocalUtil.get(KafukaTopics.DAY);
                                    break;
                                case "en_US":
                                  //  String sdasd=dateFormat.parse(endTime).toString();
                                    date=dateFormat.parse(startTime).toString().substring(4,7)+" "+(dateFormat.parse(startTime).getDate() )+"th";
                                    break;
                            }

                        manFlowMap.put("txt",date);
                    }
                    manFlowMap.put("val",count);
                    list.add(manFlowMap);
                    startTime=endTime;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 根据入井时间判断各个时段的人流量
     * @param map
     * @param day
     * @return
     */
    @Override
    public List<Object> getManFlowSelFromIncoalRecord(Integer map, int day) {
        List<Object> list=new ArrayList<>();
        try {
            //1 根据查询不同的天数 划分时间段 1->24小时 7->7天 30->30tian
            //1.1 拿当前时间往前推day天
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",localUtil.getCurrentLocale());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.add(Calendar.DAY_OF_MONTH, -day);
            String startTime=dateFormat.format(calendar.getTime());//开始时间
            String endTime=null;//结束时间
            //2 根据不同的天数 走不同分支
            if (day==1){
                //2.1 1天划分24小时查询每个小时内井下最大人数
                for (int i=1;i<=24;i++){
                    Map<String,Object> manFlowMap=new HashMap<>();
                    calendar.add(Calendar.HOUR_OF_DAY,1);
                    endTime=dateFormat.format(calendar.getTime());
                    //查询startTime-->endTime时间段内的井下最大人数
                    Integer count=0;
                     count=statisticsMapper.selectManFlowFromIncoalRecord(map,startTime,endTime);
                    //如果没有查到 就拿startTime前一条数据
                    // Integer count1=statisticsMapper.selectManFlowLately(map,startTime);
              /*      if (NullUtils.isEmpty(count)){
                        count=count1;
                    }else {
                        if (!NullUtils.isEmpty(count1)&&count<count1){
                            count=count1;
                        }
                    }
                    if (NullUtils.isEmpty(count)){
                        count=0;
                    }*/
                    manFlowMap.put("txt",dateFormat.parse(startTime).getHours()+"h");
                    manFlowMap.put("val",count);
                    list.add(manFlowMap);
                    startTime=endTime;
                }
            }else{
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.DAY_OF_MONTH,1);
                startTime=dateFormat.format(calendar.getTime());//重新设置开始时间

                day=day+1;
                for (int i=2;i<=day;i++){
                    Map<String,Object> manFlowMap=new HashMap<>();
                    calendar.add(Calendar.DAY_OF_MONTH,1);

                    endTime=dateFormat.format(calendar.getTime());
                    Integer count=0;
                    //查询startTime-->endTime时间段内的井下最大人数
                     count=statisticsMapper.selectManFlowFromIncoalRecord(map,startTime,endTime);
                    //如果没有查到 就拿startTime前一条数据
                   /* Integer count1=statisticsMapper.selectManFlowLately(map,startTime);
                    if (NullUtils.isEmpty(count)){
                        count=count1;
                    }else {
                        if (!NullUtils.isEmpty(count1)&&count<count1){
                            count=count1;
                        }
                    }
                    if (NullUtils.isEmpty(count)){
                        count=0;
                    }*/
                    if (day==8){//周
                        // int num=dateFormat.parse(startTime).getDay();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dateFormat.parse(startTime));
                        int num = cal.get(Calendar.DAY_OF_WEEK) - 1;
                        if (num < 0)
                            num = 0;
                        String week=null;
                        switch (num){
                            case 0:
                                week=LocalUtil.get(KafukaTopics.SUN);
                                break;
                            case 1:
                                week=LocalUtil.get(KafukaTopics.MON);
                                break;
                            case 2:
                                week=LocalUtil.get(KafukaTopics.TUS);;
                                break;
                            case 3:
                                week=LocalUtil.get(KafukaTopics.WED);;
                                break;
                            case 4:
                                week=LocalUtil.get(KafukaTopics.THU);;
                                break;
                            case 5:
                                week=LocalUtil.get(KafukaTopics.FRI);;
                                break;
                            case 6:
                                week=LocalUtil.get(KafukaTopics.SAT);
                                break;
                        }
                        manFlowMap.put("txt",week);
                    }else {//日
                        String date="";
                        switch (lang) {
                            case "ko_KR":
                                //   date = "name_ko";
                                break;
                            case "zh_CN":
                                date = (dateFormat.parse(startTime).getMonth() + 1) + LocalUtil.get(KafukaTopics.MONTH) + dateFormat.parse(startTime).getDate() + LocalUtil.get(KafukaTopics.DAY);
                                break;
                            case "en_US":
                                //  String sdasd=dateFormat.parse(endTime).toString();
                                date=dateFormat.parse(startTime).toString().substring(4,7)+" "+(dateFormat.parse(startTime).getDate() )+"th";
                                break;
                        }

                        manFlowMap.put("txt",date);
                    }
                    manFlowMap.put("val",count);
                    list.add(manFlowMap);
                    startTime=endTime;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Object> getWarnFlowSel(Integer map, int day, String startTime,int number) {
        List<Object> list=new ArrayList<>();
        try {
            //1.根据查询的时间单位设置条件 day=1按天  day=2按月
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",localUtil.getCurrentLocale());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateFormat.parse(startTime));
            String endTime=null;
            //2.按每天或每月的时间段来计算报警数量
            for (int i=0;i<number;i++){
                Map<String,Object> manFlowMap=new HashMap<>();
                if (day==1){
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }else if(day==2){
                    calendar.add(Calendar.MONTH, 1);
                }
                endTime = dateFormat.format(calendar.getTime());
                List<WarnFlow> warnFlows=statisticsMapper.findByWarnFlow(map,startTime,endTime,localUtil.getLocale());
                if(day==1) {
                    String date="";
                    switch (lang) {
                        case "ko_KR":
                            //   date = "name_ko";
                            break;
                        case "zh_CN":
                            date=(dateFormat.parse(startTime).getMonth()+1)+LocalUtil.get(KafukaTopics.MONTH)+dateFormat.parse(startTime).getDate()+LocalUtil.get(KafukaTopics.DAY);
                            break;
                        case "en_US":
                            date=dateFormat.parse(startTime).toString().substring(0,7);
                            break;
                    }
                    manFlowMap.put("txt", date);//日
                }else {
                    String date="";
                    switch (lang) {
                        case "ko_KR":
                            //   date = "name_ko";
                            break;
                        case "zh_CN":
                            date =(dateFormat.parse(startTime).getMonth()+1)+LocalUtil.get(KafukaTopics.MONTH);
                            break;
                        case "en_US":
                            date=dateFormat.parse(startTime).toString().substring(4,7);
                            break;
                    }
                    manFlowMap.put("txt", date);//月
                }
                manFlowMap.put("val",warnFlows);
                list.add(manFlowMap);
                startTime=endTime;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean delManFlow(String[] maps) {
        return statisticsMapper.delManFlow(maps)>0;
    }
}
