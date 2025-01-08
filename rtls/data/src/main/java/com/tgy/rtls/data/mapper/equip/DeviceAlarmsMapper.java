package com.tgy.rtls.data.mapper.equip;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.equip.DeviceAlarms;
import com.tgy.rtls.data.entity.equip.DeviceAlarmsVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.mapper.equip
*@Author: wuwei
*@CreateTime: 2023-12-22 11:25
*@Description: TODO
*@Version: 1.0
*/
public interface DeviceAlarmsMapper extends BaseMapper<DeviceAlarms> {
    long countWithConditions(@Param("id") Integer id, @Param("num") String num, @Param("placeName") String placeName, @Param("state") Integer state, @Param("map") Integer map, @Param("priority") Integer priority, @Param("deviceType") Integer deviceType, @Param("alarmType") Integer alarmType);

    List<DeviceAlarmsVo> getDeviceAlarmsData(@Param("id") Integer id, @Param("num") String num, @Param("placeName") String placeName, @Param("state") Integer state, @Param("map") Integer map, @Param("priority") Integer priority, @Param("deviceType") Integer deviceType, @Param("alarmType") Integer alarmType, @Param("desc") String desc, @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSize, @Param("mapids") String[] mapids);

    boolean updateByIds(@Param("ids") String[] ids);
    boolean updateByIds1(@Param("ids") String[] ids,@Param("desc") String desc);

    List<DeviceAlarmsVo> getDeviceAlarmsTypeConfig(@Param("id") Integer id,@Param("deviceName") Integer deviceName, @Param("alarmType") Integer alarmType,@Param("desc") String desc);

    void addDeviceType(DeviceAlarmsVo deviceAlarms);

    void addDeviceAlarmsType(DeviceAlarmsVo deviceAlarms);

    void addDeviceAlarmsConfig(DeviceAlarmsVo deviceAlarms);

    void editDeviceType(DeviceAlarmsVo deviceAlarms);
    void editDeviceAlarmsType(DeviceAlarmsVo deviceAlarms);

    void delDeviceAlarmsConfig(@Param("ids") String[] ids);
}