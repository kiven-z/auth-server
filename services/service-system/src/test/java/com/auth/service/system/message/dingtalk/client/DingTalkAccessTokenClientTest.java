package com.auth.service.system.message.dingtalk.client;

import com.auth.module.message.config.properties.DingTalkProperties;
import com.auth.service.system.message.channel.dingtalk.DingTalkAccessTokenClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link DingTalkAccessTokenClient} 单元测试
 */
@DisplayName("DingTalkAccessTokenClient 访问凭证")
@ExtendWith(MockitoExtension.class)
class DingTalkAccessTokenClientTest {

	@Mock
	private RestTemplate restTemplate;

	private DingTalkProperties properties;

	private DingTalkAccessTokenClient client;

	@BeforeEach
	void setUp() {
		properties = new DingTalkProperties();
		properties.setClientId("ding-test");
		properties.setClientSecret("secret-test");
		client = new DingTalkAccessTokenClient(properties, restTemplate);
	}

	@Test
	@DisplayName("首次获取 token 并缓存复用")
	void getAccessToken_cachesToken() {
		when(restTemplate.postForEntity(eq("https://api.dingtalk.com/v1.0/oauth2/accessToken"),
				ArgumentMatchers.<HttpEntity<?>>any(), eq(String.class)))
			.thenReturn(ResponseEntity.ok("{\"accessToken\":\"token-abc\",\"expireIn\":7200}"));

		String first = client.getAccessToken();
		String second = client.getAccessToken();

		assertThat(first).isEqualTo("token-abc");
		assertThat(second).isEqualTo("token-abc");
		verify(restTemplate, times(1)).postForEntity(eq("https://api.dingtalk.com/v1.0/oauth2/accessToken"),
				ArgumentMatchers.<HttpEntity<?>>any(), eq(String.class));
	}

}
