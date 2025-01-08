package com.tgy.rtls.check.Utils;

import com.tgy.rtls.check.controller.TagCheck;
import com.tgy.rtls.data.entity.check.TagcheckEntity;
import com.tgy.rtls.data.entity.check.TagcheckbsidEntity;
import com.tgy.rtls.data.entity.check.TagchecklocationEntity;
import com.tgy.rtls.data.kafukaentity.TagLocation;
import com.tgy.rtls.data.mapper.check.TagcheckDao;
import com.tgy.rtls.data.mapper.check.TagcheckbsidDao;
import com.tgy.rtls.data.mapper.check.TagchecklocationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;

@Component
public class CheckDataProcess {
   
    @Autowired(required = false)
    TagchecklocationDao tagchecklocationDao;
    @Autowired(required = false)
    TagcheckbsidDao tagcheckbsidDao;
    @Autowired(required = false)
    TagcheckDao tagcheckDao;
    Logger logger = LoggerFactory.getLogger(this.getClass());
   public void processTagLocation(TagLocation tagLocation){
     //  synchronized (tagcheckDao)
       {
           Long bsid = tagLocation.getBsid();
           String tagid = tagLocation.getTagid();
           TagchecklocationEntity tagchecklocationEntity = new TagchecklocationEntity();
           tagchecklocationEntity.setBsid((bsid.intValue()));
           tagchecklocationEntity.setTagid(Integer.valueOf(tagid));
           tagchecklocationEntity.setX(tagLocation.getX());
           tagchecklocationEntity.setY(tagLocation.getY());
           tagchecklocationEntity.setZ(tagLocation.getZ());
           Date date = new Date();
           List<TagcheckbsidEntity> res = tagcheckbsidDao.getByTagAndBsid(Integer.valueOf(tagid), bsid.intValue());
           List<TagcheckbsidEntity> notEndList = tagcheckbsidDao.getNotEnd(Integer.valueOf(tagid), bsid.intValue());
           int m = 0;
           for (TagcheckbsidEntity notEnd : notEndList
           ) {
               tagchecklocationEntity.setCheckbsid(notEnd.getTagcheckid());
               tagchecklocationEntity.setTime(date);
               tagchecklocationDao.insert(tagchecklocationEntity);

             /*  if (m == 0) {
                   if (TagCheck.tagCapacities.containsKey(tagLocation.getTagid() + "")) {
                       HashSet<String> set = new LinkedHashSet<>();
                       TagcheckEntity tagcheck = tagcheckDao.selectById(notEnd.getTagcheckid().intValue());
                       String[] totalTaglist = tagcheck.getTaglist().split(",");
                       Iterator<Map.Entry<String, Boolean>> iter = TagCheck.tagCapacities.entrySet().iterator();
                       while (iter.hasNext()) {
                           Map.Entry entry = (Map.Entry) iter.next();
                           String tagidd = (String) entry.getKey();
                           set.add(tagidd);
                       }

                       ArrayList<String> arrayList = new ArrayList<String>(totalTaglist.length - 1);
                       Collections.addAll(arrayList, Arrays.copyOfRange(totalTaglist, 1, totalTaglist.length));
                       HashSet<String> allset = new LinkedHashSet<>(arrayList);
                       allset.removeAll(set);

                       String lack = ",";
                       for (String ee : allset
                       ) {
                           lack = lack + ee + ",";
                       }
                       System.out.println(notEnd.getTagcheckid().intValue() + "::::::缺的标签号：" + lack);
                       TagCheck.tagCapacities.clear();
                   } else {
                       TagCheck.tagCapacities.put(tagLocation.getTagid() + "", true);
                   }
               }
               m++;*/
           }
           for (TagcheckbsidEntity tag : res
           ) {
               tag.setCurrenttotal(tag.getCurrenttotal() + 1);
               tag.setCurrentdetail(tag.getCurrentdetail() == null ? "," + tagid + "," : tag.getCurrentdetail() + tagid + ",");
               if (tag.getStart() == null) {
                   tag.setStart(date);
                    System.out.println("开始时间："+new Timestamp(date.getTime()));
               } else {
             /*  Date start = tag.getStart();
               if(date.getTime()<start.getTime()){
                   date=new Date();
               }*/

               }
               logger.info("end1212"+tag.getId()+":"+tag.getLackpercent());
               tagcheckbsidDao.updateById(tag);
               boolean end = false;
          /* System.out.println(tag.getTagcheckid()+"tag.getState()"+tag.getState());
           System.out.println(tag.getTagcheckid()+"tag.getFinishtype()"+tag.getFinishtype());
           System.out.println(tag.getTagcheckid()+"tag.getCurrenttotal()"+tag.getCurrenttotal());
           System.out.println(tag.getTagcheckid()+"tag.getTotal()"+tag.getTotal());*/
               if (((tag.getState() == 1 && ((tag.getFinishtype() == 1 && tag.getCurrenttotal().intValue() == tag.getTotal().intValue()))) ||
                       (tag.getChecktime() > 0 && (tag.getStart() != null && ((date.getTime() - tag.getStart().getTime()) / 1000) >= tag.getChecktime().intValue())))) {
                   end = true;
                   TagCheck.addFile = 0;
                   TagCheck.filterRes.clear();
                   System.out.println( "filterRes.clear()");
                   System.out.println("结束时间："+new Timestamp(date.getTime()));

               }
               List<TagcheckbsidEntity> totaltag = tagcheckbsidDao.getByTagcheckid(tag.getTagcheckid(), null);
               HashSet<String> set = new LinkedHashSet<>();
               for (TagcheckbsidEntity eachtag : totaltag
               ) {
                   String ss = eachtag.getCurrentdetail();
                   if (ss != null && ss.length() > 0) {
                       String[] c = ss.split(",");
                       int c_len = c.length;
                       for (int i = 1; i < c_len; i++
                       ) {
                           set.add(c[i]);
                       }

                   }
               }

               int totaldistinct = set.size();
               TagcheckEntity tagcheck = tagcheckDao.selectById(tag.getTagcheckid().intValue());
               String[] totalTaglist = tagcheck.getTaglist().split(",");
               ArrayList<String> arrayList = new ArrayList<String>(totalTaglist.length - 1);
               Collections.addAll(arrayList, Arrays.copyOfRange(totalTaglist, 1, totalTaglist.length));
               HashSet<String> allset = new LinkedHashSet<>(arrayList);
               allset.removeAll(set);
               String lack = ",";
               for (String ee : allset
               ) {
                   lack = lack + ee + ",";
               }
               System.out.println(tag.getTagcheckid() + "缺的标签号：" + lack);
               //Timestamp time=new Timestamp(new Date().getTime());
               int period = 0;
               if ((tag.getStart() != null))
                   period = (int) ((new Date().getTime() - tag.getStart().getTime()));
               for (TagcheckbsidEntity eachtag : totaltag
               ) {
                   eachtag.setTotaldistinct(totaldistinct);
                   logger.info ("ss"+eachtag.getId()+totaldistinct+":"+eachtag.getTotal());
                   eachtag.setLackpercent(1 - totaldistinct / (double) eachtag.getTotal());
                   eachtag.setLackedetail(lack);

                   if (end) {
                       eachtag.setState(0);
                       eachtag.setEnd(date);
                       eachtag.setPeriod(period);
                   }
                   if (eachtag.getState() == null) {
                       eachtag.setStart(date);
                   }
                   logger.info ("ss"+eachtag.getId()+"----"+eachtag.getLackpercent());
                   tagcheckbsidDao.updateById(eachtag);
               }


           }
       }

    }
    public void processTagCache(long bsid,List<TagLocation> listTaglocation,HashSet<String> set){


        Date date = new Date();
        List<TagcheckEntity> res = tagcheckbsidDao.getNotEndTagCheck((int)bsid);
        for (TagcheckEntity tagcheckEntity:res
             ) {
           String[] totalTaglist = tagcheckEntity.getTaglist().split(",");
         /*    ArrayList<String> arrayList = new ArrayList<String>(totalTaglist.length - 1);
            Collections.addAll(arrayList, Arrays.copyOfRange(totalTaglist, 1, totalTaglist.length));
            HashSet<String> allset = new LinkedHashSet<>(arrayList);
            allset.retainAll(set);
         */   for (TagLocation taglocation:listTaglocation
                 ) {
                if(tagcheckEntity.getTaglist().indexOf(","+taglocation.getTagid()+",")!=-1){
                    TagchecklocationEntity tagchecklocationEntity = new TagchecklocationEntity();
                    tagchecklocationEntity.setBsid((int)bsid);
                    tagchecklocationEntity.setTagid(Integer.valueOf(taglocation.getTagid()));
                    tagchecklocationEntity.setX(taglocation.getX());
                    tagchecklocationEntity.setY(taglocation.getY());
                    tagchecklocationEntity.setZ(taglocation.getZ());
                        tagchecklocationEntity.setCheckbsid(tagcheckEntity.getId());
                        tagchecklocationEntity.setTime(date);
                        tagchecklocationDao.insert(tagchecklocationEntity);

                }

            }


            List<TagcheckbsidEntity> tagChecklist = tagcheckbsidDao.getByTagcheckidAll(tagcheckEntity.getId());
            for (TagcheckbsidEntity tagCheckbsidEntity:tagChecklist
                 ) {
                String tagcurrentdetail=tagCheckbsidEntity.currentdetail;
                String tagdistinct=tagCheckbsidEntity.lackedetail;
                HashSet<String> allset_currents=null;
                if(bsid==tagCheckbsidEntity.bsid){
                    HashSet<String> allset_current=new HashSet<>();
                    if(tagcurrentdetail!=null) {
                        String[] current = tagcurrentdetail.split(",");
                        ArrayList<String> current_arrayList = new ArrayList<String>(current.length - 1);
                        Collections.addAll(current_arrayList, Arrays.copyOfRange(current, 1, current.length));
                         allset_current = new LinkedHashSet<>(current_arrayList);
                        allset_current.removeAll(set);
                    }
                    allset_current.addAll(set);
                    String lack = ",";
                    for (String ee : allset_current
                    ) {
                        lack = lack + ee + ",";
                    }
                    tagCheckbsidEntity.setCurrentdetail(lack);
                    tagCheckbsidEntity.setCurrenttotal(allset_current.size());
                    allset_currents=allset_current;

                }

                if (tagCheckbsidEntity.getStart() == null)
                    tagCheckbsidEntity.setStart(date);
               // TagcheckEntity tagCheck = tagcheckbsidDao.getByCheckid(tagCheckbsidEntity.getTagcheckid());
                String[] distinct = tagdistinct.split(",");
                ArrayList<String> distinct_arrayList = new ArrayList<String>(distinct.length - 1);
                Collections.addAll(distinct_arrayList, Arrays.copyOfRange(distinct, 1, distinct.length));
                HashSet<String> allset_distinct = new LinkedHashSet<>(distinct_arrayList);
                allset_distinct.removeAll(set);
                if(allset_distinct.size()==0){
                    tagCheckbsidEntity.setState(0);
                    tagCheckbsidEntity.setEnd(date);
                    System.out.println("ss"+0);
                    tagCheckbsidEntity.setLackpercent(0d);
                    TagCheck.addFile=0;
                    try {
                        tagCheckbsidEntity.setPeriod((int) (date.getTime() - tagCheckbsidEntity.getStart().getTime()));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                String lack = ",";
                for (String ee : allset_distinct
                ) {
                    lack = lack + ee + ",";
                }
                tagCheckbsidEntity.setTotaldistinct(totalTaglist.length-1-allset_distinct.size());
                tagCheckbsidEntity.setLackedetail(lack);
                logger.info("en222d"+tagCheckbsidEntity.getId()+":"+tagCheckbsidEntity.getLackpercent());
                tagcheckbsidDao.updateById(tagCheckbsidEntity);
                TagCheck.filterRes.clear();
             //   System.out.println( "filterRes.clear()");
             //   System.out.println("结束时间："+new Timestamp(date.getTime()));


            }




        }




    }



}
