package com.auth.service.system.schedule.service.impl;

import com.auth.service.system.schedule.mapper.LogJobMapper;
import com.auth.service.system.schedule.model.entity.LogJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobQuartzRuntimeStatus;
import com.auth.service.system.schedule.model.vo.SysJobPageResponse;
import com.auth.service.system.schedule.service.SysJobQuartzRuntimeQueryService;
import com.auth.service.system.schedule.support.SysJobQuartzRuntimeSnapshot;
import com.auth.service.system.schedule.support.SysJobSchedulerManager;
import lombok.RequiredArgsConstructor;
import org.quartz.JobKey;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		if (rows == null || rows.isEmpty()) {
			return;
		}
		List<JobKey> jobKeys = new ArrayList<>(rows.size());
		for (SysJobPageResponse row : rows) {
			jobKeys.add(JobKey.jobKey(row.getJobName(), row.getJobGroup()));
		}
		Map<JobKey, SysJobQuartzRuntimeSnapshot> snapshots = quartzAdmin.batchResolveRuntimeSnapshots(jobKeys);
		for (SysJobPageResponse row : rows) {
			JobKey jobKey = JobKey.jobKey(row.getJobName(), row.getJobGroup());
			SysJobQuartzRuntimeSnapshot snapshot = snapshots.get(jobKey);
			if (snapshot == null) {
				continue;
			}
			applyRuntimeSnapshot(row, snapshot);
		}
	}

	private void applyRuntimeSnapshot(SysJobPageResponse row, SysJobQuartzRuntimeSnapshot snapshot) {
		String jobName = row.getJobName();
		String jobGroup = row.getJobGroup();
		SysJobQuartzRuntimeStatus status = applyFailedIfIdle(jobName, jobGroup, snapshot.status());
		row.setQuartzRuntimeStatus(status);
		row.setQuartzFireTime(snapshot.fireTime());
		if (status == SysJobQuartzRuntimeStatus.FAILED) {
			row.setQuartzLastFailedTime(resolveLastFailedTime(jobName, jobGroup));
		}
	}

	private SysJobQuartzRuntimeStatus applyFailedIfIdle(String jobName, String jobGroup,
			SysJobQuartzRuntimeStatus status) {
		if (status != SysJobQuartzRuntimeStatus.IDLE) {
			return status;
		}
		List<LogJobEntity> recentLogs = logJobMapper.selectRecentByJobNameAndGroup(jobName, jobGroup, 1);
		if (recentLogs.isEmpty()) {
			return status;
		}
		LogJobEntity latest = recentLogs.get(0);
		if (Boolean.FALSE.equals(latest.getStatus())) {
			return SysJobQuartzRuntimeStatus.FAILED;
		}
		return status;
	}

	private java.time.LocalDateTime resolveLastFailedTime(String jobName, String jobGroup) {
		List<LogJobEntity> recentLogs = logJobMapper.selectRecentByJobNameAndGroup(jobName, jobGroup, 1);
		if (recentLogs.isEmpty()) {
			return null;
		}
		LogJobEntity latest = recentLogs.get(0);
		return Boolean.FALSE.equals(latest.getStatus()) ? latest.getCreateTime() : null;
	}

}
