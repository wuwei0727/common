// package com.tgy.rtls.data.config;
//
// import io.xjar.XConstants;
// import io.xjar.XEntryFilter;
// import io.xjar.XKit;
// import io.xjar.boot.XBoot;
// import io.xjar.jar.XJarAntEntryFilter;
// import io.xjar.key.XKey;
//
// import javax.crypto.KeyGenerator;
// import javax.crypto.SecretKey;
// import java.security.NoSuchAlgorithmException;
// import java.security.SecureRandom;
//
// public class Encry {
//     /**
//      * 加密码
//      */
//     private static String password = "io.xjar";
//
//     /**
//      * 加密前路径
//      */
//     private static String plaintext = "F:/IdeaWorkSpace/erferencvc/rtls/web/target/web-0.0.1-SNAPSHOT.jar";
//     /**
//      * 加密后路径
//      */
//     private static String encrypted = "F:/IdeaWorkSpace/erferencvc/rtls/web/target/web.jar";
//
//     public static void main(String[] args) throws Exception {
//         // Spring-Boot Jar包加密
// /*        XKey xKey = XKit.key(password);
//         XBoot.encrypt(plaintext, encrypted, xKey);*/
//         String password = "admin";
//         XKey xKey = XKit.key("DES",56,56,password);
//         xKey.getDecryptKey();
//
//         System.out.println( xKey.getEncryptKey());
//
//
//         XEntryFilter andlocation = XKit.and()
//                 .mix(new XJarAntEntryFilter("com/tgy/rtls/location/**"))
//                 .mix(new XJarAntEntryFilter("*/**.class"));
//         XBoot.encrypt("F:/IdeaWorkSpace/erferencvc/rtls/location/target/location-0.0.1-SNAPSHOT.jar", "F:/IdeaWorkSpace/erferencvc/rtls/location/target/location.jar", xKey, XConstants.MODE_DANGER,andlocation);
//
//
//         XEntryFilter andweb = XKit.and()
//                 .mix(new XJarAntEntryFilter("com/tgy/rtls/web/**"))
//                 .mix(new XJarAntEntryFilter("*/**.class"));
//
//          XBoot.encrypt("F:/IdeaWorkSpace/erferencvc/rtls/web/target/web-0.0.1-SNAPSHOT.jar", "F:/IdeaWorkSpace/erferencvc/rtls/web/target/web.jar", xKey, XConstants.MODE_DANGER,andweb);
//         System.out.println("Successfully generated encrypted jar");
//
//         decryptJar("F:/IdeaWorkSpace/erferencvc/rtls/web/target/web.jar", "F:/IdeaWorkSpace/erferencvc/rtls/web/target/webde.jar","admin");
//
//     }
//
//     public static void main1(String[] args) {
//         getKeyByPass();
//     }
//     public static void getKeyByPass() {
//         //生成秘钥
//         String password="tgyxxkj";
//         try {
//             KeyGenerator kg = KeyGenerator.getInstance("AES");
//             // kg.init(128);//要生成多少位，只需要修改这里即可128, 192或256
//             //SecureRandom是生成安全随机数序列，password.getBytes()是种子，只要种子相同，序列就一样，所以生成的秘钥就一样。
//             kg.init(128, new SecureRandom(password.getBytes()));
//             SecretKey sk = kg.generateKey();
//             byte[] b = sk.getEncoded();
//             String s = byteToHexString(b);
//             System.out.println(s);
//             System.out.println("十六进制密钥长度为"+s.length());
//             System.out.println("二进制密钥的长度为"+s.length()*4);
//         }
//         catch (NoSuchAlgorithmException e) {
//             e.printStackTrace();
//             System.out.println("没有此算法。");
//         }
//     }
//     /**
//      * byte数组转化为16进制字符串
//      * @param bytes
//      * @return
//      */
//     public static String byteToHexString(byte[] bytes) {
//         StringBuffer sb = new StringBuffer();
//         for (int i = 0; i < bytes.length; i++) {
//             String strHex=Integer.toHexString(bytes[i]);
//             if(strHex.length() > 3) {
//                 sb.append(strHex.substring(6));
//             } else {
//                 if(strHex.length() < 2) {
//                     sb.append("0" + strHex);
//                 } else {
//                     sb.append(strHex);
//                 }
//             }
//         }
//         return sb.toString();
//     }
//
//     /**
//      * jar包危险加密模式
//      * 即不需要输入密码即可启动的加密方式，这种方式META-INF/MANIFEST.MF中会保留密钥，请谨慎使用！
//      * @param fromJarPath 需要加密的jar
//      * @param toJarPath 加密后的jar
//      * @param password 加密密码
//      */
//     public static void encryptJarDangerMode(String fromJarPath, String toJarPath, String password) {
//         try {
//             XKey xKey = XKit.key(password);
//             XBoot.encrypt(fromJarPath, toJarPath, xKey, XConstants.MODE_DANGER);
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }
//
//     /**
//      * jar包解密
//      * @param fromJarPath 已通过Xjar加密的jar文件路径
//      * @param toJarPath 解密后的jar文件
//      * @param password 密码
//      */
//     public static void decryptJar(String fromJarPath, String toJarPath, String password) {
//         try {
//             XKey xKey = XKit.key(password);
//             XBoot.decrypt(fromJarPath, toJarPath, xKey);
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }
//
// }
