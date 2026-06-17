package com.auth.module.security.contract.dto.invalidation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link AuthorizationInvalidateResponse} 契约测试
 *
 * @author Bunny
 */
@DisplayName("AuthorizationInvalidateResponse 授权失效响应契约")
class AuthorizationInvalidateResponseTest {

	@Test
	@DisplayName("empty 应返回全零计数")
	void empty_shouldReturnZeroCounts() {
		AuthorizationInvalidateResponse response = AuthorizationInvalidateResponse.empty();

		assertEquals(0, response.impactedUserCount());
		assertEquals(0, response.versionBumpedCount());
		assertEquals(0, response.profileRefreshedCount());
		assertEquals(0, response.profileEvictedCount());
	}

}
