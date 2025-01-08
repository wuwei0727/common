package com.tgy.rtls.data.entity.pay;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
*@Author: wuwei
*@CreateTime: 2023/11/21 18:11
*/
@Component
@PropertySource("classpath:wxpay.properties")
@ConditionalOnProperty(name = "module.web.enabled", havingValue = "true", matchIfMissing = true)
@ConfigurationProperties(prefix = "wxpay")
@Data
public class WxPayBean {
	private String appId;
	private String privateKeyPath;
	private String privateCertPath;
	private String certP12Path;
	private String platformCertPath;
	private String mchId;
	private String apiKey;
	private String apiKey3;
	private String domain;
	private String notifyDomain;
	private String secret;

	@Override
	public String toString() {
		return "WxPayBean{" +
			"keyPath='" + privateKeyPath + '\'' +
			", certPath='" + privateCertPath + '\'' +
			", certP12Path='" + certP12Path + '\'' +
			", platformCertPath='" + platformCertPath + '\'' +
			", mchId='" + mchId + '\'' +
			", apiKey='" + apiKey + '\'' +
			", apiKey3='" + apiKey3 + '\'' +
			", domain='" + domain + '\'' +
			'}';
	}


}
