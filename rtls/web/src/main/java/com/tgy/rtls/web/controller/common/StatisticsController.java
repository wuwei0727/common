package com.tgy.rtls.web.controller.common;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.service.common.StatisticsService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.common
 * @date 2021/1/18
 * 统计管理
 */
@RequestMapping(value = "/statistics")
@CrossOrigin
@RestController
public class StatisticsController {
    @Autowired
    private StatisticsService statisticsService;

    @RequestMapping(value = "/getManFlowSel")
    @ApiOperation(value = "人流量查询接口",notes = "无")
    public CommonResult<Object> getManFlowSel(int map,int day){
        try {
            //List<Object> manFlowMap=statisticsService.getManFlowSel(map,day);
            List<Object> manFlowMap=statisticsService.getManFlowSelFromIncoalRecord(map,day);

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),manFlowMap);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,e.getMessage());
        }
    }

    @RequestMapping(value = "/getWarnFlowSel")
    @ApiOperation(value = "报警数量统计接口",notes = "无")
    public CommonResult<Object> getWarnFlowSel(int map,int day,int number,String startTime){
        try {
            List<Object> warnFlow=statisticsService.getWarnFlowSel(map,day,startTime,number);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),warnFlow);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,e.getMessage());
        }
    }
}
