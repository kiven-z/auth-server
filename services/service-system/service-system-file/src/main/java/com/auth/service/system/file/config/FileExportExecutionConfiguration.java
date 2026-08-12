package com.auth.service.system.file.config;

import com.auth.module.file.export.executor.AsyncPagedExportExecutor;
import com.auth.module.file.export.shard.ExportArtifactPackager;
import com.auth.module.file.export.writer.ChunkFileSessionFactoryResolver;
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
	 * 按导出格式解析单文件会话工厂
	 * @return 会话工厂解析器
	 */
	@Bean
	public ChunkFileSessionFactoryResolver chunkFileSessionFactoryResolver() {
		return new ChunkFileSessionFactoryResolver();
	}

	/**
	 * 导出产物打包器（单分片透传，多分片打 zip）
	 * @return 打包器
	 */
	@Bean
	public ExportArtifactPackager exportArtifactPackager() {
		return new ExportArtifactPackager();
	}

	/**
	 * 异步分页导出执行器
	 * @param resolver 会话工厂解析器
	 * @param packager 产物打包器
	 * @return 执行器
	 */
	@Bean
	public AsyncPagedExportExecutor asyncPagedExportExecutor(ChunkFileSessionFactoryResolver resolver,
			ExportArtifactPackager packager) {
		return new AsyncPagedExportExecutor(resolver, packager);
	}

}
