// package com.tgy.rtls.web.controller.equip;
//
// import com.github.pagehelper.PageHelper;
// import com.github.pagehelper.PageInfo;
// import com.tgy.rtls.data.common.KafukaTopics;
// import com.tgy.rtls.data.common.LocalUtil;
// import com.tgy.rtls.data.common.NullUtils;
// import com.tgy.rtls.data.entity.common.CommonResult;
// import com.tgy.rtls.data.entity.equip.Camera;
// import com.tgy.rtls.data.entity.user.Member;
// import com.tgy.rtls.data.service.common.OperationlogService;
// import com.tgy.rtls.data.service.common.RedisService;
// import com.tgy.rtls.data.service.equip.CameraService;
// import io.swagger.annotations.ApiImplicitParam;
// import io.swagger.annotations.ApiImplicitParams;
// import io.swagger.annotations.ApiOperation;
// import org.apache.shiro.SecurityUtils;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.kafka.core.KafkaTemplate;
// import org.springframework.web.bind.annotation.*;
//
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
//
// /**
//  * @author 许强
//  * @Package com.tgy.rtls.web.controller.equip
//  * @date 2020/12/23
//  * uwb网关管理
//  */
// @RestController
// @CrossOrigin
// @RequestMapping(value = "/camera")
// public class CameraController {
//     @Autowired
//     private CameraService cameraService;
//     @Autowired
//     private OperationlogService operationlogService;
//     @Autowired
//     private RedisService redisService;
//     @Autowired(required = false)
//     private KafkaTemplate<String, String> kafkaTemplate;
//
//
//     @RequestMapping(value = "/getCameraSel")
//     @ApiOperation(value = "摄像头查看",notes = "无")
//     @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = false,dataType = "int"),
//             @ApiImplicitParam(paramType = "query",name = "name",value = "网关名",required = false,dataType = "string"),
//             @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
//             @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
//     })
//     public CommonResult<Object> getGatewaySel(Integer map,String name,Integer relevance,
//                                           @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
//                                           @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
//         try {
//             String uid="12";
//             Member member=(Member) SecurityUtils.getSubject().getPrincipal();
//             if(!NullUtils.isEmpty(member)){
//                 uid= String.valueOf(member.getUid());
//             }
//             int instanceid= Integer.parseInt(redisService.get("instance"+uid));
//             //按条件查询
//             //pageSize<0时查询所有
//             if (pageSize<0){
//                 List<Camera> gateway_uwbs=cameraService.findByAll(instanceid,map,name);
//                 return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),gateway_uwbs);
//             }
//             /*
//              * 分页 total-->总数量
//              * */
//             int total=cameraService.findByAll(instanceid,map,name).size();
//             if (pageIndex > total / pageSize) {
//                 if (total % pageSize == 0) {
//                     pageIndex = total / pageSize;
//                 } else {
//                     pageIndex = total / pageSize + 1;
//                 }
//             }
//             PageHelper.startPage(pageIndex,pageSize);
//             List<Camera> gateway_uwbs=cameraService.findByAll(instanceid,map,name);
//             PageInfo<Camera> pageInfo=new PageInfo<>(gateway_uwbs);
//             Map<String,Object> result=new HashMap<>();
//             result.put("list",pageInfo.getList());
//             result.put("pageIndex", pageIndex);
//             result.put("total", pageInfo.getTotal());
//             result.put("pages", pageInfo.getPages());
//             //生成操作日志-->查询分站数据
//             operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.QUERY_CAMERA));
//             return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
//         }catch (Exception e){
//             e.printStackTrace();
//             return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
//         }
//     }
//
//     @RequestMapping(value = "/getCameraId/{id}")
//     @ApiOperation(value = "摄像头详情接口",notes = "无")
//     @ApiImplicitParam(paramType = "path",name = "id",value = "网关id",required = true,dataType = "int")
//     public CommonResult<Camera> getCameraId(@PathVariable("id")Integer id){
//         try {
//             Camera gateway_uwb=cameraService.findById(id);
//             return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),gateway_uwb);
//         }catch (Exception e){
//             e.printStackTrace();
//             return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
//         }
//     }
//
//     @RequestMapping(value = "/addCamera")
//     @ApiOperation(value = "添加摄像头",notes = "摄像头信息")
//     public CommonResult<Object> addCamera(Camera camera){
//         try {
//             String uid="12";
//             Member member=(Member) SecurityUtils.getSubject().getPrincipal();
//             if(!NullUtils.isEmpty(member)){
//                 uid= String.valueOf(member.getUid());
//             }
//
//
//             int instanceid= Integer.parseInt(redisService.get("instance"+uid));
//             Camera res = cameraService.findByName(camera.getName(), instanceid);
//            if(res!=null){
//                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ID_EXIST),camera.getId());
//            }
//             camera.setInstanceid(instanceid);
//             if (cameraService.addCamera(camera)){
//                 //生成操作日志-->添加分站数据
//                 operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.ADD_CAMERA)+camera.getName());
//                 return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS),camera.getId());
//             }
//
//         }catch (Exception e){
//             e.printStackTrace();
//             return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
//         }
//         return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ADD_FAIL));
//     }
//
//     @RequestMapping(value = "/updateCamera")
//     @ApiOperation(value = "修改camera",notes = "摄像头信息")
//     public CommonResult<Object> updateCamera(Camera camera){
//         try {
//             String uid="12";
//             Member member=(Member) SecurityUtils.getSubject().getPrincipal();
//             if(!NullUtils.isEmpty(member)){
//                 uid= String.valueOf(member.getUid());
//             }
//             int instanceid= Integer.parseInt(redisService.get("instance"+uid));
//             Camera res = cameraService.findByName(camera.getName(), instanceid);
//             //name重复判断
//                    if(res!=null&&res.getId()!=camera.getId()){
//                        return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ID_EXIST),camera.getId());
//                    }
//
//             if (cameraService.updateCamera(camera)){
//                 //生成操作日志-->添加分站数据
//                 operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.UDPATE_CAMERA)+camera.getName());
//
//                 return new CommonResult<>(200,LocalUtil.get(KafukaTopics.SAVE_SUCCESS),camera.getId());
//             }
//
//         }catch (Exception e){
//             e.printStackTrace();
//             return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
//         }
//         return new CommonResult<>(400,LocalUtil.get(KafukaTopics.SAVE_FAIL));
//     }
//
//     @RequestMapping(value = "/delCamera/{ids}")
//     @ApiOperation(value = "camera删除接口",notes = "camera集")
//     @ApiImplicitParam(paramType = "path",name = "ids",value = "uwb网关id集",required = true,dataType = "String")
//     public CommonResult delSub(@PathVariable("ids")String ids){
//         try {
//             String uid="12";
//             Member member=(Member) SecurityUtils.getSubject().getPrincipal();
//             if(!NullUtils.isEmpty(member)){
//                 uid= String.valueOf(member.getUid());
//             }
//             if (cameraService.delCamera(ids.split(","))){
//                 operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.DELETE_CAMERA));
//                 return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
//             }
//         }catch (Exception e){
//             e.printStackTrace();
//             return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
//         }
//         return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
//     }
//
//
// }
