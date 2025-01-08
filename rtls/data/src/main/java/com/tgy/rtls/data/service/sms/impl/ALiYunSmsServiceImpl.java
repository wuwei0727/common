package com.tgy.rtls.data.service.sms.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.tgy.rtls.data.service.sms.ALiYunSmsService;
import com.tgy.rtls.data.tool.ConstantPropertiesUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.sms
 * @Author: wuwei
 * @CreateTime: 2023-04-07 13:55
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class ALiYunSmsServiceImpl implements ALiYunSmsService {
    @Value ("${aliyun.domain}")
    private String domain;
    @Value ("${aliyun.version}")
    private String version;
    @Value ("${aliyun.action}")
    private String action;
    @Value ("${aliyun.signName}")
    private String signName;

    @Override
    public Boolean sendMessage(String phone, String templateCode, Map<String,Object> templateParamJson) {
        DefaultProfile profile = DefaultProfile.getProfile(
                ConstantPropertiesUtils.REGION_Id, ConstantPropertiesUtils.ACCESS_KEY_ID, ConstantPropertiesUtils.SECRECT);
        IAcsClient client = new DefaultAcsClient(profile);
        // 构建请求：
        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain(domain);
        request.setVersion(version);
        request.setAction(action);

        // 自定义参数：
        request.putQueryParameter("PhoneNumbers",phone);
        request.putQueryParameter("SignName",signName);
        //模版Code
        request.putQueryParameter("TemplateCode", templateCode);

        // 构建短信验证码
        request.putQueryParameter("TemplateParam", JSONObject.toJSONString(templateParamJson));

        try {
            CommonResponse response = client.getCommonResponse(request);
            JSONObject jsonObject = JSON.parseObject(response.getData());
            String code = jsonObject.getString("Code");
            return "OK".equals(code);
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return false;
    }
}
