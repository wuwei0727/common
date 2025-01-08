package com.tgy.rtls.web.aspect;

import java.lang.annotation.*;

/**
 * 通用配置注解
 */
//定义注解用在方法上
@Target(ElementType.METHOD)
//定义保留策略，运行时使用
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyPermission {
//    String value() default "";
}
