package com.auth.module.security.contract.dto.invalidation;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 按用户 ID 直接失效的业务键（用户级数据范围覆盖等场景）。
 *
 * @param userIds 用户 ID 列表，非空
 * @author Bunny
 */
public record UserScopeInvalidatePayload(List<Long> userIds) implements AuthorizationInvalidatePayload, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	public UserScopeInvalidatePayload {
		userIds = InvalidationPayloadSupport.copyNonEmpty(userIds, "userIds");
	}

	@Override
	public AuthorizationChangeKind kind() {
		return AuthorizationChangeKind.USER_SCOPE;
	}

}
