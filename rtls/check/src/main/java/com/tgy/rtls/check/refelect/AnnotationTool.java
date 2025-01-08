package com.tgy.rtls.check.refelect;

import com.tgy.rtls.data.common.Descrip;
import org.apache.commons.collections.map.ListOrderedMap;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AnnotationTool {
    /**
     * 获取打了Desc注解的字典属性列表
     * @param
     * @return 字典属性列表
     */
    public static <T> List<Map> getFixedVoList(List list) {

        List res = new ArrayList();
        int len = list.size();
        for (int i = 0; i < len; i++) {
            Object o = list.get(i);
            Map map = new ListOrderedMap();
            try {

                Field[] fields = o.getClass().getDeclaredFields();


                for (Field field : fields) {
                    Descrip descrip = field.getAnnotation(Descrip.class);
                    if (descrip != null) {
                        String para_descrip = descrip.value();
                        Object para_value = (Object) field.get(o);
                        Object value;
                        if(para_value instanceof Date){
                            Timestamp value1=new Timestamp((((Date) para_value).getTime()));
                            value=value1.toString();
                        }else{
                            value=para_value;
                        }
                        System.out.println(para_descrip + ":" + value);
                        map.put(para_descrip, value);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            res.add(map);

        }
        return res;

    }
    public static void main(String[] args) {

      /*  TagcheckdetailEntity ss = new TagcheckdetailEntity();
        ss.bsid=1000l;
        ss.time= LocalDateTime.now();
        TagcheckdetailEntity ss1 = new TagcheckdetailEntity();
        ss1.bsid=100l;
        ss1.time= LocalDateTime.now();
        List ll=new ArrayList();
        ll.add(ss);
        ll.add(ss1);
        List<Map> sss = getFixedVoList(ll);
        System.out.println(ll.size());*/

    }
}
