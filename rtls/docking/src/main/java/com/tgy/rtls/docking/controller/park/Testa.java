package com.tgy.rtls.docking.controller.park;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tgy.rtls.docking.dao.ParkingPlace;
import com.tgy.rtls.docking.dao.PlaceVideoDetection;
import com.tgy.rtls.docking.mapper.PlaceMapper;
import com.tgy.rtls.docking.utils.NullUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.docking.controller.park
 * @Author: wuwei
 * @CreateTime: 2024-09-06 14:17
 * @Description: TODO
 * @Version: 1.0
 */
@RestController("aa")
@Slf4j
public class Testa {

    @Autowired
    private PlaceMapper placeMapper;
    private static final HttpMethod post = HttpMethod.POST;

    @RequestMapping("tetst")
    public void tetst(String placeName,HttpServletResponse response1) throws IOException {
        List<PlaceVo1> placeVoList  = new ArrayList<>();
        log.error("getPlaceVideoDetectionData1 → 进入");

        List<PlaceVideoDetection> placeVideoDetections = placeMapper.getAllGuideScreenDeviceOrConditionQuery(178);

        Integer mapId = placeVideoDetections.size() > 1 ? null : Math.toIntExact(placeVideoDetections.get(0).getMap());
        String ip = placeVideoDetections.get(0).getIp();
        if (!NullUtils.isEmpty(mapId)) {
            List<ParkingPlace> allPlace = placeMapper.getAllPlace(mapId, placeName);
            String url1 = placeVideoDetections.get(0).getPlaceInquireAddress();
            // 使用 CompletableFuture 并行处理请求
            // 等待所有请求完成
            CompletableFuture.allOf(allPlace.stream()
                    .map(place -> CompletableFuture.runAsync(() -> {
                        Instant requestStart = Instant.now();

                        PlaceVo1 requestVo = new PlaceVo1();
                        requestVo.setPlateNo(place.getName());
                        String response = HttpClientUtil.httpPostClient(url1, post, JSONObject.toJSONString(requestVo), MediaType.APPLICATION_JSON);

                        Instant requestEnd = Instant.now();
                        Duration requestDuration = Duration.between(requestStart, requestEnd);
                        log.info("Request for place ID " + place.getId() + " took " + requestDuration.toMillis() + " milliseconds.");

                        JSONObject jsonResponse = JSON.parseObject(response);

                        PlaceVo1 placeVo = new PlaceVo1();
                        placeVo.setName(place.getName());


                        if (!NullUtils.isEmpty(response) && !NullUtils.isEmpty(jsonResponse.getJSONArray("Describe"))) {
                            JSONObject describe = jsonResponse.getJSONArray("Describe").getJSONObject(0);

                            placeVo.setPlateNo(describe.getString("ParkingNo"));
                            placeVo.setLicense(describe.getString("CarPlateNo"));
                            placeVo.setState("占用");
                        } else {
                            placeVo.setState("空闲");
                        }
                        // 添加到列表中
                        synchronized (placeVoList) { // 线程安全的添加操作
                            placeVoList.add(placeVo);
                        }
                    })).toArray(CompletableFuture[]::new)).join();
        }
        placeVoList.sort(Comparator.comparing(PlaceVo1::getName));
        // 设置HTTP响应头信息
        response1.setContentType("application/vnd.ms-excel");
        response1.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("停车位信息", "UTF-8").replaceAll("\\+", "%20");
        response1.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        // 使用 EasyExcel 导出
        EasyExcel.write(response1.getOutputStream(), PlaceVo1.class)
                .sheet("停车位信息")
                .doWrite(placeVoList);
    }
}
