package com.auth.service.system.schedule.support;

import com.auth.service.system.schedule.handler.SysJobTaskHandlerRegistry;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobQuartzRuntimeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link SysJobSchedulerManager} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobSchedulerManager Quartz 调度管理")
@ExtendWith(MockitoExtension.class)
class SysJobSchedulerManagerTest {

	@Mock
	private Scheduler scheduler;

	@Mock
	private SysJobTaskHandlerRegistry taskHandlerRegistry;

	@InjectMocks
	private SysJobSchedulerManager sysJobSchedulerManager;

	@Test
	@DisplayName("pauseJob：任务存在时应调用 scheduler.pauseJob")
	void pauseJob_whenJobExists_pausesSchedulerJob() throws SchedulerException {
		JobKey jobKey = JobKey.jobKey("demoJob", "DEFAULT");
		when(scheduler.checkExists(jobKey)).thenReturn(true);

		sysJobSchedulerManager.pauseJob("demoJob", "DEFAULT");

		verify(scheduler).pauseJob(jobKey);
	}

	@Test
	@DisplayName("scheduleOrReplace：暂停且 Quartz 无任务时不注册调度")
	void scheduleOrReplace_whenPausedAndMissing_doesNotSchedule() throws SchedulerException {
		SysJobEntity job = new SysJobEntity();
		job.setJobName("demo");
		job.setJobGroup("G");
		job.setStatus(false);
		JobKey jobKey = JobKey.jobKey("demo", "G");
		when(scheduler.checkExists(jobKey)).thenReturn(false);

		sysJobSchedulerManager.scheduleOrReplace(job);

		verify(scheduler).checkExists(jobKey);
		verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	@Test
	@DisplayName("scheduleOrReplace：暂停且 Quartz 仍有任务时应 pauseJob")
	void scheduleOrReplace_whenPausedAndExists_pausesJob() throws SchedulerException {
		SysJobEntity job = new SysJobEntity();
		job.setJobName("demo");
		job.setJobGroup("G");
		job.setStatus(false);
		JobKey jobKey = JobKey.jobKey("demo", "G");
		when(scheduler.checkExists(jobKey)).thenReturn(true);

		sysJobSchedulerManager.scheduleOrReplace(job);

		verify(scheduler).pauseJob(jobKey);
		verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	@Test
	@DisplayName("pauseJob：任务不存在时不调用 scheduler.pauseJob")
	void pauseJob_whenJobMissing_skipsPause() throws SchedulerException {
		JobKey jobKey = JobKey.jobKey("missing", "DEFAULT");
		when(scheduler.checkExists(jobKey)).thenReturn(false);

		sysJobSchedulerManager.pauseJob("missing", "DEFAULT");

		verify(scheduler).checkExists(jobKey);
		verifyNoInteractions(taskHandlerRegistry);
	}

	@Test
	@DisplayName("resolveRuntimeSnapshot：Job 不存在时返回 NOT_REGISTERED")
	void resolveRuntimeSnapshotWhenMissingReturnsNotRegistered() throws SchedulerException {
		JobKey jobKey = JobKey.jobKey("missing", "G");
		when(scheduler.checkExists(jobKey)).thenReturn(false);
		when(scheduler.getCurrentlyExecutingJobs()).thenReturn(Collections.emptyList());

		SysJobQuartzRuntimeSnapshot snapshot = sysJobSchedulerManager.resolveRuntimeSnapshot("missing", "G");

		assertThat(snapshot.status()).isEqualTo(SysJobQuartzRuntimeStatus.NOT_REGISTERED);
	}

	@Test
	@DisplayName("resolveRuntimeSnapshot：正在执行时返回 RUNNING")
	void resolveRuntimeSnapshotWhenRunningReturnsRunning() throws SchedulerException {
		JobKey jobKey = JobKey.jobKey("demo", "G");
		when(scheduler.checkExists(jobKey)).thenReturn(true);
		JobExecutionContext context = mock(JobExecutionContext.class);
		JobDetail jobDetail = mock(JobDetail.class);
		when(context.getJobDetail()).thenReturn(jobDetail);
		when(jobDetail.getKey()).thenReturn(jobKey);
		when(context.getFireTime()).thenReturn(new Date());
		when(scheduler.getCurrentlyExecutingJobs()).thenReturn(List.of(context));

		SysJobQuartzRuntimeSnapshot snapshot = sysJobSchedulerManager.resolveRuntimeSnapshot("demo", "G");

		assertThat(snapshot.status()).isEqualTo(SysJobQuartzRuntimeStatus.RUNNING);
		assertThat(snapshot.fireTime()).isNotNull();
	}

	@Test
	@DisplayName("resolveRuntimeSnapshot：Trigger BLOCKED 时返回 PENDING")
	void resolveRuntimeSnapshotWhenBlockedReturnsPending() throws SchedulerException {
		JobKey jobKey = JobKey.jobKey("demo", "G");
		when(scheduler.checkExists(jobKey)).thenReturn(true);
		when(scheduler.getCurrentlyExecutingJobs()).thenReturn(Collections.emptyList());
		Trigger trigger = mock(Trigger.class);
		TriggerKey triggerKey = TriggerKey.triggerKey("demo_TRIGGER", "G");
		when(trigger.getKey()).thenReturn(triggerKey);
		when(scheduler.getTriggersOfJob(jobKey)).thenAnswer(invocation -> List.of(trigger));
		when(scheduler.getTriggerState(triggerKey)).thenReturn(Trigger.TriggerState.BLOCKED);

		SysJobQuartzRuntimeSnapshot snapshot = sysJobSchedulerManager.resolveRuntimeSnapshot("demo", "G");

		assertThat(snapshot.status()).isEqualTo(SysJobQuartzRuntimeStatus.PENDING);
	}

}
