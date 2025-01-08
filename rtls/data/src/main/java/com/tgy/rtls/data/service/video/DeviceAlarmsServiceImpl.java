package com.tgy.rtls.data.service.video;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.equip.DeviceAlarms;
import com.tgy.rtls.data.entity.equip.DeviceAlarmsVo;
import com.tgy.rtls.data.mapper.equip.DeviceAlarmsMapper;
import com.tgy.rtls.data.service.video.impl.DeviceAlarmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.video
*@Author: wuwei
*@CreateTime: 2023-12-22 11:25
*@Description: TODO
*@Version: 1.0
*/
@Service
@Slf4j
public class DeviceAlarmsServiceImpl extends ServiceImpl<DeviceAlarmsMapper,DeviceAlarms> implements DeviceAlarmsService{
    @Autowired
    private DeviceAlarmsMapper deviceAlarmsMapper;

    @Override
    public List<DeviceAlarmsVo> getDeviceAlarmsData(Integer id,String num,String placeName,Integer state, Integer map, Integer priority,Integer deviceType, Integer alarmType, String desc,Integer pageIndex,Integer pageSize, String[] mapids) {
        return deviceAlarmsMapper.getDeviceAlarmsData(id,num,placeName,state, map,priority,deviceType,alarmType,desc,pageIndex,pageSize,mapids);
    }

    @Override
    public boolean updateByIds(String[] ids) {
        return deviceAlarmsMapper.updateByIds(ids);
    }

    @Override
    public boolean updateByIds1(String[] ids,String desc) {
        return deviceAlarmsMapper.updateByIds1(ids,desc);
    }

    @Override
    public List<DeviceAlarmsVo> getDeviceAlarmsTypeConfig(Integer id, Integer deviceName, Integer alarmType,String desc) {
        return deviceAlarmsMapper.getDeviceAlarmsTypeConfig(id,deviceName,alarmType,desc);
    }

    @Override
    public void addDeviceAlarmsTypeConfig(DeviceAlarmsVo deviceAlarms) {
        deviceAlarmsMapper.addDeviceType(deviceAlarms);
        deviceAlarms.setDeviceTypeId(deviceAlarms.getId());
        deviceAlarmsMapper.addDeviceAlarmsType(deviceAlarms);
        deviceAlarms.setAlarmsTypeId(deviceAlarms.getId());
        deviceAlarmsMapper.addDeviceAlarmsConfig(deviceAlarms);
    }

    @Override
    public void editDeviceAlarmsTypeConfig(DeviceAlarmsVo deviceAlarms) {
        deviceAlarmsMapper.editDeviceType(deviceAlarms);
        deviceAlarmsMapper.editDeviceAlarmsType(deviceAlarms);
    }

    @Override
    public void delDeviceAlarmsConfig(String[] ids) {
        deviceAlarmsMapper.delDeviceAlarmsConfig(ids);
    }

    @Override
    public Map<Integer, DeviceAlarms> getExistingAlarmsForDevices(int equipmentType, Integer num) {
        // 查询所有与红外设备相关的未结束告警
        List<DeviceAlarms> existingAlarms = deviceAlarmsMapper.selectList(
                new QueryWrapper<DeviceAlarms>()
                        .eq("equipment_type", equipmentType)//设备类型
                        .eq(!NullUtils.isEmpty(num),"num",num)
                        .ne("state", 1) // 非结束状态的告警
                        .isNull("end_time") // 未设置结束时间的告警
        );

        // 将结果转换为Map，设备ID作为Key，DeviceAlarms作为Value
        return existingAlarms.stream()
                .collect(Collectors.toMap(DeviceAlarms::getDeviceId,Function.identity()));
    }

    @Override
    public long countWithConditions(Integer id, String num, String placeName, Integer state, Integer map, Integer priority, Integer deviceType, Integer alarmType) {
        return deviceAlarmsMapper.countWithConditions(id,num, placeName, state, map, priority, deviceType, alarmType);
    }


}
