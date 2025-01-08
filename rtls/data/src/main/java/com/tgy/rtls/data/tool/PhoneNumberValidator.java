package com.tgy.rtls.data.tool;

import cn.hutool.core.lang.Validator;
import org.springframework.stereotype.Component;

@Component
public class PhoneNumberValidator {

    // 中国移动：134（不含1349）、135、136、137、138、139、147（数据卡）、148、150、151、152、157（TD）、158、159、165、172、178、182、183、184、187、188、195、198
    private static final String CMCC_PATTERN = "^(?:13[5-9]|14[7-8]|15[0-2,7-9]|165|17[2,8]|18[2-4,7-8]|19[5,8])\\d{8}$";
    
    // 中国联通：130、131、132、145（数据卡）、146、155、156、166、171、175、176、185、186、196
    private static final String CUCC_PATTERN = "^(?:13[0-2]|14[5-6]|15[5-6]|16[6]|17[1,5-6]|18[5-6]|19[6])\\d{8}$";
    
    // 中国电信：133、1349（卫星通信）、149、153、162、170（1700、1701、1702号段为虚拟运营商号段）、173、174、177、180、181、189、190、191、193、199
    private static final String CTCC_PATTERN = "^(?:133|1349|14[9]|153|162|170|173|174|177|18[0-1,9]|19[0-1,3,9])\\d{8}$";

    public static void main(String[] args) {
        String phoneNumber = "13100138000"; // 示例手机号

        if (validatePhoneNumber(phoneNumber)) {
            System.out.println("手机号校验通过");
        } else {
            System.out.println("手机号校验不通过");
        }
    }

    public static boolean validatePhoneNumber(String phoneNumber) {
        return Validator.isMobile(phoneNumber);

//        // 使用正则表达式进行进一步校验
//        if (ReUtil.isMatch(CMCC_PATTERN, phoneNumber)) {
//            System.out.println("该手机号属于中国移动");
//            return true;
//        } else if (ReUtil.isMatch(CUCC_PATTERN, phoneNumber)) {
//            System.out.println("该手机号属于中国联通");
//            return true;
//        } else if (ReUtil.isMatch(CTCC_PATTERN, phoneNumber)) {
//            System.out.println("该手机号属于中国电信");
//            return true;
//        } else {
//            return false;
//        }
    }
}