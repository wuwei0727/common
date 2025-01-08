package com.tgy.rtls.web.controller.park;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.userinfo.WechatUserPosition;
import com.tgy.rtls.data.mapper.park.CollectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.park
 * @Author: wuwei
 * @CreateTime: 2023-10-30 14:12
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@CrossOrigin
@RequestMapping (value = "/heatmapAndTrajectory")
public class HeatmapAndTrajectoryController {
    @Autowired
    private CollectMapper collectMapper;

    @GetMapping (value = "/getHeatmap")
    public CommonResult<Object> getHeatmap(Integer map,Integer level,Long start ,Long end,Integer userId) {
        try {
            Map<String,Object> hashMap = new HashMap<>();
            ZoneId of = ZoneId.of("Asia/Shanghai");
            // Convert start timestamp to LocalDateTime
            Instant startInstant = Instant.ofEpochMilli(start);
            LocalDateTime startDateTime = LocalDateTime.ofInstant(startInstant, of);
            // Convert end timestamp to LocalDateTime
            Instant endInstant = Instant.ofEpochMilli(end);
            LocalDateTime endDateTime = LocalDateTime.ofInstant(endInstant, of);
            // Format LocalDateTime objects to desired string format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String startTime = startDateTime.format(formatter);
            String endTime = endDateTime.format(formatter);

            List<WechatUserPosition> heatmap = collectMapper.getHeatmap(map, level, startTime, endTime,userId);
            hashMap.put("hashMap", heatmap);

            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),hashMap);

        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }
}
