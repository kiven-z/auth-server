package com.auth.module.message.modules.sms.aliyun;

import cn.hutool.core.text.CharSequenceUtil;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.auth.module.message.modules.sms.SmsSender;
import lombok.extern.slf4j.Slf4j;

/**
 * 阿里云 Dypns 验证码短信发送实现
 *
 * @author Bunny
 */
@Slf4j
public class AliyunDypnsSmsSender implements SmsSender, AutoCloseable {

	private final DypnsVerifyCodeClient dypnsClient;

	/**
	 * 使用默认 Dypns 客户端
	 * @param dypnsClient 可注入的 Dypns 客户端（生产环境由自动配置创建）
	 */
	public AliyunDypnsSmsSender(DypnsVerifyCodeClient dypnsClient) {
		this.dypnsClient = dypnsClient;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendVerifyCode(String phone, String signName, String providerTemplateCode, String templateParamJson) {
		if (CharSequenceUtil.isBlank(phone)) {
			throw new IllegalArgumentException("phone must not be blank");
		}
		if (CharSequenceUtil.isBlank(signName)) {
			throw new IllegalArgumentException("signName must not be blank");
		}
		if (CharSequenceUtil.isBlank(providerTemplateCode)) {
			throw new IllegalArgumentException("providerTemplateCode must not be blank");
		}

		SendSmsVerifyCodeRequest request = SendSmsVerifyCodeRequest.builder()
			.phoneNumber(phone)
			.signName(signName)
			.templateCode(providerTemplateCode)
			.templateParam(CharSequenceUtil.blankToDefault(templateParamJson, "{}"))
			.build();

		try {
			SendSmsVerifyCodeResponse response = dypnsClient.send(request);
			assertSendSuccess(response, phone, providerTemplateCode, signName);
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
		if (dypnsClient instanceof AutoCloseable closeable) {
			try {
				closeable.close();
			}
			catch (Exception e) {
				log.warn("Failed to close Dypns client", e);
			}
		}
	}

	/**
	 * 校验厂商回执
	 * @param response 响应
	 * @param phone 手机号
	 * @param providerTemplateCode 模板编码
	 * @param signName 签名
	 */
	private void assertSendSuccess(SendSmsVerifyCodeResponse response, String phone, String providerTemplateCode,
			String signName) {
		if (response != null && response.getBody() != null
				&& "OK".equalsIgnoreCase(String.valueOf(response.getBody().getCode()))) {
			log.debug("Aliyun Dypns send ok, phone={}, templateCode={}, signName={}", phone, providerTemplateCode,
					signName);
			return;
		}
		String detail = response == null ? "null response"
				: (response.getBody() == null ? "null body" : String.valueOf(response.getBody()));
		log.error("Aliyun Dypns send failed, phone={}, templateCode={}, detail={}", phone, providerTemplateCode,
				detail);
		throw new IllegalStateException("Aliyun Dypns send failed: " + detail);
	}

}
