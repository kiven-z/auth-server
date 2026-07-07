package com.auth.service.system.schedule.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.annotation.Validated;

/**
 * 定时任务模块可调参数（如日志触发的熔断阈值）
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.schedule.job")
@RefreshScope
@Validated
public class ScheduleJobProperties {

	/**
	 * 连续失败次数达到该值后，自动将任务置为暂停并调用 pauseJob；0 表示关闭熔断
	 */
	@Min(0)
	private int consecutiveFailureThreshold = 5;

	/**
	 * 是否记录成功日志（默认不记录）
	 */
	private boolean successEnabled = false;

	/**
	 * 启动时从 job 同步至 Quartz 的分页大小
	 */
	@Min(10)
	private int bootstrapBatchSize = 100;

}
