package com.auth.service.system.schedule.service.impl;

import com.auth.service.system.schedule.mapper.JobLogMapper;
import com.auth.service.system.schedule.model.entity.JobLogEntity;
import com.auth.service.system.schedule.model.enums.SysJobQuartzRuntimeStatus;
import com.auth.service.system.schedule.model.vo.SysJobPageResponse;
import com.auth.service.system.schedule.support.SysJobQuartzRuntimeSnapshot;
import com.auth.service.system.schedule.support.SysJobSchedulerManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobKey;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * {@link SysJobQuartzRuntimeQueryServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SysJobQuartzRuntimeQueryServiceImpl Quartz 运行时状态查询")
class SysJobQuartzRuntimeQueryServiceImplTest {

	@Mock
	private SysJobSchedulerManager quartzAdmin;

	@Mock
	private JobLogMapper jobLogMapper;

	@InjectMocks
	private SysJobQuartzRuntimeQueryServiceImpl sysJobQuartzRuntimeQueryService;

	@Test
	@DisplayName("分页补充：RUNNING 时写入 fireTime")
	void enrichPageRowsSetsRunningSnapshot() {
		SysJobPageResponse row = jobRow("demo", "G");
		LocalDateTime fireTime = LocalDateTime.of(2026, 6, 13, 10, 0);
		JobKey jobKey = JobKey.jobKey("demo", "G");
		when(quartzAdmin.batchResolveRuntimeSnapshots(List.of(jobKey)))
			.thenReturn(Map.of(jobKey, SysJobQuartzRuntimeSnapshot.of(SysJobQuartzRuntimeStatus.RUNNING, fireTime)));

		sysJobQuartzRuntimeQueryService.enrichPageRows(List.of(row));

		assertThat(row.getQuartzRuntimeStatus()).isEqualTo(SysJobQuartzRuntimeStatus.RUNNING);
		assertThat(row.getQuartzFireTime()).isEqualTo(fireTime);
	}

	@Test
	@DisplayName("分页补充：IDLE 且最近日志失败时标记 FAILED")
	void enrichPageRowsMarksFailedWhenLatestLogFailed() {
		SysJobPageResponse row = jobRow("demo", "G");
		JobKey jobKey = JobKey.jobKey("demo", "G");
		when(quartzAdmin.batchResolveRuntimeSnapshots(List.of(jobKey)))
			.thenReturn(Map.of(jobKey, SysJobQuartzRuntimeSnapshot.of(SysJobQuartzRuntimeStatus.IDLE, null)));
		JobLogEntity failedLog = new JobLogEntity();
		failedLog.setStatus(false);
		failedLog.setCreateTime(LocalDateTime.of(2026, 6, 13, 9, 0));
		when(jobLogMapper.selectRecentByJobNameAndGroup("demo", "G", 1)).thenReturn(List.of(failedLog));

		sysJobQuartzRuntimeQueryService.enrichPageRows(List.of(row));

		assertThat(row.getQuartzRuntimeStatus()).isEqualTo(SysJobQuartzRuntimeStatus.FAILED);
		assertThat(row.getQuartzLastFailedTime()).isEqualTo(failedLog.getCreateTime());
	}

	@Test
	@DisplayName("分页补充：批量写入 quartzRuntimeStatus")
	void enrichPageRowsSetsQuartzRuntimeStatus() {
		SysJobPageResponse row = jobRow("demo", "G");
		JobKey jobKey = JobKey.jobKey("demo", "G");
		when(quartzAdmin.batchResolveRuntimeSnapshots(List.of(jobKey)))
			.thenReturn(Map.of(jobKey, SysJobQuartzRuntimeSnapshot.of(SysJobQuartzRuntimeStatus.PENDING, null)));

		sysJobQuartzRuntimeQueryService.enrichPageRows(List.of(row));

		assertThat(row.getQuartzRuntimeStatus()).isEqualTo(SysJobQuartzRuntimeStatus.PENDING);
	}

	private SysJobPageResponse jobRow(String jobName, String jobGroup) {
		SysJobPageResponse row = new SysJobPageResponse();
		row.setJobName(jobName);
		row.setJobGroup(jobGroup);
		return row;
	}

}
