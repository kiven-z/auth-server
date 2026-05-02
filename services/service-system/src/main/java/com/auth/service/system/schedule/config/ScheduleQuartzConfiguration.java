package com.auth.service.system.schedule.config;

import com.auth.service.system.schedule.listener.SysJobExecutionListener;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz 与 Spring 集成：SchedulerContext 注入容器、全局 Job 监听（日志与熔断）
 *
 * @author Bunny
 */
@EnableConfigurationProperties({ ScheduleJobProperties.class, })
@Configuration
public class ScheduleQuartzConfiguration {

	/**
	 * Quartz Scheduler 上下文中存放 Spring 容器的键名（与 Job 内读取一致）
	 */
	public static final String APPLICATION_CONTEXT_KEY = "applicationContext";

	/**
	 * 将 {@link ApplicationContext} 放入 SchedulerContext，供 Job 反射调用 Spring Bean
	 * @return 定制器
	 */
	@Bean
	public SchedulerFactoryBeanCustomizer applicationContextSchedulerContextCustomizer() {
		return factory -> factory.setApplicationContextSchedulerContextKey(APPLICATION_CONTEXT_KEY);
	}

	/**
	 * 注册全局 Job 监听：写入 {@code log_job} 与连续失败熔断
	 * @param sysJobExecutionListener 监听器 Bean
	 * @return 定制器
	 */
	@Bean
	public SchedulerFactoryBeanCustomizer globalSysJobExecutionListenerCustomizer(
			SysJobExecutionListener sysJobExecutionListener) {
		return factory -> factory.setGlobalJobListeners(sysJobExecutionListener);
	}

}
