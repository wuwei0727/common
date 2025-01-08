package com.tgy.rtls.data.common;


import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 描述: 3、国际化工具类
 * 版权: Copyright (c) 2020
 * 公司: XXXX
 * 作者: yanghj
 * 版本: 4.0
 * 创建日期: 2020/9/18 10:31
 */

@Component
public class LocalUtil
{

    private static MessageSource messageSource;
    @Value("${web.lang}")
    private String lang;

    public LocalUtil(MessageSource messageSource)
    {
        LocalUtil.messageSource = messageSource;
    }

    /**
     * 获取单个国际化翻译值
     */
    public static String get(String msgKey)
    {
        try
        {
            return messageSource.getMessage(msgKey, null, LocaleContextHolder.getLocale());
        }
        catch (Exception e)
        {
            return msgKey;
        }
    }
    /**
     * 获取单个国际化翻译值
     */
    public  String getLocale()
    {
        String locale="name";
        switch (lang){
            case "ko_KR":
                locale="name_ko";
                break;
            case "zh_CN":
                break;
            case "en_US":
                locale="name_en";
                break;
        }
        return locale;

    }

    public Locale getCurrentLocale() {
        Locale default_locale=Locale.CHINA;
        switch (lang){
            case "en_US":
                default_locale=Locale.US;
                break;
            case "ko_KR":
                default_locale=Locale.KOREA;
                break;
        }
        return default_locale;
    }

    public static Map<String, Object> getKeyAndValue(Object obj) {
        Map<String, Object> map = new HashMap<String, Object>();
        // 得到类对象
        Class userCla = (Class) obj.getClass();
        /* 得到类中的所有属性集合 */
        Field[] fs = userCla.getDeclaredFields();
        for (int i = 0; i < fs.length; i++) {
            Field f = fs[i];
            f.setAccessible(true); // 设置些属性是可以访问的
            Object val = new Object();
            try {
                val = f.get(obj);
                // 得到此属性的值
                map.put(f.getName(), val);// 设置键值
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            /*
             * String type = f.getType().toString();//得到此属性的类型 if
             * (type.endsWith("String")) {
             * System.out.println(f.getType()+"\t是String"); f.set(obj,"12") ;
             * //给属性设值 }else if(type.endsWith("int") ||
             * type.endsWith("Integer")){
             * System.out.println(f.getType()+"\t是int"); f.set(obj,12) ; //给属性设值
             * }else{ System.out.println(f.getType()+"\t"); }
             */

        }
     //   System.out.println("单个对象的所有键值==反射==" + map.toString());
        return map;
    }

    public static Map<String, Object> getKeyAndValueAnnotation(Object obj) {
        Map<String, Object> map = new HashMap<String, Object>();
        // 得到类对象
        Class userCla = (Class) obj.getClass();
        /* 得到类中的所有属性集合 */
        Field[] fs = userCla.getDeclaredFields();
        for (int i = 0; i < fs.length; i++) {
            Field f = fs[i];
            f.setAccessible(true); // 设置些属性是可以访问的
            Descrip descrip = f.getAnnotation(Descrip.class);
            if (descrip != null) {
                try {
                    Object val = new Object();
                    val = f.get(obj);
                    // 得到此属性的值
                    map.put(f.getName(), val);// 设置键值
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
        //   System.out.println("单个对象的所有键值==反射==" + map.toString());
        return map;
    }


  public static StringBuilder haoXiangDataProcess(Map<String, Object> map){
       // JSONArray jsonArray=new JSONArray();
      StringBuilder res=new StringBuilder("{");
       long time = new Date().getTime();
       int size=map.size();
       int i=0;
       for(Map.Entry<String, Object> entry : map.entrySet()){
           JSONObject jsonObject=new JSONObject();
           jsonObject.put("time",time);
           jsonObject.put("value",entry.getValue());
           JSONObject jsonObject2=new JSONObject();
           jsonObject2.put(entry.getKey(),jsonObject);
           String s= jsonObject2.toString();
           int len=s.length();
           i++;
           String sd=s.substring(1,len-1);
           if(i==size)
            res.append(sd);
           else {
               res.append(sd);
               res.append(",");
           }
       }
       res.append("}");
       return res;
    }
    public static JSONObject haoXiangDataProcessJson(Map<String, Object> map){
        JSONObject jsonObject2=new JSONObject();

        long time = new Date().getTime();
        int size=map.size();
        int i=0;
        for(Map.Entry<String, Object> entry : map.entrySet()){
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("time",time);
            jsonObject.put("value",entry.getValue());
            jsonObject2.put(entry.getKey(),jsonObject);
        }

        return jsonObject2;
    }

}
