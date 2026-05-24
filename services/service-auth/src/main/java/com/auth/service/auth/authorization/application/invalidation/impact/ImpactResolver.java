package com.auth.service.auth.authorization.application.invalidation.impact;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.service.auth.authorization.domain.invalidation.model.ImpactTarget;

import java.util.Set;

/**
 * 影响面解析策略：仅负责将类型化业务键解析为受影响用户 ID。
 *
 * @param <T> 支持的 {@link ImpactTarget} 子类型
 * @author Bunny
 */
public interface ImpactResolver<T extends ImpactTarget> {

	/**
	 * 本解析器处理的变更维度。
	 * @return 变更维度
	 */
	AuthorizationChangeKind kind();

	/**
	 * 支持的目标类型（用于注册表分发时的类型检查）。
	 * @return 目标 Class
	 */
	Class<T> targetType();

	/**
	 * 解析受影响用户 ID。
	 * @param target 类型化失效目标
	 * @return 去重用户 ID
	 */
	Set<Long> resolve(T target);

}
