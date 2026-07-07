package com.auth.service.system.schedule.support.quartz;

import com.auth.service.system.schedule.model.entity.JobEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysJobSchedulerManager} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobSchedulerManager Quartz 调度写门面")
@ExtendWith(MockitoExtension.class)
class SysJobSchedulerManagerTest {

	@Mock
	private Scheduler scheduler;

	@Mock
	private SysJobQuartzJobFactory sysJobQuartzJobFactory;

	@Mock
	private SysJobQuartzTriggerFactory sysJobQuartzTriggerFactory;

	@InjectMocks
	private SysJobSchedulerManager sysJobSchedulerManager;

	private static JobEntity enabledJob() {
		JobEntity job = new JobEntity();
		job.setId(1L);
		job.setJobName("demo");
		job.setJobGroup("G");
		job.setTaskType("BEAN_INVOKE");
		job.setInvokeTarget("demo.run()");
		job.setCronExpression("0 0 12 * * ?");
		job.setStatus(true);
		job.setConcurrent(false);
		return job;
	}

	@Test
	@DisplayName("pauseJob：任务存在时应调用 scheduler.pauseJob")
	void pauseJob_whenJobExists_pausesSchedulerJob() throws SchedulerException {
		JobKey jobKey = JobKey.jobKey("demoJob", "DEFAULT");
		when(scheduler.checkExists(jobKey)).thenReturn(true);

		sysJobSchedulerManager.pauseJob("demoJob", "DEFAULT");

		verify(scheduler).pauseJob(jobKey);
	}

	@Test
	@DisplayName("pauseGroup：分组编码有效时应调用 scheduler.pauseJobs")
	void pauseGroup_whenGroupCodePresent_pausesSchedulerGroup() throws SchedulerException {
		sysJobSchedulerManager.pauseGroup("G");

		verify(scheduler).pauseJobs(GroupMatcher.groupEquals("G"));
	}

	@Test
	@DisplayName("scheduleOrReplace：暂停且 Quartz 无任务时不注册调度")
	void scheduleOrReplace_whenPausedAndMissing_doesNotSchedule() throws SchedulerException {
		JobEntity job = new JobEntity();
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
		JobEntity job = new JobEntity();
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
		verify(scheduler, never()).pauseJob(any());
	}

	@Test
	@DisplayName("scheduleOrReplace：启用时应删后建并注册调度")
	void scheduleOrReplace_whenEnabled_schedulesJob() throws SchedulerException {
		JobEntity job = enabledJob();
		JobKey jobKey = JobKey.jobKey("demo", "G");
		JobDetail jobDetail = mock(JobDetail.class);
		CronTrigger trigger = mock(CronTrigger.class);
		when(scheduler.checkExists(jobKey)).thenReturn(false);
		when(sysJobQuartzJobFactory.build(job)).thenReturn(jobDetail);
		when(sysJobQuartzTriggerFactory.build(job, jobKey)).thenReturn(trigger);

		sysJobSchedulerManager.scheduleOrReplace(job);

		verify(scheduler).scheduleJob(jobDetail, trigger);
	}

}
