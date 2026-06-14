package com.auth.service.system.schedule.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.mapper.SysJobMapper;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.form.SysJobStatusForm;
import com.auth.service.system.schedule.service.SysJobScheduleSyncService;
import com.auth.service.system.schedule.support.quartz.SysJobScheduleReconciler;
import com.auth.service.system.schedule.support.quartz.SysJobSchedulerManager;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_QUARTZ_OPERATION_FAILED;

/**
 * 定时任务运行态同步实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SysJobScheduleSyncServiceImpl extends ServiceImpl<SysJobMapper, SysJobEntity>
		implements SysJobScheduleSyncService {

	private final SysJobSchedulerManager sysJobSchedulerManager;

	private final SysJobScheduleReconciler sysJobScheduleReconciler;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void batchUpdateStatus(SysJobStatusForm form) {
		List<Long> ids = form.getIds();
		if (CollUtil.isEmpty(ids)) {
			return;
		}
		Boolean targetStatus = form.getStatus();

		List<SysJobEntity> jobsToSchedule = BooleanUtil.isTrue(targetStatus) ? list(
				Wrappers.<SysJobEntity>lambdaQuery().in(SysJobEntity::getId, ids).eq(SysJobEntity::getStatus, false))
				: List.of();

		List<SysJobEntity> updates = ids.stream().map(id -> {
			SysJobEntity entity = new SysJobEntity();
			entity.setId(id);
			entity.setStatus(targetStatus);
			return entity;
		}).toList();
		updateBatchById(updates);

		try {
			if (BooleanUtil.isFalse(targetStatus)) {
				List<SysJobEntity> jobs = list(Wrappers.<SysJobEntity>lambdaQuery().in(SysJobEntity::getId, ids));
				if (CollUtil.isEmpty(jobs)) {
					return;
				}
				for (SysJobEntity job : jobs) {
					sysJobSchedulerManager.pauseJob(job.getJobName(), job.getJobGroup());
				}
				return;
			}
			if (CollUtil.isEmpty(jobsToSchedule)) {
				return;
			}
			for (SysJobEntity job : jobsToSchedule) {
				job.setStatus(true);
				sysJobSchedulerManager.scheduleOrReplace(job);
			}
		}
		catch (SchedulerException ex) {
			log.error("批量更新任务状态同步 Quartz 失败", ex);
			registerReconcileAfterRollback(() -> sysJobScheduleReconciler.reconcileByJobIds(ids));
			throw new SysJobException(JOB_QUARTZ_OPERATION_FAILED, "batchStatus", ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void batchUpdateStatusByGroupCode(String groupCode, Boolean status) {
		List<SysJobEntity> jobs = list(Wrappers.<SysJobEntity>lambdaQuery().eq(SysJobEntity::getJobGroup, groupCode));

		if (CollUtil.isNotEmpty(jobs)) {
			List<SysJobEntity> updates = jobs.stream().map(job -> {
				SysJobEntity entity = new SysJobEntity();
				entity.setId(job.getId());
				entity.setStatus(status);
				return entity;
			}).toList();
			updateBatchById(updates);
		}

		try {
			if (BooleanUtil.isFalse(status)) {
				sysJobSchedulerManager.pauseGroup(groupCode);
				return;
			}
			for (SysJobEntity job : jobs) {
				job.setStatus(true);
				sysJobSchedulerManager.scheduleOrReplace(job);
			}
		}
		catch (SchedulerException ex) {
			log.error("分组批量更新任务状态同步 Quartz 失败 groupCode={}", groupCode, ex);
			registerReconcileAfterRollback(() -> sysJobScheduleReconciler.reconcileByGroupCode(groupCode));
			throw new SysJobException(JOB_QUARTZ_OPERATION_FAILED, "batchGroupStatus", ex.getMessage());
		}
	}

	/**
	 * 注册回滚后执行
	 * @param action 执行动作
	 */
	private void registerReconcileAfterRollback(Runnable action) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			action.run();
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(int status) {
				if (status == STATUS_ROLLED_BACK) {
					action.run();
				}
			}
		});
	}

}
