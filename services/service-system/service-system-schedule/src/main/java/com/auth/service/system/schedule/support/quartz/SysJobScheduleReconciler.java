package com.auth.service.system.schedule.support.quartz;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import com.auth.service.system.schedule.config.ScheduleJobProperties;
import com.auth.service.system.schedule.mapper.SysJobMapper;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 按 {@code sys_job} 补偿 Quartz 运行态（DB 为单一事实来源）
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class SysJobScheduleReconciler {

	private final SysJobMapper sysJobMapper;

	private final SysJobSchedulerManager sysJobSchedulerManager;

	private final ScheduleJobProperties scheduleJobProperties;

	/**
	 * 全量对账：启用与暂停任务分别分页同步
	 */
	public void reconcileAll() {
		int batchSize = scheduleJobProperties.getBootstrapBatchSize();
		reconcileByStatus(true, batchSize);
		reconcileByStatus(false, batchSize);
	}

	/**
	 * 按任务主键对账
	 * @param jobIds 任务 ID 列表
	 */
	public void reconcileByJobIds(List<Long> jobIds) {
		if (CollUtil.isEmpty(jobIds)) {
			return;
		}
		List<SysJobEntity> jobs = sysJobMapper.selectBatchIds(jobIds);
		for (SysJobEntity job : jobs) {
			reconcileSingle(job);
		}
	}

	/**
	 * 按分组编码对账
	 * @param groupCode 分组编码
	 */
	public void reconcileByGroupCode(String groupCode) {
		List<SysJobEntity> jobs = sysJobMapper
			.selectList(Wrappers.<SysJobEntity>lambdaQuery().eq(SysJobEntity::getJobGroup, groupCode));
		for (SysJobEntity job : jobs) {
			reconcileSingle(job);
		}
	}

	/**
	 * 按状态对账
	 * @param enabled 是否启用
	 * @param batchSize 批量大小
	 */
	private void reconcileByStatus(boolean enabled, int batchSize) {
		var query = Wrappers.lambdaQuery(SysJobEntity.class)
			.eq(SysJobEntity::getStatus, enabled)
			.orderByAsc(SysJobEntity::getId);
		IPage<SysJobEntity> firstPage = sysJobMapper.selectPage(new Page<>(1, batchSize), query);
		long totalPages = firstPage.getPages();
		for (long pageNum = 1; pageNum <= totalPages; pageNum++) {
			IPage<SysJobEntity> result = pageNum == 1 ? firstPage
					: sysJobMapper.selectPage(new Page<>(pageNum, batchSize), query);
			for (SysJobEntity job : result.getRecords()) {
				reconcileSingle(job);
			}
		}
	}

	/**
	 * 单个对账
	 * @param job 任务实体
	 */
	private void reconcileSingle(SysJobEntity job) {
		try {
			if (BooleanUtil.isTrue(job.getStatus())) {
				sysJobSchedulerManager.scheduleOrReplace(job);
				return;
			}
			sysJobSchedulerManager.pauseJob(job.getJobName(), job.getJobGroup());
		}
		catch (SchedulerException exception) {
			log.error("Quartz reconcile failed status={} {} / {}", job.getStatus(), job.getJobGroup(), job.getJobName(),
					exception);
		}
	}

}
