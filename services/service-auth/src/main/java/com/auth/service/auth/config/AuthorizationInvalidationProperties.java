package com.auth.service.auth.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * 授权失效编排可调参数
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.invalidation")
@Configuration
@Validated
public class AuthorizationInvalidationProperties {

	/**
	 * 递增与画像刷新/驱逐的每批大小
	 */
	@NotNull
	private Integer batchSize = 500;

	/**
	 * 是否启用策略码
	 */
	private boolean policyRoleBridgeEnabled = false;

	/**
	 * processing 占位超时阈值（分钟），超过后由清理 Job 删除占位行
	 */
	@Min(1)
	@NotNull
	private Integer processingClaimTimeoutMinutes = 30;

	/**
	 * 是否启用 processing 占位超时清理 Job
	 */
	@NotNull
	private Boolean processingClaimCleanupEnabled = false;

	/**
	 * processing 占位清理 Job 扫描间隔（毫秒）
	 */
	@Min(60_000)
	@NotNull
	private Long processingClaimCleanupIntervalMs = 300_000L;

	/**
	 * 每批释放 processing 占位上限
	 */
	@Min(1)
	@NotNull
	private Integer processingClaimCleanupBatchSize = 100;

	/**
	 * 已完成幂等事件保留天数，超出后由清理 Job 物理删除
	 */
	@Min(1)
	@NotNull
	private Integer successRetentionDays = 90;

	/**
	 * 是否启用已完成幂等事件历史清理 Job
	 */
	@NotNull
	private Boolean successCleanupEnabled = false;

	/**
	 * 已完成幂等事件清理 Job 扫描间隔（毫秒）
	 */
	@Min(3_600_000)
	@NotNull
	private Long successCleanupIntervalMs = 86_400_000L;

	/**
	 * 每批删除已完成幂等事件上限
	 */
	@Min(1)
	@NotNull
	private Integer successCleanupBatchSize = 500;

}
