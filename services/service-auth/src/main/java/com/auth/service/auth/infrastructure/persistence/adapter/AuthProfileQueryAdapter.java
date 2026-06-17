package com.auth.service.auth.infrastructure.persistence.adapter;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.api.authorization.PolicyBundle;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.auth.application.authorization.invalidation.datascope.DeptDataScopeResolver;
import com.auth.service.auth.application.port.authorization.AuthProfileQueryPort;
import com.auth.service.auth.application.port.UserReadPort;
import com.auth.service.auth.domain.authorization.model.UserAccountSnapshot;
import com.auth.service.auth.infrastructure.persistence.mapper.UserAuthorizationGrantMapper;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.AuthResultCode;

/**
 * 授权画像查询适配器：聚合用户快照、角色权限与数据范围，构建 {@link AuthProfile}。
 *
 * @author Bunny
 */
@Repository
public class AuthProfileQueryAdapter implements AuthProfileQueryPort {

	private final UserAuthorizationGrantMapper userAuthorizationGrantMapper;

	private final UserReadPort userReadPort;

	private final DeptDataScopeResolver deptDataScopeResolver;

	public AuthProfileQueryAdapter(UserAuthorizationGrantMapper userAuthorizationGrantMapper, UserReadPort userReadPort,
			DeptDataScopeResolver deptDataScopeResolver) {
		this.userAuthorizationGrantMapper = userAuthorizationGrantMapper;
		this.userReadPort = userReadPort;
		this.deptDataScopeResolver = deptDataScopeResolver;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthProfile buildByUserId(Long userId) {
		UserAccountSnapshot account = userReadPort.findById(userId)
			.orElseThrow(() -> new AuthBusinessException(AuthResultCode.USER_NOT_FOUND));
		return buildByAccount(account);
	}

	/**
	 * 根据用户快照构建授权画像（避免重复查询用户）。
	 * @param account 用户快照
	 * @return 授权画像
	 */
	public AuthProfile buildByAccount(UserAccountSnapshot account) {
		// 如果用户快照为空或用户 ID 为空，则抛出用户不存在异常
		if (account == null || account.id() == null) {
			throw new AuthBusinessException(AuthResultCode.USER_NOT_FOUND);
		}
		long userId = account.id();

		// 查询用户角色和权限
		List<String> roles = userAuthorizationGrantMapper.selectRolesByUserId(userId);
		List<String> permissions = userAuthorizationGrantMapper.selectPermissionsByUserId(userId);
		boolean admin = PermissionConstant.isAdmin(userId, roles);

		// 如果用户是管理员，则设置管理员角色和权限
		ScopeGrant deptScope;
		if (admin) {
			roles = PermissionConstant.ADMIN_ROLES;
			permissions = PermissionConstant.ADMIN_WILDCARD_PERMISSIONS;
			deptScope = ScopeGrant.builder().scopeType(DataScopeStorageType.ALL).values(List.of()).build();
		}
		else {
			deptScope = deptDataScopeResolver.resolveEffectiveGrant(userId, roles);
		}

		// 构建授权画像
		return AuthProfile.builder()
			.userId(account.id())
			.username(account.username())
			.roles(roles)
			.permissions(permissions)
			.deptScope(deptScope)
			.policies(PolicyBundle.empty())
			.permVersion(account.permVersion())
			.build();
	}

}
