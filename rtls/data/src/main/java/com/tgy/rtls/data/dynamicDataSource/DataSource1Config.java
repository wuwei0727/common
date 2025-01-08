//package com.tgy.rtls.data.dynamicDataSource;
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
//import org.springframework.context.annotation.Primary;
//import org.springframework.jdbc.datasource.DataSourceTransactionManager;
//
//import javax.sql.DataSource;
///**
// * @BelongsProject: rtls
// * @BelongsPackage: com.tgy.rtls.data.DynamicDataSource1
// * @Author: wuwei
// * @CreateTime: 2022-11-17 21:04
// * @Description: TODO
// * @Version: 1.0
// */
//@Configuration
//@MapperScan(basePackages = "com.tgy.rtls.data.test01", sqlSessionTemplateRef  = "RemoteSqlSessionTemplate")
//public class DataSource1Config {
//    @Bean(name="RemoteDataSource")//注入到这个容器
//    @ConfigurationProperties(prefix="spring.datasource.master")//表示取application.properties配置文件中的前缀
//    @Primary//primary是设置优先，因为有多个数据源，在没有明确指定用哪个的情况下，会用带有primary的，这个注解必须有一个数据源要添加
//    public DataSource testDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean(name="RemoteSqlSessionFactory")
//    @Primary
//    //@Qualifier("xxx")的含义是告诉他使用哪个DataSource
//    public SqlSessionFactory testSqlSessionFactory(@Qualifier("RemoteDataSource") DataSource dataSource) throws Exception {
//        SqlSessionFactoryBean bean=new SqlSessionFactoryBean();
//        bean.setDataSource(dataSource);
//        return bean.getObject();
//    }
//    @Bean(name="RemoteTransactionManager")//配置事务
//    @Primary
//    public DataSourceTransactionManager testTransactionManager(@Qualifier("RemoteDataSource") DataSource dataSource) {
//        return new DataSourceTransactionManager(dataSource);
//    }
//    @Bean(name="RemoteSqlSessionTemplate")
//    @Primary
//    public SqlSessionTemplate testSqlSessionTemplate(@Qualifier("RemoteSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
//        return new SqlSessionTemplate(sqlSessionFactory);
//    }
//}
