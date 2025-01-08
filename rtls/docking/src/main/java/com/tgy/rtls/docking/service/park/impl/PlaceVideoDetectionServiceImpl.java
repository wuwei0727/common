package com.tgy.rtls.docking.service.park.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tgy.rtls.docking.controller.park.HttpClientUtil;
import com.tgy.rtls.docking.dao.ParkingPlace;
import com.tgy.rtls.docking.dao.PlaceVideoDetection;
import com.tgy.rtls.docking.dao.PlaceVo;
import com.tgy.rtls.docking.mapper.PlaceMapper;
import com.tgy.rtls.docking.service.park.PlaceVideoDetectionService;
import com.tgy.rtls.docking.utils.NullUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.park
 * @Author: wuwei
 * @CreateTime: 2023-05-31 15:58
 * @Description: TODO
 * @Version: 1.0
 */
@Service
@Slf4j
public class PlaceVideoDetectionServiceImpl implements PlaceVideoDetectionService {
    @Value("${rtls.video.t0.time}")
    private Integer videoT0Time;
    @Autowired
    private PlaceMapper placeMapper;
    private static final HttpMethod post = HttpMethod.POST;
    private static Map<String, PlaceVo> oldMap = new HashMap<>();

    @Override
    public void clearMap() {
        oldMap.clear();
    }

    @Override
    public Map<String, PlaceVo> getMap() {
        return oldMap;
    }

    @Override
    public List<PlaceVideoDetection> getAllGuideScreenDeviceOrConditionQuery(Integer map) {
        return placeMapper.getAllGuideScreenDeviceOrConditionQuery(map);
    }

    @Override
    public List<ParkingPlace> getAllPlaceByMap(Integer id,Integer maps) {
        return placeMapper.getAllPlaceByMap(id,maps);
    }


    @Override
    @Async("vehicleCountThreadPool")
    public void getPlaceVideoDetectionData(String placeName) {
        Map<String, PlaceVo> map = new HashMap<>();
        Instant start = Instant.now();
        log.error("getPlaceVideoDetectionData1 → 进入");

        List<PlaceVideoDetection> placeVideoDetections = placeMapper.getAllGuideScreenDeviceOrConditionQuery(178);

        Integer mapId = placeVideoDetections.size() > 1 ? null : Math.toIntExact(placeVideoDetections.get(0).getMap());
        String ip = placeVideoDetections.get(0).getIp();
        AtomicBoolean shouldUpdateRedis = new AtomicBoolean(false);

        if (!NullUtils.isEmpty(mapId)) {
            List<ParkingPlace> allPlace = placeMapper.getAllPlace(mapId, placeName);
            String url1 = placeVideoDetections.get(0).getPlaceInquireAddress();

            // 使用 CompletableFuture 并行处理请求

            // 等待所有请求完成
            CompletableFuture.allOf(allPlace.stream()
                    .map(place -> CompletableFuture.runAsync(() -> {
                        Instant requestStart = Instant.now();

                        PlaceVo requestVo = new PlaceVo();
                        requestVo.setPlateNo(place.getName());
                        String response = HttpClientUtil.httpPostClient(url1, post, JSONObject.toJSONString(requestVo), MediaType.APPLICATION_JSON);

                        Instant requestEnd = Instant.now();
                        Duration requestDuration = Duration.between(requestStart, requestEnd);
                        log.info("Request for place ID " + place.getId() + " took " + requestDuration.toMillis() + " milliseconds.");

                        JSONObject jsonResponse = JSON.parseObject(response);

                        PlaceVo placeVo = new PlaceVo();
                        placeVo.setId(place.getId());
                        placeVo.setName(place.getName());
                        placeVo.setMap(mapId);

                        if (!NullUtils.isEmpty(response) && !NullUtils.isEmpty(jsonResponse.getJSONArray("Describe"))) {
                            JSONObject describe = jsonResponse.getJSONArray("Describe").getJSONObject(0);

                            placeVo.setCarBitNum(describe.getString("ParkingNo"));
                            placeVo.setLicense(describe.getString("CarPlateNo"));

                            String parkInTime = describe.getString("DateTime");
                            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy/M/d H:mm:ss");
                            LocalDateTime entryTime = LocalDateTime.parse(parkInTime, inputFormatter);
                            placeVo.setEntryTime(entryTime);

                            placeVo.setState(1);
                        } else {
                            placeVo.setState(0);
                        }
                        map.put(String.valueOf(place.getId()), placeVo);
                        if (!jsonResponse.getInteger("Code").equals(2)) {
                            shouldUpdateRedis.set(true); // Set flag to true if Code is not 2
                        }
                    })).toArray(CompletableFuture[]::new)).join();

            Instant end = Instant.now();
            Duration totalDuration = Duration.between(start, end);
            log.info("CompletableFuture processing time: " + totalDuration.toMillis() + " milliseconds.");
            Map<String, PlaceVo> placeVoMap = updateData(map,ip);
            if (placeVoMap.isEmpty()&&shouldUpdateRedis.get()) {
                HttpClientUtil.httpPostClient(ip, post, Collections.singletonList(new PlaceVo().setMap(178)), MediaType.APPLICATION_JSON);
                log.info("缓存没数据或者没有不同的数据");
            } else {
                log.info("success");
            }
            log.error("placeVoMap = " + placeVoMap);
        }

        Instant end = Instant.now();
        Duration totalDuration = Duration.between(start, end);
        log.info("Total processing time: " + totalDuration.toMillis() + " milliseconds.");
    }

