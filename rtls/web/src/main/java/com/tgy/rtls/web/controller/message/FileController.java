package com.tgy.rtls.web.controller.message;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.message.FileRecord;
import com.tgy.rtls.data.entity.message.FileSyn;
import com.tgy.rtls.data.entity.message.TextRecord;
import com.tgy.rtls.data.entity.message.VoiceRecord;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.user.Person;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.message.FileService;
import com.tgy.rtls.data.service.user.PersonService;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.message
 * @date 2020/10/26
 *通讯-- 文件发送管理
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/file")
public class FileController {
    @Autowired
    private FileService fileService;
    @Autowired
    private PersonService personService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private FastFileStorageClient fastFileStorageClient;
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
    private String fastDfsUrl;
    @Autowired
    private LocalUtil localUtil;
    @RequestMapping(value = "/getFileSel")
    @ApiOperation(value = "通讯信息查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "name",value = "人员名",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "startTime",value = "开始时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "endTime",value = "结束时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getFileSel(String name,String startTime,String endTime,
                                            @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                            @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            String uid="12";

     /*       SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",localUtil.getCurrentLocale());
            if(endTime!=null&&!endTime.trim().isEmpty()) {
                Date date = dateFormat.parse(endTime);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                if(date.getHours()==0&&)
                Calendar after = DateUtils.getAfterDay(calendar);
                endTime = dateFormat.format(after.getTime());
            }*/
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.QUERY_MESSAGE));
            //按条件查询
            //pageSize<0时查询所有
            if (pageSize<0){
                List<FileSyn> fileSyns = fileService.findByAll(name,startTime,endTime,instanceid,null,null);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),fileSyns);
            }
            /*
             * 分页 total-->总数量
             * */
            int total=fileService.findByAll(name,startTime,endTime,instanceid,null,null).size();
            if (total!=0) {
                if (pageIndex > total / pageSize) {
                    if (total % pageSize == 0) {
                        pageIndex = total / pageSize;
                    } else {
                        pageIndex = total / pageSize + 1;
                    }
                }
            }
            List<FileSyn> fileSyns = fileService.findByAll(name,startTime,endTime,instanceid,pageIndex-1,pageSize);
            for (FileSyn file:fileSyns
                 ) {
                FileUtils.pullRemoteFileToLocal(file.getFile());
            }
            Map<String,Object> result=new HashMap<>();
            result.put("list",fileSyns);
            result.put("pageIndex", pageIndex);
            result.put("total", total);
            result.put("pages", total/pageSize+1);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getFileId")
    @ApiOperation(value = "通讯信息详情接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "personid",value = "人员id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "startTime",value = "开始时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "endTime",value = "结束时间",required = false,dataType = "String")
    })
    public CommonResult<Object> getFileId(Integer personid,String startTime,String endTime){
        try {
            //人员信息
            Person person=personService.findById(personid);
            List<FileRecord> fileRecords=fileService.findByPersonid(personid,startTime,endTime);
            for (FileRecord file:fileRecords
            ) {
                FileUtils.pullRemoteFileToLocal(file.getFile());
            }
            Map<String,Object> map=new HashMap<>();
            map.put("person",person);
            map.put("files",fileRecords);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),map);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/addVoice")
    @ApiOperation(value = "语音新增接口",notes = "通讯信息信息")
    public CommonResult<Object> addVoice(String personids,MultipartFile file){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String currentTime = dateFormat.format(new Date());
            VoiceRecord voice=new VoiceRecord();
            voice.setInstanceid(instanceid);
            voice.setTitle(currentTime);
            voice.setDirection(1);
            voice.setStatus(-1);
            //String http="http://"+ip+":"+port;
            String http=fastDfsUrl;
            String kafkaFile="";
            if (!NullUtils.isEmpty(file) && !file.isEmpty()){
                //服务器存储的正常语音文件
                StorePath storePath=fastFileStorageClient.uploadFile(file.getInputStream(),file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()),null);
                // voice file 存储编码后的文件地址
                CommonResult<Object> commonResult= FileUtils.analysisVoice(file,uploadFolder,url,voice);
                //如果状态码不是200 直接return
                if (commonResult.getCode()!=200){
                    return commonResult;
                }
                //将存储到本地的解码后的文件上传到服务器
                String head=voice.getFile();// head 为本地编码后的文件地址
                FileItem fileItem = FileUtils.createFileItem(uploadFolder+head.substring(5));
                MultipartFile encodeFile = new CommonsMultipartFile(fileItem);
                CommonResult<Object> commonResult1 = FileUtils.uploadFileName(uploadFolder, file,storePath.getFullPath());
                voice.setFilelocal(url+commonResult1.getData());
                StorePath encodestorePath=fastFileStorageClient.uploadFile(encodeFile.getInputStream(),encodeFile.getSize(), FilenameUtils.getExtension(encodeFile.getOriginalFilename()),null);
                //解析后的文件路径
                kafkaFile=encodestorePath.getFullPath();
                voice.setFile(storePath.getFullPath());
            }

            if (fileService.addVoice(personids,voice,http,kafkaFile)){
                operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.SEND_AUDIO)+voice.getTitle());
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.SEND_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.SEND_FAIL));
    }

    @RequestMapping(value = "/addText")
    @ApiOperation(value = "文本新增接口",notes = "通讯信息信息")
    public CommonResult<Object> addText(String personids,String file) {
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String currentTime = dateFormat.format(new Date());
            TextRecord text=new TextRecord();
            text.setFile(file);
            text.setTitle(currentTime);
            text.setInstanceid(instanceid);
            text.setStatus(-1);
            if (fileService.addText(personids,text)){
                operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.SEND_TXT)+text.getTitle());
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.SEND_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.SEND_FAIL));
    }

    @RequestMapping(value = "/retreatText")
    @RequiresPermissions("text:retreat")
    @ApiOperation(value = "撤退命令",notes = "无")
    public CommonResult<Object> retreatText() {
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String title = dateFormat.format(new Date());//标题
            if (fileService.retreatText(instanceid,title)){
                operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.SEND_RETRIVE));
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.SEND_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.SEND_FAIL));
    }

    /*数据恢复*/
    @RequestMapping(value = "/recovery")
    public CommonResult<Object> recovery(MultipartFile file, String bsid) {
        try {
            if (!NullUtils.isEmpty(file)) {
                CommonResult<Object> commonResult= FileUtils.addFile(file,uploadFolder+"/"+bsid);
                if (commonResult.getCode()==200) {
                    String path = String.valueOf(commonResult.getData());
                    fileService.readFileCor(path);
                    return new CommonResult<>(200,LocalUtil.get(KafukaTopics.RECOVERY_SUCCESS));
                } else {
                    return commonResult;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.FAIL));
    }

    /*
     * 音频接收
     * */
    @RequestMapping(value = "/voiceAccept")
    public CommonResult<Object> voiceAccept(MultipartFile file, String bsid, String filename,String fileid) {
        Map<String, Object> map = new HashMap<>();
        try {
            if (null != file && !file.isEmpty()) {
                CommonResult<Object> commonResult = FileUtils.analysisVoice(file, uploadFolder+"/"+bsid, url+"/"+bsid,filename);
                if (commonResult.getCode()==200) {
                    //将存储到本地的解码后的文件上传到服务器
                    String newName= uploadFolder+"/"+bsid+"/"+filename +".wav";
                    FileItem fileItem = FileUtils.createFileItem(newName);
                    MultipartFile encodeFile = new CommonsMultipartFile(fileItem);
                    StorePath encodestorePath=fastFileStorageClient.uploadFile(encodeFile.getInputStream(),encodeFile.getSize(), FilenameUtils.getExtension(encodeFile.getOriginalFilename()),null);
                    CommonResult<Object> commonResult1 = FileUtils.uploadFileName(uploadFolder, file,encodestorePath.getFullPath());
                    //解析后的文件路径
                    String kafkaFile = encodestorePath.getFullPath();

                    fileService.updateVoice(fileid,0,kafkaFile,(String)commonResult1.getData());
                    return new CommonResult<>(200,LocalUtil.get(KafukaTopics.SUCCESS));
                } else {
                    return commonResult;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.FAIL));
    }
}
