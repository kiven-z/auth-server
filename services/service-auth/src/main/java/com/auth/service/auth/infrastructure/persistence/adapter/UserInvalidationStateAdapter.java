package com.auth.service.auth.infrastructure.persistence.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Repository;

import com.auth.service.auth.application.port.invalidation.UserInvalidationStatePort;
import com.auth.service.auth.config.AuthorizationInvalidationProperties;
import com.auth.service.auth.domain.authorization.model.UserInvalidationState;
import com.auth.service.auth.infrastructure.persistence.mapper.UserMapper;
import com.auth.service.auth.infrastructure.persistence.mapper.UserPermVersionMapper;
import com.auth.service.auth.infrastructure.persistence.projection.UserInvalidationStateProjection;
import com.auth.service.auth.shared.support.BatchPartition;

import cn.hutool.core.collection.CollUtil;

/**
 * 用户失效状态读写适配器。
 *
 * @author Bunny
 */
@Repository
public class UserInvalidationStateAdapter implements UserInvalidationStatePort {

	private final UserMapper userMapper;

	private final UserPermVersionMapper userPermVersionMapper;

	private final AuthorizationInvalidationProperties authorizationInvalidationProperties;

	public UserInvalidationStateAdapter(UserMapper userMapper, UserPermVersionMapper userPermVersionMapper,
			AuthorizationInvalidationProperties authorizationInvalidationProperties) {
		this.userMapper = userMapper;
		this.userPermVersionMapper = userPermVersionMapper;
		this.authorizationInvalidationProperties = authorizationInvalidationProperties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UserInvalidationState> loadByUserIds(Collection<Long> userIds) {
		// 如果用户 ID 集合为空，则返回空列表
		List<Long> ids = userIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(ids)) {
			return List.of();
		}

		// 分批查询用户失效状态
		List<UserInvalidationStateProjection> projections = new ArrayList<>();
		for (List<Long> batch : BatchPartition.partitionIds(ids, authorizationInvalidationProperties.getBatchSize())) {
			List<UserInvalidationStateProjection> batchProjections = userMapper
				.selectInvalidationStatesByUserIds(batch);
			if (CollUtil.isNotEmpty(batchProjections)) {
				projections.addAll(batchProjections);
			}
		}
		if (CollUtil.isEmpty(projections)) {
			return List.of();
		}

		return projections.stream()
			.filter(Objects::nonNull)
			.filter(projection -> projection.getUserId() != null)
			.map(projection -> new UserInvalidationState(projection.getUserId(), projection.getStatus()))
			.toList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int incrementPermVersionInBatches(Collection<Long> userIds, int batchSize) {
		if (CollUtil.isEmpty(userIds) || batchSize <= 0) {
			return 0;
		}

		// 分批递增 perm_version
		int totalUpdated = 0;
		for (List<Long> batch : BatchPartition.partitionIds(userIds, batchSize)) {
			totalUpdated += userPermVersionMapper.incrementPermVersionByUserIds(batch);
		}
		return totalUpdated;
	}

}
