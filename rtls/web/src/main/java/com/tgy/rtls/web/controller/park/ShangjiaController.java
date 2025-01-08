package com.tgy.rtls.web.controller.park;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.Basestation;
import com.tgy.rtls.data.entity.equip.Substation;
import com.tgy.rtls.data.entity.map.BsConfig;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.park.ShangJia;
import com.tgy.rtls.data.entity.park.StorePlace;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.mapper.equip.BaseMapper;
import com.tgy.rtls.data.mapper.equip.SubMapper;
import com.tgy.rtls.data.mapper.map.BsConfigMapper;
import com.tgy.rtls.data.mapper.park.BookMapper;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.map.BsConfigService;
import com.tgy.rtls.data.service.map.Map2dService;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
import com.tgy.rtls.web.util.FileUtils;
import io.swagger.annotations.ApiOperation;
import net.coobird.thumbnailator.Thumbnails;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping(value = "/park")
/**
 * 商家信息
 */
public class ShangjiaController {
    @Autowired
    private ParkingService parkingService;
    @Autowired
    private Map2dService map2dService;
    @Autowired(required = false)
    private BaseMapper baseMapper;
    @Autowired
    private FastFileStorageClient fastFileStorageClient;
    @Autowired
    private SubService subService;
    @Autowired(required = false)
    private SubMapper subMapper;
    @Autowired
    private BsConfigService bsConfigService;
    @Autowired(required = false)
    private BsConfigMapper bsConfigMapper;
    @Autowired
    private LocalUtil localUtil;
    @Value("${file.url}")
    public String url;
    @Value("${fdfs.url}")
    private String fdfsUrl;
    @Value("${file.uploadFolder}")
    private String uploadFolder;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private ParkMapper parkMapper;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private OperationlogService operationlogService;

    private static final long LIMIT_SIZE = 9992;
    private static final String IMG_TYPE = "jpg";
    private static final String LOCAL = "group1/M00/00/0C";

