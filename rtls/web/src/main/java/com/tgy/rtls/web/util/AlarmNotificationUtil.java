package com.tgy.rtls.web.util;

import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.warn.AlarmPersonnelBindings;
import com.tgy.rtls.data.service.park.SmsQuotaService;
import com.tgy.rtls.data.service.sms.ALiYunSmsService;
import com.tgy.rtls.data.service.warn.AlarmPersonnelBindingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmNotificationUtil {

    private final AlarmPersonnelBindingsService alarmPersonnelBindingsService;
    private final SmsQuotaService smsQuotaService;
    private final ALiYunSmsService aLiYunSmsService;


    public void sendAlarmNotifications(Set<Integer> mapIds,String templateCode,Integer count, Integer equipmentType, Integer priority) {
        for (Integer mapId : mapIds) {
            try {
                List<AlarmPersonnelBindings> bindings = alarmPersonnelBindingsService.getBindingsByCondition(mapId, equipmentType, priority);

                if (NullUtils.isEmpty(bindings)) {
                    log.warn("No maintenance staff found for map: {}, equipmentType: {}, priority: {}",mapId, equipmentType, priority);
                    continue;
                }

                // 先检查并扣除短信配额
                if (!smsQuotaService.deductQuota(mapId, count)) {
                    log.warn("Failed to deduct SMS quota for map: {}", mapId);
                    continue;
                }

                Map<String, Object> templateParams = new HashMap<>();
                templateParams.put("mapName", bindings.get(0).getMapName());
                templateParams.put("desc", bindings.get(0).getAlarmTypeNames());

                // 发送短信给每个维护人员
                for (AlarmPersonnelBindings binding : bindings) {
                    try {
                        aLiYunSmsService.sendMessage(binding.getPhone().trim(), templateCode, templateParams);
                    } catch (Exception e) {
                        log.error("Failed to send SMS to phone: " + binding.getPhone(), e);
                    }
                }
            } catch (Exception e) {
                log.error("Error sending alarm notifications for map: " + mapId, e);
            }
        }
    }
}
