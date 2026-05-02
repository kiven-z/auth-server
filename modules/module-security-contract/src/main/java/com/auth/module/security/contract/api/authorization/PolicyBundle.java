package com.auth.module.security.contract.api.authorization;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 轻量策略集合
 *
 * @author Bunny
 */
@JsonDeserialize(builder = PolicyBundle.PolicyBundleBuilder.class)
@Getter
@Builder
public class PolicyBundle implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Builder.Default
	private final List<PolicyRule> deny = Collections.emptyList();

	@Builder.Default
	private final List<PolicyRule> allow = Collections.emptyList();

	public static PolicyBundle empty() {
		return PolicyBundle.builder().build();
	}

}
