package com.auth.service.auth.authorization.application;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.api.authorization.PolicyBundle;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.auth.authorization.application.datascope.DeptDataScopeResolver;
import com.auth.service.auth.authorization.domain.model.UserAccountSnapshot;
import com.auth.service.auth.authorization.domain.port.AuthProfileQueryPort;
import com.auth.service.auth.authorization.domain.port.UserReadPort;
import com.auth.service.auth.authorization.infrastructure.persistence.UserAuthorizationGrantMapper;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 授权画像查询服务
 *
 * @author Bunny
 */
@Service
public class AuthProfileQueryService implements AuthProfileQueryPort {

	private final UserAuthorizationGrantMapper userAuthorizationGrantMapper;

	private final UserReadPort userReadPort;

	private final DeptDataScopeResolver deptDataScopeResolver;

	public AuthProfileQueryService(UserAuthorizationGrantMapper userAuthorizationGrantMapper, UserReadPort userReadPort,
			DeptDataScopeResolver deptDataScopeResolver) {
		this.userAuthorizationGrantMapper = userAuthorizationGrantMapper;
		this.userReadPort = userReadPort;
		this.deptDataScopeResolver = deptDataScopeResolver;
	}

	/**
	 * 根据用户ID构建授权画像
	 * @param userId 用户ID
	 * @return 授权画像
	 */
	@Override
	public AuthProfile buildByUserId(Long userId) {
		UserAccountSnapshot account = userReadPort.findById(userId)
			.orElseThrow(() -> new AuthBusinessException(AuthResultCode.USER_NOT_FOUND));
		return buildByAccount(account);
	}

	/**
	 * 根据用户快照构建授权画像（避免重复查询用户）
	 * @param account 用户快照
	 * @return 授权画像
	 */
	public AuthProfile buildByAccount(UserAccountSnapshot account) {
		// 如果用户快照为空或用户ID为空，则抛出用户不存在异常
		if (account == null || account.id() == null) {
			throw new AuthBusinessException(AuthResultCode.USER_NOT_FOUND);
		}
		long userId = account.id();

		List<String> roles = userAuthorizationGrantMapper.selectRolesByUserId(userId);
		List<String> permissions = userAuthorizationGrantMapper.selectPermissionsByUserId(userId);
		boolean admin = PermissionConstant.isAdmin(userId, roles);
		ScopeGrant deptScope = deptDataScopeResolver.resolveEffectiveGrant(userId, roles);
		if (admin) {
			roles = PermissionConstant.ADMIN_ROLES;
			permissions = PermissionConstant.ADMIN_WILDCARD_PERMISSIONS;
			deptScope = ScopeGrant.builder().scopeType(DataScopeStorageType.ALL).values(deptScope.getValues()).build();
		}

		return AuthProfile.builder()
			.userId(account.id())
			.username(account.username())
			.roles(roles)
			.permissions(permissions)
			.deptScope(deptScope)
			// TODO 后面完成
			.policies(PolicyBundle.empty())
			.permVersion(account.permVersion())
			.build();
	}

}
