package com.auth.module.security.contract.api.authorization;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * 轻量策略规则（type + params）
 *
 * @author Bunny
 */
@JsonDeserialize(builder = PolicyRule.PolicyRuleBuilder.class)
@Getter
@Builder
public class PolicyRule implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 规则类型：TIME_WINDOW / IP_CIDR / CLIENT_ID / ...
	 */
	private final String type;

	/**
	 * 规则参数：保持 JSON 友好（String/Number/Boolean/List/Map）
	 */
	@Builder.Default
	private final transient Map<String, Object> params = Collections.emptyMap();

}
