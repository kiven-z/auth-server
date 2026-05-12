package com.auth.service.auth.infrastructure.profile;

import com.auth.module.platform.persistence.UserEntity;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.api.authorization.PolicyBundle;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.infrastructure.profile.datascope.DataScopeEffectiveResolver;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.mapper.UserRolePermissionDataScopePolicyMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 只做 AuthProfile 构建 创建认证授权画像，授权画像构建服务实现
 *
 * @author Bunny
 */
@Service
public class AuthProfileService {

	private final UserRolePermissionDataScopePolicyMapper userRolePermissionDataScopePolicyMapper;

	private final UserMapper userMapper;

	private final DataScopeEffectiveResolver dataScopeEffectiveResolver;

	public AuthProfileService(UserRolePermissionDataScopePolicyMapper userRolePermissionDataScopePolicyMapper,
			UserMapper userMapper, DataScopeEffectiveResolver dataScopeEffectiveResolver) {
		this.userRolePermissionDataScopePolicyMapper = userRolePermissionDataScopePolicyMapper;
		this.userMapper = userMapper;
		this.dataScopeEffectiveResolver = dataScopeEffectiveResolver;
	}

	/**
	 * 根据用户ID构建授权画像
	 * @param userId 用户ID
	 * @return 授权画像
	 */
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
	public AuthProfile buildByUser(UserEntity user) {
		if (user == null || user.getId() == null) {
			throw new AuthBusinessException(AuthResultCode.USER_NOT_FOUND);
		}
		long userId = user.getId();

		// 获取功能权限
		List<String> roles = userRolePermissionDataScopePolicyMapper.selectRolesByUserId(userId);
		List<String> permissions = userRolePermissionDataScopePolicyMapper.selectPermissionsByUserId(userId);

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
