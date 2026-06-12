package com.auth.service.system.schedule.support;

import com.auth.service.system.schedule.config.ScheduleJobProperties;
import com.auth.service.system.schedule.mapper.JobLogMapper;
import com.auth.service.system.schedule.mapper.SysJobMapper;
import com.auth.service.system.schedule.model.entity.JobLogEntity;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.SchedulerException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysJobCircuitBreaker} 单元测试。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SysJobCircuitBreaker 连续失败熔断")
class SysJobCircuitBreakerTest {

	@Mock
	private SysJobSchedulerManager sysJobSchedulerManager;

	@Mock
	private SysJobMapper sysJobMapper;

	@Mock
	private JobLogMapper jobLogMapper;

	@Mock
	private ScheduleJobProperties scheduleJobProperties;

	@InjectMocks
	private SysJobCircuitBreaker sysJobCircuitBreaker;

	@Test
	@DisplayName("evaluateAfterFailure：运行中任务连续失败达到阈值时应置暂停并 pauseJob")
	void evaluateAfterFailure_whenActiveAndThresholdReached_pausesJob() throws SchedulerException {
		when(scheduleJobProperties.getConsecutiveFailureThreshold()).thenReturn(5);

		SysJobEntity job = new SysJobEntity();
		job.setId(1L);
		job.setStatus(true);
		when(sysJobMapper.selectById(1L)).thenReturn(job);
		when(jobLogMapper.selectRecentByJobNameAndGroup("demoJob", "DEFAULT", 5)).thenReturn(failedLogs(5));

		sysJobCircuitBreaker.evaluateAfterFailure(1L, "demoJob", "DEFAULT");

		verify(sysJobMapper).updateById(any(SysJobEntity.class));
		verify(sysJobSchedulerManager).pauseJob("demoJob", "DEFAULT");
	}

	@Test
	@DisplayName("evaluateAfterFailure：数据库已暂停但 Quartz 仍在跑时应补调 pauseJob")
	void evaluateAfterFailure_whenDbPausedButThresholdReached_stillPausesQuartz() throws SchedulerException {
		when(scheduleJobProperties.getConsecutiveFailureThreshold()).thenReturn(5);

		SysJobEntity job = new SysJobEntity();
		job.setId(1L);
		job.setStatus(false);
		when(sysJobMapper.selectById(1L)).thenReturn(job);
		when(jobLogMapper.selectRecentByJobNameAndGroup("demoJob", "DEFAULT", 5)).thenReturn(failedLogs(5));

		sysJobCircuitBreaker.evaluateAfterFailure(1L, "demoJob", "DEFAULT");

		verify(sysJobMapper, never()).updateById(any(SysJobEntity.class));
		verify(sysJobSchedulerManager).pauseJob("demoJob", "DEFAULT");
	}

	private static List<JobLogEntity> failedLogs(int count) {
		return java.util.stream.IntStream.range(0, count).mapToObj(i -> new JobLogEntity().setStatus(false)).toList();
	}

}
