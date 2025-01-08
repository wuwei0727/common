package com.tgy.rtls.web.controller.map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.map.Maptheme;
import com.tgy.rtls.data.entity.map.Style;
import com.tgy.rtls.data.entity.park.ParkingCompanyVo;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.mapper.map.Map2dMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.map.Map2dService;
import com.tgy.rtls.data.service.map.MapthemeService;
import com.tgy.rtls.data.service.park.ShowScreenConfigService;
import com.tgy.rtls.data.service.user.impl.MemberService;
import com.tgy.rtls.data.service.vip.FloorLockService;
import com.tgy.rtls.data.service.vip.VipAreaService;
import com.tgy.rtls.data.service.vip.VipParkingService;
import com.tgy.rtls.web.aspect.MyPermission;
import com.tgy.rtls.web.util.FileUtils;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.util.WxQrCode;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.commons.io.FilenameUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.map
 * @date 2020/10/19
 * 地图管理类
 */
@RestController
@RequestMapping(value = "/map")
@CrossOrigin
/**
 * 地图管理
 */
public class MapController {
    @Autowired
    private Map2dService map2dService;
    @Autowired
    private Map2dMapper map2dMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private FastFileStorageClient fastFileStorageClient;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MapthemeService mapthemeService;
    @Value("${file.url}")
    private String url;
    //上传真实地址
    @Value("${file.uploadFolder}")
    private String uploadFolder;
    @Value("${fdfs.url}")
    private String fdfsUrl;
    @Value("${websocket.url}")
    private String webSocketUrl;
    @Autowired
    private VipAreaService vipAreaService;
    @Autowired
    private VipParkingService vipParkingService;
    @Autowired
    private FloorLockService floorLockService;
    @Autowired
    private ShowScreenConfigService showScreenConfigService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;

