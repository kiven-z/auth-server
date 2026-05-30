package com.auth.service.system.schedule.runner;

import com.auth.service.system.schedule.service.SysJobOperationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动后将 {@code sys_job} 全量同步到 Quartz（JDBC 集群以库为准）
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class SysJobBootstrapRunner implements ApplicationRunner {

	/**
	 * 系统任务服务
	 */
	private final SysJobOperationService sysJobOperationService;

	/**
	 * 应用启动后将 {@code sys_job} 全量同步到 Quartz（JDBC 集群以库为准）
	 * @param args ApplicationArguments
	 */
	@Override
	public void run(ApplicationArguments args) {
		try {
			sysJobOperationService.syncAllFromDatabase();
			log.info("定时任务已从数据库同步至 Quartz");
		}
		catch (Exception e) {
			log.error("定时任务启动同步失败", e);
		}
	}

}
