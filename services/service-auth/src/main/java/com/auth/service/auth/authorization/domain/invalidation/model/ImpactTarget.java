package com.auth.service.auth.authorization.domain.invalidation.model;

import com.auth.service.auth.authorization.domain.enums.AuthorizationChangeKind;

/**
 * 授权失效的业务键（密封类型）：不同 {@link AuthorizationChangeKind} 使用不同子类型，避免万能 ID 列表。
 *
 * @author Bunny
 */
public sealed interface ImpactTarget permits RoleCodes {

	/**
	 * 本目标对应的变更维度。
	 * @return 变更维度
	 */
	AuthorizationChangeKind kind();

}
