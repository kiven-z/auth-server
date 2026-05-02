package com.auth.common.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置，配置常用的线程池
 *
 * @author Bunny
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncTaskConfig {

	/**
	 * <p>
	 * 安全任务线程池，如果有需要可以使用
	 * </p>
	 * <p>
	 * 使用示例：<code>@Async("securityTaskExecutor")</code>
	 * </p>
	 */
	@Bean("asyncTaskExecutor")
	public TaskExecutor asyncTaskExecutor() {
		log.info("Registering asyncTaskExecutor");

		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		// 核心线程数：即使空闲也会保留的线程数
		executor.setCorePoolSize(5);
		// 最大线程数：线程池中允许的最大线程数
		executor.setMaxPoolSize(10);
		// 队列容量：等待执行的任务队列大小
		executor.setQueueCapacity(100);
		// 线程名前缀：便于日志追踪
		executor.setThreadNamePrefix("async-task-log-");
		// 拒绝策略：当线程池和队列都满时的处理策略
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		// 线程空闲时间：超过核心线程数的线程空闲多久后被回收
		executor.setKeepAliveSeconds(60);
		// 等待所有任务完成后关闭线程池
		executor.setWaitForTasksToCompleteOnShutdown(true);
		// 等待任务完成的最大时间
		executor.setAwaitTerminationSeconds(60);

		executor.initialize();
		return executor;
	}

}