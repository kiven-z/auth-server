package com.auth.module.message.sender.modules.sms.aliyun;

import cn.hutool.core.text.CharSequenceUtil;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.auth.module.message.sender.config.properties.SmsProperties;
import com.auth.module.message.sender.modules.sms.DypnsVerifyCodeSender;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * 阿里云 Dypns 验证码短信发送实现
 *
 * @author Bunny
 */
@Slf4j
public class AliyunDypnsSmsSender implements DypnsVerifyCodeSender {

	private final DypnsVerifyCodeClient dypnsClient;

	private final SmsProperties smsProperties;

	/**
	 * @param dypnsClient 可注入的 Dypns 客户端（生产环境由自动配置创建）
	 * @param smsProperties 短信配置（含 signName）
	 */
	public AliyunDypnsSmsSender(DypnsVerifyCodeClient dypnsClient, SmsProperties smsProperties) {
		this.dypnsClient = dypnsClient;
		this.smsProperties = smsProperties;
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
	 * {@inheritDoc}
	 */
	@Override
	public void sendVerifyCode(String phone, String providerTemplateCode, String templateParamJson) {
		if (CharSequenceUtil.isBlank(phone)) {
			throw new IllegalArgumentException("phone must not be blank");
		}
		if (CharSequenceUtil.isBlank(providerTemplateCode)) {
			throw new IllegalArgumentException("providerTemplateCode must not be blank");
		}

		SendSmsVerifyCodeRequest request = SendSmsVerifyCodeRequest.builder()
			.phoneNumber(phone)
			.signName(smsProperties.getSignName())
			.templateCode(providerTemplateCode)
			.templateParam(CharSequenceUtil.blankToDefault(templateParamJson, "{}"))
			.build();

		try {
			SendSmsVerifyCodeResponse response = dypnsClient.send(request);
			assertSendSuccess(response, phone, providerTemplateCode);
		}
		catch (IllegalStateException | IllegalArgumentException e) {
			throw e;
		}
		catch (InterruptedException | ExecutionException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Aliyun Dypns send interrupted", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		try {
			dypnsClient.close();
		}
		catch (Exception e) {
			log.warn("Failed to close Dypns client", e);
		}
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

		throw new IllegalStateException("Aliyun Dypns send failed: " + detail);
	}

}
