package com.auth.module.message.sms.aliyun;

import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.auth.module.message.config.properties.SmsProperties;
import com.auth.module.message.modules.sms.aliyun.AliyunDypnsSmsSender;
import com.auth.module.message.modules.sms.aliyun.DypnsVerifyCodeClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AliyunDypnsSmsSender} 单元测试
 */
@DisplayName("AliyunDypnsSmsSender 阿里云验证码短信")
@ExtendWith(MockitoExtension.class)
class AliyunDypnsSmsSenderTest {

	@Mock
	private DypnsVerifyCodeClient dypnsClient;

	private SmsProperties smsProperties;

	@BeforeEach
	void setUp() {
		smsProperties = new SmsProperties();
		smsProperties.setSignName("测试签名");
	}

	@Test
	@DisplayName("成功回执时调用 Dypns 并传入配置的短信签名")
	void sendVerifyCode_success() throws Exception {
		SendSmsVerifyCodeResponseBody body = SendSmsVerifyCodeResponseBody.builder().code("OK").build();
		SendSmsVerifyCodeResponse response = mock(SendSmsVerifyCodeResponse.class);
		when(response.getBody()).thenReturn(body);
		when(dypnsClient.send(any(SendSmsVerifyCodeRequest.class))).thenReturn(response);

		AliyunDypnsSmsSender sender = new AliyunDypnsSmsSender(dypnsClient, smsProperties);
		sender.sendVerifyCode("13800000000", "100001", "{\"code\":\"1234\",\"min\":\"5\"}");

		ArgumentCaptor<SendSmsVerifyCodeRequest> captor = ArgumentCaptor.forClass(SendSmsVerifyCodeRequest.class);
		verify(dypnsClient).send(captor.capture());
		SendSmsVerifyCodeRequest request = captor.getValue();
		assertThat(request.getPhoneNumber()).isEqualTo("13800000000");
		assertThat(request.getSignName()).isEqualTo("测试签名");
		assertThat(request.getTemplateCode()).isEqualTo("100001");
		assertThat(request.getTemplateParam()).contains("1234");
	}

	@Test
	@DisplayName("未配置 signName 时抛出 IllegalStateException")
	void sendVerifyCode_signNameMissing() {
		smsProperties.setSignName(null);

		AliyunDypnsSmsSender sender = new AliyunDypnsSmsSender(dypnsClient, smsProperties);

		assertThatThrownBy(() -> sender.sendVerifyCode("13800000000", "100001", "{}"))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining(AliyunDypnsSmsSender.SIGN_NAME_NOT_CONFIGURED_MESSAGE);
	}

	@Test
	@DisplayName("厂商回执非 OK 时抛出 IllegalStateException")
	void sendVerifyCode_failureResponse() throws Exception {
		SendSmsVerifyCodeResponseBody body = SendSmsVerifyCodeResponseBody.builder()
			.code("FAIL")
			.message("error")
			.build();
		SendSmsVerifyCodeResponse response = mock(SendSmsVerifyCodeResponse.class);
		when(response.getBody()).thenReturn(body);
		when(dypnsClient.send(any(SendSmsVerifyCodeRequest.class))).thenReturn(response);

		AliyunDypnsSmsSender sender = new AliyunDypnsSmsSender(dypnsClient, smsProperties);

		assertThatThrownBy(() -> sender.sendVerifyCode("13800000000", "100001", "{\"code\":\"1\"}"))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Aliyun Dypns send failed");
	}

	@Test
	@DisplayName("线程中断时恢复中断标志并包装为 IllegalStateException")
	void sendVerifyCode_interrupted() throws Exception {
		when(dypnsClient.send(any(SendSmsVerifyCodeRequest.class))).thenThrow(new InterruptedException("interrupted"));

		AliyunDypnsSmsSender sender = new AliyunDypnsSmsSender(dypnsClient, smsProperties);
		try {
			assertThatThrownBy(() -> sender.sendVerifyCode("13800000000", "100001", "{}"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("interrupted");
			assertThat(Thread.currentThread().isInterrupted()).isTrue();
		}
		finally {
			// 清除本用例设置的中断标志，避免影响后续测试
			Thread.interrupted();
		}
	}

	@Test
	@DisplayName("调用异常时包装为 IllegalStateException")
	void sendVerifyCode_clientThrows() throws Exception {
		when(dypnsClient.send(any(SendSmsVerifyCodeRequest.class))).thenThrow(new RuntimeException("network"));

		AliyunDypnsSmsSender sender = new AliyunDypnsSmsSender(dypnsClient, smsProperties);

		assertThatThrownBy(() -> sender.sendVerifyCode("13800000000", "100001", "{}"))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Aliyun Dypns send error");
	}

}
