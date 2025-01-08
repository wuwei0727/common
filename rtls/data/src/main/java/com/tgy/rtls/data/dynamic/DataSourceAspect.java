//package com.tgy.rtls.data.dynamic;
//
//import com.tgy.rtls.data.dynamic.DataSource;
//import com.tgy.rtls.data.dynamic.DynamicDataSource;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.core.Ordered;
//import org.springframework.stereotype.Component;
//
//import java.lang.reflect.Method;
//
///**
// * @BelongsProject: rtls
// * @BelongsPackage: com.tgy.rtls.data
// * @Author: wuwei
// * @CreateTime: 2022-11-22 22:03
// * @Description: TODO
// * @Version: 1.0
// */
//
//@Aspect
//@Component
//public class DataSourceAspect implements Ordered {
//    protected Logger logger = LoggerFactory.getLogger(getClass());
//
//    /**
//     * 切点: 所有配置 DataSource 注解的方法
//     */
//    @Pointcut("@annotation(com.tgy.rtls.data.dynamic.DataSource)")
//    public void dataSourcePointCut() {}
//
//    @Around("dataSourcePointCut()")
//    public Object around(ProceedingJoinPoint point) throws Throwable {
//        MethodSignature signature = (MethodSignature) point.getSignature();
//        Method method = signature.getMethod();
//        DataSource ds = method.getAnnotation(DataSource.class);
//        // 通过判断 DataSource 中的值来判断当前方法应用哪个数据源
//        DynamicDataSource.setDataSource(ds.value());
//        logger.info("AOP切换数据源成功，数据源为: " + ds.value());
//        logger.info("set datasource is " + ds.value());
//        try {
//            return point.proceed();
//        } finally {
//            DynamicDataSource.clearDataSource();
//            logger.info("clean datasource");
//        }
//    }
//
//    @Override
//    public int getOrder() {
//        return 1;
//    }
//}
