package com.auth.service.system.schedule.service.impl;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

import java.util.List;

import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.schedule.convert.LogJobConverter;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.mapper.LogJobMapper;
import com.auth.service.system.schedule.model.entity.LogJobEntity;
import com.auth.service.system.schedule.model.po.LogJobPageRowPO;
import com.auth.service.system.schedule.model.query.LogJobQuery;
import com.auth.service.system.schedule.model.vo.LogJobDetailVO;
import com.auth.service.system.schedule.model.vo.LogJobPageResponse;
import com.auth.service.system.schedule.service.LogJobService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务执行日志实现
 *
 * @author Bunny
 */
@Slf4j
@Service
public class LogJobServiceImpl extends ServiceImpl<LogJobMapper, LogJobEntity> implements LogJobService {

	private final AuditUserDisplayService auditUserDisplayService;

	public LogJobServiceImpl(AuditUserDisplayService auditUserDisplayService) {
		this.auditUserDisplayService = auditUserDisplayService;
	}

	/**
	 * 构建基础日志实体
	 * @param jobId 任务ID
	 * @param jobName 任务名称
	 * @param jobGroup 任务组
	 * @param invokeTarget 任务执行目标
	 * @param triggerType 触发类型
	 * @param elapsedMs 执行时间
	 * @return 基础日志实体
	 */
	private static LogJobEntity buildBaseLog(Long jobId, String jobName, String jobGroup, String invokeTarget,
			String triggerType, long elapsedMs) {
		LogJobEntity entity = new LogJobEntity();
		entity.setJobId(jobId);
		entity.setJobName(jobName);
		entity.setJobGroup(jobGroup);
		entity.setInvokeTarget(invokeTarget);
		entity.setTriggerType(triggerType);
		entity.setElapsedTime(elapsedMs);
		return entity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<LogJobPageResponse> getPage(Page<LogJobEntity> pageParams, LogJobQuery query) {
		IPage<LogJobPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		IPage<LogJobPageResponse> voPage = page.convert(LogJobConverter.INSTANCE::toPageResponse);

		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LogJobDetailVO getDetail(Long id) {
		LogJobEntity jobLog = getById(id);
		if (jobLog == null) {
			throw new SysJobException(DATA_NOT_EXIST);
		}

		LogJobDetailVO vo = LogJobConverter.INSTANCE.toDetailVo(jobLog);
		auditUserDisplayService.enrichAuditUsernames(List.of(vo));
		return vo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void recordSuccess(Long jobId, String jobName, String jobGroup, String invokeTarget, String triggerType,
			long elapsedMs, String message) {
		LogJobEntity entity = buildBaseLog(jobId, jobName, jobGroup, invokeTarget, triggerType, elapsedMs);
		entity.setJobMessage(message);
		entity.setStatus(true);
		save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void recordFailure(Long jobId, String jobName, String jobGroup, String invokeTarget, String triggerType,
			long elapsedMs, JobExecutionException ex) {
		LogJobEntity entity = buildBaseLog(jobId, jobName, jobGroup, invokeTarget, triggerType, elapsedMs);
		entity.setJobMessage("FAILED");
		entity.setStatus(false);
		if (ex != null) {
			entity.setExceptionInfo(ExceptionUtil.stacktraceToString(ex, 8000));
		}
		save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteByJobNameAndGroup(String jobName, String jobGroup) {
		baseMapper.deleteByJobNameAndGroup(jobName, jobGroup);
	}

}
