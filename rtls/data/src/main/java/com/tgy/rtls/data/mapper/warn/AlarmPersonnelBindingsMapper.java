package com.tgy.rtls.data.mapper.warn;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.warn.AlarmPersonnelBindings;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.mapper.warn
*@Author: wuwei
*@CreateTime: 2024-10-29 16:01
*@Description: TODO
*@Version: 1.0
*/
public interface AlarmPersonnelBindingsMapper extends BaseMapper<AlarmPersonnelBindings> {
    List<AlarmPersonnelBindings> getAllOrFilteredAlarmPersonnelBindings(@Param("map") String map, @Param("desc") String desc, @Param("mapids") String[] mapids);

    AlarmPersonnelBindings getAlarmPersonnelBindingsById(int id);

    List<AlarmPersonnelBindings> getBindingsByCondition(@Param("map") Integer map, @Param("type") Integer type, @Param("priority") Integer priority);

    @Select("select id configId from device_alarms_config where device_alarm_type_id=#{id}")
    AlarmPersonnelBindings getIdByTypeId(Integer id);
}