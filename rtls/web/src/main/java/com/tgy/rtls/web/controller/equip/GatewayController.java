package com.tgy.rtls.web.controller.equip;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.Gateway_uwb;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.GatewayService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping(value = "/gateway")
public class GatewayController {
    @Autowired
    private GatewayService gatewayService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private RedisService redisService;
    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    @RequestMapping(value = "/getGatewaySel")
    @ApiOperation(value = "uwb网关查询接口", notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", name = "map", value = "地图id", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "connect", value = "连接状态", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "name", value = "网关名", required = false, dataType = "string"),
            @ApiImplicitParam(paramType = "query", name = "relevance", value = "是否关联地图", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageIndex", value = "当前页", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageSize", value = "页面大小", required = false, dataType = "int")
    })
    public CommonResult<Object> getGatewaySel(Integer map, Integer connect, String name, Integer relevance,
                                              @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                              @RequestParam(value = "pageSize", defaultValue = "1") Integer pageSize) {
        try {
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            int instanceid = Integer.parseInt(redisService.get("instance" + uid));
            //按条件查询
            //pageSize<0时查询所有
            if (pageSize < 0) {
                List<Gateway_uwb> gatewayUwbs = gatewayService.findByAll(instanceid, map, connect, name, relevance);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), gatewayUwbs);
            }
            /*
             * 分页 total-->总数量
             * */
            int total = gatewayService.findByAll(instanceid, map, connect, name, relevance).size();
            if (pageIndex > total / pageSize) {
                if (total % pageSize == 0) {
                    pageIndex = total / pageSize;
                } else {
                    pageIndex = total / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex, pageSize);
            List<Gateway_uwb> gatewayUwbs = gatewayService.findByAll(instanceid, map, connect, name, relevance);
            PageInfo<Gateway_uwb> pageInfo = new PageInfo<>(gatewayUwbs);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            //生成操作日志-->查询分站数据
            operationlogService.addOperationlog(Integer.valueOf(uid), LocalUtil.get(KafukaTopics.QUERY_GATEWAY));
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), result);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getGatewayId/{id}")
    @ApiOperation(value = "网关详情接口", notes = "无")
    @ApiImplicitParam(paramType = "path", name = "id", value = "网关id", required = true, dataType = "int")
    public CommonResult<Gateway_uwb> getGatewayId(@PathVariable("id") Integer id) {
        try {
            Gateway_uwb gatewayUwbs = gatewayService.findById(id);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), gatewayUwbs);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/addGateway")
    @ApiOperation(value = "添加uwb网关", notes = "网关信息")
    public CommonResult<Object> addGateway(Gateway_uwb gatewayUwbs) {
        try {
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            //ip重复判断
            Gateway_uwb gateway_uwb1 = gatewayService.findByIp(gatewayUwbs.getIp());
            if (!NullUtils.isEmpty(gateway_uwb1)) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.GATEWAYIP_CONFIX));
            }
            int instanceid = Integer.parseInt(redisService.get("instance" + uid));
            gatewayUwbs.setInstanceid(instanceid);
            if (gatewayService.addGateway(gatewayUwbs)) {
                //生成操作日志-->添加分站数据
                operationlogService.addOperationlog(Integer.valueOf(uid), LocalUtil.get(KafukaTopics.ADD_GATEWAY) + gatewayUwbs.getName());
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS), gatewayUwbs.getId());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequestMapping(value = "/updateGateway")
    @ApiOperation(value = "修改uwb网关", notes = "网关信息")
    public CommonResult<Object> updateGateway(Gateway_uwb gatewayUwbs) {
        try {
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            //ip重复判断
            Gateway_uwb gatewayUwbs1 = gatewayService.findByIp(gatewayUwbs.getIp());
            Gateway_uwb gatewayUwbs2 = gatewayService.findById(gatewayUwbs.getId());
            boolean updateIp = false;
            if (!gatewayUwbs2.getIp().equals(gatewayUwbs.getIp())) {
                JSONObject object = new JSONObject();
                object.put("id", gatewayUwbs.getId());//网关id
                object.put("type", 0);//1连接 0断开连接
                kafkaTemplate.send(KafukaTopics.CONNECT_GATEWAY, object.toString());
                updateIp = true;

            }
            if (!gatewayUwbs2.getIp().equals(gatewayUwbs.getIp()) && !NullUtils.isEmpty(gatewayUwbs1)) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.GATEWAYIP_CONFIX));
            }
            if (gatewayService.updateGateway(gatewayUwbs)) {
                //生成操作日志-->添加分站数据
                operationlogService.addOperationlog(Integer.valueOf(uid), LocalUtil.get(KafukaTopics.UDPATE_GATEWAY) + gatewayUwbs.getName());
                if (updateIp) {
                    Thread.sleep(1000);
                    JSONObject object = new JSONObject();
                    object.put("id", gatewayUwbs.getId());//网关id
                    object.put("type", 1);//1连接 0断开连接
                    kafkaTemplate.send(KafukaTopics.CONNECT_GATEWAY, object.toString());
                }
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.SAVE_SUCCESS), gatewayUwbs.getId());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.SAVE_FAIL));
    }

    @RequestMapping(value = "/delGateway/{ids}")
    @ApiOperation(value = "uwb网关删除接口", notes = "uwb网关id集")
    @ApiImplicitParam(paramType = "path", name = "ids", value = "uwb网关id集", required = true, dataType = "String")
    public CommonResult delSub(@PathVariable("ids") String ids) {
        try {
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            if (gatewayService.delGateway(ids)) {
                operationlogService.addOperationlog(Integer.valueOf(uid), LocalUtil.get(KafukaTopics.DELETE_GATEWAY));
                return new CommonResult(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400, LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

    /**
     * uwb网关连接
     */
    @RequestMapping("/connectGateway/{id}")
    @ApiOperation(value = "uwb网关连接接口", notes = "uwb网关id")
    @ApiImplicitParam(paramType = "path", name = "id", value = "uwb网关id", required = true, dataType = "int")
    public CommonResult connectGateway(@PathVariable("id") Integer id) throws InterruptedException {
        try {

            Gateway_uwb gatewayUwbs = gatewayService.findById(id);
            if (NullUtils.isEmpty(gatewayUwbs)) {
                return (new CommonResult<>(400, LocalUtil.get(KafukaTopics.SELECT_GATEWAY)));
            }
            redisService.set("gateway" + id, "-1");
            JSONObject object = new JSONObject();
            object.put("id", id);//网关id
            object.put("type", 1);//1连接 0断开连接
            kafkaTemplate.send(KafukaTopics.CONNECT_GATEWAY, object.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return (new CommonResult<>(400, LocalUtil.get(KafukaTopics.SYSTEM_BUSY)));
        }
        Thread.sleep(4000);
        Gateway_uwb gateway = gatewayService.findById(id);
        if (gateway.getConnect() == 0) {
            return (new CommonResult<>(400, LocalUtil.get(KafukaTopics.FAIL)));
        } else {
            return (new CommonResult<>(200, LocalUtil.get(KafukaTopics.SUCCESS)));
        }

    }

    @RequestMapping("/disconnectGateway/{id}")
    @ApiOperation(value = "uwb网关连接接口", notes = "uwb网关id")
    @ApiImplicitParam(paramType = "path", name = "id", value = "uwb网关id", required = true, dataType = "int")
    public CommonResult disconnectGateway(@PathVariable("id") Integer id) throws InterruptedException {

        try {
            //发送断开网关主题
            // gatewayService.updateGatewayConnect(id,0);
            JSONObject object = new JSONObject();
            object.put("id", id);//网关id
            object.put("type", 0);//1连接 0断开连接
            kafkaTemplate.send(KafukaTopics.CONNECT_GATEWAY, object.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return (new CommonResult<>(400, LocalUtil.get(KafukaTopics.SYSTEM_BUSY)));
        }
        Thread.sleep(4000);

        Gateway_uwb gateway = gatewayService.findById(id);
        if (gateway.getConnect() == 1) {
            return (new CommonResult<>(400, LocalUtil.get(KafukaTopics.FAIL)));
        } else {
            return (new CommonResult<>(200, LocalUtil.get(KafukaTopics.SUCCESS)));
        }

    }
}
