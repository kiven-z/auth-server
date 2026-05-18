package com.auth.service.system.schedule.service.impl;

import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.schedule.mapper.SysJobMapper;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.form.SysJobStatusForm;
import com.auth.service.system.schedule.service.SysJobOperationService;
import com.auth.service.system.schedule.support.SysJobSchedulerManager;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.auth.service.system.common.exception.code.common.SystemCommonResultCode.DATA_NOT_EXIST;
import static com.auth.service.system.common.exception.code.schedule.SystemScheduleResultCode.JOB_QUARTZ_RUN_ONCE_FAILED;

/**
 * 定时任务运行态操作服务实现
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysJobOperationServiceImpl extends ServiceImpl<SysJobMapper, SysJobEntity>
		implements SysJobOperationService {

	private final SysJobSchedulerManager quartzAdmin;

	public SysJobOperationServiceImpl(SysJobSchedulerManager quartzAdmin) {
		this.quartzAdmin = quartzAdmin;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void syncAllFromDatabase() {
		List<SysJobEntity> allJobs = list();
		for (SysJobEntity job : allJobs) {
			try {
				quartzAdmin.scheduleOrReplace(job);
			}
			catch (Exception e) {
				log.error("启动同步定时任务失败: {} / {}", job.getJobGroup(), job.getJobName(), e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void batchUpdateStatus(SysJobStatusForm form) {
		for (Long id : form.getIds()) {
			SysJobEntity dbJob = getById(id);
			if (dbJob == null) {
				continue;
			}
			dbJob.setStatus(form.getStatus());
			updateById(dbJob);
			try {
				if (Boolean.TRUE.equals(form.getStatus())) {
					quartzAdmin.resumeJob(dbJob.getJobName(), dbJob.getJobGroup());
				}
				else {
					quartzAdmin.pauseJob(dbJob.getJobName(), dbJob.getJobGroup());
				}
			}
			catch (SchedulerException ex) {
				log.warn("批量更新任务状态同步 Quartz 失败 id={} : {}", id, ex.getMessage());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void runOnce(Long id) {
		SysJobEntity dbJob = getById(id);
		if (dbJob == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		try {
			quartzAdmin.ensureJobDetail(dbJob);
			quartzAdmin.triggerJob(dbJob.getJobName(), dbJob.getJobGroup());
		}
		catch (SchedulerException ex) {
			log.error("立即执行失败", ex);
			throw new SystemBusinessException(JOB_QUARTZ_RUN_ONCE_FAILED, ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pauseAllInGroup(String groupCode) {
		List<SysJobEntity> jobs = list(
				Wrappers.lambdaQuery(SysJobEntity.class).eq(SysJobEntity::getJobGroup, groupCode));
		for (SysJobEntity job : jobs) {
			job.setStatus(false);
			updateById(job);
			try {
				quartzAdmin.pauseJob(job.getJobName(), job.getJobGroup());
			}
			catch (SchedulerException ex) {
				log.warn("分组暂停失败 {} : {}", job.getJobName(), ex.getMessage());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void resumeAllInGroup(String groupCode) {
		List<SysJobEntity> jobs = list(
				Wrappers.lambdaQuery(SysJobEntity.class).eq(SysJobEntity::getJobGroup, groupCode));
		List<Long> ids = jobs.stream().map(SysJobEntity::getId).toList();
		if (!ids.isEmpty()) {
			lambdaUpdate().set(SysJobEntity::getStatus, true).in(SysJobEntity::getId, ids).update();
		}
		try {
			quartzAdmin.resumeGroup(groupCode);
		}
		catch (SchedulerException ex) {
			log.warn("分组恢复失败 groupCode={} : {}", groupCode, ex.getMessage());
		}
	}

}
