package com.auth.module.security.autoconfigure.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.List;

/**
 * 安全配置属性（路径匹配与审计策略） 说明：此处的“路径”通常用于 Spring Security 的 request matcher（如 Ant 风格路径），
 * 具体匹配规则与优先级应与安全配置实现保持一致
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.common.security")
@RefreshScope
public class SecurityConfigProperties {

	/**
	 * 总是允许的路径（匿名） 通常对应 permitAll 的请求匹配；命中该列表的请求不会触发认证
	 */
	private List<String> permitPaths = List.of();

	/**
	 * 审计策略
	 */
	private AuditPolicy auditPolicy = AuditPolicy.ALL_RECORD;

	/**
	 * 是否开启全量审计 兼容历史实现中以 {@code fullAudit} 作为开关的写法
	 */
	public boolean isFullAudit() {
		return AuditPolicy.ALL_RECORD.equals(this.auditPolicy);
	}

}
