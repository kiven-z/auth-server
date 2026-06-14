package com.auth.service.auth.infrastructure.persistence.adapter;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.auth.application.port.invalidation.UserInvalidationStateQueryPort;
import com.auth.service.auth.domain.authorization.invalidation.model.UserInvalidationState;
import com.auth.service.auth.infrastructure.persistence.UserMapper;
import com.auth.service.auth.infrastructure.persistence.projection.UserInvalidationStateProjection;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 用户失效状态查询适配器。
 *
 * @author Bunny
 */
@Repository
public class UserInvalidationStateQueryAdapter implements UserInvalidationStateQueryPort {

	private final UserMapper userMapper;

	public UserInvalidationStateQueryAdapter(UserMapper userMapper) {
		this.userMapper = userMapper;
	}

	@Override
	public List<UserInvalidationState> loadByUserIds(Collection<Long> userIds) {
		List<Long> ids = userIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(ids)) {
			return List.of();
		}

		// 查询用户失效状态
		List<UserInvalidationStateProjection> projections = userMapper.selectInvalidationStatesByUserIds(ids);
		if (CollUtil.isEmpty(projections)) {
			return List.of();
		}

		// 转换为用户失效状态列表
		List<UserInvalidationState> states = projections.stream()
			.filter(Objects::nonNull)
			.filter(projection -> projection.getUserId() != null)
			.map(projection -> new UserInvalidationState(projection.getUserId(), projection.getStatus(),
					projection.getIsDeleted() == 1))
			.toList();

		return List.copyOf(states);
	}

}
