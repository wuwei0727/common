package com.tgy.rtls.web.controller.xianjiliang;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.xianjiliang.TypeCount;
import com.tgy.rtls.data.entity.xianjiliang.TypeTime;
import com.tgy.rtls.data.mapper.checkingin.ClassgroupMapper;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.user.PersonService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.user
 * @date 2020/10/14
 * 人员管理类
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/sample")
public class JiliangController {
    @Autowired
    private PersonService personService;
    @Autowired
    private RedisService redisService;
    @Autowired(required = false)
    private ClassgroupMapper classgroupMapper;

    @RequestMapping(value = "/getTypeTime")
    public CommonResult<Object> getTypeTime(Integer flag){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            List<TypeTime> res = personService.findTypeTimeByTypeid(instanceid,flag);
            return new CommonResult<Object>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),res);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getTypeCount")
    public CommonResult<Object> findTypeCountByTypeid(Integer flag){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            List<TypeCount> res = personService.findTypeCountByTypeid(instanceid,flag);
            return new CommonResult<Object>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),res);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

}
