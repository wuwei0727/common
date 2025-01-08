package com.tgy.rtls.data.service.video.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.equip.DeviceAlarms;
import com.tgy.rtls.data.entity.equip.DeviceAlarmsVo;
import java.util.List;
import java.util.Map;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.video.impl
*@Author: wuwei
*@CreateTime: 2023-12-22 11:25
*@Description: TODO
*@Version: 1.0
*/
public interface DeviceAlarmsService extends IService<DeviceAlarms> {

    List<DeviceAlarmsVo> getDeviceAlarmsData(Integer id,String num,String placeName,Integer state, Integer map, Integer priority,Integer deviceType, Integer alarmType, String desc,Integer pageIndex,Integer pageSize,  String[] mapids);

    boolean updateByIds(String[] ids);
    boolean updateByIds1(String[] ids,String desc);

    List<DeviceAlarmsVo> getDeviceAlarmsTypeConfig(Integer id, Integer deviceName, Integer alarmType,String desc);

    void addDeviceAlarmsTypeConfig(DeviceAlarmsVo deviceAlarms);

    void editDeviceAlarmsTypeConfig(DeviceAlarmsVo deviceAlarms);

    void delDeviceAlarmsConfig(String[] ids);

    Map<Integer, DeviceAlarms> getExistingAlarmsForDevices(int equipmentType, Integer num);

    long countWithConditions(Integer id, String num, String placeName, Integer state, Integer map, Integer priority, Integer deviceType, Integer alarmType);
}
