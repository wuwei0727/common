package com.tgy.rtls.data.service.sms.impl;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service("smsService")
public class SmsService{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public void sendPhone(String phoneNum,String code){
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://api.4321.sh/sms/send");
        httpPost.addHeader("Content-Type","application/json");
        Map<String,Object> map = new HashMap<>();
        map.put("apikey","N727579dca");
        map.put("secret","727579f7727ac6b2");
        map.put("sign_id","142972");
        map.put("mobile",phoneNum);
        map.put("content",code);
        String json = JSON.toJSONString(map);
        httpPost.setEntity(new StringEntity(json,"UTF-8"));
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String res = EntityUtils.toString(entity);
            logger.info("SMS send res:"+res);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}

