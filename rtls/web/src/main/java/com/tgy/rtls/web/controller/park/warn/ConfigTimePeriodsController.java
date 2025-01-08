package com.tgy.rtls.web.controller.park.warn;

import com.tgy.rtls.data.service.warn.impl.ConfigTimePeriodsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
* 报警配置时间段表，用于存储每个配置的多组时段(config_time_periods)表控制层
*
* @author xxxxx
*/
@RestController
@RequestMapping("/config_time_periods")
public class ConfigTimePeriodsController {

    @Resource
    private ConfigTimePeriodsService configTimePeriodsService;

}
