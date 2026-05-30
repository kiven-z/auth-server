package com.auth.service.auth.authorization.domain.invalidation.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link UserInvalidationBuckets} 单元测试
 */
@DisplayName("UserInvalidationBuckets 用户失效分桶")
class UserInvalidationBucketsTest {

	@Test
	@DisplayName("empty 工厂两桶皆空且 allUserIds 为空集")
	void empty_shouldBeEmptyWithNoUserIds() {
		UserInvalidationBuckets buckets = UserInvalidationBuckets.empty();

		assertEquals(Set.of(), buckets.allUserIds());
	}

	@Test
	@DisplayName("仅 versionBump 桶有用户时 allUserIds 仅含该桶")
	void onlyVersionBump_allUserIds_shouldContainBumpIdsOnly() {
		UserInvalidationBuckets buckets = new UserInvalidationBuckets(Set.of(1L), Set.of());

		assertTrue(buckets.evictOnlyUserIds().isEmpty());
		assertEquals(Set.of(1L), buckets.allUserIds());
	}

	@Test
	@DisplayName("仅 evictOnly 桶有用户时 allUserIds 仅含该桶")
	void onlyEvictOnly_allUserIds_shouldContainEvictIdsOnly() {
		UserInvalidationBuckets buckets = new UserInvalidationBuckets(Set.of(), Set.of(2L));

		assertTrue(buckets.versionBumpUserIds().isEmpty());
		assertEquals(Set.of(2L), buckets.allUserIds());
	}

	@Test
	@DisplayName("allUserIds 合并两桶用户")
	void allUserIds_shouldMergeBothBuckets() {
		UserInvalidationBuckets buckets = new UserInvalidationBuckets(Set.of(1L, 2L), Set.of(3L));

		assertEquals(Set.of(1L, 2L, 3L), buckets.allUserIds());
	}

	@Test
	@DisplayName("allUserIds 对跨桶重复用户 ID 去重")
	void allUserIds_overlappingUserIds_shouldDedupe() {
		UserInvalidationBuckets buckets = new UserInvalidationBuckets(Set.of(10L, 20L), Set.of(20L, 30L));

		assertEquals(Set.of(10L, 20L, 30L), buckets.allUserIds());
	}

	@Test
	@DisplayName("null 入参规范为空集")
	void constructor_nullSets_shouldNormalizeToEmpty() {
		UserInvalidationBuckets buckets = new UserInvalidationBuckets(null, null);

		assertEquals(Set.of(), buckets.allUserIds());
	}

	@Test
	@DisplayName("构造后外部修改入参集合不影响分桶")
	void constructor_shouldDefensivelyCopyInput() {
		Set<Long> bumpIds = new HashSet<>(Set.of(100L));
		Set<Long> evictIds = new HashSet<>(Set.of(200L));
		UserInvalidationBuckets buckets = new UserInvalidationBuckets(bumpIds, evictIds);

		bumpIds.add(999L);
		evictIds.clear();

		assertEquals(Set.of(100L), buckets.versionBumpUserIds());
		assertEquals(Set.of(200L), buckets.evictOnlyUserIds());
	}

	@Test
	@DisplayName("allUserIds 返回不可变集合")
	void allUserIds_shouldReturnUnmodifiableSet() {
		UserInvalidationBuckets buckets = new UserInvalidationBuckets(Set.of(1L), Set.of());

		Set<Long> allUserIds = new HashSet<>(buckets.allUserIds());
		assertThrows(UnsupportedOperationException.class, () -> allUserIds.add(2L));
	}

}
