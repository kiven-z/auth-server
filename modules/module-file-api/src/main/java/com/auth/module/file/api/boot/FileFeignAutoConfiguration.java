package com.auth.module.file.api.boot;

import com.auth.module.file.api.feign.SystemFileExportTaskFeignClient;
import com.auth.module.file.api.feign.SystemFileRecordFeignClient;
import com.auth.module.file.api.port.FileExportTaskOperations;
import com.auth.module.file.api.port.remote.RemoteFileExportTaskOperations;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

/**
 * 文件服务 Feign 客户端与跨服务 Port 装配
 *
 * @author Bunny
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.cloud.openfeign.FeignClient")
@EnableFeignClients(clients = { SystemFileRecordFeignClient.class, SystemFileExportTaskFeignClient.class })
public class FileFeignAutoConfiguration {

	/**
	 * 无进程内 Local 实现时，注册 Remote Port（微服务场景）
	 * @param feignClient 异步导出任务 Feign 客户端
	 * @return 跨服务导出任务 Port
	 */
	@ConditionalOnMissingBean(FileExportTaskOperations.class)
	@Bean
	public FileExportTaskOperations remoteFileExportTaskOperations(SystemFileExportTaskFeignClient feignClient) {
		return new RemoteFileExportTaskOperations(feignClient);
	}

}
