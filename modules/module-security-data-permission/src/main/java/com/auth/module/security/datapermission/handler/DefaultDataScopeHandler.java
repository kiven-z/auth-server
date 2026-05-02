package com.auth.module.security.datapermission.handler;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.authorization.ScopeGrantType;
import com.auth.module.security.datapermission.annotation.DataScope;

import java.util.List;
import java.util.regex.Pattern;

import static com.auth.module.security.contract.constants.PermissionConstant.*;

/**
 * 默认处理器用于部门/自身过滤
 *
 * @author Bunny
 */
public class DefaultDataScopeHandler implements DataScopeHandler {

	private static final Pattern SAFE_SQL_IDENT = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

	/**
	 * 是否是管理员角色或通配符权限
	 * @param roles 角色列表
	 * @param permissions 权限列表
	 * @return 是否是管理员角色或通配符权限
	 */
	private static boolean isAdminRoleOrWildcard(List<String> roles, List<String> permissions) {
		if (isAdminRole(roles)) {
			return true;
		}

		return isAdminPermission(permissions);
	}

	/**
	 * 安全别名前缀
	 * @param alias 别名
	 * @return 安全别名前缀
	 */
	private static String safeAliasPrefix(String alias) {
		if (CharSequenceUtil.isBlank(alias)) {
			return "";
		}
		String trimmed = CharSequenceUtil.trim(alias);
		requireSafeIdent(trimmed);
		return trimmed + ".";
	}

	/**
	 * 要求安全标识符
	 * @param ident 标识符
	 * @return 安全标识符
	 */
	private static String requireSafeIdent(String ident) {
		String trimmed = CharSequenceUtil.trimToEmpty(ident);
		if (!SAFE_SQL_IDENT.matcher(trimmed).matches()) {
			throw new IllegalArgumentException("Unsafe SQL identifier: " + ident);
		}
		return trimmed;
	}

	/**
	 * 构建当前用户的 SQL 条件
	 * @param profile 当前用户快照
	 * @param dataScope 注解元数据
	 * @return SQL 条件（不包含 WHERE）或 null 当不需要过滤时
	 */
	@Override
	public String buildCondition(AuthProfile profile, DataScope dataScope) {
		if (profile == null || dataScope == null) {
			return null;
		}

		// 超级用户 bypass
		Long userId = profile.getUserId();
		if (isSuperAdmin(userId)) {
			return null;
		}

		// 管理员角色或通配权限 bypass
		if (isAdminRoleOrWildcard(profile.getRoles(), profile.getPermissions())) {
			return null;
		}

		ScopeGrant dept = profile.getDataScopes() != null ? profile.getDataScopes().get("dept") : null;
		if (dept == null || dept.getScopeType() == null || dept.getScopeType() == ScopeGrantType.ALL) {
			return null;
		}

		String aliasPrefix = safeAliasPrefix(dataScope.alias());
		String userColumn = requireSafeIdent(dataScope.userColumn());
		String deptColumn = requireSafeIdent(dataScope.deptColumn());

		if (dept.getScopeType() == ScopeGrantType.SELF) {
			return aliasPrefix + userColumn + " = " + userId;
		}

		if (dept.getScopeType() == ScopeGrantType.NODE_ONLY || dept.getScopeType() == ScopeGrantType.NODE_AND_DESC
				|| dept.getScopeType() == ScopeGrantType.CUSTOM) {
			List<String> deptIds = dept.getValues();
			if (deptIds == null || deptIds.isEmpty()) {
				// fail-closed: 没有部门 ID -> 返回不可能的条件
				return "1 = 0";
			}
			String in = deptIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
			return aliasPrefix + deptColumn + " IN (" + in + ")";
		}

		// 其他策略由用户提供处理器处理
		return null;
	}

}
