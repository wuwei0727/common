package com.tgy.rtls.data.service.warn;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.warn.AlarmPersonnelTypeMappings;
import com.tgy.rtls.data.mapper.warn.AlarmPersonnelTypeMappingsMapper;
import com.tgy.rtls.data.service.warn.impl.AlarmPersonnelTypeMappingsService;
/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.warn
*@Author: wuwei
*@CreateTime: 2024-11-06 11:11
*@Description: TODO
*@Version: 1.0
*/
@Service
public class AlarmPersonnelTypeMappingsServiceImpl extends ServiceImpl<AlarmPersonnelTypeMappingsMapper, AlarmPersonnelTypeMappings> implements AlarmPersonnelTypeMappingsService{

}
