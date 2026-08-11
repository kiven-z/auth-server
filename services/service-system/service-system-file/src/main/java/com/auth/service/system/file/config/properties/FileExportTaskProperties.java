package com.auth.service.system.file.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * 文件异步导出任务配置
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.file.export-task")
@Configuration
@Validated
public class FileExportTaskProperties {

	/**
	 * 单轮最多领取任务数
	 */
	@Min(1)
	@NotNull
	private Integer claimBatchSize = 5;

	/**
	 * 租约时长（秒）
	 */
	@Min(10)
	@NotNull
	private Integer leaseSeconds = 60;

	/**
	 * 每用户进行中任务上限（PENDING + RUNNING）
	 */
	@Min(1)
	@NotNull
	private Integer maxInProgressPerUser = 3;

	/**
	 * 单次导出最大行数
	 */
	@Min(1)
	@NotNull
	private Integer maxRows = 100000;

	/**
	 * 导出产物保留天数
	 */
	@Min(1)
	@NotNull
	private Integer artifactExpireDays = 7;

	/**
	 * 自动重试基础延迟（秒）
	 */
	@Min(1)
	@NotNull
	private Integer retryBaseDelaySeconds = 30;

	/**
	 * 导出产物过期清理最大轮次
	 */
	@Min(1)
	@NotNull
	private Integer cleanupMaxRounds = 20;

}
