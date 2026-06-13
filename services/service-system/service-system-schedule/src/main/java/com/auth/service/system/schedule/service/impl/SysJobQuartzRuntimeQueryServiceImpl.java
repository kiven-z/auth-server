package com.auth.service.system.schedule.service.impl;

import com.auth.service.system.schedule.mapper.JobLogMapper;
import com.auth.service.system.schedule.model.entity.JobLogEntity;
import com.auth.service.system.schedule.model.enums.SysJobQuartzRuntimeStatus;
import com.auth.service.system.schedule.model.vo.SysJobPageResponse;
import com.auth.service.system.schedule.model.vo.SysJobQuartzRuntimeStatusVO;
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

	private final JobLogMapper jobLogMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysJobQuartzRuntimeStatusVO getRuntimeStatus(String jobName, String jobGroup) {
		SysJobQuartzRuntimeSnapshot snapshot = quartzAdmin.resolveRuntimeSnapshot(jobName, jobGroup);
		return toVo(jobName, jobGroup, snapshot);
	}

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
			SysJobQuartzRuntimeStatusVO vo = toVo(row.getJobName(), row.getJobGroup(), snapshot);
			row.setQuartzRuntimeStatus(vo.getStatus());
			row.setQuartzFireTime(vo.getFireTime());
			row.setQuartzLastFailedTime(vo.getLastFailedTime());
		}
	}

	private SysJobQuartzRuntimeStatusVO toVo(String jobName, String jobGroup, SysJobQuartzRuntimeSnapshot snapshot) {
		SysJobQuartzRuntimeStatus status = applyFailedIfIdle(jobName, jobGroup, snapshot.status());
		SysJobQuartzRuntimeStatusVO vo = new SysJobQuartzRuntimeStatusVO();
		vo.setJobName(jobName);
		vo.setJobGroup(jobGroup);
		vo.setStatus(status);
		vo.setFireTime(snapshot.fireTime());
		if (status == SysJobQuartzRuntimeStatus.FAILED) {
			vo.setLastFailedTime(resolveLastFailedTime(jobName, jobGroup));
		}
		return vo;
	}

	private SysJobQuartzRuntimeStatus applyFailedIfIdle(String jobName, String jobGroup,
			SysJobQuartzRuntimeStatus status) {
		if (status != SysJobQuartzRuntimeStatus.IDLE) {
			return status;
		}
		List<JobLogEntity> recentLogs = jobLogMapper.selectRecentByJobNameAndGroup(jobName, jobGroup, 1);
		if (recentLogs.isEmpty()) {
			return status;
		}
		JobLogEntity latest = recentLogs.get(0);
		if (Boolean.FALSE.equals(latest.getStatus())) {
			return SysJobQuartzRuntimeStatus.FAILED;
		}
		return status;
	}

	private java.time.LocalDateTime resolveLastFailedTime(String jobName, String jobGroup) {
		List<JobLogEntity> recentLogs = jobLogMapper.selectRecentByJobNameAndGroup(jobName, jobGroup, 1);
		if (recentLogs.isEmpty()) {
			return null;
		}
		JobLogEntity latest = recentLogs.get(0);
		return Boolean.FALSE.equals(latest.getStatus()) ? latest.getCreateTime() : null;
	}

}
