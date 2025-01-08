package com.tgy.rtls.web.controller.park.floorLock;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tgy.rtls.data.entity.park.floorLock.PlaceUnlockRecords;
import com.tgy.rtls.data.entity.park.floorLock.TimePeriodAdmin;
import com.tgy.rtls.data.service.park.floorLock.impl.PlaceUnlockRecordsService;
import com.tgy.rtls.data.service.park.floorLock.impl.TimePeriodAdminService;
import com.tgy.rtls.data.service.park.floorLock.impl.UserCompanyMapService;
import com.tgy.rtls.data.service.sms.ALiYunSmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RestController
@RequestMapping("/remind")
public class ScheduledTasks {

    @Autowired
    private PlaceUnlockRecordsService placeUnlockRecordsService;
    @Autowired
    private UserCompanyMapService userCompanyMapService;
    @Autowired
    private TimePeriodAdminService timePeriodAdminService;
    @Autowired
    private ALiYunSmsService aLiYunSmsService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Scheduled(cron = "0 */12 * * * ?")
    @RequestMapping("/checkParkingStatus1")
    public void checkParkingStatus1() {
        List<PlaceUnlockRecords> unlockInfos = placeUnlockRecordsService.getPlaceUnlockRecords(null, null, null, null, null, null, "0", null);
        LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDate today = LocalDate.now();

        if (unlockInfos == null || unlockInfos.isEmpty()) {
            return;
        }

        unlockInfos.stream()
                .filter(Objects::nonNull)
                .forEach(record -> processUnlockRecord(record, now, today));
    }

    private void processUnlockRecord(PlaceUnlockRecords record, LocalTime now, LocalDate today) {
        List<TimePeriodAdmin> timePeriods = timePeriodAdminService.list(new QueryWrapper<TimePeriodAdmin>().eq("company_id", record.getCompanyId()));

        if (timePeriods == null || timePeriods.isEmpty()) {
            return;
        }

        for (TimePeriodAdmin period : timePeriods) {
            long hoursBetween = ChronoUnit.HOURS.between(now, period.getStartTime());

            if (hoursBetween >= 2) {
                continue;
            }

            String redisKey = "sms:sent:" + record.getId() + ":" + today;
            List<String> smsTimes = Objects.requireNonNull(redisTemplate.opsForList().range(redisKey, 0, -1))
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());

            if (smsTimes.isEmpty()) {
                sendSmsReminderIfRequired(record, period, now, redisKey);
            } else if (smsTimes.size() == 1) {
                sendSecondSmsIfRequired(record, period, now, redisKey, smsTimes.get(0));
            }
        }
    }

    private void sendSmsReminderIfRequired(PlaceUnlockRecords record, TimePeriodAdmin period, LocalTime now, String redisKey) {
        Map<String, Object> map = createSmsMap(record, period);
        if (sendSmsReminder(record.getPhone(), map)) {
            redisTemplate.opsForList().rightPush(redisKey, now.toString());
            redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);
        }
    }

    private void sendSecondSmsIfRequired(PlaceUnlockRecords record, TimePeriodAdmin period, LocalTime now, String redisKey, String firstSmsTimeStr) {
        LocalTime firstSmsTime = LocalTime.parse(firstSmsTimeStr);
        long minutesSinceFirstSms = ChronoUnit.MINUTES.between(firstSmsTime, now);

        if (minutesSinceFirstSms >= 12) {
            Map<String, Object> map = createSmsMap(record, period);
            sendSmsReminder(record.getPhone(), map);
            redisTemplate.opsForList().rightPush(redisKey, now.toString());
        }
    }

    private Map<String, Object> createSmsMap(PlaceUnlockRecords record, TimePeriodAdmin period) {
        Map<String, Object> map = new HashMap<>();
        map.put("mapName", record.getMapName());
        map.put("placeName", record.getPlaceName());
        map.put("time", String.valueOf(period.getStartTime()));
        return map;
    }


    private boolean sendSmsReminder(String phone, Map<String,Object> templateParamJson) {
        // Implement SMS sending logic here
        return aLiYunSmsService.sendMessage(phone,"SMS_471785160",templateParamJson);
    }
}