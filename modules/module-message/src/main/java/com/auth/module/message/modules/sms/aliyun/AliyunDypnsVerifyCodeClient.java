package com.auth.module.message.modules.sms.aliyun;

import cn.hutool.core.text.CharSequenceUtil;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.auth.module.message.config.properties.SmsProperties;
import darabonba.core.client.ClientOverrideConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * 基于阿里云 Dypns AsyncClient 的验证码发送客户端
 *
 * @author Bunny
 */
@Slf4j
public class AliyunDypnsVerifyCodeClient implements DypnsVerifyCodeClient, AutoCloseable {

	private final AsyncClient asyncClient;

	/**
	 * 根据配置创建 Dypns 客户端
	 * @param smsProperties 短信配置
	 */
	public AliyunDypnsVerifyCodeClient(SmsProperties smsProperties) {
		validateCredentials(smsProperties);
		StaticCredentialProvider credentialProvider = StaticCredentialProvider.create(Credential.builder()
			.accessKeyId(smsProperties.getAccessKeyId())
			.accessKeySecret(smsProperties.getAccessKeySecret())
			.build());
		this.asyncClient = AsyncClient.builder()
			.region(smsProperties.getRegion())
			.credentialsProvider(credentialProvider)
			.overrideConfiguration(
					ClientOverrideConfiguration.create().setEndpointOverride(smsProperties.getEndpoint()))
			.build();
	}

	/**
	 * 校验 AK 配置
	 * @param smsProperties 短信配置
	 */
	private static void validateCredentials(SmsProperties smsProperties) {
		if (CharSequenceUtil.isBlank(smsProperties.getAccessKeyId())
				|| CharSequenceUtil.isBlank(smsProperties.getAccessKeySecret())) {
			throw new IllegalStateException("SMS access key is not configured (auth.message.sms.access-key-id/secret)");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SendSmsVerifyCodeResponse send(SendSmsVerifyCodeRequest request) throws Exception {
		CompletableFuture<SendSmsVerifyCodeResponse> future = asyncClient.sendSmsVerifyCode(request);
		return future.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		if (asyncClient != null) {
			asyncClient.close();
		}
	}

}
