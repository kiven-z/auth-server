package com.auth.module.message.modules.sms.aliyun;

import cn.hutool.core.text.CharSequenceUtil;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.auth.module.message.config.properties.SmsProperties;
import com.auth.module.message.modules.sms.DypnsVerifyCodeSender;
import lombok.extern.slf4j.Slf4j;

/**
 * 阿里云 Dypns 验证码短信发送实现
 *
 * @author Bunny
 */
@Slf4j
public class AliyunDypnsSmsSender implements DypnsVerifyCodeSender {

	/**
	 * 未配置短信签名时的异常消息前缀（供上层映射业务码）
	 */
	public static final String SIGN_NAME_NOT_CONFIGURED_MESSAGE = "SMS sign name is not configured";

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

		String signName = resolveSignName();

		SendSmsVerifyCodeRequest request = SendSmsVerifyCodeRequest.builder()
			.phoneNumber(phone)
			.signName(signName)
			.templateCode(providerTemplateCode)
			.templateParam(CharSequenceUtil.blankToDefault(templateParamJson, "{}"))
			.build();

		try {
			SendSmsVerifyCodeResponse response = dypnsClient.send(request);
			assertSendSuccess(response, phone, providerTemplateCode);
		}
		catch (IllegalStateException e) {
			throw e;
		}
		catch (Exception e) {
			log.error("Aliyun Dypns send error, phone={}, templateCode={}", phone, providerTemplateCode, e);
			throw new IllegalStateException("Aliyun Dypns send error: " + e.getMessage(), e);
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

		String detail = response == null ? "null response"
				: (response.getBody() == null ? "null body" : String.valueOf(response.getBody()));
		log.error("Aliyun Dypns send failed, phone={}, templateCode={}, detail={}", phone, providerTemplateCode,
				detail);

		throw new IllegalStateException("Aliyun Dypns send failed: " + detail);
	}

	/**
	 * 解析短信签名
	 * @return 签名
	 */
	private String resolveSignName() {
		String signName = smsProperties.getSignName();
		if (CharSequenceUtil.isBlank(signName)) {
			throw new IllegalStateException(SIGN_NAME_NOT_CONFIGURED_MESSAGE + "(auth.message.sms.sign-name)");
		}
		return signName;
	}

}
