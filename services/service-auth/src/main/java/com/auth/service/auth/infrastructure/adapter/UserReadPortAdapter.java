package com.auth.service.auth.infrastructure.adapter;

import com.auth.service.auth.application.port.UserReadPort;
import com.auth.service.auth.domain.authorization.model.UserAccountSnapshot;
import com.auth.service.auth.infrastructure.persistence.UserMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * {@link UserReadPort} 适配器：委托认证侧 {@link UserMapper} 查询并映射为 {@link UserAccountSnapshot}。
 *
 * @author Bunny
 */
@Component
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

}
