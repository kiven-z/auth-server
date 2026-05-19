package com.auth.module.message.sender.modules.sms.aliyun;

import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.auth.module.message.sender.config.properties.SmsProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AliyunDypnsVerifyCodeSender} 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AliyunDypnsVerifyCodeSender 阿里云 Dypns 短信")
class AliyunDypnsVerifyCodeSenderTest {

	@Mock
	private AsyncClient asyncClient;

	@Test
	@DisplayName("厂商返回 OK 时发送成功")
	void sendVerifyCode_shouldSucceedWhenVendorReturnsOk() throws Exception {
		SmsProperties properties = validSmsProperties();
		AliyunDypnsVerifyCodeSender sender = new AliyunDypnsVerifyCodeSender(properties, asyncClient);

		SendSmsVerifyCodeResponseBody body = mock(SendSmsVerifyCodeResponseBody.class);
		when(body.getCode()).thenReturn("OK");
		SendSmsVerifyCodeResponse response = mock(SendSmsVerifyCodeResponse.class);
		when(response.getBody()).thenReturn(body);
		when(asyncClient.sendSmsVerifyCode(any(SendSmsVerifyCodeRequest.class)))
			.thenReturn(CompletableFuture.completedFuture(response));

		sender.sendVerifyCode("13800000000", "100001", "{\"code\":\"123456\"}");

		verify(asyncClient).sendSmsVerifyCode(any(SendSmsVerifyCodeRequest.class));
	}

	@Test
	@DisplayName("厂商返回非 OK 时抛出 IllegalStateException")
	void sendVerifyCode_shouldThrowWhenVendorReturnsError() throws Exception {
		SmsProperties properties = validSmsProperties();
		AliyunDypnsVerifyCodeSender sender = new AliyunDypnsVerifyCodeSender(properties, asyncClient);

		SendSmsVerifyCodeResponseBody body = mock(SendSmsVerifyCodeResponseBody.class);
		when(body.getCode()).thenReturn("isv.BUSINESS_LIMIT_CONTROL");
		SendSmsVerifyCodeResponse response = mock(SendSmsVerifyCodeResponse.class);
		when(response.getBody()).thenReturn(body);
		when(asyncClient.sendSmsVerifyCode(any(SendSmsVerifyCodeRequest.class)))
			.thenReturn(CompletableFuture.completedFuture(response));

		assertThatThrownBy(() -> sender.sendVerifyCode("13800000000", "100001", "{}"))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Aliyun Dypns send failed");
	}

	private static SmsProperties validSmsProperties() {
		SmsProperties properties = new SmsProperties();
		properties.setRegion("cn-hangzhou");
		properties.setEndpoint("dypnsapi.aliyuncs.com");
		properties.setAccessKeyId("test-ak");
		properties.setAccessKeySecret("test-sk");
		properties.setSignName("TestSign");
		return properties;
	}

}
