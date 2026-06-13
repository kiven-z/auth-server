package com.auth.service.system.schedule.runner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.Duration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * {@link SysJobBootstrapRunner} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobBootstrapRunner 启动同步")
@ExtendWith(MockitoExtension.class)
class SysJobBootstrapRunnerTest {

	@Mock
	private SysJobBootstrapSynchronizer sysJobBootstrapSynchronizer;

	@InjectMocks
	private SysJobBootstrapRunner sysJobBootstrapRunner;

	@Test
	@DisplayName("应用就绪后应触发异步 Quartz 同步")
	void onApplicationReady_triggersAsyncSync() {
		ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
		ApplicationReadyEvent event = new ApplicationReadyEvent(mock(SpringApplication.class), new String[] {}, context,
				Duration.ZERO);

		sysJobBootstrapRunner.onApplicationReady(event);

		verify(sysJobBootstrapSynchronizer).syncFromDatabase();
	}

}