    @Async("vehicleCountThreadPool")
    @Override
    public void getPlaceVideoDetectionDataq() {
        Map<String, PlaceVo> map = new HashMap<>();
        long start = System.currentTimeMillis();

        List<PlaceVideoDetection> placeVideoDetections = placeMapper.getAllGuideScreenDeviceOrConditionQuery(178);

        Integer mapId = placeVideoDetections.size() > 1 ? null : Math.toIntExact(placeVideoDetections.get(0).getMap());
        String ip=placeVideoDetections.get(0).getIp();
        log.info("mapId = " + mapId);
        log.info("ip = " + ip);
        // Integer mapId=178;String url1 = "http://61.145.96.90:60091/api/Vehiclefind/FindCarInfo";
        if (!NullUtils.isEmpty(mapId)) {
            List<ParkingPlace> allPlace = placeMapper.getAllPlace(mapId,null);
            String url1 = placeVideoDetections.get(0).getPlaceInquireAddress();
            final int[] i = {1};
            long startfor = System.currentTimeMillis();
            boolean shouldUpdateRedis = false;

            for (ParkingPlace place : allPlace) {
                log.info ("当前 allPlace.size是："+ i[0]++);

                PlaceVo requestVo = new PlaceVo();
                requestVo.setPlateNo (place.getName ());

                String response  = HttpClientUtil.httpPostClient(url1, post, JSONObject.toJSONString(requestVo), MediaType.APPLICATION_JSON);
                JSONObject jsonResponse  = JSON.parseObject(response);

                PlaceVo placeVo = new PlaceVo();
                placeVo.setId(place.getId());
                placeVo.setName(place.getName());
                placeVo.setMap(mapId);

                if (!NullUtils.isEmpty(response ) && !NullUtils.isEmpty(jsonResponse.getJSONArray("Describe"))) {
                    JSONObject describe = jsonResponse.getJSONArray("Describe").getJSONObject(0);

                    placeVo.setCarBitNum(describe.getString("ParkingNo"));
                    placeVo.setLicense(describe.getString("CarPlateNo"));

                    String parkInTime = describe.getString("DateTime");
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy/M/d H:mm:ss");
                    LocalDateTime entryTime = LocalDateTime.parse(parkInTime, inputFormatter);
                    placeVo.setEntryTime(entryTime);

                    placeVo.setState(1);
                } else {
                    placeVo.setState(0);
                }
                map.put(String.valueOf(place.getId()),placeVo);
                // Check the Code from the response
                if (!jsonResponse.getInteger("Code").equals(2)) {
                    shouldUpdateRedis = true; // Set flag to true if Code is not 2
                }
            }
                long endfor = System.currentTimeMillis();
                log.error ("for place执行完时间是："+(endfor-startfor)/1000);
            Map<String, PlaceVo> placeVoMap = updateData(map,ip);
            if(placeVoMap.isEmpty()&& shouldUpdateRedis){
                HttpClientUtil.httpPostClient(ip, post, Collections.singletonList(new PlaceVo()), MediaType.APPLICATION_JSON);
                log.info("缓存没数据或者没有不同的数据");
            }else {
                log.info("success");
            }
            log.error("placeVoMap = " + placeVoMap);
            long end = System.currentTimeMillis();
            log.error ("getPlaceVideoDetectionData执行完时间是："+(end-start)/1000);

        }
    }

