//package com.tgy.rtls.web.config;
//
//import com.github.tobato.fastdfs.FdfsClientConfig;
//import com.github.tobato.fastdfs.domain.fdfs.StorePath;
//import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
//import com.github.tobato.fastdfs.service.FastFileStorageClient;
//import com.tgy.rtls.data.entity.map.Map_2d;
//import com.tgy.rtls.data.entity.park.ShangJia;
//import com.tgy.rtls.data.service.ImageSyncService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Import;
//
//import javax.annotation.PostConstruct;
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//
//@Configuration
//@Import(FdfsClientConfig.class)
//@Slf4j
//public class FastDFSConfig {
//    @Value("${file.uploadFolder}")
//    private String localStoragePath;
//
//    @Autowired
//    private FastFileStorageClient storageClient;
//
//    @Autowired
//    private ImageSyncService imageSyncService;
//
//    @PostConstruct
//    public void init() {
//        File directory = new File(localStoragePath);
//        if (!directory.exists()) {
//            directory.mkdirs();
//        }
//    }
//
//    @PostConstruct
//    public void syncImagesOnStartup() {
//        log.info("开始同步图片...");
//        syncImagesFromShangjia();
//        syncImagesFromMap2d();
//        log.info("图片同步完成");
//    }
//
//    private void syncImagesFromShangjia() {
//        List<ShangJia> shangjiaList = imageSyncService.findAllShangJia();
//        for (ShangJia shangjia : shangjiaList) {
//            // 同步 photo 字段
//            if (StringUtils.isNotEmpty(shangjia.getPhoto())&!"null".equals(shangjia.getPhoto())) {
//                syncSingleImage(shangjia.getPhoto());
//            }
//
//            // 同步 photo2 字段
//            if (StringUtils.isNotEmpty(shangjia.getPhoto2())&!"null".equals(shangjia.getPhoto2())) {
//                syncSingleImage(shangjia.getPhoto2());
//            }
//
//            // 同步 thumbnail 字段
//            if (StringUtils.isNotEmpty(shangjia.getThumbnail())&!"null".equals(shangjia.getThumbnail())) {
//                syncSingleImage(shangjia.getThumbnail());
//            }
//        }
//    }
//    private void syncImagesFromMap2d() {
//        List<Map_2d> map2ds = imageSyncService.findAllMap2d();
//        for (Map_2d map2d : map2ds) {
//            if (StringUtils.isNotEmpty(map2d.getQrcode())) {
//                syncSingleImage(map2d.getQrcode());
//            }
//
//            // 同步 photo2 字段
//            if (StringUtils.isNotEmpty(map2d.getWelcomePagePath())) {
//                syncSingleImage(map2d.getWelcomePagePath());
//            }
//
//            // 同步 thumbnail 字段
//            if (StringUtils.isNotEmpty(map2d.getMapLogo())) {
//                syncSingleImage(map2d.getMapLogo());
//            }
//        }
//
//    }
//
//
//
//    private void syncSingleImage(String fastdfsPath) {
//        try {
//            String localPath = localStoragePath + File.separator + getFileNameFromPath(fastdfsPath);
//            File localFile = new File(localPath);
//            if (!localFile.exists()) {
//                downloadFile(fastdfsPath, localPath);
//                log.info("成功同步图片: {}", fastdfsPath);
//            }
//        } catch (Exception e) {
//            log.error("同步图片失败: " + fastdfsPath, e);
//        }
//    }
//
//
//    private void downloadFile(String fastdfsPath, String localPath) throws IOException {
//        // 从 FastDFS 路径中提取 group 和 path
//        String group = fastdfsPath.substring(0, fastdfsPath.indexOf("/"));
//        String path = fastdfsPath.substring(fastdfsPath.indexOf("/") + 1);
//
//        // 下载文件
//        StorePath storePath = StorePath.parseFromUrl(fastdfsPath);
//        DownloadByteArray callback = new DownloadByteArray();
//        byte[] content = storageClient.downloadFile(group, path, callback);
//
//        // 保存到本地
//        FileUtils.writeByteArrayToFile(new File(localPath), content);
//    }
//
//    private String getFileNameFromPath(String fastdfsPath) {
//        return fastdfsPath.substring(fastdfsPath.lastIndexOf("/") + 1);
//    }
//}