package com.auth.service.auth.support.invalidation.policy;

import java.util.List;
import java.util.Set;

import com.auth.service.auth.model.po.user.UserInvalidationStatePO;
import com.auth.service.auth.model.value.invalidation.UserInvalidationBuckets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link UserInvalidationPolicy} 单元测试
 */
@DisplayName("UserInvalidationPolicy 用户失效分桶")
class UserInvalidationPolicyTest {

	private final UserInvalidationPolicy policy = new UserInvalidationPolicy();

	private static UserInvalidationStatePO state(Long userId, Integer status) {
		UserInvalidationStatePO state = new UserInvalidationStatePO();
		state.setUserId(userId);
		state.setStatus(status);
		return state;
	}

	@Test
	@DisplayName("正常用户进入 versionBump 桶")
	void partition_activeUser_shouldVersionBump() {
		UserInvalidationBuckets buckets = policy.partition(List.of(state(1L, 1)));

		assertEquals(Set.of(1L), buckets.versionBumpUserIds());
		assertTrue(buckets.evictOnlyUserIds().isEmpty());
	}

	@Test
	@DisplayName("禁用用户仅驱逐缓存")
	void partition_disabledUser_shouldEvictOnly() {
		UserInvalidationBuckets buckets = policy.partition(List.of(state(2L, 0)));

		assertTrue(buckets.versionBumpUserIds().isEmpty());
		assertEquals(Set.of(2L), buckets.evictOnlyUserIds());
	}

	@Test
	@DisplayName("锁定用户仅驱逐缓存")
	void partition_lockedUser_shouldEvictOnly() {
		UserInvalidationBuckets buckets = policy.partition(List.of(state(3L, 2)));

		assertTrue(buckets.versionBumpUserIds().isEmpty());
		assertEquals(Set.of(3L), buckets.evictOnlyUserIds());
	}

	@Test
	@DisplayName("空输入返回空分桶")
	void partition_empty_shouldReturnEmptyBuckets() {
		UserInvalidationBuckets buckets = policy.partition(List.of());

		assertEquals(Set.of(), buckets.allUserIds());
	}

}
