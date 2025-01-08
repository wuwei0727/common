package com.tgy.rtls.web.controller.sinopec;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.ByteUtils;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.user.Instance;
import com.tgy.rtls.data.entity.user.LoginRecord;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.user.Permission;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.sinopec.HttpClientService;
import com.tgy.rtls.data.service.user.InstanceService;
import com.tgy.rtls.data.service.user.impl.MemberService;
import com.tgy.rtls.data.service.user.impl.PermissionService;
import com.tgy.rtls.data.tool.AuthException;
import com.tgy.rtls.data.tool.Coder;
import com.tgy.rtls.data.websocket.WebSocketLocation;
import com.tgy.rtls.web.util.PinyinUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 许强
 * @Package com.tuguiyao.controller.user
 * @date 2019/10/25
 */
@RequestMapping(value = "/sinope")
@CrossOrigin
@RestController
@RequiresPermissions("member:view")
public class SinopecController {
    @Autowired
    private MemberService memberService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private InstanceService instanceService;
    @Autowired
    private HttpClientService httpClientService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private WebSocketLocation webSocketLocation;
    @RequestMapping(value = "/getMemberSel")
    @ApiOperation(value = "权限管理成员查询接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "keyword",value = "关键字",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "desc",value = "排序",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "enabled",value = "是否启用",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getMemberSel(String keyword,
                                             @RequestParam(value = "desc ", defaultValue = "addTime desc") String desc,
                                             Integer enabled,
                                             @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                             @RequestParam(value = "pageSize", defaultValue = "1") Integer pageSize) {
        try {
            String cid = null;
            if (pageSize<0){
                List<Member> members = memberService.findByAll(keyword, enabled, cid, desc);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),members);
            }
            int num = memberService.findByAll(keyword, enabled, cid, desc).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                } else {
                    pageIndex = num / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex, pageSize);
            List<Member> members = memberService.findByAll(keyword, enabled, cid, desc);
            PageInfo<Member> pageInfo = new PageInfo<>(members);
            Map<String, Object> map = new HashMap<>();
            map.put("list", pageInfo.getList());
            map.put("pageIndex", pageIndex);
            map.put("total", pageInfo.getTotal());
            map.put("pages", pageInfo.getPages());
            Integer uid=null;
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= member.getUid();
            }
            if(uid==null)
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
            operationlogService.addOperationlog(uid,LocalUtil.get(KafukaTopics.QUERY_PERSONPERMISSION));
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getLoginRecordSel")
    @ApiOperation(value = "权限管理成员查询接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "phone",value = "手机号",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "startTime",value = "开始时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "endTime",value = "结束时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getLoginRecordSel(String phone,String startTime,String endTime,
                                             @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                             @RequestParam(value = "pageSize", defaultValue = "1") Integer pageSize) {
        try {
            if (pageSize<0){
                List<LoginRecord> loginRecords =  memberService.findByLonginRecord(phone,startTime,endTime);
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),loginRecords);
            }
            int num = memberService.findByLonginRecord(phone,startTime,endTime).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                } else {
                    pageIndex = num / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex, pageSize);
            List<LoginRecord> loginRecords =  memberService.findByLonginRecord(phone,startTime,endTime);
            PageInfo<LoginRecord> pageInfo = new PageInfo<>(loginRecords);
            Map<String, Object> map = new HashMap<>();
            map.put("list", pageInfo.getList());
            map.put("pageIndex", pageIndex);
            map.put("total", pageInfo.getTotal());
            map.put("pages", pageInfo.getPages());
            Integer uid=1;
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= member.getUid();
            }
            if(uid==null){
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
            }
            operationlogService.addOperationlog(uid,LocalUtil.get(KafukaTopics.QUERY_LOGINLOG));
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getMemberId/{id}")
    @ApiOperation(value = "权限管理成员详情接口",notes = "成员id")
    @ApiImplicitParam(paramType = "path",name = "id",value = "成员id",required = true,dataType = "int")
    public CommonResult<Object> getMemberId(@PathVariable("id") Integer uid) {
        try {
            Member member = memberService.findById(uid);
            List<Permission> permissions = permissionService.findByCid(member.getCid());
            List<Instance> projects = instanceService.findByCid(member.getCid());
            List<Instance> project1 = instanceService.findByUid(uid);
            Map<String, Object> map = new HashMap<>();
            map.put("status", true);
            map.put("msg", LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            map.put("list", member);
            map.put("allPermission", permissions);//成员可选择的权限
            map.put("allProject", PinyinUtils.pinyinInstance(projects));//成员可选择的实例
            map.put("projects", project1);//成员已拥有的实例
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    /*
     * 编辑用户权限
     * */
    @RequestMapping(value = "/memberPermission")
    @ApiOperation(value = " 编辑用户权限接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "uid",value = "成员id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "permission_ids",value = "权限id集",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "project_ids",value = "实例id集",required = false,dataType = "String")
    })
    public CommonResult<Integer> memeberPermission(Integer uid, String permission_ids, String project_ids) {
        try {
            if (permissionService.memberPermission(uid, permission_ids)) {
                memberService.updateUid(uid, project_ids);
                Member member1=(Member) SecurityUtils.getSubject().getPrincipal();
                List<Permission> permissions = permissionService.findByUid(uid);
                member1.setPermissions(permissions);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequestMapping(value = "/delMember/{ids}")
    @ApiOperation(value = "权限管理成员删除接口",notes = "成员id集")
    @ApiImplicitParam(paramType = "path",name = "ids",value = "成员id集",required = true,dataType = "String")
    public CommonResult<Integer> delMember(@PathVariable("ids") String ids) {
        try {
            int num = memberService.delMember(ids);
            Integer uid=null;
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid=member.getUid();
            }
            if (num > 0) {

                operationlogService.addOperationlog(uid,LocalUtil.get(KafukaTopics.DELETE_PERSONPERMISSION)+memberService.findByNameId(ids));
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS)+ num + LocalUtil.get(KafukaTopics.N_COUNTINFO));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }
    @RequestMapping(value = "/getUser")
    @ApiOperation(value = "中石对接接口",notes = "部门信息")
    /**
     * userCode:用户代码，约定为手机号码。
     * userName:用户姓名
     * isAdmin:是否管理员（1=是,0=否）
     * isDel:是否删除用户（1=是,0=否）
     */
    public Map<String, Object> addLoginMember(String appId,String callTime,String callPwd,String userCode,String userName,String isAdmin,String isDel) {

        Map<String,Object> res=new HashMap<>();
        res.put("callPwd",callPwd);
       try {
           authrise(appId, callTime, callPwd);
           Member member;
           if(isDel.equals("1")){
               member= memberService.findByPhone(userCode);
               if(member!=null)
               memberService.delMember(member.getUid()+"");
               else
                   throw new AuthException("用户不存在");
           }else {
               member= memberService.findByPhone(userCode);
               if(member!=null){
                   if(isAdmin.equals("1"))
                    member.setPermissions(permissionService.findByAll());
                   else
                    member.setPermissions(null);
                   memberService.updateMember(member);
               }else {
                   member=new Member();
                   member.setPassword("123456");
                   member.setEnabled(1);
                   member.setPhone(userCode);
                   member.setMembername(userName);
                   if(isAdmin.equals("1"))
                     //  member.setPermissions(permissionService.findByAll());
                       member.setCid(1);
                   else
                       member.setCid(null);
                   memberService.insertMember(member);
               }
           }
       }catch (Exception e){
             res.put("isOk","0");
             res.put("errMsg",e.getMessage());
             return res;
       }
        res.put("isOk","1");
        res.put("errMsg","操作成功");
      return res;

    }

    public static void main(String[] args) {
        try {
            authrise("aqglxt","1629690474","88FAC13D3DCD100BA91E20789D428E4575D2B1282B40A8123CB182DA31423B63");
        }catch (Exception e){
            System.out.println( e.getMessage());
        }
    }
   public static  void  authrise(String appId,String callTime,String callPwd){
       String appSecret="";
       switch (appId){
           case "aqglxt"://安全管理系统
               appSecret= Coder.KEY_SHA_GUANLIXITONG;
               break;
           case "dwxt"://rtls
               appSecret=Coder.KEY_SHA_RTLS;
               break;
           case "aqdxt"://安全带系统
               appSecret=Coder.KEY_SHA_SAFEBELT;
               break;
           case "aqyxt"://安全眼
               appSecret=Coder.KEY_SHA_ANQUANYAN;
               break;
       }
       String data=callTime+appSecret;
       long current=new Date().getTime()/1000;
       String res = null;
       try {
           res = Coder.SHA256(data);
       } catch (Exception e) {
          throw new AuthException("SHA256异常");
       }
       if(!res.equals(callPwd)){
           System.out.println("密码错误");
           throw new AuthException("密码错误");
       }else if(Math.abs(current-Integer.valueOf(callTime))>300){
           System.out.println("有效期已过");
           throw new AuthException("有效期已过");
       }
    }

    public static  String  url(String text){
        String appSecret="";
        switch (text){
            case "main"://首页
                appSecret="/page/home";
                break;
            case "qxgl"://权限管理
                appSecret="/page/permiss";
                break;
            case "dzdt"://电子地图
                appSecret="/page/map";
                break;
            case "ssdw"://实时定位
                appSecret="/page/tempPos";
                break;
            case "lsgj"://历史轨迹
                appSecret="/page/trackPlayBack";
                break;
            case "dzwl"://电子围栏
                appSecret="/page/area";
                break;
            case "bqdw"://标签定位
                appSecret="page/locationCard";
                break;
            case "ssbj"://实时报警
                appSecret="/page/alarmRecord";
                break;
                default:
                    appSecret="/tologin";
        }
        return appSecret;

    }


    @RequestMapping(value = "/addMember")
    @ApiOperation(value = "权限管理成员新增接口",notes = "部门信息")
    public CommonResult<Integer> addMember(Member member) {
        try {

            if(member.getPhone()==null||!ByteUtils.isPhoneLegal(member.getPhone())){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PHONENUM_ERROR));
            }

            if(member.getPassword()==null||member.getPassword().length()<6){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PASSWORD_LENGTH));
            }
            Member member2 = memberService.findByPhone(member.getPhone());
            if (!NullUtils.isEmpty(member2)) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PHONENUM_EXIST));
            }
            Integer uid=null;
            Member member1=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member1)){
                uid=member1.getUid();
            }
            if (memberService.insertMember(member)) {
                operationlogService.addOperationlog(uid,LocalUtil.get(KafukaTopics.ADD_PERSONPERMISSION)+member.getMembername());
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS), member.getUid());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.ADD_FAIL));
    }


    @RequestMapping(value = "/updateMember")
    @ApiOperation(value = "权限管理成员修改接口",notes = "部门信息")
    public CommonResult<Integer> update(Member member) {
        try {
            if(member.getPhone()==null||!ByteUtils.isPhoneLegal(member.getPhone())){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PHONENUM_ERROR));
            }
            Member member1 = memberService.findById(member.getUid());
            Member member2 = memberService.findByPhone(member.getPhone());
            if (!member1.getPhone().equals(member.getPhone()) && member2 != null) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PHONENUM_EXIST));
            }
            if(member.getPassword()==null||member.getPassword().length()<6){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PASSWORD_LENGTH));
            }
            Integer uid=null;
            Member member3=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member3)){
                uid=member3.getUid();
            }
            if (memberService.updateMember(member)) {
                operationlogService.addOperationlog(uid,LocalUtil.get(KafukaTopics.UPDATE_PERSONPERMISSION)+member.getMembername());
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS), member.getUid());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequestMapping(value = "/getCurrentMember")
    @ApiOperation(value = "修改当前人员信息",notes = "人员信息")
    public CommonResult<Object> getCurrentUser( ) {
        try {
            Member sysUser = (Member) SecurityUtils.getSubject().getPrincipal();
            Map<String, Object> map = new HashMap<>();
            map.put("status", true);
            map.put("msg", LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            map.put("list",  sysUser);


            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    /*
    * 接收报警信息
    * */
    @RequestMapping(value = "/api/getAlert")
    @ApiOperation(value = "开发模式开启接口",notes = "")
    public Object getAlert(SinopecWarnData sinopecWarnData){
        Map<String,Object> res=new HashMap<>();
        res.put("callPwd",sinopecWarnData.getCallPwd());
        try {
            authrise(sinopecWarnData.getAppId(), sinopecWarnData.getCallTime(), sinopecWarnData.getCallPwd());
            JSONObject object = new JSONObject();
            JSONObject objectdata = new JSONObject();
            objectdata.put("counttype", 2);
            objectdata.put("count", "");
            object.put("type", 3);//定位数据传输 1定位数据 2报警数据 3.中石化其他系统预警
            object.put("data", objectdata.toString());
            object.put("map", null);
            webSocketLocation.sendAll(object.toString());

        }catch (Exception e){
            res.put("isOk","0");
            res.put("errMsg",e.getMessage());
            return res;
        }
        res.put("isOk","1");
        res.put("errMsg","");
        return res;
    }

    /*
     * 接收报警信息
     * */
    @RequestMapping(value = "/api/getOptAlert")
    @ApiOperation(value = "开发模式开启接口",notes = "")
    public Object getAlert(SinopecWarnOptData sinopecWarnOptData){
        Map<String,Object> res=new HashMap<>();
      //  res.put("callPwd",sinopecWarnOptData.getCallPwd());
        try {
       //     authrise(sinopecWarnOptData.getAppId(), sinopecWarnOptData.getCallTime(), sinopecWarnOptData.getCallPwd());
          /*  JSONObject object = new JSONObject();
            JSONObject objectdata = new JSONObject();
            objectdata.put("counttype", 2);
            objectdata.put("count", "");
            object.put("type", 3);//定位数据传输 1定位数据 2报警数据 3.中石化其他系统预警
            object.put("data", objectdata.toString());
            object.put("map", null);
            webSocketLocation.sendAll(object.toString());*/
            httpClientService.sendWarningToSinopec(null,null);


        }catch (Exception e){
            res.put("isOk","0");
            res.put("errMsg",e.getMessage());
            return res;
        }
        res.put("isOk","1");
        res.put("errMsg","");
        return res;
    }



}
