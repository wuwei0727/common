package com.tgy.rtls.web.config.pay.wxpay;

import com.tgy.rtls.data.tool.Constant;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.config.pay.wxpay
 * @Author: wuwei
 * @CreateTime: 2023-11-29 17:56
 * @Description: TODO
 * @Version: 1.0
 */
@Configuration
public class WxMpConfig {
    @Bean
    public WxMpService wxMpService() {
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxMpConfigStorage());
        return wxMpService;
    }

    @Bean
    public WxMpConfigStorage wxMpConfigStorage() {
        WxMpDefaultConfigImpl wxMpDefaultConfig = new WxMpDefaultConfigImpl();
        wxMpDefaultConfig.setAppId(Constant.ServiceNumber_APP_ID);
        wxMpDefaultConfig.setSecret(Constant.ServiceNumber_APP_SECRET);
        wxMpDefaultConfig.setToken("lrr");
        // wxMpDefaultConfig.setAppId("wxdac0b1f49f7f7e19");
        // wxMpDefaultConfig.setSecret("aa6340e7879af9835797b97189ccd2e4");
        return wxMpDefaultConfig;
    }

}