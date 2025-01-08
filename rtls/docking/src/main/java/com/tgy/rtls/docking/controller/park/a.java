package com.tgy.rtls.docking.controller.park;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.docking.controller.park
 * @Author: wuwei
 * @CreateTime: 2023-08-24 18:44
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
public class a {
    public static void main(String[] args){
        String json = "{\"Describe\":\"[{LC:西A区,FL:负一层,ParkingNo:西A-024,CarPlateNo:湘C75U02,DateTime:2023/8/24 10:46:15}]\"}";
        JSONObject jsonObject = JSONObject.parseObject(json);
        String describeString = jsonObject.getString("Describe");

        int startIndex = describeString.indexOf("[{") + 1;
        int endIndex = describeString.lastIndexOf("}]");

        String  a= describeString.substring(startIndex, endIndex);
        String lc = getValue(a, "LC");
        String fl = getValue(a, "FL");
        String parkingNo = getValue(a, "ParkingNo");
        String carPlateNo = getValue(a, "CarPlateNo");
        String dateTime = getValue(a, "DateTime");

        System.out.println("LC: " + lc);
        System.out.println("FL: " + fl);
        System.out.println("Parking No: " + parkingNo);
        System.out.println("Car Plate No: " + carPlateNo);
        System.out.println("DateTime: " + dateTime);


        JSONObject jsonObject1 = JSONObject.parseObject(json);
        String describe = jsonObject1.getString("Describe");
        String describes = describe.replaceAll("(\\w+):([^,}]+)", "\"$1\":\"$2\"");
        log.error(describes);
    }
    private static String getValue(String input, String key) {
        int startIndex = input.indexOf(key + ":") + key.length() + 1;
        int endIndex = input.indexOf(",", startIndex);
        if(!"DateTime".equals(key)){
            if (endIndex == -1) {
                endIndex = input.indexOf("}", startIndex);
            }
        return input.substring(startIndex, endIndex).replaceAll("\"", "");
        }
        else {
            return input.substring(60, 78).replaceAll("\"", "");
        }
        // -------------------------------------------------------
        //String  pattern= key + ":(.*?)(:|})";
        //Pattern regex = Pattern.compile(pattern);
        //    Matcher matcher = regex.matcher(input);
        //    if (matcher.find()) {
        //        return matcher.group(1).replaceAll("\"", "");
        //    }
    }

    public String stringToAssembleJson (String json) {
        JSONObject jsonObject1 = JSONObject.parseObject(json);
        String describe = jsonObject1.getString("Describe");
        String describes = describe.replaceAll("(\\w+):([^,}]+)", "\"$1\":\"$2\"");
        return describes;
    }
}
