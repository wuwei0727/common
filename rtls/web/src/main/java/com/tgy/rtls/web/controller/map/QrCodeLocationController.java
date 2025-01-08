package com.tgy.rtls.web.controller.map;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.map.QrCodeLocation;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.map.impl.QrCodeLocationService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
import com.tgy.rtls.web.util.StrUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/qrCodeLocation/")
public class QrCodeLocationController {

    @Resource
    private QrCodeLocationService qrCodeLocationService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private FastFileStorageClient fastFileStorageClient;
    @Autowired
    private WxMaService wxMaService;
    @Value("${file.uploadFolder}")
    private  String uploadFolder;

    @MyPermission
    @GetMapping("getAllQrCodeLocationOrConditionQuery")
    public CommonResult<Object> getAllQrCodeLocationOrConditionQuery(String map, String areaName, String floorName,Integer pageIndex, Integer pageSize, @RequestParam(value = "desc", defaultValue = "create_time desc") String desc, String maps) {
        try {
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }

            List<QrCodeLocation> data = qrCodeLocationService.getAllQrCodeLocationOrConditionQuery(map,areaName,floorName,desc,mapids);
            PageInfo<QrCodeLocation> pageInfo = new PageInfo<>(data);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        } catch (Exception e) {
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.SYSTEM_BUSY),e);
        }
    }


    @RequiresPermissions({"qcr:add"})
    @PostMapping(value = "/addQrCodeLocation")
    public CommonResult<Object> addQrCodeLocation(@RequestBody QrCodeLocation qrCodeLocation, HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();


            qrCodeLocationService.save(qrCodeLocation);

            File file = wxMaService.getQrcodeService().createWxaCodeUnlimit("m=" + qrCodeLocation.getMap()+"&t=1&id="+qrCodeLocation.getId(),"pages/map/map", uploadFolder,false, "trial", 430, true, null, false);
            StorePath storePath = fastFileStorageClient.uploadFile(Files.newInputStream(file.toPath()), file.length(), "png", null);
            String fullPath = storePath.getFullPath();
            qrCodeLocation.setQrCodeUrl(fullPath);

            qrCodeLocationService.updateById(qrCodeLocation);
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.QCL)), now);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, LocalUtil.get(KafukaTopics.ADD_FAIL));
        }
    }

    @RequiresPermissions({"qcr:del"})
    @DeleteMapping(value = "delQrCodeLocation/{ids}")
    public CommonResult<Object> delQrCodeLocation(@PathVariable("ids") String ids, HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            qrCodeLocationService.removeBatchByIds(StrUtils.convertStringToList(ids));
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.QCL)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"qcr:see","qcr:edit"},logical = Logical.OR)
    @RequestMapping(value = "getQrCodeLocationById/{id}")
    @ApiOperation(value = "获取车位出入口信息", notes = "111")
    public CommonResult<Object> getQrCodeLocationById(@PathVariable("id") Integer id) {
        try {
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),qrCodeLocationService.getQrCodeLocationById(id));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"qcr:edit"})
    @RequestMapping(value = "editQrCodeLocation")
    public CommonResult<Object> editMapPathLabel(@RequestBody QrCodeLocation qrCodeLocation,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();

            File file = wxMaService.getQrcodeService().createWxaCodeUnlimit("m=" + qrCodeLocation.getMap()+"&t=1&id="+qrCodeLocation.getId(), "pages/map/map", uploadFolder,false, "trial", 430, true, null, false);
            StorePath storePath = fastFileStorageClient.uploadFile(Files.newInputStream(file.toPath()), file.length(), "png", null);
            String fullPath = storePath.getFullPath();
            qrCodeLocation.setQrCodeUrl(fullPath);

            qrCodeLocationService.updateById(qrCodeLocation);
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.QCL)), now);

            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, LocalUtil.get(KafukaTopics.ADD_FAIL));
        }
    }

}
