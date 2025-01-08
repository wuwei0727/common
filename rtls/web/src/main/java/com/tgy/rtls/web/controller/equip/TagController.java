package com.tgy.rtls.web.controller.equip;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.ImportUsersException;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.Tag;
import com.tgy.rtls.data.entity.equip.TagFirmware;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.mapper.equip.TagMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.TagService;
import com.tgy.rtls.web.util.FileUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.equip
 * @date 2020/10/16
 * 标签管理类
 */
@RestController
@RequestMapping(value = "/tag")
@CrossOrigin
public class TagController {
    @Autowired
    private TagService tagService;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OperationlogService operationlogService;
    //上传真实地址
    @Value("${file.uploadFolder}")
    private String uploadFolder;
    @Value("${file.url}")
    private String url;
    @Value("${coal.ip}")
    private String ip;
    @Value("${server.port}")
    private String port;
    @Autowired
    private FastFileStorageClient fastFileStorageClient;
    //上传真实地址
    @Value("${fdfs.url}")
    private String fdfsUrl;
    @RequestMapping(value = "/getTagSel")
    @ApiOperation(value = "标签查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "num",value = "卡号",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "binding",value = "是否绑定人员",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getTagSel(String num,Integer binding,
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
                List<Tag> tags = tagService.findByAll(num,binding,desc,instanceid);
                return new CommonResult<Object>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),tags);
            }
            /*
             * 分页 total-->总数量
             * */
            int total=tagService.findByAll(num,binding,desc,instanceid).size();
            if (pageIndex > total / pageSize) {
                if (total % pageSize == 0) {
                    pageIndex = total / pageSize;
                } else {
                    pageIndex = total / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<Tag> tags = tagService.findByAll(num,binding,desc,instanceid);
            PageInfo<Tag> pageInfo=new PageInfo<>(tags);
            Map<String,Object> map=new HashMap<>();
            map.put("list",pageInfo.getList());
            map.put("pageIndex", pageIndex);
            map.put("total", pageInfo.getTotal());
            map.put("pages", pageInfo.getPages());
            operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.QUERY_TAG));
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),map);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getTagId/{id}")
    @ApiOperation(value = "标签详情接口",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "id",value = "标签id",required = true,dataType = "int")
    public CommonResult<Tag> getTagId(@PathVariable("id")Integer id){
        try {
            Tag tag=tagService.findById(id);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),tag);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/addTag")
    @RequiresPermissions("tag:add")
    @ApiOperation(value = "标签新增接口",notes = "标签信息")
    public CommonResult<Integer> addTag(Tag tag){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            //实例
            tag.setInstanceid(instanceid);
            //卡号重名判断
            Tag tag1=tagMapper.findByNum(tag.getNum());
            if (!NullUtils.isEmpty(tag1)){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.TAG_EXIST));
            }
            if (tagService.addTag(tag)){
                operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.ADD_TAG)+tag.getNum());
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS),tag.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<Integer>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequestMapping(value = "/updateTag")
 /*   @RequiresPermissions("tag:update")*/
    @ApiOperation(value = "标签修改接口",notes = "标签信息")
    public CommonResult updateTag(Tag tag){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            //实例
            tag.setInstanceid(instanceid);
            //修改时间
            tag.setUpdateTime(new Date());
            //卡号重名判断
            Tag tag1=tagMapper.findByNum(tag.getNum());
            Tag tag2=tagService.findById(tag.getId());
            if (!tag2.getNum().equals(tag.getNum())&&!NullUtils.isEmpty(tag1)){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.TAG_INUSE));
            }
            if (tagService.updateTag(tag)){
                operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.UPDATE_TAG)+tag.getNum());
                return new CommonResult(200,LocalUtil.get(KafukaTopics.SAVE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.SAVE_FAIL));
    }

    @RequestMapping(value = "/delTag/{ids}")
