package com.auth.service.system.message.channel.sms.dypns;

import cn.hutool.core.text.CharSequenceUtil;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.auth.service.system.common.exception.SystemMessageException;
import com.auth.service.system.message.config.SmsProperties;
import darabonba.core.client.ClientOverrideConfiguration;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.auth.service.system.common.exception.code.SystemMessageResultCode.*;

/**
 * 阿里云 Dypns 验证码短信发送实现（SendSmsVerifyCode）
 *
 * @author Bunny
 */
@Slf4j
@Component
public class AliyunDypnsVerifyCodeSender implements AutoCloseable {

	private final AsyncClient asyncClient;

	private final SmsProperties smsProperties;

	/**
	 * 根据配置创建 Dypns 客户端并发送
	 * @param smsProperties 短信配置
	 */
	public AliyunDypnsVerifyCodeSender(SmsProperties smsProperties) {
		this.smsProperties = smsProperties;
		this.asyncClient = createAsyncClient(smsProperties);
	}

	/**
	 * 创建 Dypns AsyncClient
	 * @param smsProperties 短信配置
	 * @return 客户端
	 */
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

	/**
	 * 解析失败回执说明（用于日志与异常消息）
	 * @param response 厂商响应
	 * @return 可读说明
	 */
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
	 * 发送验证码短信（阿里云 Dypns SendSmsVerifyCode，签名见 auth.message.sms.sign-name）
	 * @param phone 手机号
	 * @param providerTemplateCode 厂商已审模板编码
	 * @param templateParamJson 模板变量 JSON 对象字符串
	 */
	public void sendVerifyCode(String phone, String providerTemplateCode, String templateParamJson) {
		if (CharSequenceUtil.isBlank(phone)) {
			throw new SystemMessageException(SMS_DYPNS_PHONE_BLANK);
		}
		if (CharSequenceUtil.isBlank(providerTemplateCode)) {
			throw new SystemMessageException(SMS_DYPNS_TEMPLATE_CODE_BLANK);
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
			throw new SystemMessageException(SMS_DYPNS_SEND_INTERRUPTED);
		}
		catch (ExecutionException e) {
			throw new SystemMessageException(SMS_DYPNS_SEND_FAILED, e.getMessage());
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

	/**
	 * 调用 SendSmsVerifyCode
	 * @param request 请求参数
	 * @return 响应
	 */
	private SendSmsVerifyCodeResponse send(SendSmsVerifyCodeRequest request)
			throws ExecutionException, InterruptedException {
		CompletableFuture<SendSmsVerifyCodeResponse> future = asyncClient.sendSmsVerifyCode(request);
		return future.get();
	}

	/**
	 * 校验厂商回执
	 * @param response 响应
	 * @param phone 手机号
	 * @param providerTemplateCode 模板编码
	 */
	private void assertSendSuccess(SendSmsVerifyCodeResponse response, String phone, String providerTemplateCode) {
		if (response != null && response.getBody() != null
				&& "OK".equalsIgnoreCase(String.valueOf(response.getBody().getCode()))) {
			log.debug("Aliyun Dypns send ok, phone={}, templateCode={}", phone, providerTemplateCode);
			return;
		}

		String detail = resolveFailureDetail(response);
		log.error("Aliyun Dypns send failed, phone={}, templateCode={}, detail={}", phone, providerTemplateCode,
				detail);

		throw new SystemMessageException(SMS_DYPNS_SEND_FAILED, detail);
	}

}
