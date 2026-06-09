package com.auth.service.system.authorization.dispatch.query;

import java.util.List;

/**
 * 用户生效角色码与权限码快照（供 admin 动态菜单等只读场景）
 *
 * @param roleCodes 角色码
 * @param permissionCodes 权限码
 * @author Bunny
 */
public record UserEffectiveCodesSnapshot(List<String> roleCodes, List<String> permissionCodes) {

	/**
	 * 规范化列表字段，避免 null 向外泄漏
	 */
	public UserEffectiveCodesSnapshot {
		roleCodes = roleCodes == null ? List.of() : List.copyOf(roleCodes);
		permissionCodes = permissionCodes == null ? List.of() : List.copyOf(permissionCodes);
	}

}
