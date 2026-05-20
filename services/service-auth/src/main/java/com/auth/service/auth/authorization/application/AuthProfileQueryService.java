package com.auth.service.auth.authorization.application;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.api.authorization.PolicyBundle;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.auth.authorization.domain.model.UserAccountSnapshot;
import com.auth.service.auth.authorization.domain.port.AuthProfileQueryPort;
import com.auth.service.auth.authorization.domain.port.UserReadPort;
import com.auth.service.auth.authorization.infrastructure.persistence.UserAuthorizationGrantMapper;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 授权画像查询服务
 *
 * @author Bunny
 */
@Service
public class AuthProfileQueryService implements AuthProfileQueryPort {

	private final UserAuthorizationGrantMapper userAuthorizationGrantMapper;

	private final UserReadPort userReadPort;

	private final DataScopeEffectiveResolver dataScopeEffectiveResolver;

	public AuthProfileQueryService(UserAuthorizationGrantMapper userAuthorizationGrantMapper, UserReadPort userReadPort,
			DataScopeEffectiveResolver dataScopeEffectiveResolver) {
		this.userAuthorizationGrantMapper = userAuthorizationGrantMapper;
		this.userReadPort = userReadPort;
		this.dataScopeEffectiveResolver = dataScopeEffectiveResolver;
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
		if (account == null || account.id() == null) {
			throw new AuthBusinessException(AuthResultCode.USER_NOT_FOUND);
		}
		long userId = account.id();

		// 获取功能权限
		List<String> roles = userAuthorizationGrantMapper.selectRolesByUserId(userId);
		List<String> permissions = userAuthorizationGrantMapper.selectPermissionsByUserId(userId);

		// 若是管理员设置管理员角色和权限
		if (PermissionConstant.isAdmin(userId, roles)) {
			roles = PermissionConstant.ADMIN_ROLES;
			permissions = PermissionConstant.ADMIN_WILDCARD_PERMISSIONS;
		}

		// 查询数据权限
		Map<String, ScopeGrant> dataScopes = dataScopeEffectiveResolver.resolveByUserId(userId);

		return AuthProfile.builder()
			.userId(account.id())
			.username(account.username())
			.roles(roles)
			.permissions(permissions)
			.dataScopes(dataScopes)
			// TODO 后面完成
			.policies(PolicyBundle.empty())
			.permVersion(account.permVersion())
			.build();
	}

}
