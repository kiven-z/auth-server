package com.auth.service.system.schedule.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 应用就绪后异步将 {@code sys_job} 分页同步到 Quartz（JDBC 集群以库为准）
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class SysJobBootstrapRunner {

	private final SysJobBootstrapSynchronizer sysJobBootstrapSynchronizer;

	/**
	 * 应用就绪后触发异步同步，避免阻塞 Spring Boot 启动完成
	 * @param event 应用就绪事件
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady(ApplicationReadyEvent event) {
		log.info("Application ready, scheduling async Quartz sync from sys_job");
		sysJobBootstrapSynchronizer.syncFromDatabase();
	}

}
