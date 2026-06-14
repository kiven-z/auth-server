package com.auth.service.system.schedule.runner;

import com.auth.service.system.schedule.support.quartz.SysJobScheduleReconciler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.Duration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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

	private SysJobBootstrapSynchronizer sysJobBootstrapSynchronizer;

	private ApplicationReadyEvent applicationReadyEvent;

	@BeforeEach
	void setUp() {
		sysJobBootstrapSynchronizer = new SysJobBootstrapSynchronizer(sysJobScheduleReconciler);
		ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
		applicationReadyEvent = new ApplicationReadyEvent(mock(SpringApplication.class), new String[] {}, context,
				Duration.ZERO);
	}

	@Test
	@DisplayName("应用就绪后应触发全量对账")
	void onApplicationReadyInvokesReconcileAll() {
		sysJobBootstrapSynchronizer.onApplicationReady(applicationReadyEvent);

		verify(sysJobScheduleReconciler).reconcileAll();
	}

}
