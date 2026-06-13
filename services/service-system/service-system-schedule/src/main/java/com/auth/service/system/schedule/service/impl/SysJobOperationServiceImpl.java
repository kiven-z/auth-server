package com.auth.service.system.schedule.service.impl;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.mapper.SysJobMapper;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.form.SysJobStatusForm;
import com.auth.service.system.schedule.service.SysJobOperationService;
import com.auth.service.system.schedule.support.SysJobSchedulerManager;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_QUARTZ_OPERATION_FAILED;

/**
 * 定时任务运行态操作服务实现
 *
 * @author Bunny
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class SysJobOperationServiceImpl implements SysJobOperationService {

	private final SysJobMapper sysJobMapper;

	private final SysJobSchedulerManager quartzAdmin;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Async
	public void syncAllFromDatabase() {
		List<SysJobEntity> allJobs = sysJobMapper.selectList(null);
		for (SysJobEntity job : allJobs) {
			try {
				quartzAdmin.scheduleOrReplace(job);
			}
			catch (Exception e) {
				log.error("启动同步定时任务失败: {} / {}", job.getJobGroup(), job.getJobName(), e);
			}
		}
		log.info("定时任务已从数据库同步至 Quartz");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void batchUpdateStatus(SysJobStatusForm form) {
		for (Long id : form.getIds()) {
			SysJobEntity dbJob = sysJobMapper.selectById(id);
			if (dbJob == null) {
				continue;
			}
			dbJob.setStatus(form.getStatus());
			sysJobMapper.updateById(dbJob);
			try {
				quartzAdmin.scheduleOrReplace(dbJob);
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
		SysJobEntity dbJob = sysJobMapper.selectById(id);
		if (dbJob == null) {
			throw new SysJobException(DATA_NOT_EXIST);
		}
		try {
			quartzAdmin.ensureJobDetail(dbJob);
			quartzAdmin.triggerJob(dbJob.getJobName(), dbJob.getJobGroup());
		}
		catch (SchedulerException ex) {
			log.error("立即执行失败", ex);
			throw new SysJobException(JOB_QUARTZ_OPERATION_FAILED, "runOnce", ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pauseAllInGroup(String groupCode) {
		List<SysJobEntity> jobs = sysJobMapper
			.selectList(Wrappers.lambdaQuery(SysJobEntity.class).eq(SysJobEntity::getJobGroup, groupCode));
		List<Long> ids = jobs.stream().map(SysJobEntity::getId).toList();
		if (!ids.isEmpty()) {
			sysJobMapper.update(null,
					Wrappers.lambdaUpdate(SysJobEntity.class)
						.set(SysJobEntity::getStatus, false)
						.set(SysJobEntity::getUpdateTime, LocalDateTime.now())
						.in(SysJobEntity::getId, ids));
		}
		try {
			quartzAdmin.pauseGroup(groupCode);
		}
		catch (SchedulerException ex) {
			log.warn("分组暂停失败 groupCode={} : {}", groupCode, ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void resumeAllInGroup(String groupCode) {
		List<SysJobEntity> jobs = sysJobMapper
			.selectList(Wrappers.lambdaQuery(SysJobEntity.class).eq(SysJobEntity::getJobGroup, groupCode));
		if (jobs.isEmpty()) {
			return;
		}
		List<Long> ids = jobs.stream().map(SysJobEntity::getId).toList();
		sysJobMapper.update(null,
				Wrappers.lambdaUpdate(SysJobEntity.class)
					.set(SysJobEntity::getStatus, true)
					.set(SysJobEntity::getUpdateTime, LocalDateTime.now())
					.in(SysJobEntity::getId, ids));
		for (SysJobEntity job : jobs) {
			job.setStatus(true);
			try {
				quartzAdmin.scheduleOrReplace(job);
			}
			catch (SchedulerException ex) {
				log.warn("分组恢复失败 {} / {} : {}", job.getJobGroup(), job.getJobName(), ex.getMessage());
			}
		}
	}

}
