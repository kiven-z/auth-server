package com.auth.service.auth.infrastructure.cookie;

import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.service.auth.infrastructure.cookie.adapter.RememberMePolicyAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * RememberMePolicyAdapter 单元测试
 */
@ExtendWith(MockitoExtension.class)
class RememberMePolicyAdapterTest {

	@Mock
	private JwtProperties jwtProperties;

	@Test
	@DisplayName("勾选记住我时 Max-Age 为 refreshExpired")
	void resolveMaxAgeSeconds_shouldUseRefreshExpired_whenRemembered() {
		when(jwtProperties.getRefreshExpired()).thenReturn(604800L);
		RememberMePolicyAdapter policy = new RememberMePolicyAdapter(jwtProperties);

		assertEquals(604800L, policy.resolveMaxAgeSeconds(true));
	}

	@Test
	@DisplayName("未勾选记住我时会话级 Max-Age")
	void resolveMaxAgeSeconds_shouldUseSession_whenNotRemembered() {
		RememberMePolicyAdapter policy = new RememberMePolicyAdapter(jwtProperties);

		assertEquals(RefreshTokenCookieWriter.SESSION_COOKIE_MAX_AGE_SECONDS, policy.resolveMaxAgeSeconds(false));
	}

	@Test
	@DisplayName("readMeDay 由 refreshExpired 向上取整为天")
	void resolveReadMeDay_shouldCeilToDays() {
		when(jwtProperties.getRefreshExpired()).thenReturn(604800L);
		RememberMePolicyAdapter policy = new RememberMePolicyAdapter(jwtProperties);

		assertEquals(7L, policy.resolveReadMeDay());
	}

}
