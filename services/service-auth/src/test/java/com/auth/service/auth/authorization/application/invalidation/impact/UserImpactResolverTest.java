package com.auth.service.auth.authorization.application.invalidation.impact;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.UserInvalidatePayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link UserImpactResolver} 单元测试
 */
@DisplayName("UserImpactResolver 用户主档影响面")
class UserImpactResolverTest {

	@Test
	@DisplayName("按 payload 中的 userIds 返回影响面")
	void resolve_shouldReturnUserIds() {
		UserImpactResolver resolver = new UserImpactResolver();

		Set<Long> impacted = resolver.resolve(new UserInvalidatePayload(List.of(1L, 2L)));

		assertEquals(Set.of(1L, 2L), impacted);
	}

	@Test
	@DisplayName("kind 为 USER")
	void kind_shouldBeUser() {
		UserImpactResolver resolver = new UserImpactResolver();

		assertEquals(AuthorizationChangeKind.USER, resolver.kind());
	}

}
