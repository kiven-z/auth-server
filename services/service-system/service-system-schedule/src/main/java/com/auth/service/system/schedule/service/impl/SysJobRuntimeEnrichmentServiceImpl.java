package com.auth.service.system.schedule.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import com.auth.service.system.schedule.mapper.LogJobMapper;
import com.auth.service.system.schedule.model.entity.LogJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobLastExecutionStatus;
import com.auth.service.system.schedule.model.enums.SysJobQuartzRuntimeStatus;
import com.auth.service.system.schedule.model.vo.SysJobDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobLastExecutionView;
import com.auth.service.system.schedule.service.SysJobRuntimeEnrichmentService;
import com.auth.service.system.schedule.support.SysJobQuartzRuntimeSnapshot;
import com.auth.service.system.schedule.support.quartz.SysJobQuartzRuntimeReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys.JOB_ZONE;

/**
 * 定时任务运行时信息补充实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class SysJobRuntimeEnrichmentServiceImpl implements SysJobRuntimeEnrichmentService {

	private final SysJobQuartzRuntimeReader sysJobQuartzRuntimeReader;

	private final LogJobMapper logJobMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enrichDetail(SysJobDetailVO detail, String jobName, String jobGroup) {
		SysJobQuartzRuntimeReader.FireTimes fireTimes = sysJobQuartzRuntimeReader.getFireTimes(jobName, jobGroup);
		detail.setPreviousFireTime(toLocalDateTime(fireTimes.previousFireTime()));
		detail.setNextFireTime(toLocalDateTime(fireTimes.nextFireTime()));

		SysJobQuartzRuntimeSnapshot snapshot = sysJobQuartzRuntimeReader.resolveRuntimeSnapshot(jobName, jobGroup);
		detail.setQuartzRuntimeStatus(snapshot.status());
		detail.setQuartzFireTime(snapshot.fireTime());

		enrichLastExecution(List.of(detail));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isBusy(String jobName, String jobGroup) {
		SysJobQuartzRuntimeSnapshot snapshot = sysJobQuartzRuntimeReader.resolveRuntimeSnapshot(jobName, jobGroup);
		SysJobQuartzRuntimeStatus status = snapshot.status();
		return status == SysJobQuartzRuntimeStatus.RUNNING || status == SysJobQuartzRuntimeStatus.PENDING;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enrichLastExecution(List<? extends SysJobLastExecutionView> views) {
		if (CollUtil.isEmpty(views)) {
			return;
		}

		List<Long> jobIds = views.stream().map(SysJobLastExecutionView::getId).filter(Objects::nonNull).toList();
		Map<Long, LogJobEntity> latestLogByJobId = loadLatestLogsByJobIds(jobIds);

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

	private Map<Long, LogJobEntity> loadLatestLogsByJobIds(List<Long> jobIds) {
		if (CollUtil.isEmpty(jobIds)) {
			return Map.of();
		}
		return logJobMapper.selectLatestByJobIds(jobIds)
			.stream()
			.filter(log -> log.getJobId() != null)
			.collect(Collectors.toMap(LogJobEntity::getJobId, Function.identity(), (first, ignored) -> first));
	}

	private LocalDateTime toLocalDateTime(Date date) {
		if (date == null) {
			return null;
		}
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), JOB_ZONE);
	}

}
