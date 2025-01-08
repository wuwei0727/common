package com.tgy.rtls.web.controller.equip;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.SubScan;
import com.tgy.rtls.data.entity.equip.Substation;
import com.tgy.rtls.data.entity.equip.Tag;
import com.tgy.rtls.data.entity.equip.TagScan;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.SubScanService;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.equip.TagScanService;
import com.tgy.rtls.data.service.equip.TagService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.equip
 * @date 2021/1/19
 * 设备搜寻功能
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/scan")
public class ScanController {
    @Autowired
    private SubScanService subScanService;
    @Autowired
    private SubService subService;
    @Autowired
    private TagScanService tagScanService;
    @Autowired
    private TagService tagService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 开启与停止搜寻分站功能
     */
    @RequestMapping(value = "/switchSubScan/{id}")
    @ApiOperation(value = "开启与停止搜寻分站功能", notes = "开关")
    @ApiImplicitParam(paramType = "path", name = "id", value = "开关（0关闭 1开启）", required = true, dataType = "int")
    public CommonResult switchSubScan(@PathVariable("id") Integer id) {
        try {
            //根据id判断是开启还是关闭
            if (id == 1) {//1.开启
                //1.1清空上一次的数据
                subScanService.delSubScan();
                //1.2清空redis中的subscan缓存
                Set<String> keys6 = redisTemplate.keys("subScanNum*");//分站参数缓存
                for (String key : keys6) {
                    redisService.remove(key);
                }
                //1.3将redis中的subScan改为yes
                redisService.set("subScan", "yes");
            } else {//2.关闭
                //将redis中的subScan改为no
                redisService.set("subScan", "no");
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.OPERATION_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/switchTagScan/{id}")
    @ApiOperation(value = "开启与停止搜寻标签功能", notes = "开关")
    @ApiImplicitParam(paramType = "path", name = "id", value = "开关（0关闭 1开启）", required = true, dataType = "int")
    public CommonResult switchTagScan(@PathVariable("id") Integer id) {
        try {
            //根据id判断是开启还是关闭
            if (id == 1) {//1.开启
                //1.1清空上一次的数据
                tagScanService.delTagScan();
                //1.2清空redis中的subscan缓存
                Set<String> keys6 = redisTemplate.keys("tagScanNum*");//搜寻标签缓存
                for (String key : keys6) {
                    redisService.remove(key);
                }
                //1.3将redis中的subScan改为yes
                redisService.set("tagScan", "yes");
            } else {//2.关闭
                //将redis中的subScan改为no
                redisService.set("tagScan", "no");
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.OPERATION_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/subScanSel")
    @ApiOperation(value = "搜寻分站列表", notes = "分站列表状态")
    @ApiImplicitParam(paramType = "query", name = "status", value = "分站列表状态 0未有 1已有", required = false, dataType = "int")
    public CommonResult subScanSel(Integer status, String code1, String code2) {
        try {
            List<SubScan> subScanList = subScanService.findByAll(status, code1, code2);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), subScanList);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/tagScanSel")
    @ApiOperation(value = "搜寻标签列表", notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", name = "type", value = "标签类型", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "status", value = "标签列表状态 0未有 1已有", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "code1", value = "识别码1", required = false, dataType = "string"),
            @ApiImplicitParam(paramType = "query", name = "code2", value = "识别码2", required = false, dataType = "string")
    })
    public CommonResult tagScanSel(Integer status, Integer type, String code1, String code2) {
        try {
            List<TagScan> tagScanList = tagScanService.findByAll(status, type, code1, code2);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), tagScanList);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/addSubScan/{num}")
    @ApiOperation(value = "添加至分站列表", notes = "开关")
    @ApiImplicitParam(paramType = "path", name = "num", value = "分站编号", required = true, dataType = "string")
    public CommonResult addSubScan(@PathVariable("num") String num) {
        try {
            String[] split = num.split(",");
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            int instanceid = Integer.parseInt(redisService.get("instance" + uid));
            for (String s : split) {
                SubScan subScan = subScanService.findByNum(s);
                Substation substation = new Substation();
                substation.setNum(subScan.getNum());
                substation.setInstanceid(instanceid);
                substation.setIpAddress(subScan.getIpAddress());
                substation.setPowerstate(subScan.getPowerstate());
                substation.setBatteryVolt(String.valueOf(subScan.getBatteryVolt()));
                //添加到分站列表
                subService.addSub(substation, null);
                //修改状态
                subScanService.updateStatus(s, 1);
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/addTagScan/{num}")
    @ApiOperation(value = "添加至标签列表", notes = "开关")
    @ApiImplicitParam(paramType = "path", name = "num", value = "标签编号", required = true, dataType = "string")
    public CommonResult addTagScan(@PathVariable("num") String num) {
        try {
            String[] split = num.split(",");
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            int instanceid = Integer.parseInt(redisService.get("instance" + uid));
            for (String s : split) {
                TagScan tagScan = tagScanService.findByNum(s);
                Tag tag1 = new Tag();
                tag1.setInstanceid(instanceid);
                tag1.setType(tagScan.getType());
                tag1.setFrequency(tagScan.getFrequency());
                tag1.setPower(33);
                tag1.setBatteryVolt(String.valueOf(tagScan.getBatteryVolt()));
                tag1.setNum(tagScan.getNum());
                tagService.addTag(tag1);
                tagScanService.updateStatus(s, 1);
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

}
