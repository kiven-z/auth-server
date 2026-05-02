package com.auth.module.security.datapermission.policy;

import com.auth.module.security.contract.api.authorization.AuthProfile;

import java.util.Comparator;
import java.util.List;

/**
 * 选择处理器并根据策略优先级和条件逻辑合并条件
 *
 * <p>
 * TODO: 实现 AND/OR 合并语义，强制行为和参数绑定
 * </p>
 *
 * @author Bunny
 */
public class PolicyHandlerRegistry {

	/**
	 * 处理器列表
	 */
	private final List<PolicyDataScopeHandler> handlers;

	/**
	 * 构造函数
	 * @param handlers 处理器列表
	 */
	public PolicyHandlerRegistry(List<PolicyDataScopeHandler> handlers) {
		this.handlers = handlers != null ? List.copyOf(handlers) : List.of();
	}

	/**
	 * 构建合并条件
	 * @param profile 登录用户
	 * @param policies 策略列表
	 * @return 合并条件
	 */
	public String buildMergedCondition(AuthProfile profile, List<DataPolicy> policies) {
		if (policies == null || policies.isEmpty()) {
			return null;
		}

		// MVP: 使用 AND 按优先级应用支持的策略
		return policies.stream()
			.sorted(Comparator.comparingInt(DataPolicy::getPriority).reversed())
			.map(p -> buildOne(profile, p))
			.filter(s -> s != null && !s.isBlank())
			.reduce((a, b) -> "(" + a + ") AND (" + b + ")")
			.orElse(null);
	}

	/**
	 * 构建一个条件
	 * @param profile 登录用户
	 * @param policy 策略
	 * @return 条件
	 */
	private String buildOne(AuthProfile profile, DataPolicy policy) {
		for (PolicyDataScopeHandler h : handlers) {
			if (h != null && h.supports(policy)) {
				return h.buildCondition(profile, policy);
			}
		}
		return null;
	}

}
