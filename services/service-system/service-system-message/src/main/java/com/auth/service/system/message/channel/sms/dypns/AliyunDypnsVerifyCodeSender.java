package com.auth.service.system.message.channel.sms.dypns;

import cn.hutool.core.text.CharSequenceUtil;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.config.SmsProperties;
import darabonba.core.client.ClientOverrideConfiguration;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.auth.service.system.message.exception.MessageResultCode.*;

/**
 * 阿里云 Dypns 验证码短信发送
 *
 * @author Bunny
 */
@Slf4j
@Component
public class AliyunDypnsVerifyCodeSender implements AutoCloseable {

	private final AsyncClient asyncClient;

	private final SmsProperties smsProperties;

	public AliyunDypnsVerifyCodeSender(SmsProperties smsProperties) {
		this.smsProperties = smsProperties;
		this.asyncClient = createAsyncClient(smsProperties);
	}

	private static AsyncClient createAsyncClient(SmsProperties smsProperties) {
		StaticCredentialProvider credentialProvider = StaticCredentialProvider.create(Credential.builder()
			.accessKeyId(smsProperties.getAccessKeyId())
			.accessKeySecret(smsProperties.getAccessKeySecret())
			.build());

		return AsyncClient.builder()
			.region(smsProperties.getRegion())
			.credentialsProvider(credentialProvider)
			.overrideConfiguration(
					ClientOverrideConfiguration.create().setEndpointOverride(smsProperties.getEndpoint()))
			.build();
	}

	private static String resolveFailureDetail(SendSmsVerifyCodeResponse response) {
		if (response == null) {
			return "null response";
		}
		if (response.getBody() == null) {
			return "null body";
		}
		return String.valueOf(response.getBody());
	}

	/**
	 * 向指定手机号发送验证码短信
	 * @param phone 手机号
	 * @param providerTemplateCode 厂商侧模板编码
	 * @param templateParamJson 模板变量 JSON
	 */
	public void sendVerifyCode(String phone, String providerTemplateCode, String templateParamJson) {
		if (CharSequenceUtil.isBlank(phone)) {
			throw new SystemBusinessException(SMS_DYPNS_PHONE_BLANK);
		}
		if (CharSequenceUtil.isBlank(providerTemplateCode)) {
			throw new SystemBusinessException(SMS_DYPNS_TEMPLATE_CODE_BLANK);
		}

		SendSmsVerifyCodeRequest request = SendSmsVerifyCodeRequest.builder()
			.phoneNumber(phone)
			.signName(smsProperties.getSignName())
			.templateCode(providerTemplateCode)
			.templateParam(CharSequenceUtil.blankToDefault(templateParamJson, "{}"))
			.build();

		try {
			SendSmsVerifyCodeResponse response = send(request);
			assertSendSuccess(response, phone, providerTemplateCode);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new SystemBusinessException(SMS_DYPNS_SEND_INTERRUPTED);
		}
		catch (ExecutionException e) {
			throw new SystemBusinessException(SMS_DYPNS_SEND_FAILED, e.getMessage());
		}
	}

	@PreDestroy
	@Override
	public void close() {
		if (asyncClient != null) {
			try {
				asyncClient.close();
			}
			catch (Exception e) {
				log.warn("Failed to close Dypns client", e);
			}
		}
	}

	private SendSmsVerifyCodeResponse send(SendSmsVerifyCodeRequest request)
			throws ExecutionException, InterruptedException {
		CompletableFuture<SendSmsVerifyCodeResponse> future = asyncClient.sendSmsVerifyCode(request);
		return future.get();
	}

	private void assertSendSuccess(SendSmsVerifyCodeResponse response, String phone, String providerTemplateCode) {
		if (response != null && response.getBody() != null
				&& "OK".equalsIgnoreCase(String.valueOf(response.getBody().getCode()))) {
			log.debug("Aliyun Dypns send ok, phone={}, templateCode={}", phone, providerTemplateCode);
			return;
		}

		String detail = resolveFailureDetail(response);
		log.error("Aliyun Dypns send failed, phone={}, templateCode={}, detail={}", phone, providerTemplateCode,
				detail);

		throw new SystemBusinessException(SMS_DYPNS_SEND_FAILED, detail);
	}

}
