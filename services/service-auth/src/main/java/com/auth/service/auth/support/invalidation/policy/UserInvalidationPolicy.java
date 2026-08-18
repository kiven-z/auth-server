package com.auth.service.auth.support.invalidation.policy;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.auth.model.po.user.UserInvalidationStatePO;
import com.auth.service.auth.model.value.invalidation.UserInvalidationBuckets;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户失效分桶策略：库中有行则递增版本并刷画像；无行则仅驱逐缓存
 *
 * @author Bunny
 */
@Component
public class UserInvalidationPolicy {

	/**
	 * 按影响面与状态快照分桶：有用户行则递增版本并刷画像；库中已无行则仅驱逐缓存
	 * @param impactedUserIds 失效影响用户 ID
	 * @param states 用户状态快照（可与影响面部分重合）
	 * @return 分桶结果
	 */
	public UserInvalidationBuckets partition(Set<Long> impactedUserIds, List<UserInvalidationStatePO> states) {
		Set<Long> versionBumpUserIds = CollUtil.emptyIfNull(states)
			.stream()
			.map(UserInvalidationStatePO::getUserId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		Set<Long> evictOnlyUserIds = CollUtil.emptyIfNull(impactedUserIds)
			.stream()
			.filter(Objects::nonNull)
			.filter(id -> !versionBumpUserIds.contains(id))
			.collect(Collectors.toSet());

		return UserInvalidationBuckets.builder()
			.versionBumpUserIds(versionBumpUserIds)
			.evictOnlyUserIds(evictOnlyUserIds)
			.build();
	}

}
