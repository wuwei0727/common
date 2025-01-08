//package com.tgy.rtls.web.controller.test.nacostest;
//
//import com.alibaba.nacos.api.config.annotation.NacosValue;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * @BelongsProject: rtls
// * @BelongsPackage: com.tgy.rtls.web.controller.test.nacostest
// * @Author: wuwei
// * @CreateTime: 2023-07-31 17:43
// * @Description: TODO
// * @Version: 1.0
// */
//@RestController
////@RefreshScope
//public class NacosTestController {
//    //使用@value注解取值，它能取到值，但没有自动更新的功能
//    @Value(value = "${server.ip}")
//    private String urlhead;
////    @Value(value = "${server.name}")
//    @NacosValue(value = "${server.name}",autoRefreshed = true)
//    private String urlheadAutoRefresh;
//
//    public void setUrlheadAutoRefresh(String urlheadAutoRefresh) {
//        this.urlheadAutoRefresh = urlheadAutoRefresh;
//    }
//
//    //使用@nacosValue注解获取值，并开启自动更新
//    @RequestMapping("/getValue")
//    public String getValue() {
//        return "urlhead:"+urlhead+"\n"+"urlheadAutoRefresh:"+urlheadAutoRefresh;
//    }
//}
