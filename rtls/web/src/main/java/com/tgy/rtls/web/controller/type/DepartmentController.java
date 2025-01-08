package com.tgy.rtls.web.controller.type;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.type.Department;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.mapper.type.DepartmentMapper;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.type.DepartmentService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.type
 * @date 2020/10/15
 * 部门管理类
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/department")
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private DepartmentMapper departmentMapper;

    @RequestMapping(value = "/getDepartmentSel")
    @ApiOperation(value = "部门查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getDepartmentSel(@RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
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
                List<Department> departmentList = departmentService.findByAll(instanceid);
                return new CommonResult<Object>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),departmentList);
            }
            /*
             * 分页 num-->总数量
             * */
            int num= departmentService.findByAll(instanceid).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                } else {
                    pageIndex = num / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<Department> departmentList = departmentService.findByAll(instanceid);
            PageInfo<Department> pageInfo=new PageInfo<>(departmentList);
            Map<String,Object> map=new HashMap<>();
            map.put("list",pageInfo.getList());
            map.put("pageIndex", pageIndex);
            map.put("total", pageInfo.getTotal());
            map.put("pages", pageInfo.getPages());
            return new CommonResult<Object>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),map);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getDepartmentId/{id}")
    @ApiOperation(value = "部门详情接口",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "id",value = "部门id",required = true,dataType = "int")
    public CommonResult<Department> getDepartmentId(@PathVariable("id")Integer id){
        try {
            Department department=departmentService.findById(id);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),department);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/addDepartment")
    @ApiOperation(value = "部门新增接口",notes = "部门信息")

    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "name",value = "部门名",required = true,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "worksystem",value = "部门工作制度",required = true,dataType = "int")
    })
    public CommonResult<Integer> getDepartmentId(Department department){
        try {
            //实例
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            if (department.getName()==null||department.getName().trim().isEmpty()){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.DEPARTMNET_EMPTY));
            }

            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            List<Department> list = departmentMapper.findByName(instanceid, department.getName().trim());
            if(list!=null&&list.size()>0){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.DEPARTMNET_EXIST));
            }
            department.setInstanceid(instanceid);
            if (departmentService.addDepartment(department)){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS),department.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<Integer>(500,e.getMessage());
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequestMapping(value = "/updateDepartment")
    @ApiOperation(value = "部门修改接口",notes = "部门信息")

    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "id",value = "部门id",required = true,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "name",value = "部门名",required = true,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "worksystem",value = "部门工作制度",required = true,dataType = "int")
    })
    public CommonResult updateDepartment(Department department){
        try {
            //实例
            String uid="12";
            if (department.getName()==null||department.getName().trim().isEmpty()){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.DEPARTMNET_EMPTY));
            }
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            if (department.getName()==null||department.getName().trim().isEmpty()){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.DEPARTMNET_EMPTY));
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            List<Department> list = departmentMapper.findByName(instanceid, department.getName().trim());
            if(list!=null&&list.size()>1||list.size()==1&&list.get(0).getId().intValue()!=department.getId().intValue()){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.DEPARTMNET_EXIST));
            }
            department.setInstanceid(instanceid);
            if (departmentService.updateDepartment(department)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult(500,e.getMessage());
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequestMapping(value = "/delDepartment/{ids}")
    @ApiOperation(value = "部门删除接口",notes = "部门id集")
    @ApiImplicitParam(paramType = "path",name = "ids",value = "部门id集",required = true,dataType = "String")
    public CommonResult delDepartment(@PathVariable("ids")String ids){
        try {
            if (departmentService.delDepartment(ids)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult(500,e.getMessage());
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

}
