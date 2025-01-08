package com.tgy.rtls.web.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tgy.rtls.data.tool.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.util
 * @Author: wuwei
 * @CreateTime: 2023-04-06 13:15
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
public class AccessTokenUtils {
    public static String getAccessToken() {
        // 获取连接客户端工具
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response;
        String entityStr;
        try {
            URIBuilder uriBuilder = new URIBuilder("https://api.weixin.qq.com/cgi-bin/token");
            uriBuilder.addParameter(Constant.KEY_APP_GRANT_TYPE, "client_credential");
            uriBuilder.addParameter(Constant.KEY_APP_ID, Constant.APP_ID);
            uriBuilder.addParameter(Constant.KEY_APP_SECRET, Constant.APP_SECRET);

            //if(NullUtils.isEmpty(appID)){
            //    uriBuilder.addParameter(Constant.KEY_APP_ID,Constant.APP_ID);
            //    uriBuilder.addParameter(Constant.KEY_APP_SECRET, Constant.APP_SECRET);
            //}else {
            //    uriBuilder.addParameter(Constant.KEY_APP_ID, appID);
            //    uriBuilder.addParameter(Constant.KEY_APP_SECRET, Constant.LOCATE_APP_SECRET);
            //}


            HttpGet httpGet = new HttpGet(uriBuilder.build());
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            entityStr = EntityUtils.toString(entity, "UTF-8");

            log.info("jsCode: " + ", " + entityStr);
            JSONObject entityJsonObject = JSON.parseObject(entityStr);
            String accessToken = null;
            if (entityJsonObject.get("errcode") != null) {
                return null;
            }

            if (entityJsonObject.get("access_token") != null) {
                accessToken = (String) entityJsonObject.get("access_token");
            }
            log.info("access_token:" + accessToken);

            return accessToken;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static void sendSms(String phone, String templateCode,String parpm) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
//        String name = "客户";
//        String mapName = "香雪公寓停车场";
//        String parkingName = "F0112";
//        String startTime = "2022-01-12 12:30";
//        String endTime = "2022-01-12 12:40";
//        String urlLink = "https://wxaurl.cn/IA2RuOlxvYn";
//        String param = name + "||" + mapName + "||" + parkingName + "||" + startTime + "||" + endTime + "||" + urlLink;
        HttpPost httpPost = new HttpPost("https://api.4321.sh/sms/template");
        httpPost.addHeader("Content-Type", "application/json");
        Map<String, Object> map = new HashMap<>();
        map.put("apikey", "N954897a80");
        map.put("secret", "95489cb4fd1a9a92");
        map.put("sign_id", "176360");
        map.put("template_id", templateCode);
        map.put("mobile", phone);
        map.put("content", parpm);
        String json = JSON.toJSONString(map);
        httpPost.setEntity(new StringEntity(json, "UTF-8"));
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        String res = EntityUtils.toString(entity);
        System.out.println(res);
    }
}