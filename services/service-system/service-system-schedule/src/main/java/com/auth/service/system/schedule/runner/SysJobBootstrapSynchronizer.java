package com.auth.service.system.schedule.runner;

import com.auth.service.system.schedule.config.ScheduleJobProperties;
import com.auth.service.system.schedule.mapper.SysJobMapper;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.support.SysJobSchedulerManager;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 应用启动后将 {@code sys_job} 分页同步至 Quartz（业务表为单一事实来源）
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
	 * 异步分页同步：启用任务注册/重建，停用任务暂停 Quartz
	 */
	@Async
	public void syncFromDatabase() {
		int batchSize = scheduleJobProperties.getBootstrapBatchSize();
		syncByStatus(true, batchSize);
		syncByStatus(false, batchSize);
		log.info("定时任务已从数据库同步至 Quartz");
	}

	private void syncByStatus(boolean enabled, int batchSize) {
		long pageNum = 1;
		while (true) {
			Page<SysJobEntity> page = new Page<>(pageNum, batchSize);
			IPage<SysJobEntity> result = sysJobMapper.selectPage(page,
					Wrappers.lambdaQuery(SysJobEntity.class)
						.eq(SysJobEntity::getStatus, enabled)
						.orderByAsc(SysJobEntity::getId));
			if (result.getRecords().isEmpty()) {
				break;
			}
			for (SysJobEntity job : result.getRecords()) {
				syncSingleJob(job, enabled);
			}
			if (pageNum >= result.getPages()) {
				break;
			}
			pageNum++;
		}
	}

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
