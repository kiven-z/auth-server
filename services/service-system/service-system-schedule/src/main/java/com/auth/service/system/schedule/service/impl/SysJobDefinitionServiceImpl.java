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
import com.auth.service.system.schedule.model.entity.LogJobEntity;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.entity.SysJobGroupEntity;
import com.auth.service.system.schedule.model.enums.SysJobLastExecutionStatus;
import com.auth.service.system.schedule.model.enums.SysJobQuartzRuntimeStatus;
import com.auth.service.system.schedule.model.form.SysJobCreateForm;
import com.auth.service.system.schedule.model.form.SysJobUpdateForm;
import com.auth.service.system.schedule.model.po.SysJobDetailRowPO;
import com.auth.service.system.schedule.model.po.SysJobPageRowPO;
import com.auth.service.system.schedule.model.query.SysJobQuery;
import com.auth.service.system.schedule.model.vo.SysJobDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobLastExecutionView;
import com.auth.service.system.schedule.model.vo.SysJobPageResponse;
import com.auth.service.system.schedule.service.SysJobDefinitionService;
import com.auth.service.system.schedule.support.SysJobQuartzRuntimeSnapshot;
import com.auth.service.system.schedule.support.quartz.SysJobQuartzRuntimeReader;
import com.auth.service.system.schedule.support.quartz.SysJobSchedulerManager;
import com.auth.service.system.schedule.validation.SysJobDefinitionValidationManager;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.*;
import static com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys.JOB_ZONE;

/**
 * 定时任务定义服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SysJobDefinitionServiceImpl extends ServiceImpl<SysJobMapper, SysJobEntity>
		implements SysJobDefinitionService {

	private final SysJobSchedulerManager sysJobSchedulerManager;

	private final AuditUserDisplayService auditUserDisplayService;

	private final SysJobGroupMapper sysJobGroupMapper;

	private final LogJobMapper logJobMapper;

	private final SysJobDefinitionValidationManager validationManager;

	private final SysJobQuartzRuntimeReader sysJobQuartzRuntimeReader;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysJobPageResponse> getPage(Page<SysJobEntity> pageParams, SysJobQuery query) {
		IPage<SysJobPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		IPage<SysJobPageResponse> voPage = page.convert(SysJobConverter.INSTANCE::toPageResponse);

		enrichLastExecution(voPage.getRecords());
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
		enrichDetail(vo, detailRow.getJobName(), detailRow.getJobGroup());
		auditUserDisplayService.enrichAuditUsernames(List.of(vo));
		return vo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
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
	@Transactional(rollbackFor = Exception.class)
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
	@Transactional(rollbackFor = Exception.class)
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

		removeById(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void runOnce(Long id) {
		SysJobEntity dbJob = getById(id);
		if (dbJob == null) {
			throw new SysJobException(DATA_NOT_EXIST);
		}
		SysJobQuartzRuntimeStatus runtimeStatus = sysJobQuartzRuntimeReader
			.resolveRuntimeSnapshot(dbJob.getJobName(), dbJob.getJobGroup())
			.status();
		if (runtimeStatus == SysJobQuartzRuntimeStatus.RUNNING || runtimeStatus == SysJobQuartzRuntimeStatus.PENDING) {
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

	/**
	 * 丰富详情
	 * @param detail 详情
	 * @param jobName 任务名称
	 * @param jobGroup 任务组
	 */
	private void enrichDetail(SysJobDetailVO detail, String jobName, String jobGroup) {
		SysJobQuartzRuntimeReader.FireTimes fireTimes = sysJobQuartzRuntimeReader.getFireTimes(jobName, jobGroup);
		Date previousFireTime = fireTimes.previousFireTime();
		detail.setPreviousFireTime(previousFireTime == null ? null
				: LocalDateTime.ofInstant(Instant.ofEpochMilli(previousFireTime.getTime()), JOB_ZONE));
		Date nextFireTime = fireTimes.nextFireTime();
		detail.setNextFireTime(nextFireTime == null ? null
				: LocalDateTime.ofInstant(Instant.ofEpochMilli(nextFireTime.getTime()), JOB_ZONE));

		SysJobQuartzRuntimeSnapshot snapshot = sysJobQuartzRuntimeReader.resolveRuntimeSnapshot(jobName, jobGroup);
		detail.setQuartzRuntimeStatus(snapshot.status());
		detail.setQuartzFireTime(snapshot.fireTime());

		enrichLastExecution(List.of(detail));
	}

	/**
	 * 丰富最近一次执行
	 * @param views 视图
	 */
	private void enrichLastExecution(List<? extends SysJobLastExecutionView> views) {
		if (CollUtil.isEmpty(views)) {
			return;
		}

		List<Long> jobIds = views.stream().map(SysJobLastExecutionView::getId).filter(Objects::nonNull).toList();
		Map<Long, LogJobEntity> latestLogByJobId = CollUtil.isEmpty(jobIds) ? Map.of()
				: logJobMapper.selectLatestByJobIds(jobIds)
					.stream()
					.filter(log -> log.getJobId() != null)
					.collect(Collectors.toMap(LogJobEntity::getJobId, Function.identity(), (first, ignored) -> first));

		for (SysJobLastExecutionView view : views) {
			Long jobId = view.getId();
			LogJobEntity latestLog = jobId == null ? null : latestLogByJobId.get(jobId);
			if (latestLog == null) {
				view.setLastExecutionStatus(SysJobLastExecutionStatus.UNKNOWN);
				continue;
			}
			view.setLastExecutionTime(latestLog.getCreateTime());
			view.setLastExecutionStatus(BooleanUtil.isTrue(latestLog.getStatus()) ? SysJobLastExecutionStatus.SUCCESS
					: SysJobLastExecutionStatus.FAILED);
		}
	}

}
