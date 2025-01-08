package com.tgy.rtls.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Para {
    @Value("${file.url}")
    public String url;
    //上传真实地址
    @Value("${file.uploadFolder}")
    public String uploadFolder;
    @Value("${fdfs.url}")
    public String fdfsUrl;
}
