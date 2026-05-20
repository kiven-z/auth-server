package com.auth.service.auth.authorization.application.impl;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.api.authorization.PolicyBundle;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.auth.authentication.infrastructure.persistence.UserMapper;
import com.auth.service.auth.authorization.application.AuthProfileQueryService;
import com.auth.service.auth.authorization.application.DataScopeEffectiveResolver;
import com.auth.service.auth.authorization.infrastructure.persistence.UserAuthorizationGrantMapper;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 构建 {@link AuthProfile}：聚合角色、权限与生效数据权限。
 *
 * @author Bunny
 */
@Service
public class AuthProfileQueryServiceImpl implements AuthProfileQueryService {

	private final UserAuthorizationGrantMapper userAuthorizationGrantMapper;

	private final UserMapper userMapper;

	private final DataScopeEffectiveResolver dataScopeEffectiveResolver;

	public AuthProfileQueryServiceImpl(UserAuthorizationGrantMapper userAuthorizationGrantMapper, UserMapper userMapper,
			DataScopeEffectiveResolver dataScopeEffectiveResolver) {
		this.userAuthorizationGrantMapper = userAuthorizationGrantMapper;
		this.userMapper = userMapper;
		this.dataScopeEffectiveResolver = dataScopeEffectiveResolver;
	}

	/**
	 * 根据用户ID构建授权画像
	 * @param userId 用户ID
	 * @return 授权画像
	 */
	@Override
	public AuthProfile buildByUserId(Long userId) {
		UserEntity user = userMapper.selectById(userId);
		if (user == null) {
			throw new AuthBusinessException(AuthResultCode.USER_NOT_FOUND);
		}
		return buildByUser(user);
	}

	/**
	 * 根据用户信息构建授权画像（避免重复查询用户）
	 * @param user 用户信息
	 * @return 授权画像
	 */
	@Override
	public AuthProfile buildByUser(UserEntity user) {
		if (user == null || user.getId() == null) {
			throw new AuthBusinessException(AuthResultCode.USER_NOT_FOUND);
		}
		long userId = user.getId();

		// 获取功能权限
		List<String> roles = userAuthorizationGrantMapper.selectRolesByUserId(userId);
		List<String> permissions = userAuthorizationGrantMapper.selectPermissionsByUserId(userId);

		if (PermissionConstant.isAdmin(userId, roles)) {
			roles = PermissionConstant.ADMIN_ROLES;
			permissions = PermissionConstant.ADMIN_WILDCARD_PERMISSIONS;
		}
		Map<String, ScopeGrant> dataScopes = dataScopeEffectiveResolver.resolveByUserId(userId);

		return AuthProfile.builder()
			.userId(user.getId())
			.username(user.getUsername())
			.roles(roles)
			.permissions(permissions)
			.dataScopes(dataScopes)
			// TODO 后面完成
			.policies(PolicyBundle.empty())
			.permVersion(user.getPermVersion())
			.build();
	}

}
