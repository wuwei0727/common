package com.tgy.rtls.web.controller.promoter;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.promoter.PromoterLog;
import com.tgy.rtls.data.service.promoter.PromoterLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/promoter_log")
public class PromoterLogController {
    @Autowired
    private PromoterLogService promoterLogService;

    @RequestMapping(value = "addPromoterLog")
    public CommonResult<Object> addPromoterLog(@RequestBody PromoterLog promoterLog) {
        try {
            promoterLogService.saveOrUpdate(promoterLog);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
}
