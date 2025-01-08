package com.tgy.rtls.data.service.user.impl;

import com.tgy.rtls.data.entity.user.Instance;
import com.tgy.rtls.data.mapper.checkingin.StatementMapper;
import com.tgy.rtls.data.mapper.user.InstanceMapper;
import com.tgy.rtls.data.service.checkingin.ClassgroupService;
import com.tgy.rtls.data.service.checkingin.WorkorderService;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.equip.BaseService;
import com.tgy.rtls.data.service.equip.GatewayService;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.equip.TagService;
import com.tgy.rtls.data.service.map.Map2dService;
import com.tgy.rtls.data.service.user.InstanceService;
import com.tgy.rtls.data.service.user.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.user.impl
 * @date 2020/11/12
 */
@Service
@Transactional
public class InstanceServiceImpl implements InstanceService {
    @Autowired(required = false)
    private InstanceMapper instanceMapper;
    @Autowired(required = false)
    private StatementMapper statementMapper;
    @Autowired
    private Map2dService map2dService;
    @Autowired
    private ClassgroupService classgroupService;
    @Autowired
    private PersonService personService;
    @Autowired
    private SubService subService;
    @Autowired
    private GatewayService gatewayService;
    @Autowired
    private BaseService baseService;
    @Autowired
    private TagService tagService;
    @Autowired
    private WorkorderService workorderService;
    @Autowired
    private OperationlogService operationlogService;
    @Override
    public List<Instance> findByAll(Integer userid, String name) {
        return instanceMapper.findByAll(userid,name);
    }

    @Override
    public String findByNameId(Integer id) {
        return instanceMapper.findByNameId(id);
    }

    @Override
    public List<Instance> findByCid(Integer cid) {
        return instanceMapper.findByCid(cid);
    }

    @Override
    public List<Instance> findByUid(Integer uid) {
        return instanceMapper.findByUid(uid);
    }

    @Override
    @Cacheable(value = "instanceid",key = "#id")
    public Instance findById(Integer id) {
        return instanceMapper.findById(id);
    }

    @Override
    public Instance findByNum(String num) {
        return instanceMapper.findByNum(num);
    }

    @Override
    public Instance findByCode2(String code2) {
        return instanceMapper.findByCode2(code2);
    }

    @Override
    public boolean addInstance(Instance instance) {
        //新增实例后 添加实例的考勤规则
        if (instanceMapper.addInstance(instance)>0){
            //考勤规则
            statementMapper.addRule(instance.getId());
            //事件日志出入井
            instanceMapper.addEventlogType(instance.getId());
            return true;
        }
        return false;
    }

    @Override
    @CacheEvict(value = "instanceid",key = "#instance.id")
    public boolean updateInstance(Instance instance) {
        return instanceMapper.updateInstance(instance)>0;
    }

    @Override
   @CacheEvict(value = "instanceid",key = "#id")
    public boolean delInstance(Integer id) {
        //删除实例先将与实例相关的信息删除

        //删除操作日志
        operationlogService.deleteOperationlog(id);
        //1.考勤规则
        statementMapper.delRule(id);
        //2.地图-->报警记录warnrecord warnrule-->bsconfig
        map2dService.delMap2dInstanceid(id);
        //3.人员
        String personids=instanceMapper.delPerson(id);
        personService.delPerson(personids);
        //4.区域类型 area_type
        instanceMapper.delAreaType(id);
        //5.标签 tag
        instanceMapper.delTag(id);
        //6.班组 classgroup
        classgroupService.delClassgroupInstanceid(id);
        //7.部门 department 职务 job 等级 level 工种worktype
        instanceMapper.delDepartment(id);
        instanceMapper.delJob(id);
        instanceMapper.delLevel(id);
        instanceMapper.delWorktype(id);
        //8.排班 scheduling
        instanceMapper.delScheduling(id);
        //9.文字和音频 textrecord voicerecord
        instanceMapper.delTextrecord(id);
        instanceMapper.delVoicerecord(id);
        //10.事件日志类型
        instanceMapper.delEventlogType(id);
        //11.删除基站
        gatewayService.delGatewayByInstance(id);
        baseService.delBasestationByInstance(id);
        subService.delSubInstance(id);
        //删除标签
        tagService.delTagInstance(id);
        //删除班次
        workorderService.delWorkorderByInstance(id);





        return instanceMapper.delInstance(id)>0;
    }
}
