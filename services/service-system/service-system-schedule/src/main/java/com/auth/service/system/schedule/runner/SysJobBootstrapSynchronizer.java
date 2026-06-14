package com.auth.service.system.schedule.runner;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.auth.service.system.schedule.support.quartz.SysJobScheduleReconciler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 应用就绪后异步将 {@code sys_job} 分页同步至 Quartz（业务表为单一事实来源）
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class SysJobBootstrapSynchronizer {

	private final SysJobScheduleReconciler sysJobScheduleReconciler;

	/**
	 * 应用就绪后触发异步同步，避免阻塞 Spring Boot 启动完成
	 * @param event 应用就绪事件
	 */
	@EventListener(ApplicationReadyEvent.class)
	@Async
	public void onApplicationReady(ApplicationReadyEvent event) {
		log.info("Application ready, scheduling async Quartz sync from sys_job");
		sysJobScheduleReconciler.reconcileAll();
		log.info("定时任务已从数据库同步至 Quartz");
	}

}
