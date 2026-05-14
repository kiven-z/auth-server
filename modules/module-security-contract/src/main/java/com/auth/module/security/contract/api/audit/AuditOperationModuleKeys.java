package com.auth.module.security.contract.api.audit;

import java.util.Objects;

/**
 * 将 {@link AuditServiceDomain} 与小模块编码组合为写入 {@code log_operation.module} 的稳定键。
 * <p>
 * 小模块编码由各微服务自行定义常量，平台仅负责拼接规则。
 * </p>
 *
 * @author Bunny
 */
public final class AuditOperationModuleKeys {

	/**
	 * 复合键分隔符（大模块:小模块）
	 */
	public static final char SEPARATOR = ':';

	private AuditOperationModuleKeys() {
	}

	/**
	 * 生成持久化用模块键，形如 {@code SYSTEM:SYS_DEPT}。
	 * @param domain 大模块（微服务域）
	 * @param bizModuleCode 小模块编码，非空白（由各服务常量类提供）
	 * @return 非空复合键
	 */
	public static String toPersistedModuleKey(AuditServiceDomain domain, String bizModuleCode) {
		Objects.requireNonNull(domain, "domain");
		Objects.requireNonNull(bizModuleCode, "bizModuleCode");
		String trimmed = bizModuleCode.trim();
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException("bizModuleCode must not be blank");
		}
		return domain.name() + SEPARATOR + trimmed;
	}

}
