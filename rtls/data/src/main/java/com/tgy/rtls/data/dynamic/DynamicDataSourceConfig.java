//package com.tgy.rtls.data.dynamic;
//
//
//import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import javax.sql.DataSource;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @BelongsProject: rtls
// * @BelongsPackage: com.tgy.rtls.data.dynamic
// * @Author: wuwei
// * @CreateTime: 2022-11-22 22:06
// * @Description: TODO
// * @Version: 1.0
// */
//@Configuration
//public class DynamicDataSourceConfig {
//    /**
//     * 创建 DataSource Bean
//     * */
//
//    @Bean
//    @ConfigurationProperties("spring.datasource.druid.one")
//    public DataSource oneDataSource(){
//        DataSource dataSource = DruidDataSourceBuilder.create().build();
//        return dataSource;
//    }
//
//    @Bean
//    @ConfigurationProperties("spring.datasource.druid.two")
//    public DataSource twoDataSource(){
//        DataSource dataSource = DruidDataSourceBuilder.create().build();
//        return dataSource;
//    }
//
//    /**
//     * 将数据源信息载入targetDataSources
//     * */
//
//    @Bean
//    @Primary
//    public DynamicDataSource dataSource(DataSource oneDataSource, DataSource twoDataSource) {
//        Map<Object, Object> targetDataSources = new HashMap<>(2);
//        targetDataSources.put(DataSourceNames.ONE, oneDataSource);
//        targetDataSources.put(DataSourceNames.TWO, twoDataSource);
//        // 如果还有其他数据源,可以按照数据源one和two这种方法去进行配置，然后在targetDataSources中继续添加
//        System.out.println("加载的数据源DataSources:" + targetDataSources);
//
//        //DynamicDataSource（默认数据源,所有数据源） 第一个指定默认数据库
//        return new DynamicDataSource(oneDataSource, targetDataSources);
//    }
//}