/*    @RequiresPermissions("tag:del")*/
   /* @ApiOperation(value = "标签删除接口",notes = "标签id集")*/
    @ApiImplicitParam(paramType = "path",name = "ids",value = "标签id集",required = true,dataType = "String")
    public CommonResult delTag(@PathVariable("ids")String ids){
        try {
            Integer uid=1;
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= member.getUid();
            }
            operationlogService.addOperationlog(uid,LocalUtil.get(KafukaTopics.DELETE_TAG)+tagService.findByNameId(ids));
            if (tagService.delTag(ids)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

    /*
    * 导入标签
    * */
    @RequestMapping(value = "/importTag")
    public CommonResult<String> importTag(MultipartFile file){
        if (!file.isEmpty()){
            try {
                String uid="12";
                Member member=(Member) SecurityUtils.getSubject().getPrincipal();
                if(!NullUtils.isEmpty(member)){
                    uid= String.valueOf(member.getUid());
                }
                int instanceid= Integer.parseInt(redisService.get("instance"+uid));
                int i=tagService.importLabelFromExcel(file,instanceid);
                if (i==0){
                    return new CommonResult<>(400,LocalUtil.get(KafukaTopics.CONTENT_EMPTY));
                }else if(i>0){
                    return new CommonResult<>(200,LocalUtil.get(KafukaTopics.IMPORT_SUCCESS)+i+LocalUtil.get(KafukaTopics.N_COUNTINFO));
                }else{
                    return new CommonResult<>(400,LocalUtil.get(KafukaTopics.IMPORT_FAIL_ERRORFORMAT));
                }
            }catch (ImportUsersException ex){
                return new CommonResult<>(400,ex.getMessage());
            }catch (Exception e){
                e.printStackTrace();
                return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_ERROR));
            }
        }else {
            return new CommonResult<>(400,LocalUtil.get(KafukaTopics.IMPORT_FAIL_EMPTYFILE));
        }
    }

    /*
    * 标签应用程序版本升级
    * */
    @RequestMapping(value = "/upgradeTag")
  /*  @RequiresPermissions("developer:view")*/
    public CommonResult<Object> upgradeTag(String num,MultipartFile file){
        try {
            if (!NullUtils.isEmpty(file)){
                //判断升级包的正确
                //CommonResult<Object> commonResult= FileUtils.upgradeFile(file,uploadFolder+"/tag");
               // if (commonResult.getCode()==200)
                {
                 /*   String newName= uploadFolder+"/tag/"+commonResult.getData();
                    FileItem fileItem = FileUtils.createFileItem(newName);
                    MultipartFile encodeFile = new CommonsMultipartFile(fileItem);*/
                    StorePath encodestorePath=fastFileStorageClient.uploadFile(file.getInputStream(),file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()),null);
                    //解析后的文件路径
                    String kafkaFile = encodestorePath.getFullPath();
                    tagService.upgradeTag(num,fdfsUrl+kafkaFile);
                    return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPGRADE_SUCCESS));
                }/* else {
                    return commonResult;
                }*/
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_ERROR));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.UPLOADFILE_ERROR));
    }


    /*
    * 标签调试内容 蜂鸣器 beep  重启 reboot  文字输入  语音输入
    * */
    @RequestMapping(value = "/debugTag")
/*    @RequiresPermissions("developer:view")*/
    @ApiOperation(value = "标签调试内容接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "tagid",value = "标签编号",required = true,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "bsid",value = "基站编号",required = true,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "keyOrder",value = "下发命令判断(beep-->蜂鸣器控制 reboot-->标签重启 text-->文字输入" +
                    "id-->修改标签ID power-->功率控制 lowpower-->低功耗模式 sensorperiod-->传感器上传间隔修改 movelevel-->运动阈值" +
                    " heartperiod-->心率检测周期 locpara-->定位间隔 接收窗口时间 voice-->语音 groupbslist-->目标基站地址 grouprangetime-->组测距周期)",required = true,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "type",value = "读写标志位 0读 1写",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "beepInterval",value = "蜂鸣器鸣叫间隔",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "beepState",value = "蜂鸣器状态 0关闭 1打开",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "text",value = "文字输入",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "newId",value = "新标签编号",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "pa",value = "0 关闭 1打开",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "powerLevel",value = "功率",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "lowPowerMode",value = "0退出低功耗模式 1开启低功耗模式",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "sensorInterval",value = "传感器上传周期",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "moveLevel",value = "运动阈值",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "heartInterval",value = "心率监测间隔",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "locaInval",value = "定位间隔",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "rxInval",value = "接收窗口时间",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "groupbslist",value = "目标基站测距地址",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "grouprangetime",value = "组测距周期",required = false,dataType = "int"),
    })
    public Object debugTag(TagFirmware tagFirmware,MultipartFile file){
            try {
                if (!NullUtils.isEmpty(file) && !file.isEmpty()){
                    CommonResult<Object> commonResult= FileUtils.debugVoice(file,uploadFolder,url);
                    //如果状态码不是200 直接return
                    if (commonResult.getCode()!=200){
                        return commonResult;
                    }
                    //将存储到本地的解码后的文件上传到服务器
                    FileItem fileItem = FileUtils.createFileItem((String) commonResult.getData());
                    MultipartFile encodeFile = new CommonsMultipartFile(fileItem);
                    StorePath encodestorePath=fastFileStorageClient.uploadFile(encodeFile.getInputStream(),encodeFile.getSize(), FilenameUtils.getExtension(encodeFile.getOriginalFilename()),null);
                    //解析后的文件路径
                    String kafkaFile = encodestorePath.getFullPath();


                    tagFirmware.setUrl(fdfsUrl+kafkaFile);
                }
                tagService.debugTag(tagFirmware);
               // TimeUnit.SECONDS.sleep(4);
                //判断是否接受到了命令
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.SEND_SUCCESS),0);
            }catch (Exception e){
                e.printStackTrace();
                return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_ERROR));
            }
    }
}
