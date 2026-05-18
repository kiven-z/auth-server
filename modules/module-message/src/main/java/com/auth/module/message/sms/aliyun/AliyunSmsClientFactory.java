package com.auth.module.message.sms.aliyun;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.auth.module.message.sms.aliyun.AliyunSmsProperties;
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
	 * @param aliyunSmsProperties 阿里云短信配置
	 * @return 阿里云异步客户端
	 */
	public static AsyncClient create(AliyunSmsProperties aliyunSmsProperties) {
		StaticCredentialProvider credentialProvider = StaticCredentialProvider.create(Credential.builder()
			.accessKeyId(aliyunSmsProperties.getAccessKeyId())
			.accessKeySecret(aliyunSmsProperties.getAccessKeySecret())
			.build());

		return AsyncClient.builder()
			.region(aliyunSmsProperties.getRegion())
			.credentialsProvider(credentialProvider)
			.overrideConfiguration(
					ClientOverrideConfiguration.create().setEndpointOverride(aliyunSmsProperties.getEndpoint()))
			.build();
	}

}
