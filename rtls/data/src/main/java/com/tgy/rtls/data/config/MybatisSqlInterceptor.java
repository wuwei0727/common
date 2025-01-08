package com.tgy.rtls.data.config;


import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.*;
import java.util.regex.Matcher;

/**
 * 用于记录mybatis中的sql中不包含officeId的sql
 * Created by shiwen on 2017/9/12.
 * <p>
 */

@Intercepts({
     /*   @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),*/
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class})})
public class MybatisSqlInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(MybatisSqlInterceptor.class);
    private static final Logger sqlLogger = LoggerFactory.getLogger("mybatisSql");



    private static String [] officeIdNames = new String[]{"OFFICE_ID","OFFICEID","PK_OFFICE_ID"};

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];

            //0.sql参数获取
            Object parameter = null;
            if (invocation.getArgs().length > 1) {
                parameter = invocation.getArgs()[1];
            }

            //1.获取sqlId
            String sqlId = mappedStatement.getId();
            BoundSql boundSql = mappedStatement.getBoundSql(parameter);

            Configuration configuration = mappedStatement.getConfiguration();

            //获取真实的sql语句
            String sql = getSql(configuration, boundSql, sqlId, 0);
              logger.info(sql);

   /*         //2.判断是否有officeId
            if (hasOfficeId(sql,officeIdNames)) {
                sqlLogger.warn("{}", sql);
            } else {
                sqlLogger.debug("{}", sql);
            }*/
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return invocation.proceed();
    }


    /**
     * 判断sql语句中是否包含officeId字段
     *
     * @param sql sql语句
     * @return
     */
    private boolean hasOfficeId(String sql,String[] officeIdNames) {
        //office ID 的可能名称
        if (sql == null || sql.trim().length() == 0) {
            return false;
        }

        String afterWhereStatement = sql.toUpperCase().substring(sql.indexOf("where"));

        for (String officeIdName : officeIdNames){
            if(afterWhereStatement.indexOf(officeIdName) > 0){
                return true;
            }
        }

        return false;
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {


    }


    private static String getSql(Configuration configuration, BoundSql boundSql,
                                 String sqlId, long time) {
        String sql = showSql(configuration, boundSql);
        StringBuilder str = new StringBuilder(100);
        str.append(sqlId);
        str.append(":");
        str.append(sql);
        return str.toString();
    }

    private static String getParameterValue(Object obj) {
        String value = null;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(
                    DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(new Date()) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }

        }
        return value;
    }

    private static String showSql(Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (!parameterMappings.isEmpty() && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration
                    .getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?",
                        Matcher.quoteReplacement(getParameterValue(parameterObject)));

            } else {
                MetaObject metaObject = configuration
                        .newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql
                                .getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                    } else {
                        sql = sql.replaceFirst("\\?", "缺失");
                    }//打印出缺失，提醒该参数缺失并防止错位
                }
            }
        }
        return sql;
    }
}