    /**
     * 更新 Map 中的数据，并将不同的数据存储到 diffMap 中。
     *
     * @param currentData 当前查询到的数据。
     * @param ip
     * @return 返回包含所有不同数据的 Map。
     */
    private Map<String, PlaceVo> updateData(Map<String, PlaceVo> currentData, String ip) {
        LocalDateTime now = LocalDateTime.now();
        PlaceVo vo = new PlaceVo();
        Map<String, PlaceVo> diffMap = new HashMap<>();

        // 初次加载时，直接将当前数据存入 oldMap，并上传
        if (oldMap.isEmpty()) {
            oldMap.putAll(currentData);

//            List<PlaceVo> list = new ArrayList<>(oldMap.values());
            // 过滤掉 fullUploadTime 键，构建新的 list
            List<PlaceVo> list = oldMap.entrySet().stream()
                    .filter(entry -> !"fullUploadTime".equals(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .filter(Objects::nonNull)  // 确保 PlaceVo 非空
                    .collect(Collectors.toList());
            // JSONArray objects = JSONObject.parseArray(JSONObject.toJSONString(list));
            // log.info(JSONObject.toJSONString(list));
            HttpClientUtil.httpPostClient(ip,post,list ,MediaType.APPLICATION_JSON);
            oldMap.put("fullUploadTime", vo.setFullUploadTime(now));
        } else {
            // 检查缓存中的数据更新时间是否超过设定时间
            Duration duration = Duration.between(oldMap.get("fullUploadTime").getFullUploadTime(), now);
            if(duration.toMinutes() > videoT0Time){//大于15
                oldMap.putAll(currentData);

//                List<PlaceVo> list = new ArrayList<>(oldMap.values());
                // 过滤掉 fullUploadTime 键，构建新的 list
                List<PlaceVo> list = oldMap.entrySet().stream()
                        .filter(entry -> !"fullUploadTime".equals(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .filter(Objects::nonNull)  // 确保 PlaceVo 非空
                        .collect(Collectors.toList());

                // JSONArray objects = JSONObject.parseArray(JSONObject.toJSONString(list));
                // log.info(JSONObject.toJSONString(list));
                HttpClientUtil.httpPostClient(ip,post,list ,MediaType.APPLICATION_JSON);
                oldMap.put("fullUploadTime", vo.setFullUploadTime(now));
            }else{
                // 比较当前数据与缓存数据的差异
                currentData.forEach((key, currentValue) -> {
                    PlaceVo oldValue = oldMap.get(key);
                    // 如果oldMap没有key或者有key但value不相等，就把它添加到diffMap中
                    if (oldValue == null || !Objects.equals(oldValue.getState(), currentValue.getState())) {
                        diffMap.put(key, currentValue);
                    }});

                // 如果有差异数据，则更新缓存并上传
                if(!NullUtils.isEmpty(diffMap)){
                    List<PlaceVo> list = oldMap.entrySet().stream()
                            .filter(entry -> !"fullUploadTime".equals(entry.getKey()))
                            .map(Map.Entry::getValue)
                            .filter(Objects::nonNull)  // 确保 PlaceVo 非空
                            .collect(Collectors.toList());

                    HttpClientUtil.httpPostClient(ip,post,list,MediaType.APPLICATION_JSON);
                    oldMap.clear();
                    oldMap.put("fullUploadTime", vo.setFullUploadTime(now));
                    oldMap.putAll(currentData);
                }
            }

        }
        // 返回包含不同数据的Map
        return diffMap;
    }






    private Map<String, PlaceVo> updateData1(Map<String, PlaceVo> currentData) {
        LocalDateTime now = LocalDateTime.now();
        PlaceVo vo = new PlaceVo();
        Map<String, PlaceVo> diffMap = new HashMap<>();

        // 初次加载时，直接将当前数据存入 oldMap，并上传
        if (oldMap.isEmpty()) {
            oldMap.put("fullUploadTime", vo.setFullUploadTime(now));
            oldMap.putAll(currentData);

//            List<PlaceVo> list = new ArrayList<>(oldMap.values());
            // 过滤掉 fullUploadTime 键，构建新的 list
            List<PlaceVo> list = oldMap.entrySet().stream()
                    .filter(entry -> !"fullUploadTime".equals(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .filter(Objects::nonNull)  // 确保 PlaceVo 非空
                    .collect(Collectors.toList());
            // JSONArray objects = JSONObject.parseArray(JSONObject.toJSONString(list));
            log.info(JSONObject.toJSONString(list));
        } else {
            // 检查缓存中的数据更新时间是否超过设定时间
            Duration duration = Duration.between(oldMap.get("fullUploadTime").getFullUploadTime(), now);
            if(duration.toMinutes() > videoT0Time){//大于15
                oldMap.put("fullUploadTime", vo.setFullUploadTime(now));
                oldMap.putAll(currentData);

//                List<PlaceVo> list = new ArrayList<>(oldMap.values());
                // 过滤掉 fullUploadTime 键，构建新的 list
                List<PlaceVo> list = oldMap.entrySet().stream()
                        .filter(entry -> !"fullUploadTime".equals(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .filter(Objects::nonNull)  // 确保 PlaceVo 非空
                        .collect(Collectors.toList());

                // JSONArray objects = JSONObject.parseArray(JSONObject.toJSONString(list));
                log.info(JSONObject.toJSONString(list));
            }else{
                // 比较当前数据与缓存数据的差异
                currentData.forEach((key, currentValue) -> {
                    PlaceVo oldValue = oldMap.get(key);
                    // 如果oldMap没有key或者有key但value不相等，就把它添加到diffMap中
                    if (oldValue == null || !Objects.equals(oldValue.getState(), currentValue.getState())) {
                        diffMap.put(key, currentValue);
                    }});

                // 如果有差异数据，则更新缓存并上传
                if(!NullUtils.isEmpty(diffMap)){
                    List<PlaceVo> list = oldMap.entrySet().stream()
                            .filter(entry -> !"fullUploadTime".equals(entry.getKey()))
                            .map(Map.Entry::getValue)
                            .filter(Objects::nonNull)  // 确保 PlaceVo 非空
                            .collect(Collectors.toList());

                    oldMap.clear();
                    oldMap.put("fullUploadTime", vo.setFullUploadTime(now));
                    oldMap.putAll(currentData);
                }
            }

        }
        // 返回包含不同数据的Map
        return diffMap;
    }
}
