package com.tgy.rtls.web.controller.tplink;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

public class TpLinkApiClient {

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final String AK = "YzIwNTM0ODU0ODQwMWQ0"; // 此处填写您的ak
    private static final String SK = "48cc6946033f4c6fbfe55659988f482f"; // 此处填写您的sk
    private static final String TERMINAL_ID = UUID.randomUUID().toString().replaceAll("-", "");
    private static final String CT_JSON = "application/json; charset=utf-8";
    private static final String DEFAULT_EMPTY_PAYLOAD = "{}";
    private static final String ALGORITHM = "HmacSHA256";
    private static final String SERVICE_NAME = "tp-link";
    private static final String REQUEST_TYPE = "tp-link_request";
    private static final String HOST = "api-smbcloud.tp-link.com.cn";
    //    private static final String PATH = "/vms/open/vehicleManager/v1/deleteExportRecord"; // 删除导出记录
//    private static final String PATH = "/vms/open/vehicleManager/v1/getExportRecords"; //获取导出记录
//    private static final String PATH = "/vms/open/vehicleManager/v1/exportAllVehicleDetectionRecords"; //导出记录
    private static final String PATH = "/vms/open/vehicleManager/v1/downloadExportRecord"; //删除记录
    private static final String METHOD = "POST"; // 请求方法

    public static void main(String[] args) {
        try {
            // 构建请求体内容
            int start = 0;  // 替换为实际值
            int limit = 10; // 替换为实际值，范围是[1, 100]
            String type = "detection"; // 可以是 "detection" 或 "entryAndLeave"
            String fileId = "HJ/49cn7/k7N9tr2VXiQ6eGxZqPRK911POKpWWQPiJts7Kf+d9KMGmBhrJpGiRRAjSeb8Qo8WcTGwG83iPtoa5x7/kqNcwUMWkn76K3zarwiJPoez/SocDY9TIXzj5Ng";
            Integer listType = 0;

            String payload = String.format("{\"fileId\": \"%s\"}", fileId);
//            String payload = String.format("{\"start\": %d, \"limit\": %d, \"type\": \"%s\"}", start, limit, type);
//            String payload = String.format("{\"filterAnd\": {\"listType\": %d}, \"exportWithFlag\": true}", listType);

            // 步骤 1: 准备签名计算相关参数
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String nonce = UUID.randomUUID().toString().replaceAll("-", "");

            // 步骤 2: 拼接待签名字符串
            String hashedCanonicalRequest = sha256Hex(payload);
            String credentialScope = METHOD + " " + PATH + " " + REQUEST_TYPE;
            String stringToSign = ALGORITHM + "\n" + timestamp + "\n" + credentialScope + "\n" + hashedCanonicalRequest;

            // 步骤 3: 计算签名
            String signature = calculateSignature(SK, timestamp, PATH, stringToSign);

            // 步骤 4: 拼接 Authorization
            String authorization = buildAuthorizationHeader(timestamp, nonce, signature);

            // 输出 curl 命令
            String curlCommand = buildCurlCommand(authorization, payload);
            System.out.println(curlCommand);
        } catch (Exception e) {
            e.printStackTrace(); // 捕获并输出异常
        }
    }

    private static String calculateSignature(String secretKey, String timestamp, String path, String stringToSign) throws Exception {
        byte[] secretDate = hmac256(secretKey.getBytes(UTF8), timestamp);
        byte[] secretService = hmac256(secretDate, path);
        byte[] secretSigning = hmac256(secretService, SERVICE_NAME);
        return DatatypeConverter.printHexBinary(hmac256(secretSigning, stringToSign)).toLowerCase();
    }

    private static String buildAuthorizationHeader(String timestamp, String nonce, String signature) {
        return "Timestamp=" + timestamp + "," +
                "Nonce=" + nonce + "," +
                "AccessKey=" + AK + "," +
                "Signature=" + signature + "," +
                "TerminalId=" + TERMINAL_ID;
    }

    private static String buildCurlCommand(String authorization, String payload) {
        return new StringBuilder()
                .append("curl -X ").append(METHOD).append(" https://").append(HOST).append(PATH)
                .append(" -H \"X-Authorization: ").append(authorization).append("\"")
                .append(" -H \"Content-Type: ").append(CT_JSON).append("\"")
                .append(" -H \"Host: ").append(HOST).append("\"")
                .append(" -d '").append(payload).append("'")
                .toString();
    }

    private static byte[] hmac256(byte[] key, String msg) throws Exception {
        Mac mac = Mac.getInstance(ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, mac.getAlgorithm());
        mac.init(secretKeySpec);
        return mac.doFinal(msg.getBytes(UTF8));
    }

    private static String sha256Hex(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] d = md.digest(s.getBytes(UTF8));
        return DatatypeConverter.printHexBinary(d).toLowerCase();
    }
}