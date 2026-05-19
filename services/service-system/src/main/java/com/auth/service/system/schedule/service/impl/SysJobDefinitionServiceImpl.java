package com.auth.service.system.schedule.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.mapper.SysJobGroupMapper;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.log.model.entity.SysJobGroupEntity;
import com.auth.service.system.log.model.po.SysJobPageRowPO;
import com.auth.service.system.schedule.mapper.JobLogMapper;
import com.auth.service.system.schedule.mapper.SysJobMapper;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.form.SysJobCreateForm;
import com.auth.service.system.schedule.model.form.SysJobUpdateForm;
import com.auth.service.system.schedule.model.query.SysJobQuery;
import com.auth.service.system.schedule.model.vo.SysJobDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobPageResponse;
import com.auth.service.system.schedule.service.SysJobDefinitionService;
import com.auth.service.system.schedule.support.SysJobSchedulerManager;
import com.auth.service.system.schedule.validation.SysJobDefinitionValidationManager;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

import static com.auth.service.system.common.exception.SystemCommonResultCode.DATA_NOT_EXIST;
import static com.auth.service.system.schedule.exception.SystemScheduleResultCode.JOB_GROUP_NOT_AVAILABLE;
import static com.auth.service.system.schedule.exception.SystemScheduleResultCode.JOB_QUARTZ_OPERATION_FAILED;
import static com.auth.service.system.schedule.model.constans.SysJobQuartzDataKeys.JOB_ZONE;

/**
 * 定时任务定义服务实现（职责：定义态 CRUD + Quartz 同步）
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysJobDefinitionServiceImpl extends ServiceImpl<SysJobMapper, SysJobEntity>
		implements SysJobDefinitionService {

	private final SysJobSchedulerManager quartzAdmin;

	private final AuditUserDisplayService auditUserDisplayService;

	private final SysJobGroupMapper sysJobGroupMapper;

	private final JobLogMapper jobLogMapper;

	private final SysJobDefinitionValidationManager validationManager;

	public SysJobDefinitionServiceImpl(SysJobSchedulerManager quartzAdmin,
			AuditUserDisplayService auditUserDisplayService, SysJobGroupMapper sysJobGroupMapper,
			JobLogMapper jobLogMapper, SysJobDefinitionValidationManager validationManager) {
		this.quartzAdmin = quartzAdmin;
		this.auditUserDisplayService = auditUserDisplayService;
		this.sysJobGroupMapper = sysJobGroupMapper;
		this.jobLogMapper = jobLogMapper;
		this.validationManager = validationManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysJobPageResponse> getPage(Page<SysJobEntity> pageParams, SysJobQuery query) {
		IPage<SysJobPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		IPage<SysJobPageResponse> voPage = page.convert(po -> {
			SysJobPageResponse vo = new SysJobPageResponse();
			BeanUtils.copyProperties(po, vo);
			return vo;
		});
		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysJobDetailVO getDetail(Long id) {
		SysJobEntity sysJob = getById(id);
		if (sysJob == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		SysJobDetailVO vo = new SysJobDetailVO();
		BeanUtils.copyProperties(sysJob, vo);
		SysJobSchedulerManager.FireTimes fireTimes = quartzAdmin.getFireTimes(sysJob.getJobName(),
				sysJob.getJobGroup());
		vo.setPreviousFireTime(toLocalDateTime(fireTimes.previousFireTime()));
		vo.setNextFireTime(toLocalDateTime(fireTimes.nextFireTime()));
		return vo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(SysJobCreateForm form) {
		validateGroupEnabled(form.getJobGroup());
		SysJobEntity sysJob = BeanUtil.copyProperties(form, SysJobEntity.class);
		normalizeTaskDefinition(sysJob);
		save(sysJob);
		try {
			quartzAdmin.scheduleOrReplace(sysJob);
		}
		catch (SchedulerException ex) {
			log.error("注册 Quartz 失败", ex);
			throw new SystemBusinessException(JOB_QUARTZ_OPERATION_FAILED, "register", ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(SysJobUpdateForm form) {
		SysJobEntity dbJob = getById(Long.parseLong(form.getId()));
		if (dbJob == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		dbJob.setCronExpression(form.getCronExpression());
		dbJob.setMisfirePolicy(form.getMisfirePolicy());
		dbJob.setConcurrent(form.getConcurrent());
		dbJob.setStartTime(form.getStartTime());
		dbJob.setEndTime(form.getEndTime());
		dbJob.setStatus(form.getStatus());
		dbJob.setJobParams(form.getJobParams());
		dbJob.setPayloadJson(form.getPayloadJson());
		dbJob.setHandlerCode(form.getHandlerCode());
		dbJob.setTaskType(form.getTaskType());
		dbJob.setRemark(form.getRemark());
		if (form.getInvokeTarget() != null) {
			dbJob.setInvokeTarget(form.getInvokeTarget());
		}

		normalizeTaskDefinition(dbJob);
		updateById(dbJob);
		try {
			quartzAdmin.scheduleOrReplace(dbJob);
		}
		catch (SchedulerException ex) {
			log.error("更新 Quartz 失败", ex);
			throw new SystemBusinessException(JOB_QUARTZ_OPERATION_FAILED, "update", ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteById(Long id) {
		SysJobEntity dbJob = getById(id);
		if (dbJob == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		String jobName = dbJob.getJobName();
		String jobGroup = dbJob.getJobGroup();

		try {
			quartzAdmin.deleteJobIfExists(JobKey.jobKey(jobName, jobGroup));
		}
		catch (SchedulerException ex) {
			log.error("删除 Quartz Job 失败", ex);
			throw new SystemBusinessException(JOB_QUARTZ_OPERATION_FAILED, "delete", ex.getMessage());
		}
		try {
			jobLogMapper.deleteByJobNameAndGroup(jobName, jobGroup);
		}
		catch (Exception exception) {
			log.error("级联物理删除日志失败", exception);
		}
		removeById(id);
	}

	/**
	 * 统一规范任务定义并执行类型校验
	 * @param job 任务定义
	 */
	private void normalizeTaskDefinition(SysJobEntity job) {
		validationManager.normalizeAndValidate(job);
	}

	private LocalDateTime toLocalDateTime(Date date) {
		if (date == null) {
			return null;
		}
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), JOB_ZONE);
	}

	/**
	 * 验证分组是否启用
	 * @param groupCode 分组编码
	 */
	private void validateGroupEnabled(String groupCode) {
		LambdaQueryWrapper<SysJobGroupEntity> queryWrapper = new LambdaQueryWrapper<SysJobGroupEntity>()
			.eq(SysJobGroupEntity::getGroupCode, groupCode);
		SysJobGroupEntity sysJobGroup = sysJobGroupMapper.selectOne(queryWrapper);
		if (sysJobGroup == null || !BooleanUtil.isTrue(sysJobGroup.getStatus())) {
			throw new SystemBusinessException(JOB_GROUP_NOT_AVAILABLE);
		}
	}

}
