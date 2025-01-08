package com.tgy.rtls.web.controller.user;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.ImportUsersException;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.user.Person;
import com.tgy.rtls.data.entity.user.PersonVO;
import com.tgy.rtls.data.mapper.checkingin.ClassgroupMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.user.PersonService;
import com.tgy.rtls.data.tool.ExportTxt;
import com.tgy.rtls.web.util.FileUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.FilenameUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.user
 * @date 2020/10/14
 * 人员管理类
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/person")
public class PersonController {
    @Autowired
    private PersonService personService;
    @Autowired
    private RedisService redisService;
    @Autowired(required = false)
    private ClassgroupMapper classgroupMapper;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private FastFileStorageClient fastFileStorageClient;
    @Value("${file.url}")
    public String url;
    //上传真实地址
    @Value("${file.uploadFolder}")
    public String uploadFolder;
    @Value("${fdfs.url}")
    public String fdfsUrl;

    @RequestMapping(value = "/getPersonSel")
    @ApiOperation(value = "人员查询接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "keyword",value = "关键字",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "classid",value = "班组id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "status",value = "在线状态",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getPersonSel(String keyword, Integer departmentid, Integer worktypeid, Integer jobid, Integer classid,Integer status,Integer workorder,
                                             @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                             @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            String instanceid=(redisService.get("instance"+uid));
            //按条件查询

            //pageSize<0时查询所有
            if (pageSize<0){
                List<PersonVO> personList = personService.findByAll(instanceid,departmentid,worktypeid,jobid,classid,status,keyword,workorder);
                return new CommonResult<Object>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),personList);
            }
            /*
            * 分页 num-->总数量
            * */
            int num=personService.findByAll(instanceid,departmentid,worktypeid,jobid,classid,status,keyword,workorder).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                } else {
                    pageIndex = num / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<PersonVO> personList = personService.findByAll(instanceid,departmentid,worktypeid,jobid,classid,status,keyword,workorder);
            PageInfo<PersonVO> pageInfo=new PageInfo<>(personList);
            Map<String,Object> map=new HashMap<>();
            map.put("list",pageInfo.getList());
            map.put("pageIndex", pageIndex);
            map.put("total", pageInfo.getTotal());
            map.put("pages", pageInfo.getPages());
            operationlogService.addOperationlog(Integer.valueOf(uid), LocalUtil.get(KafukaTopics.QUERY_PERSONINFO));
            return new CommonResult<Object>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),map);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getPersonId/{id}")
    @ApiOperation(value = "人员详情接口",notes = "人员id")
    @ApiImplicitParam(paramType = "path",name = "id",value = "人员id",required = true,dataType = "int")
    public CommonResult<Person> getPersonId(@PathVariable("id")Integer id){
        try {
            Person person=personService.findById(id);
            FileUtils.pullRemoteFileToLocal(person.getPhoto());
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),person);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


    @RequestMapping(value = "/addPerson")
