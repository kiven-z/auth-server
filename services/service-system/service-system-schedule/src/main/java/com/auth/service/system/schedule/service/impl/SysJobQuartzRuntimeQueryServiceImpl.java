package com.auth.service.system.schedule.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
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
		if (CollUtil.isEmpty(rows)) {
			return;
		}

		List<JobKey> jobKeys = rows.stream().map(row -> JobKey.jobKey(row.getJobName(), row.getJobGroup())).toList();
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
		SysJobQuartzRuntimeStatus status = snapshot.status();
		row.setQuartzFireTime(snapshot.fireTime());
		if (status != SysJobQuartzRuntimeStatus.IDLE) {
			row.setQuartzRuntimeStatus(status);
			return;
		}

		LogJobEntity latestLog = CollUtil
			.getFirst(logJobMapper.selectRecentByJobNameAndGroup(row.getJobName(), row.getJobGroup(), 1));

		if (latestLog != null && BooleanUtil.isFalse(latestLog.getStatus())) {
			row.setQuartzRuntimeStatus(SysJobQuartzRuntimeStatus.FAILED);
			row.setQuartzLastFailedTime(latestLog.getCreateTime());
			return;
		}

		row.setQuartzRuntimeStatus(status);
	}

}
