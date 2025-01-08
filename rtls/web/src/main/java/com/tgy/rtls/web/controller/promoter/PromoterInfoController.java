package com.tgy.rtls.web.controller.promoter;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.promoter.PromoterInfo;
import com.tgy.rtls.data.entity.promoter.PromoterLog;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.promoter.PromoterInfoService;
import com.tgy.rtls.data.service.promoter.PromoterLogService;
import com.tgy.rtls.data.tool.IpUtil;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/promoterInfo/")
public class PromoterInfoController {

    @Autowired
    private PromoterInfoService promoterInfoService;
    @Autowired
    private PromoterLogService promoterLogService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;

    // @MyPermission
    @RequestMapping(value = "getPromoterInfo")
    public CommonResult<Object> getPromoterInfo(String name, Integer map, String province,String city,String area, Integer pageIndex,Integer pageSize,
                                                @RequestParam(defaultValue = "pi.id desc") String desc) {
        try {
            // String[] mapids = null;
            // if (!NullUtils.isEmpty(maps)) {
            //     mapids = maps.split(",");
            // }
            if(pageSize!=-1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<PromoterInfo> data = promoterInfoService.getPromoterInfo(name, map, province, city, area,desc);
            PageInfo<PromoterInfo> pageInfo = new PageInfo<>(data);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions("po:add")
    @RequestMapping(value = "addPromoterInfo")
    public CommonResult<Object> addPromoterInfo(@RequestBody PromoterInfo promoterInfo, HttpServletRequest request) {
        try {
            String uid = "12";

            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            promoterInfo.setCreateTime(LocalDateTime.now());
            promoterInfoService.insert(promoterInfo);
            LocalDateTime now = LocalDateTime.now();

            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.PRO_INFO)), now);

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions("po:edit")
    @RequestMapping(value = "updatePromoterInfo")
    public CommonResult<Object> updatePromoterInfo(@RequestBody PromoterInfo promoterInfo, HttpServletRequest request) {
        try {
            String uid = "12";

            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            promoterInfo.setUpdateTime(LocalDateTime.now());
            promoterInfoService.updateByPrimaryKey(promoterInfo);
            LocalDateTime now = LocalDateTime.now();

            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.PRO_INFO)), now);

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.EDIT_SUCCESS));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequiresPermissions(value = {"po:see","po:edit"},logical = Logical.OR)
    @GetMapping("selectOne")
    public CommonResult<Object> selectOne(Integer id) {
        QueryWrapper<PromoterLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pro_info_id", id);
        List<PromoterLog> promoterQrCode = promoterLogService.list(queryWrapper);
        PromoterInfo promoterInfo =promoterInfoService.selectByPrimaryKey(id);
        promoterInfo.setPromoterCount(promoterQrCode);
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),promoterInfo);
    }

    @RequiresPermissions({"po:del"})
    @RequestMapping(value = "/delPromoterInfo/{ids}")
    public CommonResult<Object> delPromoterInfo(@PathVariable("ids") String ids, HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            promoterInfoService.deleteByIdIn(ids.split(","));
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.PRO_INFO)), now);

            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

}
