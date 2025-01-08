package com.tgy.rtls.web.controller.park.floorLock;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.ParkingCompanyVo;
import com.tgy.rtls.data.entity.park.floorLock.CarPlate;
import com.tgy.rtls.data.entity.park.floorLock.UserCompanyMap;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.park.floorLock.impl.CarPlateService;
import com.tgy.rtls.data.service.park.floorLock.impl.UserCompanyMapService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
import com.tgy.rtls.web.util.StrUtils;
import io.swagger.annotations.ApiOperation;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/carPlate")
public class CarPlateController {

    @Resource
    private CarPlateService carPlateService;
    @Resource
    private UserCompanyMapService userCompanyMapService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private ParkMapper parkMapper;

    @MyPermission
    @RequestMapping(value = "/getCarPlate")
    @ApiOperation(value = "获取公司名", notes = "111")
    public CommonResult<Object> getCarPlate(Integer map, String companyId, String plateNumber, String phone, Integer pageIndex, Integer pageSize,
                                            @RequestParam(value = "desc", defaultValue = "id desc") String desc, String maps) {
        try {
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }

            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<CarPlate> data = carPlateService.getCarPlate(map, companyId, plateNumber, phone, desc, mapids);
            List<ParkingCompanyVo> mapComName = parkMapper.getAllMap(mapids);
            PageInfo<CarPlate> pageInfo = new PageInfo<>(data);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            result.put("mapComName", mapComName);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), result);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @PostMapping("addCarPlate")
    @RequiresPermissions("cp:add")
    public CommonResult<Object> addCarPlate(@RequestBody CarPlate carPlate, HttpServletRequest request) {
        try {
//            CarPlate one = carPlateService.getOne(new QueryWrapper<CarPlate>().eq("plate_number", carPlate.getPlateNumber()).eq("phone_number", carPlate.getPhoneNumber()).eq("map_id", carPlate.getMapId()));
//            if (!NullUtils.isEmpty(one)){
//                return new CommonResult<>(500, LocalUtil.get("手机号或车牌号重复添加,请重新确认！！！"));
//            }
            long cpCount = parkMapper.getPlaceCountByCompany(carPlate.getMapId(), carPlate.getCompanyId());
            long carCount = carPlateService.count(new QueryWrapper<CarPlate>()
                    .eq("map_id", carPlate.getMapId())
                    .eq("company_id", carPlate.getCompanyId()));
            if(carCount>=cpCount){
               return new CommonResult<>(400,String.format("当前 %s 下的车牌超限,请检查公司管理分配的车位数量！！！",carPlate.getCompanyName()));
            }
            if(carPlateService.save(carPlate)){
                LocalDateTime now = LocalDateTime.now();
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.cp)), now);
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @GetMapping("/getCarPlateById/{id}")
    @RequiresPermissions(value = {"cp:see","cp:edit"},logical = Logical.OR)
    public CommonResult<Object> getCarPlateById(@PathVariable("id") Integer id) {
        try {
            CarPlate carPlate = carPlateService.getCarPlateById(id);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),carPlate);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @PutMapping("/editCarPlate")
    @RequiresPermissions("cp:edit")
    public CommonResult<Object> editCarPlate(@RequestBody CarPlate carPlate, HttpServletRequest request) {
        try {
//            CarPlate one = carPlateService.getOne(new QueryWrapper<CarPlate>().ne("id",carPlate.getId()).eq("plate_number", carPlate.getPlateNumber()).eq("phone_number", carPlate.getPhoneNumber()).eq("map_id", carPlate.getMapId()));
//            if (!NullUtils.isEmpty(one)){
//                return new CommonResult<>(500, LocalUtil.get("手机号或车牌号重复添加,请重新确认！！！"));
//            }
            long cpCount = parkMapper.getPlaceCountByCompany(carPlate.getMapId(), carPlate.getCompanyId());
            long carCount = carPlateService.count(new QueryWrapper<CarPlate>()
                    .eq("map_id", carPlate.getMapId())
                    .eq("company_id", carPlate.getCompanyId())
                    .ne("id",carPlate.getId()));
            if(carCount>=cpCount){
                return new CommonResult<>(400,String.format("当前 %s 下的车牌超限,请检查公司管理分配的车位数量！！！",carPlate.getCompanyName()));
            }
            if(carPlateService.updateById(carPlate)){
                LocalDateTime now = LocalDateTime.now();
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.cp)), now);
            }

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @DeleteMapping("/delCarPlateById/{ids}")
    @RequiresPermissions("cp:del")
    public CommonResult<Object> delCarPlateById(@PathVariable("ids") String ids, HttpServletRequest request) {
        try {
            List<Integer> list = StrUtils.convertStringToList(ids);
            if(carPlateService.removeBatchByIds(list)){
                for (Integer id : list) {
                    UserCompanyMap one = userCompanyMapService.getOne(new QueryWrapper<UserCompanyMap>().eq("license_plate_id", id));
                    if(!NullUtils.isEmpty(one)){
                        userCompanyMapService.removeById(one.getId());
                    }
                }

                LocalDateTime now = LocalDateTime.now();
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.cp)), now);
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

}
