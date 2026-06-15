package com.auth.service.auth.infrastructure.persistence.adapter;

import com.auth.service.auth.application.port.authorization.AuthProfileGrantQueryPort;
import com.auth.service.auth.infrastructure.persistence.UserAuthorizationGrantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * {@link AuthProfileGrantQueryPort} 适配器：委托 {@link UserAuthorizationGrantMapper}
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Repository
public class AuthProfileGrantQueryAdapter implements AuthProfileGrantQueryPort {

	private final UserAuthorizationGrantMapper userAuthorizationGrantMapper;

	@Override
	public List<String> selectRolesByUserId(long userId) {
		return userAuthorizationGrantMapper.selectRolesByUserId(userId);
	}

	@Override
	public List<String> selectPermissionsByUserId(long userId) {
		return userAuthorizationGrantMapper.selectPermissionsByUserId(userId);
	}

}
