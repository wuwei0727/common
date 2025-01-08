package com.tgy.rtls.docking.controller.park;

import com.alibaba.fastjson.JSONObject;
import com.tgy.rtls.docking.dao.ParkingPlace;
import com.tgy.rtls.docking.dao.PlaceVideoDetection;
import com.tgy.rtls.docking.dao.PlaceVo;
import com.tgy.rtls.docking.mapper.PlaceMapper;
import com.tgy.rtls.docking.service.park.PlaceVideoDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.docking.controller.park
 * @Author: wuwei
 * @CreateTime: 2023-12-08 17:06
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class PlaceVideoScheduledTasks {
    @Resource
    private PlaceVideoDetectionService placeVideoDetectionService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private PlaceMapper placeMapper;
    private static Map<String, PlaceVo> oldMap = new HashMap<>();
    private final AtomicReference<String> placeName = new AtomicReference<>();
    private static final String API_A_URL = "http://api-a-url/parking-places";

    public void setPlaceName(String placeName) {
        this.placeName.set(placeName);
    }

    @Scheduled(cron = "0 */20 * * * ?")
    @RequestMapping("/syncParkingPlaces")
    public void syncParkingPlaces() {
        List<PlaceVideoDetection> placeVideoDetections = placeVideoDetectionService.getAllGuideScreenDeviceOrConditionQuery(178);

        List<ParkingPlace> apiAData = fetchDataFromApiA(placeVideoDetections.get(0).getApiUrl());
        List<ParkingPlace> localData = fetchLocalData();
        List<ParkingPlace> recordsToUpdate = compareAndUpdate(apiAData,localData);
        // 批量更新
        if (!recordsToUpdate.isEmpty()) {
            batchUpdateParkingPlaces(recordsToUpdate);
        }
    }
    private void batchUpdateParkingPlaces(List<ParkingPlace> recordsToUpdate) {
//        for (ParkingPlace placeVo : recordsToUpdate) {
//            placeVideoDetectionService.getAllPlaceByMap(placeVo.getId(),178);
//        }
        placeMapper.batchInsertParkingPlaces(recordsToUpdate);
        log.info("Batch update completed. Total records updated: " + recordsToUpdate.size());
    }

    @RequestMapping("/clearMap")
    public int clearMap(){
        placeVideoDetectionService.clearMap();
        return 1;
    }

    @RequestMapping("/getMap")
    public Map<String, PlaceVo> getMap(){
        return placeVideoDetectionService.getMap();
    }

    @GetMapping("/getPlaceVideoDetectionData")
    public void executeTask(String placeName) {
        placeVideoDetectionService.getPlaceVideoDetectionData(placeName);
    }




    @RequestMapping("/getTime")
    public String getTime(){
        // 获取当前时间
        LocalTime now = LocalTime.now();
        LocalTime timeBefore16Minutes = now.minus(16, ChronoUnit.MINUTES);
        return timeBefore16Minutes.toString();
    }

    private List<ParkingPlace> fetchDataFromApiA(String apiUrl) {
        String urlWithParams = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("mapId", 178)
                .build().toUriString();
        ResponseEntity<List<ParkingPlace>> response = restTemplate.exchange(urlWithParams,HttpMethod.GET,null,new ParameterizedTypeReference<List<ParkingPlace>>() {});
        return response.getBody();
    }

    private List<ParkingPlace> fetchLocalData() {
        return placeVideoDetectionService.getAllPlaceByMap(null,178);
    }

    private List<ParkingPlace> compareAndUpdate(List<ParkingPlace> apiAData, List<ParkingPlace> localData) {
        // 创建一个列表用于存储需要更新的记录
        List<ParkingPlace> recordsToUpdate = new ArrayList<>();

        // 创建一个map以便于快速查找本地数据
        Map<String, ParkingPlace> localDataMap = localData.stream()
                .collect(Collectors.toMap(ParkingPlace::getName, Function.identity(),(existing, replacement) -> existing));

        // 对比API A的数据和本地数据
        for (ParkingPlace apiARecord : apiAData) {
            ParkingPlace localRecord = localDataMap.get(apiARecord.getName());
            if(localRecord!=null){
                if (!apiARecord.getName().equals(localRecord.getName())) {
                    recordsToUpdate.add(apiARecord);
                }
            }else {
                recordsToUpdate.add(apiARecord);
            }
        }

        return recordsToUpdate;
    }


    @RequestMapping("/testq")
    public void testq(String PlateNo){
        // PlaceVo placeVo = new PlaceVo();
        // placeVo.setId(1);
        // placeVo.setName("东A-165");
        // placeVo.setState(0);
        // placeVo.setMap(178);
        // placeVo.setConfigWay(2);
        // placeVo.setEntryTime(LocalDateTime.now());
        //
        // PlaceVo placeVo1 = new PlaceVo();
        // placeVo1.setId(27218);
        // placeVo1.setState(0);
        // placeVo1.setMap(178);
        // placeVo1.setName("东A-166");
        // placeVo1.setCarBitNum("东A-166");
        // placeVo1.setConfigWay(3);
        // placeVo1.setLicense("粤EF33515");
        // placeVo1.setEntryTime(LocalDateTime.of(2023,12,14,9,10,16));
        //
        // Map<String,PlaceVo> map = new HashMap<>();
        // map.put(String.valueOf(placeVo.getId()), placeVo);
        // map.put(String.valueOf(placeVo1.getId()), placeVo1);
        //
        // updateData(map);


    }

    public static void main(String[] args) {
        PlaceVo placeVo = new PlaceVo();
        placeVo.setId(1);
        placeVo.setState(0);
        placeVo.setLicense("hhhh");
        placeVo.setEntryTime(LocalDateTime.now());

        PlaceVo placeVo1 = new PlaceVo();
        placeVo1.setId(2);
        placeVo1.setState(1);
        placeVo1.setLicense("hhhh");
        PlaceVo vo = new PlaceVo();

        PlaceVo placeVo2 = new PlaceVo();
        placeVo2.setId(3);
        placeVo2.setState(2);
        placeVo2.setLicense("hhhh");
        Map<String,PlaceVo> map = new HashMap<>();
        map.put(String.valueOf(placeVo.getId()), placeVo);
        map.put(String.valueOf(placeVo.getId()), placeVo1);
        map.put(String.valueOf(placeVo.getId()), placeVo2);
        map.put("fullUploadTime", vo.setEntryTime(LocalDateTime.now()));
        System.out.println("jso = " + map);
        // updateData(map);
        List<PlaceVo> list = map.entrySet().stream()
                .filter(entry -> !"fullUploadTime".equals(entry.getKey()))  // 排除 fullUploadTime 键
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)  // 确保 PlaceVo 非空
                .collect(Collectors.toList());
        System.out.println("list = " + JSONObject.parseArray(JSONObject.toJSONString(list)));
    }

}
