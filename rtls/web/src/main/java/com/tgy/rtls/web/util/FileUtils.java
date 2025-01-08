package com.tgy.rtls.web.util;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tgy.rtls.data.common.EncodeUtils;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.message.VoiceRecord;
import com.tgy.rtls.web.config.Para;
import com.tgy.rtls.web.config.SpringContextHolder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.util
 * @date 2020/10/15
 * 文件处理工具类
 */
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
    /*
    * 图片存储 存储到本地 随机获取文件名称
    * */
    public static CommonResult<Object> uploadFile(String uploadFolder, MultipartFile file,String name) {
        try {

            //找到存储图片的文件夹  没有就创建
            File file1=new File(uploadFolder);
            if(!file1.exists()){
                file1.mkdirs();
            }
           // String fileName = file.getOriginalFilename();
           // String suffixName = fileName.substring(fileName.lastIndexOf("."));
            // 文件上传后的路径
            //fileName = UUID.randomUUID() + suffixName;
            File targetFile = new File(uploadFolder+name);
            file.transferTo(targetFile);
            return new CommonResult<>(200,"存储成功",name);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,"文件处理出现错误");
        }

    }

    /*
     * 图片存储 存储到本地 手动输入文件名称
     * */
    public static CommonResult<Object> uploadFileName(String uploadFolder, MultipartFile file,String name) {
        try {
            String fileExtName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
       /*     if (!"jpg".equals(fileExtName) && !"png".equals(fileExtName)&&!"zip".equals(fileExtName)) {
                return new CommonResult<>(400,"文件格式不正确");
            }*/

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
    /*
    * 语音解析 file-->语音文件  uploadFolder-->实际位置 url-->映射位置 http-->ip:port  voice-->语音相关信息 personids-->下发给到的人员id
    * */
    public static CommonResult<Object> analysisVoice(MultipartFile file, String uploadFolder, String url, VoiceRecord voice){
        try {
            File folder=new File(uploadFolder);
            if (!folder.exists()){
                folder.mkdirs();
            }
            String newName= UUID.randomUUID() +".wav";
            file.transferTo(new File(folder,newName));
            File file1=new File(uploadFolder+"/analysis");
            if (!file1.exists()){
                file1.mkdirs();
            }
            //解析后的文件路径
            voice.setFile(url+"/analysis/"+newName);
            ProcessAudio.encodefile(uploadFolder+"/"+newName,uploadFolder+"/analysis/"+newName);
            return new CommonResult<>(200,"传输成功",url+newName);
        }catch(Exception e){
            e.printStackTrace();
            return new CommonResult<>(400,"系统异常！");
        }
    }
   // file 转mutilefile
    public static FileItem createFileItem(String filePath)
    {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        String textFieldName = "textField";
        int num = filePath.lastIndexOf(".");
        String extFile = filePath.substring(num);
        FileItem item = factory.createItem(textFieldName, "text/plain", true,
                "MyFileName" + extFile);
        File newfile = new File(filePath);
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        try
        {
            FileInputStream fis = new FileInputStream(newfile);
            OutputStream os = item.getOutputStream();
            while ((bytesRead = fis.read(buffer, 0, 8192))
                    != -1)
            {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            fis.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return item;
    }


    //用于标签调试
    public static CommonResult<Object> debugVoice(MultipartFile file, String uploadFolder, String url){
        try {
            File folder=new File(uploadFolder);
            if (!folder.exists()){
                folder.mkdirs();
            }
            String newName= UUID.randomUUID() +".wav";
            file.transferTo(new File(folder,newName));
            File file1=new File(uploadFolder+"/analysis");
            if (!file1.exists()){
                file1.mkdirs();
            }
            //解析后的文件路径
            ProcessAudio.encodefile(uploadFolder+"/"+newName,uploadFolder+"/analysis/"+newName);
            return new CommonResult<>(200,"传输成功",uploadFolder+"/analysis/"+newName);
        }catch(Exception e){
            e.printStackTrace();
            return new CommonResult<>(400,"系统异常！");
        }
    }

    //音频解析 不传kafka
    public static CommonResult<Object> analysisVoice(MultipartFile file, String uploadFolder, String url, String filename){
        try {
            File folder=new File(uploadFolder);
            if (!folder.exists()){
                folder.mkdirs();
            }
            String newName= filename +".wav";
            ProcessAudio.decodefile(file.getBytes(),uploadFolder+"/"+newName);
            return new CommonResult<>(200,"传输成功",url+newName);
        }catch(Exception e){
            e.printStackTrace();
            return new CommonResult<>(400,"系统异常！");
        }
    }
    /*
    * 文件存储
    * */
    public static CommonResult<Object> addFile(MultipartFile file,String uploadFolder){
        try {
            File folder=new File(uploadFolder);
            if (!folder.exists()){
                folder.mkdirs();
            }
            String newName=file.getOriginalFilename();
            file.transferTo(new File(folder,newName));
            return new CommonResult<>(200,"传输成功",uploadFolder+"/"+newName);
        }catch(Exception e){
            e.printStackTrace();
            return new CommonResult<>(400,"系统异常！");
        }
    }

    /*
    * 上传升级包
    * */
    public static CommonResult<Object> upgradeFile(MultipartFile file,String uploadFolder) {
        try {
            File folder=new File(uploadFolder);
            if (!folder.exists()){
                folder.mkdirs();
            }
            String newName=file.getOriginalFilename();
            file.transferTo(new File(folder,newName));
            return new CommonResult<>(200,"传输成功",newName);
        }catch(Exception e){
            e.printStackTrace();
            return new CommonResult<>(400,"系统异常！");
        }
    }

    /**
     * zip文件解压
     * @param inputFile  待解压文件夹/文件
     * @param destDirPath  解压路径
     */
    public static String zipUncompress(String inputFile,String destDirPath) throws Exception {
        String name=null;
        File srcFile = new File(inputFile);//获取当前压缩文件
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            throw new Exception(srcFile.getPath() + "所指文件不存在");
        }

        String fileEncode = EncodeUtils.getEncode(inputFile,true);

        ZipFile zipFile = new ZipFile(srcFile, Charset.forName(fileEncode));//创建压缩文件对象
        //开始解压
        Enumeration<?> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            String sub=entry.getName().substring(entry.getName().lastIndexOf(".")+1);
            if (sub.equals("zip")){
                name=entry.getName().substring(0,entry.getName().lastIndexOf("."));
            }
            // 如果是文件夹，就创建个文件夹
            if (entry.isDirectory()) {
                String dirPath = destDirPath + "/" + entry.getName();
                srcFile.mkdirs();
            } else {
                // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                File targetFile = new File(destDirPath + "/" + entry.getName());
                // 保证这个文件的父文件夹必须要存在
                if (!targetFile.getParentFile().exists()) {
                    targetFile.getParentFile().mkdirs();
                }
                targetFile.createNewFile();
                // 将压缩文件内容写入到这个文件中
                InputStream is = zipFile.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(targetFile);
                int len;
                byte[] buf = new byte[1024];
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                }
                // 关流顺序，先打开的后关闭
                fos.close();
                is.close();
            }
        }
        return name;
    }

   public static void  pullRemoteFileToLocal(String remoteUrl){

           try {

                   if (!NullUtils.isEmpty(remoteUrl)) {
                       //判断压缩包路径是否存在
                       Para Para = SpringContextHolder.getBean(com.tgy.rtls.web.config.Para.class);
                       File file = new File(Para.uploadFolder + remoteUrl);
                       if (!file.exists()) {//不存在 就拉取创建
                           URL url = new URL(Para.fdfsUrl + remoteUrl);
                           URLConnection urlConnection = url.openConnection();
                           HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
                           httpURLConnection.setConnectTimeout(1000 * 5);
                           httpURLConnection.setRequestMethod("GET");
                           httpURLConnection.setRequestProperty("Charset", "UTF-8");
                           // 打开到此 URL引用的资源的通信链接（如果尚未建立这样的连接）。
                           int fileLength = httpURLConnection.getContentLength();
                           httpURLConnection.connect();
                           // 建立链接从请求中获取数据
                           BufferedInputStream bin = new BufferedInputStream(httpURLConnection.getInputStream());
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

                                           }
                   }
               }
            catch (Exception e) {

           }
       }


}
