package com.auth.service.system.authorization.dispatch.support;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link InvalidationEventIds} 单元测试
 */
@DisplayName("InvalidationEventIds eventId 生成")
class InvalidationEventIdsTest {

	@Test
	@DisplayName("next 应生成 kind 前缀加雪花 ID")
	void next_shouldUseKindPrefixAndSnowflakeId() {
		// 使用 USER_DEPT 验证前缀来自 AuthorizationChangeKind.eventIdPrefix()
		String eventId = InvalidationEventIds.next(AuthorizationChangeKind.USER_DEPT);

		assertTrue(eventId.startsWith("dept:"));
		assertFalse(eventId.contains("-"));
		assertTrue(eventId.length() <= 64);
	}

}
