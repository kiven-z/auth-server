package com.auth.service.auth.authorization.application.invalidation.policy;

import com.auth.service.auth.authorization.domain.invalidation.model.UserInvalidationBuckets;
import com.auth.service.auth.authorization.domain.invalidation.model.UserInvalidationState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link UserInvalidationPolicy} 单元测试
 */
@DisplayName("UserInvalidationPolicy 用户失效分桶")
class UserInvalidationPolicyTest {

	private final UserInvalidationPolicy policy = new UserInvalidationPolicy();

	@Test
	@DisplayName("正常用户进入 versionBump 桶")
	void partition_activeUser_shouldVersionBump() {
		UserInvalidationBuckets buckets = policy.partition(List.of(new UserInvalidationState(1L, 1, false)));

		assertEquals(Set.of(1L), buckets.versionBumpUserIds());
		assertTrue(buckets.evictOnlyUserIds().isEmpty());
	}

	@Test
	@DisplayName("禁用用户仅驱逐缓存")
	void partition_disabledUser_shouldEvictOnly() {
		UserInvalidationBuckets buckets = policy.partition(List.of(new UserInvalidationState(2L, 0, false)));

		assertTrue(buckets.versionBumpUserIds().isEmpty());
		assertEquals(Set.of(2L), buckets.evictOnlyUserIds());
	}

	@Test
	@DisplayName("锁定用户仅驱逐缓存")
	void partition_lockedUser_shouldEvictOnly() {
		UserInvalidationBuckets buckets = policy.partition(List.of(new UserInvalidationState(3L, 2, false)));

		assertTrue(buckets.versionBumpUserIds().isEmpty());
		assertEquals(Set.of(3L), buckets.evictOnlyUserIds());
	}

	@Test
	@DisplayName("逻辑删除用户仅驱逐缓存")
	void partition_deletedUser_shouldEvictOnly() {
		UserInvalidationBuckets buckets = policy.partition(List.of(new UserInvalidationState(4L, 1, true)));

		assertTrue(buckets.versionBumpUserIds().isEmpty());
		assertEquals(Set.of(4L), buckets.evictOnlyUserIds());
	}

	@Test
	@DisplayName("空输入返回空分桶")
	void partition_empty_shouldReturnEmptyBuckets() {
		UserInvalidationBuckets buckets = policy.partition(List.of());

		assertEquals(Set.of(), buckets.allUserIds());
	}

}
