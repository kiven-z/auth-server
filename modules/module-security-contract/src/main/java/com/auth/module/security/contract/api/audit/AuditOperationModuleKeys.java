package com.auth.module.security.contract.api.audit;

/**
 * 将 {@link AuditServiceDomain} 与 {@link AuditBizModule} 组合为写入
 * {@code log_operation.module} 的稳定键。
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
	 * @param biz 小模块（业务菜单/资源，与 {@link AuditBizModule} 枚举名一致）
	 * @return 非空复合键
	 */
	public static String toPersistedModuleKey(AuditServiceDomain domain, AuditBizModule biz) {
		return domain.name() + SEPARATOR + biz.name();
	}

}
