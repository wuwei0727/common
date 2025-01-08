package com.tgy.rtls.web.api;

import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.BsSyn;
import com.tgy.rtls.data.entity.equip.Substation;
import com.tgy.rtls.data.entity.map.BsConfig;
import com.tgy.rtls.data.entity.user.Instance;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.mapper.equip.SubMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.map.BsConfigService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping(value = "/subApi")
@ApiModel("分站Api")
public class SubApi {

    @Autowired
    private SubService subService;
    @Autowired(required = false)
    private SubMapper subMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private FastFileStorageClient fastFileStorageClient;
    @Autowired
    private InstanceService instanceService;
    @Autowired
    LocalUtil localUtil;
    //上传真实地址
    @Value("${file.uploadFolder}")
    private String uploadFolder;
    @Value("${file.url}")
    private String url;
    @Value("${coal.ip}")
    private String ip;
    @Value("${server.port}")
    private String port;
    @Value("${fdfs.url}")
    private String fdfsUrl;//分布式文件系统地址
    @Autowired
    private BsConfigService bsConfigService;
    @Autowired
    private MemberService memberService;

    @RequestMapping(value = "/getSubSel")
    @ApiOperation(value = "分站查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "num",value = "分站ID",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "networkstate",value = "网络状态",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "powerstate",value = "供电状态",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "relevance",value = "是否关联地图",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "error",value = "故障信息",required = false,dataType = "int")

    })
    public CommonResult<Object> getSubSel(String num, Integer type, Integer networkstate, Integer powerstate, Integer relevance,
                                          Integer map, Integer error,String token
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


            //int instanceid= Integer.parseInt(redisService.get("instance"+uid));

            List<BsSyn> substations = subService.findByAll(num,type,networkstate,powerstate,relevance,map,error,null,null,null,instances.get(0).getId());
            Map<String,Object> result=new HashMap<>();
            result.put("list",substations);
            //生成操作日志-->查询分站数据
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
    @RequestMapping(value = "/getSubId/{id}")
    @ApiOperation(value = "分站详情接口",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "id",value = "分站id",required = true,dataType = "int")
    public CommonResult<Substation> getSubId(@PathVariable("id")Integer id,String token){
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Substation substation=subService.findById(id);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),substation);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
    @RequestMapping(value = "/addSub")
    @ApiOperation(value = "分站新增接口",notes = "分站信息")
    public CommonResult<Object> addSub(Substation sub, String token,MultipartFile file){
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());


            Substation substation1=subMapper.findByNum(sub.getNum(),localUtil.getLocale());
            if (substation1!=null){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.SUB_INUSE));
            }
            //实例
            sub.setInstanceid(instances.get(0).getId());
/*            if (!NullUtils.isEmpty(file) && !file.isEmpty()){
                //限制图片的宽高 480*272
                BufferedImage bufferedImage= ImageIO.read(file.getInputStream());
                if(bufferedImage !=null) {
                    int width = bufferedImage.getWidth();
                    int height = bufferedImage.getHeight();
                    if (width>480||height>272){
                        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.PICSIZE_ERROR));
                    }
                }
*//*                    CommonResult<Object> commonResult= FileUtils.uploadFile(uploadFolder,file);
                //如果状态码不是200 直接return
                if (commonResult.getCode()!=200){
                    return commonResult;
                }
                //添加图片路径
                sub.setBackground(url+commonResult.getData());*//*

                *//*
                 * 将图片传输到文件服务器fdfs上
                 * *//*
                StorePath storePath=fastFileStorageClient.uploadFile(file.getInputStream(),file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()),null);
                sub.setBackground(storePath.getFullPath());
            }*/
            if (subService.addSub(sub,fdfsUrl)){
                //生成操作日志-->添加分站数据
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS),sub.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ADD_FAIL));
    }
    @RequestMapping(value = "/updateSub")
    @ApiOperation(value = "分站修改接口",notes = "分站信息")
    public CommonResult updateSub(Substation sub,String token){
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());


            Substation substation1=subMapper.findById(sub.getId(),localUtil.getLocale());
            Substation substation2=subMapper.findByNum(sub.getNum(),localUtil.getLocale());
            if (!substation1.getNum().equals(sub.getNum())&&substation2!=null){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.SUB_INUSE));
            }
            //实例
            sub.setInstanceid(instances.get(0).getId());
            //修改时间
            sub.setUpdateTime(new Date());

            if (subService.updateSub(sub,fdfsUrl)){
                if(substation1.getNetworkstate()==0){
                    return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SUBOFFLINE));
                }else
                    return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS),sub.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }
    @RequestMapping(value = "/delSub/{ids}")
    @RequiresPermissions("sub:del")
    @ApiOperation(value = "分站删除接口",notes = "分站id集")
    @ApiImplicitParam(paramType = "path",name = "ids",value = "分站id集",required = true,dataType = "String")
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
            if (subService.delSub(ids)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

    @RequestMapping(value = "/getBsConfigSel")
    @ApiOperation(value = "分站参数查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = true,dataType = "int")

    })
    public CommonResult<Object> getBsConfigSel(@RequestParam(value = "map")Integer map,String token){
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());
            List<BsConfig> bsConfigs = bsConfigService.findByAll(map);
            Map<String,Object> result=new HashMap<>();
            result.put("list",bsConfigs);
            return new CommonResult<Object>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getBsConfigId/{id}")
    @ApiOperation(value = "分站参数详情接口",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "id",value = "分站参数id",required = true,dataType = "int")
    public CommonResult<BsConfig> getBsConfigId(@PathVariable("id")Integer id,String token){
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());
            BsConfig bsConfig=bsConfigService.findById(id);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),bsConfig);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getBsConfigNum/{num}")
    @ApiOperation(value = "分站编号查询详情接口",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "num",value = "分站编号",required = true,dataType = "string")
    public CommonResult<BsConfig> getBsConfigNum(@PathVariable("num")String num,String token){
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());
            BsConfig bsConfig=bsConfigService.findByNum(num);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),bsConfig);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/updateBsConfig")
    @ApiOperation(value = "分站参数修改接口",notes = "分站参数信息")
    public CommonResult updateBsConfig(BsConfig bsConfig,String token){
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());
            if (bsConfigService.updateBsConfig(bsConfig)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequestMapping(value = "/delBsConfig/{ids}")
    @ApiOperation(value = "分站参数删除接口",notes = "分站参数id集")
    @ApiImplicitParam(paramType = "path",name = "ids",value = "分站参数id集",required = true,dataType = "String")
    public CommonResult delBsConfig(@PathVariable("ids")String ids,String token){
        try {
            String phone=null;
            if(token==null|| TokenUtil.getName(token).length()!=11){
                return new CommonResult<>(404,LocalUtil.get(KafukaTopics.TOKEN_ERROR),null);
            }else{
                phone=TokenUtil.getName(token);
            }
            Member member = memberService.findByPhone(phone);
            List<Instance> instances = instanceService.findByUid(member.getUid());
            if (bsConfigService.delBsConfig(ids)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }


}
