package com.auth.module.security.datapermission.policy;

import lombok.Builder;
import lombok.Value;

import java.util.Collections;
import java.util.Map;

/**
 * User-defined data policy (structured; never store raw SQL).
 *
 * @author Bunny
 */
@Value
@Builder
public class DataPolicy {

	/**
	 * 策略代码
	 */
	String policyCode;

	/**
	 * 是否强制
	 */
	boolean mandatory;

	/**
	 * 优先级
	 */
	@Builder.Default
	int priority = 0;

	/**
	 * 参数
	 */
	@Builder.Default
	Map<String, Object> params = Collections.emptyMap();

	/**
	 * 条件逻辑
	 */
	@Builder.Default
	ConditionLogic conditionLogic = ConditionLogic.AND;

	/**
	 * 条件逻辑
	 */
	public enum ConditionLogic {

		/**
		 * 与
		 */
		AND,

		/**
		 * 或
		 */
		OR

	}

}