/*    @RequiresPermissions("person:add")*/
    @ApiOperation(value = "人员新增接口",notes = "人员信息")
    public CommonResult<Object> addPerson(Person person, MultipartFile file){
        try {
            //实例id
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }

            if (person.getNum()==null||person.getNum().trim().isEmpty()){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.NUM_EMPTY));
            }
            if (person.getName()==null||person.getName().trim().isEmpty()){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.NAME_EMPTY));
            }


            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            //工号重名判断
            Person person1=personService.findByNum(person.getNum());
            if (person1!=null){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.NUM_EXIST));
            }
            if (!NullUtils.isEmpty(file) && !file.isEmpty()){
/*                CommonResult<Object> commonResult=FileUtils.uploadFile(uploadFolder,file);
                //如果状态码不是200 直接return
                if (commonResult.getCode()!=200){
                    return commonResult;
                }
                //添加图片路径
                person.setPhoto(url+String.valueOf(commonResult.getData()));*/
                StorePath storePath=fastFileStorageClient.uploadFile(file.getInputStream(),file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()),null);
                person.setPhoto(storePath.getFullPath());
            }

            person.setInstanceid(instanceid);
            if (personService.addPerson(person)){
                operationlogService.addOperationlog(Integer.valueOf(uid), LocalUtil.get(KafukaTopics.ADD_PERSON)+person.getName());
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS),person.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequestMapping(value = "/updatePerson")
  /*  @RequiresPermissions("person:update")*/
    @ApiOperation(value = "人员修改接口",notes = "人员信息")
    public CommonResult<Object> updatePerson(Person person, MultipartFile file){
        try {
            //实例id
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            if (person.getName()==null||person.getName().trim().isEmpty()){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.NAME_EMPTY));
            }

            if (person.getNum()==null||person.getNum().trim().isEmpty()){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.NUM_EMPTY));
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            Person person1=personService.findById(person.getId());
            Person person2=personService.findByNum(person.getNum());
            if (person2!=null && !person1.getNum().equals(person.getNum())){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.NUM_EXIST));
            }
            if (!NullUtils.isEmpty(file)&&!file.isEmpty()){
                String fileExtName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
                if (!"jpg".equals(fileExtName) && !"png".equals(fileExtName)) {
                    return new CommonResult<>(400,"文件格式不正确");
                }
                StorePath storePath=fastFileStorageClient.uploadFile(file.getInputStream(),file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()),null);
                person.setPhoto(storePath.getFullPath());

                String fullPath=storePath.getFullPath();
                CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, file,fullPath);
                if (commonResult.getCode()==200){
                    person.setPhotolocal(url+String.valueOf(commonResult.getData()));
                }else
                    return commonResult;


            }
            person.setInstanceid(instanceid);
            if (personService.updatePerson(person)){
                operationlogService.addOperationlog(Integer.valueOf(uid), LocalUtil.get(KafukaTopics.UPDATE_PERSON)+person.getName());
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS),person.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }


    @RequestMapping(value = "/delPerson/{ids}")
/*    @RequiresPermissions("person:del")*/
    @ApiOperation(value = "人员删除接口",notes = "人员id集")
    @ApiImplicitParam(paramType = "path",name = "ids",value = "人员id集",required = true,dataType = "String")
    public CommonResult delPerson(@PathVariable("ids")String ids){
        try {
            Integer uid=null;
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid=member.getUid();
            }
            String num=personService.findByNameId(ids);
            classgroupMapper.delClassgroupByperson( ids.split(","));
            if (personService.delPerson(ids)){
                operationlogService.addOperationlog(uid,LocalUtil.get(KafukaTopics.DELETE_PERSON)+num);
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL_NOPERSON));
    }

    /*
    * 导入人员信息
    * */
    @RequestMapping(value = "/importPerson")
    public CommonResult<String> importTag(MultipartFile file){
        if (!file.isEmpty()){
            try {
                String uid="12";
                Member member=(Member) SecurityUtils.getSubject().getPrincipal();
                if(!NullUtils.isEmpty(member)){
                    uid= String.valueOf(member.getUid());
                }
                int instanceid= Integer.parseInt(redisService.get("instance"+uid));
                int i=personService.importPersonFromExcel(file,instanceid);
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
    * 导出人员信息
    * */
    @RequestMapping("/exportPerson")
    @ApiOperation(value = "人员信息导出接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "keyword",value = "关键字",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "status",value = "在线状态",required = false,dataType = "int")
    })
    public void exportPerson(String keyword, Integer departmentid, Integer worktypeid, Integer jobid
            ,Integer status,String title, HttpServletResponse response){
        response.setContentType("application/binary;charset=UTF-8");
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            String instanceid= (redisService.get("instance"+uid));
            ServletOutputStream out = response.getOutputStream();
            String fileName = new String((LocalUtil.get(KafukaTopics.PERSONINFO) + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())).getBytes(), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
            personService.exportPerson(out,instanceid,keyword,departmentid,worktypeid,jobid,status,title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @RequestMapping(value = "/exportPersonSel")
    @ApiOperation(value = "人员查询接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "keyword",value = "关键字",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "classid",value = "班组id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "status",value = "在线状态",required = false,dataType = "int")
    })
    public void exportPersonSel(String keyword, Integer departmentid, Integer worktypeid, Integer jobid, Integer classid, Integer status, Integer workorder,
                                HttpServletResponse response){

            String uid = "1";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            String instanceid = redisService.get("instance" + uid);
            List<PersonVO> personList = personService.findByAll(instanceid, departmentid, worktypeid, jobid, classid, status, keyword, workorder);
        String outTxt="";
            for (PersonVO p :personList
             ) {
              outTxt=outTxt+p.getTagName()+","+p.getName()+","+p.getLevelName()+"\r\n";
           }
        ExportTxt.exportTxt(response,outTxt,null);


    }
}
