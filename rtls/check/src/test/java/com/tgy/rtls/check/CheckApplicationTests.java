package com.tgy.rtls.check;


import com.tgy.rtls.check.Utils.CheckDataProcess;
import com.tgy.rtls.check.controller.TagCheck;
import com.tgy.rtls.check.entity.TagCache;
import com.tgy.rtls.data.kafukaentity.TagLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootTest
public  class CheckApplicationTests {
@Autowired
    CheckDataProcess checkDataProcess;


  public   void main(){
    {
        TagLocation tag1 = new TagLocation();
        TagLocation tag2 = new TagLocation();
        TagLocation tag3 = new TagLocation();
        TagLocation tag4 = new TagLocation();
        tag1.setTagid(100+"");
        tag2.setTagid(101+"");
        tag3.setTagid(102+"");
        tag4.setTagid(103+"");
        TagCheck.filterRes.put("8200",tag1);
        TagCheck.filterRes.put("8200",tag2);
        TagCheck.filterRes.put("8000",tag3);
        TagCheck.filterRes.put("8000",tag4);
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
        while (iter1.hasNext()){
            TagCache tagCache = iter1.next().getValue();
            String bsid = iter1.next().getKey();
            checkDataProcess.processTagCache(Long.valueOf(bsid).longValue(),tagCache.listTaglocation,tagCache.set);
        }
    }
}

}
