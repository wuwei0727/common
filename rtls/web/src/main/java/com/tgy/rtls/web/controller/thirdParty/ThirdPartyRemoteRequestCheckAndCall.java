package com.tgy.rtls.web.controller.thirdParty;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.FloorLockConfig;
import com.tgy.rtls.data.service.park.impl.FloorLockConfigService;
import com.tgy.rtls.data.service.remote.RemoteRequestServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.thirdParty
 * @Author: wuwei
 * @CreateTime: 2024-06-16 20:01
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping(value = "/thirdParty")
@CrossOrigin
@Slf4j
public class ThirdPartyRemoteRequestCheckAndCall {
    @Resource
    private RemoteRequestServiceImpl requestService;
    @Resource
    private FloorLockConfigService lockConfigService;
    @PostMapping(value = "/thirdPartyCheckAndCall")
    public CommonResult<Object> thirdPartyCheckAndCall(String nedid, String mode, String code) {
        try {
            FloorLockConfig one = lockConfigService.getOne(new QueryWrapper<FloorLockConfig>().eq("call_code", code));
            if(NullUtils.isEmpty(one)){
               return new CommonResult<>(198,"验证码无效");
           }

            LocalDateTime currentDateTime = LocalDateTime.now();

            if (currentDateTime.isAfter(one.getValidEndTime())) {
                return new CommonResult<>(199,"验证码过期");
            }

            String url = String.format("/kk/setNedMode?nedid=%s&mode=%s", nedid, mode);
            return requestService.checkAndCall(url);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(e.getCause().getMessage()));
        }
    }

    @PostMapping(value = "/thirdPartyCheckAndCall2")
    public CommonResult<Object> thirdPartyCheckAndCall2(String nedid, String mode) {
        try {
            String url = String.format("/kk/setNedMode?nedid=%s&mode=%s", nedid, mode);
            return requestService.checkAndCall(url);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(e.getCause().getMessage()));
        }
    }
}
