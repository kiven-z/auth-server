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
import com.auth.service.system.schedule.validation.task.SysJobDefinitionValidationManager;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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

		// 丰富最近一次执行
		enrichLastExecution(voPage.getRecords());

		// 丰富审计用户名
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

		// 查询任务触发时间
		String jobName = detailRow.getJobName();
		String jobGroup = detailRow.getJobGroup();
		SysJobQuartzRuntimeReader.FireTimes fireTimes = sysJobQuartzRuntimeReader.getFireTimes(jobName, jobGroup);

		// 构建详情VO
		SysJobDetailVO vo = SysJobConverter.INSTANCE.toDetailVo(detailRow);
		vo.setPreviousFireTime(fireTimes.previousFireTime());
		vo.setNextFireTime(fireTimes.nextFireTime());

		// 解析单个任务的 Quartz 运行时状态
		SysJobQuartzRuntimeSnapshot snapshot = sysJobQuartzRuntimeReader.resolveRuntimeSnapshot(jobName, jobGroup);
		vo.setQuartzRuntimeStatus(snapshot.status());
		vo.setQuartzFireTime(snapshot.fireTime());

		enrichLastExecution(List.of(vo));
		auditUserDisplayService.enrichAuditUsernames(List.of(vo));
		return vo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void create(SysJobCreateForm form) {
		// 验证分组是否存在
		SysJobGroupEntity sysJobGroup = sysJobGroupMapper.selectOne(
				Wrappers.<SysJobGroupEntity>lambdaQuery().eq(SysJobGroupEntity::getGroupCode, form.getJobGroup()));
		if (sysJobGroup == null) {
			throw new SysJobException(DATA_NOT_EXIST);
		}

		// 验证分组是否启用
		if (BooleanUtil.isFalse(sysJobGroup.getStatus())) {
			throw new SysJobException(JOB_GROUP_NOT_AVAILABLE);
		}

		// 转换为实体
		SysJobEntity sysJob = SysJobConverter.INSTANCE.toEntity(form);
		validationManager.normalizeAndValidate(sysJob);
		save(sysJob);

		// 如果任务禁用，则直接返回
		if (BooleanUtil.isFalse(sysJob.getStatus())) {
			return;
		}

		try {
			sysJobSchedulerManager.scheduleOrReplace(sysJob);
		}
		catch (SchedulerException ex) {
			log.error("注册 Quartz 失败", ex);
			throw new SysJobException(JOB_QUARTZ_OPERATION_FAILED, "register", ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(SysJobUpdateForm form) {
		// 验证 id 是否存在
		SysJobEntity dbJob = getDbJob(form.getId());

		// 更新实体
		SysJobConverter.INSTANCE.updateEntity(form, dbJob);
		validationManager.normalizeAndValidate(dbJob);
		updateById(dbJob);

		try {
			// 注册 Quartz
			sysJobSchedulerManager.scheduleOrReplace(dbJob);
		}
		catch (SchedulerException ex) {
			log.error("更新 Quartz 失败", ex);
			throw new SysJobException(JOB_QUARTZ_OPERATION_FAILED, "update", ex.getMessage());
		}
	}

	@NotNull
	private SysJobEntity getDbJob(Long id) {
		SysJobEntity dbJob = getById(id);
		if (dbJob == null) {
			throw new SysJobException(DATA_NOT_EXIST);
		}
		return dbJob;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteById(Long id) {
		SysJobEntity dbJob = getDbJob(id);

		try {
			// 删除 Quartz Job
			String jobName = dbJob.getJobName();
			String jobGroup = dbJob.getJobGroup();
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
		SysJobEntity dbJob = getDbJob(id);

		// 验证 Quartz 运行状态
		String jobName = dbJob.getJobName();
		String jobGroup = dbJob.getJobGroup();
		SysJobQuartzRuntimeStatus runtimeStatus = sysJobQuartzRuntimeReader.resolveRuntimeSnapshot(jobName, jobGroup)
			.status();
		if (runtimeStatus == SysJobQuartzRuntimeStatus.RUNNING || runtimeStatus == SysJobQuartzRuntimeStatus.PENDING) {
			throw new SysJobException(JOB_BUSY);
		}

		try {
			// 确保 JobDetail 存在
			sysJobSchedulerManager.ensureJobDetail(dbJob);

			// 触发 Job
			sysJobSchedulerManager.triggerJob(jobName, jobGroup);
		}
		catch (SchedulerException ex) {
			log.error("立即执行失败", ex);
			throw new SysJobException(JOB_QUARTZ_OPERATION_FAILED, "runOnce", ex.getMessage());
		}
	}

	/**
	 * 丰富最近一次执行
	 * @param views 视图
	 */
	private void enrichLastExecution(List<? extends SysJobLastExecutionView> views) {
		// 如果视图列表为空，则直接返回
		if (CollUtil.isEmpty(views)) {
			return;
		}
		// 获取任务 ID 列表
		List<Long> jobIds = views.stream().map(SysJobLastExecutionView::getId).filter(Objects::nonNull).toList();
		Map<Long, LogJobEntity> latestLogByJobId = CollUtil.isEmpty(jobIds) ? Map.of()
				: logJobMapper.selectLatestByJobIds(jobIds)
					.stream()
					.filter(log -> log.getJobId() != null)
					.collect(Collectors.toMap(LogJobEntity::getJobId, Function.identity(), (first, ignored) -> first));

		// 丰富最近一次执行
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
