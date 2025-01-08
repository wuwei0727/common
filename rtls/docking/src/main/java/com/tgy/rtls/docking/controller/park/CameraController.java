package com.tgy.rtls.docking.controller.park;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tgy.rtls.docking.dao.ParkingPlace;
import com.tgy.rtls.docking.dao.PlaceVideoDetection;
import com.tgy.rtls.docking.dao.PlaceVo;
import com.tgy.rtls.docking.mapper.PlaceMapper;
import com.tgy.rtls.docking.service.park.PlaceVideoDetectionService;
import com.tgy.rtls.docking.utils.NullUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.docking.controller.park
 * @Author: wuwei
 * @CreateTime: 2023-08-21 16:05
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@RestController
@Configuration
@EnableAsync
@RequestMapping("/camera")
public class CameraController {
    @Resource
    private PlaceMapper placeMapper;
    @Resource
    private PlaceVideoDetectionService placeVideoDetectionService;

    //@GetMapping("/test")
    //public String test() {
    //    return "{\n" +
    //            "  \"Code\": 1,\n" +
    //            "  \"Describe\": [{}]\n" +
    //            "}";
    //}


    @GetMapping("/test")
    @ResponseBody
    public String test(){
        return "{\n" +
                "\"Code\": 0,\n" +
                "\"Describe\": [\n" +
                "{\n" +
                "\"LC\": \"a区\",\n" +
                "\"FL\": \"b1\",\n" +
                "\"ParkingNo\": \"001\",\n" +
                "\"CarPlateNo\": \"粤B12345\",\n" +
                "\"PictureByte\": \"base64编码\",\n" +
                "\"DateTime\": \"2018/11/26 11:45:50\"\n" +
                "}\n" +
                "]\n" +
                "}";
    }

    // @GetMapping("/a")
    // public void a() {
    //     String url = "http://61.145.96.90:60091/api/Vehiclefind/FindCarInfo";
    //     String a = "东A-052,东A-058,东A-054,东A-055,东A-056,东A-057,东A-058,东A-059,东A-060";
    //     for (String placeVo : a.split(",")) {
    //         PlaceVo placeVo1 = new PlaceVo();
    //         placeVo1.setPlateNo(placeVo);
    //         String data =HttpClientUtil.httpPostClient(url, HttpMethod.POST, JSONObject.toJSONString(placeVo1), MediaType.APPLICATION_JSON);
    //         log.info("调用getPlaceVideoDetectionData方法---->"+data);
    //     }
    // }


    @GetMapping("/getPlaceVideoDetectionData")
    // @Scheduled(cron = "0 */4 * * * ?")
    @Async (value = "VipCarBitTimeoutOrOccupyTaskExecutor")
    public void getPlaceVideoDetectionData() {
        long start = System.currentTimeMillis();
        List<PlaceVideoDetection> placeVideoDetections = placeVideoDetectionService.getAllGuideScreenDeviceOrConditionQuery(178);
//        List<PlaceVideoDetection> placeVideoDetections = placeVideoDetectionService.getAllGuideScreenDeviceOrConditionQuery(309);
        Integer mapId;
        if (!(placeVideoDetections.size() > 1)) {
            mapId = Math.toIntExact(placeVideoDetections.get(0).getMap());
            log.error("mapId:" + mapId);
        } else {
            mapId = null;
        }
        if (!NullUtils.isEmpty(mapId)) {
            List<ParkingPlace> allPlace = placeMapper.getAllPlace(mapId, "东A-160");
            String url1 = placeVideoDetections.get(0).getPlaceInquireAddress();
            final int[] i = {1};
            List<PlaceVo> placeList = new ArrayList<>();
            allPlace.forEach(place -> {
                log.error ("当前allPlace.size是："+ i[0]++);
                PlaceVo placeVo1 = new PlaceVo();
                placeVo1.setPlateNo (place.getName ());
                String url = HttpClientUtil.httpPostClient(url1, HttpMethod.POST, JSONObject.toJSONString(placeVo1),MediaType.APPLICATION_JSON);
                JSONObject jso = JSON.parseObject(url);
                PlaceVo placeVo = new PlaceVo();
                placeVo.setId(place.getId());
                placeVo.setName(place.getName());
                placeVo.setMap(mapId);
                if (!NullUtils.isEmpty(url) && !NullUtils.isEmpty(jso.getJSONArray("Describe"))) {
                    String carBitNum = jso.getJSONArray("Describe").getJSONObject(0).get("ParkingNo").toString();
                    String license = jso.getJSONArray("Describe").getJSONObject(0).get("CarPlateNo").toString();
                    String parkInTime = jso.getJSONArray("Describe").getJSONObject(0).get("DateTime").toString();
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy/M/d H:mm:ss");
                    LocalDateTime entryTime = LocalDateTime.parse(parkInTime, inputFormatter);
                    placeVo.setCarBitNum(carBitNum);
                    placeVo.setLicense(license);
                    placeVo.setEntryTime(entryTime);
                    placeList.add(placeVo);
                    ultramanTiga(jso, placeVo, place, mapId, "0", 1);
                } else if (!NullUtils.isEmpty(url) && !NullUtils.isEmpty(jso)){
                    ultramanTiga(jso, placeVo, place, mapId, "1", 0);
                }
            });
            long end = System.currentTimeMillis();
            log.error ("当前allPlace.size是："+(end-start));

        }
    }


