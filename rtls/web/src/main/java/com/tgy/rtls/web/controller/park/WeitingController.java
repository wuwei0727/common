package com.tgy.rtls.web.controller.park;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.WeiTing;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.web.aspect.MyPermission;
import com.tgy.rtls.web.util.FileUtils;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.FilenameUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping(value = "/park")
/**
 * 通道违停
 */
public class WeitingController {
    @Autowired
    private ParkingService parkingService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private FastFileStorageClient fastFileStorageClient;
    @Value("${file.url}")
    public String url;
    @Value("${file.uploadFolder}")
    private String uploadFolder;

    @MyPermission
    @RequestMapping(value = "/getWeiTing")
    @ApiOperation(value = "获取车位信息", notes = "111")
    public CommonResult<Object> getWeiTing(String license, Integer map, Integer state, String start, String end, Integer pageIndex, Integer pageSize, String maps) {
        try {
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            if (license !=  null) {
                license = license.toUpperCase();
            }
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            String instanceid = redisService.get("instance" + uid);
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            if(!NullUtils.isEmpty(start)){
                if (start.compareTo(end) > 0) {
                    return new CommonResult<>(400, LocalUtil.get("开始时间不能大于结束时间!!!"));
                }
            }
            List<WeiTing> data = parkingService.findByAllWeiTing2(null, license, start, end, map, state, instanceid, pageIndex, pageSize, mapids);
            PageInfo<WeiTing> pageInfo = new PageInfo<>(data);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());

            if (pageSize != null) {
                res.setData(result);
            } else {
                res.setData(data);
            }
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions({"violate:edit"})
    @RequestMapping(value = "/getWeiTingById/{id}")
    @ApiOperation(value = "获取车位信息", notes = "111")
    public CommonResult<Object> getPlace(@PathVariable("id") Integer id) {
        try {
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            String instanceid = redisService.get("instance" + uid);
            ;
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            List<WeiTing> data = parkingService.findByAllWeiTing(id, null, null, null, null, null, null, null, null);
            res.setData(data.size() == 0 ? null : data.get(0));
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions({"violate:add"})
    @RequestMapping(value = "/addViolate")
    @ApiOperation(value = "添加车位信息", notes = "111")
    public CommonResult<Object> addViolate(WeiTing weiTing, MultipartFile file) {
        try {

            if (!NullUtils.isEmpty(file) && !file.isEmpty()) {
                StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
                weiTing.setPhoto(storePath.getFullPath());
                String fullPath = storePath.getFullPath();
                CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, file, fullPath);
                if (commonResult.getCode() == 200) {
                    weiTing.setPhotolocal(url + String.valueOf(commonResult.getData()));
                }
            }
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
//            parkingService.save(weiTing);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequiresPermissions({"violate:del"})
    @RequestMapping(value = "/delViolate/{id}")
    @ApiOperation(value = "删除通道违停", notes = "111")
    public CommonResult<Object> delViolate(@PathVariable("id") String id) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            parkingService.delViolate(id);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

}
