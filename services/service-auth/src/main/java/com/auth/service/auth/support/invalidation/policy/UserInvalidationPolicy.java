package com.auth.service.auth.support.invalidation.policy;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.data.model.enums.UserStatus;
import com.auth.service.auth.model.po.user.UserInvalidationStatePO;
import com.auth.service.auth.model.value.invalidation.UserInvalidationBuckets;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户失效分桶策略：按 {@link UserStatus} 区分启用与非启用
 *
 * @author Bunny
 */
@Component
public class UserInvalidationPolicy {

	/**
	 * 将反查得到的用户按状态分为「递增版本+刷画像」与「仅驱逐缓存」
	 * @param states 用户状态快照（须含反查得到的每个 userId）
	 * @return 分桶结果
	 */
	public UserInvalidationBuckets partition(Collection<UserInvalidationStatePO> states) {
		if (CollUtil.isEmpty(states)) {
			return UserInvalidationBuckets.empty();
		}

		Set<Long> versionBumpUserIds = new HashSet<>();
		Set<Long> evictOnlyUserIds = new HashSet<>();

		for (UserInvalidationStatePO state : states) {
			if (state.getUserId() == null) {
				continue;
			}

			UserStatus status = UserStatus.ofNullable(state.getStatus());
			if (status != null && status.allowsLogin()) {
				versionBumpUserIds.add(state.getUserId());
			}
			else {
				evictOnlyUserIds.add(state.getUserId());
			}
		}

		return UserInvalidationBuckets.builder()
			.versionBumpUserIds(versionBumpUserIds)
			.evictOnlyUserIds(evictOnlyUserIds)
			.build();
	}

}
