package com.tgy.rtls.data.entity.pay;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:alipay.properties")
@ConditionalOnProperty(name = "module.web.enabled", havingValue = "true", matchIfMissing = true)
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
}
