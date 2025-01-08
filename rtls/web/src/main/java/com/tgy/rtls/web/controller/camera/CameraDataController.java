package com.tgy.rtls.web.controller.camera;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.vip.BarrierGate;
import com.tgy.rtls.data.entity.vip.VipArea;
import com.tgy.rtls.data.service.vip.BarrierGateService;
import com.tgy.rtls.data.service.vip.VipAreaService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.camera
 * @Author: wuwei
 * @CreateTime: 2023-03-31 14:17
 * @Description: TODO
 * @Version: 1.0
 */
@RequestMapping(value = "/ajb")
@CrossOrigin
@RestController
@Slf4j
public class CameraDataController {
    @Autowired
    private VipAreaService vipAreaService;
    @Autowired
    private BarrierGateService barrierGateService;
    @Autowired
    private LocalUtil localUtil;
    private static final String TEXT = "欢迎进入VIP区域";

    @RequestMapping(value = "/gateLicense")
    @ApiOperation(value = "车牌信息上报接口", notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", name = "license", value = "车牌号", dataType = "string"), @ApiImplicitParam(paramType = "query", name = "num", value = "道闸编号", dataType = "string"),})
    public CommonResult<Object> gateLicense(String license, String num) {
        try {
            log.info("调用gateLicense方法-------------->" + num);
            CommonResult<Object> result = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));

            if (NullUtils.isEmpty(license) && NullUtils.isEmpty(num)) {
                return new CommonResult<>(400, LocalUtil.get("请求参数不能为空"));
            }
            BarrierGate barrierGateNum = barrierGateService.getBarrierGateInfoInfoByNum(num);
            VipArea vipArea = vipAreaService.gateLicense(license, String.valueOf(barrierGateNum.getId()));

            VipArea vipAreaVo = new VipArea();
            // 获取当前时间
            LocalDateTime currentTime = LocalDateTime.now();
            if (!NullUtils.isEmpty(vipArea)) {
                // 获取开始时间和结束时间
                LocalDateTime startTime = vipArea.getStartTime();
                LocalDateTime endTime = vipArea.getEndTime();
                if (currentTime.isBefore(startTime)) {
                    vipAreaVo.setLicense(vipArea.getLicense());
                    vipAreaVo.setState((byte) 0);
                    vipAreaVo.setText("还没有到预约时间！！！");
                } else if (currentTime.isAfter(endTime)) {
                    vipAreaVo.setLicense(vipArea.getLicense());
                    vipAreaVo.setState((byte) 0);
                    vipAreaVo.setText("预约时间已超过！！！");
                } else {
                    vipAreaVo.setLicense(vipArea.getLicense());
                    vipAreaVo.setMapName(vipArea.getMapName());
                    vipAreaVo.setState(vipArea.getState());
                    vipAreaVo.setBarrierGateNum(vipArea.getBarrierGateNum());
                    vipAreaVo.setText(vipArea.getLicense() + TEXT);
                }
            } else {
                vipAreaVo.setState((byte) 0);
                vipAreaVo.setText("未知车牌");
            }
            result.setData(vipAreaVo);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/gateCmd")
    @ApiOperation(value = "道闸控制和屏幕显示指令", notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", name = "license", value = "车牌号", dataType = "string"), @ApiImplicitParam(paramType = "query", name = "num", value = "道闸编号", dataType = "string"),})
    public CommonResult<Object> gateCmd(String num) {
        try {
            log.info("调用gateCmd方法-------------->" + num);
            CommonResult<Object> result = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            if (NullUtils.isEmpty(num)) {
                return new CommonResult<>(400, LocalUtil.get("请求参数不能为空"));
            }

            BarrierGate barrierGateNum = barrierGateService.getBarrierGateInfoInfoByNum(num);
            VipArea vipArea = vipAreaService.gateCmdWhetherOvertime((!NullUtils.isEmpty(barrierGateNum)) ? String.valueOf(barrierGateNum.getId()) : null, (!NullUtils.isEmpty(barrierGateNum)) ? barrierGateNum.getMap() : null);

            VipArea vipAreaVo = new VipArea();
            if (!NullUtils.isEmpty(vipArea.getLicense())) {
                vipAreaVo.setLicense(vipArea.getLicense());
                vipAreaVo.setMapName(vipArea.getMapName());
                vipAreaVo.setState((byte) 1);
                vipAreaVo.setLicense(vipArea.getLicense());
                vipAreaVo.setBarrierGateNum(vipArea.getBarrierGateNum());
                vipAreaVo.setText(TEXT);
            } else {
                vipAreaVo.setState((byte) 0);
                vipAreaVo.setText("无效车牌");
            }
            result.setData(vipAreaVo);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
}
