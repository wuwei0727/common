package com.tgy.rtls.docking.utils;

import java.util.Collection;
import java.util.Map;

/**
 * @author 许强
 * @Package com.example.util
 * @date 2019/6/13
 */
public class NullUtils {

    /*
    * 非空判断
    * */
    public static boolean isEmpty(Object obj){
        if (obj == null) {
            return true;
        }
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() == 0;
        }
        if (obj instanceof Collection) {
            return ((Collection) obj).isEmpty();
        }
        if (obj instanceof Map) {
            return ((Map) obj).isEmpty();
        }
        if (obj instanceof Object[]) {
            Object[] object = (Object[]) obj;
            if (object.length == 0) {
                return true;
            }
            boolean empty = true;
            for (int i = 0; i < object.length; i++) {
                if (!isEmpty(object[i])) {
                    empty = false;
                    break;
                }
            }
            return empty;
        }
        return false;
    }
}
