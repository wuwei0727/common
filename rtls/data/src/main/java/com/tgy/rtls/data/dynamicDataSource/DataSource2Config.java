//package com.tgy.rtls.data.DynamicDataSource1;
//
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.mybatis.spring.SqlSessionFactoryBean;
//import org.mybatis.spring.SqlSessionTemplate;
//import org.mybatis.spring.annotation.MapperScan;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
//import org.springframework.jdbc.datasource.DataSourceTransactionManager;
//
//import javax.sql.DataSource;
//
///**
// * @BelongsProject: rtls
// * @BelongsPackage: com.tgy.rtls.data.DynamicDataSource1
// * @Author: wuwei
// * @CreateTime: 2022-11-17 21:16
// * @Description: TODO
// * @Version: 1.0
// */
//@Configuration
//@MapperScan(basePackages = "com.tgy.rtls.data.test02", sqlSessionTemplateRef  = "LocalSqlSessionTemplate")
//public class DataSource2Config {
//    @Bean(name = "LocalDataSource")
//    @ConfigurationProperties(prefix = "spring.datasource.nuoyi")
//    public DataSource testDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean(name = "LocalSqlSessionFactory")
//    public SqlSessionFactory localSqlSessionFactory(@Qualifier("LocalDataSource") DataSource dataSource) throws Exception {
//        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
//        bean.setDataSource(dataSource);
//        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:com.tgy.rtls.data.test01.mapper2/*.xml"));
//        return bean.getObject();
//    }
//
//    @Bean(name = "LocalTransactionManager")
//    public DataSourceTransactionManager localTransactionManager(@Qualifier("LocalDataSource") DataSource dataSource) {
//        return new DataSourceTransactionManager(dataSource);
//    }
//
//    @Bean(name = "LocalSqlSessionTemplate")
//    public SqlSessionTemplate localSqlSessionTemplate(@Qualifier("LocalSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
//        return new SqlSessionTemplate(sqlSessionFactory);
//    }
//}
