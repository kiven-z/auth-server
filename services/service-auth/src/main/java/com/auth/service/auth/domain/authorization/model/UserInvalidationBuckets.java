package com.auth.service.auth.domain.authorization.model;

import cn.hutool.core.util.ObjectUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * 用户失效分桶：正常用户递增版本并刷新画像；其余仅驱逐缓存
 *
 * @param versionBumpUserIds 需要 {@code perm_version + 1} 并刷新画像的用户 ID
 * @param evictOnlyUserIds 仅需驱逐 Redis 画像的用户 ID
 * @author Bunny
 */
public record UserInvalidationBuckets(Set<Long> versionBumpUserIds, Set<Long> evictOnlyUserIds) {

	public UserInvalidationBuckets {
		versionBumpUserIds = Set.copyOf(ObjectUtil.defaultIfNull(versionBumpUserIds, Set.of()));
		evictOnlyUserIds = Set.copyOf(ObjectUtil.defaultIfNull(evictOnlyUserIds, Set.of()));
	}

	/**
	 * 空分桶
	 * @return 无任何用户
	 */
	public static UserInvalidationBuckets empty() {
		return new UserInvalidationBuckets(Set.of(), Set.of());
	}

	/**
	 * 合并后的全部用户 ID（去重）
	 * @return 所有涉及的用户 ID
	 */
	public Set<Long> allUserIds() {
		Set<Long> all = new HashSet<>(versionBumpUserIds);
		all.addAll(evictOnlyUserIds);
		return Set.copyOf(all);
	}

}
