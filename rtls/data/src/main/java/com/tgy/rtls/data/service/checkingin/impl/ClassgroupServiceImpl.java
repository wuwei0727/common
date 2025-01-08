package com.tgy.rtls.data.service.checkingin.impl;

import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.checkingin.Classgroup;
import com.tgy.rtls.data.mapper.checkingin.ClassgroupMapper;
import com.tgy.rtls.data.service.checkingin.ClassgroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.checkingin.impl
 * @date 2020/11/16
 * 班组管理
 */
@Service
@Transactional
public class ClassgroupServiceImpl implements ClassgroupService {
    @Autowired(required = false)
    private ClassgroupMapper classgroupMapper;
    @Autowired
    private LocalUtil localUtil;

    @Override
    public List<Classgroup> findByAllEqual(String name, String instanceid) {
        return classgroupMapper.findByAllEqual(name,instanceid,localUtil.getLocale());
    }

    @Override
    public List<Classgroup> findByAllLike(String name, Integer instanceid) {
        return classgroupMapper.findByAllLike(name,instanceid,localUtil.getLocale());
    }
    @Override
    public boolean addClassgroup(Classgroup classgroup,String personids) {
        //新增班组  在绑定班组人员
        if (classgroupMapper.addClassgroup(classgroup)>0){
            if (!NullUtils.isEmpty(personids)){
                String[] split = personids.split(",");
                for (String s : split) {
                    classgroupMapper.addPersonClassgroup(s, classgroup.getId());
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean updateClassgroup(Classgroup classgroup,String personids) {
        //修改班组  在修改绑定的班组人员
        if (classgroupMapper.updateClassgroup(classgroup)>0){
            if (!NullUtils.isEmpty(personids)) {
                if (personids.equals("-1")){//传-1表示清空
                    classgroupMapper.delPersonClassgroupId(classgroup.getId());
                    return true;
                }
                classgroupMapper.delPersonClassgroupId(classgroup.getId());
                String[] split = personids.split(",");
                for (String s : split) {
                    classgroupMapper.addPersonClassgroup(s, classgroup.getId());
                }
            }else{
                classgroupMapper.delPersonClassgroupId(classgroup.getId());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean delClassgroup(String ids) {
        String[] split=ids.split(",");
        //解除班组和人员的绑定
        classgroupMapper.delPersonClassgroup(split);
        //删除班组
        classgroupMapper.delClassgroup(split);
        return true;
    }

    @Override
    public boolean delClassgroupInstanceid(Integer instanceid) {
        String ids=classgroupMapper.findByClassgroup(instanceid);
        if (!NullUtils.isEmpty(ids)){
            delClassgroup(ids);
        }
        return true;
    }
}
