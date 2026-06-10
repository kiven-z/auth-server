package com.auth.module.security.contract.api.authorization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link AuthorizationChangeKind} 单元测试
 */
@DisplayName("AuthorizationChangeKind 授权变更维度")
class AuthorizationChangeKindTest {

	@Test
	@DisplayName("eventIdPrefix 应与各维度约定短前缀一致")
	void eventIdPrefix_shouldMatchContract() {
		assertEquals("role", AuthorizationChangeKind.ROLE.eventIdPrefix());
		assertEquals("perm", AuthorizationChangeKind.PERMISSION.eventIdPrefix());
		assertEquals("policy", AuthorizationChangeKind.POLICY.eventIdPrefix());
		assertEquals("grant", AuthorizationChangeKind.GRANT.eventIdPrefix());
		assertEquals("dept", AuthorizationChangeKind.USER_DEPT.eventIdPrefix());
		assertEquals("post", AuthorizationChangeKind.USER_POST.eventIdPrefix());
		assertEquals("user", AuthorizationChangeKind.USER.eventIdPrefix());
	}

}
