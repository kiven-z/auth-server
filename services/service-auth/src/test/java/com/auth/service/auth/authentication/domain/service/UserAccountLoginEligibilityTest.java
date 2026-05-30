package com.auth.service.auth.authentication.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link UserAccountLoginEligibility} 单元测试
 */
@DisplayName("UserAccountLoginEligibility 账户登录资格")
class UserAccountLoginEligibilityTest {

	@Test
	@DisplayName("status 为 null 时视为禁用")
	void fromStatus_null_shouldBeDisabled() {
		assertEquals(UserAccountLoginEligibility.DISABLED, UserAccountLoginEligibility.fromStatus(null));
	}

	@Test
	@DisplayName("status 为 0 时视为禁用")
	void fromStatus_zero_shouldBeDisabled() {
		assertEquals(UserAccountLoginEligibility.DISABLED, UserAccountLoginEligibility.fromStatus(0));
	}

	@Test
	@DisplayName("status 为 2 时视为状态锁定")
	void fromStatus_two_shouldBeStatusLocked() {
		assertEquals(UserAccountLoginEligibility.STATUS_LOCKED, UserAccountLoginEligibility.fromStatus(2));
	}

	@Test
	@DisplayName("status 为正常启用值时允许登录")
	void fromStatus_active_shouldBeAllowed() {
		assertEquals(UserAccountLoginEligibility.ALLOWED, UserAccountLoginEligibility.fromStatus(1));
	}

}
