package com.auth.service.system.message.dingtalk.client;

import com.auth.module.message.config.properties.DingTalkProperties;
import com.auth.module.message.dingtalk.DingTalkMessageType;
import com.auth.service.system.message.channel.dingtalk.DingTalkAccessTokenClient;
import com.auth.service.system.message.channel.dingtalk.DingTalkWorkNoticeClient;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link DingTalkWorkNoticeClient} 单元测试
 */
@DisplayName("DingTalkWorkNoticeClient 工作通知")
@ExtendWith(MockitoExtension.class)
class DingTalkWorkNoticeClientTest {

	@Mock
	private DingTalkAccessTokenClient accessTokenClient;

	@Mock
	private RestTemplate restTemplate;

	private DingTalkProperties properties;

	private DingTalkWorkNoticeClient client;

	@BeforeEach
	void setUp() {
		properties = new DingTalkProperties();
		properties.setAgentId(4589739774L);
		client = new DingTalkWorkNoticeClient(properties, accessTokenClient, restTemplate);
	}

	@Test
	@DisplayName("发送文本工作通知成功")
	void sendWorkNotice_text_success() {
		when(accessTokenClient.getAccessToken()).thenReturn("access-token");
		when(restTemplate.postForEntity(contains("asyncsend_v2"), ArgumentMatchers.<HttpEntity<?>>any(),
				eq(String.class)))
			.thenReturn(ResponseEntity.ok("{\"errcode\":0,\"errmsg\":\"ok\",\"task_id\":1}"));

		client.sendWorkNotice(List.of("user001"), DingTalkMessageType.TEXT, null, "hello");

		verify(restTemplate).postForEntity(contains("asyncsend_v2"), ArgumentMatchers.<HttpEntity<?>>any(),
				eq(String.class));
	}

	@Test
	@DisplayName("钉钉返回 errcode 非 0 时抛出异常")
	void sendWorkNotice_apiError_throws() {
		when(accessTokenClient.getAccessToken()).thenReturn("access-token");
		when(restTemplate.postForEntity(contains("asyncsend_v2"), ArgumentMatchers.<HttpEntity<?>>any(),
				eq(String.class)))
			.thenReturn(ResponseEntity.ok("{\"errcode\":40014,\"errmsg\":\"invalid token\"}"));

		assertThatThrownBy(() -> client.sendWorkNotice(List.of("user001"), DingTalkMessageType.TEXT, null, "hello"))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("invalid token");
	}

}
