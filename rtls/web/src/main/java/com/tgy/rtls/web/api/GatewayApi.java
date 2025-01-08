package com.tgy.rtls.web.api;

import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.Gateway_uwb;
import com.tgy.rtls.data.entity.user.Instance;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.GatewayService;
import com.tgy.rtls.data.service.user.InstanceService;
import com.tgy.rtls.data.service.user.impl.MemberService;
import com.tgy.rtls.web.jwt.TokenUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping(value = "/gatewayApi")
@ApiModel("网关Api")
public class GatewayApi {
    @Autowired
    private GatewayService gatewayService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private RedisService redisService;
    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private MemberService memberService;
    @Autowired
    private InstanceService instanceService;


    @RequestMapping(value = "/getGatewaySel")
    @ApiOperation(value = "uwb网关查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "connect",value = "连接状态",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "name",value = "网关名",required = false,dataType = "string"),
            @ApiImplicitParam(paramType = "query",name = "relevance",value = "是否关联地图",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getGatewaySel(Integer map, Integer connect,String name,Integer relevance,String token){
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());

            List<Gateway_uwb> gateway_uwbs=gatewayService.findByAll(instances.get(0).getId(),map,connect,name,relevance);
            PageInfo<Gateway_uwb> pageInfo=new PageInfo<>(gateway_uwbs);
            Map<String,Object> result=new HashMap<>();
            result.put("list",pageInfo.getList());
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getGatewayId/{id}")
    @ApiOperation(value = "网关详情接口",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "id",value = "网关id",required = true,dataType = "int")
    public CommonResult<Gateway_uwb> getGatewayId(@PathVariable("id")Integer id,String token){
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());
            Gateway_uwb gateway_uwb=gatewayService.findById(id);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),gateway_uwb);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/addGateway")
    @ApiOperation(value = "添加uwb网关",notes = "网关信息")
    public CommonResult<Object> addGateway(Gateway_uwb gateway_uwb,String token){
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
            Gateway_uwb gateway_uwb1=gatewayService.findByIp(gateway_uwb.getIp());
            if (!NullUtils.isEmpty(gateway_uwb1)){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.GATEWAYIP_CONFIX));
            }
            gateway_uwb.setInstanceid(instances.get(0).getId());
            if (gatewayService.addGateway(gateway_uwb)){
                  return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS),gateway_uwb.getId());
            }

        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequestMapping(value = "/updateGateway")
    @ApiOperation(value = "修改uwb网关",notes = "网关信息")
    public CommonResult<Object> updateGateway(Gateway_uwb gateway_uwb,String token){
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
            Gateway_uwb gateway_uwb1=gatewayService.findByIp(gateway_uwb.getIp());
            Gateway_uwb gateway_uwb2=gatewayService.findById(gateway_uwb.getId());
            boolean updateIp=false;
            if(!gateway_uwb2.getIp().equals(gateway_uwb.getIp())){
                JSONObject object=new JSONObject();
                object.put("id",gateway_uwb.getId());//网关id
                object.put("type",0);//1连接 0断开连接
                kafkaTemplate.send(KafukaTopics.CONNECT_GATEWAY,object.toString());
                updateIp=true;


            }
            if (!gateway_uwb2.getIp().equals(gateway_uwb.getIp())&&!NullUtils.isEmpty(gateway_uwb1)){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.GATEWAYIP_CONFIX));
            }
            if (gatewayService.updateGateway(gateway_uwb)){
                //生成操作日志-->添加分站数据
                      if(updateIp){
                    Thread.sleep(1000);
                    JSONObject object=new JSONObject();
                    object.put("id",gateway_uwb.getId());//网关id
                    object.put("type",1);//1连接 0断开连接
                    kafkaTemplate.send(KafukaTopics.CONNECT_GATEWAY,object.toString());
                }
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.SAVE_SUCCESS),gateway_uwb.getId());
            }

        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.SAVE_FAIL));
    }

    @RequestMapping(value = "/delGateway/{ids}")
    @ApiOperation(value = "uwb网关删除接口",notes = "uwb网关id集")
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
            if (gatewayService.delGateway(ids)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

    /*
     * uwb网关连接
     * */
    @RequestMapping("/connectGateway/{id}")
    @ApiOperation(value = "uwb网关连接接口",notes = "uwb网关id")
    @ApiImplicitParam(paramType = "path",name = "id",value = "uwb网关id",required = true,dataType = "int")
    public CommonResult connectGateway(@PathVariable("id")Integer id,String token) throws InterruptedException {
        try {

            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());

            Gateway_uwb gateway_uwb = gatewayService.findById(id);
            if (NullUtils.isEmpty(gateway_uwb)) {
               // return ;
              return (new CommonResult<>(400,LocalUtil.get(KafukaTopics.SELECT_GATEWAY)));

            }
            redisService.set("gateway"+id,"-1");
            JSONObject object=new JSONObject();
            object.put("id",id);//网关id
            object.put("type",1);//1连接 0断开连接
            kafkaTemplate.send(KafukaTopics.CONNECT_GATEWAY,object.toString());
        }catch (Exception e){
            e.printStackTrace();
           // return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
            return (new CommonResult<>(400,LocalUtil.get(KafukaTopics.SYSTEM_BUSY)));
        }
                Thread.sleep(4000);
                  Gateway_uwb gateway = gatewayService.findById(id);
                  if(gateway.getConnect()==0)
                 return (new CommonResult<>(400,LocalUtil.get(KafukaTopics.FAIL)));
                  else
                      return (new CommonResult<>(200,LocalUtil.get(KafukaTopics.SUCCESS)));


    }


    @RequestMapping("/disconnectGateway/{id}")
    @ApiOperation(value = "uwb网关连接接口",notes = "uwb网关id")
    @ApiImplicitParam(paramType = "path",name = "id",value = "uwb网关id",required = true,dataType = "int")
    public CommonResult disconnectGateway(@PathVariable("id")Integer id,String token) throws InterruptedException {

        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());
            JSONObject object=new JSONObject();
            object.put("id",id);//网关id
            object.put("type",0);//1连接 0断开连接
            kafkaTemplate.send(KafukaTopics.CONNECT_GATEWAY,object.toString());
        }catch (Exception e){
            e.printStackTrace();
            return (new CommonResult<>(400,LocalUtil.get(KafukaTopics.SYSTEM_BUSY)));
        }
                 Thread.sleep(4000);
                Gateway_uwb gateway = gatewayService.findById(id);
                if(gateway.getConnect()==1)
                    return (new CommonResult<>(400,LocalUtil.get(KafukaTopics.FAIL)));
                else
                    return (new CommonResult<>(200,LocalUtil.get(KafukaTopics.SUCCESS)));
    }


}
