package com.tgy.rtls.location.check;

import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.update.TagfirmwareEntity;
import com.tgy.rtls.data.kafukaentity.TagPara;
import com.tgy.rtls.data.service.update.TagFirmwareService;
import com.tgy.rtls.data.service.update.imp.TagFirmwareServiceImp;
import com.tgy.rtls.location.config.deviceconfig.TagParaConfig;
import com.tgy.rtls.location.config.deviceconfig.TagParaConfigImp;
import com.tgy.rtls.location.netty.MapContainer;
import com.tgy.rtls.location.struct.TagFirmware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TagUpdateCheckRun implements Runnable {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private TagParaConfig tagParaConfig = SpringContextHolder.getBean(TagParaConfigImp.class);;
    private MapContainer mapContainer= SpringContextHolder.getBean("mapContainer");
    private TagFirmwareService tagFirmwareService= SpringContextHolder.getBean(TagFirmwareServiceImp.class);
    public  static volatile boolean run=false;


 public TagUpdateCheckRun(TagPara tagPara){
     long tagid = Long.valueOf(tagPara.getTagid());
     TagFirmware tagFirmware=new TagFirmware();
     try {
         if (mapContainer.currentUpdateTag.size() < 5) {
             if (mapContainer.currentUpdateTag.containsKey(tagid))
                 mapContainer.currentUpdateTag.remove(tagid);
             mapContainer.currentUpdateTag.put(tagid, tagPara);
             tagParaConfig.processTagUpdate(-1l, tagid, tagFirmware, tagPara.getFirmwareUrl(), "", tagPara.getPkglen(), tagPara.getFirmwareVersion());
         } else {
             if (mapContainer.waitUpdateTag.containsKey(tagid))
                 mapContainer.waitUpdateTag.remove(tagid);
             mapContainer.waitUpdateTag.put(tagid, tagPara);
         }
     } catch (IOException e) {
         e.printStackTrace();
     }
    }
    @Override
    public void run() {
            if(run)
                return;
            else
                run=true;

          while (run) {
              System.out.println(LocalTime.now() +"judge  tag update ");
              try {
                  Thread.sleep(30000);
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
              long now = new Date().getTime();
              boolean setUpdate=true;
              List<TagfirmwareEntity> updateTagList = tagFirmwareService.getAll();
              for (TagfirmwareEntity tag : updateTagList
              ) {
                  long diff = Math.abs(tag.getUtc() - now);
                  if (diff > 120000&&tag.getUpdatestate()!=0&&tag.getUpdatestate()!=-1&&tag.getUpdatestate()!=100) {
                     // tag.setUpdatestate(-1);
                      tagFirmwareService.updateById(tag);
                  }
                  if(tag.getUpdatestate()!=0&&tag.getUpdatestate()!=-1&&tag.getUpdatestate()!=100)
                      setUpdate=false;

              }


              if(mapContainer.currentUpdateTag.size()==0&&mapContainer.waitUpdateTag.size()==0&&setUpdate){
                  run=false;
                  return;
              }



             //移除队列中升级失败的标签
              Iterator<Map.Entry<Long, TagPara>> nowIter = mapContainer.currentUpdateTag.entrySet().iterator();
              while (nowIter.hasNext()) {
                  Map.Entry<Long, TagPara> updateEntry = nowIter.next();
                  TagPara updateTag = updateEntry.getValue();
                  TagfirmwareEntity tagfirmwareEntity = tagFirmwareService.findByTagid(Long.valueOf(updateTag.getTagid()));
                  boolean insert = false;
                  if (tagfirmwareEntity == null) {
                      insert = true;
                      tagfirmwareEntity = new TagfirmwareEntity();
                      tagfirmwareEntity.setTagid(Integer.valueOf(updateTag.getTagid()));
                  }
                  long delay = Math.abs(now - updateTag.getTime());
                  if (delay > 50000 || delay > 30000 && tagfirmwareEntity.getUpdatestate() == -1) {
                      logger.error("timerTo    MoveTag:" + updateEntry.getKey());
                      mapContainer.currentUpdateTag.remove(updateEntry.getKey());
                      //tagfirmwareEntity.setUpdatestate(-1);
                      if (insert)
                          tagFirmwareService.insert(tagfirmwareEntity);
                      else
                          tagFirmwareService.updateById(tagfirmwareEntity);
                  }
              }
              //从等待升级的设备中移除，添加到升级中去
              Iterator<Map.Entry<Long, TagPara>> iter = mapContainer.waitUpdateTag.entrySet().iterator();
              while (iter.hasNext()) {
                  Map.Entry<Long, TagPara> waitEntry = iter.next();
                  TagPara addTag = waitEntry.getValue();
                  if (!(mapContainer.currentUpdateTag.size() > 5)) {
                      addTag.setTime(now);
                      logger.error("timerTo    AddTag:" + waitEntry.getKey());
                      try {
                          mapContainer.waitUpdateTag.remove(waitEntry.getKey());
                          mapContainer.currentUpdateTag.put(waitEntry.getKey(), addTag);
                          tagParaConfig.processTagUpdate(-1l, waitEntry.getKey(), null, addTag.getFirmwareUrl(), "", addTag.getPkglen(), addTag.getFirmwareVersion());
                      } catch (IOException e) {
                          e.printStackTrace();
                      }
                  } else {
                      break;
                  }
              }


          }

    }

    public static void main(String[] args) {


    }
}


