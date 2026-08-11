package com.auth.service.system.file.config;

import com.auth.module.file.export.executor.AsyncPagedExportExecutor;
import com.auth.module.file.export.writer.ExportWriterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件异步导出执行器装配
 *
 * @author Bunny
 */
@Configuration
public class FileExportExecutionConfiguration {

	/**
	 * 导出写入器工厂
	 * @return 写入器工厂
	 */
	@Bean
	public ExportWriterFactory exportWriterFactory() {
		return new ExportWriterFactory();
	}

	/**
	 * 异步分页导出执行器
	 * @param exportWriterFactory 写入器工厂
	 * @return 执行器
	 */
	@Bean
	public AsyncPagedExportExecutor asyncPagedExportExecutor(ExportWriterFactory exportWriterFactory) {
		return new AsyncPagedExportExecutor(exportWriterFactory);
	}

}
