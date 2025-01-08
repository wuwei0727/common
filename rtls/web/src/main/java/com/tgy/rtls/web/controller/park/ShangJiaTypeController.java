package com.tgy.rtls.web.controller.park;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.ShangJiaType;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.park.impl.ShangJiaTypeService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.util.FileUtils;
import com.tgy.rtls.web.util.StrUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.commons.io.FilenameUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.park
 * @Author: wuwei
 * @CreateTime: 2024-11-12 09:46
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping(value = "/park")
public class ShangJiaTypeController {

    @Autowired
    private ShangJiaTypeService service;
    @Autowired
    private FastFileStorageClient fastFileStorageClient;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Value("${file.uploadFolder}")
    private String uploadFolder;
    private final String url="http://112.94.22.123:10087/rtls/UWB/";


    @RequestMapping(value = "/getShangjiaType")
    @ApiOperation(value = "获取车位信息", notes = "111")
    public CommonResult<Object> getShangJiaType(String name,@RequestParam(value = "desc", defaultValue = "id desc") String desc, Integer pageIndex,Integer pageSize) {
        try {
            if(pageSize==-1){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS), service.list());
            }
            PageHelper.startPage(pageIndex, pageSize);
            List<ShangJiaType> data = service.list(new QueryWrapper<ShangJiaType>().eq(!NullUtils.isEmpty(name),"name",name).orderByDesc("id"));
            PageInfo<ShangJiaType> pageInfo = new PageInfo<>(data);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS), result);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


    @PostMapping("/addShangJiangType")
    @RequiresPermissions("sjt:add")
    public CommonResult<Object> addShangJiangType(ShangJiaType shangJiaType, MultipartFile file, HttpServletRequest request) {
        try {
            ShangJiaType one = service.getOne(new QueryWrapper<ShangJiaType>().eq(!NullUtils.isEmpty(shangJiaType.getName()), "name", shangJiaType.getName()));
            if(!NullUtils.isEmpty(one)){
                return new CommonResult<>(500,LocalUtil.get(KafukaTopics.NAME_EXIST));
            }
            StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
            CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, file,storePath.getFullPath());
            if (commonResult.getCode()==200) {
                shangJiaType.setUrl(url+ commonResult.getData());
            }
            service.save(shangJiaType);
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.SJT)), now);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        } catch (IOException e) {
            e.getStackTrace();
            log.error(e.getMessage());
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @DeleteMapping("/delShangJiangType/{ids}")
    @RequiresPermissions("sjt:del")
    public CommonResult<Object> delShangJiangType(@PathVariable String ids, HttpServletRequest request) {
        boolean b = service.removeBatchByIds(StrUtils.convertStringToList(ids));
        LocalDateTime now = LocalDateTime.now();
            if (b) {
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.SJT)), now);
            }
        return new CommonResult<>(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
    }

    @PostMapping("/getShangJiangTypeById/{id}")
    @RequiresPermissions(value = {"sjt:see","sjt:edit"},logical = Logical.OR)
    public CommonResult<Object> getShangJiangTypeById(@PathVariable String id) {
        ShangJiaType shangJiaType = service.getById(id);
        return new CommonResult<>(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS),shangJiaType);
    }

    @PostMapping("/updateShangJiangType")
    @RequiresPermissions("sjt:edit")
    public CommonResult<Object> updateShangJiangType(ShangJiaType shangJiaType, MultipartFile file, HttpServletRequest request) {
        try {
            ShangJiaType one = service.getOne(new QueryWrapper<ShangJiaType>().eq(!NullUtils.isEmpty(shangJiaType.getName()), "name", shangJiaType.getName()).ne("id", shangJiaType.getId()));
            if(!NullUtils.isEmpty(one)){
                return new CommonResult<>(500,LocalUtil.get(KafukaTopics.NAME_EXIST));
            }
            if(!NullUtils.isEmpty(file)){
                StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
                CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, file,storePath.getFullPath());
                if (commonResult.getCode()==200) {
                    shangJiaType.setUrl(url+ commonResult.getData());
                }
            }
            if(shangJiaType.getPhone()!=null){
                shangJiaType.setUrl(shangJiaType.getPhone());
            }
            service.updateById(shangJiaType);
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.SJT)), now);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
        } catch (IOException e) {
            e.getStackTrace();
            log.error(e.getMessage());
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
}
