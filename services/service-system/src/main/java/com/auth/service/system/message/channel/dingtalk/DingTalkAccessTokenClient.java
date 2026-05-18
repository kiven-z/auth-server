package com.auth.service.system.message.channel.dingtalk;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.auth.module.message.config.properties.DingTalkProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 钉钉应用 accessToken 获取与内存缓存
 *
 * @author Bunny
 */
@Slf4j
@ConditionalOnProperty(prefix = "auth.message.ding-talk", name = "enabled", havingValue = "true")
@Component
public class DingTalkAccessTokenClient {

	private static final String OAUTH_TOKEN_URL = "https://api.dingtalk.com/v1.0/oauth2/accessToken";

	/**
	 * 提前刷新秒数，避免临界过期
	 */
	private static final long REFRESH_SKEW_SECONDS = 120L;

	private final DingTalkProperties dingTalkProperties;

	private final RestTemplate restTemplate;

	private final AtomicReference<CachedToken> cachedToken = new AtomicReference<>();

	public DingTalkAccessTokenClient(DingTalkProperties dingTalkProperties, RestTemplate restTemplate) {
		this.dingTalkProperties = dingTalkProperties;
		this.restTemplate = restTemplate;
	}

	/**
	 * 获取有效 accessToken（带缓存）
	 * @return accessToken
	 */
	public String getAccessToken() {
		CachedToken current = cachedToken.get();
		if (current != null && current.isValid()) {
			return current.token();
		}
		return refreshAccessToken();
	}

	/**
	 * 刷新并缓存 accessToken
	 * @return accessToken
	 */
	private synchronized String refreshAccessToken() {
		// 获取缓存中的 accessToken
		CachedToken current = cachedToken.get();
		if (current != null && current.isValid()) {
			return current.token();
		}

		// 解析客户端ID和客户端密钥
		String clientId = dingTalkProperties.getClientId();
		String clientSecret = dingTalkProperties.getClientSecret();
		// 如果客户端ID或客户端密钥为空，则抛出异常
		if (CharSequenceUtil.isBlank(clientId) || CharSequenceUtil.isBlank(clientSecret)) {
			throw new IllegalStateException("DingTalk clientId or clientSecret is not configured");
		}

		// 构建请求头
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		// 构建请求体
		Map<String, String> body = Map.of("appKey", clientId, "appSecret", clientSecret);
		// 构建请求实体
		HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

		try {
			// 发送请求
			ResponseEntity<String> response = restTemplate.postForEntity(OAUTH_TOKEN_URL, entity, String.class);
			// 解析响应体
			String responseBody = response.getBody();
			if (CharSequenceUtil.isBlank(responseBody)) {
				throw new IllegalStateException("DingTalk OAuth response body is empty");
			}

			// 解析响应体
			JSONObject json = JSONUtil.parseObj(responseBody);
			String accessToken = json.getStr("accessToken");
			if (CharSequenceUtil.isBlank(accessToken)) {
				throw new IllegalStateException("DingTalk OAuth accessToken is blank");
			}

			// 解析过期时间
			Long expireIn = json.getLong("expireIn");
			long expireSeconds = expireIn != null && expireIn > 0 ? expireIn : 7200L;

			// 构建过期时间
			Instant expiresAt = Instant.now().plusSeconds(expireSeconds);
			cachedToken.set(new CachedToken(accessToken, expiresAt));
			log.debug("DingTalk accessToken refreshed, expiresIn={}s", expireSeconds);
			return accessToken;
		}
		catch (RestClientException ex) {
			log.error("DingTalk OAuth token request failed", ex);
			throw new IllegalStateException("DingTalk OAuth token request failed: " + ex.getMessage(), ex);
		}
	}

	private record CachedToken(String token, Instant expiresAt) {
		boolean isValid() {
			return CharSequenceUtil.isNotBlank(token)
					&& Instant.now().isBefore(expiresAt.minusSeconds(REFRESH_SKEW_SECONDS));
		}
	}

}
