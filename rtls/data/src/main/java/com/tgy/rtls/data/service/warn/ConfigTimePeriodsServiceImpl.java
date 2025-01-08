package com.tgy.rtls.data.service.warn;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.mapper.warn.ConfigTimePeriodsMapper;
import com.tgy.rtls.data.entity.warn.ConfigTimePeriods;
import com.tgy.rtls.data.service.warn.impl.ConfigTimePeriodsService;
/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.warn
*@Author: wuwei
*@CreateTime: 2024-11-05 10:38
*@Description: TODO
*@Version: 1.0
*/
@Service
public class ConfigTimePeriodsServiceImpl extends ServiceImpl<ConfigTimePeriodsMapper, ConfigTimePeriods> implements ConfigTimePeriodsService{

}
