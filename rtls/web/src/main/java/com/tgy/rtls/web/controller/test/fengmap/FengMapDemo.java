package com.tgy.rtls.web.controller.test.fengmap;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.test.fengmap
 * @Author: wuwei
 * @CreateTime: 2024-06-24 11:54
 * @Description: TODO
 * @Version: 1.0
 */
public class FengMapDemo {

    public static void main(String[] args) {
        //秘钥
        String sKey = "077d852db60d29357ee847288338132e";
        String timestamp = String.valueOf(System.currentTimeMillis());
        System.out.println(timestamp);

        //请求参数
        Map<String, String> hMap = new HashMap<>();
        hMap.put("timestamp", timestamp);
        hMap.put("uuid", "4861ae982363116caf870d65c45bc6a2");
        //获取签名
        String sign = sign(hMap, sKey);
        System.out.println(sign);
    }


    /**
     * 签名生成
     * @param map 请求参数
     * @param key 秘钥
     * @return 签名
     */
    private static String sign(Map<String, String> map, String key){
        //1:将key排序
        map = sortMapByKey(map);
        //2:拼接参数
        StringBuilder keyStr = new StringBuilder();
        for(Map.Entry<String, String> entry : map.entrySet()){
            keyStr.append(entry.getKey());
            keyStr.append(entry.getValue());
        }
        keyStr.append(key);
        //3：签名
        return calculateMd5(keyStr.toString());
    }


    private static Map<String, String> sortMapByKey(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Map<String, String> sortMap = new TreeMap<>(Comparator.naturalOrder());
        sortMap.putAll(map);
        return sortMap;
    }



    private static String calculateMd5(String sSecret) {
        try {
            MessageDigest bmd5 = MessageDigest.getInstance("MD5");
            bmd5.update(sSecret.getBytes());
            StringBuilder buf = new StringBuilder();
            byte[] b = bmd5.digest();
            int i;
            for (byte value : b) {
                i = value;
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
