package com.auth.service.auth.identity.infrastructure.persistence;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.authentication.infrastructure.persistence.UserMapper;
import com.auth.service.auth.identity.application.UserReadGateway;
import org.springframework.stereotype.Service;

/**
 * 用户只读网关实现：委托认证 BC 的 {@link UserMapper} 访问用户表。
 *
 * @author Bunny
 */
@Service
public class UserReadGatewayImpl implements UserReadGateway {

	private final UserMapper userMapper;

	public UserReadGatewayImpl(UserMapper userMapper) {
		this.userMapper = userMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserEntity findById(Long userId) {
		if (userId == null) {
			return null;
		}
		return userMapper.selectById(userId);
	}

}
