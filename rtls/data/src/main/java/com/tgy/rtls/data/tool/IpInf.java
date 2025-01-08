package com.tgy.rtls.data.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public class IpInf {
    /**
     * 获取用户ip地址
     * @return
     */
    public static String getIp(HttpServletRequest request){
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    public static String sendGet(String urlParam) throws HttpException, IOException {
        // 创建httpClient实例对象
        HttpClient httpClient = new HttpClient();
        // 设置httpClient连接主机服务器超时时间：15000毫秒
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(15000);
        // 创建GET请求方法实例对象
        GetMethod getMethod = new GetMethod(urlParam);
        // 设置post请求超时时间
        getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 60000);
        getMethod.addRequestHeader("Content-Type", "application/json");

        httpClient.executeMethod(getMethod);

        String result = getMethod.getResponseBodyAsString();
        getMethod.releaseConnection();
        return result;
    }

//    public static void main(String[] args) {
//        String url ="https://restapi.amap.com/v3/ip?ip=114.247.50.2&output=json&key=7e9b7d5ac0e855c017d3c6b5a1f0eb91";
//        try {
//            String res = sendGet(url);
//            JSONObject json =  JSONObject.fromObject(res);
//            IpLocation ipLocation=(IpLocation)JSONObject.toBean(json, IpLocation.class);
//            System.out.println();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

  public static   String getIpLocationInf(String ip){
        String gaoDe="https://restapi.amap.com/v3/ip?";
        String ipaddr="ip="+ip+"&output=json&key=";
        String key="7e9b7d5ac0e855c017d3c6b5a1f0eb91";
        return gaoDe+ipaddr+key;
    }

    public static void main(String[] args) {
        OkHttpClient httpClient = new OkHttpClient();
        String ip = "112.94.22.123";
        String url = "http://whois.pconline.com.cn/ipJson.jsp?ip=" + ip +"&json=true";
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = httpClient.newCall(request).execute();
            String result = response.body().string();
            ObjectMapper objectMapper = new ObjectMapper();
            Map resultMap = objectMapper.readValue(result,Map.class);
            System.out.println("ip信息：" + resultMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
