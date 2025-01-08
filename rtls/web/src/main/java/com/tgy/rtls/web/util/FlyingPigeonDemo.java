package com.tgy.rtls.web.util;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.util
 * @Author: wuwei
 * @CreateTime: 2023-04-07 14:05
 * @Description: TODO
 * @Version: 1.0
 */
public class FlyingPigeonDemo {
    public static void main(String[] args) throws Exception{
        HttpClient httpClient = HttpClients.createDefault();
        String name ="客户";
        String mapName ="香雪公寓停车场";
        String parkingName ="F0112";
        String startTime ="2022-01-12 12:30";
        String endTime ="2022-01-12 12:40";
        String urlLink ="https://wxaurl.cn/IA2RuOlxvYn";
        String param = name+"||"+mapName+"||"+parkingName+"||"+startTime+"||"+endTime+"||"+urlLink;
        HttpPost httpPost = new HttpPost("https://api.4321.sh/sms/template");
        httpPost.addHeader("Content-Type","application/json");
        Map<String,Object> map = new HashMap<>();
        map.put("apikey","N954897a80");
        map.put("secret","95489cb4fd1a9a92");
        map.put("sign_id","176360");
        map.put("template_id","137875");
        map.put("mobile","15625148902");
        map.put("content",param);
        String json = JSON.toJSONString(map);
        httpPost.setEntity(new StringEntity(json,"UTF-8"));
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        String res = EntityUtils.toString(entity);
        System.out.println(res);
    }
}
