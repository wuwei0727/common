package com.tgy.rtls.area.warn.impl;

import com.tgy.rtls.area.kafka.KafkaSender;
import com.tgy.rtls.area.warn.GatherWarn;
import com.tgy.rtls.data.entity.common.Point2d;
import com.tgy.rtls.data.entity.message.WarnRecord;
import com.tgy.rtls.data.entity.message.WarnRule;
import com.tgy.rtls.data.gather.Cal;
import com.tgy.rtls.data.gather.Gather;
import com.tgy.rtls.data.mapper.message.WarnRecordMapper;
import com.tgy.rtls.data.mapper.message.WarnRuleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 *   处理聚集报警
 */
@Component
public class GatherWarnImpl implements GatherWarn {
    @Autowired(required = false)
    WarnRecordMapper warnRecordMapper;
    @Autowired(required = false)
    WarnRuleMapper warnRuleMapper;
    @Autowired
    private KafkaSender kafkaSender;
    @Override
    public Gather getGatherInfo(ConcurrentHashMap<String, LinkedBlockingDeque<Point2d>> personLocation) {

        Iterator<Map.Entry<String, LinkedBlockingDeque<Point2d>>> iterator = personLocation.entrySet().iterator();
        ConcurrentHashMap<String, List<Point2d>> floor_tagList=new ConcurrentHashMap<>();

        while (iterator.hasNext()){
            Map.Entry<String,  LinkedBlockingDeque<Point2d>> taginf =iterator.next();
            String tagid= taginf.getKey();
            LinkedBlockingDeque<Point2d> queue = taginf.getValue();
            Object[] array = queue.toArray();
            ConcurrentHashMap<String, Point2d> floor_pos=new ConcurrentHashMap<>();
            for (Object o:array
            ) {
                Point2d point=(Point2d) o;
                String map=point.getMap();
                String floor=point.getFloor();
                Point2d pos = floor_pos.get(map+":"+floor);
                if(pos!=null){
                    pos.x=pos.x+point.x;
                    pos.y=pos.y+point.y;
                    pos.count++;
                }else {
                    pos=new Point2d(point.x,point.y,point.getName(),point.getFloor(),point.getMap());
                    pos.setTagid(tagid);
                    pos.count=1;
                    floor_pos.put(map+":"+floor, pos);
                }
            }
            if(floor_pos.size()==1){
                Object[] values = floor_pos.values().toArray();
                Point2d p=(Point2d) values[0];
                p.x=p.x/p.count;
                p.y=p.y/p.count;
                List<Point2d> floor_list = floor_tagList.get(p.getMap() + ":" + p.getFloor());
                if(floor_list==null){
                    floor_list=new LinkedList<>();
                    floor_list.add(p);
                    floor_tagList.put(p.getMap() + ":" + p.getFloor(),floor_list);
                }else {
                    floor_list.add(p);
                }
            }

        }


        Iterator<Map.Entry<String, List<Point2d>>> floor_tagList_entry = floor_tagList.entrySet().iterator();
        while (floor_tagList_entry.hasNext()){
            Map.Entry<String, List<Point2d>> floor_entry = floor_tagList_entry.next();
            String floor = floor_entry.getKey();
            List persons= floor_entry.getValue();
            String[] map_floor= floor.split(":");
            WarnRule gatherRule = warnRuleMapper.findByType(5, Integer.valueOf(map_floor[0]), 1);
            if(gatherRule!=null) {
                Cal cal = new Cal();
                String[] rule= gatherRule.getRule().split(",");
                Gather res = cal.getMax(Float.valueOf(rule[0]), Integer.valueOf(rule[1]), persons);//r 为聚集设置半径，count为聚集人数，res.warningflag为true则报警，返回报警人员名称数组
                List<WarnRecord> warnList = null;
                String name = null;
                if (res.warningflag) {
                    Collections.sort(res.warningIndex);
                    Collections.sort(res.warningTagid);
                    name = "碰撞人员名单:" + Arrays.toString(res.warningIndex.toArray());
                    System.out.println(floor + ":碰撞人员名单" + Arrays.toString(res.warningIndex.toArray()));
                    warnList = warnRecordMapper.findGatherWarn(11, Integer.valueOf(map_floor[0]), map_floor[1], Arrays.toString(res.warningTagid.toArray()), name);
                } else
                    warnList = warnRecordMapper.findGatherWarn(11, Integer.valueOf(map_floor[0]), map_floor[1], null, null);

                // System.out.println("end:" + new Timestamp(new Date().getTime()) + ":" + res.warningflag + "::" + res.warningIndex.length + "centerpos:" + res.centerPos[0] + ":" + res.centerPos[1]);
                if (res.warningflag) {
                    if ((warnList == null || warnList.size() == 0)) {
                        WarnRecord addWarnRecord = new WarnRecord();
                        addWarnRecord.setStartTime(new Date());
                        addWarnRecord.setWarnstate(0);
                        addWarnRecord.setDescribe(name);
                        addWarnRecord.setMap(Integer.valueOf(map_floor[0]));
                        addWarnRecord.setFloor(map_floor[1]);
                        addWarnRecord.setType(11);
                        addWarnRecord.setPersonid(Arrays.toString(res.warningTagid.toArray()));
                       int id= warnRecordMapper.addWarnRecord(addWarnRecord);
                        //传输报警信息
                        kafkaSender.sendWarn(addWarnRecord.getId());
                    }
                } else {
                    if (warnList != null && warnList.size() > 0) {
                        for (WarnRecord w : warnList
                        ) {
                            warnRecordMapper.updateWarnRecord(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), w.getId());
                            kafkaSender.sendEndWarn( w.getId(),w);
                        }
                    }
                }

            }else {//地图聚集报警关闭，则结束当前地图的所有聚集报警
                List<WarnRecord> warnList = warnRecordMapper.findByWarnType(11, Integer.valueOf(map_floor[0]));
                if (warnList != null && warnList.size() > 0) {
                    for (WarnRecord w : warnList
                    ) {
                        warnRecordMapper.updateWarnRecord(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), w.getId());
                        kafkaSender.sendEndWarn( w.getId(),w);
                    }
                }
            }




        }


      /*  List<Point2d> allperson_everyfloor=new ArrayList<>();//每层人员位置列表，包含x，y,人员名称
        Random rand=new Random();

        for(int i=0;i<20;i++){ //20人数，
            float f1 = rand.nextFloat();
            float f2 = rand.nextFloat();
            Point2d p0=new   Point2d(100+f1,100+f2);
            p0.setName(i+"许");
            //  System.out.println("::"+i*i+":"+Math.sqrt((double)i));
            allperson_everyfloor.add(p0);

        }
*/
        System.out.println("start:"+new Timestamp(new Date().getTime()));

        return null;
    }

    public static Gather getGatherInfo1(ConcurrentHashMap<String, LinkedBlockingDeque<Point2d>> personLocation) {



        Iterator<Map.Entry<String, LinkedBlockingDeque<Point2d>>> iterator = personLocation.entrySet().iterator();
        ConcurrentHashMap<String, List<Point2d>> floor_tagList=new ConcurrentHashMap<>();

        while (iterator.hasNext()){
            Map.Entry<String,  LinkedBlockingDeque<Point2d>> taginf =iterator.next();
            String tagid= taginf.getKey();
            LinkedBlockingDeque<Point2d> queue = taginf.getValue();
            Object[] array = queue.toArray();
            ConcurrentHashMap<String, Point2d> floor_pos=new ConcurrentHashMap<>();
            for (Object o:array
            ) {
                Point2d point=(Point2d) o;
                String map=point.getMap();
                String floor=point.getFloor();
                Point2d pos = floor_pos.get(map+":"+floor);
                if(pos!=null){
                    pos.x=pos.x+point.x;
                    pos.y=pos.y+point.y;
                    pos.count++;
                }else {
                    pos=new Point2d(point.x,point.y,point.getName(),point.getFloor(),point.getMap());
                    pos.setTagid(tagid);
                    pos.count=1;
                    floor_pos.put(map+":"+floor, pos);
                }
            }
            if(floor_pos.size()==1){
                Object[] values = floor_pos.values().toArray();
                Point2d p=(Point2d) values[0];
                p.x=p.x/p.count;
                p.y=p.y/p.count;
                List<Point2d> floor_list = floor_tagList.get(p.getMap() + ":" + p.getFloor());
                if(floor_list==null){
                    floor_list=new LinkedList<>();
                    floor_list.add(p);
                    floor_tagList.put(p.getMap() + ":" + p.getFloor(),floor_list);
                }else {
                    floor_list.add(p);
                }
            }

        }


        Iterator<Map.Entry<String, List<Point2d>>> floor_tagList_entry = floor_tagList.entrySet().iterator();
        while (floor_tagList_entry.hasNext()){
            Map.Entry<String, List<Point2d>> floor_entry = floor_tagList_entry.next();
            String floor = floor_entry.getKey();
            List persons= floor_entry.getValue();
            Cal cal= new Cal();
            Gather res= cal.getMax(5,2,persons);//r 为聚集设置半径，count为聚集人数，res.warningflag为true则报警，返回报警人员名称数组
            if(res.warningflag) {
               // System.out.println("end:" + new Timestamp(new Date().getTime()) + ":" + res.warningflag + "::" + res.warningIndex.length + "centerpos:" + res.centerPos[0] + ":" + res.centerPos[1]);
                System.out.println(floor+":聚集人员名单" + Arrays.toString(res.warningIndex.toArray()));
            }
        }


      /*  List<Point2d> allperson_everyfloor=new ArrayList<>();//每层人员位置列表，包含x，y,人员名称
        Random rand=new Random();

        for(int i=0;i<20;i++){ //20人数，
            float f1 = rand.nextFloat();
            float f2 = rand.nextFloat();
            Point2d p0=new   Point2d(100+f1,100+f2);
            p0.setName(i+"许");
            //  System.out.println("::"+i*i+":"+Math.sqrt((double)i));
            allperson_everyfloor.add(p0);

        }
*/


        return null;
    }

        public static void main(String[] args) {
            ConcurrentHashMap<String, LinkedBlockingDeque<Point2d>> personLocation =new ConcurrentHashMap<>();
            Point2d zhang_1=new Point2d(0,4,"张三",null,"1");
            Point2d zhang_2=new Point2d(0.1,4,"张三",null,"1");
            Point2d zhang_3=new Point2d(0,4.1,"张三",null,"1");

            Point2d li_1=new Point2d(4,0.1,"李四","2","1");
            Point2d li_2=new Point2d(4.1,0.1,"李四","2","1");
            Point2d li_3=new Point2d(4,-0.1,"李四","2","1");

            Point2d wang_1=new Point2d(0,4,"王五","2","1");
            Point2d wang_2=new Point2d(0.1,4,"王五","2","1");
            Point2d wang_3=new Point2d(0,4.1,"王五","2","1");


            Point2d ma_1=new Point2d(2,2,"马六",null,"1");
            Point2d ma_2=new Point2d(2.1,2,"马六",null,"1");
            Point2d ma_3=new Point2d(2,2.1,"马六",null,"1");

            LinkedBlockingDeque<Point2d> zhang_queue = new LinkedBlockingDeque<>();
            zhang_queue.push(zhang_1);
            zhang_queue.push(zhang_2);
            zhang_queue.push(zhang_3);

            LinkedBlockingDeque<Point2d> li_queue = new LinkedBlockingDeque<>();
            li_queue.push(li_1);
            li_queue.push(li_2);
            li_queue.push(li_3);

            LinkedBlockingDeque<Point2d> wang_queue = new LinkedBlockingDeque<>();
            wang_queue.push(wang_1);
            wang_queue.push(wang_2);
            wang_queue.push(wang_3);

            LinkedBlockingDeque<Point2d> ma_queue = new LinkedBlockingDeque<>();
            ma_queue.push(ma_1);
            ma_queue.push(ma_2);
            ma_queue.push(ma_3);
            personLocation.put("1",zhang_queue);
            personLocation.put("2",li_queue);
            personLocation.put("3",wang_queue);
            personLocation.put("4",ma_queue);
            System.out.println("start:"+new Timestamp(new Date().getTime()));
            GatherWarnImpl.getGatherInfo1(personLocation);
            System.out.println("end:"+new Timestamp(new Date().getTime()));





        }
}
