package com.auth.service.auth.infrastructure.cookie;

import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.service.auth.infrastructure.cookie.adapter.RefreshCookieAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * {@link RefreshCookieAdapter} remember-me 策略单元测试
 */
@ExtendWith(MockitoExtension.class)
class RememberMeCookiePolicyTest {

	@Mock
	private JwtProperties jwtProperties;

	@Test
	@DisplayName("readMeDay 由 refreshExpired 向上取整为天")
	void resolveReadMeDay_shouldCeilToDays() {
		when(jwtProperties.getRefreshExpired()).thenReturn(604800L);
		RefreshCookieAdapter adapter = new RefreshCookieAdapter(jwtProperties);

		assertEquals(7L, adapter.resolveReadMeDay());
	}

	@Test
	@DisplayName("refreshExpired 非法时 readMeDay 至少为 1")
	void resolveReadMeDay_shouldDefaultToOne_whenRefreshExpiredNonPositive() {
		when(jwtProperties.getRefreshExpired()).thenReturn(0L);
		RefreshCookieAdapter adapter = new RefreshCookieAdapter(jwtProperties);

		assertEquals(1L, adapter.resolveReadMeDay());
	}

}
