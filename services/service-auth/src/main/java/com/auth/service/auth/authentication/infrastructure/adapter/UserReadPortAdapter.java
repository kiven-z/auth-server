package com.auth.service.auth.authentication.infrastructure.adapter;

import com.auth.service.auth.authentication.infrastructure.persistence.UserMapper;
import com.auth.service.auth.authorization.domain.model.UserAccountSnapshot;
import com.auth.service.auth.authorization.domain.port.UserReadPort;
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
