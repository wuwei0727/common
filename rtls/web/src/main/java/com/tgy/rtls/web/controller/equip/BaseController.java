package com.tgy.rtls.web.controller.equip;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.Basestation;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.BaseService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping(value = "/base")
@ApiModel("微基站管理")
@CrossOrigin
public class BaseController {
    @Autowired
    private BaseService baseService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @RequestMapping(value = "/getBaseSel")
    @ApiOperation(value = "uwb微基站查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "networkstate",value = "网络状态",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "num",value = "分站ID",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "relevance",value = "是否关联地图",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getBaseSel(Integer map, Integer networkstate,String num,Integer relevance,
                                            @RequestParam(value = "desc", defaultValue = "addTime desc")String desc,
                                              @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                              @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            //按条件查询
            //pageSize<0时查询所有
            if (pageSize<0){
                List<Basestation> basestations=baseService.findByAll(num,networkstate,desc,map,relevance,instanceid);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),basestations);
            }
            /*
             * 分页 total-->总数量
             * */
            int total=baseService.findByAll(num,networkstate,desc,map,relevance,instanceid).size();
            if (pageIndex > total / pageSize) {
                if (total % pageSize == 0) {
                    pageIndex = total / pageSize;
                } else {
                    pageIndex = total / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<Basestation> basestations=baseService.findByAll(num,networkstate,desc,map,relevance,instanceid);
            PageInfo<Basestation> pageInfo=new PageInfo<>(basestations);
            Map<String,Object> result=new HashMap<>();
            result.put("list",pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            //生成操作日志-->查询分站数据
            operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.QUERY_MICROBSINFO));
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getBaseId/{id}")
    @ApiOperation(value = "微基站详情接口",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "id",value = "微基站id",required = true,dataType = "int")
    public CommonResult<Basestation> getBaseId(@PathVariable("id")Integer id){
        try {
            Basestation basestation=baseService.findById(id);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),basestation);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/addBase")
    @ApiOperation(value = "添加微基站",notes = "网关信息")
    public CommonResult<Object> addBase(Basestation basestation){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            //ip重复判断
            Basestation basestation1=baseService.findByNum(basestation.getNum());
            if (!NullUtils.isEmpty(basestation1)){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.MICROBS_EXIST));
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            basestation.setInstanceid(instanceid);
            if (baseService.addBasestation(basestation)){
                //生成操作日志-->添加分站数据
                operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.ADD_MICROBS)+basestation.getNum());
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
    public CommonResult<Object> updateBase(Basestation basestation){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            //ip重复判断
            if(basestation.getNum()!=null) {
                Basestation basestation1 = baseService.findByNum(basestation.getNum());
                Basestation basestation2 = baseService.findById(basestation.getId());
                if (!basestation2.getNum().equals(basestation.getNum()) && !NullUtils.isEmpty(basestation1)) {
                    return new CommonResult<>(400, LocalUtil.get(KafukaTopics.MICROBS_EXIST));
                }
            }
            Basestation cc = baseService.findById(basestation.getId());
            basestation.setUpdateTime(new Date());
            basestation.setNum(cc.getNum());
            basestation.setInitiator(cc.getInitiator());
            if (baseService.updateBasestation(basestation)){
                basestation=baseService.findById(basestation.getId());
                kafkaTemplate.send( KafukaTopics.UPDATE_DW1001BS,null,basestation.toString());
                //生成操作日志-->添加分站数据
                operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.UPDATE_MICROBS)+basestation.getNum());
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
    public CommonResult delSub(@PathVariable("ids")String ids){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            if (baseService.delBasestation(ids)){
                operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.DELETE_MICROBS));
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

}
