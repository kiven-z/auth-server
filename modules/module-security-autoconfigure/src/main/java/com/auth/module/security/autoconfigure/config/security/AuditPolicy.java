package com.auth.module.security.autoconfigure.config.security;

/**
 * 审计策略
 *
 * <p>
 * 说明：这里的“公开接口”建议与安全配置中的 {@code permitPaths} 或公开接口标记保持一致， 避免“public”的定义在不同模块中出现歧义
 * </p>
 *
 * @author Bunny
 */
public enum AuditPolicy {

	/**
	 * 无审计（关闭）
	 */
	NONE,

	/**
	 * 访问公开接口不记录
	 */
	PUBLIC_NO_RECORD,

	/**
	 * 访问公开接口只要携带Token就记录
	 */
	PUBLIC_WITH_TOKEN_RECORD,

	/**
	 * 全量审计（只要访问就记录）
	 */
	ALL_RECORD,

}
