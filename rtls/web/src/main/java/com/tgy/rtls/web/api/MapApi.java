package com.tgy.rtls.web.api;

import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.user.Instance;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.map.BsConfigService;
import com.tgy.rtls.data.service.map.Map2dService;
import com.tgy.rtls.data.service.user.InstanceService;
import com.tgy.rtls.data.service.user.impl.MemberService;
import com.tgy.rtls.web.jwt.TokenUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.map
 * @date 2020/10/19
 * 地图管理类
 */
@RestController
@RequestMapping(value = "/mapApi")
@CrossOrigin
@ApiModel("地图Api")
public class MapApi {
    @Autowired
    private Map2dService map2dService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private FastFileStorageClient fastFileStorageClient;
    @Autowired
    private BsConfigService bsConfigService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private InstanceService instanceService;

    @Value("${file.url}")
    private String url;
    //上传真实地址
    @Value("${file.uploadFolder}")
    private String uploadFolder;
    @Value("${fdfs.url}")
    private String fdfsUrl;
    @Value("${websocket.url}")
    private String webSocketUrl;
    @RequestMapping(value = "/getMap2dSel")
    @ApiOperation(value = "2维地图查询接口", notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", name = "name", value = "地图名", required = false, dataType = "String")
    })
    public CommonResult<Object> getMap2dSel(String name,
                                            @RequestParam(value = "enable", defaultValue = "1") Integer enable,String token
                                        ) {
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());
            List<Map_2d> map2ds = map2dService.findByAll(name, enable, instances.get(0).getId()==null?null:instances.get(0).getId()+"");
            Map<String, Object> map = new HashMap<>();
            map.put("list",map2ds);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getMap2dId/{id}")
    @ApiOperation(value = "地图详情接口", notes = "无")
    @ApiImplicitParam(paramType = "path", name = "id", value = "2维地图id", required = true, dataType = "int")
    public CommonResult<Map_2d> getMap2dId(@PathVariable("id") Integer id,String token) {
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());
            Map_2d map2d = map2dService.findById(id);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map2d);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/addMap2d")
    @RequiresPermissions("map:add")
    @ApiOperation(value = "2维地图新增接口", notes = "2维地图信息")
    public CommonResult<Object> addMap2d(Map_2d map2d, MultipartFile file,String token) {
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());

            //实例
            map2d.setInstanceid(instances.get(0).getId()==null?null:instances.get(0).getId()+"");

            if (map2dService.addMap2d(map2d)) {
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS), map2d.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<Object>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequestMapping(value = "/updateMap2d")
    @RequiresPermissions("map:update")
    @ApiOperation(value = "2维地图修改接口", notes = "2维地图信息")
    public CommonResult updateMap2d(Map_2d map2d, MultipartFile file,String token) {
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());
            String name=map2d.getName();
            if(name==null||name.trim().equals("")){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.NAME_EMPTY));
            }
            String instanceid = instances.get(0).getId()==null?null:instances.get(0).getId()+"";
            List<Map_2d> sameName = map2dService.findByAllSame(name);
            if(sameName!=null&&sameName.size()>0&&!map2d.getId().equals(sameName.get(0).getId())){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.NAME_CONFLICT));
            }
            //实例
            map2d.setInstanceid(instanceid);
            //修改时间
            map2d.setUpdateTime(new Date());
            //根据不同的地图类型解析不同文件

            if (map2dService.updateMap2d(map2d)) {
                    return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400, LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequestMapping(value = "/delMap2d/{ids}")
    @ApiOperation(value = "2维地图删除接口", notes = "2维地图id集")
    @ApiImplicitParam(paramType = "path", name = "ids", value = "2维地图id集", required = true, dataType = "String")
    public CommonResult delMap2d(@PathVariable("ids") String ids,String token) {
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());
            if (map2dService.delMap2d(ids)) {
                return new CommonResult(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400, LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }




}
