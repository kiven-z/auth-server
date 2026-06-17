package com.auth.service.auth.infrastructure.persistence.adapter;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.application.port.UserReadPort;
import com.auth.service.auth.domain.authorization.model.UserAccountSnapshot;
import com.auth.service.auth.domain.session.model.UserOnlineBrief;
import com.auth.service.auth.infrastructure.persistence.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@link UserReadPort} 适配器：委托 {@link UserMapper} 查询并映射用户快照。
 *
 * @author Bunny
 */
@Repository
public class UserReadPortAdapter implements UserReadPort {

	private final UserMapper userMapper;

	public UserReadPortAdapter(UserMapper userMapper) {
		this.userMapper = userMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<UserAccountSnapshot> findById(Long userId) {
		if (userId == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(userMapper.selectById(userId)).map(UserAccountSnapshot::from);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Long, UserOnlineBrief> findOnlineBriefByIds(Collection<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return Collections.emptyMap();
		}

		List<Long> distinctIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(distinctIds)) {
			return Collections.emptyMap();
		}

		QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
		wrapper.in("id", distinctIds).select("id", "username", "nickname");
		return userMapper.selectList(wrapper)
			.stream()
			.collect(Collectors.toMap(UserEntity::getId,
					entity -> new UserOnlineBrief(entity.getId(), entity.getUsername(), entity.getNickname()),
					(left, right) -> left));
	}

}
