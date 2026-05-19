package com.auth.module.message.sender.modules.sms.aliyun;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.auth.module.message.sender.config.properties.SmsProperties;
import darabonba.core.client.ClientOverrideConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 基于阿里云 Dypns AsyncClient 的验证码发送客户端
 *
 * @author Bunny
 */
@Slf4j
public class AliyunDypnsVerifyCodeClient implements DypnsVerifyCodeClient {

	private final AsyncClient asyncClient;

	/**
	 * 根据配置创建 Dypns 客户端
	 * @param smsProperties 短信配置
	 */
	public AliyunDypnsVerifyCodeClient(SmsProperties smsProperties) {
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
	 * {@inheritDoc}
	 */
	@Override
	public SendSmsVerifyCodeResponse send(SendSmsVerifyCodeRequest request)
			throws ExecutionException, InterruptedException {
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
