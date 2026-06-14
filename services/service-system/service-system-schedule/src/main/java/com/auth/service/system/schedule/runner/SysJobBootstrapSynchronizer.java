package com.auth.service.system.schedule.runner;

import org.quartz.SchedulerException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.auth.service.system.schedule.config.ScheduleJobProperties;
import com.auth.service.system.schedule.mapper.SysJobMapper;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.support.SysJobSchedulerManager;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

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

	private final SysJobMapper sysJobMapper;

	private final SysJobSchedulerManager sysJobSchedulerManager;

	private final ScheduleJobProperties scheduleJobProperties;

	/**
	 * 应用就绪后触发异步同步，避免阻塞 Spring Boot 启动完成
	 * @param event 应用就绪事件
	 */
	@EventListener(ApplicationReadyEvent.class)
	@Async
	public void onApplicationReady(ApplicationReadyEvent event) {
		log.info("Application ready, scheduling async Quartz sync from sys_job");
		int batchSize = scheduleJobProperties.getBootstrapBatchSize();
		syncByStatus(true, batchSize);
		syncByStatus(false, batchSize);
		log.info("定时任务已从数据库同步至 Quartz");
	}

	/**
	 * 根据状态同步任务
	 * @param enabled 是否启用
	 * @param batchSize 分页大小
	 */
	private void syncByStatus(boolean enabled, int batchSize) {
		LambdaQueryWrapper<SysJobEntity> query = Wrappers.lambdaQuery(SysJobEntity.class)
			.eq(SysJobEntity::getStatus, enabled)
			.orderByAsc(SysJobEntity::getId);

		// 获取第一页
		IPage<SysJobEntity> firstPage = sysJobMapper.selectPage(new Page<>(1, batchSize), query);
		long totalPages = firstPage.getPages();

		// 遍历所有页
		for (long pageNum = 1; pageNum <= totalPages; pageNum++) {
			IPage<SysJobEntity> result = pageNum == 1 ? firstPage
					: sysJobMapper.selectPage(new Page<>(pageNum, batchSize), query);

			// 同步单个任务
			for (SysJobEntity job : result.getRecords()) {
				syncSingleJob(job, enabled);
			}
		}
	}

	/**
	 * 同步单个任务
	 * @param job 任务
	 * @param enabled 是否启用
	 */
	private void syncSingleJob(SysJobEntity job, boolean enabled) {
		try {
			if (enabled) {
				sysJobSchedulerManager.scheduleOrReplace(job);
				return;
			}
			sysJobSchedulerManager.pauseJob(job.getJobName(), job.getJobGroup());
		}
		catch (SchedulerException exception) {
			log.error("启动同步定时任务失败 enabled={} {} / {}", enabled, job.getJobGroup(), job.getJobName(), exception);
		}
	}

}
