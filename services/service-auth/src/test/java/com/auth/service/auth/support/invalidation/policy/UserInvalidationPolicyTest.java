package com.auth.service.auth.support.invalidation.policy;

import com.auth.common.data.model.enums.UserStatus;
import com.auth.service.auth.model.po.user.UserInvalidationStatePO;
import com.auth.service.auth.model.value.invalidation.UserInvalidationBuckets;
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

	private static UserInvalidationStatePO state(Long userId, Integer status) {
		UserInvalidationStatePO state = new UserInvalidationStatePO();
		state.setUserId(userId);
		state.setStatus(status);
		return state;
	}

	@Test
	@DisplayName("库中有行进入 versionBump 桶")
	void partition_existingUser_shouldVersionBump() {
		UserInvalidationBuckets buckets = policy.partition(List.of(1L),
				List.of(state(1L, UserStatus.NORMAL.getCode())));

		assertEquals(Set.of(1L), buckets.versionBumpUserIds());
		assertTrue(buckets.evictOnlyUserIds().isEmpty());
	}

	@Test
	@DisplayName("禁用用户仍有行时进入 versionBump 桶")
	void partition_disabledUser_shouldVersionBump() {
		UserInvalidationBuckets buckets = policy.partition(List.of(2L),
				List.of(state(2L, UserStatus.DISABLED.getCode())));

		assertEquals(Set.of(2L), buckets.versionBumpUserIds());
		assertTrue(buckets.evictOnlyUserIds().isEmpty());
	}

	@Test
	@DisplayName("影响面存在但库中无行时仅驱逐缓存")
	void partition_missingUserRow_shouldEvictOnly() {
		UserInvalidationBuckets buckets = policy.partition(List.of(99L), List.of());

		assertTrue(buckets.versionBumpUserIds().isEmpty());
		assertEquals(Set.of(99L), buckets.evictOnlyUserIds());
	}

	@Test
	@DisplayName("有行与无行混合分桶")
	void partition_mixedImpact_shouldSplitByPresence() {
		UserInvalidationBuckets buckets = policy.partition(List.of(1L, 99L),
				List.of(state(1L, UserStatus.NORMAL.getCode())));

		assertEquals(Set.of(1L), buckets.versionBumpUserIds());
		assertEquals(Set.of(99L), buckets.evictOnlyUserIds());
	}

	@Test
	@DisplayName("空影响面返回空分桶")
	void partition_emptyImpact_shouldReturnEmptyBuckets() {
		UserInvalidationBuckets buckets = policy.partition(List.of(), List.of());

		assertEquals(Set.of(), buckets.allUserIds());
	}

}
