package com.tgy.rtls.data.common;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.ijpay.core.kit.PayKit;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 订单号工具类
 *
 * @author wuwei
 * @since 1.0
 */
@Slf4j
public class OrderNoUtils {
    private static String serialNo;

    public static String getOrderNo() {
        return "ORDER_" + getNo();
    }

    public static String getRefundNo() {
        return "REFUND_" + getNo();
    }

    public static String getNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String newDate = sdf.format(new Date());
        StringBuilder result = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 3; i++) {
            result.append(random.nextInt(10));
        }
        return newDate + result;
    }

    public static class SerialNumberUtils {
    public static String getSerialNumber(String privateCertPath,String mchId) {
        if (StrUtil.isEmpty(serialNo)) {
            // 获取证书序列号
            X509Certificate certificate = PayKit.getCertificate(privateCertPath);
            if (null != certificate) {
                serialNo = certificate.getSerialNumber().toString(16).toUpperCase();
                // 提前两天检查证书是否有效
                boolean isValid = PayKit.checkCertificateIsValid(certificate, mchId, -2);
                log.info("证书是否可用 {} 证书有效期为 {}", isValid, DateUtil.format(certificate.getNotAfter(), DatePattern.NORM_DATETIME_PATTERN));
            }
        }
        System.out.println("serialNo:" + serialNo);
        return serialNo;
    }
}
}
