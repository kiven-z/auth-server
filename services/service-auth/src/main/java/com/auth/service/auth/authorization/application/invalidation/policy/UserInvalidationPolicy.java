package com.auth.service.auth.authorization.application.invalidation.policy;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.auth.authorization.domain.invalidation.model.UserInvalidationBuckets;
import com.auth.service.auth.authorization.domain.invalidation.model.UserInvalidationState;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户失效分桶策略：与 {@link com.auth.module.platform.persistence.model.UserEntity#getStatus()}
 * 约定一致。
 *
 * @author Bunny
 */
@Component
public class UserInvalidationPolicy {

	/**
	 * 正常启用。
	 */
	private static final int STATUS_NORMAL = 1;

	/**
	 * 判断用户状态是否需要递增版本+刷画像
	 * @param state 用户状态
	 * @return 是否需要递增版本+刷画像
	 */
	private static boolean shouldVersionBump(UserInvalidationState state) {
		return !state.deleted() && state.status() != null && state.status() == STATUS_NORMAL;
	}

	/**
	 * 将反查得到的用户按状态分为「递增版本+刷画像」与「仅驱逐缓存」。
	 * @param states 用户状态快照（须含反查得到的每个 userId）
	 * @return 分桶结果
	 */
	public UserInvalidationBuckets partition(Collection<UserInvalidationState> states) {
		// 如果状态集合为空，则返回空分桶
		if (CollUtil.isEmpty(states)) {
			return UserInvalidationBuckets.empty();
		}

		// 递增版本+刷画像的用户ID集合
		Set<Long> versionBumpUserIds = new HashSet<>();
		// 仅驱逐缓存的用户ID集合
		Set<Long> evictOnlyUserIds = new HashSet<>();

		// 遍历用户状态快照
		for (UserInvalidationState state : states) {
			// 如果用户状态为空或用户ID为空，则跳过
			if (state.userId() == null) {
				continue;
			}

			// 如果用户状态需要递增版本+刷画像，则将用户ID添加到递增版本+刷画像的用户ID集合中
			if (shouldVersionBump(state)) {
				versionBumpUserIds.add(state.userId());
			}
			else {
				evictOnlyUserIds.add(state.userId());
			}
		}

		return new UserInvalidationBuckets(versionBumpUserIds, evictOnlyUserIds);
	}

}
