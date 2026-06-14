package com.auth.service.system.schedule.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.schedule.convert.SysJobConverter;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.mapper.LogJobMapper;
import com.auth.service.system.schedule.mapper.SysJobGroupMapper;
import com.auth.service.system.schedule.mapper.SysJobMapper;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.entity.SysJobGroupEntity;
import com.auth.service.system.schedule.model.form.SysJobCreateForm;
import com.auth.service.system.schedule.model.form.SysJobStatusForm;
import com.auth.service.system.schedule.model.form.SysJobUpdateForm;
import com.auth.service.system.schedule.model.po.SysJobDetailRowPO;
import com.auth.service.system.schedule.model.po.SysJobPageRowPO;
import com.auth.service.system.schedule.model.query.SysJobQuery;
import com.auth.service.system.schedule.model.vo.SysJobDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobPageResponse;
import com.auth.service.system.schedule.service.SysJobDefinitionService;
import com.auth.service.system.schedule.service.SysJobRuntimeEnrichmentService;
import com.auth.service.system.schedule.support.SysJobSchedulerManager;
import com.auth.service.system.schedule.validation.SysJobDefinitionValidationManager;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.*;

/**
 * 定时任务定义服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysJobDefinitionServiceImpl extends ServiceImpl<SysJobMapper, SysJobEntity>
		implements SysJobDefinitionService {

	private final SysJobSchedulerManager sysJobSchedulerManager;

	private final AuditUserDisplayService auditUserDisplayService;

	private final SysJobGroupMapper sysJobGroupMapper;

	private final LogJobMapper logJobMapper;

	private final SysJobDefinitionValidationManager validationManager;

	private final SysJobRuntimeEnrichmentService sysJobRuntimeEnrichmentService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysJobPageResponse> getPage(Page<SysJobEntity> pageParams, SysJobQuery query) {
		IPage<SysJobPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		IPage<SysJobPageResponse> voPage = page.convert(SysJobConverter.INSTANCE::toPageResponse);

		sysJobRuntimeEnrichmentService.enrichLastExecution(voPage.getRecords());
		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysJobDetailVO getDetail(Long id) {
		SysJobDetailRowPO detailRow = baseMapper.selectDetailById(id);
		if (detailRow == null) {
			throw new SysJobException(DATA_NOT_EXIST);
		}

		SysJobDetailVO vo = SysJobConverter.INSTANCE.toDetailVo(detailRow);
		sysJobRuntimeEnrichmentService.enrichDetail(vo, detailRow.getJobName(), detailRow.getJobGroup());
		auditUserDisplayService.enrichAuditUsernames(List.of(vo));
		return vo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(SysJobCreateForm form) {
		SysJobGroupEntity sysJobGroup = sysJobGroupMapper.selectOne(
				Wrappers.<SysJobGroupEntity>lambdaQuery().eq(SysJobGroupEntity::getGroupCode, form.getJobGroup()));
		if (sysJobGroup == null) {
			throw new SysJobException(DATA_NOT_EXIST);
		}
		if (BooleanUtil.isFalse(sysJobGroup.getStatus())) {
			throw new SysJobException(JOB_GROUP_NOT_AVAILABLE);
		}

		SysJobEntity sysJob = SysJobConverter.INSTANCE.toEntity(form);
		validationManager.normalizeAndValidate(sysJob);
		save(sysJob);

		if (BooleanUtil.isTrue(sysJob.getStatus())) {
			try {
				sysJobSchedulerManager.scheduleOrReplace(sysJob);
			}
			catch (SchedulerException ex) {
				log.error("注册 Quartz 失败", ex);
				throw new SysJobException(JOB_QUARTZ_OPERATION_FAILED, "register", ex.getMessage());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(SysJobUpdateForm form) {
		SysJobEntity dbJob = getById(form.getId());
		if (dbJob == null) {
			throw new SysJobException(DATA_NOT_EXIST);
		}

		SysJobConverter.INSTANCE.updateEntity(form, dbJob);
		validationManager.normalizeAndValidate(dbJob);
		updateById(dbJob);

		try {
			sysJobSchedulerManager.scheduleOrReplace(dbJob);
		}
		catch (SchedulerException ex) {
			log.error("更新 Quartz 失败", ex);
			throw new SysJobException(JOB_QUARTZ_OPERATION_FAILED, "update", ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteById(Long id) {
		SysJobEntity dbJob = getById(id);
		if (dbJob == null) {
			throw new SysJobException(DATA_NOT_EXIST);
		}
		String jobName = dbJob.getJobName();
		String jobGroup = dbJob.getJobGroup();

		try {
			sysJobSchedulerManager.deleteJobIfExists(JobKey.jobKey(jobName, jobGroup));
		}
		catch (SchedulerException ex) {
			log.error("删除 Quartz Job 失败", ex);
			throw new SysJobException(JOB_QUARTZ_OPERATION_FAILED, "delete", ex.getMessage());
		}

		try {
			logJobMapper.deleteByJobNameAndGroup(jobName, jobGroup);
			removeById(id);
		}
		catch (DataAccessException ex) {
			log.error("级联物理删除日志失败", ex);
			throw new SysJobException(JOB_LOG_DELETE_FAILED, ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
			throw new SysJobException(JOB_QUARTZ_OPERATION_FAILED, "batchStatus", ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
			throw new SysJobException(JOB_QUARTZ_OPERATION_FAILED, "batchGroupStatus", ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void runOnce(Long id) {
		SysJobEntity dbJob = getById(id);
		if (dbJob == null) {
			throw new SysJobException(DATA_NOT_EXIST);
		}
		if (sysJobRuntimeEnrichmentService.isBusy(dbJob.getJobName(), dbJob.getJobGroup())) {
			throw new SysJobException(JOB_BUSY);
		}
		try {
			sysJobSchedulerManager.ensureJobDetail(dbJob);
			sysJobSchedulerManager.triggerJob(dbJob.getJobName(), dbJob.getJobGroup());
		}
		catch (SchedulerException ex) {
			log.error("立即执行失败", ex);
			throw new SysJobException(JOB_QUARTZ_OPERATION_FAILED, "runOnce", ex.getMessage());
		}
	}

}
