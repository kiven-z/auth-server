package com.auth.module.security.contract.dto.invalidation;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 按策略码失效的业务键。
 *
 * @param policyCodes 策略编码列表，非空
 * @author Bunny
 */
public record PolicyInvalidatePayload(
		List<String> policyCodes) implements AuthorizationInvalidatePayload, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	public PolicyInvalidatePayload {
		policyCodes = InvalidationPayloadSupport.copyNonEmpty(policyCodes, "policyCodes");
	}

	@Override
	public AuthorizationChangeKind kind() {
		return AuthorizationChangeKind.POLICY;
	}

}
