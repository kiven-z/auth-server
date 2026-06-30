package com.auth.service.system.schedule.runner;

import java.time.Duration;

import com.auth.service.system.schedule.support.quartz.SysJobScheduleReconciler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysJobBootstrapSynchronizer} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobBootstrapSynchronizer 启动同步")
@ExtendWith(MockitoExtension.class)
class SysJobBootstrapSynchronizerTest {

	@Mock
	private SysJobScheduleReconciler sysJobScheduleReconciler;

	@Mock
	private Scheduler scheduler;

	private SysJobBootstrapSynchronizer sysJobBootstrapSynchronizer;

	private ApplicationReadyEvent applicationReadyEvent;

	@BeforeEach
	void setUp() {
		sysJobBootstrapSynchronizer = new SysJobBootstrapSynchronizer(sysJobScheduleReconciler, scheduler);
		ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
		applicationReadyEvent = new ApplicationReadyEvent(mock(SpringApplication.class), new String[] {}, context,
				Duration.ZERO);
	}

	@Test
	@DisplayName("应用就绪后应先全量对账再启动调度器")
	void onApplicationReadyReconcilesThenStartsScheduler() throws SchedulerException {
		when(scheduler.isStarted()).thenReturn(false);

		sysJobBootstrapSynchronizer.onApplicationReady(applicationReadyEvent);

		verify(sysJobScheduleReconciler).reconcileAll();
		verify(scheduler).start();
	}

	@Test
	@DisplayName("调度器已启动时不应重复 start")
	void onApplicationReadySkipsStartWhenSchedulerAlreadyStarted() throws SchedulerException {
		when(scheduler.isStarted()).thenReturn(true);

		sysJobBootstrapSynchronizer.onApplicationReady(applicationReadyEvent);

		verify(sysJobScheduleReconciler).reconcileAll();
		verify(scheduler, never()).start();
	}

	@Test
	@DisplayName("对账失败时仍应启动调度器")
	void onApplicationReadyStartsSchedulerEvenWhenReconcileFails() throws SchedulerException {
		when(scheduler.isStarted()).thenReturn(false);
		doThrow(new RuntimeException("reconcile failed")).when(sysJobScheduleReconciler).reconcileAll();

		assertThrows(RuntimeException.class,
				() -> sysJobBootstrapSynchronizer.onApplicationReady(applicationReadyEvent));

		verify(scheduler).start();
	}

	@Test
	@DisplayName("调度器启动失败时应抛出 IllegalStateException")
	void onApplicationReadyWrapsSchedulerStartFailure() throws SchedulerException {
		when(scheduler.isStarted()).thenReturn(false);
		doThrow(new SchedulerException("start failed")).when(scheduler).start();

		assertThrows(IllegalStateException.class,
				() -> sysJobBootstrapSynchronizer.onApplicationReady(applicationReadyEvent));
	}

}
