package com.auth.service.system.file.config;

import com.auth.service.system.file.config.properties.FileExportTaskProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 文件异步导出 Worker 线程池
 *
 * @author Bunny
 */
@Configuration
public class FileExportWorkerConfiguration {

	/**
	 * 导出任务执行池
	 * @param properties 导出任务配置
	 * @return 执行器
	 */
	@Bean(name = "fileExportTaskExecutor")
	public ThreadPoolTaskExecutor fileExportTaskExecutor(FileExportTaskProperties properties) {
		int concurrency = properties.getMaxConcurrency();
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(concurrency);
		executor.setMaxPoolSize(concurrency);
		executor.setQueueCapacity(0);
		executor.setThreadNamePrefix("file-export-");
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(60);
		executor.initialize();
		return executor;
	}

}