    private void ultramanTiga(JSONObject jso, PlaceVo placeVo, ParkingPlace place, Integer mapId, String code, int state) {
        if (code.equals(jso.get("Code").toString()) && !NullUtils.isEmpty(placeVo.getLicense())) {
            if (placeMapper.updatePlaceById(place.getId(), state, placeVo.getLicense()) > 0) {
                placeVo.setState(state);
                Object json = JSONObject.toJSON(placeVo);
                Map<String, Object> map = new HashMap<>();
                map.put("params", json);
                String jsonString = JSONObject.toJSONString(map);
                HttpMethod post = HttpMethod.POST;
                //String onLineUrl = "http://192.168.1.124:8081/park/updatePlaceDataByPlaceId";
                String onLineUrl = "http://www.3-si.cn:10087/UWB/park/updatePlaceDataByPlaceId";
                String webUrl = HttpClientUtil.httpPostClient(onLineUrl, post, jsonString,MediaType.APPLICATION_JSON);
                log.error("webUrl:" + webUrl);
            }
        }
        if (code.equals(jso.get("Code").toString()) && NullUtils.isEmpty(placeVo.getLicense())) {
            if (placeMapper.updatePlaceById(place.getId(), state, "null") > 0) {
                placeVo.setState(state);
                Object json = JSONObject.toJSON(placeVo);
                Map<String, Object> map = new HashMap<>();
                map.put("params", json);
                String jsonString = JSONObject.toJSONString(map);
                HttpMethod post = HttpMethod.POST;
                //String onLineUrl = "http://192.168.1.124:8081/park/updatePlaceDataByPlaceId";
                String onLineUrl = "http://www.3-si.cn:10087/UWB/park/updatePlaceDataByPlaceId";
                String webUrl = HttpClientUtil.httpPostClient(onLineUrl, post, jsonString,MediaType.APPLICATION_JSON);
                log.error("webUrl:" + webUrl);
            }
        }
    }

    //这是一个main方法，程序的入口
    //这是一个main方法，程序的入口
    public static void main(String[] args){
        Map<String,Object> map = new HashMap<>();
        List<PlaceVo> placeVoList = new ArrayList<>();
        PlaceVo placeVo = new PlaceVo();
        placeVo.setId(1312);
        placeVo.setMap(1);
        placeVo.setState(1);
        placeVo.setLicense("2313");
        placeVoList.add(placeVo);
        String jsonArray=JSON.toJSONString(placeVoList);
        map.put("params", jsonArray);
    }




    @RequestMapping(value = "/testBatch")
    public String testBatch() {
        List<PlaceVo> a = new ArrayList<>();
        PlaceVo v1 = new PlaceVo();
        v1.setId(236536);
        v1.setName("test1111");
        v1.setState(0);
        v1.setLicense("111");

        PlaceVo v2 = new PlaceVo();
        v2.setId(236537);
        v2.setName("test2222");
        v2.setState(0);
        v2.setLicense("111");

        PlaceVo v3 = new PlaceVo();
        v3.setId(236538);
        v3.setName("test3333");
        v3.setState(0);
        v3.setLicense("11");

        PlaceVo v4 = new PlaceVo();
        v4.setId(236539);
        v4.setName("test4444");
        v4.setState(0);
        v4.setLicense("11");

        PlaceVo v5 = new PlaceVo();
        v5.setId(236540);
        v5.setName("test5555");
        v5.setState(0);
        v5.setLicense("231");
        a.add(v1);
        a.add(v2);
        a.add(v3);
        a.add(v4);
        a.add(v5);
        placeMapper.updateBatchById(a);
        return "111";
    }





    @RequestMapping("add")
    public String add(){
        Map<String,Object> map = new HashMap<>();
        List<PlaceVo> placeVoList = new ArrayList<>();
        PlaceVo placeVo = new PlaceVo();
        placeVo.setId(1312);
        placeVo.setMap(1);
        placeVo.setState( 1);
        placeVo.setLicense("2313");
        placeVoList.add(placeVo);
        map.put("params", placeVoList);
        return "111";
    }
}