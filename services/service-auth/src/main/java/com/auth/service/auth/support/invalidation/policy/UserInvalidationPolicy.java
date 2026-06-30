package com.auth.service.auth.support.invalidation.policy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.auth.model.po.user.UserInvalidationStatePO;
import com.auth.service.auth.model.value.invalidation.UserInvalidationBuckets;

import org.springframework.stereotype.Component;

/**
 * 用户失效分桶策略：与 {@link com.auth.module.platform.persistence.model.UserEntity#getStatus()}
 * 约定一致
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
		// 如果状态集合为空，则返回空分桶
		if (CollUtil.isEmpty(states)) {
			return UserInvalidationBuckets.empty();
		}

		// 递增版本+刷画像的用户ID集合
		Set<Long> versionBumpUserIds = new HashSet<>();
		// 仅驱逐缓存的用户ID集合
		Set<Long> evictOnlyUserIds = new HashSet<>();

		for (UserInvalidationStatePO state : states) {
			// 如果用户状态为空或用户ID为空，则跳过
			if (state.getUserId() == null) {
				continue;
			}

			// 如果用户状态需要递增版本+刷画像，则将用户ID添加到递增版本+刷画像的用户ID集合中
			if (state.getStatus() != null && state.getStatus() == 1) {
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
