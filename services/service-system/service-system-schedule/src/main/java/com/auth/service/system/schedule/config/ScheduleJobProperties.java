package com.auth.service.system.schedule.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 定时任务模块可调参数（如日志触发的熔断阈值）
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.system.schedule.job")
@Validated
public class ScheduleJobProperties {

	/**
	 * 连续失败次数达到该值后，自动将任务置为暂停并调用 pauseJob
	 */
	private int consecutiveFailureThreshold = 5;

	/**
	 * 是否记录成功日志（默认不记录）
	 */
	private boolean successEnabled = false;

}
