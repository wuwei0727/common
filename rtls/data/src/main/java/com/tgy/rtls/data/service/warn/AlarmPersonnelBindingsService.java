package com.tgy.rtls.data.service.warn;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.warn.AlarmPersonnelBindings;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.warn.impl
*@Author: wuwei
*@CreateTime: 2024-10-29 16:01
*@Description: TODO
*@Version: 1.0
*/
public interface AlarmPersonnelBindingsService extends IService<AlarmPersonnelBindings>{


    List<AlarmPersonnelBindings> getAllOrFilteredAlarmPersonnelBindings(String map, String desc, String[] mapids);

    AlarmPersonnelBindings getAlarmPersonnelBindingsById(int id);

    AlarmPersonnelBindings getIdByTypeId(Integer id);


    boolean checkStaffExists(String map, String staffId);

    boolean checkStaffExistsExclude(String map, String staffId, Long excludeId);

    void saveBindingWithMappings(AlarmPersonnelBindings binding, List<String> typeIds, List<String> levelIds);

    boolean updateBindingWithMappings(AlarmPersonnelBindings binding, List<String> alarmTypeIds, List<String> levelIds);

    boolean removeBindingsWithMappings(List<Integer> integers);

    List<AlarmPersonnelBindings> getBindingsByCondition(Integer map, Integer type, Integer priority);
}
