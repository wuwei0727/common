package com.tgy.rtls.web.api;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.Basestation;
import com.tgy.rtls.data.entity.user.Instance;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.BaseService;
import com.tgy.rtls.data.service.map.BsConfigService;
import com.tgy.rtls.data.service.user.InstanceService;
import com.tgy.rtls.data.service.user.impl.MemberService;
import com.tgy.rtls.web.jwt.TokenUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.equip
 * @date 2021/1/5
 */
@RestController
@RequestMapping(value = "/baseApi")
@ApiModel("微基站Api")
@CrossOrigin
public class BaseApi {
    @Autowired
    private BaseService baseService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private BsConfigService bsConfigService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private InstanceService instanceService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @RequestMapping(value = "/getBaseSel")
    @ApiOperation(value = "uwb微基站查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "networkstate",value = "网络状态",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "num",value = "分站ID",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "relevance",value = "是否关联地图",required = false,dataType = "int")
    })
    public CommonResult<Object> getBaseSel(Integer map, Integer networkstate,String num,Integer relevance,String token
                                     ){
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());
            List<Basestation> basestations=baseService.findByAll(num,networkstate,null,map,relevance,instances.get(0).getId());
            Map<String,Object> result=new HashMap<>();
            result.put("list",basestations);

            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getBaseId/{id}")
    @ApiOperation(value = "微基站详情接口",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "id",value = "微基站id",required = true,dataType = "int")
    public CommonResult<Basestation> getBaseId(@PathVariable("id")Integer id,String token){
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());
            Basestation basestation=baseService.findById(id);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),basestation);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/addBase")
    @ApiOperation(value = "添加微基站",notes = "网关信息")
    public CommonResult<Object> addBase(Basestation basestation,String token){
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());
            //ip重复判断
            Basestation basestation1=baseService.findByNum(basestation.getNum());
            if (!NullUtils.isEmpty(basestation1)){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.MICROBS_EXIST));
            }
            basestation.setInstanceid(instances.get(0).getId());
            if (baseService.addBasestation(basestation)){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS),basestation.getId());
            }

        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequestMapping(value = "/updateBase")
    @ApiOperation(value = "修改微基站",notes = "基站信息")
    public CommonResult<Object> updateBase(Basestation basestation,String token){
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());
            //ip重复判断
            if(basestation!=null) {
                Basestation basestation1 = baseService.findByNum(basestation.getNum());
                Basestation basestation2 = baseService.findById(basestation.getId());
                if (!basestation2.getNum().equals(basestation.getNum()) && !NullUtils.isEmpty(basestation1)) {
                    return new CommonResult<>(400, LocalUtil.get(KafukaTopics.MICROBS_EXIST));
                }
            }
            basestation.setUpdateTime(new Date());
            if (baseService.updateBasestation(basestation)){
                basestation=baseService.findById(basestation.getId());
                kafkaTemplate.send( KafukaTopics.UPDATE_DW1001BS,null,basestation.toString());
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS),basestation.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequestMapping(value = "/delBase/{ids}")
    @ApiOperation(value = "微基站删除接口",notes = "微基站id集")
    @ApiImplicitParam(paramType = "path",name = "ids",value = "uwb网关id集",required = true,dataType = "String")
    public CommonResult delSub(@PathVariable("ids")String ids,String token){
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());
            if (baseService.delBasestation(ids)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

}
