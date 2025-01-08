package com.tgy.rtls.web.config.pay.wxpay;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.WxMaConfig;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import com.tgy.rtls.data.tool.Constant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.config.pay.wxpay
 * @Author: wuwei
 * @CreateTime: 2023-12-01 15:54
 * @Description: TODO
 * @Version: 1.0
 */
@Configuration
public class WeChatMpConfig {

    @Bean
    public WxMaService wxMaService(){
        WxMaServiceImpl wxMaService = new WxMaServiceImpl();
        wxMaService.setWxMaConfig(wxMaConfig());
        return wxMaService;
    }

    @Bean
    public WxMaConfig wxMaConfig(){
        WxMaDefaultConfigImpl wxMaConfig = new WxMaDefaultConfigImpl();
        wxMaConfig.setAppid(Constant.APP_ID);
        wxMaConfig.setSecret(Constant.APP_SECRET);
        return wxMaConfig;
    }
}
