package com.tgy.rtls.web.controller.promoter;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.promoter.PromoterQrCode;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.promoter.PromoterQrCodeService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
import com.tgy.rtls.web.config.SpringContextHolder;
import com.tgy.rtls.web.util.PromoterQrCodeUtil;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/promoter_qr_code")
public class PromoterQrCodeController {
    @Autowired
    private PromoterQrCodeService promoterQrCodeService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;

    @MyPermission
    @RequestMapping(value = "getPromoterQrCodeInfo")
    public CommonResult<Object> getPromoterQrCodeInfo(String name,String shangjiaName, Integer map, Integer pageIndex,Integer pageSize,@RequestParam(defaultValue = "pqc.id desc") String desc, String maps) {
        try {
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            if(pageSize!=-1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<PromoterQrCode> data = promoterQrCodeService.getPromoterQrCodeInfo(name,shangjiaName, map,desc,mapids);
            PageInfo<PromoterQrCode> pageInfo = new PageInfo<>(data);
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


    @RequiresPermissions("pqc:add")
    @RequestMapping(value = "addPromoterQrCode")
    public CommonResult<Object> addPromoterQrCode(@RequestBody PromoterQrCode promoterQrCode, HttpServletRequest request) {
        try {
            String uid = "12";

            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            promoterQrCode.setCreateTime(new Date());
            PromoterQrCode one = promoterQrCodeService.getOne(new QueryWrapper<PromoterQrCode>()
                    .eq("map", promoterQrCode.getMap())
                    .eq("dest_id", promoterQrCode.getDestId())
                    .eq("pro_info_id", promoterQrCode.getProInfoId()));
            if(!NullUtils.isEmpty(one)){
                return new CommonResult<>(500, LocalUtil.get("当前推广人下已有重复商家"));
            }
            promoterQrCodeService.insert(promoterQrCode);
            String mapQrCode = PromoterQrCodeUtil.getPromoterQrCode(promoterQrCode.getId(),promoterQrCode.getMap());
            File files = new File(mapQrCode);//获取二维码路径
            InputStream inputStream = Files.newInputStream(files.toPath());
            FastFileStorageClient fastFileStorageClient = SpringContextHolder.getBean(FastFileStorageClient.class);

            StorePath storePath = fastFileStorageClient.uploadFile(inputStream, files.length(), "png", null);
            promoterQrCode.setQrcode(storePath.getFullPath());
            String imgPath = StringUtils.substringAfter(files.getPath(), "rtls");
            if (imgPath.contains("\\")) {
                imgPath = imgPath.replace("\\", "/");
            }
            promoterQrCode.setQrcodelocal("/rtls"+imgPath);
            promoterQrCodeService.updateQrCodeById(promoterQrCode);

            LocalDateTime now = LocalDateTime.now();

            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.PRO_CODE_INFO)), now);

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions("pqc:edit")
    @RequestMapping(value = "updatePromoterQrCode")
    public CommonResult<Object> updatePromoterQrCode(@RequestBody PromoterQrCode promoterQrCode, HttpServletRequest request) {
        try {
            String uid = "12";

            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            promoterQrCode.setUpdateTime(new Date());
            PromoterQrCode one = promoterQrCodeService.getOne(new QueryWrapper<PromoterQrCode>().eq("map", promoterQrCode.getMap())
                    .eq("dest_id", promoterQrCode.getDestId())
                    .eq("pro_info_id", promoterQrCode.getProInfoId())
                    .ne("id", promoterQrCode.getId()));
            if(!NullUtils.isEmpty(one)){
                return new CommonResult<>(500, LocalUtil.get("当前推广人下已有重复商家"));
            }
            promoterQrCodeService.updateByPrimaryKey(promoterQrCode);

            String mapQrCode = PromoterQrCodeUtil.getPromoterQrCode(promoterQrCode.getId(),promoterQrCode.getMap());
            File files = new File(mapQrCode);//获取二维码路径
            InputStream inputStream = Files.newInputStream(files.toPath());
            FastFileStorageClient fastFileStorageClient = SpringContextHolder.getBean(FastFileStorageClient.class);

            StorePath storePath = fastFileStorageClient.uploadFile(inputStream, files.length(), "png", null);
            promoterQrCode.setQrcode(storePath.getFullPath());
            String imgPath = StringUtils.substringAfter(files.getPath(), "rtls");
            if (imgPath.contains("\\")) {
                imgPath = imgPath.replace("\\", "/");
            }
            promoterQrCode.setQrcodelocal("/rtls"+imgPath);
            promoterQrCodeService.updateQrCodeById(promoterQrCode);

            LocalDateTime now = LocalDateTime.now();

            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.PRO_CODE_INFO)), now);

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.EDIT_SUCCESS));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequiresPermissions(value = {"pqc:see","pqc:edit"},logical = Logical.OR)
    @GetMapping("selectOne")
    public CommonResult<Object> selectOne(Integer id) {
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),promoterQrCodeService.selectByPrimaryKey(id));
    }

    @RequiresPermissions({"pqc:del"})
    @RequestMapping(value = "/delPromoterQrCode/{ids}")
    public CommonResult<Object> delPromoterQrCode(@PathVariable("ids") String ids, HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            promoterQrCodeService.deleteByIdIn(ids.split(","));
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.PRO_CODE_INFO)), now);

            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }




}
