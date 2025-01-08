package com.tgy.rtls.data.service.warn.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.warn.AlarmPersonnelBindings;
import com.tgy.rtls.data.entity.warn.AlarmPersonnelLevelMappings;
import com.tgy.rtls.data.entity.warn.AlarmPersonnelTypeMappings;
import com.tgy.rtls.data.mapper.warn.AlarmPersonnelBindingsMapper;
import com.tgy.rtls.data.service.warn.AlarmPersonnelBindingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.warn
*@Author: wuwei
*@CreateTime: 2024-10-29 16:01
*@Description: TODO
*@Version: 1.0
*/
@Service
public class AlarmPersonnelBindingsServiceImpl extends ServiceImpl<AlarmPersonnelBindingsMapper, AlarmPersonnelBindings> implements AlarmPersonnelBindingsService {

    @Autowired
    private AlarmPersonnelTypeMappingsService typeMappingsService;

    @Autowired
    private AlarmPersonnelLevelMappingsService levelMappingsService;
    @Override

    public List<AlarmPersonnelBindings> getAllOrFilteredAlarmPersonnelBindings(String map, String desc, String[] mapids) {
        return baseMapper.getAllOrFilteredAlarmPersonnelBindings(map,desc,mapids);
    }

    @Override
    public AlarmPersonnelBindings getAlarmPersonnelBindingsById(int id) {
        return baseMapper.getAlarmPersonnelBindingsById(id);
    }

    @Override
    public AlarmPersonnelBindings getIdByTypeId(Integer id) {
        return baseMapper.getIdByTypeId(id);
    }

    @Override
    public boolean checkStaffExists(String map, String staffId) {
        return baseMapper.exists(new QueryWrapper<AlarmPersonnelBindings>()
                .eq("map", map)
                .eq("maintenance_staff_id", staffId));
    }

    @Override
    public boolean checkStaffExistsExclude(String map, String staffId, Long excludeId) {
        return baseMapper.exists(new QueryWrapper<AlarmPersonnelBindings>()
                .eq("map", map)
                .eq("maintenance_staff_id", staffId)
                .ne("id", excludeId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBindingWithMappings(AlarmPersonnelBindings binding, List<String> typeIds, List<String> levelIds) {
                this.save(binding);
        //保存告警类型映射
        List<AlarmPersonnelTypeMappings> typeMappings = typeIds.stream()
                .map(typeId -> {
                    AlarmPersonnelTypeMappings mapping = new AlarmPersonnelTypeMappings();
                    mapping.setBindingId(String.valueOf(binding.getId()));
                    mapping.setAlarmTypeId(typeId);
                    return mapping;
                })
                .collect(Collectors.toList());
        typeMappingsService.saveBatch(typeMappings);

        //保存级别映射
        List<AlarmPersonnelLevelMappings> levelMappings = levelIds.stream()
                .map(levelId -> {
                    AlarmPersonnelLevelMappings mapping = new AlarmPersonnelLevelMappings();
                    mapping.setBindingId(binding.getId());
                    mapping.setLevelId(Integer.parseInt(levelId));
                    return mapping;
                })
                .collect(Collectors.toList());
        levelMappingsService.saveBatch(levelMappings);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateBindingWithMappings(AlarmPersonnelBindings binding,
                                             List<String> typeIds, List<String> levelIds) {
        // 1. 更新主表
        boolean result = this.updateById(binding);
        if (!result) {
            return false;
        }

        // 2. 删除旧的映射关系
        typeMappingsService.remove(new QueryWrapper<AlarmPersonnelTypeMappings>()
                .eq("binding_id", binding.getId()));
        levelMappingsService.remove(new QueryWrapper<AlarmPersonnelLevelMappings>()
                .eq("binding_id", binding.getId()));

        // 3. 保存新的映射关系
        List<AlarmPersonnelTypeMappings> typeMappings = typeIds.stream()
                .map(typeId -> {
                    AlarmPersonnelTypeMappings mapping = new AlarmPersonnelTypeMappings();
                    mapping.setBindingId(String.valueOf(binding.getId()));
                    mapping.setAlarmTypeId(typeId);
                    return mapping;
                })
                .collect(Collectors.toList());
        typeMappingsService.saveBatch(typeMappings);

        List<AlarmPersonnelLevelMappings> levelMappings = levelIds.stream()
                .map(levelId -> {
                    AlarmPersonnelLevelMappings mapping = new AlarmPersonnelLevelMappings();
                    mapping.setBindingId(binding.getId());
                    mapping.setLevelId(Integer.parseInt(levelId));
                    return mapping;
                })
                .collect(Collectors.toList());
        levelMappingsService.saveBatch(levelMappings);

        return true;
    }

    @Override
    public boolean removeBindingsWithMappings(List<Integer> ids) {
        // 1. 删除告警类型映射
        typeMappingsService.remove(new QueryWrapper<AlarmPersonnelTypeMappings>().in("binding_id", ids));

        // 2. 删除级别映射
        levelMappingsService.remove(new QueryWrapper<AlarmPersonnelLevelMappings>().in("binding_id", ids));

        // 3. 删除主表数据
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    public List<AlarmPersonnelBindings> getBindingsByCondition(Integer map, Integer type, Integer priority) {
        return baseMapper.getBindingsByCondition(map, type, priority);
    }
}
