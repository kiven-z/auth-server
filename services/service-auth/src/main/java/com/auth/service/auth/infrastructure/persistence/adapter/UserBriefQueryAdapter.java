package com.auth.service.auth.infrastructure.persistence.adapter;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.application.port.UserBriefQueryPort;
import com.auth.service.auth.domain.session.UserOnlineBrief;
import com.auth.service.auth.infrastructure.persistence.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link UserBriefQueryPort} 实现：委托共用 {@link UserMapper} 查询在线列表所需用户快照
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class UserBriefQueryAdapter implements UserBriefQueryPort {

	private final UserMapper userMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Long, UserOnlineBrief> findByIds(Collection<Long> userIds) {
		// 若用户 ID 集合为空则返回空 Map
		if (CollUtil.isEmpty(userIds)) {
			return Collections.emptyMap();
		}

		// 过滤掉空 ID 并去重
		List<Long> distinctIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(distinctIds)) {
			return Collections.emptyMap();
		}

		// 构建查询条件
		QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
		wrapper.in("id", distinctIds).select("id", "username", "nickname");
		return userMapper.selectList(wrapper)
			.stream()
			.collect(Collectors.toMap(UserEntity::getId,
					entity -> new UserOnlineBrief(entity.getId(), entity.getUsername(), entity.getNickname()),
					(left, right) -> left));
	}

}
