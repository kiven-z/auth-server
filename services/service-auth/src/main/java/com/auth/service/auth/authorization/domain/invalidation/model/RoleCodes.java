package com.auth.service.auth.authorization.domain.invalidation.model;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.auth.authorization.domain.enums.AuthorizationChangeKind;

import java.util.List;

/**
 * 角色码维度失效目标（可多角色一次失效）。
 *
 * @param codes 角色编码列表，非空
 * @author Bunny
 */
public record RoleCodes(List<String> codes) implements ImpactTarget {

	/**
	 * 紧凑构造：拒绝 null 与空列表。
	 * @param codes 角色编码
	 */
	public RoleCodes {
		if (CollUtil.isEmpty(codes)) {
			throw new IllegalArgumentException("roleCodes must not be null or empty");
		}
	}

	@Override
	public AuthorizationChangeKind kind() {
		return AuthorizationChangeKind.ROLE;
	}

}
