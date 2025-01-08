package com.tgy.rtls.web.config.pay.alipay;

import com.ijpay.alipay.AliPayApiConfig;
import com.ijpay.alipay.AliPayApiConfigKit;
import com.tgy.rtls.data.entity.pay.AliPayBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.config.pay.alipay
 * @Author: wuwei
 * @CreateTime: 2023-11-13 10:16
 * @Description: TODO
 * @Version: 1.0
 */
@Configuration
public class AliPayClientConfig {
    @Resource
    private AliPayBean aliPayBean;
    @Bean
    public AliPayApiConfig getApiConfig() {

        AliPayApiConfig aliPayApiConfig = AliPayApiConfig.builder()
                .setAppId(aliPayBean.getAppId())
                .setAliPayPublicKey(aliPayBean.getPublicKey())
                .setPrivateKey(aliPayBean.getPrivateKey())
                .setServiceUrl(aliPayBean.getServerUrl())
                .build();
        AliPayApiConfigKit.setThreadLocalAppId(aliPayBean.getAppId());
        AliPayApiConfigKit.setThreadLocalAliPayApiConfig(aliPayApiConfig);
        return aliPayApiConfig;
    }
}
