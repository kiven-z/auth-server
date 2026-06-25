package com.auth.service.auth.support.authorization;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.api.authorization.PolicyBundle;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.mapper.UserAuthorizationGrantMapper;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.model.value.authorization.UserAccountSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 授权画像查询：聚合用户快照、角色权限与数据范围，构建 {@link AuthProfile}。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Repository
public class AuthProfileRepository {

	private final UserAuthorizationGrantMapper userAuthorizationGrantMapper;

	private final DeptDataScopeResolver deptDataScopeResolver;

	private final UserMapper userMapper;

	/**
	 * 根据用户 ID 构建授权画像。
	 * @param userId 用户 ID
	 * @return 授权画像
	 */
	public AuthProfile buildByUserId(Long userId) {
		UserAccountSnapshot account = Optional.ofNullable(userMapper.selectById(userId))
			.map(UserAccountSnapshot::from)
			.orElseThrow(() -> new AuthBusinessException(AuthResultCode.USER_NOT_FOUND));
		return buildByAccount(account);
	}

	/**
	 * 按用户 ID 查询登录名（轻量读，供审计等场景缓存未命中时使用）。
	 * @param userId 用户 ID
	 * @return 登录名，用户不存在或用户名为空时为空
	 */
	public Optional<String> findUsernameByUserId(Long userId) {
		if (userId == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(userMapper.selectById(userId))
			.map(UserEntity::getUsername)
			.filter(CharSequenceUtil::isNotBlank);
	}

	/**
	 * 根据用户快照构建授权画像（避免重复查询用户）。
	 * @param account 用户快照
	 * @return 授权画像
	 */
	public AuthProfile buildByAccount(UserAccountSnapshot account) {
		if (account == null || account.id() == null) {
			throw new AuthBusinessException(AuthResultCode.USER_NOT_FOUND);
		}
		long userId = account.id();

		List<String> roles = userAuthorizationGrantMapper.selectRolesByUserId(userId);
		List<String> permissions = userAuthorizationGrantMapper.selectPermissionsByUserId(userId);
		boolean admin = PermissionConstant.isAdmin(userId, roles);

		ScopeGrant deptScope;
		if (admin) {
			roles = PermissionConstant.ADMIN_ROLES;
			permissions = PermissionConstant.ADMIN_WILDCARD_PERMISSIONS;
			deptScope = ScopeGrant.builder().scopeType(DataScopeStorageType.ALL).values(List.of()).build();
		}
		else {
			deptScope = deptDataScopeResolver.resolveEffectiveGrant(userId, roles);
		}

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
