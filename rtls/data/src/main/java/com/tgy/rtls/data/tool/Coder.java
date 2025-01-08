package com.tgy.rtls.data.tool;

import java.security.MessageDigest;

/**
 * 基础加密组件
 *
 * @author 梁栋
 * @version 1.0
 * @since 1.0
 */
public abstract class Coder {
    public static final String KEY_SHA_RTLS = "04b65aaf88ac47099543f22d52e301d2";
    public static final String KEY_SHA_SAFEBELT = "6ef32c7e63ee4bceba4e76ab0c03796e";
    public static final String KEY_SHA_GUANLIXITONG = "9c7e6cf9a1f64fe6b16146c954f27d96";
    public static final String KEY_SHA_ANQUANYAN = "61c4bcc97bbb44c980b3442306535511";

   /* public static final String KEY_MD5 = "MD5";

    *//**
     * MAC算法可选以下多种算法
     *
     * <pre>
     * HmacMD5
     * HmacSHA1
     * HmacSHA256
     * HmacSHA384
     * HmacSHA512
     * </pre>
     *//*
    public static final String KEY_MAC = "HmacMD5";

    *//**
     * BASE64解密
     *
     * @param key
     * @return
     * @throws Exception
     *//*
    public static byte[] decryptBASE64(String key) throws Exception {
        return (new BASE64Decoder()).decodeBuffer(key);
    }

    *//**
     * BASE64加密
     *
     * @param key
     * @return
     * @throws Exception
     *//*
    public static String encryptBASE64(byte[] key) throws Exception {
        return (new BASE64Encoder()).encodeBuffer(key);
    }

    *//**
     * MD5加密
     *
     * @param data
     * @return
     * @throws Exception
     *//*
    public static byte[] encryptMD5(byte[] data) throws Exception {

        MessageDigest md5 = MessageDigest.getInstance(KEY_MD5);
        md5.update(data);

        return md5.digest();

    }

    *//**
     * SHA加密
     *
     * @param data
     * @return
     * @throws Exception
     *//*
    public static byte[] encryptSHA(byte[] data) throws Exception {

        MessageDigest sha = MessageDigest.getInstance("SHA");
        sha.update(data);

        return sha.digest();

    }

    *//**
     * 初始化HMAC密钥
     *
     * @return
     * @throws Exception
     *//*
    public static String initMacKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_MAC);

        SecretKey secretKey = keyGenerator.generateKey();
        return encryptBASE64(secretKey.getEncoded());
    }

    *//**
     * HMAC加密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     *//*
    public static byte[] encryptHMAC(byte[] data, String key) throws Exception {

        SecretKey secretKey = new SecretKeySpec(decryptBASE64(key), KEY_MAC);
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        mac.init(secretKey);

        return mac.doFinal(data);

    }*/
    //Java代码实现SHA-256消息加密
    public static String SHA256(String data) throws Exception {
        //返回实现指定摘要算法的MessageDigest对象。
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        //使用指定的字节更新摘要
        md.update(data.getBytes("UTF-8"));
        //通过执行最后的操作（如填充）来完成哈希计算
        byte[] array = md.digest();
        StringBuilder sb = new StringBuilder();
        //对哈希数进行密码散列计算
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF)|0x100).substring(1, 3));
        }
        return sb.toString().toUpperCase();
    }

   /* public static void main(String[] args) throws Exception {

        byte[]  inputData=  encryptSHA("16296904749c7e6cf9a1f64fe6b16146c954f27d96".getBytes());
       String res= SHA256("16296904749c7e6cf9a1f64fe6b16146c954f27d96");
        System.out.println(res);
        BigInteger sha = new BigInteger(inputData);
        System.err.println("SHA:\n" + sha.toString(16));
    }*/
}

