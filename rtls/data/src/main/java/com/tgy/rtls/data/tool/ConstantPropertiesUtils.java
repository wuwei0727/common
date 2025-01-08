package com.tgy.rtls.data.tool;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.util
 * @Author: wuwei
 * @CreateTime: 2023-04-07 13:52
 * @Description: TODO
 * @Version: 1.0
 */

@Component
public class ConstantPropertiesUtils implements InitializingBean {
    @Value("${aliyun.sms.regionId}")
    private String regionId;

    @Value("${aliyun.sms.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.sms.secret}")
    private String secret;

    public static String REGION_Id;
    public static String ACCESS_KEY_ID;
    public static String SECRECT;

    @Override
    public void afterPropertiesSet() {
        REGION_Id=regionId;
        ACCESS_KEY_ID=accessKeyId;
        SECRECT=secret;
    }

}
