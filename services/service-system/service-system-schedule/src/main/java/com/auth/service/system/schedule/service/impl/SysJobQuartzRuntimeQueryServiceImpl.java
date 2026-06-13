package com.auth.service.system.schedule.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import com.auth.service.system.schedule.mapper.LogJobMapper;
import com.auth.service.system.schedule.model.entity.LogJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobLastExecutionStatus;
import com.auth.service.system.schedule.model.vo.SysJobPageResponse;
import com.auth.service.system.schedule.service.SysJobQuartzRuntimeQueryService;
import com.auth.service.system.schedule.support.SysJobQuartzRuntimeSnapshot;
import com.auth.service.system.schedule.support.SysJobSchedulerManager;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.quartz.JobKey;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Quartz 任务运行时状态查询实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class SysJobQuartzRuntimeQueryServiceImpl implements SysJobQuartzRuntimeQueryService {

	private final SysJobSchedulerManager quartzAdmin;

	private final LogJobMapper logJobMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enrichPageRows(List<SysJobPageResponse> rows) {
		if (CollUtil.isEmpty(rows)) {
			return;
		}

		List<JobKey> jobKeys = rows.stream().map(row -> JobKey.jobKey(row.getJobName(), row.getJobGroup())).toList();
		Map<JobKey, SysJobQuartzRuntimeSnapshot> snapshots = quartzAdmin.batchResolveRuntimeSnapshots(jobKeys);

		List<Long> jobIds = rows.stream().map(SysJobPageResponse::getId).filter(Objects::nonNull).toList();
		Map<Long, LogJobEntity> latestLogByJobId = loadLatestLogsByJobIds(jobIds);

		for (SysJobPageResponse row : rows) {
			JobKey jobKey = JobKey.jobKey(row.getJobName(), row.getJobGroup());
			SysJobQuartzRuntimeSnapshot snapshot = snapshots.get(jobKey);
			if (snapshot == null) {
				continue;
			}
			row.setQuartzRuntimeStatus(snapshot.status());
			row.setQuartzFireTime(snapshot.fireTime());

			Long jobId = row.getId();
			LogJobEntity latestLog = jobId == null ? null : latestLogByJobId.get(jobId);
			if (latestLog == null) {
				row.setLastExecutionStatus(SysJobLastExecutionStatus.UNKNOWN);
				continue;
			}
			row.setLastExecutionTime(latestLog.getCreateTime());
			row.setLastExecutionStatus(BooleanUtil.isTrue(latestLog.getStatus()) ? SysJobLastExecutionStatus.SUCCESS
					: SysJobLastExecutionStatus.FAILED);
		}
	}

	private Map<Long, LogJobEntity> loadLatestLogsByJobIds(List<Long> jobIds) {
		if (CollUtil.isEmpty(jobIds)) {
			return Map.of();
		}
		return logJobMapper
			.selectList(Wrappers.<LogJobEntity>lambdaQuery()
				.in(LogJobEntity::getJobId, jobIds)
				.orderByDesc(LogJobEntity::getCreateTime))
			.stream()
			.filter(log -> log.getJobId() != null)
			.collect(Collectors.toMap(LogJobEntity::getJobId, Function.identity(), (first, ignored) -> first));
	}

}
