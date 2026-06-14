package com.auth.service.auth.session.infrastructure.persistence;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.authentication.infrastructure.persistence.UserMapper;
import com.auth.service.auth.session.domain.model.UserOnlineBrief;
import com.auth.service.auth.session.domain.port.OnlineSessionUserLookupPort;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * {@link OnlineSessionUserLookupPort} 实现：委托认证侧 {@link UserMapper} 查询用户展示信息
 *
 * @author Bunny
 */
@Component
public class OnlineSessionUserLookupAdapter implements OnlineSessionUserLookupPort {

	private final UserMapper userMapper;

	public OnlineSessionUserLookupAdapter(UserMapper userMapper) {
		this.userMapper = userMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Long> findUserIdsByUsernameLike(String username) {
		if (CharSequenceUtil.isBlank(username)) {
			return List.of();
		}
		return userMapper.selectIdsByUsernameLike(username.trim());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Long, UserOnlineBrief> findBriefByIds(Collection<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return Collections.emptyMap();
		}
		List<Long> distinctIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(distinctIds)) {
			return Collections.emptyMap();
		}
		LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
		wrapper.in(UserEntity::getId, distinctIds)
			.select(UserEntity::getId, UserEntity::getUsername, UserEntity::getNickname);
		return userMapper.selectList(wrapper)
			.stream()
			.filter(entity -> entity.getId() != null)
			.collect(Collectors.toMap(UserEntity::getId, this::toBrief, (left, right) -> left));
	}

	/**
	 * 用户实体转在线展示快照
	 * @param entity 用户实体（仅 id/username/nickname 列）
	 * @return 用户展示快照
	 */
	private UserOnlineBrief toBrief(UserEntity entity) {
		return new UserOnlineBrief(entity.getId(), entity.getUsername(), entity.getNickname());
	}

}