    @MyPermission
    @RequestMapping(value = "/getMap2dSel")
    @ApiOperation(value = "2维地图查询接口", notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", name = "name", value = "地图名", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "pageIndex", value = "当前页", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageSize", value = "页面大小", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "mapAll", value = "区分页面13下拉12查全部", required = false, dataType = "int")
    })
    public CommonResult<Object> getMap2dSel(String name,
                                            Integer enable,
                                            Integer status,
                                            Integer mapAll,String floorName,
                                            @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                            @RequestParam(value = "pageSize", defaultValue = "1") Integer pageSize,
                                            @RequestParam(value = "all", defaultValue = "3") Integer all, Integer ena,
                                            String companyId, String areaName, String placeName, String showScreen, String type, String maps) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String[] mapids = null;
            String[] mapidAlls = null;
            if (!NullUtils.isEmpty(mapAll)) {
                List<Map_2d> map2dList = memberService.getMapIdAll(member.getUid());
                for (Map_2d str : map2dList) {
                    mapidAlls = stringBuilder.append(str.getId()).append(",").toString().split(",");
                }
                if (mapAll == 12) {
                    /*
                     * 分页 total-->总数量
                     * */
                    int total = map2dService.findByAll3(name, enable, null,floorName, mapidAlls).size();
                    if (pageIndex > total / pageSize) {
                        if (total % pageSize == 0) {
                            pageIndex = total / pageSize;
                        } else {
                            pageIndex = total / pageSize + 1;
                        }
                    }

                    PageHelper.startPage(pageIndex, pageSize);
                    List<Map_2d> map2ds = map2dService.findByAll3(name, enable, null,floorName, mapidAlls);
                    List<Maptheme> maptheme = mapthemeService.list();
                    PageInfo<Map_2d> pageInfo = new PageInfo<>(map2ds);
                    //加载蜂鸟地图
                    fastMap(map2ds);
                    Map<String, Object> map = new HashMap<>();
                    map.put("list", pageInfo.getList());
                    map.put("maptheme", maptheme);
                    map.put("pageIndex", pageIndex);
                    map.put("total", pageInfo.getTotal());
                    map.put("pages", pageInfo.getPages());
                    return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
                }

            }
            //地图管理页面下拉
            if (!NullUtils.isEmpty(status)) {
                if (mapAll == 13 && status == 0) {
                    if (pageSize < 0) {
                        List<Map_2d> map2ds = map2dService.findByAll3(name, enable, null, floorName,mapidAlls);
                        //加载蜂鸟地图
                        fastMap(map2ds);
                        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map2ds);
                    }
                }
            }

            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            //搜索重置
            if (!NullUtils.isEmpty(ena)) {
                if (ena == 3) {
                    List<Map_2d> map2ds = map2dService.findByAll3(name, enable, null,floorName, mapids);
                    PageInfo<Map_2d> pageInfo = new PageInfo<>(map2ds);
                    Map<String, Object> map = new HashMap<>();
                    map.put("list", pageInfo.getList());
                    map.put("pageIndex", pageIndex);
                    map.put("total", pageInfo.getTotal());
                    map.put("pages", pageInfo.getPages());
                    return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
                }
            }

            //按条件查询
            //首页下拉
            //pageSize<0时查询所有
            if (pageSize < 0 && NullUtils.isEmpty(areaName)) {
                List<Map_2d> map2ds = map2dService.findByAll2(name, enable, null, companyId, floorName,mapids);
                //加载蜂鸟地图
                fastMap(map2ds);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map2ds);
            }
            //默认查全部 **
            if (!NullUtils.isEmpty(status)) {
                if (all == 3 && status == 0) {
                    List<Map_2d> map2ds = map2dService.findByAll2(name, enable, null, companyId,floorName, mapids);
                    PageInfo<Map_2d> pageInfo = new PageInfo<>(map2ds);
                    Map<String, Object> map = new HashMap<>();
                    map.put("list", pageInfo.getList());
                    map.put("pageIndex", pageIndex);
                    map.put("total", pageInfo.getTotal());
                    map.put("pages", pageInfo.getPages());
                    return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
                }
            }
            /*
             * 分页 total-->总数量
             * */
            int total = map2dService.findByAll3(name, enable, null, floorName, mapids).size();
            if (pageIndex > total / pageSize) {
                if (total % pageSize == 0) {
                    pageIndex = total / pageSize;
                } else {
                    pageIndex = total / pageSize + 1;
                }
            }

            Map<String, Object> map = new HashMap<>();
            if (!NullUtils.isEmpty(areaName) && "areaName".equals(areaName)) {
                List<ParkingCompanyVo> areaNameList = vipAreaService.getMapIdByAllAreaName(mapids);
                map.put("areaName", areaNameList);
            }
            if (!NullUtils.isEmpty(placeName) && "placeName".equals(placeName)) {
                List<ParkingCompanyVo> placeNameList = vipParkingService.getAllPlaceNameByMapId(mapids,type);
                map.put("placeName", placeNameList);
            }

            if (!NullUtils.isEmpty(placeName) && "floorLock".equals(placeName)) {
                List<ParkingCompanyVo> placeNameList = floorLockService.getAllPlaceNameByMapId(mapids,type);
                map.put("placeName", placeNameList);
            }
            if (!NullUtils.isEmpty(showScreen) && "showScreen".equals(showScreen)) {
                List<ParkingCompanyVo> showScreenList = showScreenConfigService.getAllShowScreenByMapId(mapids);
                map.put("showScreen", showScreenList);
            }

            PageHelper.startPage(pageIndex, pageSize);
            List<Map_2d> map2ds = map2dService.findByAll3(name, enable, null, floorName, mapids);
            PageInfo<Map_2d> pageInfo = new PageInfo<>(map2ds);
            //加载蜂鸟地图
            fastMap(map2ds);
            map.put("list", pageInfo.getList());
            map.put("pageIndex", pageIndex);
            map.put("total", pageInfo.getTotal());
            map.put("pages", pageInfo.getPages());

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getMap2dSel2")
    @ApiOperation(value = "2维地图查询接口", notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", name = "name", value = "地图名", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "pageIndex", value = "当前页", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageSize", value = "页面大小", required = false, dataType = "int")
    })
    public CommonResult<Object> getMap2dSel2(String name,
                                             @RequestParam(value = "enable", defaultValue = "1") Integer enable,
                                             @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                             @RequestParam(value = "pageSize", defaultValue = "1") Integer pageSize) {
        try {
            //按条件查询
            //pageSize<0时查询所有
            if (pageSize < 0) {
                List<Map_2d> map2ds = map2dService.findByAll(name, enable, null);
                //加载蜂鸟地图
                fastMap(map2ds);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map2ds);
            }
            /*
             * 分页 total-->总数量
             * */
            int total = map2dService.findByAll(name, enable, null).size();
            if (pageIndex > total / pageSize) {
                if (total % pageSize == 0) {
                    pageIndex = total / pageSize;
                } else {
                    pageIndex = total / pageSize + 1;
                }
            }

            PageHelper.startPage(pageIndex, pageSize);
            List<Map_2d> map2ds = map2dService.findByAll(name, enable, null);
            PageInfo<Map_2d> pageInfo = new PageInfo<>(map2ds);
            //加载蜂鸟地图
            fastMap(map2ds);
            Map<String, Object> map = new HashMap<>();
            map.put("list", pageInfo.getList());
            map.put("pageIndex", pageIndex);
            map.put("total", pageInfo.getTotal());
            map.put("pages", pageInfo.getPages());
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            operationlogService.addOperationlog(member.getUid(), LocalUtil.get(KafukaTopics.QUERY_MAP));
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"map:see", "map:edit"}, logical = Logical.OR)
    @RequestMapping(value = "/getMap2dId/{id}")
    @ApiOperation(value = "地图详情接口", notes = "无")
    @ApiImplicitParam(paramType = "path", name = "id", value = "2维地图id", required = true, dataType = "int")
    public CommonResult<Map_2d> getMap2dId(@PathVariable("id") Integer id) {
        try {
            Map_2d map2d = map2dService.findById(id);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map2d);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getMap2dQrCode/{id}")
    @ApiOperation(value = "地图详情接口", notes = "无")
    @ApiImplicitParam(paramType = "path", name = "id", value = "2维地图id", required = true, dataType = "int")
    public CommonResult<Map_2d> getMap2dQrCode(@PathVariable("id") Integer id) {
        try {
            Map_2d map2d = map2dService.findById(id);
            String downloadUrl = fdfsUrl + map2d.getQrcode();
            File fileData;
            File fileWin;
            if (uploadFolder.startsWith("/data")) {
                fileData = new File(uploadFolder + File.separator);
                if (!fileData.exists()) {
                    fileData.mkdirs();
                }
                String imgPath = String.valueOf(fileData).replace("\\", "/");
                if (uploadFolder.startsWith("/data")) {
                    URL url = new URL(downloadUrl);
                    URLConnection urlConnection = url.openConnection();
                    //开始获取数据
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String localPath = imgPath + downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    OutputStream os = Files.newOutputStream(Paths.get(localPath));
                    int len;
                    byte[] arr = new byte[1024];
                    while ((len = inputStream.read(arr)) != -1) {
                        os.write(arr, 0, len);
                        os.flush();
                    }
                    map2d.setQrcodelocal(StringUtils.substringAfter(localPath, "downloadfile"));
                    map2dService.updateMap2d(map2d);
                    os.close();
                }
            } else {
                fileWin = new File(uploadFolder + File.separator + "img");
                File[] files = fileWin.listFiles();
                if (!fileWin.exists()) {
                    fileWin.mkdirs();
                }
                String imgPath = String.valueOf(fileWin).replace("\\", "/");
                String spiltPath = null;
                if (!uploadFolder.startsWith("/data")) {
                    if (files != null) {
                        for (File file1 : files) {
                            spiltPath = StringUtils.substringAfter(file1, "upload").replace("\\", "/");
                            if (map2d.getQrcodelocal().equals(spiltPath)) {

                                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map2d);
                            }

                        }
                        map2d.setQrcodelocal(spiltPath);
                        map2dService.updateMap2d(map2d);
                    }
                    URL url = new URL(downloadUrl);
                    URLConnection urlConnection = url.openConnection();
                    //开始获取数据
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String localPath = imgPath + downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    OutputStream os = Files.newOutputStream(Paths.get(localPath));
                    int len;
                    byte[] arr = new byte[1024];
                    while ((len = inputStream.read(arr)) != -1) {
                        os.write(arr, 0, len);
                        os.flush();
                    }
                    map2d.setQrcodelocal(StringUtils.substringAfter(localPath, "upload"));
                    map2dService.updateMap2d(map2d);
                    os.close();
                }

            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map2d);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions("map:add")
    @RequestMapping(value = "/addMap2d")
    @ApiOperation(value = "2维地图新增接口", notes = "2维地图信息")
    public CommonResult<Object> addMap2d(Map_2d map2d, MultipartFile file, MultipartFile file2,MultipartFile mapLogoPath,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String uid = "";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            List<Map_2d> map2dList = map2dService.findByfmapId(null, map2d.getFmapID());
            if (!NullUtils.isEmpty(map2dList)) {
                return new CommonResult<>(400, "该地图ID已经被" + map2dList.get(0).getName() + "使用！！！" + LocalUtil.get("请重新确认!"));
            }
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            String name = map2d.getName();
            if (name == null || "".equals(name.trim())) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.NAME_EMPTY));
            }
            if (NullUtils.isEmpty(map2d.getLng()) && NullUtils.isEmpty(map2d.getLat())) {
                return new CommonResult<>(400, LocalUtil.get("经度或者纬度不能为空！选择经纬度！！！"));
            }
            if (map2d.getName().length() >= 20) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.MAP_NAME_LENGTH));
            }
            List<Map_2d> sameName = map2dService.findByAllSame(name);
            if (sameName != null && sameName.size() > 0) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.NAME_CONFLICT));
            }
            if(!NullUtils.isEmpty(mapLogoPath)){
                StorePath storePath = fastFileStorageClient.uploadFile(mapLogoPath.getInputStream(), mapLogoPath.getSize(), FilenameUtils.getExtension(mapLogoPath.getOriginalFilename()), null);
                map2d.setMapLogo(storePath.getFullPath());
                CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, mapLogoPath,storePath.getFullPath());
                if (commonResult.getCode()==200) {
                    map2d.setMapLogolocal(url + commonResult.getData());
                }
            }
            //根据不同的地图类型解析不同文件
            if(!NullUtils.isEmpty(file2)){
                StorePath storePath = fastFileStorageClient.uploadFile(file2.getInputStream(), file2.getSize(), FilenameUtils.getExtension(file2.getOriginalFilename()), null);
                map2d.setWelcomePagePath(storePath.getFullPath());
                CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, file2,storePath.getFullPath());
                if (commonResult.getCode()==200) {
                    map2d.setWelcomePagePathlocal(url + commonResult.getData());
                }
            }
            if ((!NullUtils.isEmpty(file) && !file.isEmpty())) {
                StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
                map2d.setUrl(storePath.getFullPath());
                if (map2d.getType() == 1) {//普通地图
                    CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, file, storePath.getFullPath());
                    //如果状态码不是200 直接return
                    if (commonResult.getCode() != 200) {
                        return commonResult;
                    }
                    //zip压缩包全称
                    String path = String.valueOf(commonResult.getData());
                    //压缩包路径
                    map2d.setThemeImg(url + path);
                    map2d.setMapImg(url + path);
                }
                if (map2d.getType() == 2) {//蜂鸟地图
                    CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, file, storePath.getFullPath());
                    //如果状态码不是200 直接return
                    if (commonResult.getCode() != 200) {
                        return commonResult;
                    }
                    //zip压缩包全称
                    String path = String.valueOf(commonResult.getData());
                    FileUtils.zipUncompress(uploadFolder + path, uploadFolder + path.substring(0, path.lastIndexOf(".")));
                    map2d.setThemeImg(url + path.substring(0, path.lastIndexOf(".")));
                    //压缩包路径
                    map2d.setMapImg(url + path);
                }
                if (map2d.getType() == 3) {//三维地图
                    CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, file, storePath.getFullPath());
                    //如果状态码不是200 直接return
                    if (commonResult.getCode() != 200) {
                        return commonResult;
                    }
                    //zip压缩包全称
                    String path = String.valueOf(commonResult.getData());
                    FileUtils.zipUncompress(uploadFolder + path, uploadFolder + path.substring(0, path.lastIndexOf(".")));
                    map2d.setThemeImg(url + path.substring(0, path.lastIndexOf(".")));
                    //压缩包路径
                    map2d.setMapImg(url + path);
                }
            }
            if (map2dService.addMap2d(map2d)) {
                map2dService.addUsermap(member.getUid(), String.valueOf(map2d.getId()));
                //修改地图生成微信二维码
                String fileName = WxQrCode.getMapQrCode(String.valueOf(map2d.getId()));//二维码路径
                File files = new File(fileName);//获取二维码路径
                InputStream inputStream = Files.newInputStream(files.toPath());
                FastFileStorageClient fastFileStorageClient = SpringContextHolder.getBean(FastFileStorageClient.class);

                StorePath storePath = fastFileStorageClient.uploadFile(inputStream, files.length(), "png", null);
                map2d.setQrcode(storePath.getFullPath());
                FileUtils.pullRemoteFileToLocal(storePath.getFullPath());
//                map2d.setQrcodelocal("/rtls/"+ storePath.getFullPath());

                String imgPath = StringUtils.substringAfter(files.getPath(), "rtls");
                if (imgPath.contains("\\")) {
                    imgPath = imgPath.replace("\\", "/");
                }
                map2d.setQrcodelocal("/rtls" + imgPath);

                map2dService.updateByIDCode(map2d);
                operationlogService.addOperationlog(Integer.valueOf(uid), LocalUtil.get(KafukaTopics.ADD_MAP) + map2d.getName());
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.MAP_INFO)), now);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS), map2d.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequiresPermissions("map:edit")
    @RequestMapping(value = "/updateMap2d")
    @ApiOperation(value = "2维地图修改接口", notes = "2维地图信息")
    public CommonResult<Object> updateMap2d(Map_2d map2d, MultipartFile file,MultipartFile file2,MultipartFile mapLogoPath,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String uid = "1";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            List<Map_2d> map2dList = map2dService.findByfmapId(map2d.getId(), map2d.getFmapID());
            if (!NullUtils.isEmpty(map2dList)) {
                return new CommonResult<>(400, "该地图ID已经被" + map2dList.get(0).getName() + "使用！！！" + LocalUtil.get(KafukaTopics.NAME_EXIST));
            }
            String name = map2d.getName();
            if (name == null || "".equals(name.trim())) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.NAME_EMPTY));
            }
            String instanceid = redisService.get("instance" + uid);
            List<Map_2d> sameName = map2dService.findByAllSame(name);
            if (sameName != null && sameName.size() > 0 && !map2d.getId().equals(sameName.get(0).getId())) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.NAME_CONFLICT));
            }
            if (map2d.getName().length() >= 20) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.MAP_NAME_LENGTH));
            }
            //实例
            map2d.setInstanceid(instanceid);
            //修改时间
            map2d.setUpdateTime(new Date());
            if(!NullUtils.isEmpty(mapLogoPath)){
                StorePath storePath = fastFileStorageClient.uploadFile(mapLogoPath.getInputStream(), mapLogoPath.getSize(), FilenameUtils.getExtension(mapLogoPath.getOriginalFilename()), null);
                map2d.setMapLogo(storePath.getFullPath());
                CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, mapLogoPath,storePath.getFullPath());
                if (commonResult.getCode()==200) {
                    map2d.setMapLogolocal(url + commonResult.getData());
                }
            }
            if(NullUtils.isEmpty(sameName)){
                if(!"".equals(map2d.getMapLogo())&&!NullUtils.isEmpty(map2d.getMapLogo())){
                    map2d.setMapLogolocal(url+map2d.getMapLogo());
                }
            }else {
                if(!NullUtils.isEmpty(mapLogoPath)||(!NullUtils.isEmpty(sameName.get(0).getMapLogo()) || !NullUtils.isEmpty(sameName.get(0).getMapLogolocal()))){
                    if(!"".equals(map2d.getMapLogo())&&!NullUtils.isEmpty(map2d.getMapLogo())){
                        map2d.setMapLogolocal(url+map2d.getMapLogo());
                    }
                }else{
                    map2d.setMapLogo(null);
                    map2d.setMapLogolocal(null);
                }
            }


            if(!NullUtils.isEmpty(file2)){
                StorePath storePath = fastFileStorageClient.uploadFile(file2.getInputStream(), file2.getSize(), FilenameUtils.getExtension(file2.getOriginalFilename()), null);
                map2d.setWelcomePagePath(storePath.getFullPath());
                CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, file2,storePath.getFullPath());
                if (commonResult.getCode()==200) {
                    map2d.setWelcomePagePathlocal(url + commonResult.getData());
                }
            } else {
                if(!"".equals(map2d.getWelcomePagePath())&&!NullUtils.isEmpty(map2d.getWelcomePagePath())){
                    map2d.setWelcomePagePathlocal(url+map2d.getWelcomePagePath());
                }
            }
            //根据不同的地图类型解析不同文件
            if (!NullUtils.isEmpty(file) && !file.isEmpty()) {
                StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
                map2d.setUrl(storePath.getFullPath());
                if (map2d.getType() == 1) {//图片地图
                    CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, file, storePath.getFullPath());
                    //如果状态码不是200 直接return
                    if (commonResult.getCode() != 200) {
                        return commonResult;
                    }
                    //zip压缩包全称
                    String path = String.valueOf(commonResult.getData());
                    //   FileUtils.zipUncompress(uploadFolder+path,uploadFolder+path.substring(0, path.lastIndexOf(".")));
                    //二次解压后的文件名
                    //  map2d.setThemeImg(url + path.substring(0, path.lastIndexOf(".")));
                    map2d.setThemeImg(url + path);
                    //压缩包路径
                    map2d.setMapImg(url + path);
                }
                if (map2d.getType() == 2) {//蜂鸟地图
                    CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, file, storePath.getFullPath());
                    //如果状态码不是200 直接return
                    if (commonResult.getCode() != 200) {
                        return commonResult;
                    }
                    //zip压缩包全称
                    String path = String.valueOf(commonResult.getData());
                    FileUtils.zipUncompress(uploadFolder + path, uploadFolder + path.substring(0, path.lastIndexOf(".")));
                    //二次解压后的文件名
                    map2d.setThemeImg(url + path.substring(0, path.lastIndexOf(".")));
                    //压缩包路径
                    map2d.setMapImg(url + path);
                }
                if (map2d.getType() == 3) {//三维地图
                    CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, file, storePath.getFullPath());
                    //如果状态码不是200 直接return
                    if (commonResult.getCode() != 200) {
                        return commonResult;
                    }
                    //zip压缩包全称
                    String path = String.valueOf(commonResult.getData());
                    FileUtils.zipUncompress(uploadFolder + path, uploadFolder + path.substring(0, path.lastIndexOf(".")));
                    //二次解压后的文件名
                    map2d.setThemeImg(url + path.substring(0, path.lastIndexOf(".")));
                    //压缩包路径
                    map2d.setMapImg(url + path);
                }
            }
            if (map2dService.updateMap2d(map2d)) {
//                //修改地图生成微信二维码
                String fileName = WxQrCode.getMapQrCode(String.valueOf(map2d.getId()));
                File files = new File(fileName);
                InputStream inputStream = Files.newInputStream(files.toPath());
                FastFileStorageClient fastFileStorageClient = SpringContextHolder.getBean(FastFileStorageClient.class);

                StorePath storePath = fastFileStorageClient.uploadFile(inputStream, files.length(), "png", null);
                map2d.setQrcode(storePath.getFullPath());
                FileUtils.pullRemoteFileToLocal(storePath.getFullPath());
//                map2d.setQrcodelocal("/rtls/"+ storePath.getFullPath());

                String imgPath = StringUtils.substringAfter(files.getPath(), "rtls");
                if (imgPath.contains("\\")) {
                    imgPath = imgPath.replace("\\", "/");
                }
                map2d.setQrcodelocal("/rtls" + imgPath);

                map2dService.updateByIDCode(map2d);
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.MAP_INFO)), now);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS), map2d.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequiresPermissions("map:del")
    @RequestMapping(value = "/delMap2d/{ids}")
    @ApiOperation(value = "2维地图删除接口", notes = "2维地图id集")
    @ApiImplicitParam(paramType = "path", name = "ids", value = "2维地图id集", required = true, dataType = "String")
    public CommonResult<Object> delMap2d(@PathVariable("ids") String ids,HttpServletRequest request) {
        try {
            Integer uid = 1;
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = member.getUid();
            }
//            map2dService.delPlace(ids);//删除车位
            if (map2dService.delMap2d(ids)) {
                map2dService.deleteByMapIdMap(ids, String.valueOf(member.getUid()));
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.MAP_INFO)), now);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

    /*
     * 获取样式的信息
     * */
    @RequestMapping(value = "/findByStyle")
    @ApiOperation(value = "获取样式接口")
    public CommonResult<Object> findByStyle() {
        try {
            //基站样式
            List<Style> styleSub = map2dService.findByStyle("sub");
            //标签样式
            List<Style> styleTag = map2dService.findByStyle("tag");
            //网关样式
            List<Style> styleGateway = map2dService.findByStyle("gateway");
            Map<String, Object> map = new HashMap<>();
            map.put("subStyles", styleSub);
            map.put("tagStyles", styleTag);
            map.put("gatewayStyles", styleGateway);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    /*
     * 获取相关连接地址信息
     * */
    @RequestMapping(value = "/url")
    public CommonResult url() {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("fdfsUrl", fdfsUrl);
            map.put("webSocketUrl", webSocketUrl);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    /*
     * 蜂鸟地图加载判断
     * */
    private void fastMap(List<Map_2d> map2ds) {
        try {
            //判断蜂鸟地图的压缩包是否再当前环境下 ，否 从fastDFS系统中拉取
            for (Map_2d map : map2ds) {
                if (/*(map.getType() == 2|| map.getType() == 3) &&*/ !NullUtils.isEmpty(map.getUrl())) {
                    //判断压缩包路径是否存在
                    File file = new File(uploadFolder + map.getUrl());
                    if (!file.exists()) {//不存在 就拉取创建
                        URL url = new URL(fdfsUrl + map.getUrl());
                        URLConnection urlConnection = url.openConnection();
                        HttpURLConnection httpUrlConnection = (HttpURLConnection) urlConnection;
                        httpUrlConnection.setConnectTimeout(1000 * 5);
                        httpUrlConnection.setRequestMethod("GET");
                        httpUrlConnection.setRequestProperty("Charset", "UTF-8");
                        // 打开到此 URL引用的资源的通信链接（如果尚未建立这样的连接）。
                        int fileLength = httpUrlConnection.getContentLength();
                        httpUrlConnection.connect();
                        // 建立链接从请求中获取数据
                        BufferedInputStream bin = new BufferedInputStream(httpUrlConnection.getInputStream());
                        // 校验文件夹目录是否存在，不存在就创建一个目录
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                        OutputStream out = new FileOutputStream(file);
                        int size = 0;
                        int len = 0;
                        byte[] buf = new byte[2048];
                        while ((size = bin.read(buf)) != -1) {
                            len += size;
                            out.write(buf, 0, size);
                            // 控制台打印文件下载的百分比情况
                            //  System.out.println("下载了-------> " + len * 100 / fileLength + "%\n");
                        }
                        // 关闭资源
                        bin.close();
                        out.close();
                        //解压
                        if ((map.getType() == 2 || map.getType() == 3)) {
                            FileUtils.zipUncompress(uploadFolder + map.getUrl(), uploadFolder + map.getUrl().substring(0, map.getUrl().lastIndexOf(".")));
                        }
                    }
                }
            }
        } catch (Exception e) {
            return;
        }
    }

}
