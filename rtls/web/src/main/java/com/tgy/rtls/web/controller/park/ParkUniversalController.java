package com.tgy.rtls.web.controller.park;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.vip.VipParking;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.vip.VipParkingService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.park
 * @Author: wuwei
 * @CreateTime: 2024-01-11 10:29
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/parkUniversal")
@Slf4j
public class ParkUniversalController {
    @Autowired
    private VipParkingService vipParkingService;
    @Autowired
    private OperationlogService operationlogService;



    @RequestMapping(value = "/updateVipParingSpaceInfo")
    @ApiOperation(value = "更新VIP车位信息", notes = "111")
    public CommonResult<Object> updateVipParingSpaceInfo(@RequestBody VipParking vipParking) {
        try {
            if (!NullUtils.isEmpty(vipParking.getUserid())) {
                vipParkingService.updateById(vipParking);

            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS), vipParking);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }
}
