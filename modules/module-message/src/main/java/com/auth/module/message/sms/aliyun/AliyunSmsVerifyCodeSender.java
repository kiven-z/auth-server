package com.auth.module.message.sms.aliyun;

import cn.hutool.core.text.CharSequenceUtil;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 阿里云 Dypnsapi 验证码短信发送实现
 *
 * @author Bunny
 */
@Slf4j
public class AliyunSmsVerifyCodeSender implements SmsVerifyCodeSender {

	private final AsyncClient aliyunSmsAsyncClient;

	public AliyunSmsVerifyCodeSender(AsyncClient aliyunSmsAsyncClient) {
		this.aliyunSmsAsyncClient = aliyunSmsAsyncClient;
	}

	@Override
	public void sendVerifyCode(String phone, String providerTemplateCode, String signName, String templateParam,
			String countryCode, String schemeName) {
		try {
			SendSmsVerifyCodeRequest.Builder builder = SendSmsVerifyCodeRequest.builder()
				.templateCode(providerTemplateCode)
				.signName(signName)
				.phoneNumber(phone)
				.countryCode(countryCode)
				.templateParam(templateParam);
			if (CharSequenceUtil.isNotBlank(schemeName)) {
				builder.schemeName(schemeName);
			}
			SendSmsVerifyCodeResponse response = aliyunSmsAsyncClient.sendSmsVerifyCode(builder.build()).get();
			log.debug("Aliyun SMS sent, phone={}, templateCode={}, response={}", phone, providerTemplateCode, response);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Aliyun SMS send interrupted: " + e.getMessage(), e);
		}
		catch (Exception e) {
			log.error("Aliyun SMS send failed, phone={}, templateCode={}", phone, providerTemplateCode, e);
			throw new IllegalStateException("Aliyun SMS send failed: " + e.getMessage(), e);
		}
	}

}
