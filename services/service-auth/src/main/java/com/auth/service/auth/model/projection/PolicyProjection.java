package com.auth.service.auth.model.projection;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 策略行数据
 *
 * @author Bunny
 */
@Getter
@Setter
public class PolicyProjection implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 策略编码
	 */
	private String policyCode;

	/**
	 * 策略类型：TIME/IP/MFA/GEO/DEVICE/CUSTOM
	 */
	private String policyType;

	/**
	 * 最终动作：DENY/AUDIT_ONLY/CHALLENGE_MFA
	 */
	private String finalAction;

	/**
	 * 条件类型：SPEL/SCRIPT/JSON_RULE
	 */
	private String conditionType;

	/**
	 * 最终条件表达式（覆盖后）
	 */
	private String finalConditionExpression;

	/**
	 * 策略参数（JSON）
	 */
	private String config;

}
