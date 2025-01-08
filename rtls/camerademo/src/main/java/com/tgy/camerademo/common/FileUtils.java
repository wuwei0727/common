package com.tgy.camerademo.common;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Component
public class FileUtils {

    public static CommonResult<Object> uploadBase64ImageToFastDFS(String base64Str, String uploadFolder) throws IOException {
        // 解码 Base64 字符串
        byte[] imageBytes = Base64.decodeBase64(base64Str);

        // 将字节数组转换为 InputStream
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);

        // 生成临时 MultipartFile 文件对象
        MultipartFile file = createCommonsMultipartFile(inputStream, imageBytes.length, generateUniqueFileName("jpg"));
        // 获取文件扩展名
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());

        FastFileStorageClient fastFileStorageClient = SpringContextHolder.getBean(FastFileStorageClient.class);
        // 上传到 FastDFS
        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);

        // 调用处理文件名的方法
        CommonResult<Object> finalFile = uploadFileName(uploadFolder, file, storePath.getFullPath());
        finalFile.setData(storePath.getFullPath());

        return finalFile;
    }

    private static CommonsMultipartFile createCommonsMultipartFile(InputStream inputStream, long size, String fileName) throws IOException {
        // 使用 DiskFileItem 来包装文件数据
        DiskFileItem fileItem = new DiskFileItem("file", "image/jpeg", true, fileName, (int) size, new File(System.getProperty("java.io.tmpdir")));
        try (InputStream fileContent = inputStream) {
            // 将 InputStream 中的内容写入到 DiskFileItem
            IOUtils.copy(fileContent, fileItem.getOutputStream());
        }
        // 使用 DiskFileItem 创建 CommonsMultipartFile 对象
        return new CommonsMultipartFile(fileItem);
    }

    /**
     * 生成一个唯一的文件名
     *
     * @param extension 文件扩展名（不带点，例如 "jpg" 或 "png"）
     * @return 生成的唯一文件名
     */
    public static String generateUniqueFileName(String extension) {
        // 使用 UUID 生成唯一标识符
        String uuid = UUID.randomUUID().toString();

        // 使用当前时间戳
        long timestamp = System.currentTimeMillis();

        // 组合 UUID 和时间戳，生成唯一文件名
        return uuid + "_" + timestamp + "." + extension;
    }

    /*
     * 图片存储 存储到本地 手动输入文件名称
     * */
    public static CommonResult<Object> uploadFileName(String uploadFolder, MultipartFile file,String name) {
        try {
            //找到存储图片的文件夹  没有就创建
            File file1=new File(uploadFolder+name.substring(0,name.lastIndexOf("/")));
            if(!file1.exists()){
                file1.mkdirs();
            }
            File targetFile = new File(uploadFolder+name);
            file.transferTo(targetFile);
            return new CommonResult<>(200,"存储成功",name);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,"文件处理出现错误");
        }

    }
}