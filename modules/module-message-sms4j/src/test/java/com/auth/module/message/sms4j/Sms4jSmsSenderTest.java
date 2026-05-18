package com.auth.module.message.sms4j;

import com.auth.module.message.config.properties.SmsProperties;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link Sms4jSmsSender} 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Sms4jSmsSender 短信发送")
class Sms4jSmsSenderTest {

	@Mock
	private SmsBlend smsBlend;

	private SmsProperties smsProperties;

	private Sms4jSmsSender sender;

	@BeforeEach
	void setUp() {
		smsProperties = new SmsProperties();
		smsProperties.setDefaultConfigId("aliyun-main");
		sender = new Sms4jSmsSender(smsProperties);
	}

	@Test
	@DisplayName("blend 未注册：抛出 IllegalStateException")
	void sendTemplateSms_blendNotFound_throws() {
		try (MockedStatic<SmsFactory> factory = mockStatic(SmsFactory.class)) {
			factory.when(() -> SmsFactory.getSmsBlend("aliyun-main")).thenReturn(null);

			assertThatThrownBy(() -> sender.sendTemplateSms("13800000001", "SMS_123", "签名", "{}", null))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("blend not found");
		}
	}

	@Test
	@DisplayName("厂商回执成功：正常完成发送")
	void sendTemplateSms_success_completes() {
		SmsResponse response = mock(SmsResponse.class);
		when(response.isSuccess()).thenReturn(true);

		try (MockedStatic<SmsFactory> factory = mockStatic(SmsFactory.class)) {
			factory.when(() -> SmsFactory.getSmsBlend("aliyun-main")).thenReturn(smsBlend);
			when(smsBlend.sendMessage(eq("13800000001"), eq("SMS_123"), any(LinkedHashMap.class))).thenReturn(response);

			sender.sendTemplateSms("13800000001", "SMS_123", "签名", "{\"code\":\"1\"}", null);

			verify(smsBlend).sendMessage(eq("13800000001"), eq("SMS_123"), any(LinkedHashMap.class));
		}
	}

	@Test
	@DisplayName("厂商回执失败：抛出 IllegalStateException")
	void sendTemplateSms_failedResponse_throws() {
		SmsResponse response = mock(SmsResponse.class);
		when(response.isSuccess()).thenReturn(false);
		when(response.getData()).thenReturn("quota exceeded");

		try (MockedStatic<SmsFactory> factory = mockStatic(SmsFactory.class)) {
			factory.when(() -> SmsFactory.getSmsBlend("aliyun-main")).thenReturn(smsBlend);
			when(smsBlend.sendMessage(any(), any(), any())).thenReturn(response);

			assertThatThrownBy(() -> sender.sendTemplateSms("13800000001", "SMS_123", "签名", "{}", null))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("SMS4J send failed")
				.hasMessageContaining("quota exceeded");
		}
	}

}
