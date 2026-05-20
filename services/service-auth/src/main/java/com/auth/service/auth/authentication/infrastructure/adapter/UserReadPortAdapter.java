package com.auth.service.auth.authentication.infrastructure.adapter;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.authentication.infrastructure.persistence.UserMapper;
import com.auth.service.auth.authorization.domain.port.UserReadPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * {@link UserReadPort} 适配器：委托认证侧 {@link UserMapper} 查询用户。
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
	public Optional<UserEntity> findById(Long userId) {
		if (userId == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(userMapper.selectById(userId));
	}

}
