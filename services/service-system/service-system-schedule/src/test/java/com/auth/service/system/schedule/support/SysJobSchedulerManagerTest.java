package com.auth.service.system.schedule.support;

import com.auth.service.system.schedule.handler.SysJobTaskHandlerRegistry;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link SysJobSchedulerManager} 单元测试。
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SysJobSchedulerManager Quartz 调度管理")
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

}
