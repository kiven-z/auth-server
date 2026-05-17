package com.auth.module.message.sms.aliyun;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.auth.module.message.config.properties.SmsProperties;
import darabonba.core.client.ClientOverrideConfiguration;
import lombok.experimental.UtilityClass;

/**
 * 阿里云短信客户端工厂（仅包含阿里云 SDK 依赖代码）
 *
 * @author Bunny
 */
@UtilityClass
public class AliyunSmsClientFactory {

	/**
	 * 创建可关闭的异步客户端（由 Spring 容器负责 {@link AsyncClient#close()})
	 * @param smsProperties 短信配置
	 * @return 阿里云异步客户端
	 */
	public static AsyncClient create(SmsProperties smsProperties) {
		SmsProperties.Aliyun aliyun = smsProperties.getAliyun();
		StaticCredentialProvider credentialProvider = StaticCredentialProvider.create(Credential.builder()
			.accessKeyId(aliyun.getAccessKeyId())
			.accessKeySecret(aliyun.getAccessKeySecret())
			.build());

		return AsyncClient.builder()
			.region(aliyun.getRegion())
			.credentialsProvider(credentialProvider)
			.overrideConfiguration(ClientOverrideConfiguration.create().setEndpointOverride(aliyun.getEndpoint()))
			.build();
	}

}
