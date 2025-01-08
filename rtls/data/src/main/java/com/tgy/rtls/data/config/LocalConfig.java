package com.tgy.rtls.data.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * 描述: 2、配置国际化语言
 * 版权: Copyright (c) 2020
 * 公司: XXX
 * 作者: yanghj
 * 版本: 4.0
 * 创建日期: 2020/9/18 10:25
 */
@Configuration
public class LocalConfig {

    /**
     * 默认解析器 其中locale表示默认语言
     */
   @Value("${web.lang}")
    private String lang;
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
       Locale default_locale=Locale.CHINA;
        switch (lang){
            case "en_US":
                default_locale=Locale.US;
                break;
            case "ko_KR":
                default_locale=Locale.KOREA;
                break;
        }
        localeResolver.setDefaultLocale(default_locale);
        return localeResolver;
    }




    /**
     * 默认拦截器 其中lang表示切换语言的参数名
     */
    @Bean
    public WebMvcConfigurer localeInterceptor() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                LocaleChangeInterceptor localeInterceptor = new LocaleChangeInterceptor();
                localeInterceptor.setParamName("lang");  //拦截lang参数
                registry.addInterceptor(localeInterceptor);
            }
        };

    }

    /**
     * mybatis 自定义拦截器
     */
/*    @Bean
    public Interceptor getInterceptor(){
        return new MybatisSqlInterceptor();

    }*/

}
