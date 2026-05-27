package com.auth.service.system.authorization.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * system 侧授权失效投递配置
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.system.authorization-invalidation")
@Configuration
@Validated
public class AuthorizationInvalidationProperties {

	/**
	 * 事务提交后是否同步 Feign 调用 auth（过渡方案；失败由 Outbox Job 补偿）。
	 */
	@NotNull
	private Boolean syncDispatchEnabled = true;

	/**
	 * 补偿 Job 扫描间隔（毫秒）。
	 */
	@Min(5000)
	@NotNull
	private Long pollIntervalMs = 30_000L;

	/**
	 * 每批抢占 Outbox 条数上限。
	 */
	@Min(1)
	@NotNull
	private Integer pollBatchSize = 50;

	/**
	 * 失败后重试间隔基数（秒），实际为 base * 2^retryCount。
	 */
	@Min(1)
	@NotNull
	private Integer retryBackoffSeconds = 30;

}
