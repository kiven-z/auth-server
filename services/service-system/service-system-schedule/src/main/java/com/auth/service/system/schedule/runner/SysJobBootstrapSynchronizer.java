package com.auth.service.system.schedule.runner;

import com.auth.service.system.schedule.support.quartz.SysJobScheduleReconciler;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 应用就绪后同步将 {@code sys_job} 对账至 Quartz，再启动调度器（业务表为单一事实来源）
 *
 * @author Bunny
 */
@Slf4j
@Component
public class SysJobBootstrapSynchronizer {

	private final SysJobScheduleReconciler sysJobScheduleReconciler;

	private final Scheduler scheduler;

	public SysJobBootstrapSynchronizer(SysJobScheduleReconciler sysJobScheduleReconciler, @Lazy Scheduler scheduler) {
		this.sysJobScheduleReconciler = sysJobScheduleReconciler;
		this.scheduler = scheduler;
	}

	/**
	 * 应用就绪后先对账再启动 Quartz，避免调度线程与删建 Trigger 并发
	 * @param event 应用就绪事件
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady(ApplicationReadyEvent event) {
		try {
			log.info("Syncing Quartz from sys_job before scheduler start");
			sysJobScheduleReconciler.reconcileAll();
			log.info("sys_job reconciled to Quartz");
		}
		finally {
			startSchedulerIfNeeded();
		}
	}

	private void startSchedulerIfNeeded() {
		try {
			if (!scheduler.isStarted()) {
				scheduler.start();
				log.info("Quartz scheduler started after sys_job bootstrap sync");
			}
		}
		catch (SchedulerException exception) {
			log.error("Failed to start Quartz scheduler after bootstrap sync", exception);
			throw new IllegalStateException("Quartz scheduler failed to start after bootstrap sync", exception);
		}
	}

}
