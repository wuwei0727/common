package com.ww.springbootalipay.dao;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:alipay.properties")
@ConfigurationProperties(prefix = "alipay")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AliPayBean {
	private String appId;
	private String privateKey;
	private String publicKey;
	private String appCertPath;
	private String aliPayCertPath;
	private String aliPayRootCertPath;
	private String serverUrl;
	private String domain;
	private String sellerId;
	@Override
	public String toString() {
		return "AliPayBean{" +
			"appId='" + appId + '\'' +
			", privateKey='" + privateKey + '\'' +
			", publicKey='" + publicKey + '\'' +
			", appCertPath='" + appCertPath + '\'' +
			", aliPayCertPath='" + aliPayCertPath + '\'' +
			", aliPayRootCertPath='" + aliPayRootCertPath + '\'' +
			", serverUrl='" + serverUrl + '\'' +
			", domain='" + domain + '\'' +
			'}';
	}

	//这是一个main方法，程序的入口
	public static void main(String[] args) throws AlipayApiException {
		String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCF0rbuJzFF8IT3gJtPu0S6TRDTmPD0uYOVxmmzRJeq/5KDcKQFL07YFnXwTm1+tLIBx9gmTYOYEd9LyHx91gGvUgdBu1nRrYa0aYPMUByQVX6cnhooONHJxZOQJKWOIePz1N4PKlRopBPlj3lGOYOv8mmZ7rSbWzcyEjfmThjpLO5wT5Akcw+VKDHT5q+69L3kItfzC9KtS88Egc9BWqrSVk3LT289LnZZx6C+KbwLwh82YJYjUl/a0CR25kcB+NWoskjELqwO5uHdpkNcFXJqhG8k6AAsiyeAJ/nKZYFfDH+AEMzU9k1vTR0TuCF0dPeFlZcvmxFYIghRhRX+YbWTAgMBAAECggEAdDWSc2cHa9O8s+0zcd6OhTXNKeLDQ9Ehilu9iFZFWtILGicj1/jH//ApRJ/mh8REXDV9520inFjQzs9wBBpmZifWc2dNTT7iHiMOt8rNsyz7AOhukIUe8JepV9/rAG0/tYshtoustx3cxVl9F9V0HMNmnqzim5JvcQTPFCi3iywRu9ICahR/3Xckpse34JAj85HjRT5YDk9vd7ypF1cAQVjMHsIw7nwUFpXoj1Trb/vLCt0ZDOH2AYiB08qIULRGP8MvbEF8k1X2RLTILifK+7nblvhZG69PJoSt86Swr5uJ3gD1tNS6SN5qf1HXbMyZG+3SYem8m+7fcsehH9u7wQKBgQD2c/RRYqEq0/MXo78jCTqtbZ8KvekrfZmvHNjy/HJmzxZfRWfTrLJUWC/L4YTjVTnD43AH3hRm31NqdetyMPECtPbUF6qywzVH2gID/H3EwewWYeNk6skIPBFSJ80x35pnoxOWdaT5qG95lNtFu1u8jxZ45O84XeYVRMvVABDRIQKBgQCLAdKtWu+vjK+REXhifGLvc5rurIjAx0aSisZOh31wQqub2OHv0EiW29t2SPpIaf5tPffbVs/sYWt3FP2seaLI1dkxX6G+dr59qvC6Ms2zpLa9cBxqVW+u9FOF5rtJlx1NtHJ4OSJzUN1PArWfBnbA/rJ6W8zSaDI+tjxEEWqMMwKBgQDCfIMDbrgM2jHuJ9SAS5o4nldx3KiTH40WOcejCUEmjKn1CQqEarZYBTfCxvddhzYMtgEE1GZ5QCfNzby94o1vO2vFt9+HLrFvQEVy6+A8U2SBzUHrs7RtgSmSEcLGZtSskG62v6QvOJKv+Z6IivA7/xcW1GNLsh4ODrpkHqcmwQKBgCB6x5Ar7E83bxpVW6yfZbqPZ4cXF0j8BvhcpqNYsy2U1mYu0fPn1d4x9uB3gsuCwKkjzpMYgLK5vJNxeyher3N/zCTmNHO9aRsR4veOwVOVTAGNfYY2+kaF3jIuTFqQjY9Hd2ux5ePN5Df+6RMEPqEvoeJHKt+K6xqOGBzLePrTAoGAHeCgrccKPui4Vx1GD2txP5da8exu7tVefzzjdIkfuXkGjzkUt9VX7dZn+XWjMS/4xU34JvhJTd4EhiDpAkd2XSGDcFsnIv8Mxy6hDd6s1Hau9xrImJ70MS20FLGEHT28sqTjaC/z0eaYz2eN1DLfCivs+SGJvarCdAljffPujwQ=";
		String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1c1GM8qU43YYIUszkhnFtaB0toLZGVmELVx26HYvGL7EaIWnQbryd0idKz9Q2f6cF+OTgyHwT0r8K9mNCmmU4+kwMDaevKKlJsMdFXQYVqQf3UmbBWxTb3X9zYcMIAwyrvNch+M8usxH4zMwFquJ97XzHrXloXIGQom7FB6RfvrBL4jozOgqZcvLgKXdy1hUyDUlx8LjgdoVyS/ZCrod28HrMifwURlWyCjwxHSQE2B/y8dTw+ga1EX9IIy/bxxiptFhb+rrP9lJAXxn0eh38x6Xz6Gp/i4oFLjGv6I3hGUVXP9/utIVFaiIJ/RCvRniNzUzGcRJdUN0j7w33BCaRwIDAQAB";
		AlipayConfig alipayConfig = new AlipayConfig();
		alipayConfig.setServerUrl("https://openapi.alipay.com/gateway.do");
		alipayConfig.setAppId("2021004122685881");
		alipayConfig.setPrivateKey(privateKey);
		alipayConfig.setFormat("json");
		alipayConfig.setAlipayPublicKey(alipayPublicKey);
		alipayConfig.setCharset("UTF-8");
		alipayConfig.setSignType("RSA2");
		AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);
		AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
		AlipayTradeCloseModel model = new AlipayTradeCloseModel();
		model.setTradeNo("111315455816998");
		request.setBizModel(model);
		AlipayTradeCloseResponse response = alipayClient.execute(request);
		System.out.println(response.getBody());
		if (response.isSuccess()) {
			System.out.println("调用成功");
		} else {
			System.out.println("调用失败");
			// sdk版本是"4.38.0.ALL"及以上,可以参考下面的示例获取诊断链接
			// String diagnosisUrl = DiagnosisUtils.getDiagnosisUrl(response);
			// System.out.println(diagnosisUrl);
		}
	}
}
