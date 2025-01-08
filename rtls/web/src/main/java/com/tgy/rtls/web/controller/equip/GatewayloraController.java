package com.tgy.rtls.web.controller.equip;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.Gateway_lora;
import com.tgy.rtls.data.entity.eventserver.DeviceDeleteEvent;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.mapper.equip.GatewayMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.web.aspect.MyPermission;
import com.tgy.rtls.data.tool.IpUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.equip
 * @date 2020/12/23
 * uwb网关管理
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/gateway_lora")
/**
 * 网关
 */
public class GatewayloraController {
    @Autowired
    private GatewayMapper gatewayMapper;
    @Autowired
    LocalUtil localUtil;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private RedisService redisService;
    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MyPermission
    @RequestMapping(value = "/getGatewaySel")
    @ApiOperation(value = "uwb网关查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "networkstate",value = "连接状态",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "num",value = "网关名",required = false,dataType = "string"),
            @ApiImplicitParam(paramType = "query",name = "relevance",value = "是否关联地图",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getGatewaySel(Integer map, Integer networkstate,String num,Integer relevance, String name,
                                          @RequestParam(value = "desc", defaultValue = "addTime desc")String desc,
                                          @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,String floorName,
                                          @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize,String maps){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            String[] mapids = null;
            if(!NullUtils.isEmpty(maps)){
                mapids = maps.split(",");
            }
            //按条件查询
            //pageSize<0时查询所有
            if (pageSize<0){
                List<Gateway_lora> gateway_uwbs=gatewayMapper.findByAllGatewayLora2(null,map,networkstate,num,relevance,localUtil.getLocale(),desc,name,floorName,mapids);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),gateway_uwbs);
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<Gateway_lora> gateway_uwbs=gatewayMapper.findByAllGatewayLora2(null,map,networkstate,num,relevance,localUtil.getLocale(),desc,name,floorName,mapids);
            PageInfo<Gateway_lora> pageInfo=new PageInfo<>(gateway_uwbs);
            Map<String,Object> result=new HashMap<>();
            result.put("list",pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            //生成操作日志-->查询分站数据
            operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.QUERY_GATEWAY));
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"gy:see","gy:edit"},logical = Logical.OR)
    @RequestMapping(value = "/getGatewayId/{id}")
    @ApiOperation(value = "网关详情接口",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "id",value = "网关id",required = true,dataType = "int")
    public CommonResult<Gateway_lora> getGatewayId(@PathVariable("id")Integer id){
        try {
           List< Gateway_lora> res=  gatewayMapper.findGateway_loraByIdAndName(id,null,null);
            Gateway_lora data=null;
            if(res!=null&& res.size()>0)
                data =  res.get(0);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),data);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions({"gy:del"})
    @RequestMapping(value = "/delGateway/{ids}")
    @ApiOperation(value = "uwb网关删除接口",notes = "uwb网关id集")
    @ApiImplicitParam(paramType = "path",name = "ids",value = "uwb网关id集",required = true,dataType = "String")
    public CommonResult delSub(@PathVariable("ids")String ids, HttpServletRequest request){
        try {
            String uid="12";
            LocalDateTime now = LocalDateTime.now();
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            if (gatewayMapper.delGateway_lora(ids.split(","))>0){
                eventPublisher.publishEvent(new DeviceDeleteEvent(this, ids, 2));
                operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.DELETE_GATEWAY));
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.GATEWAY_INFO)), now);
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

}
