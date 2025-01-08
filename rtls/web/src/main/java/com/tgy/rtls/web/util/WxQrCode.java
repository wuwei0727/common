package com.tgy.rtls.web.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tgy.rtls.data.tool.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


/**
 * @BelongsProject: 智慧停车场
 * @BelongsPackage: com.tgy.rtls.web.util
 * @Author: wuwei
 * @CreateTime: 2022-07-22 09:36
 * @Description: TODO
 * @Version: 1.0
 */
@Component
@Slf4j
public class WxQrCode {
    /**
     *上传真实地址
     */
    @Value("${file.uploadFolder}")
    private String uploadFolder;

    private static String uploadPath;

    /**
     *解决spring静态注入
     */
    @PostConstruct
    public void init() {
        //静态赋值给非静态的
        uploadPath = uploadFolder;
    }

    /**
     * 获取AccessToken路径
     * 小程序id
     */
    private static final String ACCESS_TOKEN_URL="https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
    /**
     * 获取二维码路径
     * 小程序密钥
     */
    private static final String WX_CODE_URL="https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=ACCESS_TOKEN";
    private static final String APPID="wxf0f25ad3fc36365e";
    private static final String APPSECRET="79c8b7c3e5cd646c08444bcdfd0b65c8";

    private static final String IP="172.18.0.3";


    /**
     * 用于获取access_token
     * @return access_token
     * @throws Exception
     */
    public static String getAccessToken(String appid, String secret) throws Exception {
        String requestUrl = ACCESS_TOKEN_URL.replace("APPID", appid).replace("APPSECRET", secret);
        URL url = new URL(requestUrl);
        // 打开和URL之间的连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        // 设置通用的请求属性
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setUseCaches(false);
        //post必须写下面的俩个方法
        connection.setDoOutput(true);
        connection.setDoInput(true);

        // 得到请求的输出流对象
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.writeBytes("");
        out.flush();
        out.close();

        // 建立实际的连接
        connection.connect();
        // 定义 BufferedReader输入流来读取URL的响应
        BufferedReader in = null;
        if (requestUrl.contains("nlp")) {
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        } else {
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        }
        String result = "";
        String getLine;
        while ((getLine = in.readLine()) != null) {
            result += getLine;
        }
        in.close();
        JSONObject jsonObject = JSON.parseObject(result);
        return jsonObject.getString("access_token");
    }

    /**
     * 获取 二维码图片
     *
     */
    public static String getMapQrCode(String mapId) {
        //文件生成时间时间
        String toDay = new SimpleDateFormat("yyyyMMdd").format(new Date());
        //打乱文件名方式重复
        String uuid = UUID.randomUUID().toString().replace("-", "");
        //文件目录
        String dir = "img";
        //文件名
        String fileName = "park";
        //文件后缀
        String suffix = ".png";
        //本地生成文件目录
        File file = new File(uploadPath + File.separator + dir);
        //本地没有就创建
        if (!file.exists()) {
            file.mkdirs();// 创建文件根目录
        }
        //保存本地路径
        String savePath = file.getPath() + File.separator + fileName + toDay + uuid + mapId + suffix;
        if (savePath.contains("\\")) {
            savePath = savePath.replace("\\", "/");
        }
        InputStream inputStream;
        try {
            // 获取token
            String token = getAccessToken(Constant.APP_ID,Constant.APP_SECRET);
            String wxCodeUrl = WX_CODE_URL.replace("ACCESS_TOKEN", token);
            URL url = new URL(wxCodeUrl);
            HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
            // 提交模式
            httpUrlConnection.setRequestMethod("POST");
            // 发送POST请求必须设置如下两行
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            PrintWriter printWriter = new PrintWriter(httpUrlConnection.getOutputStream());
            // 二维码中信息
            String mid = "mapId=";
            // 发送请求参数
            JSONObject paramJson = new JSONObject();
            //二维码要跳转的页面
            paramJson.put("page", "pages/map/map");
            // 携带参数
            paramJson.put("scene", String.join("", mid, mapId));
            // if("172.0.1.12".equals(Inet4Address.getLocalHost().getHostAddress())){
               paramJson.put("env_version","release");
            // }else {
            //      paramJson.put("env_version","trial");
            // }
            printWriter.write(paramJson.toString());
            // flush输出流的缓冲
            printWriter.flush();
            //开始获取数据
            inputStream = new BufferedInputStream(httpUrlConnection.getInputStream());
            OutputStream os = new FileOutputStream(savePath);
            int len;
            byte[] arr = new byte[1024];
            while ((len = inputStream.read(arr)) != -1) {
                os.write(arr, 0, len);
                os.flush();
            }
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return savePath;
    }

    //这是一个main方法，程序的入口
    public static void main(String[] args){
        ServerProperties p = new ServerProperties();
        System.out.println(p.getPort()+"p = " + p.getAddress());
    }
}

