package com.auth.service.system.message.channel.sms.dypns.transport;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.message.exception.MessageResultCode.SMS_DYPNS_SEND_FAILED;
import static com.auth.service.system.message.exception.MessageResultCode.SMS_DYPNS_SEND_INTERRUPTED;

/**
 * 阿里云 Dypns 验证码短信发送
 *
 * @author Bunny
 */
@Slf4j
@Component
public class AliyunDypnsTransport implements AutoCloseable {

	private final AsyncClient asyncClient;

	private final SmsProperties smsProperties;

	public AliyunDypnsTransport(SmsProperties smsProperties) {
		this.smsProperties = smsProperties;
		this.asyncClient = createAsyncClient(smsProperties);
	}

	/**
	 * 创建异步客户端
	 * @param smsProperties 短信配置
	 * @return 异步客户端
	 */
	private static AsyncClient createAsyncClient(SmsProperties smsProperties) {
		// 创建凭证提供者
		StaticCredentialProvider credentialProvider = StaticCredentialProvider.create(Credential.builder()
			.accessKeyId(smsProperties.getAccessKeyId())
			.accessKeySecret(smsProperties.getAccessKeySecret())
			.build());

		// 创建异步客户端
		return AsyncClient.builder()
			.region(smsProperties.getRegion())
			.credentialsProvider(credentialProvider)
			.overrideConfiguration(
					ClientOverrideConfiguration.create().setEndpointOverride(smsProperties.getEndpoint()))
			.build();
	}

	/**
	 * 解析失败详情
	 * @param response 响应
	 * @return 失败详情
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
	 * 向指定手机号发送验证码短信
	 * @param phone 手机号
	 * @param providerTemplateCode 厂商侧模板编码
	 * @param templateParamJson 模板变量 JSON
	 */
	public void sendVerifyCode(String phone, String providerTemplateCode, String templateParamJson) {
		// 验证手机号
		if (CharSequenceUtil.isBlank(phone)) {
			throw new SystemBusinessException(PARAM_REQUIRED, "手机号");
		}
		// 验证厂商模板编码
		if (CharSequenceUtil.isBlank(providerTemplateCode)) {
			throw new SystemBusinessException(PARAM_REQUIRED, "厂商模板编码");
		}

		// 构建请求
		SendSmsVerifyCodeRequest request = SendSmsVerifyCodeRequest.builder()
			.phoneNumber(phone)
			.signName(smsProperties.getSignName())
			.templateCode(providerTemplateCode)
			.templateParam(CharSequenceUtil.blankToDefault(templateParamJson, "{}"))
			.build();

		try {
			// 发送请求
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

	/**
	 * 发送请求
	 * @param request 请求
	 * @return 响应
	 * @throws ExecutionException 执行异常
	 * @throws InterruptedException 中断异常
	 */
	private SendSmsVerifyCodeResponse send(SendSmsVerifyCodeRequest request)
			throws ExecutionException, InterruptedException {
		CompletableFuture<SendSmsVerifyCodeResponse> future = asyncClient.sendSmsVerifyCode(request);
		return future.get();
	}

	/**
	 * 断言发送成功
	 * @param response 响应
	 * @param phone 手机号
	 * @param providerTemplateCode 厂商模板编码
	 */
	private void assertSendSuccess(SendSmsVerifyCodeResponse response, String phone, String providerTemplateCode) {
		// 判断响应是否成功
		if (response != null && response.getBody() != null
				&& "OK".equalsIgnoreCase(String.valueOf(response.getBody().getCode()))) {
			log.debug("Aliyun Dypns send ok, phone={}, templateCode={}", phone, providerTemplateCode);
			return;
		}

		// 解析失败详情
		String detail = resolveFailureDetail(response);
		log.error("Aliyun Dypns send failed, phone={}, templateCode={}, detail={}", phone, providerTemplateCode,
				detail);

		throw new SystemBusinessException(SMS_DYPNS_SEND_FAILED, detail);
	}

}
