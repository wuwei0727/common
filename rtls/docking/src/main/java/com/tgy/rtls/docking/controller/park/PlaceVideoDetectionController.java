package com.tgy.rtls.docking.controller.park;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tgy.rtls.docking.dao.ParkingPlace;
import com.tgy.rtls.docking.dao.PlaceVideoDetection;
import com.tgy.rtls.docking.dao.PlaceVo;
import com.tgy.rtls.docking.service.park.DockingService;
import com.tgy.rtls.docking.service.park.PlaceVideoDetectionService;
import com.tgy.rtls.docking.utils.NullUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: wuwei
 * @CreateTime: 2023/5/31 16:45
 * 车位视频检测
 */
@Slf4j
@RestController
@Configuration
@EnableAsync
@RequestMapping("/placeVideoDetection")
public class PlaceVideoDetectionController {
    @Resource
    private PlaceVideoDetectionService placeVideoDetectionService;
    @Autowired
    private DockingService dockingService;


//    @GetMapping("/test")
//    public String test() {
//        return "{\n" +
//                "\"msg\": \"01\",\n" +
//                "\"carInfoList\": [],\n" +
//                "\"pageCount\": 0,\n" +
//                "\"count\": 0\n" +
//                "}";
//    }

//    @GetMapping("/test")
//    @ResponseBody
//    public String test(){
//        return "{\n" +
//                "\"msg\": \"01\",\n" +
//                "\"carInfoList\": [\n" +
//                "{\n" +
//                "\"carPicUrl\": \"http://192.168.32.2:12799/static/upload/temp/192.168.33.45-01.jpg\",\n" +
//                "\"carportCode\": \"11071\",\n" +
//                "\"carNum\": \"\",\n" +
//                "\"parkIntime\": \"2023-06-13 03:24:16\",\n" +
//                "\"parkNameCarport\": \"056\"\n" +
//                "}\n" +
//                "],\n" +
//                "\"pageCount\": 1,\n" +
//                "\"count\": 1\n" +
//                "}";
//    }

    @GetMapping("/test")
    @ResponseBody
    public String test(){
        return "{\n" +
                "\"msg\": \"01\",\n" +
                "\"carInfoList\": [\n" +
                "{\n" +
                "\"carPicUrl\": \"http://192.168.32.2:12799/static/upload/temp/192.168.33.45-01.jpg\",\n" +
                "\"carportCode\": \"11071\",\n" +
                "\"carNum\": \"001\",\n" +
                "\"parkIntime\": \"2023-06-13 03:24:16\",\n" +
                "\"parkNameCarport\": \"001\"\n" +
                "}\n" +
                "],\n" +
                "\"pageCount\": 1,\n" +
                "\"count\": 1\n" +
                "}";
    }

    @GetMapping("/getPlaceVideoDetectionData")
//    @Scheduled(cron = "12 * * * * ? ")
    @Async(value = "VipCarBitTimeoutOrOccupyTaskExecutor")
    public void getPlaceVideoDetectionData() {
        List<PlaceVideoDetection> placeVideoDetections = placeVideoDetectionService.getAllGuideScreenDeviceOrConditionQuery(null);
        Integer mapId =null;
        if(!(placeVideoDetections.size() >1)) {
            mapId = Math.toIntExact(placeVideoDetections.get(0).getMap());
            log.error("mapId:"+mapId);
        }
        if(!NullUtils.isEmpty(mapId)){
            List<ParkingPlace> parkingPlaces = placeVideoDetectionService.getAllPlaceByMap(null,mapId);
            for (ParkingPlace place : parkingPlaces) {
                log.error("placeId:"+place.getId());
                String builder = placeVideoDetections.get(0).getPlaceInquireAddress() +
                        "?showCode=" + place.getName() +
                        "&pageIndex=" + "1" +
                        "&pageSize=" + "1";

                String url = HttpClientUtil.doGet(builder, null);
                if(!NullUtils.isEmpty(url)){
                    JSONObject jso= JSON.parseObject(url);
                    PlaceVo placeVo = new PlaceVo();
                    placeVo.setId(place.getId());
                    placeVo.setName(place.getName());
                    placeVo.setMap(place.getMap());
                    log.error("MapID:"+placeVo.getMap());
                    String license = null;
                    String carBitNum;
                    if(!NullUtils.isEmpty(jso.getJSONArray("carInfoList"))){
                        carBitNum = jso.getJSONArray("carInfoList").getJSONObject(0).get("parkNameCarport").toString();
                        license = jso.getJSONArray("carInfoList").getJSONObject(0).get("carNum").toString();
                        String parkInTime = jso.getJSONArray("carInfoList").getJSONObject(0).get("parkIntime").toString();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        LocalDateTime entryTime = LocalDateTime.parse(parkInTime, formatter);
                        log.error("车牌license:"+license);
                        log.error("carBitNum:"+carBitNum);
                        placeVo.setLicense(license);
                        placeVo.setEntryTime(entryTime);
                    }
                    if("01".equals(jso.get("msg"))&&!NullUtils.isEmpty(jso.getJSONArray("carInfoList"))&&!NullUtils.isEmpty(license)){
                        if (dockingService.updatePlaceById(place.getId(), 1, license) > 0) {
                            placeVo.setState(1);

                            Object json = JSONObject.toJSON(placeVo);
                            Map<String,Object> map = new HashMap<>();
                            map.put("params", json);
                            String jsonString = JSONObject.toJSONString(map);
                            HttpMethod post = HttpMethod.POST;
                            // String onLineUrl = "http://www.3-si.cn:10087/UWB/park/updatePlaceDataByPlaceId";
//                            String onLineUrl = "http://192.168.1.124:8081/park/updatePlaceDataByPlaceId";
//                             String webUrl = HttpClientUtil.httpPostClient(onLineUrl,post,jsonString);
//                             log.error("webUrl:"+webUrl);
                        }
                    }
                    if ("01".equals(jso.get("msg"))&&!NullUtils.isEmpty(jso.getJSONArray("carInfoList"))&&NullUtils.isEmpty(license)){
                        if(dockingService.updatePlaceById(place.getId(), 0,"null")>0){
                            placeVo.setState(0);

                            Object json = JSONObject.toJSON(placeVo);
                            Map<String,Object> map = new HashMap<>();
                            map.put("params", json);
                            String jsonString = JSONObject.toJSONString(map);
                            HttpMethod post = HttpMethod.POST;
//                             String onLineUrl = "http://www.3-si.cn:10087/UWB/park/updatePlaceDataByPlaceId";
// //                            String onLineUrl = "http://192.168.1.124:8081/park/updatePlaceDataByPlaceId";
//                             String webUrl = HttpClientUtil.httpPostClient(onLineUrl,post,jsonString);
//                             log.error("webUrl:"+webUrl);
                        }
                    }

                }
            }
        }
    }













    //这是一个main方法，程序的入口
    public static void main(String[] args){
        PlaceVo placeVo = new PlaceVo();
        placeVo.setId(1);
        placeVo.setState(1);
        placeVo.setLicense("hhhh");

        PlaceVo placeVo1 = new PlaceVo();
        placeVo1.setId(2);
        placeVo1.setState(1);
        placeVo1.setLicense("hhhh");

        List<Object> list = new ArrayList<>();
        list.add(placeVo);
        list.add(placeVo1);
        Map<Integer,Object> map = new HashMap<>();
        map.put(placeVo.getId(), placeVo);
        map.put(placeVo1.getId(), placeVo1);
        System.out.println("map = " + map);
    }
}