    @MyPermission
    @RequestMapping(value = "/getShangjia")
    @ApiOperation(value = "getShangjia", notes = "111")
    public CommonResult<Object> getShangjia(String name,
                                            Integer type, Integer map, Integer pageIndex,String floorName, Integer pageSize, String maps) {
        try {
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<ShangJia> data = parkingService.findByAllShangjia2(null, map, type, name, null, null, null,floorName, mapids,null);
            PageInfo<ShangJia> pageInfo = new PageInfo<>(data);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());

            if (pageSize != null && pageSize != -1) {
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

    @RequestMapping(value = "/getShangjia1")
    public CommonResult<Object> getShangjia1(Integer map) {
        try {

            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));

            List<ShangJia> data = parkingService.findByAllShangjia2(null, map, null, null, null, null, null,null, null,null);
            res.setData(data);

            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/getShangjiaThumbnail")
    @ApiOperation(value = "getShangjiaThumbnail", notes = "111")
    public CommonResult<Object> getShangjiaThumbnail(String name, Integer type, Integer map,String floorName, Integer pageIndex, Integer pageSize) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);

                List<ShangJia> shangJia = parkingService.findByAllShangjia2(null, map, type, name, null, null, null, floorName, null,null);
                File file = new File(uploadFolder + LOCAL);
                File[] files = file.listFiles();
                if (!file.exists()) {
                    file.mkdirs();
                }
                //将\转/
                String imgPath = String.valueOf(file).replace("\\", "/");

                for (ShangJia shangJia1 : shangJia) {

                    URL url = new URL(fdfsUrl + shangJia1.getThumbnail());
                    String downloadUrl = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + url.getPath();
                    URLConnection urlConnection = url.openConnection();
                    //开始获取数据
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String localPath = imgPath + downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    OutputStream os = new FileOutputStream(localPath);
                    int len;
                    byte[] arr = new byte[1024];
                    while ((len = inputStream.read(arr)) != -1) {
                        os.write(arr, 0, len);
                        os.flush();
                    }
                    os.close();
                }
            }
            List<ShangJia> data = parkingService.findByAllShangjia2(null, map, type, name, null, null, null, floorName, null,null);
            PageInfo<ShangJia> pageInfo = new PageInfo<>(data);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());

            if (pageSize != null && pageSize != -1) {
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

    @RequiresPermissions(value = {"bss:see","bss:edit"},logical = Logical.OR)
    @RequestMapping(value = "/getShangjiaByid/{id}")
    @ApiOperation(value = "获取商家信息", notes = "111")
    public CommonResult<Object> getPlace(@PathVariable("id") Integer id) {
        try {
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }

            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            List<ShangJia> data = parkingService.findByAllShangjia(id, null, null, null, null, null, null);
            res.setData(data.size() == 0 ? null : data.get(0));
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

     @RequiresPermissions({"bss:add"})
    @RequestMapping(value = "/addShangjia")
    @ApiOperation(value = "添加车位信息", notes = "111")
    public CommonResult<Object> addShangJia(ShangJia shangJia, MultipartFile file,MultipartFile file2,HttpServletRequest request) {
        try {
            // Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            LocalDateTime now = LocalDateTime.now();
            if (NullUtils.isEmpty(shangJia.getType())) {
                return new CommonResult<>(400, LocalUtil.get("请选择商家类型"));
            }
//            if (shangJia.getPhone() == null || !ByteUtils.isPhoneLegal(shangJia.getPhone())) {
//                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PHONENUM_ERROR));
//            }
            if (NullUtils.isEmpty(shangJia.getMap())) {
                return new CommonResult<>(400, LocalUtil.get("请选择关联地图"));
            }
//            List<ShangJia> list = parkingService.findShangjiaPhone(shangJia.getPhone(), shangJia.getId());
            //同一个地图不能重复
            List<ShangJia> findMapName = parkingService.findShangjiaMapName(null,shangJia.getName(), shangJia.getMap());
            if (findMapName != null && findMapName.size() > 0) {
                return new CommonResult<>(400, LocalUtil.get("所选择的关联地图已经存在该商家名称"));
            }
//            if (list != null && list.size() > 0) {
//                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PHONENUM_EXIST));
//            }

            if(!NullUtils.isEmpty(file2)){
                StorePath storePath = fastFileStorageClient.uploadFile(file2.getInputStream(), file2.getSize(), FilenameUtils.getExtension(file2.getOriginalFilename()), null);
                shangJia.setPhoto2(storePath.getFullPath());
                CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, file2,storePath.getFullPath());
                if (commonResult.getCode()==200) {
                    shangJia.setPhotolocal2(url + commonResult.getData());
                }
            }
            if (!NullUtils.isEmpty(file) && !file.isEmpty()) {
                //FilenameUtils.getExtension（Filename -要检索扩展名的文件名。）获取文件拓展名
                StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
                shangJia.setPhoto(storePath.getFullPath());
                String fullPath = storePath.getFullPath();
                //设置统一图片后缀名
                String suffixName = null;
                //获取图片文件格式
                //获取图片后缀名,判断如果是png的话就不进行格式转换,因为Thumbnails存在转png->jpg图片变红bug
                String fileType = FilenameUtils.getExtension(file.getOriginalFilename());
                if (fileType != null) {
                    suffixName = Optional.ofNullable(suffixName).orElse("png");
                    //缩略图报红-》把图片转成png格式
                    if (fileType.equalsIgnoreCase(IMG_TYPE)) {
                        suffixName = fileType.replaceAll(fileType, "png");
                    } else {
                        suffixName = fileType.replaceAll(fileType, "png");
                    }
                    if (file.getSize() <= LIMIT_SIZE) {
                        //获取文件上传后的全路径，带着组名
                        String thumbnailFullPath = storePath.getFullPath();
                        shangJia.setThumbnail(thumbnailFullPath);
                        CommonResult<Object> thumbnailLocal = FileUtils.uploadFileName(uploadFolder, file, thumbnailFullPath);
                        if (thumbnailLocal.getCode() == 200) {
                            shangJia.setPhotolocal(url + thumbnailLocal.getData());
                            shangJia.setThumbnaillocal(url + thumbnailLocal.getData());
                        }
                    } else {
                        String downloadUrl = fdfsUrl + shangJia.getPhoto();

                        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                        URL url1 = new URL(downloadUrl);
                        HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
                        //设置超时间为12秒
                        conn.setConnectTimeout(12 * 1000);
                        //防止屏蔽程序抓取而返回403错误
                        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
                        //得到输入流
                        InputStream inputStream = conn.getInputStream();
                        //获取自己数组
                        byte[] getData = readInputStream(inputStream);
                        //本地保存路径
                        String propertiesFile = uploadFolder + LOCAL;
                        //文件保存位置
                        File saveDir = new File(propertiesFile);
                        if (!saveDir.exists()) {
                            saveDir.mkdir();
                        }
                        File file1 = new File(propertiesFile + File.separator + fileName);
                        FileOutputStream fos = new FileOutputStream(file1);
                        fos.write(getData);
                        if (fos != null) {
                            fos.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        BufferedImage bufferedImage = Thumbnails.of(file1).size(120, 120).outputQuality(1.0f).asBufferedImage();
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        ImageOutputStream imOut = ImageIO.createImageOutputStream(os);
                        ImageIO.write(bufferedImage, suffixName, imOut);
                        InputStream input = new ByteArrayInputStream(os.toByteArray());
                        // 上传并且生成缩略图
                        StorePath thumbnail = fastFileStorageClient.uploadImageAndCrtThumbImage(input, os.toByteArray().length, suffixName, null);

                        MultipartFile cMultiFile = getMultipartFile(file1);

                        //获取文件上传后的全路径，带着组名
                        String thumbnailFullPath = thumbnail.getFullPath();
                        CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, cMultiFile, fullPath);
                        if (commonResult.getCode() == 200) {
                            shangJia.setPhotolocal(url + commonResult.getData());
                        }
                        shangJia.setThumbnail(thumbnailFullPath);
                        shangJia.setThumbnaillocal(url + LOCAL + fileName);
                    }
                }
            }
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
            if("".equals(shangJia.getX())&&"".equals(shangJia.getY())&&"".equals(shangJia.getZ())) {
                return new CommonResult<>(400, LocalUtil.get("添加的商家关联地图未选点，请在地图上选点！！！"));
            }
            parkingService.addShangjia(shangJia);
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            // operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.BUSINESS_INFO)), now);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequiresPermissions(value = {"bss:edit"})
    @RequestMapping(value = "/updateShangjia")
    @ApiOperation(value = "更新车位信息", notes = "111")
    public CommonResult<Object> updatePlace(ShangJia shangJia, MultipartFile file,MultipartFile file2, HttpServletRequest request) {
        try {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            LocalDateTime now = LocalDateTime.now();
            if (NullUtils.isEmpty(shangJia.getType())) {
                return new CommonResult<>(400, LocalUtil.get("请选择商家类型"));
            }
//            if (shangJia.getPhone() == null || !ByteUtils.isPhoneLegal(shangJia.getPhone())) {
//                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PHONENUM_ERROR));
//            }
            if (NullUtils.isEmpty(shangJia.getMap())) {
                return new CommonResult<>(400, LocalUtil.get("请选择关联地图"));
            }

            if(!NullUtils.isEmpty(file2)){
                StorePath storePath = fastFileStorageClient.uploadFile(file2.getInputStream(), file2.getSize(), FilenameUtils.getExtension(file2.getOriginalFilename()), null);
                shangJia.setPhoto2(storePath.getFullPath());
                CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, file2,storePath.getFullPath());
                if (commonResult.getCode()==200) {
                    shangJia.setPhotolocal2(url + commonResult.getData());
                }
            }
            if (!NullUtils.isEmpty(file) && !file.isEmpty()) {
                StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
                shangJia.setPhoto(storePath.getFullPath());
                String fullPath = storePath.getFullPath();
                //设置统一图片后缀名
                String suffixName = null;
                //获取图片文件格式
                //获取图片后缀名,判断如果是png的话就不进行格式转换,因为Thumbnails存在转png->jpg图片变红bug
                String fileType = FilenameUtils.getExtension(file.getOriginalFilename());

                if (fileType != null) {
                    suffixName = Optional.ofNullable(suffixName).orElse("png");
                    //缩略图报红-》把图片转成png格式
                    if (fileType.equalsIgnoreCase(IMG_TYPE)) {
                        suffixName = fileType.replaceAll(fileType, "png");
                    }
                    // 缩略图文件流处理
                    //BufferedImage bufferedImage = Thumbnails.of(file.getInputStream()).size(270, 270).asBufferedImage();
                    if (file.getSize() <= LIMIT_SIZE) {
                        //获取文件上传后的全路径，带着组名
                        String thumbnailFullPath = storePath.getFullPath();
                        shangJia.setThumbnail(thumbnailFullPath);
                        CommonResult<Object> thumbnailLocal = FileUtils.uploadFileName(uploadFolder, file, thumbnailFullPath);
                        if (thumbnailLocal.getCode() == 200) {
                            shangJia.setPhotolocal(url + thumbnailLocal.getData());
                            shangJia.setThumbnaillocal(url + thumbnailLocal.getData());
                        }
                    } else {
                        String downloadUrl = fdfsUrl + shangJia.getPhoto();
                        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                        URL url1 = new URL(downloadUrl);
                        HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
                        //设置超时间为3秒
                        conn.setConnectTimeout(5 * 1000);
                        //防止屏蔽程序抓取而返回403错误
                        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
                        //得到输入流
                        InputStream inputStream = conn.getInputStream();
                        //获取自己数组
                        byte[] getData = readInputStream(inputStream);
                        //本地保存路径
                        String propertiesFile = uploadFolder + LOCAL;
                        //文件保存位置
                        File saveDir = new File(propertiesFile);
                        if (!saveDir.exists()) {
                            saveDir.mkdirs();
                        }
                        File file1 = new File(propertiesFile + File.separator + fileName);
                        FileOutputStream fos = new FileOutputStream(file1);
                        fos.write(getData);
                        if (fos != null) {
                            fos.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        BufferedImage bufferedImage = Thumbnails.of(file1).size(120, 120).outputQuality(1.0f).asBufferedImage();
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        ImageOutputStream imOut = ImageIO.createImageOutputStream(os);
                        ImageIO.write(bufferedImage, suffixName, imOut);
                        InputStream input = new ByteArrayInputStream(os.toByteArray());
                        // 上传并且生成缩略图
                        StorePath thumbnail = fastFileStorageClient.uploadImageAndCrtThumbImage(input, os.toByteArray().length, suffixName, null);

                        MultipartFile cMultiFile = getMultipartFile(file1);

                        //获取文件上传后的全路径，带着组名
                        String thumbnailFullPath = thumbnail.getFullPath();
                        CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, cMultiFile, fullPath);
                        if (commonResult.getCode() == 200) {
                            shangJia.setPhotolocal(url + String.valueOf(commonResult.getData()));
                        }
                        shangJia.setThumbnail(thumbnailFullPath);
                        shangJia.setThumbnaillocal(url + LOCAL + fileName);
                    }
                }
            }
//            List<ShangJia> list = parkingService.findShangjiaPhone(shangJia.getPhone(), shangJia.getId());
            //同一个地图不能重复
            List<ShangJia> findMapName = parkingService.findShangjiaMapName1(shangJia.getName(), shangJia.getMap(), shangJia.getId());
            if (findMapName != null && findMapName.size() > 0) {
                return new CommonResult<>(400, LocalUtil.get("所选择的关联地图已经存在该商家名称"));
            }
//            if (list != null && list.size() > 0) {
//                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PHONENUM_EXIST));
//            }
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
            //原来的
            List<ShangJia> shangJiaList = parkingService.getShangjiaMap(shangJia.getId());
            if(!NullUtils.isEmpty(shangJiaList)){
                if(!shangJiaList.get(0).getMap().equals(shangJia.getMap())&&shangJiaList.get(0).getX().equals(shangJia.getX())&&shangJiaList.get(0).getY().equals(shangJia.getY())){
                    return new CommonResult<>(400, LocalUtil.get("更换的商家关联地图未选点，请在地图上选点！！！"));
                }
            }

            if(!NullUtils.isEmpty(file)||(!NullUtils.isEmpty(shangJiaList.get(0).getPhoto()) || !NullUtils.isEmpty(shangJiaList.get(0).getPhotolocal()))){
                if(!"".equals(shangJia.getPhoto())&&!NullUtils.isEmpty(shangJia.getPhoto())){
                    shangJia.setPhotolocal(url+shangJia.getPhoto());
                    shangJia.setPhotolocal2(url+shangJia.getPhoto());
                }
            }else{
                shangJia.setPhoto(null);
                shangJia.setPhotolocal(null);
            }
            if((shangJia.getPhoto()==null||shangJia.getPhoto().isEmpty())||shangJia.getPhotolocal()==null){
                shangJia.setThumbnaillocal(null);
            }else {
                shangJia.setThumbnail(shangJia.getPhoto());
                shangJia.setThumbnaillocal(shangJia.getPhotolocal());
            }
            List<StorePlace> storePlaceByName =null;
            if(!shangJiaList.get(0).getName().equals(shangJia.getName())){
                storePlaceByName = bookMapper.getStorePlaceByName(shangJiaList.get(0).getName());
                if(!NullUtils.isEmpty(storePlaceByName)){
                    for (StorePlace storePlace : storePlaceByName) {
                        bookMapper.delStorePlaceById(String.valueOf(storePlace.getId()));
                    }
                }
            }else {
                storePlaceByName = bookMapper.getStorePlaceByName(shangJiaList.get(0).getName());
            }

            List<StorePlace> storePlaceByFid=null;
            if(!shangJiaList.get(0).getFid().equals(shangJia.getFid())){
                storePlaceByFid = bookMapper.getStorePlaceByFid(shangJiaList.get(0).getFid());
                if(!NullUtils.isEmpty(storePlaceByFid)){
                    for (StorePlace storePlace : storePlaceByFid) {
                        storePlace.setFloor(shangJia.getFloor());
                        storePlace.setX(shangJia.getX());
                        storePlace.setY(shangJia.getY());
                        storePlace.setFid(shangJia.getFid());
                        bookMapper.updateStorePlace(storePlace);
                    }
                }
            }

            parkingService.updateShangjia(shangJia);
            if (!NullUtils.isEmpty(storePlaceByName)) {
                for (StorePlace storePlace : storePlaceByName) {
                    storePlace.setId(storePlace.getId());
                    storePlace.setMap(storePlace.getMap());
                    storePlace.setX(shangJia.getX());
                    storePlace.setY(shangJia.getY());
                    storePlace.setFid(shangJia.getFid());
                    storePlace.setFloor(shangJia.getFloor());
                    bookMapper.updateStorePlace(storePlace);
                }
            }

            if(!String.valueOf(shangJiaList.get(0).getMap()).equals(String.valueOf(shangJia.getMap()))){
                storePlaceByName = bookMapper.getStorePlaceByName(shangJiaList.get(0).getName());
                if(!NullUtils.isEmpty(storePlaceByName)){
                    for (StorePlace storePlace : storePlaceByName) {
                        bookMapper.delStorePlaceById(String.valueOf(storePlace.getId()));
                    }
                }
            }
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.BUSINESS_INFO)), now);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    public static MultipartFile getMultipartFile(File file) {
        FileItem item = new DiskFileItemFactory().createItem("file"
                , MediaType.MULTIPART_FORM_DATA_VALUE
                , true
                , file.getName());
        try (InputStream input = new FileInputStream(file);
             OutputStream os = item.getOutputStream()) {
            // 流转移
            IOUtils.copy(input, os);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid file: " + e, e);
        }

        return new CommonsMultipartFile(item);
    }

    /**
     * 从输入流中获取字节数组
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    @RequiresPermissions({"bss:del"})
    @RequestMapping(value = "/delShangjia/{ids}")
    @ApiOperation(value = "删除车位信息", notes = "111")
    public CommonResult<Object> delPlace(@PathVariable("ids") String ids,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            for (String id : ids.split(",")) {
                ShangJia shangJia = parkingService.getShangJiaById(id);
                if(!NullUtils.isEmpty(shangJia)){
                    List<StorePlace> storePlaceByName = bookMapper.getStorePlaceByName(shangJia.getName());
                    if(!NullUtils.isEmpty(storePlaceByName)){
                        for (StorePlace storePlace : storePlaceByName) {
                            bookMapper.delStorePlaceById(String.valueOf(storePlace.getId()));
                        }
                    }
                }
            }
            parkingService.delShangjia(ids.split(","));
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.BUSINESS_INFO)), now);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

//    @RequestMapping(value = "/getShangjiaType")
//    @ApiOperation(value = "获取商家类型", notes = "111")
//    public CommonResult<Object> getPlace() {
//        try {
//            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
//            List<ShangJiaType> data = parkingService.findByAllShangjiaType(null, null);
//            res.setData(data);
//            return res;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
//        }
//
//    }

    @RequestMapping(value = "/beaconPos")
    @ApiOperation(value = "添加信标位置", notes = "111")
    public CommonResult<Object> addBeacon(String beaconNum, String x, String y, String z, String fmapID, String floor) {
        CommonResult<Object> res = null;
        try {
            if (fmapID == null) {
                res.setMessage("fmap is null");
                res.setCode(500);
                return res;
            }
            Map_2d map_2d = map2dService.findByfmapID(fmapID);
            Integer mapid = null;
            if (map_2d != null) {
                mapid = map_2d.getId();
            }
            //   List<Basestation> data = baseService.findByAll(beaconNum, null, null, mapid, null, null);
            //     List<BsSyn> data=  subService.findByAll(beaconNum,null,null,null,null,mapid,null,null,null);
            BsConfig data = bsConfigMapper.findByNum(beaconNum, localUtil.getLocale());
            if (data == null) {
                 /*   Basestation basestation=new Basestation();
                    basestation.setNum(beaconNum);
                    basestation.setMap(mapid+"");
                    basestation.setFloor(Short.valueOf(floor));
                    basestation.setX(Double.valueOf(x));
                    basestation.setY(Double.valueOf(y));
                    basestation.setZ(Double.valueOf(z));
                    baseService.addBasestation(basestation);*/
                Substation sub = new Substation();
                sub.setNum(beaconNum);
                sub.setMap(mapid + "");
                if (subService.addSub(sub, null)) {
                    BsConfig bsConfig = new BsConfig();
                    bsConfig.setMap(mapid);
                    bsConfig.setBsid(sub.getId());
                    bsConfig.setX(Double.valueOf(x));
                    bsConfig.setY(Double.valueOf(y));
                    bsConfig.setZ(Double.valueOf(z));
                    bsConfig.setFloor(Short.valueOf(floor));
                    bsConfigMapper.addDisparkBsConfig(bsConfig);
                    res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS), sub);
                }
                //res.setData(sub);
            } else {
                Substation sub = subService.findByNum(beaconNum);
                data.setFloor(Short.valueOf(floor));
                data.setX(Double.valueOf(x));
                data.setY(Double.valueOf(y));
                data.setZ(Double.valueOf(z));
                data.setMap(mapid);
                sub.setMap(mapid + "");
                bsConfigService.updateBsConfig(data);
                subMapper.updateSub(sub);
                res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS), sub);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return res;
    }

    @RequestMapping(value = "/recoverybeaconPos")
    @ApiOperation(value = "添加信标位置", notes = "111")
    public CommonResult<Object> addBeacon() {

        List<Basestation> list = baseMapper.findByAll(null, null, null, 75, 1, null, localUtil.getLocale());

        for (Basestation ba : list
        ) {
            BsConfig data = bsConfigMapper.findByNum(ba.getNum(), localUtil.getLocale());
            if (data == null) {
                 /*   Basestation basestation=new Basestation();
                    basestation.setNum(beaconNum);
                    basestation.setMap(mapid+"");
                    basestation.setFloor(Short.valueOf(floor));
                    basestation.setX(Double.valueOf(x));
                    basestation.setY(Double.valueOf(y));
                    basestation.setZ(Double.valueOf(z));
                    baseService.addBasestation(basestation);*/
                Substation sub = new Substation();
                sub.setNum(ba.getNum());
                sub.setMap(ba.getMap() + "");
                if (subService.addSub(sub, null)) {
                    BsConfig bsConfig = new BsConfig();
                    bsConfig.setMap(Integer.valueOf(ba.getMap()));
                    bsConfig.setBsid(sub.getId());
                    bsConfig.setX(ba.getX());
                    bsConfig.setY(ba.getY());
                    bsConfig.setZ(ba.getZ());
                    bsConfig.setFloor(ba.getFloor());
                    bsConfigMapper.addDisparkBsConfig(bsConfig);
                }
                // res.setData(sub);
            } else {

             /*   data.setFloor(Short.valueOf(floor));
                data.setX(Double.valueOf(x));
                data.setY(Double.valueOf(y));
                data.setZ(Double.valueOf(z));
                bsConfigService.updateBsConfig(data);*/
                // res.setData(basestation);

            }
        }

        return null;
    }

    @RequestMapping(value = "/getbeaconPos")
    @ApiOperation(value = "获取信标", notes = "111")
    public CommonResult<Object> getPlace(String beaconNum, String fmapID) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            if (fmapID != null) {
                Map_2d map_2d = map2dService.findByfmapID(fmapID);
                Integer mapid = null;
                if (map_2d != null) {
                    mapid = map_2d.getId();
                }
                //List<Basestation> data = baseService.findByAll(beaconNum, null, null, mapid, null, null);
                List<BsConfig> bsConfigs = bsConfigService.findByAll(mapid);
                res.setData(bsConfigs);
            } else {
                BsConfig beacon = bsConfigService.findByNum(beaconNum);
                if (beacon == null || beacon.getMap() == null) {
                    res.setCode(401);
                    res.setMessage("未获取到信标的地图信息");
                } else {
                    res.setData(beacon);
                }
            }

            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

}
