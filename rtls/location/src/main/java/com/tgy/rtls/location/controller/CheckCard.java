package com.tgy.rtls.location.controller;

import com.tgy.rtls.location.check.Task;
import com.tgy.rtls.location.config.deviceconfig.BsParaConfig;
import com.tgy.rtls.location.model.BsCheck;
import com.tgy.rtls.location.model.TagInf;
import com.tgy.rtls.location.netty.DataProcess;
import com.tgy.rtls.location.netty.MapContainer;
import io.netty.channel.Channel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@CrossOrigin
/*@RequestMapping("/com/tgy/rtls/location/check")*/
public class CheckCard {

 /*   @Autowired
    private AutoKey autoKey;*/
    @Autowired
    DataProcess dataProcess;
    @Autowired
    BsParaConfig bsParaConfig;
    @Autowired
    MapContainer mapContainer;

    @ResponseBody
    @RequestMapping(value ="/start", method= RequestMethod.GET)
    public boolean test1(){
        mapContainer.tagInf.clear();
        mapContainer.flag=true;
        return true;

    }


    @ResponseBody
    @RequestMapping(value ="/stop", method= RequestMethod.GET)
    public boolean test2(){
        mapContainer.flag=false;
        return true;

    }

    @ResponseBody
    @RequestMapping(value ="/getCount", method= RequestMethod.GET)
    public Integer range(){
       // bsParaConfig.sendBsFile(filePara.getBsid(),(int)filePara.getTarget().longValue(),(byte)filePara.getFileType(),filePara.getMessageid(),filePara.getUrl(),filePara.getFileName());
       // System.out.println("");
        return mapContainer.tagInf.size();
  //  return Long.valueOf(autoKey.getAutoId("random")+"");
    }

    @ResponseBody
    @RequestMapping(value ="/getDetail", method= RequestMethod.GET)
    public List getDetail( ){
      //  System.out.println(mapContainer.tagInf.size());
        Collection<TagInf> ss = mapContainer.tagInf.values();
        List<TagInf> ll=new ArrayList();
        for (TagInf tag:ss
             ) {
            ll.add(tag);
        }
        Collections.sort(ll);
        List list=new ArrayList();
        for (TagInf tagInf:ll
             ) {
            JSONObject json=new JSONObject();
            json.put("tagid",tagInf.tagId);
            json.put("time",tagInf.locationTime.toString());
            json.put("bsid",tagInf.bsid);
            json.put("dir",tagInf.getDir());
            json.put("dis",String.format("%.2f",tagInf.getDis()));
            list.add(json);
        }
        Collections.sort(list);
        return list;
    }

    @ResponseBody
    @RequestMapping(value ="/getRandomPara", method= RequestMethod.GET)
    public String sendRandomTobsid(Long bsid,Long interval,Long count,Integer cmd ){
        //  System.out.println(mapContainer.tagInf.size());
        Channel channel = mapContainer.all_channel.get(bsid);
                 if(channel==null||!channel.isActive()){
                     return "设备未连接";
                 }

                     BsCheck bsif = mapContainer.bsCheck.get(bsid);
                     if(bsif==null){
                         bsif=new BsCheck();
                         mapContainer.bsCheck.put(bsid,bsif);
                     }
                     if(cmd==0){
                         bsif.flag=false;
                     }
                     else {
                         bsif.flag = true;
                         new Task(bsid,interval,count).start();
                     }




        return "操作成功";
    }

    @ResponseBody
    @RequestMapping(value ="/getRandom", method= RequestMethod.GET)
    public JSONArray getRandomTobsid(Long bsid){

        BsCheck bscheck = mapContainer.bsCheck.get(bsid);
        ConcurrentHashMap<Long, Boolean> list = bscheck.random;
        Iterator<Map.Entry<Long, Boolean>> iter = list.entrySet().iterator();
        JSONArray array=new JSONArray();
        while (iter.hasNext()){
            Map.Entry<Long, Boolean> entry = iter.next();
            JSONObject obj=new JSONObject();
            obj.put("random",entry.getKey());
            obj.put("state",entry.getValue());
            array.add(obj);
        }
        return array;

    }


}